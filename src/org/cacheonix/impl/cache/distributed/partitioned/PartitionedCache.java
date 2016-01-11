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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cacheonix.CacheonixException;
import org.cacheonix.NotSubscribedException;
import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.cache.executor.Aggregator;
import org.cacheonix.cache.executor.Executable;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.cache.CacheonixCache;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.BinaryFactory;
import org.cacheonix.impl.cache.item.BinaryFactoryBuilder;
import org.cacheonix.impl.cache.item.BinaryType;
import org.cacheonix.impl.cache.item.BinaryUtils;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.lock.DistributedReadWriteLock;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.ArgumentValidator;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.cache.EntryImpl;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.locks.ReadWriteLock;

/**
 * PartitionedCache implements CacheonixCache by converting method calls to requests and sending them to an associated
 * CacheProcessor.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection ProhibitedExceptionThrown, RedundantIfStatement
 * @since Jul 11, 2009 10:11:52 PM
 */
public final class PartitionedCache<K extends Serializable, V extends Serializable> implements CacheonixCache<K, V> {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PartitionedCache.class); // NOPMD

   /**
    * A binary factory builder.
    */
   private static final BinaryFactoryBuilder BINARY_FACTORY_BUILDER = new BinaryFactoryBuilder();

   // REVIEWME: simeshev@cacheonix.org - 2009-07-16 - Consider depending on the configuration. Right now configuration defines only

   private final BinaryFactory binaryFactory = BINARY_FACTORY_BUILDER.createFactory(BinaryType.BY_COPY);

   /**
    * Retries an operation if it throws a RetryException.
    */
   private final Retrier retrier = new Retrier();

   /**
    * Default unlock timeout millis.
    */
   private final long defaultUnlockTimeoutMillis;

   /**
    * Cache name.
    */
   private final String cacheName;

   /**
    * Name of this Cache's lock region name.
    */
   private final String lockRegionName;

   /**
    * An address of this stub.
    */
   private final ClusterNodeAddress address;


   /**
    * Cluster processor.
    */
   private final ClusterProcessor clusterProcessor;

   /**
    * Bucket index calculator.
    */
   private final BucketIndexCalculator bucketCalculator = new BucketIndexCalculator(
           ConfigurationConstants.BUCKET_COUNT);

   /**
    * Cluster clock.
    */
   private final Clock clock;


   public PartitionedCache(final ClusterProcessor clusterProcessor, final Clock clock, final ClusterNodeAddress address,
           final String cacheName, final long defaultUnlockTimeoutMillis) {

      this.clusterProcessor = clusterProcessor;
      this.defaultUnlockTimeoutMillis = defaultUnlockTimeoutMillis;
      this.lockRegionName = "cache-" + cacheName;
      this.cacheName = cacheName;
      this.address = address;
      this.clock = clock;
   }


   public void shutdown() {

   }


   /**
    * @return list of key owners. Can be empty.
    */
   public List getKeyOwners() {

      return (List) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final GetKeyOwnersRequest request = new GetKeyOwnersRequest(cacheName);
            request.setReceiver(address);

            final List<ClusterNodeAddress> contributorAddresses = clusterProcessor.execute(request);
            final List<CacheMemberImpl> result = new ArrayList<CacheMemberImpl>(contributorAddresses.size());
            for (final ClusterNodeAddress contributorAddress : contributorAddresses) {
               result.add(new CacheMemberImpl(contributorAddress, cacheName));
            }
            return result;
         }


         public String description() {

            return "getKeyOwners";
         }
      });
   }


   public Serializable execute(final Executable executable, final Aggregator aggregator) {

      return execute(null, executable, aggregator);
   }


   public Serializable execute(final EntryFilter entryFilter, final Executable executable,
           final Aggregator aggregator) {

      // Create copies to get a bullet-proof guarantee that they can pass through the wire
      final Executable executableCopy = copy(executable);
      final EntryFilter entryFilterCopy = copy(entryFilter);

      // Execute
      return (Serializable) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ExecuteRequest request = new ExecuteRequest(cacheName);
            request.setExecutable(executableCopy);
            request.setEntryFilter(entryFilterCopy);

            final Collection<Serializable> partialResults = clusterProcessor.execute(request);
            return aggregator.aggregate(partialResults);
         }


         public String description() {

            return "execute";
         }
      });
   }


   public Serializable executeAll(final Set<K> keys, final Executable executable, final Aggregator aggregator) {

      // Optimize for single size key set
      if (keys.isEmpty()) {

         return aggregator.aggregate(new ArrayList<Serializable>(0));
      }

      // Run
      @SuppressWarnings("unchecked")
      final Collection<Serializable> partialResults = (Collection<Serializable>) retrier.retryUntilDone(
              new Retryable() {

                 public Object execute() throws RetryException {

                    final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
                    final ExecuteAllRequest request = new ExecuteAllRequest(cacheName);
                    request.setKeySet(toBinaryKeySet(keys));
                    request.setExecutable(executable);
                    return clusterProcessor.execute(request);
                 }


                 public String description() {

                    return "removeAll";
                 }
              });
      return aggregator.aggregate(partialResults);
   }


   public int size() {

      return ((Number) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final SizeRequest request = new SizeRequest(cacheName);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "size";
         }
      })).intValue();
   }


   public void clear() {

      retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {
            // NOTE: simeshev@cacheonix.org - 2010-03-16 - ClearRequest
            // is a chained request, so we set receiver to self.

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final ClearRequest request = new ClearRequest(cacheName);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "clear";
         }
      });
   }


   public boolean isEmpty() {

      return size() == 0;
   }


   public boolean containsKey(final Object key) {

      return (Boolean) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final Binary binary = createBinary(key);
            final ContainsKeyRequest request = new ContainsKeyRequest(cacheName, binary);

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "containsKey";
         }
      });
   }


   public boolean containsValue(final Object value) {

      return result((Boolean) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final Binary binaryValue = createBinary(value);

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final ContainsValueRequest request = new ContainsValueRequest(cacheName, binaryValue);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "containsValue";
         }
      }));
   }


   public Collection<V> values() {

      //noinspection unchecked
      return (Collection<V>) retrier.retryUntilDone(new Retryable() {

         @SuppressWarnings("unchecked")
         public Object execute() throws RetryException {

            // Send request

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final ValuesRequest request = new ValuesRequest(cacheName);
            final LinkedList<Binary> binaryValues = clusterProcessor.execute(request);

            // Convert to object collection
            final Collection<Object> result = new ArrayList<Object>(binaryValues.size());
            while (!binaryValues.isEmpty()) {
               result.add(binaryValues.removeFirst().getValue());
            }
            return result;
         }


         public String description() {

            return "values";
         }
      });
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This method splits the incoming map to a set of maps with keys belonging to an owner.
    */
   public void putAll(final Map<? extends K, ? extends V> map) {

      // Don't do anything if the map is empty
      if (map.isEmpty()) {
         return;
      }

      // Invoke simple put for a single-entry map
      if (map.size() == 1) {

         final Entry<? extends K, ? extends V> entry = map.entrySet().iterator().next();
         put(entry.getKey(), entry.getValue());
         return;
      }

      // Create binary map
      final HashMap<Binary, Binary> binaryMap = new HashMap<Binary, Binary>(map.size());
      for (final Object o : map.entrySet()) {
         final Entry entry = (Entry) o;
         final Binary binaryKey = createBinary(entry.getKey());
         final Binary binaryValue = createBinary(entry.getValue());
         binaryMap.put(binaryKey, binaryValue);
      }

      // Put
      retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final PutAllRequest request = new PutAllRequest(cacheName);
            request.setEntrySet(binaryMap);

            return clusterProcessor.execute(request);
         }


         public String description() {

            return "putAll";
         }
      });
   }


   public Set<Entry<K, V>> entrySet() {

      //noinspection unchecked
      return (Set<Entry<K, V>>) retrier.retryUntilDone(new Retryable() {

         @SuppressWarnings("unchecked")
         public Object execute() throws RetryException {

            // Send request

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final GetEntrySetRequest request = new GetEntrySetRequest(cacheName);
            final LinkedList<Entry<Binary, Binary>> binaryEntries = clusterProcessor.execute(
                    request);

            // Convert to object collection
            final Set<Object> result = new HashSet<Object>(binaryEntries.size());
            while (!binaryEntries.isEmpty()) {
               final Entry<Binary, Binary> binaryEntry = binaryEntries.removeFirst();
               final Binary key = binaryEntry.getKey();
               final Binary value = binaryEntry.getValue();
               result.add(new EntryImpl(key.getValue(), value.getValue()));
            }
            return result;
         }


         public String description() {

            return "entrySet";
         }
      });
   }


   public Set<K> keySet() {

      //noinspection unchecked
      return (Set<K>) retrier.retryUntilDone(new Retryable() {

         @SuppressWarnings("unchecked")
         public Object execute() throws RetryException {

            // Send request

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final GetKeySetRequest request = new GetKeySetRequest(cacheName);
            final LinkedList<Binary> binaryKeys = clusterProcessor.execute(request);

            // Convert to object collection
            final Set<Object> result = new HashSet<Object>(binaryKeys.size());
            while (!binaryKeys.isEmpty()) {
               result.add(binaryKeys.removeFirst().getValue());
            }
            return result;
         }


         public String description() {

            return "keySet";
         }
      });
   }


   public V get(final Object key) {

      //noinspection unchecked
      return (V) retrier.retryUntilDone(new Retryable() {


         public Object execute() throws RetryException {

            final Binary binaryKey = createBinary(key);
            final GetRequest request = new GetRequest(cacheName, binaryKey);

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final CacheableValue cacheableValue = clusterProcessor.execute(request);
            return result(cacheableValue);
         }


         public String description() {

            return "get";
         }
      });
   }


   public V remove(final Object key) {

      //noinspection unchecked
      return (V) result(doRemove(key));
   }


   /**
    * {@inheritDoc}
    */
   public boolean remove(final Object key, final Object value) {

      return (Boolean) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final Binary binaryKey = createBinary(key);
            final Binary binaryValue = createBinary(value);
            final AtomicRemoveRequest request = new AtomicRemoveRequest(address, cacheName, binaryKey, binaryValue);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "remove";
         }
      });
   }


   /**
    * {@inheritDoc}
    */
   public final boolean replace(final K key, final V oldValue, final V newValue) {

      return (Boolean) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final Binary binaryKey = createBinary(key);
            final Binary binaryOldValue = createBinary(oldValue);
            final Binary binaryNewValue = createBinary(newValue);
            final AtomicReplaceRequest request = new AtomicReplaceRequest(address, cacheName,
                    binaryKey, binaryOldValue, binaryNewValue);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "replace";
         }
      });
   }


   public V replace(final K key, final V value) {

      //noinspection unchecked
      return (V) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final Binary binaryKey = createBinary(key);
            final Binary binaryValue = createBinary(value);
            final ReplaceIfMappedRequest request = new ReplaceIfMappedRequest(address, cacheName, binaryKey,
                    binaryValue);
            return result((Binary) clusterProcessor.execute(request));
         }


         public String description() {

            return "replace";
         }
      });
   }


   /**
    * Removes a keys and returns a previous <code>Binary</code> value or null if there was not a key in the cache.
    *
    * @param key the key to remove.
    * @return the previous <code>Binary</code> value or null if there was not the key in the cache.
    */
   private Binary doRemove(final Object key) {

      return (Binary) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final Binary binaryKey = createBinary(key);
            final RemoveRequest request = new RemoveRequest(address, cacheName, binaryKey);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "remove";
         }
      });
   }


   /**
    * Removes all cache entries that match the <code>keySet</code>.
    *
    * @param keys the set of keys to remove.
    * @return <code>true</code> if any of the keys was removed.
    */
   public boolean removeAll(final Set<K> keys) {

      // Check if there is work to do
      if (keys.isEmpty()) {

         // Nothing to process, returns 'false' - no keys were removed
         return false;
      }

      // Optimize for single size key set
      if (keys.size() == 1) {
         final Binary binary = doRemove(keys.iterator().next());
         return binary != null;
      }

      // Run
      return (Boolean) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final RemoveAllRequest request = new RemoveAllRequest(cacheName);
            request.setKeySet(toBinaryKeySet(keys));
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "removeAll";
         }
      });
   }


   /**
    * Returns all cache entries that match the <code>keySet</code>.
    *
    * @param keys the set of keys to return.
    * @return a collection with values.
    */
   public Map<K, V> getAll(final Set<K> keys) {


      if (keys.isEmpty()) {
         return Collections.emptyMap();
      }

      // Convert to binary set

      final IntObjectHashMap<HashSet<Binary>> binaryKeySet = toBinaryKeySet(keys);

      // Run
      //noinspection unchecked
      return (Map<K, V>) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final GetAllRequest request = new GetAllRequest(cacheName);
            request.setKeySet(binaryKeySet);
            final Collection<CacheableEntry> collection = clusterProcessor.execute(request);
            final Map<Object, Object> result = new HashMap<Object, Object>(collection.size());
            for (final CacheableEntry cacheableEntry : collection) {

               result.put(result(cacheableEntry.getKey()), result(cacheableEntry.getValue()));
            }
            return result;
         }


         public String description() {

            return "getAll";
         }
      });
   }


   private IntObjectHashMap<HashSet<Binary>> toBinaryKeySet(final Set<K> keys) {

      final IntObjectHashMap<HashSet<Binary>> binaryKeySet = new IntObjectHashMap<HashSet<Binary>>(1);
      for (final K key : keys) {

         final int bucketNumber = bucketCalculator.calculateBucketIndex(key);
         HashSet<Binary> binaries = binaryKeySet.get(bucketNumber);
         if (binaries == null) {

            binaries = new HashSet<Binary>(1);
            binaryKeySet.put(bucketNumber, binaries);
         }
         binaries.add(createBinary(key));
      }
      return binaryKeySet;
   }


   public boolean retainAll(final Set<K> keySet) {

      // Convert to binary set
      // NOPMD

      final HashSet<Binary> binaryKeySet = BinaryUtils.toBinarySet(keySet);

      // Run
      return (Boolean) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final RetainAllRequest request = new RetainAllRequest(cacheName);
            request.setKeySet(binaryKeySet);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "retainAll";
         }
      });

   }


   public V put(final K key, final V value) {

      return put(key, value, -1L);
   }


   public V put(final K key, final V value, final long expirationTimeMillis) {

      //noinspection unchecked
      return (V) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final Binary binaryKey = createBinary(key);
            final Binary binaryValue = createBinary(value);
            final Time expirationTime = expirationTimeMillis > 0 ? clock.currentTime().add(expirationTimeMillis) : null;
            final PutRequest request = new PutRequest(address, cacheName, binaryKey, binaryValue, expirationTime,
                    false);

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final CacheableValue cacheableValue = clusterProcessor.execute(request);
            return result(cacheableValue);
         }


         public String description() {

            return "put";
         }
      });
   }


   /**
    * {@inheritDoc}
    */
   public V putIfAbsent(final K key, final V value) {

      //noinspection unchecked
      return (V) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final Binary binaryKey = createBinary(key);
            final Binary binaryValue = createBinary(value);
            final PutRequest request = new PutRequest(address, cacheName, binaryKey, binaryValue, null, true);

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final CacheableValue cacheableValue = clusterProcessor.execute(request);
            return result(cacheableValue);
         }


         public String description() {

            return "putIfAbsent";
         }
      });
   }


   private static Object result(final Binary result) {

      if (result == null) {
         return null;
      }

      //noinspection unchecked
      return result.getValue();
   }


   private static boolean result(final Boolean result) {

      return result != null && result;
   }


   /**
    * Converts a cacheable value to an object.
    *
    * @param cacheableValue the cachable value.
    * @return an object or null if the cachable value is null or the binary content of the cachable value is null.
    */
   private static Object result(final CacheableValue cacheableValue) {

      return cacheableValue == null ? null : result(cacheableValue.getBinaryValue());
   }


   public CacheStatistics getStatistics() {

      return (CacheStatistics) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final GetStatisticsRequest request = new GetStatisticsRequest(cacheName);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "getStatistics";
         }
      });

   }


   public String getName() {

      return cacheName;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * When this method is called, the cache processor can be pending join or blocked. This means we should use the same
    * request approach as with data methods.
    */
   public long getMaxSize() {

      return (Long) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final Message request = new GetMaxSizeRequest(cacheName);
            request.setSender(address);
            request.setReceiver(address);

            return clusterProcessor.execute(request);
         }


         public String description() {

            return "getMaxSize";
         }
      });
   }


   public long getMaxSizeBytes() {

      return 0L;  //To change body of implemented methods use File | Settings | File Templates.
   }


   public long getSizeOnDisk() {

      return 0L;  //To change body of implemented methods use File | Settings | File Templates.
   }


   public long longSize() {

      return 0L;  //To change body of implemented methods use File | Settings | File Templates.
   }


   /**
    * {@inheritDoc}
    */
   public CacheMember getKeyOwner(final K key) {

      return (CacheMember) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ClusterProcessor clusterProcessor = PartitionedCache.this.clusterProcessor;
            final GetKeyOwnerRequest request = new GetKeyOwnerRequest(cacheName, 0,
                    bucketCalculator.calculateBucketIndex(key));
            request.setReceiver(address);
            final ClusterNodeAddress owner = clusterProcessor.execute(request);
            return new CacheMemberImpl(owner, cacheName);
         }


         public String description() {

            return "retainAll";
         }
      });

   }


   /**
    * {@inheritDoc}
    */
   public void addEventSubscriber(final Set<K> keys, final EntryModifiedSubscriber subscriber) {

      // Create binary key set

      addEventSubscriber(BinaryUtils.toBinarySet(keys), subscriber);
   }


   /**
    * {@inheritDoc}
    *
    * @param key        a key of interest.
    * @param subscriber the subscriber to an event when a cache element is added, updated or removed.
    */
   public void addEventSubscriber(final K key, final EntryModifiedSubscriber subscriber) {

      addEventSubscriber(BinaryUtils.toBinarySet(key), subscriber);
   }


   private void addEventSubscriber(final HashSet<Binary> binaryKeys, // NOPMD
           final EntryModifiedSubscriber subscriber) {

      final ClusterProcessor clusterProcessor = this.clusterProcessor;

      retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            // Create request
            final AddEntryModifiedSubscriberRequest request = new AddEntryModifiedSubscriberRequest(cacheName);
            request.setSubscriberAddress(address);
            request.setLocalSubscriber(subscriber);
            request.setReceiver(address);
            request.setKeys(binaryKeys);

            // Wait until done
            clusterProcessor.execute(request);

            // Done
            return null;
         }


         public String description() {

            return "addEventSubscriber";
         }
      });
   }


   /**
    * {@inheritDoc}
    */
   public void removeEventSubscriber(final Set<K> keys, final EntryModifiedSubscriber subscriber) {

      removeEventSubscriber(BinaryUtils.toBinarySet(keys), subscriber);
   }


   /**
    * {@inheritDoc}
    */
   public void removeEventSubscriber(final K key,
           final EntryModifiedSubscriber subscriber) throws NotSubscribedException {

      removeEventSubscriber(BinaryUtils.toBinarySet(key), subscriber);
   }


   private void removeEventSubscriber(final HashSet<Binary> binaryKeys, // NOPMD
           final EntryModifiedSubscriber subscriber) {

      final ClusterProcessor clusterProcessor = this.clusterProcessor;

      retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            // Create request
            final RemoveEntryModifiedSubscriberRequest request = new RemoveEntryModifiedSubscriberRequest(cacheName);
            request.setSubscriberAddress(address);
            request.setSubscriber(subscriber);
            request.setReceiver(address);
            request.setKeys(binaryKeys);

            // Wait until done
            clusterProcessor.execute(request);

            // Done
            return null;
         }


         public String description() {

            return "removeEventSubscriber";
         }
      });
   }


   /**
    * {@inheritDoc}
    */
   public long getSizeBytes() {

      return 0L;  //To change body of implemented methods use File | Settings | File Templates.
   }


   public ReadWriteLock getReadWriteLock() {

      return getReadWriteLock("default");
   }


   public ReadWriteLock getReadWriteLock(final Serializable lockKey) {

      return new DistributedReadWriteLock(clusterProcessor, lockRegionName, BinaryUtils.toBinary(lockKey),
              defaultUnlockTimeoutMillis);
   }


   private Binary createBinary(final Object obj) {

      try {
         return binaryFactory.createBinary(obj);
      } catch (final InvalidObjectException e) {
         throw new CacheonixException(e);
      }
   }


   private static Executable copy(final Executable executable) {

      final Executable executableCopy;
      try {
         final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
         final byte[] bytes = serializer.serialize(executable);
         executableCopy = (Executable) serializer.deserialize(bytes);
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new RuntimeInvalidObjectException(e);
      }
      return executableCopy;
   }


   private static EntryFilter copy(final EntryFilter filter) {

      final EntryFilter filterCopy;
      try {
         final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
         final byte[] bytes = serializer.serialize(filter);
         filterCopy = (EntryFilter) serializer.deserialize(bytes);
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new RuntimeInvalidObjectException(e);
      }
      return filterCopy;
   }


   public String toString() {

      return "PartitionedCache{" +
              "binaryFactory=" + binaryFactory +
              ", cacheName='" + cacheName + '\'' +
              ", address=" + address +
              '}';
   }


   private static final class CacheMemberImpl implements CacheMember {

      private final ClusterNodeAddress owner;

      private final String cacheName;


      CacheMemberImpl(final ClusterNodeAddress owner, final String cacheName) {

         this.owner = (ClusterNodeAddress) ArgumentValidator.validateArgumentNotNull(owner, "Owner");
         this.cacheName = ArgumentValidator.validateArgumentNotBlank(cacheName, "cacheName");
      }


      public final List getInetAddresses() {

         return Collections.unmodifiableList(Arrays.asList(owner.getAddresses()));
      }


      public final String getCacheName() {

         return cacheName;
      }


      public final boolean equals(final Object obj) {

         if (this == obj) {
            return true;
         }
         if (!(obj instanceof CacheMemberImpl)) {
            return false;
         }

         final CacheMemberImpl clusterService = (CacheMemberImpl) obj;

         if (!cacheName.equals(clusterService.cacheName)) {
            return false;
         }
         if (!owner.equals(clusterService.owner)) {
            return false;
         }

         return true;
      }


      public final int hashCode() {

         int result = owner.hashCode();
         result = 31 * result + cacheName.hashCode();
         return result;
      }


      public final String toString() {

         return "CacheMemberImpl{" +
                 "cacheName='" + cacheName + '\'' +
                 ", owner=" + owner +
                 '}';
      }
   }
}
