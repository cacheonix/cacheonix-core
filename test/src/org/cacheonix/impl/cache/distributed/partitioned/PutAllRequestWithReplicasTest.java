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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import org.cacheonix.impl.cache.CacheonixCache;
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
public final class PutAllRequestWithReplicasTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PutAllRequestWithReplicasTest.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster node.
    */
   private static final String[] NODE_CONFIGURATIONS = {
           "cacheonix-config-cluster-member-w-replicas-1.xml",
           "cacheonix-config-cluster-member-w-replicas-2.xml",
           "cacheonix-config-cluster-member-w-replicas-3.xml",
   };

   private static final int NODE_COUNT = NODE_CONFIGURATIONS.length;

   private static final String DISTRIBUTED_CACHE_NAME = "partitioned.distributed.cache";

   /**
    * List of cache managers.
    */
   private final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);

   /**
    * List of clustered caches.
    */
   private final List<CacheonixCache> cacheList = new ArrayList<CacheonixCache>(5);

   /**
    * Number of keys to try.
    */
   private static final long KEY_COUNT = 10000L;


   public void testPutAll() throws InterruptedException {


      // Wait for cluster to stabilize
      LOG.debug("================================================================================================");
      LOG.debug("=============== Wait for cluster to stabilize ==================================================");
      LOG.debug("================================================================================================");
      final Timeout timeoutForOwnersToArrive = new TimeoutImpl(10000L).reset();
      while (!timeoutForOwnersToArrive.isExpired() && cache(0).getKeyOwners().size() != NODE_COUNT) {
         Thread.sleep(100L);
      }

      Thread.sleep(5000L);

      assertEquals(NODE_COUNT, cache(0).getKeyOwners().size());

      LOG.debug("================================================================================================");
      LOG.debug("=============== putAll() =======================================================================");
      LOG.debug("================================================================================================");
      final Map<String, String> map = new HashMap<String, String>(3);
      final Cache cache = cache(0);
      for (int i = 0; i < (int) KEY_COUNT; i++) {
         map.put(TEST_KEY_PREFIX + i, TEST_OBJECT_PREFIX + i);
      }

      final long begin = System.currentTimeMillis();
      cache.putAll(map);
      LOG.debug("================================================================================================");
      LOG.debug("=============== Finished putAll(), time: " + (System.currentTimeMillis() - begin) + " ms ===============================================");
      LOG.debug("================================================================================================");

      // Assert
      for (int i = 1; i < cacheList.size(); i++) {
         final Cache remoteCache = cache(i);
         for (int j = 0; j < (int) KEY_COUNT; j++) {
            assertEquals("Object not found in cache " + remoteCache.getName(), TEST_OBJECT_PREFIX + j, remoteCache.get(TEST_KEY_PREFIX + j));
         }
      }
   }


   private CacheonixCache cache(final int index) {

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

      // Startup caches
      for (int i = 0; i < NODE_COUNT; i++) {
         final String configurationPath = TestUtils.getTestFile(NODE_CONFIGURATIONS[i]).toString();
         final Cacheonix manager = Cacheonix.getInstance(configurationPath);
         cacheManagerList.add(manager);
         cacheList.add((CacheonixCache) manager.getCache(DISTRIBUTED_CACHE_NAME));
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

      for (int i = 0; i < NODE_COUNT; i++) {
         (cacheManagerList.get(i)).shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      }
      cacheManagerList.clear();
      cacheList.clear();

      super.tearDown();
      LOG.debug("================================================================================================");
      LOG.debug("========== Teared down =========================================================================");
      LOG.debug("================================================================================================");
   }


   public PutAllRequestWithReplicasTest(final String name) {

      super(name);
   }


   public String toString() {

      return "CacheNodeOnNodeLeavingWithoutReplicasTest{" +
              "cacheList=" + cacheList +
              ", cacheManagerList=" + cacheManagerList +
              "} " + super.toString();
   }
}