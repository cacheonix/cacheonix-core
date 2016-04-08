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

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.storage.disk.StorageFactory;
import org.cacheonix.impl.cache.util.StandardObjectSizeCalculator;
import org.cacheonix.impl.config.ElementEventNotification;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

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

   private static final int MAX_SIZE_BYTES = 10000000; // 10 million bytes

   /**
    * Max size allows for twice more than allowed bytes.
    */
   private static final int MAX_SIZE = MAX_SIZE_BYTES / ESTIMATED_ENTRY_SIZE * 2;

   private LocalCache<ByteArrayKey, byte[]> cache = null;

   private DiskStorage diskStorage = null;


   private static final int ZERO_EXPIRATION_TIME_MILLIS = 0;

   private static final int ZERO_IDLE_TIME_MILLIS = 0;


   /**
    * Tests {@link LocalCache#put(Object, Object)} evicts to disk
    */
   public void testPut() {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Cache size: " + MAX_SIZE);
      }

      for (int i = 0; i < MAX_SIZE; i++) {

         final ByteArrayKey key = makeKey(i);
         final byte[] valueOne = makeValue(i, SUFFIX_ONE);
         assertNull(cache.put(key, makeValue(i, SUFFIX_ONE)));
         assertEquals(valueOne, cache.get(key));
         assertEquals(valueOne, cache.put(key, makeValue(i, SUFFIX_TWO)));
      }

      assertEquals(MAX_SIZE, cache.size());

      assertEquals("Number of elements evicted to disk", 21925, cache.getSizeOnDisk());
   }


   /**
    * Tests {@link LocalCache#get(Object)}
    */
   public void testGet() {

      final List<Entry<ByteArrayKey, byte[]>> entries = populate(MAX_SIZE);
      for (int i = 0; i < MAX_SIZE; i++) {
         final Entry<ByteArrayKey, byte[]> e = entries.get(i);
         assertEquals(e.getValue(), cache.get(e.getKey()));
      }
      assertNull(cache.get(makeKey(999999999)));
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

      cache.clear();
      assertTrue(cache.isEmpty());

      cache.put(makeKey(0), makeValue(0));
      assertTrue(cache.containsKey(makeKey(0)));
      assertFalse(cache.containsKey(makeKey(999999999)));
   }


   public void testEntrySet() {

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
      final Map<ByteArrayKey, byte[]> entries = new HashMap<ByteArrayKey, byte[]>(overflownMaxSize);
      for (int i = 0; i < overflownMaxSize; i++) {
         entries.put(makeKey(i), makeValue(i));
      }
      cache.putAll(entries);
      assertEquals(MAX_SIZE, cache.size());
   }


   public void testRemove() {

      populate(MAX_SIZE);
      final ByteArrayKey key = makeKey(0);
      final byte[] value = makeValue(0);
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

      populate(MAX_SIZE);
      assertEquals(MAX_SIZE, cache.values().size());
      cache.put(makeKey(MAX_SIZE + 1), makeValue(MAX_SIZE + 1));
      assertEquals(MAX_SIZE, cache.values().size());
   }


   private List<Entry<ByteArrayKey, byte[]>> populate(final int maxSize) {

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


   protected void setUp() throws Exception {

      super.setUp();

      diskStorage = StorageFactory.createStorage(TestConstants.LOCAL_TEST_CACHE, (long) MAX_SIZE_BYTES, STORAGE_PATH);

      cache = new LocalCache<ByteArrayKey, byte[]>(TestConstants.LOCAL_TEST_CACHE, MAX_SIZE, MAX_SIZE_BYTES * 3,
              ZERO_EXPIRATION_TIME_MILLIS, ZERO_IDLE_TIME_MILLIS, getClock(), getEventNotificationExecutor(),
              diskStorage, new StandardObjectSizeCalculator(), new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      cache.shutdown();
      super.tearDown();
   }


   private static File getTestFile(final String name) {

      try {
         return TestUtils.getTestFile(name);
      } catch (final IOException e) {
         throw ExceptionUtils.createIllegalArgumentException(e);
      }
   }


   public String toString() {

      return "LocalCacheTest{" +
              "cache=" + cache +
              '}';
   }


   private static int calculateEstimatedEntrySize() {

      final StandardObjectSizeCalculator sizeCalculator = new StandardObjectSizeCalculator();
      return (int) sizeCalculator.sum(0L, sizeCalculator.sizeOf(makeKey(1)), sizeCalculator.sizeOf(makeValue(1)));
   }


   @SuppressWarnings("RedundantIfStatement")
   private static final class ByteArrayKey implements Externalizable {

      private static final long serialVersionUID = 3595907779192315311L;

      private byte[] content;


      public ByteArrayKey(final byte[] content) {

         this.content = ArrayUtils.copy(content);
      }


      public ByteArrayKey() {

      }


      public byte[] getContent() {

         return ArrayUtils.copy(content);
      }


      public void writeExternal(final ObjectOutput out) throws IOException {

         SerializerUtils.writeByteArray(out, content);
      }


      public void readExternal(final ObjectInput in) throws IOException {

         content = SerializerUtils.readByteArray(in);
      }


      public boolean equals(final Object o) {

         if (this == o) {
            return true;
         }
         if (o == null || getClass() != o.getClass()) {
            return false;
         }

         final ByteArrayKey testKey = (ByteArrayKey) o;

         if (!Arrays.equals(content, testKey.content)) {
            return false;
         }

         return true;
      }


      public int hashCode() {

         return Arrays.hashCode(content);
      }


      public String toString() {

         return "ByteArrayKey{" +
                 "content=" + Arrays.toString(content) +
                 '}';
      }
   }
}
