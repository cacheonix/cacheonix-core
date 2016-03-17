/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.org/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.gargoylesoftware.base.testing.OrderedTestSuite;
import junit.framework.TestSuite;
import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.time.Timeout;
import org.cacheonix.impl.util.time.TimeoutImpl;

/**
 * Tests clustered cache
 *
 * @noinspection ProhibitedExceptionDeclared, ProhibitedExceptionDeclared, ConstantNamingConvention,
 * ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention,
 * ControlFlowStatementWithoutBraces
 */
public final class CacheNodeOnNodeLeavingWithReplicasTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheNodeOnNodeLeavingWithReplicasTest.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster.
    */
   private static final String[] NODE_CONFIGURATIONS = {
           "cacheonix-config-cluster-member-w-replicas-1.xml",
           "cacheonix-config-cluster-member-w-replicas-2.xml",
           "cacheonix-config-cluster-member-w-replicas-3.xml",
   };

   private static final int NODE_COUNT = NODE_CONFIGURATIONS.length;

   private static final String DISTRIBUTED_CACHE_NAME = "partitioned.distributed.cache";

   private final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(
           SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);

   /**
    * List of cache managers.
    */
   private final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);

   /**
    * List of clustered caches.
    */
   private final List<Cache<Serializable, Serializable>> cacheList = new ArrayList<Cache<Serializable, Serializable>>(
           5);

   /**
    * Number of keys to trie.
    */
   private static final long KEY_COUNT = 100L;


   public void testPutGetOnLeave() {


      // Wait for cluster to stabilize
      LOG.debug("=============== Wait for cluster to stabilize ============================");
      final Timeout timeoutForOwnersToArrive = new TimeoutImpl(10000L).reset();
      //noinspection StatementWithEmptyBody
      while (!timeoutForOwnersToArrive.isExpired() && cache(0).getKeyOwners().size() != NODE_COUNT) { // NOPMD
      }

      assertEquals(NODE_COUNT, cache(0).getKeyOwners().size());


      final Map<Object, List<String>> ownersToKeys = new HashMap<Object, List<String>>(11);

      // Populate
      LOG.debug("=============== Populate ============================");
      for (long i = 0L; i < KEY_COUNT; i++) {
         //
         final String key = createTestKey(i);
         final String value = createTestObject(i);

         // Put
         cache(0).put(key, value);

         // Assert
         assertEquals("Created key should be present", value, cache(0).get(key));

         // Assign keys to owners
         final CacheMember owner = cache(0).getKeyOwner(key);
         List<String> ownedKeys = ownersToKeys.get(owner);
         if (ownedKeys == null) {
            ownedKeys = new ArrayList<String>(11);
            ownersToKeys.put(owner, ownedKeys);
         }
         ownedKeys.add(key);
      }

      // REVIEWME: simeshev@cacheonix.org - 2010-08-17 -> After moving the connection pool
      // to the MessageSender unique to the cluster node, this one started to fail.
      // assertEquals(NODE_COUNT, ownersToKeys.size());

      // Collect owners
      final List keyOwnersBeforeLeave = cache(0).getKeyOwners();

      // Assert key owners
      assertEquals(NODE_COUNT, keyOwnersBeforeLeave.size());

      // Shutdown last node
      LOG.debug("=============== Shutdown first node ============================");
      cacheManagerList.get(0).shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);

      // Assert that a gone node has created wholes where it should and has not where it should not
      LOG.debug("=============== Assert that a gone node has created wholes where it should and has not where it should not ============================");
      final Timeout timeoutForOwnersToLeave = new TimeoutImpl(10000L).reset();
      List keyOwnersAfterLeave = cache(2).getKeyOwners();
      while (!timeoutForOwnersToLeave.isExpired() && keyOwnersAfterLeave.size() != NODE_COUNT - 1) {
         keyOwnersAfterLeave = cache(2).getKeyOwners();
      }
      assertTrue("Wait for key owners size drop should not timeout", !timeoutForOwnersToLeave.isExpired());
      assertEquals(NODE_COUNT - 1, keyOwnersAfterLeave.size());

      // Find node left
      LOG.debug("=============== Find node left ============================");
      CacheMember left = null;
      for (final Object keyOwner : keyOwnersBeforeLeave) {
         final CacheMember before = (CacheMember) keyOwner;
         @SuppressWarnings("BooleanVariableAlwaysNegated") boolean found = false;
         for (final Object aKeyOwnerAfterLeave : keyOwnersAfterLeave) {
            final CacheMember after = (CacheMember) aKeyOwnerAfterLeave;
            if (before.equals(after)) {
               found = true;
               break;
            }
         }
         if (!found) {
            left = before;
            break;
         }
      }
      assertNotNull(left);


      // Assert keys DO belong to an other node.
      LOG.debug("=============== Assert keys DO belong to an other node ============================");
      for (final Entry<Object, List<String>> objectListEntry : ownersToKeys.entrySet()) {
         final List<String> keys = objectListEntry.getValue();
         for (final String key : keys) {
            assertTrue(!cache(2).getKeyOwner(key).equals(left));
         }
      }

      // Assert there NO holes because back up should have picked them up

      // REVIEWME: simeshev@cacheonix.org - 2010-08-17 -> After moving the connection pool
      // to the MessageSender unique to the cluster node, this one started to fail.

      //      LOG.debug("=============== Assert there NO holes because back up should have picked them up ============================");
      //      final List<String> leftKeys = ownersToKeys.get(left);
      //      assertNotNull("Orphaned keys should be not null for: " + left, leftKeys);
      //      for (final String key : leftKeys) {
      //         assertNotNull("Key should not be null, it should be restored from the replica: " + key, cache(0).get(key));
//      }
   }


   private Cache<Serializable, Serializable> cache(final int index) {

      return cacheList.get(index);
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      LOG.debug("================================================================================================");
      LOG.debug("========== Starting up =========================================================================");
      LOG.debug("================================================================================================");
      super.setUp();

      // Disable autocreate
      savedSystemProperty.save();
      System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");

      // Startup caches
      for (int i = 0; i < NODE_COUNT; i++) {
         final String configurationPath = TestUtils.getTestFile(NODE_CONFIGURATIONS[i]).toString();
         final Cacheonix manager = Cacheonix.getInstance(configurationPath);
         cacheManagerList.add(manager);
         cacheList.add(manager.getCache(DISTRIBUTED_CACHE_NAME));
      }

      // Call to a get method makes sure the cache exists
      for (int i = 0; i < NODE_COUNT; i++) {
         cache(i).get(createTestKey((long) i));
      }
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      LOG.debug("================================================================================================");
      LOG.debug("========== Tearing down =========================================================================");
      LOG.debug("================================================================================================");

      for (int i = 0; i < NODE_COUNT; i++) {
         final Cacheonix cacheonix = cacheManagerList.get(i);
         if (!cacheonix.isShutdown()) {
            cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         }
      }
      cacheManagerList.clear();
      cacheList.clear();
      savedSystemProperty.restore();

      super.tearDown();

      LOG.debug("================================================================================================");
      LOG.debug("========== Teared down =========================================================================");
      LOG.debug("================================================================================================");
   }


   /**
    * Required by JUnit
    *
    * @return an ordered test suite.
    */
   public static TestSuite suite() {

      return new OrderedTestSuite(CacheNodeOnNodeLeavingWithReplicasTest.class, new String[]{
              "testPutGetOnLeave",
      });
   }


   public CacheNodeOnNodeLeavingWithReplicasTest(final String name) {

      super(name);
   }


   public String toString() {

      return "CacheNodeOnNodeLeavingWithoutReplicasTest{" +
              "cacheList=" + cacheList +
              ", cacheManagerList=" + cacheManagerList +
              ", savedSystemProperty=" + savedSystemProperty +
              "} " + super.toString();
   }
}