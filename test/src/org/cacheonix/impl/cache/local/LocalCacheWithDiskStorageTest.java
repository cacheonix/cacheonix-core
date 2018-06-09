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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.CacheonixCache;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.cache.storage.disk.StorageFactory;
import org.cacheonix.impl.cache.util.StandardObjectSizeCalculator;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.TestConstants.LOCAL_TEST_CACHE;
import static org.cacheonix.impl.config.ElementEventNotification.SYNCHRONOUS;

/**
 * Tests {@link LocalCacheWithDiskStorageTest}
 *
 * @noinspection FieldNotUsedInToString, FieldCanBeLocal
 */
public final class LocalCacheWithDiskStorageTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalCacheWithDiskStorageTest.class); // NOPMD

   private static final String STORAGE_PATH = getTestFile("storage_"
           + LocalCacheWithDiskStorageTest.class.getName() + ".dat").toString();


   private static final String KEY = "key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key_key";

   private static final String VALUE = "value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value_value";

   private static final int ESTIMATED_ENTRY_SIZE = calculateEstimatedEntrySize();


   private static final String SUFFIX_ONE = "_1";

   private static final String SUFFIX_TWO = "_2";

   private static final int MAX_SIZE_BYTES_ON_DISK = 10000000; // 10 million bytes

   public static final int MAX_SIZE_BYTES = MAX_SIZE_BYTES_ON_DISK * 3;

   /**
    * Max size allows for twice more than allowed bytes.
    */
   private static final int MAX_SIZE_ELEMENTS = MAX_SIZE_BYTES_ON_DISK / ESTIMATED_ENTRY_SIZE * 2;


   private static final int ZERO_EXPIRATION_TIME_MILLIS = 0;

   private static final int ZERO_IDLE_TIME_MILLIS = 0;


   /**
    * Tests {@link LocalCache#put(Object, Object)} evicts to disk
    */
   public void testPut() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);
      try {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Cache size: " + MAX_SIZE_ELEMENTS);
         }

         for (int i = 0; i < MAX_SIZE_ELEMENTS; i++) {

            final ByteArrayKey key = makeKey(i);
            final byte[] valueOne = makeValue(i, SUFFIX_ONE);
            assertNull(cache.put(key, makeValue(i, SUFFIX_ONE)));
            assertEquals(valueOne, cache.get(key));
            assertEquals(valueOne, cache.put(key, makeValue(i, SUFFIX_TWO)));
         }

         assertEquals(MAX_SIZE_ELEMENTS, cache.size());

         assertEquals("Number of elements evicted to disk", 21925, cache.getSizeOnDisk());
      } finally {
         cache.shutdown();
      }
   }


   /**
    * Tests local cache behaviour with zero max element size and bytes passed to the constructor. The expect behaviour is that all
    * data goes to memory and on to disk becuase zero max element size means 'unlimited'.
    */
   public void testGetZeroMaxElementsCache() throws StorageException {


      // Create a cache with zero max elements
      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(0, 0);
      try {

         final List<Entry<ByteArrayKey, byte[]>> entries = populate(MAX_SIZE_ELEMENTS, cache);
         for (int i = 0; i < MAX_SIZE_ELEMENTS; i++) {
            final Entry<ByteArrayKey, byte[]> e = entries.get(i);
            assertEquals(e.getValue(), cache.get(e.getKey()));
         }

         // Size on disk should be zero
         final long sizeOnDisk = cache.getSizeOnDisk();
         assertEquals("Size on disk", 0L, sizeOnDisk);

         assertNull(cache.get(makeKey(999999999)));

      } finally {

         cache.shutdown();
      }
   }


   /**
    * Tests {@link LocalCache#clear()}
    */
   public void testClear() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);
      try {
         populate(MAX_SIZE_ELEMENTS, cache);
         assertEquals(MAX_SIZE_ELEMENTS, cache.size());
         cache.clear();
         assertEquals(0, cache.size());
      } finally {

         cache.shutdown();
      }
   }


   /**
    * Tests {@link LocalCache#clear()}
    */
   public void testContainsKey() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);

      try {
         populate(MAX_SIZE_ELEMENTS, cache);
         assertTrue(cache.containsKey(makeKey(0)));

         cache.clear();
         assertTrue(cache.isEmpty());

         cache.put(makeKey(0), makeValue(0));
         assertTrue(cache.containsKey(makeKey(0)));
         assertFalse(cache.containsKey(makeKey(999999999)));
      } finally {

         cache.shutdown();
      }
   }


   public void testEntrySet() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);

      try {
         populate(MAX_SIZE_ELEMENTS, cache);
         assertEquals(MAX_SIZE_ELEMENTS, cache.entrySet().size());
      } finally {

         cache.shutdown();
      }
   }


   public void testIsEmpty() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);

      try {
         assertTrue(cache.isEmpty());
         populate(MAX_SIZE_ELEMENTS, cache);
         assertFalse(cache.isEmpty());
      } finally {

         cache.shutdown();
      }
   }


   public void testKeySet() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);

      try {
         populate(MAX_SIZE_ELEMENTS, cache);
         assertEquals(MAX_SIZE_ELEMENTS, cache.keySet().size());
      } finally {

         cache.shutdown();
      }
   }


   public void testPutAll() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);

      try {
         final int overflownMaxSize = MAX_SIZE_ELEMENTS + 1;
         final Map<ByteArrayKey, byte[]> entries = new HashMap<ByteArrayKey, byte[]>(overflownMaxSize);
         for (int i = 0; i < overflownMaxSize; i++) {
            entries.put(makeKey(i), makeValue(i));
         }
         cache.putAll(entries);
         assertEquals(MAX_SIZE_ELEMENTS, cache.size());
      } finally {

         cache.shutdown();
      }
   }


   public void testRemove() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);
      try {
         populate(MAX_SIZE_ELEMENTS, cache);
         final ByteArrayKey key = makeKey(0);
         final byte[] value = makeValue(0);
         assertEquals(value, cache.remove(key));
         assertEquals(MAX_SIZE_ELEMENTS - 1, cache.size());
         assertNull(cache.get(key));
      } finally {

         cache.shutdown();
      }
   }


   public void testSize() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);

      try {
         populate(MAX_SIZE_ELEMENTS, cache);
         assertEquals(MAX_SIZE_ELEMENTS, cache.size());
         cache.put(makeKey(MAX_SIZE_ELEMENTS + 1), makeValue(MAX_SIZE_ELEMENTS + 1));
         assertEquals(MAX_SIZE_ELEMENTS, cache.size());
      } finally {

         cache.shutdown();
      }
   }


   public void testValues() throws StorageException {

      final LocalCache<ByteArrayKey, byte[]> cache = createLocalCache(MAX_SIZE_ELEMENTS, MAX_SIZE_BYTES);
      try {
         populate(MAX_SIZE_ELEMENTS, cache);
         assertEquals(MAX_SIZE_ELEMENTS, cache.values().size());
         cache.put(makeKey(MAX_SIZE_ELEMENTS + 1), makeValue(MAX_SIZE_ELEMENTS + 1));
         assertEquals(MAX_SIZE_ELEMENTS, cache.values().size());

      } finally {

         cache.shutdown();
      }
   }


   private LocalCache<ByteArrayKey, byte[]> createLocalCache(final int maxSizeElements,
           final int maxSizeBytes) throws StorageException {

      final DiskStorage diskStorage = StorageFactory.createStorage(LOCAL_TEST_CACHE, (long) MAX_SIZE_BYTES_ON_DISK,
              STORAGE_PATH);

      return new LocalCache<ByteArrayKey, byte[]>(LOCAL_TEST_CACHE, maxSizeElements, maxSizeBytes,
              ZERO_EXPIRATION_TIME_MILLIS, ZERO_IDLE_TIME_MILLIS, getClock(), getEventNotificationExecutor(),
              diskStorage, new StandardObjectSizeCalculator(), new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), SYNCHRONOUS);
   }


   private static List<Entry<ByteArrayKey, byte[]>> populate(final int maxSize,
           final CacheonixCache<ByteArrayKey, byte[]> cache) {

      final List<Entry<ByteArrayKey, byte[]>> entries = new ArrayList<Entry<ByteArrayKey, byte[]>>(maxSize);
      for (int i = 0; i < maxSize; i++) {
         final Entry<ByteArrayKey, byte[]> e = new Entry<ByteArrayKey, byte[]>(makeKey(i), makeValue(i));
         cache.put(e.getKey(), e.getValue());
         entries.add(e);
      }
      return entries;
   }


   private static ByteArrayKey makeKey(final int i) {

      return new ByteArrayKey((i + KEY).getBytes());
   }


   private static byte[] makeValue(final int i) {

      return (i + VALUE).getBytes();
   }


   private static byte[] makeValue(final int i, final String suffix) {

      return (i + VALUE + suffix).getBytes();
   }


   private static File getTestFile(final String name) {

      try {
         return TestUtils.getTestFile(name);
      } catch (final IOException e) {
         throw ExceptionUtils.createIllegalArgumentException(e);
      }
   }


   private static int calculateEstimatedEntrySize() {

      final StandardObjectSizeCalculator sizeCalculator = new StandardObjectSizeCalculator();
      return (int) sizeCalculator.sum(0L, sizeCalculator.sizeOf(makeKey(1)), sizeCalculator.sizeOf(makeValue(1)));
   }
}
