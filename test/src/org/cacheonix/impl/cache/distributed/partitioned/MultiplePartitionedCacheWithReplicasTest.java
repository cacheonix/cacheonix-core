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
import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tests clustered cache
 *
 * @noinspection ProhibitedExceptionDeclared, ProhibitedExceptionDeclared, ConstantNamingConvention,
 * ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention
 */
public final class MultiplePartitionedCacheWithReplicasTest extends MultiplePartitionedCacheTestDriver {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MultiplePartitionedCacheWithReplicasTest.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster.
    */
   private static final String[] CONFIGURATIONS = {
           "cacheonix-config-cluster-member-w-replicas-1.xml",
           "cacheonix-config-cluster-member-w-replicas-2.xml",
           "cacheonix-config-cluster-member-w-replicas-3.xml",
   };


   public void testCoherenceGetAllClear() throws Exception { // NOPMD

      super.testCoherenceGetAllClear();
   }


   /**
    * This method overwrites driver's becuase with GetRequest and GetAllRequest reading from the local buckets and
    * repartitioning between replicas in process statistics behaves unpredictably. This method removes volitile
    * asserts.
    */
   public void testGetStatistics() {

      // Set up
      final int keyCount = MAX_SIZE;
      final Map<String, String> map = new HashMap<String, String>(3);
      final Cache<String, String> cache = cacheList.get(0);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Add
      cache.putAll(map);

      // Get statistics immediately after write and assert
      final CacheStatistics statistics = cache.getStatistics();

      // Read stats
      assertEquals(0, statistics.getElementsOnDiskCount());
      assertEquals(0, statistics.getReadHitCount());
      assertEquals(0, statistics.getReadMissCount());
      assertEquals(0.0f, statistics.getReadHitRatio());
      assertEquals(0.0f, statistics.getReadMissRatio());

      // Write stats
      assertEquals(0, statistics.getWriteHitCount());
      assertEquals(MAX_SIZE, statistics.getWriteMissCount());
      assertEquals(0.0f, statistics.getWriteHitRatio());
      assertEquals(1.0f, statistics.getWriteMissRatio());

      // Do read
      final int readSize = MAX_SIZE / 100;
      final Set<String> readSet = new HashSet<String>(readSize);
      for (int i = 0; i < readSize; i++) {
         readSet.add(createKey(i));

      }
      cache.getAll(readSet);

      final CacheStatistics readStats = cache.getStatistics();

      // Read stats
      assertEquals(0, readStats.getElementsOnDiskCount());
      assertEquals(0, readStats.getReadMissCount());
      assertEquals(1f, readStats.getReadHitRatio());
      assertEquals(0f, readStats.getReadMissRatio());

      // Write stats
      assertEquals(0, readStats.getWriteHitCount());
      assertEquals(MAX_SIZE, readStats.getWriteMissCount());
      assertEquals(0.0f, readStats.getWriteHitRatio());
      assertEquals(1.0f, readStats.getWriteMissRatio());


      // Force eviction
      for (int i = MAX_SIZE; i < MAX_SIZE + MAX_SIZE * 0.1; i++) {
         map.put(createKey(i), createValue(i));
      }
   }


   public MultiplePartitionedCacheWithReplicasTest() {

      super(CONFIGURATIONS);
   }
}
