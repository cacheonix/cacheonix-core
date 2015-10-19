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

import java.util.Map;
import java.util.Set;

import org.cacheonix.cache.Cache;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.ShutdownMode.GRACEFUL_SHUTDOWN;

/**
 * Tests partitioned cache with a front cache.
 *
 * @noinspection ProhibitedExceptionDeclared, ProhibitedExceptionDeclared, ConstantNamingConvention,
 * ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention
 */
public final class MultiplePartitionedCacheWithFrontCacheTest extends MultiplePartitionedCacheTestDriver {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MultiplePartitionedCacheWithFrontCacheTest.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster.
    */
   private static final String[] CONFIGURATIONS = {
           "cacheonix-config-cluster-member-with-front-cache-1.xml",
           "cacheonix-config-cluster-member-with-front-cache-2.xml",
           "cacheonix-config-cluster-member-with-front-cache-3.xml"
   };


   public void testGetAllFromCacheFasterThanFromRemote() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(1));
      }

      // Put to cache
      final Cache<String, String> cache = cacheList.get(0);
      cache.putAll(map);

      final int keySetCount = keyCount - 100;
      final Set<String> subset = new HashSet<String>(keyCount);
      for (int i = 0; i < keySetCount; i++) {
         subset.add(createKey(i));
      }

      // Get from remote
      final long start1 = System.currentTimeMillis();
      cache.getAll(subset);
      final long nonCachedDuration = System.currentTimeMillis() - start1;

      // Get from cache
      final long start2 = System.currentTimeMillis();
      cache.getAll(subset);
      final long cachedDuration = System.currentTimeMillis() - start2;

      // Assert
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("cachedDuration: " + cachedDuration); // NOPMD
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("nonCachedDuration: " + nonCachedDuration); // NOPMD
      assertTrue("Non-cached: " + nonCachedDuration + " ms, cached: " + cachedDuration + " ms", nonCachedDuration / cachedDuration >= 2);
   }


   public void testGetFromFrontCacheIsFasterThanFromRemote() throws Exception {

      // Set up
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(3);

      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Populate
      cache(0).putAll(map);

      // Fill front caches
      final long start1 = System.currentTimeMillis();
      for (int i = 0; i < cacheList.size(); i++) {

         for (final Map.Entry<String, String> entry : map.entrySet()) {

            cache(i).get(entry.getKey());
         }
      }

      final long nonCachedDuration = System.currentTimeMillis() - start1;

      // Read from the front cache
      final long start2 = System.currentTimeMillis();
      for (int i = 0; i < cacheList.size(); i++) {

         for (final Map.Entry<String, String> entry : map.entrySet()) {

            cache(i).get(entry.getKey());
         }
      }
      final long cachedDuration = System.currentTimeMillis() - start2;

      assertTrue("Non-cached: " + nonCachedDuration + " ms, cached: " + cachedDuration + " ms", nonCachedDuration / cachedDuration >= 4);
   }


   public void testRepartitioningOccursOnNodeLeaving() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> sourceMap = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         sourceMap.put(createKey(i), createValue(1));
      }

      // Put using the last cache
      cache(2).putAll(sourceMap);

      // Populate local cache of node # 2
      for (final Map.Entry<String, String> entry : sourceMap.entrySet()) {
         assertEquals(entry.getValue(), cache(2).get(entry.getKey()));
      }


      // Shutdown node # 0
      cacheonix().shutdown(GRACEFUL_SHUTDOWN, true);

      // Assert there is less data left
      final Map<String, String> newMap = cache(2).getAll(sourceMap.keySet());

      assertTrue(sourceMap.equals(newMap));
   }


   public MultiplePartitionedCacheWithFrontCacheTest() {

      super(CONFIGURATIONS);
   }
}
