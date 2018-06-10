package org.cacheonix.impl.cache.distributed.partitioned;

import java.util.Map;
import java.util.Set;

import org.cacheonix.cache.Cache;
import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;

/**
 * Created by vimeshev on 2/7/16.
 */
public class MultiplePartitionedCacheWithReplicasTestCase extends MultiplePartitionedCacheTestCase {

   public MultiplePartitionedCacheWithReplicasTestCase(final String[] configurations) {

      super(configurations);
   }


   /**
    * This method overwrites driver's becuase with GetRequest and GetAllRequest reading from the local buckets and
    * repartitioning between replicas in process statistics behaves unpredictably. This method removes volitile
    * asserts.
    */
   public void testGetStatistics() {

      // Set up
      final Map<String, String> map = new HashMap<String, String>(3);
      final Cache<String, String> cache = cacheList.get(0);
      for (int i = 0; i < MAX_SIZE; i++) {
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
}
