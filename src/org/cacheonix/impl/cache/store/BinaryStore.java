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
package org.cacheonix.impl.cache.store;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cacheonix.CacheonixException;
import org.cacheonix.NotSubscribedException;
import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.impl.RuntimeIOException;
import org.cacheonix.impl.RuntimeStorageException;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSource;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSourceObject;
import org.cacheonix.impl.cache.datastore.StorableImpl;
import org.cacheonix.impl.cache.distributed.partitioned.BinaryStoreContext;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.BinaryFactory;
import org.cacheonix.impl.cache.item.BinaryFactoryBuilder;
import org.cacheonix.impl.cache.item.BinaryType;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.ObjectObjectProcedure;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.cache.store.BinaryStoreUtils.getExpirationTime;
import static org.cacheonix.impl.cache.store.BinaryStoreUtils.getValue;

/**
 * Implementation of cache with LRU eviction policy.
 *
 * @noinspection JavaDoc
 */
public final class BinaryStore implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * A factory for binaries.
    */
   private static final BinaryFactoryBuilder BINARY_FACTORY_BUILDER = new BinaryFactoryBuilder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BinaryStore.class); // NOPMD

   /**
    * Initial capacity for the map.
    */
   private static final int INITIAL_CAPACITY = 11;

   /**
    * System clock.
    */
   private transient Clock clock;


   private final BinaryFactory binaryFactory = BINARY_FACTORY_BUILDER.createFactory(BinaryType.BY_COPY);

   /**
    * Map the holds cache entries.
    */
   private HashMap<Binary, BinaryStoreElement> elements; // NOPMD

   /**
    * The head of the doubly linked list.
    */
   private BinaryStoreElement header;

   /**
    * Cache statistics.
    */
   private CacheStatisticsImpl statistics = new CacheStatisticsImpl();

   /**
    * Number of milliseconds since the element was put into cache before an element is discarded from the cache.
    */
   private Time expirationInterval;

   /**
    * Number milliseconds since the element was accessed last time before an element is discarded from the cache.
    */
   private Time idleInterval;

   /**
    * Secondary storage for this cache.
    */
//   private DiskStorage diskStorage;

   /**
    * Maximum number of elements stored in memory.
    */
   private SharedCounter elementCounter;

   /**
    * Max size of elements stored in memory, bytes.
    */
   private SharedCounter byteCounter;

   /**
    * A list of subscribers for the event when an element is updated. The BinaryStore needs to keep track of subscribers
    * to support attaching subscribers to newly added elements.
    */
   private final transient Map<Binary, List<BinaryEntryModifiedSubscriber>> updateSubscribers = new HashMap<Binary, List<BinaryEntryModifiedSubscriber>>(
           1); // NOPMD

   private BinaryStoreContext binaryStoreContext;

   private BinaryStoreElementContext binaryStoreElementContext;


   public BinaryStore() {

   }


   /**
    * @param expirationIntervalMillis
    * @param idleIntervalMillis
    */
   public BinaryStore(final Clock clock, final long expirationIntervalMillis, final long idleIntervalMillis) {

      this.clock = clock;
      this.expirationInterval = expirationIntervalMillis == 0 ? null : new TimeImpl(expirationIntervalMillis, 0L);
      this.idleInterval = idleIntervalMillis == 0 ? null : new TimeImpl(idleIntervalMillis, 0L);
      this.elements = new HashMap<Binary, BinaryStoreElement>(INITIAL_CAPACITY);
      this.initLinkedList();
   }


   private void initLinkedList() {

      this.header = createElement(null, null, null);
      this.header.setBefore(header);
      this.header.setAfter(header);
   }


   /**
    * Returns the number of key-value mappings in this map.  If the map contains more than <tt>Integer.MAX_VALUE</tt>
    * elements, returns <tt>Integer.MAX_VALUE</tt>.
    *
    * @return the number of key-value mappings in this map.
    */
   public int size() {

      return elements.size();
   }


   /**
    * Attaches this store to a shared element counter. The counter value is adjusted according to the content of this
    * store.
    *
    * @param elementCounter the element counter to attach to.
    */
   public void attachToElementCounter(final SharedCounter elementCounter) {

      this.elementCounter = elementCounter;
      this.elementCounter.add(elements.size());
   }


   public void detachElementCounter() {

      elementCounter.subtract(elements.size());
      elementCounter = null;
   }


   /**
    * Attaches this binary store to a shared byte counter. The counter value is adjusted according to the content of
    * this store.
    *
    * @param byteCounter the shared byte counter to attach to.
    */
   public void attachToByteCounter(final SharedCounter byteCounter) {

      this.byteCounter = byteCounter;

      // Add our size to the byte counter
      final long byteSize = calculateSizeBytes();

      this.byteCounter.add(byteSize);
   }


   public void detachByteCounter() {

      final long sizeBytes = calculateSizeBytes();
      byteCounter.subtract(sizeBytes);
      byteCounter = null;
   }


   /**
    * Removes all stored elements from the disk store and disconnects the bucket from a disk store.
    * <p/>
    * This is a one time operation that is called before discarding the bucket.
    */
   public void detachDiskStorage() {

      // For each stored element, remove stored value
      elements.forEachValue(new ObjectProcedure<BinaryStoreElement>() {

         public boolean execute(final BinaryStoreElement element) {

            try {

               removeFromDiskStorage(element);

            } catch (final IOException e) {
               LOG.error(e, e);
            }

            return true;
         }
      });

      // Set disk storage to null;
      binaryStoreContext.setDiskStorage(null);
   }


   /**
    * Calculates a number of bytes occupied by the store elements.
    *
    * @return the number of bytes occupied by the store elements.
    */
   private long calculateSizeBytes() {

      final long[] byteSize = {0L};

      elements.forEachValue(new ObjectProcedure<BinaryStoreElement>() {

         public boolean execute(final BinaryStoreElement element) {

            // Increment
            byteSize[0] += element.getSizeBytes();

            // Continue
            return true;
         }
      });

      return byteSize[0];
   }


   /**
    * Sets a system clock. This method must be called immediately after de-serialization is complete.
    *
    * @param clock the system clock to set.
    */
   public void setClock(final Clock clock) {

      this.clock = clock;
   }


   /**
    * Removes all mappings from this map.
    *
    * @throws UnsupportedOperationException clear is not supported by this map.
    */
   public void clear() {

      // Get sizes before clear to use to adjust shared counters
      final long oldSizeBytes = calculateSizeBytes();
      final long oldSize = elements.size();

      // Discard stored elements from the shared storage. Binary store can share the disk storage with other Binary
      // stores belonging to the same cache processor. This means that stored elements belonging only to the store
      // being cleanup must be removed from the disk storage.

      detachDiskStorage();

      // Clear elements
      elements.clear();

      // Re-initialize linked list
      initLinkedList();

      // Clear statistics
      statistics.reset();

      // Adjust shared element counter
      elementCounter.subtract(oldSize);

      // Adjust shared bye counter
      byteCounter.subtract(oldSizeBytes);
   }


   /**
    * Returns <tt>true</tt> if this map contains no key-value mappings.
    *
    * @return <tt>true</tt> if this map contains no key-value mappings.
    */
   public boolean isEmpty() {

      return elements.isEmpty();
   }


   /**
    * Returns <tt>true</tt> if this map contains a mapping for the specified key.  More formally, returns <tt>true</tt>
    * if and only if this map contains at a mapping for a key <tt>k</tt> such that <tt>(key==null ? k==null :
    * key.equals(k))</tt>.  (There can be at most one such mapping.)
    *
    * @param key key whose presence in this map is to be tested.
    * @return <tt>true</tt> if this map contains a mapping for the specified key.
    * @throws ClassCastException   if the key is of an inappropriate type for this map (optional).
    * @throws NullPointerException if the key is <tt>null</tt> and this map does not not permit <tt>null</tt> keys
    *                              (optional).
    */
   public boolean containsKey(final Binary key) {

      try {

         // Check if exists
         final BinaryStoreElement binaryStoreElement = elements.get(key);
         if (binaryStoreElement == null) {

            return false;
         }

         // Check if expired or was invalidated
         if (binaryStoreElement.isExpired(clock) || !binaryStoreElement.isValid()) {

            // Remove and notify about expiration
            removeElement(binaryStoreElement, EntryModifiedEventType.EXPIRE);
            return false;
         }

         // Key exists
         return true;
      } catch (final IOException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      } catch (final StorageException e) {
         throw ExceptionUtils.createIllegalStateException(e);
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
   public boolean containsValue(final Binary value) {

      try {

         if (elements.isEmpty()) {

            return false;
         }

         final Collection<Entry<Binary, BinaryStoreElement>> collection = elements.entrySet();

         for (final Entry<Binary, BinaryStoreElement> entry : collection) {

            // Get cache element
            final BinaryStoreElement element = entry.getValue();
            final Binary binaryValue = getValue(element);

            // Compare
            if (value != null && binaryValue != null && value.equals(binaryValue)
                    || value == null && binaryValue == null) {

               return true;
            }
         }
         return false;
      } catch (final StorageException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   /**
    * Returns a collection view of the values contained in this map.  The collection is detached from the map, so
    * changes to the map are not reflected in the collection, and vice-versa.
    *
    * @return a collection of the values contained in this map.
    */
   public Collection<Binary> values() {

      // General checks
      if (elements.isEmpty()) {
         return new ArrayList<Binary>(0);
      }

      // Create result collection
      final List<Binary> result = new ArrayList<Binary>(elements.size());

      // Process
      final Time idleTime = calculateIdleTime(idleInterval);

      elements.retainEntries(new ObjectObjectProcedure<Binary, BinaryStoreElement>() {

         public boolean execute(final Binary key, final BinaryStoreElement element) {

            try {

               if (element.isExpired(clock) || !element.isValid()) {

                  // Remove element if it is expired
                  removeElement(element);

                  return false;
               } else {

                  // Update idle time
                  element.setIdleTime(idleTime);

                  // Set value
                  final Binary binaryValue = getValue(element);
                  result.add(binaryValue);

                  return true;
               }
            } catch (final IOException e) {
               throw new RuntimeIOException(e);
            } catch (final StorageException e) {
               throw new RuntimeStorageException(e);
            }
         }
      });

      return result;
   }


   /**
    * Copies all of the mappings from the specified map to this map.  The effect of this call is equivalent to that of
    * calling {@link #put(Binary, Binary)} on this map once for each mapping from key <tt>k</tt> to value <tt>v</tt> in
    * the specified map.  The behavior of this operation is unspecified if the specified map is modified while the
    * operation is in progress.
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
   public void putAll(final Map<Binary, Binary> map) {

      // Check if there is anything to do
      if (map.isEmpty()) {
         return;
      }

      try {

         for (final Entry<Binary, Binary> entry : map.entrySet()) {

            final Binary key = entry.getKey();
            final Binary value = entry.getValue();
            final Time expirationTime = calculateExpirationTime(expirationInterval);

            put(key, value, expirationTime, false, null);
         }
      } catch (final StorageException e) {
         throw new RuntimeStorageException(e);
      } catch (final IOException e) {
         throw new RuntimeIOException(e);
      }
   }


   /**
    * Handles puts.
    *
    * @param key                          to put
    * @param value                        to put
    * @param expirationTime
    * @param returnReplacedValue          <code>true</code> if value should be returned.  @return replaced value or null
    *                                     if was requested not to return value.
    * @param timeTookToReadFromDataSource
    * @throws IOException
    * @throws StorageException
    * @noinspection ParameterHidesMemberVariable
    */
   public ReadableElement put(final Binary key, final Binary value, final Time expirationTime,
           final boolean returnReplacedValue, final Time timeTookToReadFromDataSource)
           throws IOException, StorageException {

      // Put into the element map
      final BinaryStoreElement newElement = createElement(key, value, expirationTime);
      final BinaryStoreElement replacedElement = elements.put(newElement.getKey(), newElement);

      // Calculate new size
      final long newElementSizeBytes = newElement.getSizeBytes();
      final long replacedElementSizeBytes = replacedElement == null ? 0L : replacedElement.getSizeBytes();
      final long byteSizeChange = newElementSizeBytes - replacedElementSizeBytes;
      byteCounter.add(byteSizeChange);

      if (replacedElement == null) {

         // Previous element doesn't exist

         // Update shared size counter
         elementCounter.increment();

         // Update statistics
         statistics.incrementWriteMissCount();

         // Add subscribers
         if (!updateSubscribers.isEmpty()) {

            newElement.addEntryModifiedSubscribers(updateSubscribers.get(key));
         }

         // Notify update listeners
         newElement.notifyModificationSubscribers(null, EntryModifiedEventType.ADD);

      } else {

         // There is previous element

         // Cancel prefetch
         replacedElement.cancelPrefetch();

         // Load value from storage if necessary
         if (returnReplacedValue && !replacedElement.isExpired(clock)) {

            // We use a direct value stored in the element instead of the copy because life of the
            // replaced element object ends upon exit of this method.
            replacedElement.load();
         }

         // Update statistics
         statistics.incrementWriteHitCount();

         // Increment update counter
         newElement.setUpdateCounter(replacedElement.getUpdateCounter() + 1L);

         // Transfer subscribers
         replacedElement.transferEntryModifiedSubscribers(newElement);


         // Notify update listeners
         newElement.notifyModificationSubscribers(replacedElement, EntryModifiedEventType.UPDATE);

         // Remove replaced element
         replacedElement.removeFromLRUList();
         removeFromDiskStorage(replacedElement);
      }

      // Schedule prefetch
      final BinaryStoreDataSource binaryStoreDataSource = binaryStoreContext.getDataSource();
      binaryStoreDataSource.schedulePrefetch(newElement, timeTookToReadFromDataSource);

      // Add new new element to the end of the linked list
      addToLRUList(newElement);

      // Store element in the user-provided data store

      final DataStore dataStore = binaryStoreContext.getDataStore();
      dataStore.store(new StorableImpl(key, value));

      // Evict eldest element if exceeded size
      guardElementCount();

      // Evict elements if exceeded byte size
      guardByteSize();

      return !returnReplacedValue || replacedElement == null || replacedElement.isExpired(
              clock) ? null : replacedElement;
   }


   /**
    * Updates an element on condition that the element update counter is the same.
    *
    * @param key                          the key to update.
    * @param value                        the new value.
    * @param timeToRead                   time took to read the element from a data source. Can be null meaning that it
    *                                     wasn't read from the data source.
    * @param expectedElementUpdateCounter the element update counter that the element should have to be updated.
    * @return a previous value.
    */
   public ReadableElement update(final Binary key, final Binary value, final Time timeToRead,
           final long expectedElementUpdateCounter) throws StorageException, IOException {

      // Get element
      final BinaryStoreElement element = elements.get(key);

      // Check if element exists
      if (element == null) {

         // There is nothing to update, the element is gone, exit
         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Nothing to update, key: " + key); // NOPMD
         return null;
      }

      // Check if version match
      if (element.getUpdateCounter() != expectedElementUpdateCounter) {

         // Element has been updated, exit
         if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Element has changed, expected '" + expectedElementUpdateCounter + "' but got '" + element.getUpdateCounter() + "', element: " + element); // NOPMD
         }
         return null;
      }


      final Time expirationTime = calculateExpirationTime(expirationInterval);
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Updating element: " + element); // NOPMD
      return put(key, value, expirationTime, true, timeToRead);
   }


   private void guardElementCount() throws StorageException, IOException {

      // Check if this is unlimited size
      if (elementCounter.isUnlimitedSize()) {
         return;
      }

      // Check if current size is smaller than maximum
      if (elements.size() <= elementCounter.getMaxValue()) {
         return;
      }

      // Evict
      final BinaryStoreElement eldestElement = header.getAfter();
      removeElement(eldestElement, EntryModifiedEventType.EVICT);
   }


   private void guardByteSize() throws IOException, StorageException {

      // Check if unlimited size
      if (byteCounter.isUnlimitedSize()) {

         return;
      }

      // Check if need guarding
      if (byteCounter.value() <= byteCounter.getMaxValue()) {

         return;
      }


      // By the time this method is called, the size could have been increased significantly.
      // First, we have to try to store elements to reduce size. Second, if the size still exceeds
      // the maximum allowed, we will have to evict some elements.

      // Try to store elements that are not already stored
      BinaryStoreElement element = header.getAfter();
      while (!element.equals(header) && byteCounter.value() > byteCounter.getMaxValue()) {

         // Save element after
         final BinaryStoreElement elementAfter = element.getAfter();

         // Guard against expiration
         if (element.isExpired(clock) || !element.isValid()) {

            // Remove element
            removeElement(element, EntryModifiedEventType.EXPIRE);

         } else {

            // Try to store
            if (!element.isStored()) {

               final long oldSizeBytes = element.getSizeBytes();

               if (element.store()) {

                  // Successfuly stored to disk. The key and the reference to storage are still there.
                  final long newSizeBytes = element.getSizeBytes();
                  final long byteSizeChange = oldSizeBytes - newSizeBytes;
                  byteCounter.subtract(byteSizeChange);

                  // Increment counter
                  statistics.incrementElementsOnDiskCount();
               } else {

                  // Could not save to disk, stop traversing the list
                  break;
               }
            }
         }


         // Set next element
         element = elementAfter;
      }

      // Evict until max value
      element = header.getAfter();
      while (!element.equals(header) && byteCounter.value() > byteCounter.getMaxValue()) {

         removeElement(element, EntryModifiedEventType.EVICT);
         element = header.getAfter();
      }
   }


   private void removeElement(final BinaryStoreElement element,
           final EntryModifiedEventType eventType) throws IOException, StorageException {


      // Notify listeners
      assert EntryModifiedEventType.EVICT.equals(eventType) || EntryModifiedEventType.EXPIRE.equals(eventType);
      element.notifyModificationSubscribers(element, eventType);

      elements.remove(element.getKey());

      removeElement(element);
   }


   /**
    * Removes element from all structures except {@link #elements}.
    *
    * @param element an element to remove.
    * @throws IOException if I/O error occurred.
    */
   private void removeElement(final BinaryStoreElement element) throws IOException {

      element.cancelPrefetch();
      element.removeFromLRUList();
      byteCounter.subtract(element.getSizeBytes());
      elementCounter.decrement();
      removeFromDiskStorage(element);
   }


   /**
    * Helper method to create a cache element.
    *
    * @param key            of the element.
    * @param value          of the element
    * @param expirationTime
    * @return created cache element
    * @noinspection ParameterHidesMemberVariable
    */
   private BinaryStoreElement createElement(final Binary key, final Binary value, final Time expirationTime) {

      // Create element
      final Time idleTime = idleInterval == null ? null : clock.currentTime().add(idleInterval);
      final BinaryStoreElement binaryStoreElement = new BinaryStoreElement(key, value, clock.currentTime(),
              expirationTime, idleTime);

      // Set context
      binaryStoreElement.setContext(binaryStoreElementContext);

      // Return result
      return binaryStoreElement;
   }


   /**
    * Adds element to the end of the linked list.
    *
    * @param element to add
    */
   private void addToLRUList(final BinaryStoreElement element) {

      final BinaryStoreElement before = header.getBefore();
      element.setAfter(header);
      element.setBefore(before);
      before.setAfter(element);
      header.setBefore(element);
   }


   /**
    * Returns a set view of the mappings contained in this map.  Each element in the returned set is a {@link
    * Entry}. The set is detached from the map, so changes to the map are not reflected in the set, and vice-versa.
    *
    * @return a set view of the mappings contained in this map.
    */
   public Set<Entry<Binary, Binary>> entrySet() {

      final Set<Entry<Binary, Binary>> result = new HashSet<Entry<Binary, Binary>>(elements.size());
      if (elements.isEmpty()) {

         return result;
      }

      final Time idleTime = calculateIdleTime(idleInterval);

      elements.retainEntries(new ObjectObjectProcedure<Binary, BinaryStoreElement>() {

         public boolean execute(final Binary key, final BinaryStoreElement element) {

            try {

               if (element.isExpired(clock) || !element.isValid()) {

                  // Remove element if it is expired
                  removeElement(element);

                  return false;
               } else {

                  // Update access time
                  element.setIdleTime(idleTime);

                  // Put the entry in the result
                  final Binary binaryValue = getValue(element);
                  result.add(new BinaryStoreEntry(element.getKey(), binaryValue));

                  return true;
               }
            } catch (final IOException e) {
               throw new RuntimeIOException(e);
            } catch (final StorageException e) {
               throw new RuntimeStorageException(e);
            }
         }
      });

      return result;
   }


   /**
    * Returns a set view of the keys contained in this map.  The set is detached from the map, so changes to the map are
    * not reflected in the set, and vice-versa.
    *
    * @return a set view of the keys contained in this map.
    */
   public Set<Binary> keySet() {

      final Set<Binary> result = new HashSet<Binary>(elements.size());
      if (elements.isEmpty()) {

         return result;
      }

      final Time idleTime = calculateIdleTime(idleInterval);

      elements.retainEntries(new ObjectObjectProcedure<Binary, BinaryStoreElement>() {

         public boolean execute(final Binary key, final BinaryStoreElement element) {

            try {

               if (element.isExpired(clock) || !element.isValid()) {

                  // Remove element if it is expired
                  removeElement(element);

                  return false;
               } else {

                  // Update access time
                  element.setIdleTime(idleTime);
                  result.add(key);
                  return true;
               }
            } catch (final IOException e) {
               throw new RuntimeIOException(e);
            }
         }
      });

      return result;
   }


   /**
    * Returns the value to which this map maps the specified key.  Returns <tt>null</tt> if the map contains no mapping
    * for this key.  A return value of <tt>null</tt> does not <i>necessarily</i> indicate that the map contains no
    * mapping for the key; it's also possible that the map explicitly maps the key to <tt>null</tt>.  The
    * <tt>containsKey</tt> operation may be used to distinguish these two cases.
    * <p/>
    * <p>More formally, if this map contains a mapping from a key <tt>k</tt> to a value <tt>v</tt> such that
    * <tt>(key==null ? k==null : key.equals(k))</tt>, then this method returns <tt>v</tt>; otherwise it returns
    * <tt>null</tt>.  (There can be at most one such mapping.)
    *
    * @param key key whose associated value is to be returned.
    * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no mapping for
    * this key.
    * @throws ClassCastException   if the key is of an inappropriate type for this map (optional).
    * @throws NullPointerException key is <tt>null</tt> and this map does not not permit <tt>null</tt> keys (optional).
    * @see #containsKey(Binary)
    */
   public ReadableElement get(final Binary key) throws InvalidObjectException {

      try {

         final BinaryStoreElement element = getElement(key);
         if (element == null) {

            // Increment read miss
            statistics.incrementReadMissCount();

//            //noinspection ControlFlowStatementWithoutBraces
//            if (LOG.isDebugEnabled()) LOG.debug("Read miss, element: " + element); // NOPMD

            final BinaryStoreDataSource binaryStoreDataSource = binaryStoreContext.getDataSource();
            final BinaryStoreDataSourceObject binaryStoreDataSourceObject = binaryStoreDataSource.get(key);
            if (binaryStoreDataSourceObject == null) {

               // Not found in data source.
               return null;
            } else {

               // Found in data source
               final Serializable valueFromDataSource = binaryStoreDataSourceObject.getObject();
               if (valueFromDataSource == null) {
                  return null;
               }

               // Put found value from the data source to the storage
               final Time timeTookToReadFromDataSource = binaryStoreDataSourceObject.getTimeToRead();
               final Binary binaryValue = objectToBinary(valueFromDataSource);
               final Time expirationTime = calculateExpirationTime(expirationInterval);
               final Time createdTime = element == null ? null : element.getCreatedTime();
               put(key, binaryValue, expirationTime, false, timeTookToReadFromDataSource);

               // Return a new readable element becuase put() above return previous
               // value which is not suitable for returning from get.
               return new SimpleReadableElement(binaryValue, createdTime, expirationTime);
            }
         } else {

            element.setIdleTime(calculateIdleTime(idleInterval));
            statistics.incrementReadHitCount();
            updateLRUAccess(element);
            return element;
         }
      } catch (final StorageException e) {

         throw ExceptionUtils.createIllegalStateException(e);
      } catch (final IOException e) {

         throw new RuntimeIOException(e);
      }
   }


   /**
    * Converts an object to a binary.
    *
    * @param objectValue an object value to convert to binary
    * @return
    */
   private Binary objectToBinary(final Serializable objectValue) throws InvalidObjectException {

      return binaryFactory.createBinary(objectValue);
   }


   private void updateLRUAccess(final BinaryStoreElement element) {

      element.removeFromLRUList();
      addToLRUList(element);
   }


   /**
    * Removes the mapping for this key from this map if it is present.   More formally, if this map contains a mapping
    * from key <tt>k</tt> to value <tt>v</tt> such that <code>(key==null ? k==null : key.equals(k))</code>, that mapping
    * is removed.  (The map can contain at most one such mapping.)
    * <p/>
    * <p>Returns the value to which the map previously associated the key. returned PreviousValue's
    * isPreviousValuePresent() returns <code>false</code> if the map contained no mapping for this key.   The map will
    * not contain a mapping for the specified key once the call returns.
    *
    * @param key key whose mapping is to be removed from the map.
    * @return previous the value to which the map previously associated the key. returned PreviousValue's
    * isPreviousValuePresent() returns <code>false</code> if the map contained no mapping for this key. This methods
    * cannot return <tt>null</tt>.
    * @throws ClassCastException            if the key is of an inappropriate type for this map (optional).
    * @throws NullPointerException          if the key is <tt>null</tt> and this map does not not permit <tt>null</tt>
    *                                       keys (optional).
    * @throws UnsupportedOperationException if the <tt>remove</tt> method is not supported by this map.
    */
   public PreviousValue remove(final Binary key) {

      try {

         final BinaryStoreElement element = elements.remove(key);
         if (element == null) {

            return new PreviousValue(null, false);
         } else {

            byteCounter.subtract(element.getSizeBytes());
            elementCounter.decrement();

            final Binary binaryValue = getValue(element);

            element.notifyModificationSubscribers(element, EntryModifiedEventType.REMOVE); // Self means 'remove'
            element.removeFromLRUList();
            removeFromDiskStorage(element);

            return new PreviousValue(binaryValue, true);
         }
      } catch (final StorageException e) {

         throw ExceptionUtils.createIllegalStateException(e);
      } catch (final IOException e) {

         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   /**
    * Removes entry for key only if currently mapped to given value. Acts as
    * <pre>
    * if ((map.containsKey(key) && map.get(key).equals(value)) {
    *    map.remove(key);
    *    return true;
    * } else {
    *    return false;
    * }
    *
    * @param binaryKey   key with which the specified value is associated.
    * @param binaryValue value associated with the specified key.
    * @return true if the value was removed, false otherwise.
    */
   public boolean remove(final Binary binaryKey, final Binary binaryValue) {

      try {
         final BinaryStoreElement element = elements.get(binaryKey);
         if (element == null) {
            return false;
         }

         if (!element.getValue().equals(binaryValue)) {
            return false;
         }

         // Remove
         elements.remove(binaryKey);

         // Update counters
         byteCounter.subtract(element.getSizeBytes());
         elementCounter.decrement();

         // Notify
         element.notifyModificationSubscribers(element, EntryModifiedEventType.REMOVE); // Self means 'remove'
         element.removeFromLRUList();

         // Remove from disk storage
         removeFromDiskStorage(element);

         return true;
      } catch (final StorageException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      } catch (final IOException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   public boolean replace(final Binary binaryKey, final Binary binaryOldValue, final Binary binaryNewValue) {

      try {

         final BinaryStoreElement element = elements.get(binaryKey);
         if (element == null) {
            return false;
         }

         if (!element.getValue().equals(binaryOldValue)) {
            return false;
         }

         put(binaryKey, binaryNewValue);

         return true;
      } catch (final StorageException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   /**
    * Replaces an entry for the key only if it is currently mapped to some value. Acts as
    * <pre>
    *    if ((store.containsKey(key)) {
    *       return store.put(key, value);
    *    } else {
    *       return null;
    *    }
    * </pre>
    *
    * @param binaryKey   a key with which the specified value is associated with.
    * @param binaryValue a value to be associated with the specified key.
    * @return a previous value associated with the specified key, or null if there was no mapping for the key. A null
    * can also indicate that the map previously associated null with the specified key, if the implementation supports
    * null values.
    */
   public PreviousValue replace(final Binary binaryKey, final Binary binaryValue) {

      if (containsKey(binaryKey)) {

         // Contains the key, update
         final Binary previousBinaryValue = put(binaryKey, binaryValue);
         return new PreviousValue(previousBinaryValue, true);
      } else {

         // Didn't have a key
         return new PreviousValue(null, false);
      }
   }


   /**
    * Removes an event subscriber.
    *
    * @param key                a key of interest.
    * @param subscriberIdentity a subscriber identity
    */
   public void removeEventSubscriber(final Binary key, final int subscriberIdentity) throws NotSubscribedException {

      try {

         boolean atLeastOneUnSubscribed = false;

         // Remove the subscriber from the internal Binary store list
         final List<BinaryEntryModifiedSubscriber> keySubscribers = updateSubscribers.get(key);
         for (final Iterator<BinaryEntryModifiedSubscriber> iterator = keySubscribers.iterator(); iterator.hasNext(); ) {

            final BinaryEntryModifiedSubscriber subscriber = iterator.next();
            if (subscriber.getIdentity() == subscriberIdentity) {

               iterator.remove();

               atLeastOneUnSubscribed = true;
            }
         }

         // Remove the keys from a cache element
         final BinaryStoreElement element = getElement(key);
         if (element != null) {

            // There is an element associated with the key, add a subscriber to it.
            element.removeEntryModifiedSubscriber(subscriberIdentity);
         }

         if (!atLeastOneUnSubscribed) {

            throw new NotSubscribedException();
         }
      } catch (final RuntimeException e) {

         throw e;
      } catch (final Exception e) {

         throw new CacheonixException(e);
      }
   }


   /**
    * Associates the specified value with the specified key in this map.  If the map previously contained a mapping for
    * this key, the old value is replaced by the specified value.  (A map <tt>m</tt> is said to contain a mapping for a
    * key <tt>k</tt> if and only if {@link #containsKey(Binary) m.containsKey(k)} would return <tt>true</tt>.))
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
   public Binary put(final Binary key, final Binary value) {

      return put(key, value, calculateExpirationTime(expirationInterval));
   }


   /**
    * {@inheritDoc}
    *
    * @noinspection ParameterHidesMemberVariable
    */
   public Binary put(final Binary key, final Binary value, final Time expirationTime) {

      try {

         final ReadableElement replacedElement = put(key, value, expirationTime, true, null);

         return getValue(replacedElement);

      } catch (final StorageException e) {

         throw new RuntimeStorageException(e);
      } catch (final IOException e) {

         throw new RuntimeIOException(e);
      }
   }


   /**
    * If the specified key is not already associated with a value, associate it with the given value. This is equivalent
    * to
    * <pre>
    *    if (!containsKey(key))
    *       return put(key, value);
    *    else
    *       return get(key);
    * </pre>
    * except that the action is performed atomically.
    *
    * @param key   a key with which the specified value is to be associated.
    * @param value a value to be associated with the specified key.
    * @return a previous value associated with the specified key, or null if there was no mapping for the key.
    * @throws NullPointerException if the specified key is null.
    */
   public Binary putIfAbsent(final Binary key, final Binary value) {

      try {

         final ReadableElement readableElement = get(key);
         if (readableElement != null) {

            // Bucket contains the key

            return readableElement.getValue();
         } else {

            // Bucket didn't contain the key, do put
            return put(key, value);
         }
      } catch (final InvalidObjectException e) {

         throw new CacheonixException(e);
      } catch (final StorageException e) {

         throw new CacheonixException(e);
      }
   }


   /**
    * Returns current cache statistics. The statistics is returned in a {@link CacheStatistics} object.
    *
    * @return current cache statistics.
    */
   public CacheStatistics getStatistics() {

      return statistics;
   }


   public boolean retainAll(final Set keys) {

      // Fast clean up for zero retention.
      if (keys.isEmpty()) {

         if (isEmpty()) {

            return false;
         } else {
            clear();
            return true;
         }
      }

      // Exception holder
      final IOException[] exception = new IOException[1];

      // Retain entries
      final boolean modified = elements.retainEntries(new ObjectObjectProcedure<Binary, BinaryStoreElement>() {

         public boolean execute(final Binary key, final BinaryStoreElement element) {

            if (keys.contains(key)) {

               // Retain key
               return true;
            } else {

               // Remove from LRU - retainEntries will remove the key
               byteCounter.subtract(element.getSizeBytes());
               elementCounter.decrement();
               element.removeFromLRUList();

               // Remove from the storage
               try {

                  removeFromDiskStorage(element);
               } catch (final IOException e) {

                  exception[0] = e;
               }

               return false;
            }
         }
      });

      // Throw exception if an error was encountered while processing
      if (exception[0] != null) {

         throw new RuntimeIOException(exception[0]);
      }

      // Return the result
      return modified;
   }


   /**
    * {@inheritDoc}
    */
   public void addEventSubscriber(final HashSet<Binary> keys, final BinaryEntryModifiedSubscriber subscriber) { // NOPMD

      // Parameter check
      if (keys.isEmpty()) {
         return;
      }

      // Add subscribers to the keys
      keys.forEach(new ObjectProcedure<Binary>() {

         public boolean execute(final Binary key) {

            addEventSubscriber(key, subscriber);

            // Continue
            return true;
         }
      });
   }


   /**
    * Adds a subscriber to entry modified events.
    * <p/>
    * This operation adds the subscriber to the current key entry and also remembers it for future setting up of an
    * entry for cases when it is added.
    *
    * @param key        a key of interest.
    * @param subscriber the subscriber to add.
    */
   public void addEventSubscriber(final Binary key, final BinaryEntryModifiedSubscriber subscriber) {

      // Get a list of subscriptions associated with a given key
      List<BinaryEntryModifiedSubscriber> subscriberList = updateSubscribers.get(key);
      if (subscriberList == null) {

         subscriberList = new ArrayList<BinaryEntryModifiedSubscriber>(1);
         updateSubscribers.put(key, subscriberList);
      }

      // Check if already subscribed
      for (final BinaryEntryModifiedSubscriber registeredSubscriber : subscriberList) {

         if (registeredSubscriber.getIdentity() == subscriber.getIdentity()) {

            return;
         }
      }

      // Not registered, add to store's list of subscribers for this key
      subscriberList.add(subscriber);

      // Add subscriber to an element
      try {
         final BinaryStoreElement element = getElement(key);
         if (element != null) {

            // There is an element associated with the key, add a subscriber to it.
            element.addEventSubscriber(subscriber);
         }
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheonixException(e);
      }
   }


   /**
    * @return number of elements evicted to disk.
    */
   public long getSizeOnDisk() {

      return statistics.getElementsOnDiskCount();
   }


   /**
    * {@inheritDoc}
    */
   public Time getExpirationInterval() {

      return expirationInterval;
   }


   /**
    * {@inheritDoc}
    */
   public Time getIdleInterval() {

      return idleInterval;
   }


   public void forEachElement(final BinaryStoreElementProcedure procedure) throws StorageException {

      final StorageException[] exception = new StorageException[1];

      elements.forEachValue(new ObjectProcedure<BinaryStoreElement>() {

         public boolean execute(final BinaryStoreElement element) {

            try {

               final Binary binaryKey = element.getKey();
               final Binary binaryValue = getValue(element);
               return procedure.processEntry(binaryKey, binaryValue);
            } catch (final StorageException e) {

               exception[0] = e;
               return false;
            }
         }
      });

      if (exception[0] != null) {
         throw exception[0];
      }
   }


   /**
    * Transfers the elements contained in this binary store to the receiver binary store. Modification listeners are not
    * not invoked.
    *
    * @param receiverStore the receiver store.
    */
   public void transferTo(final BinaryStore receiverStore) {

      // For each element, transfer to receiver and remove from source
      elements.retainEntries(new ObjectObjectProcedure<Binary, BinaryStoreElement>() {

         public boolean execute(final Binary binaryKey, final BinaryStoreElement element) {

            try {

               // Create a copy of the element
               final Time expirationTime = getExpirationTime(element);
               final Binary binaryValue = getValue(element);
               final Time idleTime = element.getIdleTime();
               final BinaryStoreElement newElement = new BinaryStoreElement(binaryKey, binaryValue, clock.currentTime(),
                       expirationTime, idleTime);
               newElement.setContext(binaryStoreElementContext);

               // Store if source element is stored
               if (element.isStored()) {

                  newElement.store();
                  receiverStore.statistics.incrementElementsOnDiskCount();
               }

               // Register element
               receiverStore.addToLRUList(newElement);
               receiverStore.elements.put(binaryKey, newElement);

               // Adjust counters
               receiverStore.byteCounter.add(newElement.getSizeBytes());
               receiverStore.elementCounter.increment();

               // Remove this element from the source
               removeElement(element);

            } catch (final IOException e) {

               throw new RuntimeIOException(e);
            } catch (final StorageException e) {

               throw new RuntimeStorageException(e);
            }

            // Note that we always return <code>false</code> because we
            // delete all elements in this (source) element store.
            return false;
         }
      });

      // Guard destination sizes
      try {

         // Evict eldest element if exceeded size
         receiverStore.guardElementCount();

         // Evict elements if exceeded byte size
         receiverStore.guardByteSize();

      } catch (final IOException e) {

         throw new RuntimeIOException(e);
      } catch (final StorageException e) {

         throw new RuntimeStorageException(e);
      }
   }


   /**
    * Returns an unexpired element.
    *
    * @param key
    * @return an unexpired element.
    */
   private BinaryStoreElement getElement(final Binary key) throws IOException, StorageException {

      final BinaryStoreElement element = elements.get(key);

      // Check if exists
      if (element == null) {

         return null;
      }

      // Check if expired
      if (element.isExpired(clock) || !element.isValid()) {

//         //noinspection ControlFlowStatementWithoutBraces
//         if (LOG.isDebugEnabled()) LOG.debug("Element expired: " + element.getExpirationTime()); // NOPMD
         removeElement(element, EntryModifiedEventType.EXPIRE);

         return null;
      }

      // Restore element if counters allow
      restoreElement(element);

      // Return result
      return element;
   }


   /**
    * Restores a possibly stored element if the byte counter allows and adjusts the byte counter.
    *
    * @param element the element to restore.
    * @throws StorageException
    * @throws IOException
    */
   private void restoreElement(final BinaryStoreElement element) throws StorageException, IOException {

      if (element == null) {
         return;
      }

      if (byteCounter.isUnlimitedSize()) {
         return;
      }

      if (byteCounter.value() >= byteCounter.getMaxValue()) {
         return;
      }

      if (!element.isStored()) {
         return;
      }

      // Restore element and adjust counters
      final long oldSizeBytes = element.getSizeBytes();
      final long newSizeBytes = element.load();
      final long sizeChangeBytes = newSizeBytes - oldSizeBytes;
      byteCounter.add(sizeChangeBytes);

      // Decrement statistics
      statistics.descrementCountOnDisk();
   }


   /**
    * If the element is stored in the disk, removes element from the disk storage.
    *
    * @param element
    * @throws IOException
    */
   private void removeFromDiskStorage(final BinaryStoreElement element) throws IOException {

      if (!element.isStored()) {
         return;
      }

      // Remove from storage
      element.discard();

      // Decrement count on disk
      statistics.descrementCountOnDisk();
   }


   private Time calculateIdleTime(final Time idleInterval) {

      if (idleInterval == null) {

         return null;

      } else {

         return clock.currentTime().add(idleInterval);
      }
   }


   public Time calculateExpirationTime(final Time expirationInterval) {

      if (expirationInterval == null) {

         return null;

      } else {

         return clock.currentTime().add(expirationInterval);
      }
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      // Write basic fields
      SerializerUtils.writeTime(expirationInterval, out);
      SerializerUtils.writeTime(idleInterval, out);

      // Write statistics
      statistics.writeWire(out);

      // Write header
      header.writeWire(out);

      // Write elements
      out.writeInt(elements.size());
      for (BinaryStoreElement element = header.getAfter(); !element.equals(header); element = element.getAfter()) {

         element.writeWire(out);
      }
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      // Read basic fields
      expirationInterval = SerializerUtils.readTime(in);
      idleInterval = SerializerUtils.readTime(in);

      // Read statistics
      statistics = new CacheStatisticsImpl();
      statistics.readWire(in);

      // Read header
      header = new BinaryStoreElement();
      header.readWire(in);
      header.setBefore(header);
      header.setAfter(header);

      // Read elements
      final int elementSize = in.readInt();
      elements = new HashMap<Binary, BinaryStoreElement>(elementSize);
      for (int i = 0; i < elementSize; i++) {

         // Read from wire
         final BinaryStoreElement element = new BinaryStoreElement();
         element.readWire(in);

         // Add to LRU list
         addToLRUList(element);

         // Add to elements
         elements.put(element.getKey(), element);
      }
   }


   public int getWireableType() {

      return TYPE_BINARY_STORE;
   }


   @SuppressWarnings("RedundantIfStatement")
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final BinaryStore that = (BinaryStore) o;

      if (elements != null ? !elements.equals(that.elements) : that.elements != null) {
         return false;
      }
      if (expirationInterval != null ? !expirationInterval.equals(
              that.expirationInterval) : that.expirationInterval != null) {
         return false;
      }
      if (idleInterval != null ? !idleInterval.equals(that.idleInterval) : that.idleInterval != null) {
         return false;
      }
      if (statistics != null ? !statistics.equals(that.statistics) : that.statistics != null) {
         return false;
      }
      return true;
   }


   public int hashCode() {

      int result = 0;
      result = 31 * result + (elements != null ? elements.hashCode() : 0);
      result = 31 * result + (statistics != null ? statistics.hashCode() : 0);
      result = 31 * result + (expirationInterval != null ? expirationInterval.hashCode() : 0);
      result = 31 * result + (idleInterval != null ? idleInterval.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "BinaryStore{" +
              "elements.size()=" + (elements == null ? "null" : Integer.toString(elements.size())) +
              ", header=" + header +
              ", statistics=" + statistics +
              ", expirationTimeMillis=" + expirationInterval +
              ", idleTimeMillis=" + idleInterval +
              ", maxSize=" + elementCounter +
              ", maxSizeBytes=" + byteCounter +
              ", updateListeners=" + updateSubscribers +
              ", binaryFactory=" + binaryFactory +
              '}';
   }


   public void setContext(final BinaryStoreContext binaryStoreContext) {

      // Set context
      this.binaryStoreContext = binaryStoreContext;

      // Create element context
      binaryStoreElementContext = new BinaryStoreElementContextImpl();
      binaryStoreElementContext.setObjectSizeCalculator(binaryStoreContext.getObjectSizeCalculator());
      binaryStoreElementContext.setDiskStorage(binaryStoreContext.getDiskStorage());
      binaryStoreElementContext.setInvalidator(binaryStoreContext.getInvalidator());

      // Set the invalidator in all elements
      elements.forEachValue(new ObjectProcedure<BinaryStoreElement>() {

         public boolean execute(final BinaryStoreElement binaryStoreElement) {

            binaryStoreElement.setContext(binaryStoreElementContext);

            return true;
         }
      });
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new BinaryStore();
      }
   }
}
