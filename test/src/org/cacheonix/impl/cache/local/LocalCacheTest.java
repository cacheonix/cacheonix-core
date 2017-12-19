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
package org.cacheonix.impl.cache.local;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.cache.util.DummyObjectSizeCalculator;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;

import static java.lang.Thread.sleep;
import static org.cacheonix.TestConstants.LOCAL_TEST_CACHE;
import static org.cacheonix.impl.config.ElementEventNotification.SYNCHRONOUS;

/**
 * Tests {@link LocalCacheTest}
 */
public final class LocalCacheTest extends CacheonixTestCase {

   private static final String KEY = "key";

   private static final String VALUE = "value";

   private static final String SUFFIX_ONE = "_1";

   private static final String SUFFIX_TWO = "_2";

   private static final String KEY_0 = createTestKey(0);

   private static final String KEY_1 = createTestKey(1);

   private static final String OBJECT_0 = createTestObject(0);

   private static final String OBJECT_1 = createTestObject(1);

   private static final String OBJECT_2 = createTestObject(2);

   private static final int MAX_SIZE = 1000;

   public static final DummyDiskStorage DUMMY_DISK_STORAGE = new DummyDiskStorage(LOCAL_TEST_CACHE);

   public static final DummyObjectSizeCalculator DUMMY_OBJECT_SIZE_CALCULATOR = new DummyObjectSizeCalculator();

   public static final DummyBinaryStoreDataSource DUMMY_BINARY_STORE_DATA_SOURCE = new DummyBinaryStoreDataSource();

   public static final DummyDataStore DUMMY_DATA_STORE = new DummyDataStore();

   public static final DummyCacheInvalidator DUMMY_CACHE_INVALIDATOR = new DummyCacheInvalidator();

   public static final DummyCacheLoader DUMMY_CACHE_LOADER = new DummyCacheLoader();


   private LocalCache<String, String> cache;


   /**
    * Tests {@link LocalCache#put(Object, Object)}
    *
    * @noinspection JUnitTestMethodWithNoAssertions
    */
   public void testPut() {

      for (int i = 0; i <= MAX_SIZE; i++) {
         final int expectedSize = i < MAX_SIZE ? i + 1 : MAX_SIZE;
         putAndAssert(makeKey(i), makeValue(i) + SUFFIX_ONE, makeValue(i) + SUFFIX_TWO, expectedSize);
      }
   }


   /**
    * Tests {@link LocalCache#put(Serializable, Serializable, long, TimeUnit)}.
    */
   public void testPutCustomTimeUnit() throws InterruptedException {

      final long delay = 50L;
      final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      cache().put(KEY_0, OBJECT_0, delay, timeUnit);

      assertEquals(OBJECT_0, cache().get(KEY_0));
      sleep(timeUnit.toMillis(delay) * 2);
      assertNull(cache().get(KEY_0));
   }


   /**
    * Tests get() affects LRU.
    *
    * @noinspection JUnitTestMethodWithNoAssertions
    */
   public void testGetAffectsLRU() {
      // Fill 0,1,2
      for (int i = 0; i < MAX_SIZE; i++) {
         cache.put(makeKey(i), makeValue(i));
      }
      cache.get(makeKey(0));
      cache.put(makeKey(MAX_SIZE), makeValue(MAX_SIZE));
      assertNotNull(cache.get(makeKey(0)));
      assertNull(cache.get(makeKey(1)));
   }


   /**
    * Tests {@link LocalCache#get(Object)}
    */
   public void testGet() {

      final List<Entry<String, String>> entries = populate(MAX_SIZE);
      for (int i = 0; i < MAX_SIZE; i++) {
         final Entry<String, String> e = entries.get(i);
         assertEquals(e.getValue(), cache.get(e.getKey()));
      }
      assertNull(cache.get(KEY + "never existed"));
   }


   /**
    *
    */
   public void testGetAll() {

      // Put element into cache
      final int keyCount = MAX_SIZE;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(makeKey(i), makeValue(1));
      }

      // Put to cache
      cache.putAll(map);

      final int keySetCount = keyCount - 100;
      final Set<String> subset = new HashSet<String>(keyCount);
      for (int i = 0; i < keySetCount; i++) {
         subset.add(makeKey(i));
      }

      // Remove and assert
      final Map<String, String> result = cache.getAll(subset);
      assertEquals(subset.size(), result.size());
      assertEquals(subset, result.keySet());
   }


   /**
    *
    */
   public void testGetAllWithMoreKeysThanElements() {

      // Put element into cache
      final int keyCount = MAX_SIZE;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(makeKey(i), makeValue(1));
      }

      // Put to cache
      cache.putAll(map);

      final int keySetCount = keyCount * 2;
      final Set<String> subset = new HashSet<String>(keyCount);
      for (int i = 0; i < keySetCount; i++) {
         subset.add(makeKey(i));
      }

      // Remove and assert
      final Map<String, String> result = cache.getAll(subset);
      assertEquals(keyCount, result.size());

      for (final String resultKey : result.keySet()) {
         assertTrue(subset.contains(resultKey));
      }
   }


   /**
    * Tests {@link LocalCache#clear()}
    */
   public void testClear() {

      populate(MAX_SIZE);
      assertEquals(MAX_SIZE, cache.size());
      cache.clear();
      assertEquals(0, cache.size());
   }


   /**
    * Tests {@link LocalCache#clear()}
    */
   public void testContainsKey() {

      populate(MAX_SIZE);
      assertTrue(cache.containsKey(makeKey(0)));
   }


   public void testContainsValue() {

      final String value = makeValue(0);
      cache.put(makeKey(0), value);
      assertTrue(cache.containsValue(value));
      assertFalse(cache.containsValue(makeValue(999999)));
   }


   public void testEntrySet() {
      // Empty
      assertEquals(0, cache.entrySet().size());
      // Filled
      populate(MAX_SIZE);
      assertEquals(MAX_SIZE, cache.entrySet().size());
   }


   public void testIsEmpty() {

      assertTrue(cache.isEmpty());
      populate(MAX_SIZE);
      assertFalse(cache.isEmpty());
   }


   public void testKeySet() {

      // Put different element into each cache
      final int keyCount = MAX_SIZE;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(TEST_KEY_PREFIX + i, TEST_OBJECT_PREFIX + i);
      }

      //
      cache.putAll(map);

      // Assert each element is available in all caches.

      final Set<String> result = cache.keySet();
      assertEquals(map.size(), result.size());


      final String key = TEST_KEY_PREFIX + Integer.toString(keyCount / 2);
      assertTrue("Source for cache should contain: " + key, map.containsKey(key));
      assertTrue("Result for cache should contain: " + key, result.contains(key));
   }


   public void testPutAll() {

      final int overflownMaxSize = MAX_SIZE + 1;
      final Map<String, String> entries = new HashMap<String, String>(overflownMaxSize);
      for (int i = 0; i < overflownMaxSize; i++) {
         entries.put(makeKey(i), makeValue(i));
      }
      cache.putAll(entries);
      assertEquals(MAX_SIZE, cache.size());
   }


   public void testPutAllEmpty() {

      cache.putAll(Collections.<String, String>emptyMap());
      assertEquals(0, cache.size());
   }


   public void testRemove() {

      populate(MAX_SIZE);
      final String key = makeKey(0);
      final String value = makeValue(0);
      assertEquals(value, cache.remove(key));
      assertEquals(MAX_SIZE - 1, cache.size());
      assertNull(cache.get(key));
   }


   public void testAtomicRemove() {

      // ----------
      // - Step 1 -
      // ----------

      // Prepare
      cache.put(KEY_0, OBJECT_0);
      assertNotNull(cache.get(KEY_0));

      // Clear one
      assertTrue(cache.remove(KEY_0, OBJECT_0));

      // Make sure element is gone
      assertNull(cache.get(KEY_0));

      // ----------
      // - Step 2 -
      // ----------

      // Prepare
      cache.put(KEY_1, OBJECT_1);
      assertNotNull(cache.get(KEY_1));

      // Try to remove key with a mismatched value�
      assertFalse(cache.remove(KEY_1, OBJECT_0));

      // Assert remove didn't happen
      assertEquals(cache.get(KEY_1), OBJECT_1);
   }


   public void testAtomicReplace() {

      // Prepare
      cache.put(KEY_0, OBJECT_0);
      assertNotNull(cache.get(KEY_0));

      // Replace one
      assertTrue(cache.replace(KEY_0, OBJECT_0, OBJECT_1));

      // Make sure element was replaced
      assertEquals(OBJECT_1, cache.get(KEY_0));
   }


   public void testAtomicReplaceNonexistent() {


      // Prepare
      cache.put(KEY_0, OBJECT_0);
      assertNotNull(cache.get(KEY_0));

      // Try to replace key with a mismatched value�
      assertFalse(cache.replace(KEY_0, OBJECT_2, OBJECT_1));

      // Assert replace didn't happen
      assertEquals(cache.get(KEY_0), OBJECT_0);
   }


   public void testAtomicReplaceNullNewValue() {

      // Prepare
      cache.put(KEY_0, OBJECT_0);
      assertNotNull(cache.get(KEY_0));

      // Try to replace key with a null value�
      assertTrue(cache.replace(KEY_0, OBJECT_0, null));

      // Assert replace happened
      assertNull(cache.get(KEY_0));
   }


   public void testAtomicReplaceNullOldValue() {

      // Prepare
      cache.put(KEY_0, OBJECT_0);
      assertNotNull(cache.get(KEY_0));

      // Try to replace key with a null value�
      assertFalse(cache.replace(KEY_0, null, OBJECT_1));

      // Assert didn't happen
      assertEquals(OBJECT_0, cache.get(KEY_0));
   }


   public void testReplaceIfPresentExistent() {

      // Prepare
      cache().put(KEY_0, OBJECT_0);
      assertEquals(OBJECT_0, cache().get(KEY_0));

      // Replace existing
      assertEquals(OBJECT_0, cache().replace(KEY_0, OBJECT_1));
   }


   private LocalCache<String, String> cache() {

      return cache;
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


   public void testSize() {

      populate(MAX_SIZE);
      assertEquals(MAX_SIZE, cache.size());
      cache.put(makeKey(MAX_SIZE + 1), makeValue(MAX_SIZE + 1));
      assertEquals(MAX_SIZE, cache.size());
   }


   public void testValues() {

      assertEquals(0, cache.values().size());
      populate(MAX_SIZE);
      assertEquals(MAX_SIZE, cache.values().size());
      cache.put(makeKey(MAX_SIZE + 1), makeValue(MAX_SIZE + 1));
      assertEquals(MAX_SIZE, cache.values().size());
   }


   public void testGetMaxSizeMBytes() {

      assertEquals(0, cache.getMaxSizeBytes());
   }


   public void testAddEntryEvictedSubscriber() {

      final HashSet<String> interestKeys = new HashSet<String>(MAX_SIZE);

      for (int i = 0; i <= MAX_SIZE; i++) {
         interestKeys.add(makeKey(i));
      }

      final TestEntryEvictedSubscriber listener = new TestEntryEvictedSubscriber();
      cache.addEventSubscriber(interestKeys, listener);

      for (int i = 0; i <= MAX_SIZE; i++) {
         cache.put(makeKey(i), makeValue(i));
      }

      assertTrue(listener.called);
      assertEquals(makeKey(0), listener.getEvictedKey());
      assertEquals(makeValue(0), listener.getEvictedValue());
   }


   /**
    *
    */
   public void testRemoveAll() {

      // Prepare test data
      final int keyCount = MAX_SIZE;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(makeKey(i), makeValue(i));
      }

      cache.putAll(map);

      // Prepare key set to remove
      final int keySetCount = keyCount - 100;
      final Set<String> keySetToRemove = new HashSet<String>(keySetCount);
      for (int i = 0; i < keySetCount; i++) {
         keySetToRemove.add(makeKey(i));
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
   public void testRetainAll() {

      // Prepare test data
      final int keyCount = MAX_SIZE;
      final Map<String, String> map = new HashMap<String, String>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(makeKey(i), makeValue(i));
      }

      // Put to cache
      cache.putAll(map);

      // Prepare key set to retain
      final int keySetCount = keyCount - 100;
      final Set<String> keysToRetain = new HashSet<String>(keySetCount);
      for (int i = 0; i < keySetCount; i++) {
         keysToRetain.add(makeKey(i));
      }

      // Remove and assert
      assertEquals(map.size(), cache.size());
      assertEquals(keysToRetain.size(), keySetCount);
      assertTrue("First run should modify", cache.retainAll(keysToRetain));

      for (final Map.Entry<String, String> entry : cache.entrySet()) {
         assertTrue(keysToRetain.contains(entry.getKey()));
      }

      assertEquals(keysToRetain.size(), cache.size());
      assertTrue("Second run should *not* modify", !cache.retainAll(keysToRetain));
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


   public void testPutTimedWitNonZeroDelayProducesExpiringElement() throws InterruptedException {

      final long expirationIntervalMillis = 50L;

      cache().put(KEY_0, OBJECT_0, expirationIntervalMillis, TimeUnit.MILLISECONDS);

      // Wait for the expiration time to pass.
      sleep(expirationIntervalMillis * 2L);

      // Assert the element is still gone.
      assertNull(cache().get(KEY_0));
   }


   public void testPutTimedWitZeroDelayProducesNonexpiringElement() throws InterruptedException {

      final long expirationIntervalMillis = 50L;
      final Cache<String, String> cacheWithExpiration = new LocalCache<String, String>(LOCAL_TEST_CACHE, MAX_SIZE, 0,
              expirationIntervalMillis, 0, getClock(), getEventNotificationExecutor(), DUMMY_DISK_STORAGE,
              DUMMY_OBJECT_SIZE_CALCULATOR, DUMMY_BINARY_STORE_DATA_SOURCE, DUMMY_DATA_STORE,
              DUMMY_CACHE_INVALIDATOR, DUMMY_CACHE_LOADER, SYNCHRONOUS);
      cacheWithExpiration.put(KEY_0, OBJECT_0, 0, TimeUnit.MILLISECONDS);

      // Wait for the expiration time to pass.
      sleep(expirationIntervalMillis * 2L);

      // Assert the element is still there.
      assertEquals(OBJECT_0, cacheWithExpiration.get(KEY_0));
   }


   private List<Entry<String, String>> populate(final int maxSize) {

      final List<Entry<String, String>> entries = new ArrayList<Entry<String, String>>(maxSize);
      for (int i = 0; i < maxSize; i++) {
         final Entry<String, String> e = new Entry<String, String>(makeKey(i), makeValue(i));
         cache.put(e.getKey(), e.getValue());
         entries.add(e);
      }
      return entries;
   }


   private static String makeKey(final int i) {

      return KEY + i;
   }


   private static String makeValue(final int i) {

      return VALUE + i;
   }


   /**
    * Helper method.
    *
    * @param key          a key
    * @param valueOne     a value
    * @param valueTwo     a value
    * @param expectedSize an expected size
    */
   private void putAndAssert(final String key, final String valueOne, final String valueTwo,
           final int expectedSize) {

      assertNull(cache.put(key, valueOne));
      assertEquals(valueOne, cache.put(key, valueTwo));
      assertEquals(expectedSize, cache.size());
   }


   protected void setUp() throws Exception {

      super.setUp();
      cache = new LocalCache<String, String>(LOCAL_TEST_CACHE, MAX_SIZE, 0, 0, 0,
              getClock(), getEventNotificationExecutor(), DUMMY_DISK_STORAGE,
              DUMMY_OBJECT_SIZE_CALCULATOR, DUMMY_BINARY_STORE_DATA_SOURCE, DUMMY_DATA_STORE,
              DUMMY_CACHE_INVALIDATOR, DUMMY_CACHE_LOADER, SYNCHRONOUS);

   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      cache.shutdown();
      super.tearDown();
   }

   /**
    *
    */
   private static final class TestEntryEvictedSubscriber implements EntryModifiedSubscriber {

      private boolean called = false;

      private Object evictedValue;

      private Object evictedKey;


      /**
       * Returns <code>called</code> flag.
       *
       * @return the <code>called</code> flag.
       */
      public boolean isCalled() {

         return called;
      }


      public void notifyKeysUpdated(final List<EntryModifiedEvent> events) {

         called = true;

         evictedValue = events.get(0).getPreviousValue();
         evictedKey = events.get(0).getUpdatedKey();
      }


      public EntryModifiedNotificationMode getNotificationMode() {

         return EntryModifiedNotificationMode.SINGLE;
      }


      public Set<EntryModifiedEventType> getModificationTypes() {


         final HashSet<EntryModifiedEventType> eventTypes = new HashSet<EntryModifiedEventType>(4, 0.75f);
         eventTypes.add(EntryModifiedEventType.EVICT);
         return eventTypes;
      }


      public List<EntryModifiedEventContentFlag> getEventContentFlags() {

         final ArrayList<EntryModifiedEventContentFlag> entryModifiedEventContentFlags = new ArrayList<EntryModifiedEventContentFlag>(
                 1);
         entryModifiedEventContentFlags.add(EntryModifiedEventContentFlag.NEED_ALL);
         return entryModifiedEventContentFlags;
      }


      public Object getEvictedValue() {

         return evictedValue;
      }


      public Object getEvictedKey() {

         return evictedKey;
      }
   }
}
