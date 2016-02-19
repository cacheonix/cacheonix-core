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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.cache.store.BinaryStore;
import org.cacheonix.impl.cache.store.SharedCounter;
import org.cacheonix.impl.cache.util.DummyObjectSizeCalculator;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.logging.Logger;

/**
 * TransferBucketRequestTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 14, 2009 3:31:35 PM
 */
public final class TransferBucketRequestTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TransferBucketRequestTest.class); // NOPMD

   private static final long LEASE_DURATION_MILLIS = 5L;

   private static final int BUCKET_NUMBER = 1;

   private static final ClusterNodeAddress SENDER = TestUtils.createTestAddress(BUCKET_NUMBER);

   private static final ClusterNodeAddress NEW_OWNER = TestUtils.createTestAddress(2);

   private static final ClusterNodeAddress CURRENT_OWNER = TestUtils.createTestAddress(3);

   private static final String MY_CACHE = "my.cache";

   private static final byte SOURCE_STORAGE_NUMBER = 1;

   private static final byte DESTINATION_STORAGE_NUMBER = 2;

   private TransferBucketRequest request;


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new TransferBucketRequest().toString());
   }


   @SuppressWarnings("TooBroadScope")
   public void testSetGetBucket() {

      final int bucketNumber = BUCKET_NUMBER;
      final BinaryStore keyStore = createKeyStore();
      final Bucket bucket = new Bucket(BUCKET_NUMBER, keyStore, LEASE_DURATION_MILLIS);
      request.addBucket(bucket);
      assertEquals(bucket, request.getOrCreateBuckets(1).get(0));
      assertEquals(Integer.valueOf(bucketNumber), request.getBucketNumbers().get(0));
   }


   public void testSetSourceStorageNumber() {

      assertEquals(SOURCE_STORAGE_NUMBER, request.getSourceStorageNumber());
   }


   public void testSetDestinationStorageNumber() {

      assertEquals(DESTINATION_STORAGE_NUMBER, request.getDestinationStorageNumber());
   }


   public void testHashCode() {

      assertTrue(request.hashCode() != 0);
   }


   public void testToString() {

      assertNotNull(request.toString());
   }


   public void testCreate() {

      assertTrue(request.getOrCreateBuckets(1).isEmpty());
      assertTrue(request.getBucketNumbers().isEmpty());
      assertEquals(SOURCE_STORAGE_NUMBER, request.getSourceStorageNumber());
      assertEquals(DESTINATION_STORAGE_NUMBER, request.getDestinationStorageNumber());
   }


   public void testSerializeDeserialize() throws IOException {

      final BinaryStore keyStore = createKeyStore();
      request.addBucket(new Bucket(BUCKET_NUMBER, keyStore, LEASE_DURATION_MILLIS));
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
   }


   private BinaryStore createKeyStore() {

      // Test context
      final BinaryStoreContext context = new BinaryStoreContextImpl();
      context.setObjectSizeCalculator(new DummyObjectSizeCalculator());
      context.setDiskStorage(new DummyDiskStorage("test.cache"));
      context.setDataSource(new DummyBinaryStoreDataSource());
      context.setInvalidator(new DummyCacheInvalidator());
      context.setDataStore(new DummyDataStore());

      final BinaryStore keyStore = new BinaryStore(getClock(), Integer.MAX_VALUE, Integer.MAX_VALUE);
      keyStore.attachToByteCounter(new SharedCounter(0L));
      keyStore.attachToElementCounter(new SharedCounter(0L));
      keyStore.setContext(context);

      return keyStore;
   }


   protected void setUp() throws Exception {

      super.setUp();
      request = new TransferBucketRequest(MY_CACHE);
      request.setSourceStorageNumber(SOURCE_STORAGE_NUMBER);
      request.setDestinationStorageNumber(DESTINATION_STORAGE_NUMBER);
      request.setSender(SENDER);
      request.setNewOwner(NEW_OWNER);
      request.setReceiver(NEW_OWNER);
      request.setCurrentOwner(CURRENT_OWNER);
   }


   public String toString() {

      return "TransferBucketRequestTest{" +
              "request=" + request +
              "} " + super.toString();
   }
}
