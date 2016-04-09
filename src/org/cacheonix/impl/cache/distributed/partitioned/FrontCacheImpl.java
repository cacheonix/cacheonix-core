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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.cache.store.BinaryStore;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.cache.store.SharedCounter;
import org.cacheonix.impl.cache.util.ObjectSizeCalculator;
import org.cacheonix.impl.cache.util.StandardObjectSizeCalculator;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.config.CacheStoreConfiguration;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.config.FrontCacheConfiguration;

/**
 * Partitioned cache's front cache.
 */
@SuppressWarnings("FieldCanBeLocal")
public final class FrontCacheImpl implements FrontCache {

   /**
    * Bucket index calculator.
    */
   private final BucketIndexCalculator bucketCalculator = new BucketIndexCalculator(ConfigurationConstants.BUCKET_COUNT);

   private final ObjectSizeCalculator objectSizeCalculator = new StandardObjectSizeCalculator();

   private final DummyDataStore dummyCacheDataStore = new DummyDataStore();

   private final DummyBinaryStoreDataSource dummyCacheDataSource = new DummyBinaryStoreDataSource();

   private final DummyCacheInvalidator dummyCacheInvalidator = new DummyCacheInvalidator();

   private final DummyDiskStorage dummyDiskStorage = new DummyDiskStorage("front.cache");

   private final FrontCacheConfiguration frontCacheConfiguration;

   private final SharedCounter byteCounter;

   private final SharedCounter elementCounter;

   private final BinaryStore[] keyStores;

   private final Clock clock;

   private final long timeToLiveMillis;

   private final long idleTimeMillis;

   private final BinaryStoreContext binaryStoreContext;


   public FrontCacheImpl(final Clock clock, final FrontCacheConfiguration frontCacheConfiguration) {

      // Basic fields
      this.clock = clock;
      this.frontCacheConfiguration = frontCacheConfiguration;
      this.keyStores = new BinaryStore[ConfigurationConstants.BUCKET_COUNT];

      // Fields derived from cache configuration
      final CacheStoreConfiguration storeConfiguration = frontCacheConfiguration.getStore();
      this.byteCounter = new SharedCounter(storeConfiguration.getLru().getMaxBytes());
      this.elementCounter = new SharedCounter(storeConfiguration.getLru().getMaxElements());
      this.timeToLiveMillis = storeConfiguration.getExpiration().getTimeToLiveMillis();
      this.idleTimeMillis = storeConfiguration.getExpiration().getIdleTimeMillis();

      // Context
      this.binaryStoreContext = new BinaryStoreContextImpl();
      this.binaryStoreContext.setObjectSizeCalculator(objectSizeCalculator);
      this.binaryStoreContext.setInvalidator(dummyCacheInvalidator);
      this.binaryStoreContext.setDataSource(dummyCacheDataSource);
      this.binaryStoreContext.setDataStore(dummyCacheDataStore);
      this.binaryStoreContext.setDiskStorage(dummyDiskStorage);
   }


   public void put(final Binary key, final Binary value, final Time expirationTime) {

      getOrCreateStore(key).put(key, value, expirationTime);
   }


   private BinaryStore getOrCreateStore(final Binary key) {

      final int bucketNumber = bucketCalculator.calculateBucketIndex(key);
      if (keyStores[bucketNumber] == null) {

         final BinaryStore keyStore = new BinaryStore(clock, timeToLiveMillis, idleTimeMillis);
         keyStore.setContext(binaryStoreContext);
         keyStore.attachToElementCounter(elementCounter);
         keyStore.attachToByteCounter(byteCounter);
         keyStores[bucketNumber] = keyStore;
      }

      return keyStores[bucketNumber];
   }


   public void clear() {

      for (int i = 0; i < keyStores.length; i++) {

         // Get key store
         final BinaryStore keyStore = keyStores[i];

         // Cleat key store
         if (keyStore != null) {

            keyStore.clear();
            keyStores[i] = null;
         }
      }
   }


   public ReadableElement get(final Binary key) throws InvalidObjectException {

      final int bucketNumber = bucketCalculator.calculateBucketIndex(key);
      final BinaryStore binaryStore = keyStores[bucketNumber];
      return binaryStore == null ? null : binaryStore.get(key);
   }


   public FrontCacheConfiguration getFrontCacheConfiguration() {

      return frontCacheConfiguration;
   }


   public void clearBucket(final int bucketNumber) {

      final BinaryStore binaryStore = keyStores[bucketNumber];
      if (binaryStore != null) {

         binaryStore.clear();
      }
   }
}
