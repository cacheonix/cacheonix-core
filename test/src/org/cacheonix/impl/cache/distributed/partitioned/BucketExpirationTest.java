/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.BinaryStore;
import org.cacheonix.impl.cache.store.SharedCounter;
import org.cacheonix.impl.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.util.cache.DummyObjectSizeCalculator;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A tester for key expiration in a bucket with configured expiration.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 */
public final class BucketExpirationTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketExpirationTest.class); // NOPMD

   private static final long EXPIRATION_INTERVAL_MILLIS = 50L;

   private static final long LEASE_DURATION_MILLIS = 5L;

   private static final int BUCKET_NUMBER = 1;


   private Bucket bucket;

   private Binary key;

   private Binary value;


   public void testPutWithNullExpiration() throws InterruptedException {

      bucket.put(key, value, null);
      assertTrue(bucket.containsKey(key));

      // Wait twice the configured expiration time
      Thread.sleep(EXPIRATION_INTERVAL_MILLIS * 2L);

      // Assert key is gone
      assertFalse(bucket.containsKey(key));
   }


   public void testPutWithSetExpiration() throws InterruptedException {

      // Set expiration as a triple of the configured time
      bucket.put(key, value, getClock().currentTime().add(EXPIRATION_INTERVAL_MILLIS * 3));
      assertTrue(bucket.containsKey(key));

      // Wait twice the configured expiration time and assert the key is still there
      Thread.sleep(EXPIRATION_INTERVAL_MILLIS * 2L);
      assertTrue(bucket.containsKey(key));

      // Wait beyond the expiration set at put and assert the key is gone
      Thread.sleep(EXPIRATION_INTERVAL_MILLIS * 2L);
      assertFalse(bucket.containsKey(key));
   }


   protected void setUp() throws Exception {

      super.setUp();

      final BinaryStore keyStore = new BinaryStore(getClock(), EXPIRATION_INTERVAL_MILLIS, Integer.MAX_VALUE);

      keyStore.setObjectSizeCalculator(new DummyObjectSizeCalculator());
      keyStore.setDiskStorage(new DummyDiskStorage("test"));
      keyStore.setInvalidator(new DummyCacheInvalidator());
      keyStore.setDataStore(new DummyDataStore());
      keyStore.setDataSource(new DummyBinaryStoreDataSource());
      keyStore.attachToByteCounter(new SharedCounter(0L));
      keyStore.attachToElementCounter(new SharedCounter(0L));

      key = TestUtils.toBinary("key");
      value = TestUtils.toBinary("value");
      bucket = new Bucket(BUCKET_NUMBER, keyStore, LEASE_DURATION_MILLIS);
   }


   public String toString() {

      return "BucketTest{" +
              "bucket=" + bucket +
              ", key=" + key +
              ", value=" + value +
              "} " + super.toString();
   }
}
