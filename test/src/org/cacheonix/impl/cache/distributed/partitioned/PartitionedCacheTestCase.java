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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.RuntimeInterruptedException;
import org.cacheonix.impl.cache.local.LocalCache;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.lock.DistributedLock;
import org.cacheonix.impl.util.ArgumentValidator;
import org.cacheonix.impl.util.MutableBoolean;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.locks.BrokenLockException;
import org.cacheonix.locks.DeadlockException;
import org.cacheonix.locks.Lock;
import org.cacheonix.locks.ReadWriteLock;

/**
 * Tests clustered cache
 *
 * @noinspection ProhibitedExceptionDeclared, ProhibitedExceptionDeclared, ConstantNamingConvention,
 * ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention,
 * JUnitTestCaseWithNonTrivialConstructors
 */
public abstract class PartitionedCacheTestCase extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PartitionedCacheTestCase.class); // NOPMD

   protected static final String DISTRIBUTED_CACHE_NAME = "partitioned.distributed.cache";

   protected static final String KEY_0 = createTestKey(0);

   protected static final String KEY_1 = createTestKey(1);

   protected static final String OBJECT_0 = createTestObject(0);

   protected static final String OBJECT_1 = createTestObject(1);

   protected static final String OBJECT_2 = createTestObject(2);

   protected static final String NEVER_EXISTED_KEY = createTestKey(Long.MAX_VALUE);

   protected static final String TEST_KEY = createTestKey();

   protected static final String TEST_OBJECT = createTestObject();

   static final int MAX_SIZE = 100000;

   private final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(
           SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);

   /**
    * Number of keys to get.
    */
   private int singleKeyPerformanceCount;


   public void testGet() {

      cache().put(TEST_KEY, TEST_OBJECT);
      assertEquals("Cache " + cache() + " should find object ", TEST_OBJECT, cache().get(TEST_KEY));
   }


   /**
    * Tests that get(Object) handles situation when a local cache holds an element in Invalid state.
    */
   public void testGetInvalid() {

      assertNull(cache().put(KEY_0, OBJECT_0));
      assertEquals(OBJECT_0, cache().put(KEY_0, OBJECT_1));
      assertEquals(OBJECT_1, cache().get(KEY_0));
   }


   /**
    * Tests that get(Object) handles situation when a local cache holds an element in Exclusive state.
    */
   public void testGetExclusive() {

      cache().put(KEY_0, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));
   }


   /**
    */
   public void testGetOwner() {

      // Simple compare
      assertEquals(cache().getKeyOwner(KEY_0), cache().getKeyOwner(KEY_0));
      assertEquals(cache().getKeyOwner(KEY_1), cache().getKeyOwner(KEY_1));
   }


   public void testGetName() {

      assertEquals(DISTRIBUTED_CACHE_NAME, cache().getName());
   }


   public void testIsEmpty() {

      assertTrue(cache().isEmpty());

      cache().put(KEY_0, TEST_OBJECT);
      cache().put(KEY_1, TEST_OBJECT);
      cache().get(KEY_0);
      assertTrue(!cache().isEmpty());

      cache().remove(KEY_0);
      cache().remove(KEY_1);
      assertTrue(cache().isEmpty());
   }


   public void testGetStatistics() {

      // Set up
      final int keyCount = MAX_SIZE;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      final Cache<String, String> cache = cache();
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

      assertEquals(1000, readStats.getReadHitCount());


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

      assertEquals(1000, readStats.getReadHitCount());
      assertEquals(MAX_SIZE, readStats.getWriteMissCount());
   }


   public void testPut() {

      cache().put(TEST_KEY, TEST_OBJECT);
      assertEquals("Cache " + cache() + " should find object ", TEST_OBJECT, cache().get(TEST_KEY));
   }


   public void testPutMany() throws InterruptedException {

      final int threadCount = 5;
      final int keyCount = 100;

      final Cache<String, String> cache = cache();
      final Thread[] threads = new Thread[threadCount];

      for (int j = 0; j < threadCount; j++) {

         threads[j] = new Thread(new Runnable() {

            public void run() {

               for (int i = 0; i < keyCount; i++) {
                  cache.put(TEST_KEY + i, TEST_OBJECT);
               }
            }
         });
         threads[j].start();
      }

      for (final Thread thread : threads) {
         thread.join();
      }
   }


   public void testPutTwoKeys() {

      // Put different element into each cache
      final Object object = cache().put(KEY_0, OBJECT_0);
      if (LOG.isDebugEnabled()) {
         LOG.debug("object: " + object);
      }
      assertNull(object);
      final Object object1 = cache().put(KEY_1, OBJECT_1);
      assertNull("Object should be null: " + object1, object1);

      // Assert all keys found in all caches
      assertEquals(OBJECT_0, cache().get(KEY_0));
      assertEquals(OBJECT_1, cache().get(KEY_1));
   }


   public void testPutIfAbsent() {

      // Put to empty, should get null back
      final String previousValue = cache().putIfAbsent(KEY_0, OBJECT_0);
      assertNull(previousValue);
      assertEquals(OBJECT_0, cache().get(KEY_0));

      // Put to existing, the value should remain unchanged
      final String newPreviousValue = cache().putIfAbsent(KEY_0, OBJECT_1);
      assertEquals(newPreviousValue, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));
   }


   public void testPutManyKeys() {

      // Set up
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      final Cache<String, String> cache = cache();
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      for (final Map.Entry<String, String> entry : map.entrySet()) {
         cache.put(entry.getKey(), entry.getValue());
      }

      // Assert
      assertEquals(map.size(), cache.size());
      final Set<Map.Entry<String, String>> entries = map.entrySet();
      for (final Map.Entry<String, String> entry : entries) {
         assertEquals("Object not found in cache " + cache.getName(), entry.getValue(), cache.get(entry.getKey()));
      }
   }


   /**
    * Tests put into the item in invalid state.
    */
   public void testPutToInvalid() {

      cache().put(KEY_0, OBJECT_0);
      cache().put(KEY_0, OBJECT_1);

      // Put into cache #0 in Invalid state
      assertNotNull(cache().put(KEY_0, OBJECT_2));
      assertEquals(OBJECT_2, cache().get(KEY_0));

      assertNotNull(cache().put(KEY_0, OBJECT_1));
      assertEquals(OBJECT_1, cache().get(KEY_0));
   }


   public void testSize() {

      assertEquals(0, cache().size());

      cache().put(KEY_0, OBJECT_0);
      cache().put(KEY_1, OBJECT_1);

      // Populate local caches
      cache().get(KEY_0);
      cache().get(KEY_1);

      assertEquals(2, cache().size());
   }


   public void testClear() {

      // Put different element into each cache
      final Cache<String, String> cache = cache();
      cache.put(KEY_0, OBJECT_0);
      cache.put(KEY_1, OBJECT_1);

      // Make sure size is zero
      assertEquals("Size for " + cache.getName(), 2, cache.size());

      // Populate local caches
      cache.get(KEY_0);
      cache.get(KEY_1);

      // Clear on one
      cache.clear();

      // Make sure size is zero
      assertEquals("Size for " + cache.getName(), 0, cache.size());
      assertNull(cache.get(KEY_0));
      assertNull(cache.get(KEY_1));
   }


   public void testContainsKey() {

      // Put different
      cache().put(createKey(0), createValue(0));

      // Assert each element is available in all caches.
      final String key = createKey(0);
      assertTrue("Cache " + cache().getName() + " contains key " + key, cache().containsKey(key));
   }


   public void testContainsValue() {

      // Put different element into each cache
      final Cache<String, String> cache = cache();
      cache.put(createKey(0), createValue(0));

      // Assert each element is available in all caches.
      final String value = createValue(0);
      final boolean found = cache.containsValue(value);
      assertTrue("Cache " + cache.getName() + " should contain value", found);
   }


   public void testContainsNullValue() {

      // Put different element into each cache
      cache().put(createKey(0), null);

      // Assert each element is available in all caches.
      assertTrue("Cache  should contain value", cache().containsValue(null));
   }


   public void testValues() {

      // Put different element into each cache
      final int valuesSize = 5000;
      final Cache<String, String> cache = cache();
      for (int i = 0; i < valuesSize; i++) {
         cache.put(createKey(i), createValue(i));
      }

      // Assert each element is available in all caches.
      final Collection<String> values = cache.values();
      assertEquals(valuesSize, values.size());
      for (final Object value : values) {
         assertTrue(value.getClass().toString(), String.class.isAssignableFrom(value.getClass()));
      }
   }


   public void testPutAll() throws InterruptedException {

      // Set up
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      final Set<Map.Entry<String, String>> entries = map.entrySet();

      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Assert

      final Set<Map.Entry<String, String>> cacheEntries = cache.entrySet();

      assertEquals(entries.size(), cacheEntries.size());
      assertEquals(entries, cacheEntries);

      final StringBuilder builder = new StringBuilder(10);
      for (final Map.Entry<String, String> entry : entries) {

         final String value = cache.get(entry.getKey());

         if (!entry.getValue().equals(value)) {
            builder.append(entry.getValue()).append('/').append(value).append(' ');
         }
      }

      assertEquals("Object not found in cache: " + builder, 0, builder.length());
   }


   public void testGetSingleKeyPerformance() throws ExecutionException, InterruptedException {

      executeGetSingleKeyPerformance(1);
   }


   public void testGetSingleKeyParallelPerformance() throws ExecutionException, InterruptedException {

      final int availableProcessors = Runtime.getRuntime().availableProcessors() * 4;
      final int threadCount = availableProcessors == 0 ? 1 : availableProcessors;

      executeGetSingleKeyPerformance(threadCount);
   }


   private final void executeGetSingleKeyPerformance(final int threadCount) throws InterruptedException {


      // Prepare
      final Cache<String, String> cache = cache();
      cache.put(KEY_0, createValue(0));

      final List<Thread> readerTreads = new ArrayList<Thread>(threadCount);

      for (int i = 0; i < threadCount; i++) {

         readerTreads.add(new Thread(new Runnable() {

            public void run() {

               for (int j = 0; j < singleKeyPerformanceCount; j++) {
                  cache.get(KEY_0);
               }
            }
         }, "ReaderThread"));
      }


      // Mark the beginning of the test
      final long start = System.currentTimeMillis();

      // Start
      for (final Thread readerTread : readerTreads) {

         readerTread.start();
      }

      // Finish
      for (final Thread readerTread : readerTreads) {

         readerTread.join();
      }

      // Mark the end of the test
      final long end = System.currentTimeMillis();

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) {

         final long durationMillis = end - start;
         final long latencyMicros = TimeUnit.MILLISECONDS.toMicros(durationMillis) / singleKeyPerformanceCount;
         final double latencyMillis = (double) latencyMicros / (1000.0);
         LOG.debug("---------------------"); // NOPMD
         LOG.debug(
                 "Time test took to execute " + singleKeyPerformanceCount + " gets: " + TimeUnit.MILLISECONDS.toSeconds(
                         durationMillis) + " secs"); // NOPMD
         LOG.debug(
                 "Single client throughput for " + singleKeyPerformanceCount + " gets: " + (1000L * singleKeyPerformanceCount) / durationMillis + " op/sec"); // NOPMD
         LOG.debug(
                 "Total client throughput for " + singleKeyPerformanceCount + " gets, " + threadCount + " threads: " + (1000L * singleKeyPerformanceCount * threadCount) / durationMillis + " op/sec"); // NOPMD
         LOG.debug("Latency: " + latencyMillis + " ms, or " + latencyMicros + " mks");
         LOG.debug("---------------------"); // NOPMD
      }
   }


   /**
    * Sets a counter for the single key get performance.
    *
    * @param singleKeyPerformanceCount counter for the single key get performance.
    * @see #testGetSingleKeyPerformance()
    * @see #testGetSingleKeyParallelPerformance()
    */
   public final void setSingleKeyPerformanceCount(final int singleKeyPerformanceCount) {

      ArgumentValidator.validateArgumentGTZero(singleKeyPerformanceCount, "singleKeyPerformanceCount");

      this.singleKeyPerformanceCount = singleKeyPerformanceCount;
   }


   public void testPutAllWithEmptyMap() {

      // Set up
      final Map<String, String> map = new HashMap<String, String>(0);

      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Assert
      assertTrue(cache.isEmpty());
   }


   public void testEntrySet() {

      // Set up
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      final Cache<String, String> cache = cache();
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }
      cache.putAll(map);

      // Assert
      final Set<Map.Entry<String, String>> result = cache.entrySet();
      for (final Object anEntry : result) {
         final Map.Entry entry = (Map.Entry) anEntry;
         //noinspection SuspiciousMethodCalls
         assertEquals(map.get(entry.getKey()), entry.getValue());
      }
   }


   public void testKeySet() {

      // Put different element into each cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      //
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Assert each element is available in all caches.

      final Set<String> result = cache.keySet();
      assertEquals(map.size(), result.size());


      for (final String s : map.keySet()) {
         assertTrue("Result for cache should contain: " + s, result.contains(s));
      }

      for (final Object aString : result) {

         final String s = (String) aString;
         assertTrue("Source for cache should contain: " + s, map.containsKey(s));
      }
   }


   public void testRemove() {

      // Put different element into each cache
      cache().put(KEY_0, OBJECT_0);
      cache().put(KEY_1, OBJECT_1);

      // Validate preparation
      assertNotNull(cache().get(KEY_1));

      // Clear one
      cache().remove(KEY_1);

      // Make sure size is zero
      assertNull(cache().get(KEY_1));

      // Remove never existed
      assertNull(cache().remove(NEVER_EXISTED_KEY));

      // Remove removed
      assertNull(cache().remove(KEY_1));
   }


   public void testAtomicRemove() {

      // ----------
      // - Step 1 -
      // ----------

      // Prepare
      cache().put(KEY_0, OBJECT_0);
      assertNotNull(cache().get(KEY_0));

      // Clear one
      assertTrue(cache().remove(KEY_0, OBJECT_0));

      // Make sure element is gone
      final String value = cache().get(KEY_0);
      assertNull("Expected value to be null butit was: " + value, value);

      // ----------
      // - Step 2 -
      // ----------

      // Prepare
      cache().put(KEY_1, OBJECT_1);
      assertNotNull(cache().get(KEY_1));

      // Try to remove key with a mismatched value�
      assertFalse(cache().remove(KEY_1, OBJECT_0));

      // Assert remove didn't happen
      assertEquals(cache().get(KEY_1), OBJECT_1);
   }


   public void testAtomicReplace() {

      // Prepare
      cache().put(KEY_0, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));

      // Replace one
      assertTrue(cache().replace(KEY_0, OBJECT_0, OBJECT_1));

      // Make sure element was replaced
      assertEquals(OBJECT_1, cache().get(KEY_0));
   }


   public void testAtomicReplaceNonexistent() {


      // Prepare
      cache().put(KEY_0, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));

      // Try to replace key with a mismatched value�
      assertFalse(cache().replace(KEY_0, OBJECT_2, OBJECT_1));

      // Assert replace didn't happen
      assertEquals(cache().get(KEY_0), OBJECT_0);
   }


   public void testAtomicReplaceNullNewValue() {

      // Prepare
      cache().put(KEY_0, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));

      // Try to replace key with a null value�
      assertTrue(cache().replace(KEY_0, OBJECT_0, null));

      // Assert replace happened
      assertNull(cache().get(KEY_0));
   }


   public void testAtomicReplaceNullOldValue() {

      // Prepare
      cache().put(KEY_0, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));

      // Try to replace key with a null value�
      assertFalse(cache().replace(KEY_0, null, OBJECT_1));

      // Assert didn't happen
      assertEquals(OBJECT_0, cache().get(KEY_0));
   }


   public void testReplaceIfPresentExistent() {

      // Prepare
      cache().put(KEY_0, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));

      // Replace existing
      assertEquals(OBJECT_0, cache().replace(KEY_0, OBJECT_1));
   }


   public void testReplaceIfPresentNonexistent() {


      // Replace non-existent�
      assertNull(cache().replace(KEY_0, OBJECT_0));

      // Assert replace happened
      assertNull(cache().get(KEY_0));
   }


   public void testReplaceIfPresentNullNewValue() {

      // Prepare
      cache().put(KEY_0, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));

      // Replace existing
      assertEquals(OBJECT_0, cache().replace(KEY_0, null));
      assertNull(cache().get(KEY_0));
   }


   public void testReplaceIfPresentNullOldValue() {


      // Prepare
      cache().put(KEY_0, null);
      assertNull(cache().get(KEY_0));

      // Replace existing
      assertNull(cache().replace(KEY_0, OBJECT_0));
      assertEquals(OBJECT_0, cache().get(KEY_0));
   }


   public void testGetMaxSize() {

      assertEquals(MAX_SIZE, cache().getMaxSize());
   }


   public void testGetSizeOnDisk() {

      assertEquals(0L, cache().getSizeOnDisk());
   }


   /**
    * Tests getKeyOwner().
    */
   public void testGetKeyOwner() {

      final CacheMember keyOwner = cache().getKeyOwner(KEY_0);
      assertNotNull(keyOwner);
   }


   /**
    *
    */
   public void testExecute() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(1));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Create executable and aggregator.
      final Executable executable = new Executable();
      final Aggregator aggregator = new Aggregator();

      final Integer result = (Integer) cache.execute(executable, aggregator);
      assertEquals(keyCount, result.intValue());
   }


   /**
    *
    */
   public void testExecuteWithFilter() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(1));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Create executable and aggregator.
      final EntryFilter filter = new EvenEntryFilter();
      final Executable executable = new Executable();
      final Aggregator aggregator = new Aggregator();

      final Integer result = (Integer) cache.execute(filter, executable, aggregator);
      assertEquals(keyCount / 2, result.intValue());
   }


   /**
    *
    */
   public void testGetAll() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(1));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      final int keySetCount = keyCount - 100;
      final Set<String> subset = new HashSet<String>(keyCount);
      for (int i = 0; i < keySetCount; i++) {
         subset.add(createKey(i));
      }

      // Remove and assert
      final Map result = cache.getAll(subset);
      assertEquals(subset.size(), result.size());
      assertEquals(subset, result.keySet());
   }


   /**
    * @throws InterruptedException if the execution was interrupted.
    */
   public void testGetAllHandlesNullValues() throws InterruptedException {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), null);
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      final int keySetCount = keyCount - 100;
      final Set<String> subset = new HashSet<String>(keyCount);
      for (int i = 0; i < keySetCount; i++) {
         subset.add(createKey(i));
      }

      // Remove and assert
      final Map result = cache.getAll(subset);
      assertEquals(subset.size(), result.size());
      assertEquals(subset, result.keySet());
   }


   /**
    *
    */
   public void testGetAllHandlesMissingKeys() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      final int keySetCount = keyCount + 100;
      final Set<String> subset = new HashSet<String>(keyCount);
      for (int i = keyCount - 1; i < keySetCount; i++) {
         subset.add(createKey(i));
      }

      // Remove and assert
      final Map<String, String> result = cache.getAll(subset);
      assertEquals(1, result.size());

      final Set<Map.Entry<String, String>> entries = result.entrySet();
      final Iterator<Map.Entry<String, String>> iterator = entries.iterator();
      final Map.Entry<String, String> next = iterator.next();
      assertEquals(createKey(keyCount - 1), next.getKey());
      assertEquals(createValue(keyCount - 1), next.getValue());
   }


   /**
    *
    */
   public void testGetAllWithEmptySubset() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {

         map.put(createKey(i), createValue(1));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Create empty set
      final Set<String> subset = new HashSet<String>(0);

      // Remove and assert
      final Map result = cache.getAll(subset);
      assertEquals(subset.size(), result.size());
      assertEquals(subset, result.keySet());
   }


   /**
    *
    */
   public void testRetainAll() {

      // Prepare test data
      final int keyCount = 10000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Prepare key set to retain
      final int keySetCount = keyCount - 1000;
      final Set<String> keySetToRetain = new HashSet<String>(keySetCount);
      for (int i = 0; i < keySetCount; i++) {
         keySetToRetain.add(createKey(i));
      }

      // Remove and assert
      assertEquals(map.size(), cache.size());
      assertEquals(keySetToRetain.size(), keySetCount);
      assertTrue("First run should modify", cache.retainAll(keySetToRetain));

      final Set set = cache.entrySet();
      for (final Object anEntry : set) {
         final Map.Entry entry = (Map.Entry) anEntry;
         //noinspection SuspiciousMethodCalls
         if (!keySetToRetain.contains(entry.getKey())) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("************** Should not exist entry.key: " + entry.getKey());
            }
         }
      }
      assertEquals(keySetToRetain.size(), cache.size());
      assertTrue("Second run should *not* modify", !cache.retainAll(keySetToRetain));
   }


   /**
    *
    */
   public void testRetainAllWithEmptySubset() {

      // Prepare test data
      final int keyCount = 10000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Prepare key set to retain
      final Set<String> keySetToRetain = new HashSet<String>(0);

      // Remove and assert
      assertTrue(cache.retainAll(keySetToRetain));
      assertEquals(0, cache.size());
   }


   /**
    *
    */
   public void testRemoveAll() {

      // Prepare test data
      final int keyCount = 10000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Prepare key set to remove
      final int keySetCount = keyCount - 1000;
      final Set<String> keySetToRemove = new HashSet<String>(keySetCount);
      for (int i = 0; i < keySetCount; i++) {
         keySetToRemove.add(createKey(i));
      }

      // Remove and assert
      assertEquals(map.size(), cache.size());
      assertEquals(keySetToRemove.size(), keySetCount);
      assertTrue("First run should modify", cache.removeAll(keySetToRemove));
      assertEquals(keySetToRemove.size() + cache.size(), keyCount);
      assertTrue("Second run should *not* modify", !cache.removeAll(keySetToRemove));
   }


   /**
    *
    */
   public void testRemoveAllWithEmptyKeySet() {

      // Prepare test data
      final int keyCount = 10000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Prepare key set to remove
      final Set<String> keySetToRemove = new HashSet<String>(0);

      // Remove and assert
      assertTrue("First run should not modify", !cache.removeAll(keySetToRemove));
      assertEquals(map.size(), cache.size());
   }


   /**
    *
    */
   public void testExecuteAll() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(1));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Create subset
      final int subsetCount = keyCount / 2;
      final Set<String> subset = new HashSet<String>(keyCount);
      for (int i = 0; i < subsetCount; i++) {
         subset.add(createKey(i));
      }

      // Create executable and aggregator.
      final Executable executable = new Executable();
      final Aggregator aggregator = new Aggregator();

      // Execute and assert
      @SuppressWarnings("unchecked")
      final Integer result = (Integer) cache.executeAll(subset, executable, aggregator);
      assertEquals(keyCount - subsetCount, result.intValue());
   }


   /**
    *
    */
   public void testExecuteAllWithEmptySubset() {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(1));
      }

      // Put to cache
      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Create subset
      final Set<String> subset = new HashSet<String>(0);

      // Create executable and aggregator.
      final Executable executable = new Executable();
      final Aggregator aggregator = new Aggregator();

      // Execute and assert
      final Integer result = (Integer) cache.executeAll(subset, executable, aggregator);
      assertEquals(0, result.intValue());
   }


   public void testGetLock() {

      final ReadWriteLock writeLock = cache().getReadWriteLock();
      final Lock lock = writeLock.writeLock();
      lock.lock();
      try {

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("lock: " + lock); // NOPMD
      } finally {
         lock.unlock();
      }
   }


   @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
   public void testTryLockWithNoWait() throws InterruptedException {


      //
      final AtomicBoolean locked = new AtomicBoolean();
      final AtomicBoolean alive = new AtomicBoolean();
      final AtomicReference<InterruptedException> thrown = new AtomicReference<InterruptedException>();

      // Place a hard lock
      final Lock lock0 = cache().getReadWriteLock().writeLock();
      lock0.lock();
      try {


         // Start a separate thread
         final Thread otherTread = new Thread(new Runnable() {

            public void run() {

               final Lock lock1 = cache().getReadWriteLock().writeLock();
               locked.set(lock1.tryLock());
            }
         });
         otherTread.start();

         // Wait to finish
         try {

            otherTread.join(10000L);
         } catch (final InterruptedException e) {
            thrown.set(e);
         }

         //
         if (thrown.get() != null) {

            throw thrown.get();
         }

         // Asserts
         if (otherTread.isAlive()) {

            alive.set(true);
         }

         assertFalse(alive.get());
         assertFalse(locked.get());

      } finally {
         lock0.unlock();
      }
   }


   @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
   public void testGrantsMultipleReadLocks() throws InterruptedException {


      //
      final AtomicBoolean locked = new AtomicBoolean();
      final AtomicBoolean alive = new AtomicBoolean();
      final AtomicReference<InterruptedException> thrown = new AtomicReference<InterruptedException>();

      // Place a hard lock
      final Lock lock0 = cache().getReadWriteLock().readLock();
      lock0.lock();
      try {


         // Start a separate thread
         final Thread otherTread = new Thread(new Runnable() {

            public void run() {

               final Lock lock1 = cache().getReadWriteLock().readLock();
               try {

                  locked.set(lock1.tryLock());
               } finally {

                  lock1.unlock();
               }
            }
         });
         otherTread.start();

         // Wait to finish
         try {

            otherTread.join(10000L);
         } catch (final InterruptedException e) {
            thrown.set(e);
         }

         //
         if (thrown.get() != null) {

            throw thrown.get();
         }

         // Asserts
         if (otherTread.isAlive()) {

            alive.set(true);
         }

         assertFalse(alive.get());
         assertTrue(locked.get());

      } finally {
         lock0.unlock();
      }
   }


   @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
   public void testDoesNotUpgradeReadLockWhileOtherThreadHoldsRead() throws InterruptedException {


      //
      final AtomicBoolean writeLock0Locked = new AtomicBoolean();
      final AtomicBoolean readLock1Locked = new AtomicBoolean();
      final AtomicBoolean alive = new AtomicBoolean();
      final AtomicReference<InterruptedException> thrown = new AtomicReference<InterruptedException>();

      // Place a hard lock
      final ReadWriteLock readWriteLock0 = cache().getReadWriteLock();
      final Lock readLock0 = readWriteLock0.readLock();
      readLock0.lock();
      try {


         // Start a separate thread
         final Thread otherTread = new Thread(new Runnable() {

            public void run() {

               final ReadWriteLock readWriteLock1 = cache().getReadWriteLock();
               final Lock readLock1 = readWriteLock1.readLock();
               try {

                  readLock1Locked.set(readLock1.tryLock());

                  final Lock writeLock0 = readWriteLock0.writeLock();
                  writeLock0Locked.set(writeLock0.tryLock());

               } finally {

                  readLock1.unlock();
               }


            }
         });
         otherTread.start();

         // Wait to finish
         try {

            otherTread.join(10000L);
         } catch (final InterruptedException e) {
            thrown.set(e);
         }

         //
         if (thrown.get() != null) {

            throw thrown.get();
         }

         // Asserts
         if (otherTread.isAlive()) {

            alive.set(true);
         }

         assertFalse(alive.get());
         assertTrue(readLock1Locked.get());
         assertFalse(writeLock0Locked.get());

      } finally {
         readLock0.unlock();
      }
   }


   /**
    * Test that multiple threads can call lock() on the same lock object.
    * <p/>
    * The idea is the following:
    * <p/>
    * 1. Start two threads.
    * <p/>
    * 2. Let them wait until they have started.
    * <p/>
    * 3. Command them to lock.
    * <p/>
    * 4. Wait after the lock.
    * <p/>
    * 5. Command them to unlock.
    * <p/>
    * 6. Finish.
    * <p/>
    * The correct test should demonstrate that no exceptions are thrown and that one lock waits until other one
    * unlocks.
    *
    * @throws Exception if an error occurs.
    */
   public void testLockObjectSharing() throws Exception {


      final Collection<Exception> errors = new ConcurrentLinkedQueue<Exception>();
      final Lock lock = cache().getReadWriteLock().writeLock();

      final CountDownLatch startupLatch = new CountDownLatch(2);

      final Thread thread1 = new Thread(new MyRunnable(startupLatch, lock, errors));
      final Thread thread2 = new Thread(new MyRunnable(startupLatch, lock, errors));

      thread1.start();
      thread2.start();

      thread1.join();
      thread2.join();

      if (!errors.isEmpty()) {
         throw errors.iterator().next();
      }
   }


   public void testNestedWriteLocks() {

      // Place a write lock
      final Lock writeLock = cache().getReadWriteLock().writeLock();
      writeLock.lock();
      try {
         writeLock.lock();
         //noinspection EmptyTryBlock
         try { // NOPMD
         } finally {
            writeLock.unlock(); // NOPMD Avoid empty try blocks
         }
      } finally {
         writeLock.unlock();
      }
   }


   public void testNestedLocksWithDifferentLockObjects() {

      // Place a write lock
      final Cache<String, String> cache = cache();
      cache.getReadWriteLock().writeLock().lock();
      try {
         cache.getReadWriteLock().writeLock().lock();
         //noinspection EmptyTryBlock
         try { // NOPMD
         } finally {
            cache.getReadWriteLock().writeLock().unlock();
         }
      } finally {
         cache.getReadWriteLock().writeLock().unlock();
      }
   }


   public void testNestedLocksDetectMismatchedUnlock() {

      // Place a write lock
      boolean brokenLockExceptionThrown = false;
      final Lock lock = cache().getReadWriteLock().writeLock();
      lock.lock();
      try {
         lock.lock();
         //noinspection EmptyTryBlock
         try { // NOPMD
         } finally {
            lock.unlock();
         }
      } finally {
         lock.unlock();
         try {
            lock.unlock();
         } catch (final BrokenLockException ignored) {
            brokenLockExceptionThrown = true;
         }
      }

      assertTrue(brokenLockExceptionThrown);
   }


   public void testNestedLocksDetectMismatchedUnlockWithDifferentLockObjects() {

      // Place a write lock
      final Cache<String, String> cache = cache();
      cache.getReadWriteLock().writeLock().lock();
      boolean brokenLockExceptionThrown = false;
      try {
         cache.getReadWriteLock().writeLock().lock();
         //noinspection EmptyTryBlock
         try { // NOPMD
         } finally {
            cache.getReadWriteLock().writeLock().unlock();
         }
      } finally {
         cache.getReadWriteLock().writeLock().unlock();
         try {
            cache.getReadWriteLock().writeLock().unlock();
         } catch (final BrokenLockException ignored) {
            brokenLockExceptionThrown = true;
         }
      }

      assertTrue(brokenLockExceptionThrown);
   }


   public void testNestedReadLocks() {

      final Cache<String, String> cache = cache();
      final Lock readLock = cache.getReadWriteLock().readLock();
      readLock.lock();
      try {
         readLock.lock();
         try {
            assertEquals(2, ((DistributedLock) readLock).getEntryCount());
         } finally {
            readLock.unlock();
         }
      } finally {
         readLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
   }


   public void testReadLockUpgradesToWrite() {

      final Cache<String, String> cache = cache();
      final ReadWriteLock readWriteLock = cache.getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      final Lock writeLock = readWriteLock.writeLock();
      readLock.lock();
      try {
         writeLock.lock();
         try {
            assertEquals(1, ((DistributedLock) writeLock).getEntryCount());
            assertEquals(1, ((DistributedLock) readLock).getEntryCount());
         } finally {
            writeLock.unlock();
         }
      } finally {
         readLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   public void testWriteLockUpgradesToRead() {

      final Cache<String, String> cache = cache();
      final ReadWriteLock readWriteLock = cache.getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      final Lock writeLock = readWriteLock.writeLock();
      writeLock.lock();
      try {
         readLock.lock();
         try {
            assertEquals(1, ((DistributedLock) writeLock).getEntryCount());
            assertEquals(1, ((DistributedLock) readLock).getEntryCount());
         } finally {
            readLock.unlock();
         }
      } finally {
         writeLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   public void testWriteToReadToRead() {

      final Cache<String, String> cache = cache();
      final ReadWriteLock readWriteLock = cache.getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      final Lock writeLock = readWriteLock.writeLock();
      writeLock.lock();
      try {
         readLock.lock();
         try {
            readLock.lock();
            try {
               assertEquals(1, ((DistributedLock) writeLock).getEntryCount());
               assertEquals(2, ((DistributedLock) readLock).getEntryCount());
            } finally {
               readLock.unlock();
            }
         } finally {
            readLock.unlock();
         }
      } finally {
         writeLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   public void testReadToWriteToWrite() {

      final Cache<String, String> cache = cache();
      final ReadWriteLock readWriteLock = cache.getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      final Lock writeLock = readWriteLock.writeLock();
      readLock.lock();
      try {
         writeLock.lock();
         try {
            writeLock.lock();
            try {
               assertEquals(2, ((DistributedLock) writeLock).getEntryCount());
               assertEquals(1, ((DistributedLock) readLock).getEntryCount());
            } finally {
               writeLock.unlock();
            }
         } finally {
            writeLock.unlock();
         }
      } finally {
         readLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   /**
    * Tests that a deadlock is detected
    *
    * @throws InterruptedException if interrupt occurs.
    */
   public void testDetectsDeadlock() throws InterruptedException {

      final Lock writeLock0 = cache().getReadWriteLock("lock0").writeLock();
      final Lock writeLock1 = cache().getReadWriteLock("lock1").writeLock();
      final MutableBoolean detected = new MutableBoolean();

      final CountDownLatch step1Latch = new CountDownLatch(2);
      final CountDownLatch step2Latch = new CountDownLatch(2);

      final Thread thread0 = new Thread(new Runnable() {

         public void run() {

            try {

               // Step #1
               step1Latch.countDown();
               step1Latch.await();
               writeLock0.lock();
               try {

                  // Step #2
                  step2Latch.countDown();
                  step2Latch.await();
                  boolean acquired = false;
                  try {

                     writeLock1.lock();
                     acquired = true;
                     Thread.sleep(100);
                  } catch (final DeadlockException ignored) {

                     detected.set(true);
                  } finally {

                     if (acquired) {

                        writeLock1.unlock();
                     }
                  }
               } finally {
                  writeLock0.unlock();
               }
            } catch (final InterruptedException e) {
               throw new RuntimeInterruptedException(e);
            }
         }
      });

      final Thread thread1 = new Thread(new Runnable() {

         public void run() {

            try {

               // Step #1
               step1Latch.countDown();
               step1Latch.await();
               writeLock1.lock();
               try {

                  // Step #2
                  step2Latch.countDown();
                  step2Latch.await();
                  boolean acquired = false;
                  try {
                     writeLock0.lock();
                     acquired = true;
                     Thread.sleep(100);
                  } catch (final DeadlockException ignored) {

                     detected.set(true);
                  } finally {

                     if (acquired) {

                        writeLock0.unlock();
                     }
                  }
               } finally {

                  writeLock1.unlock();
               }
            } catch (final InterruptedException e) {
               throw new RuntimeInterruptedException(e);
            }
         }
      });

      //
      thread0.start();
      thread1.start();

      // Wait for threads to finish
      thread0.join();
      thread1.join();

      // Assert
      assertTrue(detected);
   }


   /**
    * Tests timed tryLock().
    *
    * @throws InterruptedException if interrupt occurred.
    */
   @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
   public void testTimedTryLock() throws InterruptedException {


      //
      final AtomicBoolean locked = new AtomicBoolean();
      final AtomicBoolean alive = new AtomicBoolean();
      final AtomicReference<InterruptedException> thrown = new AtomicReference<InterruptedException>();

      // Place a hard lock
      final Lock lock0 = cache().getReadWriteLock().writeLock();
      lock0.lock();
      try {


         // Start a separate thread
         final Thread otherTread = new Thread(new Runnable() {

            public void run() {

               final Lock lock1 = cache().getReadWriteLock().writeLock();
               try {
                  locked.set(lock1.tryLock(100L));
               } catch (final InterruptedException e) {
                  thrown.set(e);
               }
            }
         });
         otherTread.start();

         // Wait to finish
         try {

            otherTread.join(10000L);
         } catch (final InterruptedException e) {
            thrown.set(e);
         }

         //
         if (thrown.get() != null) {

            throw thrown.get();
         }

         // Asserts
         if (otherTread.isAlive()) {

            alive.set(true);
         }

         assertFalse(alive.get());
         assertFalse(locked.get());

      } finally {
         lock0.unlock();
      }
   }


   /**
    * Tests timed tryLock().
    *
    * @throws InterruptedException if interrupt occurred.
    */
   @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
   public void testAutomaticUnlock() throws InterruptedException {


      //
      final AtomicReference<InterruptedException> thrown = new AtomicReference<InterruptedException>();
      final AtomicBoolean lockGone = new AtomicBoolean();
      final AtomicBoolean locked = new AtomicBoolean();
      final AtomicBoolean alive = new AtomicBoolean();

      // Place a hard lock
      final Lock lock0 = cache().getReadWriteLock().writeLock();
      lock0.lock(200L);
      try {


         // Start a separate thread
         final Thread otherTread = new Thread(new Runnable() {

            public void run() {

               final Lock lock1 = cache().getReadWriteLock().writeLock();
               try {

                  // Let it to timeout
                  Thread.sleep(300L);

                  // Lock
                  final boolean success = lock1.tryLock();
                  try {
                     locked.set(success);
                  } finally {
                     if (success) {
                        lock1.unlock();
                     }
                  }
               } catch (final InterruptedException e) {
                  thrown.set(e);
               }
            }
         });
         otherTread.start();

         // Wait to finish
         try {

            otherTread.join(10000L);
         } catch (final InterruptedException e) {
            thrown.set(e);
         }

         //
         if (thrown.get() != null) {

            throw thrown.get();
         }

         // Asserts
         if (otherTread.isAlive()) {

            alive.set(true);
         }

         assertFalse(alive.get());
         assertTrue(locked.get());

      } finally {
         try {
            lock0.unlock();
         } catch (final BrokenLockException ignored) {
            lockGone.set(true);
         }
      }
   }


   @SuppressWarnings("UnusedDeclaration")
   public abstract void testWaitingForLockThrowsExceptionOnShutdown() throws InterruptedException;


   protected abstract Cacheonix cacheonix();


   public void testCoherencePutGetPutGet() {

      // Put
      cache().put(TEST_KEY, TEST_OBJECT);

      // Populate local cache in cache 1
      assertEquals(TEST_OBJECT, cache().get(TEST_KEY));

      // Put another object
      final String newValue = createTestObject(1);
      cache().put(TEST_KEY, newValue);

      // Assert that other cache has it invalidated
      assertEquals(newValue, cache().get(TEST_KEY));
   }


   public void testCoherencePutGet() {

      // Put - may cache the TEST_OBJECT at cache0
      cache().put(TEST_KEY, TEST_OBJECT);

      // Put another object from another node
      final String newValue = createTestObject(1);
      cache().put(TEST_KEY, newValue);

      // Assert that first cache has it invalidated
      assertEquals(newValue, cache().get(TEST_KEY));
   }


   public void testCoherenceClear() throws Exception {

      // Set up
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);

      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Populate
      cache().putAll(map);

      // Fill front caches
      for (final Map.Entry<String, String> entry : map.entrySet()) {

         cache().get(entry.getKey());
      }

      // Clear
      cache().clear();

      // Assert all elements gone
      for (final Map.Entry<String, String> entry : map.entrySet()) {

         assertNull(cache().get(entry.getKey()));
      }
   }


   public void testCoherenceRetainAll() throws Exception {

      // Set up
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);

      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Populate
      cache().putAll(map);

      // Fill front caches
      for (final Map.Entry<String, String> entry : map.entrySet()) {

         cache().get(entry.getKey());
      }

      // Prepare key set to retain
      final int keySetCount = keyCount - 1000;
      final Set<String> keySetToRetain = new HashSet<String>(keySetCount);
      for (int i = 0; i < keySetCount; i++) {
         keySetToRetain.add(createKey(i));
      }

      // Retain keys
      cache().retainAll(keySetToRetain);

      // Gather keys to be evaluated as gone
      final Set<String> keysToBeGone = map.keySet();
      keysToBeGone.removeAll(keySetToRetain);

      // Assert non-retained keys are gone
      for (final String key : keysToBeGone) {

         assertNull(cache().get(key));
      }
   }


   public void testCoherencePutAll() throws Exception {

      // Set up
      final int keyCount = 5000;
      final Map<String, String> initialMap = new HashMap<String, String>(keyCount);

      for (int i = 0; i < keyCount; i++) {
         initialMap.put(createKey(i), createValue(i));
      }

      // Populate
      cache().putAll(initialMap);

      // Fill front caches
      for (final Map.Entry<String, String> entry : initialMap.entrySet()) {

         cache().get(entry.getKey());
      }
      // Update
      final Map<String, String> updatedMap = new HashMap<String, String>(keyCount);

      for (int i = 0; i < keyCount; i++) {
         updatedMap.put(createKey(i), createValue(i + 1)); // i+1 produces a value different  from the initialMap
      }

      // Populate
      cache().putAll(updatedMap);


      // Assert all elements gone
      for (final Map.Entry<String, String> entry : updatedMap.entrySet()) {

         assertEquals(cache().get(entry.getKey()), entry.getValue());
      }
   }


   public void testCoherenceRemoveAll() throws Exception {

      // Set up
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);

      for (int i = 0; i < keyCount; i++) {
         map.put(createKey(i), createValue(i));
      }

      // Populate
      cache().putAll(map);

      // Fill front caches
      for (final Map.Entry<String, String> entry : map.entrySet()) {

         cache().get(entry.getKey());
      }

      // Prepare key set to remove
      final int keySetCount = keyCount - 1000;
      final Set<String> keySetToRemove = new HashSet<String>(keySetCount);
      for (int i = 0; i < keySetCount; i++) {
         keySetToRemove.add(createKey(i));
      }

      // Retain keys
      cache().removeAll(keySetToRemove);

      // Assert non-retained keys are gone
      for (final String key : keySetToRemove) {

         assertNull(cache().get(key));
      }
   }


   public void testCoherenceRemove() throws Exception {

      // Populate
      final String key = createKey(0);
      final String value0 = createValue(0);
      cache().put(key, value0);

      // Fill front caches
      assertEquals(value0, cache().get(key));

      // Remove key
      assertEquals(value0, cache().remove(key));

      // Assert non-retained keys are gone
      assertNull(cache().get(key));
   }


   /**
    * @throws InterruptedException if the execution was interrupted.
    */
   public void testCoherenceGetAllClear() throws Exception {

      // Put element into cache
      final int keyCount = 5000;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {

         map.put(createKey(i), createValue(i));
      }

      // Put to cache
      cache().putAll(map);

      // Create keys to retrieve
      final int keySetCount = keyCount - 100;
      final Set<String> subset = new HashSet<String>(keyCount);
      for (int i = 0; i < keySetCount; i++) {

         subset.add(createKey(i));
      }

      // Fill front caches
      Map result = cache().getAll(subset);
      assertEquals("Result from cache must have the same size as the subset", subset.size(), result.size());
      assertEquals("Result from cache must have the key set as the subset", subset, result.keySet());

      // Clear
      cache().clear();

      // Assert all keys are gone
      result = cache().getAll(subset);
      assertEquals(0, cache().size());
      assertEquals("Cache  must be empty: " + result, 0, result.size());
   }


   /**
    * Tests {@link LocalCache#put(Serializable, Serializable, long, TimeUnit)}.
    */
   public void testPutCustomTimeUnit() throws InterruptedException {

      final long delay = 50L;
      final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      cache().put(KEY_0, OBJECT_0, delay, timeUnit);

      assertEquals(OBJECT_0, cache().get(KEY_0));
      Thread.sleep(timeUnit.toMillis(delay) * 2);
      assertNull(cache().get(KEY_0));
   }


   protected abstract Cache<String, String> cache();


   static String createKey(final int i) {

      return TEST_KEY_PREFIX + i;
   }


   static String createValue(final int i) {

      return TEST_OBJECT_PREFIX + i;
   }


   static String createLargeValue(final int i) {

      final StringBuilder builder = new StringBuilder(64000);
      builder.append(TEST_OBJECT_PREFIX);
      builder.append('-');
      builder.append(Integer.toString(i));
      builder.append('-');

      for (int j = 0; j < 64000; j++) {
         builder.append('*');
      }

      return builder.toString();
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      LOG.debug("========== Starting up =========================================================================");

      super.setUp();


      setSingleKeyPerformanceCount(1000);

      savedSystemProperty.save();
      System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");

      LOG.debug("========== Started up =========================================================================");
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      LOG.debug("========== Tearing down ========================================================================");

      savedSystemProperty.restore();
      super.tearDown();

      LOG.debug("========== Teared down =========================================================================");
   }


   protected static final class Executable implements org.cacheonix.cache.executor.Executable {

      private static final long serialVersionUID = -1963185501088791484L;


      public Serializable execute(final Collection<CacheEntry> cacheEntries) {

         int partialResult = 0;
         for (final CacheEntry entry : cacheEntries) {
            final Integer value = Integer.parseInt(((String) entry.getValue()).substring(TEST_OBJECT_PREFIX.length()));
            partialResult += value;
         }
         return partialResult;
      }
   }

   protected static final class Aggregator implements org.cacheonix.cache.executor.Aggregator {

      public Serializable aggregate(final Collection<Serializable> partialResults) {

         int result = 0;
         for (final Object partialResult : partialResults) {
            if (partialResult instanceof Exception) {
               throw new RuntimeException((Throwable) partialResult);
            }
            result += (Integer) partialResult;
         }
         return result;
      }
   }


   /**
    * The goal of this runnable is to confirm that that the same local lock object can be accessed from multiple
    * threads.
    */
   private static class MyRunnable implements Runnable {

      private final CountDownLatch startupLatch;

      private final Lock lock;

      private final Collection<Exception> errors;


      public MyRunnable(final CountDownLatch startupLatch, final Lock lock,
              final Collection<Exception> errors) {

         this.startupLatch = startupLatch;
         this.lock = lock;
         //noinspection AssignmentToCollectionOrArrayFieldFromParameter
         this.errors = errors;
      }


      public void run() {

         try {

            // Wait until both are ready
            startupLatch.countDown();
            startupLatch.await();
            lock.lock();
            try {

               // Wait
               Thread.sleep(10L);

            } finally {
               lock.unlock();
            }
         } catch (final Exception e) {
            errors.add(e);
         }
      }
   }


   public String toString() {

      return "PartitionedCacheTestDriver{" +
              "savedSystemProperty=" + savedSystemProperty +
              "} " + super.toString();
   }
}
