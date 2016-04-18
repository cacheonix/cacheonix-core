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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cacheonix.CacheonixException;
import org.cacheonix.NotSubscribedException;
import org.cacheonix.ShutdownException;
import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.cache.executor.Aggregator;
import org.cacheonix.cache.executor.Executable;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.cache.loader.CacheLoader;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.cache.CacheonixCache;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSource;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.distributed.partitioned.BinaryStoreContext;
import org.cacheonix.impl.cache.distributed.partitioned.BinaryStoreContextImpl;
import org.cacheonix.impl.cache.entry.CacheEntryImpl;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.BinaryUtils;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.cache.store.AsynchronousEntryModifiedSubscriberAdapter;
import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriberAdapter;
import org.cacheonix.impl.cache.store.BinaryStore;
import org.cacheonix.impl.cache.store.BinaryStoreElementProcedure;
import org.cacheonix.impl.cache.store.BinaryStoreUtils;
import org.cacheonix.impl.cache.store.LoadableBinaryStoreAdapter;
import org.cacheonix.impl.cache.store.PreviousValue;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.cache.store.SafeEntryUpdateSubscriber;
import org.cacheonix.impl.cache.store.SharedCounter;
import org.cacheonix.impl.cache.util.ObjectSizeCalculator;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.config.ElementEventNotification;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.cache.CacheUtils.createExpirationTime;
import static org.cacheonix.impl.cache.item.BinaryUtils.toBinary;
import static org.cacheonix.impl.config.ElementEventNotification.ASYNCHRONOUS;
import static org.cacheonix.impl.config.ElementEventNotification.SYNCHRONOUS;

/**
 * Implementation of cache with LRU eviction policy.
 *
 * @noinspection JavaDoc, TooBroadScope
 */
public final class LocalCache<K extends Serializable, V extends Serializable> implements CacheonixCache<K, V> {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalCache.class); // NOPMD


   /**
    * Holds read/write lock.
    */
   private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

   /**
    * Convenience reference for read lock.
    */
   private final Lock readLock = readWriteLock.readLock();

   /**
    * Convenience reference for write lock.
    */
   private final Lock writeLock = readWriteLock.writeLock();

   /**
    * The binary store holding elements of this cache.
    */
   private final AtomicReference<BinaryStore> binaryStore = new AtomicReference<BinaryStore>();

   /**
    * The disk storage to keep overflow in.
    */
   private final DiskStorage overflowDiskStorage;

   /**
    * The cache name.
    */
   private final String name;

   /**
    * A tracker of number of elements stored in the cache.
    */
   private final SharedCounter elementCounter;

   /**
    * A tracker of number of bytes stored in the cache.
    */
   private final SharedCounter byteCounter;

   private final Map<Serializable, org.cacheonix.locks.ReadWriteLock> lockRegistry = new HashMap<Serializable, org.cacheonix.locks.ReadWriteLock>(
           1);

   private final Clock clock;

   private final Executor eventNotificationExecutor;

   /**
    * Defines how a subscriber to element event is going to be notified.
    *
    * @see {@link ElementEventNotification#SYNCHRONOUS}
    * @see {@link ElementEventNotification#ASYNCHRONOUS}
    */
   private final ElementEventNotification eventNotification;


   /**
    * @param name
    * @param maxSizeElements
    * @param maxSizeBytes
    * @param expirationIntervalMillis
    * @param idleIntervalMillis
    * @param clock
    * @param eventNotificationExecutor
    * @param overflowDiskStorage
    * @param objectSizeCalculator
    * @param dataSource                data source. This can be a custom implementation or {@link
    *                                  DummyBinaryStoreDataSource}
    * @param dataStore                 data store. This can be a custom implementation or {@link DummyDataStore}
    * @param invalidator               invalidator. This can be a custom implementation or {@link
    *                                  DummyCacheInvalidator}
    * @param loader
    * @param eventNotification
    */
   public LocalCache(final String name, final long maxSizeElements, final long maxSizeBytes,
           final long expirationIntervalMillis, final long idleIntervalMillis, final Clock clock,
           final Executor eventNotificationExecutor, final DiskStorage overflowDiskStorage,
           final ObjectSizeCalculator objectSizeCalculator,
           final BinaryStoreDataSource dataSource, final DataStore dataStore,
           final CacheInvalidator invalidator, final CacheLoader loader,
           final ElementEventNotification eventNotification) {

      this.eventNotificationExecutor = eventNotificationExecutor;
      this.eventNotification = eventNotification;

      try {

         this.name = name;
         this.clock = clock;
         this.elementCounter = new SharedCounter(maxSizeElements);
         this.byteCounter = new SharedCounter(maxSizeBytes);
         this.overflowDiskStorage = overflowDiskStorage;


         // Create context
         final BinaryStoreContext binaryStoreContext = new BinaryStoreContextImpl();
         binaryStoreContext.setObjectSizeCalculator(objectSizeCalculator);
         binaryStoreContext.setDiskStorage(overflowDiskStorage);
         binaryStoreContext.setInvalidator(invalidator);
         binaryStoreContext.setDataSource(dataSource);
         binaryStoreContext.setDataStore(dataStore);

         // Create binary store
         final BinaryStore newBinaryStore = new BinaryStore(clock, expirationIntervalMillis, idleIntervalMillis);
         newBinaryStore.attachToElementCounter(elementCounter);
         newBinaryStore.attachToByteCounter(byteCounter);
         newBinaryStore.setContext(binaryStoreContext);

         // Load
         final LoadableBinaryStoreAdapter loadableAdapter = new LoadableBinaryStoreAdapter(newBinaryStore);
         loader.load(loadableAdapter);

         // Set the atomic reference
         this.binaryStore.set(newBinaryStore);
      } catch (final RuntimeException e) {

         throw e;
      } catch (final Exception e) {

         throw new IllegalStateException(e);
      }
   }


   /**
    * Returns the number of key-value mappings in this map.  If the map contains more than <tt>Integer.MAX_VALUE</tt>
    * elements, returns <tt>Integer.MAX_VALUE</tt>.
    *
    * @return the number of key-value mappings in this map.
    */
   public int size() {

      readLock.lock();
      try {

         return validStorage().size();

      } finally {
         readLock.unlock();
      }
   }


   /**
    * Removes all mappings from this map.
    *
    * @throws UnsupportedOperationException clear is not supported by this map.
    */
   public void clear() {

      writeLock.lock();
      try {

         validStorage().clear();
      } finally {

         writeLock.unlock();
      }
   }


   /**
    * Returns <tt>true</tt> if this map contains no key-value mappings.
    *
    * @return <tt>true</tt> if this map contains no key-value mappings.
    */
   public boolean isEmpty() {

      readLock.lock();
      try {

         return validStorage().isEmpty();
      } finally {

         readLock.unlock();
      }
   }


   /**
    * {@inheritDoc}.
    */
   public boolean containsKey(final Object key) {

      final Binary binaryKey = toBinary(toSerializable("key", key));

      readLock.lock();
      try {

         return validStorage().containsKey(binaryKey);
      } finally {

         readLock.unlock();
      }
   }


   /**
    * Returns <tt>true</tt> if this map maps one or more keys to the specified value.  More formally, returns
    * <tt>true</tt> if and only if this map contains at least one mapping to a value <tt>v</tt> such that
    * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation will probably require time linear in the map
    * size for most implementations of the <tt>Map</tt> interface.
    *
    * @param value value whose presence in this map is to be tested.
    * @return <tt>true</tt> if this map maps one or more keys to the specified value.
    * @throws ClassCastException   if the value is of an inappropriate type for this map (optional).
    * @throws NullPointerException if the value is <tt>null</tt> and this map does not not permit <tt>null</tt> values
    *                              (optional).
    */
   public boolean containsValue(final Object value) {


      final Binary binaryValue = toBinary(toSerializable("value", value));

      readLock.lock();
      try {
         return validStorage().containsValue(binaryValue);
      } finally {

         readLock.unlock();
      }
   }


   /**
    * Returns a collection view of the values contained in this map.  The collection is detached from the map, so
    * changes to the map are not reflected in the collection, and vice-versa.
    *
    * @return a collection of the values contained in this map.
    */
   public Collection<V> values() {


      final Collection<Binary> binaryValues;
      writeLock.lock();
      try {
         binaryValues = validStorage().values();
      } finally {
         writeLock.unlock();
      }

      final Collection<V> objectValues = new ArrayList<V>(binaryValues.size());

      for (final Binary binary : binaryValues) {

         //noinspection unchecked
         objectValues.add((V) BinaryUtils.toObject(binary));
      }
      return objectValues;
   }


   /**
    * Copies all of the mappings from the specified map to this map.  The effect of this call is equivalent to that of
    * calling {@link #put(Object, Object) put(k, v)} on this map once for each mapping from key <tt>k</tt> to value
    * <tt>v</tt> in the specified map.  The behavior of this operation is unspecified if the specified map is modified
    * while the operation is in progress.
    *
    * @param map Mappings to be stored in this map.
    * @throws UnsupportedOperationException if the <tt>putAll</tt> method is not supported by this map.
    * @throws ClassCastException            if the class of a key or value in the specified map prevents it from being
    *                                       stored in this map.
    * @throws IllegalArgumentException      some aspect of a key or value in the specified map prevents it from being
    *                                       stored in this map.
    * @throws NullPointerException          the specified map is <tt>null</tt>, or if this map does not permit
    *                                       <tt>null</tt> keys or values, and the specified map contains <tt>null</tt>
    *                                       keys or values.
    */
   public void putAll(final Map<? extends K, ? extends V> map) {


      final Map<Binary, Binary> binaryMap = new HashMap<Binary, Binary>(map.size());

      for (final Entry<? extends Serializable, ? extends Serializable> entry : map.entrySet()) {

         final Binary key = toBinary(entry.getKey());
         final Binary value = toBinary(entry.getValue());
         binaryMap.put(key, value);
      }

      writeLock.lock();
      try {

         validStorage().putAll(binaryMap);
      } finally {

         writeLock.unlock();
      }
   }


   /**
    * Returns a set view of the mappings contained in this map.  Each element in the returned set is a {@link Entry}.
    * The set is detached from the map, so changes to the map are not reflected in the set, and vice-versa.
    *
    * @return a set view of the mappings contained in this map.
    */
   @SuppressWarnings({"TooBroadScope", "unchecked"})
   public Set<Entry<K, V>> entrySet() {

      final Set<Entry<Binary, Binary>> entrySet;
      writeLock.lock();
      try {

         entrySet = validStorage().entrySet();
      } finally {

         writeLock.unlock();
      }

      final Set<Entry<K, V>> result = new HashSet<Entry<K, V>>(entrySet.size(), 1.0f);
      for (final Entry<Binary, Binary> binaryEntry : entrySet) {

         final K key = (K) BinaryUtils.toObject(binaryEntry.getKey());
         final V value = (V) BinaryUtils.toObject(binaryEntry.getValue());
         result.add(new SerializableMapEntry<K, V>(key, value));
      }

      return result;
   }


   /**
    * Returns a set view of the keys contained in this map.  The set is detached from the map, so changes to the map are
    * not reflected in the set, and vice-versa.
    *
    * @return a set view of the keys contained in this map.
    */
   @SuppressWarnings("unchecked")
   public Set<K> keySet() {

      final Set<Binary> binaryKeySet;

      writeLock.lock();
      try {

         binaryKeySet = validStorage().keySet();
      } finally {

         writeLock.unlock();
      }

      final Set<K> result = new HashSet<K>(validStorage().size());
      for (final Binary binaryKey : binaryKeySet) {

         result.add((K) BinaryUtils.toObject(binaryKey));
      }

      return result;
   }


   /**
    * {@inheritDoc}
    */
   public V get(final Object key) {

      final Binary binaryKey = toBinary(toSerializable("key", key));
      final Binary binaryValue;

      writeLock.lock();
      try {

         final ReadableElement element = validStorage().get(binaryKey);
         binaryValue = BinaryStoreUtils.getValue(element);
      } catch (final RuntimeException e) {

         throw e;
      } catch (final Exception e) {

         throw new CacheonixException(e);
      } finally {

         writeLock.unlock();
      }

      //noinspection unchecked
      return (V) BinaryUtils.toObject(binaryValue);
   }


   /**
    * {@inheritDoc}
    */
   public CacheEntry entry(final K key) {

      final Binary binaryKey = toBinary(toSerializable("key", key));
      final Binary binaryValue;

      writeLock.lock();
      try {

         // Get element
         final ReadableElement element = validStorage().get(binaryKey);
         if (element == null) {

            return null;
         }

         // Get entry metadata
         final Time expirationTime = element.getExpirationTime();
         final Time createdTime = element.getCreatedTime();

         // Get value
         binaryValue = BinaryStoreUtils.getValue(element);

         //noinspection unchecked
         final V value = (V) BinaryUtils.toObject(binaryValue);

         return new CacheEntryImpl(key, value, createdTime, expirationTime);
      } catch (final RuntimeException e) {

         throw e;
      } catch (final Exception e) {

         throw new CacheonixException(e);
      } finally {

         writeLock.unlock();
      }
   }


   /**
    * Removes the mapping for this key from this map if it is present.   More formally, if this map contains a mapping
    * from key <tt>k</tt> to value <tt>v</tt> such that <code>(key==null ? k==null : key.equals(k))</code>, that mapping
    * is removed.  (The map can contain at most one such mapping.)
    * <p/>
    * <p>Returns the value to which the map previously associated the key, or <tt>null</tt> if the map contained no
    * mapping for this key.  (A <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt>
    * with the specified key if the implementation supports <tt>null</tt> values.)  The map will not contain a mapping
    * for the specified  key once the call returns.
    *
    * @param key key whose mapping is to be removed from the map.
    * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
    * @throws ClassCastException            if the key is of an inappropriate type for this map (optional).
    * @throws NullPointerException          if the key is <tt>null</tt> and this map does not not permit <tt>null</tt>
    *                                       keys (optional).
    * @throws UnsupportedOperationException if the <tt>remove</tt> method is not supported by this map.
    */
   public V remove(final Object key) {


      final Binary binaryKey = toBinary(toSerializable("key", key));

      writeLock.lock();
      try {

         final PreviousValue previousValue = validStorage().remove(binaryKey);

         //noinspection unchecked
         return (V) BinaryUtils.toObject(previousValue.getValue());
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * {@inheritDoc}
    */
   public boolean remove(final Object key, final Object value) {

      final Binary binaryKey = toBinary(toSerializable("key", key));
      final Binary binaryValue = toBinary(toSerializable("value", value));
      writeLock.lock();
      try {

         return validStorage().remove(binaryKey, binaryValue);
      } finally {
         writeLock.unlock();
      }
   }


   public boolean replace(final K key, final V oldValue, final V newValue) {

      final Binary binaryKey = toBinary(toSerializable("key", key));
      final Binary binaryOldValue = toBinary(toSerializable("oldValue", oldValue));
      final Binary binaryNewValue = toBinary(toSerializable("newValue", newValue));
      writeLock.lock();
      try {

         return validStorage().replace(binaryKey, binaryOldValue, binaryNewValue);
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public V replace(final K key, final V value) {

      final Binary binaryKey = toBinary(toSerializable("key", key));
      final Binary binaryValue = toBinary(toSerializable("value", value));
      writeLock.lock();
      try {

         final PreviousValue previousValue = validStorage().replace(binaryKey, binaryValue);
         return (V) BinaryUtils.toObject(previousValue.getValue());
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * Associates the specified value with the specified key in this map.  If the map previously contained a mapping for
    * this key, the old value is replaced by the specified value.  (A map <tt>m</tt> is said to contain a mapping for a
    * key <tt>k</tt> if and only if {@link #containsKey(Object) m.containsKey(k)} would return <tt>true</tt>.))
    *
    * @param key   key with which the specified value is to be associated.
    * @param value value to be associated with the specified key.
    * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.  A
    * <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt> with the specified key, if
    * the implementation supports <tt>null</tt> values.
    * @throws UnsupportedOperationException if the <tt>put</tt> operation is not supported by this map.
    * @throws ClassCastException            if the class of the specified key or value prevents it from being stored in
    *                                       this map.
    * @throws IllegalArgumentException      if some aspect of this key or value prevents it from being stored in this
    *                                       map.
    * @throws NullPointerException          this map does not permit <tt>null</tt> keys or values, and the specified key
    *                                       or value is <tt>null</tt>.
    */
   @SuppressWarnings("TooBroadScope")
   public V put(final K key, final V value) {

      final Binary binaryPreviousValue;
      final Binary binaryKey = toBinary(key);
      final Binary binaryValue = toBinary(value);

      writeLock.lock();
      try {

         binaryPreviousValue = validStorage().put(binaryKey, binaryValue);

      } finally {
         writeLock.unlock();
      }
      //noinspection unchecked
      return (V) BinaryUtils.toObject(binaryPreviousValue);
   }


   /**
    * Updates a keys with a value that was synchronously retrieved by prefetch.
    *
    * @param key
    * @param value
    * @param timeToRead
    * @param expectedElementUpdateCounter
    */
   public void update(final Serializable key, final Serializable value, final Time timeToRead,
           final long expectedElementUpdateCounter) throws ShutdownException {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Begin updating"); // NOPMD

      final Binary binaryKey = toBinary(key);
      final Binary binaryValue = toBinary(value);

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("In update lock section"); // NOPMD

      writeLock.lock();
      try {

         validStorage().update(binaryKey, binaryValue, timeToRead, expectedElementUpdateCounter);

      } catch (final RuntimeException e) {

         throw e;
      } catch (final Exception e) {

         throw new CacheonixException(e);
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * {@inheritDoc}
    */
   public V put(final K key, final V value, final long delay, final TimeUnit timeUnit) {

      final Binary binaryPreviousValue;
      final Binary binaryKey = toBinary(key);
      final Binary binaryValue = toBinary(value);
      final Time expirationTime = createExpirationTime(clock, delay, timeUnit);

      writeLock.lock();
      try {
         binaryPreviousValue = validStorage().put(binaryKey, binaryValue, expirationTime);
      } finally {
         writeLock.unlock();
      }

      //noinspection unchecked
      return (V) BinaryUtils.toObject(binaryPreviousValue);
   }


   public V putIfAbsent(final K key, final V value) {

      final Binary binaryPreviousValue;
      final Binary binaryKey = toBinary(key);
      final Binary binaryValue = toBinary(value);

      writeLock.lock();
      try {

         binaryPreviousValue = validStorage().putIfAbsent(binaryKey, binaryValue);

      } finally {
         writeLock.unlock();
      }

      //noinspection unchecked
      return (V) BinaryUtils.toObject(binaryPreviousValue);
   }


   /**
    * Returns current cache statistics. The statistics is returned in a {@link CacheStatistics} object.
    *
    * @return current cache statistics.
    */
   public CacheStatistics getStatistics() {

      return validStorage().getStatistics();
   }


   /**
    * Returns cache name.
    *
    * @return cache name.
    */
   public String getName() {

      return name;
   }


   /**
    * Shuts down this cache. A cache that was shut down cannot be restarted.
    *
    * @throws IllegalStateException if the cache was already shutdown.
    */
   public void shutdown() {

      writeLock.lock();
      try {

         binaryStore.set(null);
         overflowDiskStorage.shutdown(true);
      } finally {
         writeLock.unlock();
      }
   }


   public List getKeyOwners() {

      return Collections.emptyList();
   }


   @SuppressWarnings({"unchecked", "TooBroadScope"})
   public Serializable execute(final Executable executable, final Aggregator aggregator) {

      // Get entries
      final ArrayList<CacheEntry> entriesToProcess = new ArrayList<CacheEntry>(binaryStore.get().size());

      readLock.lock();
      try {

         binaryStore.get().forEachElement(new BinaryStoreElementProcedure() {

            public boolean processEntry(final Binary key, final Binary value) {

               // REVIEWME: simeshev@cacheonix.org -> 2016-04-15 - Find if there is a way
               // to find out created time and expiration time. For now leaving it empty.
               final LocalCacheEntry localCacheEntry = new LocalCacheEntry(key, value, null, null);
               entriesToProcess.add(localCacheEntry);

               return true;
            }
         });
      } catch (final StorageException e) {

         throw new IllegalStateException(e);
      } finally {

         readLock.unlock();
      }

      // Execute
      final Serializable executionResult = executable.execute(entriesToProcess);

      // Aggregate and return the result
      final Collection<Serializable> collection = new ArrayList<Serializable>(1);
      collection.add(executionResult);
      return aggregator.aggregate(collection);
   }


   public Serializable execute(final EntryFilter entryFilter, final Executable executable,
           final Aggregator aggregator) {

      // Create entries
      final Collection<CacheEntry> entries = new LinkedList<CacheEntry>();

      readLock.lock();
      try {

         binaryStore.get().forEachElement(new BinaryStoreElementProcedure() {

            public boolean processEntry(final Binary key, final Binary value) {

               // REVIEWME: simeshev@cacheonix.com -> 2016-04-15 - Find if there is a way
               // to find out created time and expiration time. For now leaving it empty.
               final LocalCacheEntry cacheEntry = new LocalCacheEntry(key, value, null, null);
               if (entryFilter.matches(cacheEntry)) {

                  entries.add(cacheEntry);
               }

               return true;
            }
         });
      } catch (final StorageException e) {

         throw new IllegalStateException(e);
      } finally {

         readLock.unlock();
      }

      // Execute
      final Serializable executionResult = executable.execute(entries);
      final Collection<Serializable> collection = new ArrayList<Serializable>(1);
      collection.add(executionResult);
      return aggregator.aggregate(collection);
   }


   public Serializable executeAll(final Set<K> keySet, final Executable executable,
           final Aggregator aggregator) {


      // Create entries
      final Collection<CacheEntry> entries = new ArrayList<CacheEntry>(keySet.size());
      readLock.lock();
      try {

         for (final Serializable key : keySet) {

            final Binary binaryKey = toBinary(key);
            final ReadableElement element = binaryStore.get().get(binaryKey);
            final Binary binaryValue = BinaryStoreUtils.getValue(element);

            // REVIEWME: simeshev@cacheonix.org -> 2016-04-15 - Find if there is a way
            // to find out created time and expiration time. For now leaving it empty.
            entries.add(new LocalCacheEntry(binaryKey, binaryValue, null, null));
         }
      } catch (final RuntimeException e) {

         throw e;
      } catch (final Exception e) {

         throw new CacheonixException(e);
      } finally {

         readLock.unlock();
      }

      // Execute
      final Serializable executionResult = executable.execute(entries);
      final Collection<Serializable> collection = new ArrayList<Serializable>(1);
      collection.add(executionResult);
      return aggregator.aggregate(collection);
   }


   public boolean removeAll(final Set<K> keySet) {

      writeLock.lock();
      try {

         boolean modified = false;
         final BinaryStore store = validStorage();
         for (final K key : keySet) {

            final Binary binaryKey = toBinary(key);
            final PreviousValue previousValue = store.remove(binaryKey);
            modified |= previousValue.isPreviousValuePresent();
         }

         return modified;
      } finally {
         writeLock.unlock();
      }
   }


   @SuppressWarnings("unchecked")
   public Map<K, V> getAll(final Set<K> keys) {

      final Map<K, V> result = new HashMap<K, V>(keys.size());

      writeLock.lock();
      try {

         final BinaryStore store = validStorage();
         for (final K key : keys) {

            final Binary binaryKey = toBinary(key);
            if (store.containsKey(binaryKey)) {

               final ReadableElement element = store.get(binaryKey);
               final Binary binaryValue = BinaryStoreUtils.getValue(element);
               final K foundKey = (K) BinaryUtils.toObject(binaryKey);
               final V foundValue = (V) BinaryUtils.toObject(binaryValue);
               result.put(foundKey, foundValue);
            }
         }

         return result;
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheonixException(e);
      } finally {
         writeLock.unlock();
      }
   }


   public boolean retainAll(final Set<K> keySet) {

      // Create a binary key set
      final Set<Binary> binaryKeys = new HashSet<Binary>(keySet.size());
      for (final K key : keySet) {

         binaryKeys.add(toBinary(key));
      }

      // Process retainAll();
      writeLock.lock();
      try {

         return validStorage().retainAll(binaryKeys);
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * Returns the number of key-value mappings in this map.  If the map contains more than <tt>Long.MAX_VALUE</tt>
    * elements, returns <tt>Long.MAX_VALUE</tt>.
    *
    * @return the number of key-value mappings in this map.
    */
   public long longSize() {

      return (long) size();
   }


   /**
    * Returns <code>true</code> if the cache was shut down.
    *
    * @see #shutdown()
    */
   public boolean isShutdown() {

      return binaryStore.get() == null;
   }


   /**
    * {@inheritDoc}
    */
   public void addEventSubscriber(final Set<K> keys, final EntryModifiedSubscriber subscriber) {

      // Wrap into safe subscriber and add
      final SafeEntryUpdateSubscriber safeSubscriber = new SafeEntryUpdateSubscriber(subscriber);
      final BinaryEntryModifiedSubscriberAdapter binarySubscriber = new BinaryEntryModifiedSubscriberAdapter(
              safeSubscriber);
      validStorage().addEventSubscriber(BinaryUtils.toBinarySet(keys), binarySubscriber);
   }


   /**
    * {@inheritDoc}
    */
   public void addEventSubscriber(final K key, final EntryModifiedSubscriber subscriber) {

      final BinaryEntryModifiedSubscriberAdapter binarySubscriber = makeBinarySubscriber(subscriber);
      validStorage().addEventSubscriber(BinaryUtils.toBinarySet(key), binarySubscriber);
   }


   private BinaryEntryModifiedSubscriberAdapter makeBinarySubscriber(final EntryModifiedSubscriber subscriber) {

      final SafeEntryUpdateSubscriber safeSubscriber = new SafeEntryUpdateSubscriber(subscriber);

      if (ASYNCHRONOUS.equals(eventNotification)) {

         // Wrap safe subscriber into async subscriber
         return new BinaryEntryModifiedSubscriberAdapter(new AsynchronousEntryModifiedSubscriberAdapter(
                 eventNotificationExecutor, safeSubscriber));
      } else if (SYNCHRONOUS.equals(eventNotification)) {

         // Use the subscriber straight
         return new BinaryEntryModifiedSubscriberAdapter(safeSubscriber);
      } else {
         throw new IllegalArgumentException("Unsupported event notification: " + eventNotification);
      }
   }


   /**
    * {@inheritDoc}
    */
   public void removeEventSubscriber(final Set<K> keys,
           final EntryModifiedSubscriber subscriber) throws NotSubscribedException {

      final int subscriberIdentity = System.identityHashCode(subscriber);

      writeLock.lock();
      try {

         NotSubscribedException notSubscribedException = null;
         for (final K key : keys) {

            try {

               validStorage().removeEventSubscriber(toBinary(key), subscriberIdentity);
            } catch (final NotSubscribedException e) {

               // Don't throw now, remember instead
               notSubscribedException = e;
            }
         }

         // Throw remembered exception if any
         if (notSubscribedException != null) {

            final String message = notSubscribedException.getMessage();
            if (StringUtils.isBlank(message)) {

               final NotSubscribedException moreInformativeException = new NotSubscribedException(
                       subscriber.toString());
               moreInformativeException.setStackTrace(notSubscribedException.getStackTrace());
               throw moreInformativeException;
            } else {

               throw notSubscribedException;
            }
         }
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * {@inheritDoc}
    */
   public void removeEventSubscriber(final K key,
           final EntryModifiedSubscriber subscriber) throws NotSubscribedException {

      final HashSet<K> keys = new HashSet<K>(1);
      keys.add(key);

      removeEventSubscriber(keys, subscriber);
   }


   /**
    * @return maximum number of elements in memory.
    */
   public long getMaxSize() {

      return elementCounter.getMaxValue();
   }


   /**
    * @return number of elements evicted to disk.
    */
   public long getSizeOnDisk() {

      return validStorage().getSizeOnDisk();
   }


   /**
    * {@inheritDoc}
    */
   public Time getExpirationInterval() {

      return validStorage().getExpirationInterval();
   }


   /**
    * {@inheritDoc}
    */
   public Time getIdleInterval() {

      return validStorage().getIdleInterval();
   }


   /**
    * Returns <code>CacheMember</code> that is responsible for storing a given key.
    *
    * @return <code>CacheMember</code> that is responsible for storing a given key.
    */
   public CacheMember getKeyOwner(final K key) {

      return new LocalCacheMember(name);
   }


   /**
    * Returns maximum size of the cache in bytes. Cacheonix does not limit the cache size if the maximum size is not set
    * or if it is set to zero.
    *
    * @return maximum size of the cache in bytes.
    */
   public long getMaxSizeBytes() {

      return byteCounter.getMaxValue();
   }


   /**
    * Returns size of keys and values in bytes. Returns zero if eviction based on object size is not enabled for this
    * cache.
    *
    * @return size of keys and values in bytes or zero if eviction based on object size is not enabled for this cache.
    */
   public long getSizeBytes() {

      readLock.lock();
      try {

         return byteCounter.value();

      } finally {
         readLock.unlock();
      }
   }


   /**
    * {@inheritDoc}
    */
   public org.cacheonix.locks.ReadWriteLock getReadWriteLock() {


      return getReadWriteLock("default");
   }


   /**
    * {@inheritDoc}
    */
   public org.cacheonix.locks.ReadWriteLock getReadWriteLock(final Serializable lockKey) {

      writeLock.lock();
      try {

         org.cacheonix.locks.ReadWriteLock lock = lockRegistry.get(lockKey);

         if (lock == null) {

            lock = new LocalReadWriteLock(lockKey);
            lockRegistry.put(lockKey, lock);
         }
         return lock;

      } finally {

         writeLock.unlock();
      }
   }


   /**
    * Returns a valid binary storage.
    *
    * @return a valid binary storage.
    * @throws ShutdownException if this local cache has been shutdown
    */
   private BinaryStore validStorage() throws ShutdownException {

      if (isShutdown()) {

         throw new ShutdownException("Cache " + name + " has been shutdown");
      }
      return binaryStore.get();
   }


   /**
    * Casts an argument to <code>java.io.Serializable</code>
    *
    * @param argumentName an argument name.
    * @param argument     the argument to cast.
    * @return a Serializable
    * @throws IllegalArgumentException if the argument is not serializable.
    */
   private static Serializable toSerializable(final String argumentName,
           final Object argument) throws IllegalArgumentException {

      if (argument != null && !(argument instanceof Serializable)) {

         throw new IllegalArgumentException(
                 "Argument '" + argumentName + "' must implement java.io.Serializable: " + argument.getClass().getName());
      }

      return (Serializable) argument;
   }


   public String toString() {

      return "LocalCache{" +
              "name='" + name + '\'' +
              ", elementCounter=" + elementCounter +
              ", byteCounter=" + byteCounter +
              ", binaryStore=" + binaryStore +
              ", readWriteLock=" + readWriteLock +
              ", readLock=" + readLock +
              ", writeLock=" + writeLock +
              ", overflowDiskStorage=" + overflowDiskStorage +
              '}';
   }

}
