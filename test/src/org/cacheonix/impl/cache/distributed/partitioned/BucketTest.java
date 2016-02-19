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

import java.io.IOException;
import java.util.Map;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.cache.store.BinaryStore;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.cache.store.SharedCounter;
import org.cacheonix.impl.cache.util.DummyObjectSizeCalculator;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BucketTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 14, 2009 2:57:49 PM
 */
public final class BucketTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketTest.class); // NOPMD

   private static final long LEASE_DURATION_MILLIS = 5L;

   private static final int BUCKET_NUMBER = 1;

   private Time expirationTime = null;

   private Bucket bucket;

   private Binary key;

   private Binary value;


   public void testGet() throws InvalidObjectException, StorageException {

      assertNull(bucket.put(key, value, expirationTime));
      assertEquals(value, bucket.get(key).getValue());
   }


   public void testPut() {

      assertNull(bucket.put(key, value, expirationTime));
      assertEquals(value, bucket.put(key, value, expirationTime));
   }


   public void testHashCode() {

      bucket.put(key, value, expirationTime);
      assertTrue(bucket.hashCode() != 0);
   }


   public void testRetainAll() {

      // Prepare test data
      final int keyCount = 5000;
      final Map<Binary, Binary> map = new HashMap<Binary, Binary>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(toBinary(i), toBinary(i));
      }

      // Put to cache
      bucket.putAll(map);

      // Prepare key set to retain
      final int keySetCount = keyCount / 11;
      final HashSet<Binary> keySetToRetain = new HashSet<Binary>(keyCount);
      for (int i = 0; i < keySetCount; i++) {
         keySetToRetain.add(toBinary(Integer.valueOf(i)));
      }

      // Remove and assert
      assertTrue("First run should modify", bucket.retainAll(keySetToRetain));
      assertTrue("Second run should *not* modify", !bucket.retainAll(keySetToRetain));
      assertEquals(keySetToRetain.size(), bucket.size());
      assertEquals(keySetToRetain, bucket.keySet());
   }


   public void testGetLeaseDurationMills() {

      assertEquals(LEASE_DURATION_MILLIS, bucket.getLeaseDurationMillis());
   }


   public void testSetGetLeaseExpirationTime() {

      final Time time = getClock().currentTime();
      bucket.setLeaseExpirationTime(time);
      assertEquals(time, bucket.getLeaseExpirationTime());
   }


   public void testToString() {

      assertNotNull(bucket.toString());
   }


   public void testSerializeDeserialize() throws IOException {

      bucket.put(key, value, expirationTime);
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(bucket, ser.deserialize(ser.serialize(bucket)));
   }


   public void testUpdate() throws Exception {

      bucket.put(key, value, expirationTime);

      final Binary newValue = toBinary("new.value");
      final Time timeToRead = new TimeImpl(10, 0);
      final ReadableElement previousElement = bucket.update(key, newValue, timeToRead, 0);
      assertNotNull(previousElement);
   }


   protected void setUp() throws Exception {

      super.setUp();

      expirationTime = getClock().currentTime().add(1000L);

      // Test context
      final BinaryStoreContext context = new BinaryStoreContextImpl();
      context.setObjectSizeCalculator(new DummyObjectSizeCalculator());
      context.setDiskStorage(new DummyDiskStorage("test.cache"));
      context.setDataSource(new DummyBinaryStoreDataSource());
      context.setInvalidator(new DummyCacheInvalidator());
      context.setDataStore(new DummyDataStore());

      final BinaryStore keyStore = new BinaryStore(getClock(), Integer.MAX_VALUE, Integer.MAX_VALUE);
      keyStore.attachToElementCounter(new SharedCounter(0L));
      keyStore.attachToByteCounter(new SharedCounter(0L));
      keyStore.setContext(context);

      key = TestUtils.toBinary("key");
      value = TestUtils.toBinary("value");
      bucket = new Bucket(BUCKET_NUMBER, keyStore, LEASE_DURATION_MILLIS);
   }


   protected void tearDown() throws Exception {

      bucket = null;
      value = null;
      key = null;
      expirationTime = null;

      super.tearDown();
   }


   public String toString() {

      return "BucketTest{" +
              "bucket=" + bucket +
              ", key=" + key +
              ", value=" + value +
              "} " + super.toString();
   }
}
