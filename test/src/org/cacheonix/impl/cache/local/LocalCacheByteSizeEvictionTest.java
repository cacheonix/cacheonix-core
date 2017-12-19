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
import java.util.List;
import java.util.Map;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.cache.util.ObjectSizeCalculator;
import org.cacheonix.impl.cache.util.StandardObjectSizeCalculator;
import org.cacheonix.impl.config.ElementEventNotification;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tests {@link LocalCacheByteSizeEvictionTest}
 *
 * @noinspection FieldNotUsedInToString, FieldCanBeLocal
 */
public final class LocalCacheByteSizeEvictionTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalCacheByteSizeEvictionTest.class); // NOPMD

   private static final String KEY = "key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key";

   private static final String VALUE = "value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value";

   private static final String SUFFIX_ONE = "_1";

   private static final int MAX_SIZE_BYTES = 1000000; // 1 million bytes;

   private static final int ESTIMATED_ENTRY_SIZE = calculateEstimatedEntrySize();

   /**
    * Twice bigger than max size bytes, guarantees eviction based on the object size
    */
   private static final int CACHE_MAX_SIZE = (MAX_SIZE_BYTES / ESTIMATED_ENTRY_SIZE) * 2;

   /**
    * Smaller then maximum size that would evict based on size.
    */
   private static final int CACHE_SMALL_MAX_SIZE = (MAX_SIZE_BYTES / ESTIMATED_ENTRY_SIZE) / 2;

   /**
    * Guaranteed eviction based ob\n the object count.
    */
   private static final int PUT_SIZE = (int) (CACHE_MAX_SIZE * 1.1);

   private static final int NEVER_EXISTED_VALUE = 999999;

   private static final int ZERO_EXPIRATION_TIME_MILLIS = 0;

   private static final int ZERO_IDLE_TIME_MILLIS = 0;

   private static final int EXPECTED_SIZE_1225 = 1225;

   private LocalCache<String, String> cache = null;


   @SuppressWarnings("RedundantStringConstructorCall")
   private static int calculateEstimatedEntrySize() {

      final StandardObjectSizeCalculator sizeCalculator = new StandardObjectSizeCalculator();
      final long keySize = sizeCalculator.sizeOf(new String(KEY + 1));
      final long valueSize = sizeCalculator.sizeOf(new String(VALUE + 1));
      return (int) sizeCalculator.sum(0L, keySize, valueSize);
   }


   private static String makeKey(final int i) {

      return KEY + i;
   }


   private static String makeValue(final int i) {

      return VALUE + i;
   }


   /**
    * Tests {@link LocalCache#put(Object, Object)}
    */
   public void testPut() {

      for (int i = 0; i < PUT_SIZE; i++) {
         final String key = makeKey(i);
         final String valueOne = makeValue(i) + SUFFIX_ONE;
         assertNull(cache.put(key, valueOne));
         assertEquals(valueOne, cache.get(key));
         assertTrue(cache.getSizeBytes() > 0);
         assertTrue(cache.getSizeBytes() <= MAX_SIZE_BYTES);
      }
      assertEquals(1213, cache.size());

      // Check how clear behaves
      cache.clear();
      assertEquals(0, cache.getSizeBytes());
   }


   /**
    * Tests {@link LocalCache#get(Object)}
    */
   public void testGet() {

      final List<Entry<String, String>> entries = populate(CACHE_SMALL_MAX_SIZE);
      for (int i = 0; i < CACHE_SMALL_MAX_SIZE; i++) {
         final Entry<String, String> e = entries.get(i);
         assertEquals(e.getValue(), cache.get(e.getKey()));
      }
      assertNull(cache.get(KEY + "never existed"));
   }


   /**
    * Tests {@link LocalCache#clear()}
    */
   public void testClear() {

      populate(PUT_SIZE);
      assertEquals(EXPECTED_SIZE_1225, cache.size());
      cache.clear();
      assertEquals(0, cache.size());
      assertEquals(0, cache.getSizeBytes());
   }


   /**
    * Tests {@link LocalCache#clear()}
    */
   public void testContainsKey() {

      populate(CACHE_SMALL_MAX_SIZE);
      assertTrue(cache.containsKey(makeKey(0)));
   }


   public void testContainsValue() {

      final String value = makeValue(0);
      cache.put(makeKey(0), value);
      assertTrue(cache.containsValue(value));
      assertFalse(cache.containsValue(makeValue(NEVER_EXISTED_VALUE)));
   }


   public void testEntrySet() {

      populate(PUT_SIZE);
      assertEquals(EXPECTED_SIZE_1225, cache.entrySet().size());
   }


   public void testIsEmpty() {

      assertTrue(cache.isEmpty());
      populate(PUT_SIZE);
      assertFalse(cache.isEmpty());
   }


   public void testKeySet() {

      populate(PUT_SIZE);
      assertEquals(EXPECTED_SIZE_1225, cache.keySet().size());
   }


   public void testPutAll() {

      final int overflownMaxSize = PUT_SIZE + 1;
      final Map<String, String> entries = new HashMap<String, String>(overflownMaxSize);
      for (int i = 0; i < overflownMaxSize; i++) {
         entries.put(makeKey(i), makeValue(i));
      }
      cache.putAll(entries);
      assertEquals(1225, cache.size());
   }


   public void testRemove() {

      populate(CACHE_SMALL_MAX_SIZE);
      final String key = makeKey(0);
      final String value = makeValue(0);
      assertEquals(value, cache.remove(key));
      assertEquals(821, cache.size());
      assertNull(cache.get(key));
   }


   public void testSize() {

      populate(PUT_SIZE);
      assertEquals(EXPECTED_SIZE_1225, cache.size());
      cache.put(makeKey(PUT_SIZE + 1), makeValue(PUT_SIZE + 1));
      assertEquals(EXPECTED_SIZE_1225, cache.size());
   }


   public void testValues() {

      populate(PUT_SIZE);
      assertEquals(EXPECTED_SIZE_1225, cache.values().size());
      cache.put(makeKey(PUT_SIZE + 1), makeValue(PUT_SIZE + 1));
      assertEquals(EXPECTED_SIZE_1225, cache.values().size());
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


   protected void setUp() throws Exception {

      super.setUp();
      final DiskStorage diskStorage = new DummyDiskStorage(TestConstants.LOCAL_TEST_CACHE);
      final ObjectSizeCalculator objectSizeCalculator = new StandardObjectSizeCalculator();
      cache = new LocalCache<String, String>(TestConstants.LOCAL_TEST_CACHE, CACHE_MAX_SIZE, MAX_SIZE_BYTES,
              ZERO_EXPIRATION_TIME_MILLIS, ZERO_IDLE_TIME_MILLIS, getClock(), getEventNotificationExecutor(),
              diskStorage, objectSizeCalculator, new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      cache.shutdown();
      super.tearDown();
   }
}
