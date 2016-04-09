/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.time.Timeout;
import org.cacheonix.impl.util.time.TimeoutImpl;

/**
 * Tests clustered cache
 *
 * @noinspection ProhibitedExceptionDeclared, ProhibitedExceptionDeclared, ConstantNamingConvention,
 * ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention,
 * JUnitTestCaseWithNonTrivialConstructors
 */
public abstract class CacheNodeOnNodeLeavingWithoutReplicasTestDriver extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheNodeOnNodeLeavingWithoutReplicasTestDriver.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster.
    */
   private final String[] configurations;

   private final int nodeCount;

   private static final String DISTRIBUTED_CACHE_NAME = "partitioned.distributed.cache";

   private final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);

   /**
    * List of cache managers.
    */
   private final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);

   /**
    * List of clustered caches.
    */
   private final List<Cache<String, String>> cacheList = new ArrayList<Cache<String, String>>(5);

   /**
    * Number of keys to try.
    */
   private static final int KEY_COUNT = 10000;


   /**
    * No-arg constructor to enable serialization. This method is not intended to be used by mere mortals without calling
    * setName().
    *
    * @param configurations configurations
    */
   public CacheNodeOnNodeLeavingWithoutReplicasTestDriver(final String[] configurations) {

      this.configurations = ArrayUtils.copy(configurations);
      this.nodeCount = configurations.length;
   }


   public void testPutGetOnLeave() throws InterruptedException {

      final Map<Object, List<String>> ownersToKeys = new HashMap<Object, List<String>>(11);

      // Wait for cluster to stabilize
      LOG.debug("================================================================================================");
      LOG.debug("=============== Wait for cluster to stabilize ==================================================");
      LOG.debug("================================================================================================");
      final Timeout timeoutForOwnersToArrive = new TimeoutImpl(10000L).reset();
      while (!timeoutForOwnersToArrive.isExpired() && cache(0).getKeyOwners().size() != nodeCount) {
         Thread.sleep(100L);
      }
      assertEquals(nodeCount, cache(0).getKeyOwners().size());

      // Populate
      final long startPutTiming = System.currentTimeMillis();
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
      LOG.info("Time to put " + KEY_COUNT + " keys: " + (System.currentTimeMillis() - startPutTiming) + "ms");
      assertEquals(nodeCount, ownersToKeys.size());

      // Collect owners
      final List keyOwnersBeforeLeave = cache(0).getKeyOwners();

      // Assert key owners
      assertEquals(nodeCount, keyOwnersBeforeLeave.size());

      // Shutdown last node
      LOG.debug("================================================================================================");
      LOG.debug("========== Shutting down last node =============================================================");
      LOG.debug("================================================================================================");
      cacheManagerList.get(nodeCount - 1).shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);

      LOG.debug("================================================================================================");
      LOG.debug("========== Waiting for key owner count to drop down ============================================");
      LOG.debug("================================================================================================");
      // Assert that a gone node has created wholes where it should and has not where it should not
      final Timeout timeout = new TimeoutImpl(10000L).reset();
      List keyOwnersAfterLeave = cache(0).getKeyOwners();
      while (!timeout.isExpired() && keyOwnersAfterLeave.size() != configurations.length - 1) {
         Thread.sleep(10L);
         keyOwnersAfterLeave = cache(0).getKeyOwners();
      }
      assertTrue("Wait for key owners sise drop should not timeout", !timeout.isExpired());
      assertEquals(configurations.length - 1, keyOwnersAfterLeave.size());

      // Find node left
      CacheMember left = null;
      for (final Object keyOwner : keyOwnersBeforeLeave) {
         final CacheMember before = (CacheMember) keyOwner;
         boolean found = false;
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

      // Assert there are holes
      final List<String> leftKeys = ownersToKeys.get(left);
      assertNotNull("Orphaned keys should be not null for: " + left, leftKeys);
      for (final String key : leftKeys) {
         assertNotNull(cache(0).get(key));
      }


      // Assert no keys belong to the left node any more.
      for (final Map.Entry<Object, List<String>> objectListEntry : ownersToKeys.entrySet()) {
         final List<String> keys = (objectListEntry).getValue();
         for (final String key : keys) {
            assertTrue(!cache(0).getKeyOwner(key).equals(left));
         }
      }
   }


   private Cache<String, String> cache(final int index) {

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
      for (int i = 0; i < nodeCount; i++) {
         final String configurationPath = TestUtils.getTestFile(configurations[i]).toString();
         final Cacheonix manager = Cacheonix.getInstance(configurationPath);
         cacheManagerList.add(manager);
         final Cache<String, String> cache = manager.getCache(DISTRIBUTED_CACHE_NAME);
         assertNotNull("Cache # " + i + " should be not null", cache);
         cacheList.add(cache);
      }

      // Call to a get method makes sure the cache exists
      for (int i = 0; i < nodeCount; i++) {
         cache(i).get(createTestKey(i));
      }
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      for (int i = 0; i < nodeCount; i++) {
         final Cacheonix cacheonix = cacheManagerList.get(i);
         if (!cacheonix.isShutdown()) {
            cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         }
      }
      cacheList.clear();
      savedSystemProperty.restore();

      super.tearDown();
      LOG.debug("================================================================================================");
      LOG.debug("========== Teared down =========================================================================");
      LOG.debug("================================================================================================");
   }


   public String toString() {

      return "CacheNodeOnNodeLeavingWithoutReplicasTest{" +
              "cacheList=" + cacheList +
              ", cacheManagerList=" + cacheManagerList +
              ", savedSystemProperty=" + savedSystemProperty +
              "} " + super.toString();
   }
}