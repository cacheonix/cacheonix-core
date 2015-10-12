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

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.Executor;

import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSource;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSourceFactory;
import org.cacheonix.impl.cache.datasource.PrefetchStage;
import org.cacheonix.impl.cache.datastore.DataStoreFactory;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.distributed.partitioned.subscriber.EntryModifiedSubscription;
import org.cacheonix.impl.cache.distributed.partitioned.subscriber.LocalSubscription;
import org.cacheonix.impl.cache.distributed.partitioned.subscriber.RemoteEntryModifiedSubscriber;
import org.cacheonix.impl.cache.invalidator.CacheInvalidatorFactory;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.cache.storage.disk.StorageFactory;
import org.cacheonix.impl.cache.store.BinaryStore;
import org.cacheonix.impl.cache.store.SharedCounter;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.configuration.ConfigurationConstants;
import org.cacheonix.impl.configuration.DataSourceConfiguration;
import org.cacheonix.impl.configuration.DataStoreConfiguration;
import org.cacheonix.impl.configuration.FrontCacheConfiguration;
import org.cacheonix.impl.configuration.InvalidatorConfiguration;
import org.cacheonix.impl.configuration.OverflowToDiskConfiguration;
import org.cacheonix.impl.configuration.PartitionedCacheConfiguration;
import org.cacheonix.impl.configuration.PropertyConfiguration;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.AbstractRequestProcessor;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.util.ArgumentValidator;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.ObjectObjectProcedure;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.cache.DummyObjectSizeCalculator;
import org.cacheonix.impl.util.cache.ObjectSizeCalculator;
import org.cacheonix.impl.util.cache.ObjectSizeCalculatorFactory;
import org.cacheonix.impl.util.cache.StandardObjectSizeCalculator;
import org.cacheonix.impl.util.logging.Logger;

/**
 * CacheProcessor is a specialized request processor that serves cache requests.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 28, 2008 8:09:17 PM
 */
public final class CacheProcessorImpl extends AbstractRequestProcessor implements CacheProcessor {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheProcessor.class); // NOPMD

   /**
    * An object size calculator.
    */
   private final StandardObjectSizeCalculator standardObjectSizeCalculator = new StandardObjectSizeCalculator();

   /**
    * An object size calculator that always returns zero size.
    */
   private final DummyObjectSizeCalculator dummyObjectSizeCalculator = new DummyObjectSizeCalculator();

   /**
    * Bucket index calculator.
    */
   private final BucketIndexCalculator bucketCalculator = new BucketIndexCalculator(
           ConfigurationConstants.BUCKET_COUNT);

   private final ObjectSizeCalculatorFactory objectSizeCalculatorFactory = new ObjectSizeCalculatorFactory();

   private final BinaryStoreDataSourceFactory binaryStoreDataSourceFactory = new BinaryStoreDataSourceFactory();

   private final CacheInvalidatorFactory invalidatorFactory = new CacheInvalidatorFactory();

   private final DataStoreFactory dataStoreFactory = new DataStoreFactory();

   /**
    * The executor used to execute event notifications outside of the processor's loop.
    */
   private final Executor eventNotificationExecutor;


   /**
    * Cache name.
    */
   private final String cacheName;

   /**
    * Cache config.
    */
   private final PartitionedCacheConfiguration cacheConfig;


   /**
    * A map of buckets to storages. The size of the array is the number of replicas + 1.
    */
   private final IntObjectHashMap<Bucket>[] bucketStorages;


   /**
    * A map of bucket disk storages to storages. The size of the array is the number of replicas + 1.
    */
   private final DiskStorage[] diskStorages;


   /**
    * Replicated group this cache member belongs to.
    */
   private final Group group;

   /**
    * A max number of elements in memory.
    */
   private final SharedCounter elementCounter;


   /**
    * Entry modification subscriptions submitted by local callers.
    * <p/>
    * This map is used for fast subscriber lookup and to track over -subscription/-un-subscription.
    *
    * @see PartitionedCache#addEventSubscriber(HashSet, EntryModifiedSubscriber)
    */
   private Map<Integer, LocalSubscription> localEntryModifiedSubscriptions;


   /**
    * A cluster-wide partition size setting.
    */
   private final SharedCounter byteCounter;

   private int state = STATE_OPERATIONAL;

   private final FrontCache frontCache;

   private final BinaryStoreDataSource dataSource;


   /**
    * Constructs a distributed cache.
    *
    * @param timer                     a timer to use for timeout scheduling etc.
    * @param clock                     synchronized cluster clock.
    * @param prefetchStage             a scheduler responsible for scheduling prefetch orders.
    * @param router                    a router.
    * @param eventNotificationExecutor an executor for event notifications.
    * @param group                     a group.
    * @param cacheName                 a cache name.
    * @param address                   local address.
    * @param cacheConfig               cache configuration.     @throws StorageException if a storage error occurred.
    * @throws StorageException if a storage error occured.
    */
   public CacheProcessorImpl(final Timer timer, final Clock clock, final PrefetchStage prefetchStage,
           final Router router, final Executor eventNotificationExecutor, final Group group,
           final String cacheName, final ClusterNodeAddress address,
           final PartitionedCacheConfiguration cacheConfig) throws StorageException {

      super(clock, timer, "CacheProcessor:" + address.getTcpPort(), address, router);
      this.eventNotificationExecutor = eventNotificationExecutor;
      this.group = group;
      this.frontCache = createFrontCache(cacheConfig.getFrontCacheConfiguration());
      this.cacheName = ArgumentValidator.validateArgumentNotBlank(cacheName, "cacheName");
      this.cacheConfig = cacheConfig;
      this.dataSource = createDataSource(cacheName, cacheConfig, prefetchStage, getRouter(), getClock());
      this.diskStorages = createDiskStorages(cacheName, Integer.toString(System.identityHashCode(this)),
              group.getReplicaCount(), cacheConfig);
      this.bucketStorages = createLocalBucketsStorage(group.getReplicaCount());
      this.byteCounter = new SharedCounter(group.getPartitionSizeBytes());
      this.elementCounter = new SharedCounter(group.getMaxElements());
   }


   public void setState(final int state) {

      this.state = state;
   }


   public int getState() {

      return state;
   }


   public String getCacheName() {

      return cacheName;
   }


   public Executor getEventNotificationExecutor() {

      return eventNotificationExecutor;
   }


   public FrontCache getFrontCache() {

      return frontCache;
   }


   public ClusterNodeAddress getBucketOwner(final int storageNumber, final int bucketNumber) {

      return group.getBucketOwner(storageNumber, bucketNumber);
   }


   public int getBucketOwnerCount() {

      return group.getBucketOwnerCount();
   }


   public Bucket createBucket(final int storageNumber, final Integer bucketNumber) {

      Assert.assertTrue(cacheConfig.isPartitionContributor(),
              "Creating bucket store is allowed only for partition contributors: {0} ", cacheConfig);
      Assert.assertNotNull(cacheConfig.getStore().getLru(), "Unknown result eviction policy: {0}",
              cacheConfig.getStore().getLru());

      //
      final boolean primaryStore = storageNumber == 0;
      final long expirationTimeMillis = primaryStore ? cacheConfig.getStore().getExpiration().getTimeToLiveMillis() : 0L;
      final long idleTimeMillis = primaryStore ? cacheConfig.getStore().getExpiration().getIdleTimeMillis() : 0L;
      final long leaseDurationMillis = primaryStore ? cacheConfig.getStore().getCoherence().getLease().getLeaseTimeMillis() : 0L;

      // Create store
      final BinaryStore keyStore = new BinaryStore(getClock(), expirationTimeMillis, idleTimeMillis);

      // Create bucket and return result
      final Bucket bucket = new Bucket(bucketNumber, keyStore, leaseDurationMillis);

      // Register bucket in the bucket access map
      setBucket(storageNumber, bucketNumber, bucket);

      return bucket;
   }


   public int getBucketNumber(final Binary key) {

      return bucketCalculator.calculateBucketIndex(key);
   }


   public int getReplicaCount() {

      return bucketStorages.length - 1;
   }


   public int getBucketCount() {

      return group.getBucketCount();
   }


   public Bucket removeBucket(final int storageNumber, final Integer bucketNumber) {

      final Bucket removedBucket = bucketStorages[storageNumber].remove(bucketNumber);

      // NOTE: simeshev@cacheonix.org - 2011-01-05 - In theory this method is not going
      // to be called against a non-existing bucket, but, just in case let's check.
      if (removedBucket != null) {

         removedBucket.detachElementCounter();
         removedBucket.detachByteCounter();
         removedBucket.detachDiskStorage();
      }

      return removedBucket;
   }


   public Bucket setBucket(final int storageNumber, final Integer bucketNumber, final Bucket bucket) {

      final boolean primaryStorage = storageNumber == 0;

      // Set clock
      bucket.setClock(getClock());

      // Set shared counters. Replica counters are *not* shared.
      bucket.attachToElementCounter(primaryStorage ? elementCounter : new SharedCounter(0L));
      bucket.attachToByteCounter(primaryStorage ? byteCounter : new SharedCounter(0L));

      // Set other dependencies
      bucket.setObjectSizeCalculator(getObjectSizeCalculator(storageNumber));
      bucket.setInvalidator(createInvalidator(storageNumber));
      bucket.setDataStore(createDataStore(storageNumber));
      bucket.setDiskStorage(diskStorages[storageNumber]);
      bucket.setDataSource(dataSource);


      // Set entry modification subscribers
      if (primaryStorage) {

         // REVIEWME: simeshev@cacheonix.org - 2011-02-09 -> Possibility of ConcurrentModificationException. Switch
         // to creating a replicated copy of the modification subscriptions at each CacheProcessor. The try/catch
         // block is only a temp solution.

         try {

            final HashMap<Binary, HashSet<EntryModifiedSubscription>> bucketSubscriptions = group.getEntryModifiedSubscriptions().get(
                    bucketNumber); // NOPMD
            if (!CollectionUtils.isEmpty(bucketSubscriptions)) {

               bucketSubscriptions.forEachEntry(
                       new ObjectObjectProcedure<Binary, HashSet<EntryModifiedSubscription>>() {

                          public boolean execute(final Binary key,
                                  final HashSet<EntryModifiedSubscription> keySubscriptions) { // NOPMD

                             keySubscriptions.forEach(new ObjectProcedure<EntryModifiedSubscription>() {

                                public boolean execute(final EntryModifiedSubscription subscription) {

                                   // Create a remote subscriber
                                   final RemoteEntryModifiedSubscriber subscriber = new RemoteEntryModifiedSubscriber();
                                   subscriber.setSubscription(subscription);
                                   subscriber.setCacheName(getCacheName());
                                   subscriber.setProcessor(CacheProcessorImpl.this);

                                   // Add subscriber to the bucket
                                   bucket.addEventSubscriber(key, subscriber);

                                   return true;
                                }
                             });

                             return true;
                          }
                       });
            }
         } catch (final Exception e) {

            LOG.error("Failed to add subscribers to bucket " + bucket + " at storage " + storageNumber + ": " + e, e);
         }
      }

      return bucketStorages[storageNumber].put(bucketNumber, bucket);
   }


   public void restorePrimaryBucket(final Integer bucketNumber, final int replicaStorageNumber) {

      final Bucket replicaBucket = bucketStorages[replicaStorageNumber].remove(bucketNumber);

      // Replica bucket does not exist, create a new empty bucket
      final Bucket primaryBucket = createBucket(0, bucketNumber);

      if (replicaBucket != null) {

         // There is replica bucket. Transfer the content of the replica bucket to the primary bucket.

         replicaBucket.transferTo(primaryBucket);
      }
   }


   /**
    * @param bucketNumber bucket number.
    * @return the primary bucket owner.
    * @noinspection UnusedDeclaration
    */
   private ClusterNodeAddress getPrimaryOwner(final Integer bucketNumber) {

      return getBucketOwner(0, bucketNumber);
   }


   public Bucket getBucket(final int storageNumber, final int integerBucketNumber) {

      return bucketStorages[storageNumber].get(integerBucketNumber);
   }


   public Map<Integer, LocalSubscription> getLocalEntryModifiedSubscriptions() {

      if (localEntryModifiedSubscriptions == null) {

         localEntryModifiedSubscriptions = new HashMap<Integer, LocalSubscription>(1);
      }

      return localEntryModifiedSubscriptions;
   }


   public long getMaxSize() {

      return elementCounter.getMaxValue();
   }


   public boolean hasBucket(final int storageNumber, final int bucketNumber) {

      return bucketStorages[storageNumber].containsKey(bucketNumber);
   }


   public boolean isBucketOwner(final int storageNumber, final int bucketNumber) {

      return getAddress().equals(getBucketOwner(storageNumber, bucketNumber));
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation extends the default behaviour by destroying disk storages created at construction.
    */
   public void shutdown() {

      try {

         super.shutdown();
      } finally {

         destroyDiskStorages();
         destroyBucketStorages();
         destroyFrontCache();
         destroyEntryModifiedSubscriptions();
      }
   }


   /**
    * Shutdowns all disk storages allocated in the <code>CacheProcessor</code> constructor.
    *
    * @see #shutdown()
    */
   private void destroyDiskStorages() {

      for (final DiskStorage diskStorage : diskStorages) {

         diskStorage.shutdown(true);
      }
   }


   /**
    * Clears bucket storages at shutdown.
    *
    * @see #shutdown()
    */
   private void destroyBucketStorages() {

      for (int i = 0; i < bucketStorages.length; i++) {
         final IntObjectHashMap<Bucket> bucketStorage = bucketStorages[i];

         if (bucketStorage != null) {

            bucketStorage.clear();
         }
         bucketStorages[i] = null;
      }
   }


   /**
    * Clears the front cache at shutdown.
    */
   private void destroyFrontCache() {

      if (frontCache != null) {

         frontCache.clear();
      }
   }


   /**
    * Clears entry modified subscriptions at shutdown.
    */
   private void destroyEntryModifiedSubscriptions() {

      if (localEntryModifiedSubscriptions != null && !localEntryModifiedSubscriptions.isEmpty()) {

         localEntryModifiedSubscriptions.clear();
      }
   }


   @SuppressWarnings("unchecked")
   private static IntObjectHashMap<Bucket>[] createLocalBucketsStorage(final int replicaCount) {

      final IntObjectHashMap<Bucket>[] result = new IntObjectHashMap[replicaCount + 1];
      for (int i = 0; i < result.length; i++) {
         result[i] = new IntObjectHashMap<Bucket>();
      }
      return result;
   }


   private ObjectSizeCalculator getObjectSizeCalculator(final int storageNumber) {

      final long maxSizeBytes = storageNumber == 0 ? byteCounter.getMaxValue() : 0L;

      if (maxSizeBytes >= 0) {

         return standardObjectSizeCalculator;
      } else {

         return dummyObjectSizeCalculator;
      }
   }


   private static BinaryStoreDataSource createDataSource(final String cacheName,
           final PartitionedCacheConfiguration cacheConfig,
           final PrefetchStage prefetchScheduler, final Router router,
           final Clock clock) {

      final DataSourceConfiguration dataSourceConfiguration = cacheConfig.getStore().getDataSource();
      final String dataSourceClass = dataSourceConfiguration == null ? null : dataSourceConfiguration.getClassName();
      final Properties dataSourceProperties = dataSourceConfiguration == null ? new Properties() : PropertyConfiguration.toProperties(
              dataSourceConfiguration.getParams());
      final BinaryStoreDataSourceFactory binaryStoreDataSourceFactory = new BinaryStoreDataSourceFactory();
      final boolean prefetchEnabled = dataSourceConfiguration != null && dataSourceConfiguration.isPrefetchConfigurationSet() && dataSourceConfiguration.getPrefetchConfiguration().isEnabled();
      final DistributedPrefetchElementUpdater prefetchElementUpdater = new DistributedPrefetchElementUpdater(router,
              cacheName);
      return binaryStoreDataSourceFactory.createDataSource(clock, cacheName, dataSourceClass, dataSourceProperties,
              prefetchEnabled, prefetchScheduler, prefetchElementUpdater);
   }


   /**
    * Creates a map of disk storages to storages.
    *
    * @param cacheName              a cache name.
    * @param cacheProcessorIdentity an identity that distinguishes this cache processor from others in this JVM. Cache
    *                               name is not enough becuase there may be many cluster nodes running the same caches
    *                               in the JVM.
    * @param replicaCount           a number of replicas
    * @param cacheConfig            a cache config.   @return a map of bucket disk storages to storages. The size of the
    *                               array is the number of replicas + 1.
    * @return an array of disk storages, one per cache storage.
    * @throws StorageException if an error occurs while creating a storage.
    */
   private static DiskStorage[] createDiskStorages(final String cacheName, final String cacheProcessorIdentity,
           final int replicaCount,
           final PartitionedCacheConfiguration cacheConfig)
           throws StorageException {

      final DiskStorage[] result = new DiskStorage[replicaCount + 1];
      for (int storageIndex = 0; storageIndex < replicaCount + 1; storageIndex++) {

         final OverflowToDiskConfiguration overflowToDiskConfiguration = cacheConfig.getStore().getOverflowToDiskConfiguration();
         final long adjustedOverflowSizeMBytes = overflowToDiskConfiguration == null ? 0L : overflowToDiskConfiguration.getMaxOverflowBytes();
         final String tempDir = cacheConfig.getServerConfiguration().getCacheonixConfiguration().getTempDir().getPath();
         final String diskStorageName = ConfigurationConstants.STORAGE_FILE_PREFIX + cacheName + '-' + cacheProcessorIdentity + '-' + storageIndex;
         final String storageFile = tempDir + File.separatorChar + diskStorageName + ConfigurationConstants.STORAGE_FILE_EXTENSION;
         final DiskStorage diskStorage = StorageFactory.createStorage(diskStorageName, adjustedOverflowSizeMBytes,
                 storageFile);
         result[storageIndex] = diskStorage;
      }
      return result;
   }


   private CacheInvalidator createInvalidator(final int storageNumber) {

      // Replicas do not support invalidation
      if (storageNumber > 0) {

         return new DummyCacheInvalidator();
      }

      final InvalidatorConfiguration invalidatorConfiguration = cacheConfig.getStore().getInvalidator();
      final String invalidatorClass = invalidatorConfiguration == null ? null : invalidatorConfiguration.getClassName();
      final Properties invalidatorProperties = invalidatorConfiguration == null ? new Properties() : PropertyConfiguration.toProperties(
              invalidatorConfiguration.getParams());
      final CacheInvalidatorFactory invalidatorFactory = new CacheInvalidatorFactory();
      return invalidatorFactory.createInvalidator(cacheName, invalidatorClass, invalidatorProperties);
   }


   private DataStore createDataStore(final int storageNumber) {

      // Replicas do not support writing
      if (storageNumber > 0) {

         return new DummyDataStore();
      }
      final DataStoreConfiguration dataStoreConfiguration = cacheConfig.getStore().getDataStore();
      final String dataStoreClass = dataStoreConfiguration == null ? null : dataStoreConfiguration.getClassName();
      final Properties dataStoreProperties = dataStoreConfiguration == null ? new Properties() : PropertyConfiguration.toProperties(
              dataStoreConfiguration.getParams());
      final DataStoreFactory dataStoreFactory = new DataStoreFactory();
      return dataStoreFactory.createDataStore(cacheName, dataStoreClass, dataStoreProperties);
   }


   /**
    * Creates a new front cache.
    *
    * @param frontCacheConfiguration a configuration to use to create the front cache.
    * @return the new front cache.
    */
   private FrontCache createFrontCache(final FrontCacheConfiguration frontCacheConfiguration) {

      return frontCacheConfiguration == null ? null : new FrontCache(getClock(), frontCacheConfiguration);
   }


   @SuppressWarnings("unchecked")
   public String toString() {

      return "CacheProcessor{" +
              "cacheName='" + cacheName + '\'' +
              ", bucketStorages=" + (bucketStorages == null ? null : Arrays.asList(bucketStorages)) +
              ", bucketCalculator=" + bucketCalculator +
              ", objectSizeCalculatorFactory=" + objectSizeCalculatorFactory +
              ", binaryStoreDataSourceFactory=" + binaryStoreDataSourceFactory +
              ", invalidatorFactory=" + invalidatorFactory +
              ", dataStoreFactory=" + dataStoreFactory +
              ", cacheConfig=" + cacheConfig +
              ", diskStorages=" + (diskStorages == null ? null : Arrays.asList(diskStorages)) +
              ", group=" + group +
              ", elementCounter=" + elementCounter +
              ", localEntryModifiedSubscriptions=" + localEntryModifiedSubscriptions +
              ", byteCounter=" + byteCounter +
              ", state=" + state +
              "} " + super.toString();
   }
}
