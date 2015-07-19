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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cacheonix.Cacheonix;
import org.cacheonix.ShutdownException;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.locks.BrokenLockException;
import org.cacheonix.locks.Lock;
import org.cacheonix.locks.ReadWriteLock;
import org.cacheonix.impl.cache.CacheonixCache;
import org.cacheonix.impl.configuration.SystemProperty;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.MutableBoolean;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

/**
 */
public abstract class MultiplePartitionedCacheTestDriver extends PartitionedCacheTestDriver {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PartitionedCacheTestDriver.class); // NOPMD


   /**
    * Cacheonix configurations, one per cluster.
    */
   private final String[] configurations;

   /**
    * List of cache managers.
    */
   private final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);

   /**
    * List of clustered caches.
    */
   final List<Cache<String, String>> cacheList = new ArrayList<Cache<String, String>>(5);


   MultiplePartitionedCacheTestDriver(final String[] configurations) {

      this.configurations = ArrayUtils.copy(configurations);
   }


   public void testGetCache() throws InterruptedException {

      for (int i = 0; i < cacheManagerList.size(); i++) {
         final Cacheonix cacheonix = cacheManagerList.get(i);
         final Cache cache = cacheonix.getCache(DISTRIBUTED_CACHE_NAME);
         assertNotNull("Cache from  " + configurations[i] + " should not be null", cache);
      }
   }


   public void testGetName() {

      for (final Cache<String, String> cache : cacheList) {
         assertEquals(DISTRIBUTED_CACHE_NAME, cache.getName());
      }
   }


   public void testGet() {

      cache().put(TEST_KEY, TEST_OBJECT);
      for (int i = 0; i < cacheList.size(); i++) {
         final CacheonixCache<String, String> remoteCache = cache(i);
         assertEquals("Cache # " + i + ": " + remoteCache + " should find object ", TEST_OBJECT, remoteCache.get(TEST_KEY));
      }
   }


   /**
    * Tests that get(Object) handles situation when a local cache holds an element in Invalid state.
    */
   public void testGetInvalid() {

      // Put into cache # 0
      assertNull(cache().put(KEY_0, OBJECT_0));
      // Put into cache # 1, # 0 moved to Invalid state
      assertEquals(OBJECT_0, cache(1).put(KEY_0, OBJECT_1));
      assertEquals(OBJECT_1, cache(1).get(KEY_0));
      assertEquals(OBJECT_1, cache().get(KEY_0));
   }


   /**
    */
   public void testGetOwner() {
      // Simple compare
      assertEquals(cache().getKeyOwner(KEY_0), cache(1).getKeyOwner(KEY_0));
      assertEquals(cache().getKeyOwner(KEY_1), cache(1).getKeyOwner(KEY_1));
   }


   public void testValues() {

      // Put different element into each cache
      final int valuesSize = 5000;
      final Cache<String, String> cache = cache();
      for (int i = 0; i < valuesSize; i++) {
         cache.put(createKey(i), createValue(i));
      }

      // Assert each element is available in all caches.
      for (int i = 0; i < cacheList.size(); i++) {
         final Cache<String, String> remoteCache = cache(i);
         final Collection<String> values = remoteCache.values();
         assertEquals(valuesSize, values.size());
         for (final Object value : values) {
            assertTrue(value.getClass().toString(), String.class.isAssignableFrom(value.getClass()));
         }
      }
   }


   /**
    * Tests that get(Object) handles situation when a local cache holds an element in Shared state.
    */
   public void testGetShared() {
      // Put into cache # 0
      cache().put(KEY_0, OBJECT_0);
      // Get from cache # 1, # 1 move to Shared state
      assertEquals(OBJECT_0, cache(1).get(KEY_0));
      // Get from # 1 that should be in Shared state.
      assertEquals(OBJECT_0, cache(1).get(KEY_0));
   }


   public void testIsEmpty() {

      assertTrue(cache().isEmpty());
      assertTrue(cache(1).isEmpty());

      cache().put(KEY_0, TEST_OBJECT);
      cache(1).put(KEY_1, TEST_OBJECT);
      cache(1).get(KEY_1);
      cache().get(KEY_0);
      assertTrue(!cache().isEmpty());
      assertTrue(!cache(1).isEmpty());

      cache().remove(KEY_0);
      cache().remove(KEY_1);
      assertTrue(cache().isEmpty());
      assertTrue(cache(1).isEmpty());
   }


   public void testPutTwoKeys() {

      // Put different element into each cache
      final Object object = cache().put(KEY_0, OBJECT_0);
      if (LOG.isDebugEnabled()) {
         LOG.debug("object: " + object);
      }
      assertNull(object);
      final Object object1 = cache(1).put(KEY_1, OBJECT_1);
      assertNull("Object should be null: " + object1, object1);

      // Assert all keys found in all caches
      assertEquals(OBJECT_0, cache().get(KEY_0));
      assertEquals(OBJECT_1, cache().get(KEY_1));

      assertEquals(OBJECT_1, cache(1).get(KEY_1));
      assertEquals(OBJECT_0, cache(1).get(KEY_0));
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
      for (int i = 1; i < cacheList.size(); i++) {
         final Cache<String, String> remoteCache = cache(i);
         assertEquals(map.size(), remoteCache.size());
         final Set<Map.Entry<String, String>> entries = map.entrySet();
         for (final Map.Entry<String, String> entry : entries) {
            assertEquals("Object not found in cache " + remoteCache.getName(), entry.getValue(), remoteCache.get(entry.getKey()));
         }
      }
   }


   public void testPut() {

      cache().put(TEST_KEY, TEST_OBJECT);
      for (int i = 0; i < cacheList.size(); i++) {
         final Cache<String, String> remoteCache = cacheList.get(i);
         assertEquals("Cache # " + i + ": " + remoteCache + " should find object ", TEST_OBJECT,
                 remoteCache.get(TEST_KEY));
      }
   }


   public void testPutAllWithEmptyMap() {

      // Set up
      final Map<String, String> map = new HashMap<String, String>(0);

      final Cache<String, String> cache = cache();
      cache.putAll(map);

      // Assert
      for (int i = 0; i < cacheList.size(); i++) {
         final Cache<String, String> remoteCache = cache(i);
         assertTrue(remoteCache.isEmpty());
      }
   }


   /**
    * Tests put into the item in invalid state.
    */
   public void testPutToInvalid() {

      cache().put(KEY_0, OBJECT_0);
      cache(1).put(KEY_0, OBJECT_1);

      // Put into cache #0 in Invalid state
      assertNotNull(cache().put(KEY_0, OBJECT_2));
      assertEquals(OBJECT_2, cache().get(KEY_0));
      assertEquals(OBJECT_2, cache(1).get(KEY_0));

      assertNotNull(cache(1).put(KEY_0, OBJECT_1));
      assertEquals(OBJECT_1, cache(1).get(KEY_0));
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

      for (int i = 0; i < cacheList.size(); i++) {

         final Cache<String, String> remoteCache = cache(i);
         final Integer result = (Integer) remoteCache.execute(filter, executable, aggregator);
         assertEquals(keyCount / 2, result.intValue());
      }
   }


   public void testSize() {

      assertEquals(0, cache().size());

      cache().put(KEY_0, OBJECT_0);
      cache(1).put(KEY_1, OBJECT_1);

      // Populate local caches
      cache().get(KEY_0);
      cache(1).get(KEY_1);
      cache().get(KEY_1);
      cache(1).get(KEY_0);

      assertEquals(2, cache().size());
      assertEquals(2, cache(1).size());
   }


   public void testClear() {

      // Put different element into each cache
      cache().put(KEY_0, OBJECT_0);
      cache(1).put(KEY_1, OBJECT_1);

      // Make sure size is zero
      for (final Cache<String, String> cache : cacheList) {
         assertEquals("Size for " + cache.getName(), 2, cache.size());
      }
      // Populate local caches
      cache().get(KEY_0);
      cache(1).get(KEY_1);
      cache().get(KEY_1);
      cache(1).get(KEY_0);

      // Clear on one
      cache().clear();

      // Make sure size is zero
      for (final Cache<String, String> cache : cacheList) {
         assertEquals("Size for " + cache.getName(), 0, cache.size());
         assertNull(cache.get(KEY_0));
         assertNull(cache.get(KEY_1));
      }
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
      for (int i = 1; i < cacheList.size(); i++) {
         final Cache<String, String> remoteCache = cache(i);
         final Set<Map.Entry<String, String>> result = remoteCache.entrySet();
         for (final Object anEntry : result) {
            final Map.Entry entry = (Map.Entry) anEntry;
            //noinspection SuspiciousMethodCalls
            assertEquals(map.get(entry.getKey()), entry.getValue());
         }
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
      for (int i = 0; i < cacheList.size(); i++) {

         final Cache<String, String> remoteCache = cache(i);
         final Set<String> result = remoteCache.keySet();
         assertEquals(map.size(), result.size());


         for (final String s : map.keySet()) {
            assertTrue("Result for cache " + i + " should contain: " + s, result.contains(s));
         }

         for (final Object aString : result) {

            final String s = (String) aString;
            assertTrue("Source for cache " + i + " should contain: " + s, map.containsKey(s));
         }
      }
   }


   public void testRemove() {

      // Put different element into each cache
      cache().put(KEY_0, OBJECT_0);
      cache(1).put(KEY_1, OBJECT_1);

      // Validate preparation
      assertNotNull(cache().get(KEY_1));
      assertNotNull(cache(1).get(KEY_1));

      // Clear one
      cache(1).remove(KEY_1);

      // Make sure size is zero
      assertNull(cache().get(KEY_1));
      assertNull(cache(1).get(KEY_1));

      // Remove never existed
      assertNull(cache().remove(NEVER_EXISTED_KEY));

      // Remove removed
      assertNull(cache(1).remove(KEY_1));
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

      for (int i = 0; i < cacheList.size(); i++) {

         final Cache<String, String> remoteCache = cache(i);
         final Integer result = (Integer) remoteCache.execute(executable, aggregator);
         assertEquals(keyCount, result.intValue());
      }
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
      for (int i = 0; i < cacheList.size(); i++) {

         final Cache remoteCache = cache(i);
         @SuppressWarnings("unchecked")
         final Integer result = (Integer) remoteCache.executeAll(subset, executable, aggregator);
         assertEquals(keyCount - subsetCount, result.intValue());
      }
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
      for (int i = 0; i < cacheList.size(); i++) {

         final Cache remoteCache = cache(i);
         @SuppressWarnings("unchecked")
         final Integer result = (Integer) remoteCache.executeAll(subset, executable, aggregator);
         assertEquals(0, result.intValue());
      }
   }


   public void testCoherencePutGetPutGet() {

      // Put
      cache().put(TEST_KEY, TEST_OBJECT);

      // Populate local cache in cache 1
      assertEquals(TEST_OBJECT, cache(1).get(TEST_KEY));

      // Put another object
      final String newValue = createTestObject(1);
      cache().put(TEST_KEY, newValue);

      // Assert that other cache has it invalidated
      assertEquals(newValue, cache(1).get(TEST_KEY));

      // Assert that the cache that put it has it invalidated.
      assertEquals(newValue, cache().get(TEST_KEY));
   }


   public void testCoherencePutGet() {

      // Put - may cache the TEST_OBJECT at cache0
      cache().put(TEST_KEY, TEST_OBJECT);

      // Put another object from another node
      final String newValue = createTestObject(1);
      cache(1).put(TEST_KEY, newValue);

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
      for (int i = 0; i < cacheList.size(); i++) {

         for (final Map.Entry<String, String> entry : map.entrySet()) {

            cache(i).get(entry.getKey());
         }
      }

      // Clear
      cache().clear();

      // Assert all elements gone
      for (int i = 0; i < cacheList.size(); i++) {

         for (final Map.Entry<String, String> entry : map.entrySet()) {

            assertNull(cache(i).get(entry.getKey()));
         }
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
      for (int i = 0; i < cacheList.size(); i++) {

         for (final Map.Entry<String, String> entry : map.entrySet()) {

            cache(i).get(entry.getKey());
         }
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
      for (int i = 0; i < cacheList.size(); i++) {

         for (final String key : keysToBeGone) {

            assertNull(cache(i).get(key));
         }
      }
   }


   public void testCoherencePutAll() throws Exception {

      // Set up
      final int keyCount = 1;
      final Map<String, String> initialMap = new HashMap<String, String>(keyCount);

      for (int i = 0; i < keyCount; i++) {
         initialMap.put(createKey(i), createValue(i));
      }

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Populate"); // NOPMD
      cache().putAll(initialMap);

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Fill front caches"); // NOPMD
      for (int i = 0; i < cacheList.size(); i++) {

         for (final Map.Entry<String, String> entry : initialMap.entrySet()) {

            cache(i).get(entry.getKey());
         }
      }

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Update"); // NOPMD
      final Map<String, String> updatedMap = new HashMap<String, String>(keyCount);

      for (int i = 0; i < keyCount; i++) {
         updatedMap.put(createKey(i), createValue(i + 1)); // i+1 produces a value different  from the initialMap
      }

      // Populate
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Populate"); // NOPMD
      cache().putAll(updatedMap);


      // Assert all elements gone
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Assert all elements gone"); // NOPMD
      for (final Cache<String, String> cache : cacheList) {

         for (final Map.Entry<String, String> entry : updatedMap.entrySet()) {

            assertEquals(cache.get(entry.getKey()), entry.getValue());
         }
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
      for (int i = 0; i < cacheList.size(); i++) {

         for (final Map.Entry<String, String> entry : map.entrySet()) {

            cache(i).get(entry.getKey());
         }
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
      for (final Cache<String, String> cache : cacheList) {

         for (final String key : keySetToRemove) {

            assertNull(cache.get(key));
         }
      }
   }


   public void testCoherenceRemove() throws Exception {

      // Populate
      final String key = createKey(0);
      final String value0 = createValue(0);
      cache().put(key, value0);

      // Fill front caches
      for (int i = 0; i < cacheList.size(); i++) {

         assertEquals(value0, cache(i).get(key));
      }

      // Remove key
      assertEquals(value0, cache().remove(key));

      // Assert non-retained keys are gone
      for (final Cache<String, String> cache : cacheList) {

         assertNull(cache.get(key));
      }
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
      for (int i = 0; i < cacheList.size(); i++) {

         final Map result = cache(i).getAll(subset);
         assertEquals("Result from cache " + i + " must have the same size as the subset", subset.size(), result.size());
         assertEquals("Result from cache " + i + " must have the key set as the subset", subset, result.keySet());
      }

      // Clear
      cache().clear();

      // Assert all keys are gone
      for (int i = 0; i < cacheList.size(); i++) {

         final Map result = cache(i).getAll(subset);
         assertEquals(0, cache(i).size());
         assertEquals("Cache " + i + " must be empty: " + result, 0, result.size());
      }
   }


   public void testContainsKey() {

      // Put different element into each cache
      final int cacheListSize = cacheList.size();
      for (int i = 0; i < cacheListSize; i++) {
         final Cache<String, String> cache = cacheList.get(i);
         cache.put(createKey(i), createValue(i));
      }

      // Assert each element is available in all caches.
      for (final Cache<String, String> cache : cacheList) {
         for (int j = 0; j < cacheListSize; j++) {
            final String key = createKey(j);
            assertTrue("Cache " + cache.getName() + " contains key " + key,
                    cache.containsKey(key));
         }
      }
   }


   public void testContainsValue() {

      // Put different element into each cache
      final int cacheListSize = cacheList.size();
      for (int i = 0; i < cacheListSize; i++) {

         final Cache<String, String> cache = cacheList.get(i);
         cache.put(createKey(i), createValue(i));
      }

      // Assert each element is available in all caches.
      for (final Cache<String, String> cache : cacheList) {
         boolean found = false;
         for (int j = 0; j < cacheListSize && !found; j++) {

            final String value = createValue(j);
            found = cache.containsValue(value);
         }
         assertTrue("Cache " + cache.getName() + " should contain value", found);
      }
   }


   public void testTryLockWithNoWait() {

      // Place a hard lock
      final Lock lockAtNode0 = cache().getReadWriteLock().writeLock();
      lockAtNode0.lock();
      try {

         final Lock lockAtNode1 = cacheList.get(1).getReadWriteLock().writeLock();
         assertFalse("Must not succeed because write lock is already held by other node", lockAtNode1.tryLock());
      } finally {
         lockAtNode0.unlock();
      }
   }


   public void testGrantsMultipleReadLocks() {

      // Place a hard lock
      final Lock lockAtNode0 = cache().getReadWriteLock().readLock();
      lockAtNode0.lock();
      try {

         final Lock lockAtNode1 = cacheList.get(1).getReadWriteLock().readLock();
         assertTrue(lockAtNode1.tryLock());
         lockAtNode1.unlock();
      } finally {
         lockAtNode0.unlock();
      }
   }


   public void testDoesNotUpgradeReadLockWhileOtherThreadHoldsRead() {

      final ReadWriteLock rwLockAtNode0 = cache().getReadWriteLock();
      final Lock readLockAtNode0 = rwLockAtNode0.readLock();
      readLockAtNode0.lock();
      try {
         final ReadWriteLock rwLockAtNode1 = cacheList.get(1).getReadWriteLock();
         final Lock readLockAtNode1 = rwLockAtNode1.readLock();
         assertTrue(readLockAtNode1.tryLock());
         final Lock writeLockAtNode0 = rwLockAtNode0.writeLock();
         assertFalse(writeLockAtNode0.tryLock());
         readLockAtNode1.unlock();
      } finally {
         readLockAtNode0.unlock();
      }
   }


   /**
    * Tests timed tryLock().
    *
    * @throws InterruptedException if interrupt occurred.
    */
   public void testTimedTryLock() throws InterruptedException {

      final Cache<String, String> cache0 = cache();
      final Lock writeLock = cache0.getReadWriteLock().writeLock();
      writeLock.lock();
      try {

         final Cache<String, String> cache1 = cacheList.get(1);
         assertFalse(cache1.getReadWriteLock().writeLock().tryLock(100L));

      } finally {
         writeLock.unlock();
      }
   }


   /**
    * Tests timed tryLock().
    *
    * @throws InterruptedException if interrupt occurred.
    */
   @SuppressWarnings("TooBroadScope")
   public void testAutomaticUnlock() throws InterruptedException {

      final MutableBoolean lockGone = new MutableBoolean();
      final Lock writeLock = cache().getReadWriteLock().writeLock();
      writeLock.lock(200L);
      try {

         // Let it to timeout
         Thread.sleep(300L);

         // Assert we can lock now.
         assertTrue(cacheList.get(1).getReadWriteLock().writeLock().tryLock());

      } finally {
         try {
            writeLock.unlock();
         } catch (final BrokenLockException ignored) {
            lockGone.set(true);
         }
      }

      assertTrue(lockGone);
   }


   public void testWaitingForLockThrowsExceptionOnShutdown() throws InterruptedException {

      final Cache<String, String> cache0 = cache();
      final Cache<String, String> cache1 = cacheList.get(1);

      final MutableBoolean thrown = new MutableBoolean();
      final Lock writeLock = cache0.getReadWriteLock().writeLock();
      writeLock.lock();
      try {


         final Thread thread = new Thread(new Runnable() {

            public void run() {

               try {
                  cache1.getReadWriteLock().writeLock().lock();
               } catch (final ShutdownException ignored) {
                  thrown.set(true);
               }
            }
         });
         thread.start();

         Thread.sleep(100L);
         cacheManagerList.get(1).shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         thread.join();

      } finally {
         writeLock.unlock();
      }

      assertTrue(thrown);
   }


   /**
    * Tests that it a cluster node leaves, this releases locks it held.
    *
    * @throws Exception if error occurs.
    */
   @SuppressWarnings("TooBroadScope")
   public void testWaitingForLockAcquiresLockOnShutdown() throws Exception {

      final long waitForLockTimeout = 30000L;
      final Exception[] asyncException = new Exception[1];
      final Cache<String, String> cache0 = cache();
      final Cache<String, String> cache1 = cacheList.get(1);
      final MutableBoolean thrown = new MutableBoolean();
      final MutableBoolean acquired = new MutableBoolean();
      final Lock writeLock0 = cache0.getReadWriteLock().writeLock();
      writeLock0.lock();
      try {


         final Thread thread = new Thread(new Runnable() {

            public void run() {

               try {

                  // Will acquire because shutdown of the node cacheonix0 will release a write writeLock1 held by it.
                  final Lock writeLock1 = cache1.getReadWriteLock().writeLock();

                  acquired.set(writeLock1.tryLock(waitForLockTimeout));

                  if (acquired.get()) {

                     writeLock1.unlock();
                  }
               } catch (final Exception e) {

                  asyncException[0] = e;
               }
            }
         });
         thread.start();

         // Wait
         Thread.sleep(100L);

         // Shutdown owner of the write lock (cacheonix1) is waiting for a lock
         cacheManagerList.get(0).shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);

         // The fact that thread allowed to
         thread.join();

      } finally {
         try {
            writeLock0.unlock();
         } catch (final ShutdownException ignored) {
            // Because by this time the owner of the first lock is shutdown
            thrown.set(true);
         }
      }


      // Throw exception occured in the async thread if any
      if (asyncException[0] != null) {

         throw asyncException[0];
      }

      // Asserts
      assertTrue(acquired);
      assertTrue(thrown);
   }


   public void testContainsNullValue() {

      // Put different element into each cache
      final int cacheListSize = cacheList.size();
      for (int i = 0; i < cacheListSize; i++) {

         final Cache<String, String> cache = cacheList.get(i);
         cache.put(createKey(i), null);
      }

      // Assert each element is available in all caches.
      for (final Cache<String, String> cache : cacheList) {
         boolean found = false;
         for (int j = 0; j < cacheListSize && !found; j++) {

            found = cache.containsValue(null);
         }
         assertTrue("Cache " + cache.getName() + " should contain value", found);
      }
   }


   /**
    * @throws Exception if an error occured while running the test.
    */
   public void testOwnersEvenlyDistributed() throws Exception {

      // Wait until all members obtain ownership
      Thread.sleep(5000);
      final Map<CacheMember, int[]> counters = new HashMap<CacheMember, int[]>(11);
      counters.clear();

      final int count = 1000;
      for (int i = 0; i < count; i++) {

         final CacheMember owner = cache().getKeyOwner(createKey(i));
         int[] counter = counters.get(owner);
         if (counter == null) {

            counter = new int[1];
            counters.put(owner, counter);
         }
         counter[0]++;
      }
      assertEquals(configurations.length, counters.size());
      final List<int[]> c = new ArrayList<int[]>(counters.values());
      final double difference = (double) Math.abs((c.get(0))[0] - (c.get(1))[0]) / (double) count;
      final double expected = 0.1;
      assertTrue("Difference should be under " + expected + " but it is " + difference, Math.abs(difference) <= expected);
   }


   final CacheonixCache<String, String> cache(final int index) {

      return (CacheonixCache<String, String>) cacheList.get(index);
   }


   protected Cacheonix cacheonix() {

      return cacheManagerList.get(0);
   }


   protected Cache<String, String> cache() {

      return cacheList.get(0);
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      LOG.debug("================================================================================================");
      LOG.debug("========== Starting up =========================================================================");
      LOG.debug("================================================================================================");
      super.setUp();
      assertTrue("This test makes sense only for sizes bigger than 2", configurations.length >= 2);
      System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
      for (int i = 0; i < configurations.length; i++) {

         final String configurationPath = TestUtils.getTestFile(configurations[i]).toString();
         final Cacheonix manager = Cacheonix.getInstance(configurationPath);
         cacheManagerList.add(manager);
         @SuppressWarnings("unchecked")
         final Cache<String, String> cache = manager.getCache(DISTRIBUTED_CACHE_NAME);
         assertNotNull("Cache " + i + " should be not null", cache);
         cacheList.add(cache);
      }

      // Let the cluster form
      Thread.sleep(1000L);
      LOG.debug("================================================================================================");
      LOG.debug("========== Started up =========================================================================");
      LOG.debug("================================================================================================");
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      LOG.debug("================================================================================================");
      LOG.debug("========== Tearing down ========================================================================");
      LOG.debug("================================================================================================");

      for (int i = 0; i < configurations.length; i++) {

         final Cacheonix cacheonix = cacheManagerList.get(i);
         if (!cacheonix.isShutdown()) {
            cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         }
      }
      cacheList.clear();
      cacheManagerList.clear();
      super.tearDown();
      LOG.debug("================================================================================================");
      LOG.debug("========== Teared down =========================================================================");
      LOG.debug("================================================================================================");
   }
}
