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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
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
import org.cacheonix.impl.config.ElementEventNotification;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;

/**
 * Tests {@link LocalCacheStoreKeyValueByValueTest}
 */
public final class LocalCacheStoreKeyValueByValueTest extends CacheonixTestCase {

   private static final String KEY = "key";

   private static final String VALUE = "value";

   private static final String SUFFIX_ONE = "_1";

   private static final String SUFFIX_TWO = "_2";

   private static final int MAX_SIZE = 3;


   private LocalCache<String, String> cache;


   private static String makeKey(final int i) {

      return KEY + i;
   }


   private static String makeValue(final int i) {

      return VALUE + i;
   }


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

      populate(MAX_SIZE);
      assertEquals(MAX_SIZE, cache.keySet().size());
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


   public void testAddListener() {

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
   }


   public void testGetMaxSizeMBytes() {

      assertEquals(0, cache.getMaxSizeBytes());
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


   /**
    * Helper method.
    *
    * @param key          a key
    * @param valueOne     a value.
    * @param valueTwo     a value
    * @param expectedSize expected size.
    */
   private void putAndAssert(final String key, final String valueOne, final String valueTwo,
           final int expectedSize) {

      assertNull(cache.put(key, valueOne));
      assertEquals(valueOne, cache.put(key, valueTwo));
      assertEquals(expectedSize, cache.size());
   }


   protected void setUp() throws Exception {

      super.setUp();
      cache = new LocalCache<String, String>(TestConstants.LOCAL_TEST_CACHE, MAX_SIZE, 0, 0, 0,
              getClock(), getEventNotificationExecutor(), new DummyDiskStorage(TestConstants.LOCAL_TEST_CACHE),
              new DummyObjectSizeCalculator(), new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);
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
   private static class TestEntryEvictedSubscriber implements EntryModifiedSubscriber {

      private boolean called = false;


      /**
       * Returns <code>called</code> flag.
       *
       * @return <code>called</code> flag.
       */
      public boolean isCalled() {

         return called;
      }


      public void notifyKeysUpdated(final List<EntryModifiedEvent> events) {

         called = true;
         throw new RuntimeException("Test exception");
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

         return Collections.emptyList();
      }


      public String toString() {

         return "TestEntryEvictedSubscriber{" +
                 "called=" + called +
                 '}';
      }
   }
}
