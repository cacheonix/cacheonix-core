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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.exceptions.CacheonixException;
import org.cacheonix.exceptions.RuntimeIOException;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSource;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriber;
import org.cacheonix.impl.cache.store.BinaryStore;
import org.cacheonix.impl.cache.store.PreviousValue;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.cache.store.SharedCounter;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.configuration.LeaseConfiguration;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.storage.disk.DiskStorage;
import org.cacheonix.impl.util.cache.ObjectSizeCalculator;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Bucket
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferenceInEquals, RedundantIfStatement, ParameterNameDiffersFromOverriddenParameter
 * @since Aug 3, 2009 10:28:35 PM
 */
public final class Bucket implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Bucket.class); // NOPMD

   private transient volatile boolean reconfiguring = false;

   private final transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   /**
    * Bucket number.
    */
   private int bucketNumber = 0;

   /**
    * The configuration parameter <code>lease duration</code> in millis.
    *
    * @see LeaseConfiguration#getLeaseTimeMillis()
    */
   private long leaseDurationMillis = 0L;

   /**
    * Expiration time of the read lease.
    */
   private Time leaseExpirationTime;

   /**
    * Key store.
    */
   private BinaryStore keyStore = null;


   /**
    * Required by Externalizable.
    */
   public Bucket() {

   }


   public Bucket(final int bucketNumber, final BinaryStore keyStore, final long leaseDurationMillis) {

      this.leaseDurationMillis = leaseDurationMillis;
      this.bucketNumber = bucketNumber;
      this.keyStore = keyStore;
   }


   /**
    * Adds to the <code>keyStore</code> a subscriber to entry modified events.
    * <p/>
    * This operation adds the subscriber to the current key entry and also remembers it for future setting up of an
    * entry for cases when it is added.
    *
    * @param key        a key of interest.
    * @param subscriber the subscriber to add.
    */
   public void addEventSubscriber(final Binary key, final BinaryEntryModifiedSubscriber subscriber) {

      keyStore.addEventSubscriber(key, subscriber);
   }


   /**
    * Returns value associated with the given key.
    *
    * @param key the key.
    * @return value associated with the given key.
    * @throws InvalidObjectException if an object cannot be retrieved.
    */
   public ReadableElement get(final Binary key) throws InvalidObjectException {

      return keyStore.get(key);
   }


   public int getBucketNumber() {

      return bucketNumber;
   }


   /**
    * Puts a key to this bucket.
    *
    * @param key            a key to put.
    * @param value          a value to put.
    * @param expirationTime expiration time. null means that the configuration expiration time will be used.
    * @return a previous value.
    */
   public Binary put(final Binary key, final Binary value, final Time expirationTime) {

      if (expirationTime == null) {

         return keyStore.put(key, value);
      } else {

         return keyStore.put(key, value, expirationTime);
      }
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
           final long expectedElementUpdateCounter) {

      try {

         return keyStore.update(key, value, timeToRead, expectedElementUpdateCounter);

      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheonixException(e);
      }
   }


   public boolean containsKey(final Binary key) {

      return keyStore.containsKey(key);
   }


   public boolean containsValue(final Binary value) {

      return keyStore.containsValue(value);
   }


   public PreviousValue remove(final Binary key) {

      return keyStore.remove(key);
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
    * @param key   key with which the specified value is associated.
    * @param value value associated with the specified key.
    * @return true if the value was removed, false otherwise.
    */
   public boolean remove(final Binary key, final Binary value) {

      return keyStore.remove(key, value);
   }


   /**
    * Replace entry for key only if currently mapped to given value. Acts as
    * <pre>
    *  if ((map.containsKey(key) && map.get(key).equals(oldValue)) {
    *     map.put(key, newValue);
    *     return true;
    * } else {
    *    return false;
    * }
    * </pre>
    * except that the action is performed atomically.
    *
    * @param key      key with which the specified value is associated.
    * @param oldValue value expected to be associated with the specified key.
    * @param newValue value to be associated with the specified key.
    * @return true if the value was replaced
    */
   public boolean replace(final Binary key, final Binary oldValue, final Binary newValue) {

      return keyStore.replace(key, oldValue, newValue);
   }


   /**
    * Replaces an entry for the key only if it is currently mapped to some value. Acts as
    * <pre>
    *    if ((map.containsKey(key)) {
    *       return map.put(key, value);
    *    } else {
    *       return null;
    *    }
    * </pre>
    * except that the action is performed atomically.
    *
    * @param key   a key with which the specified value is associated with.
    * @param value a value to be associated with the specified key.
    * @return a previous value associated with the specified key, or null if there was no mapping for the key. A null
    *         can also indicate that the map previously associated null with the specified key, if the implementation
    *         supports null values.
    */
   public PreviousValue replace(final Binary key, final Binary value) {

      return keyStore.replace(key, value);
   }


   public void removeEventSubscriber(final Binary key, final int subscriberIdentity) {

      keyStore.removeEventSubscriber(key, subscriberIdentity);
   }


   public int size() {

      return keyStore.size();
   }


   public Lock getWriteLock() {

      return lock.writeLock();
   }


   public boolean isReconfiguring() {

      return reconfiguring;
   }


   public void setReconfiguring(final boolean reconfiguring) {

      this.reconfiguring = reconfiguring;
   }


   public void clear() {

      keyStore.clear();
   }


   public Collection<Binary> values() {

      return keyStore.values();
   }


   public Collection<Binary> keySet() {

      return keyStore.keySet();
   }


   public Set<Map.Entry<Binary, Binary>> entrySet() {

      return keyStore.entrySet();
   }


   /**
    * Returns <code>true</code> if bucket's key store is empty.
    *
    * @return <code>true</code> if bucket's key store is empty.
    */
   public boolean isEmpty() {

      return keyStore.isEmpty();
   }


   public int getWireableType() {

      return TYPE_BUCKET;
   }


   public boolean retainAll(final Set<Binary> keys) {

      return keyStore.retainAll(keys);
   }


   public void putAll(final Map<Binary, Binary> map) {

      keyStore.putAll(map);
   }


   /**
    * Attaches this bucket to a shared byte counter. The counter value is adjusted according to the content of this
    * bucket.
    *
    * @param byteCounter the shared byte counter to attach to.
    */
   public void attachToByteCounter(final SharedCounter byteCounter) {

      keyStore.attachToByteCounter(byteCounter);
   }


   /**
    * Attaches this bucket to a shared element counter. The counter value is adjusted according to the content of this
    * bucket.
    *
    * @param elementCounter the element counter to attach to.
    */
   public void attachToElementCounter(final SharedCounter elementCounter) {

      keyStore.attachToElementCounter(elementCounter);
   }


   /**
    * Sets an invalidator. This method must be called immediately after de-serialization is complete.
    *
    * @param invalidator the invalidator to set.
    */
   public void setInvalidator(final CacheInvalidator invalidator) {

      keyStore.setInvalidator(invalidator);
   }


   /**
    * Sets a disk storage. This method must be called immediately after de-serialization is complete.
    *
    * @param diskStorage the disk storage to set.
    */
   public void setDiskStorage(final DiskStorage diskStorage) {

      keyStore.setDiskStorage(diskStorage);
   }


   /**
    * Sets an auxiliary, user-provided data source. This method must be called immediately after de-serialization is
    * complete.
    *
    * @param dataSource the data source to set.
    */
   public void setDataSource(final BinaryStoreDataSource dataSource) {

      keyStore.setDataSource(dataSource);
   }


   /**
    * Sets an auxiliary, user-provided data store. This method must be called immediately after de-serialization is
    * complete.
    *
    * @param dataStore the data store to set.
    */
   public void setDataStore(final DataStore dataStore) {

      keyStore.setDataStore(dataStore);
   }


   /**
    * Sets a mandatory object size calculator. This method must be called immediately after de-serialization is
    * complete.
    *
    * @param objectSizeCalculator the object size calculator to set.
    */
   public void setObjectSizeCalculator(final ObjectSizeCalculator objectSizeCalculator) {

      keyStore.setObjectSizeCalculator(objectSizeCalculator);
   }


   public void detachElementCounter() {

      keyStore.detachElementCounter();
   }


   public void detachByteCounter() {

      keyStore.detachByteCounter();
   }


   /**
    * Disconnects the bucket from a disk storage.
    * <p/>
    * This is a one time operation that is called before discarding the bucket in {@link
    * CacheProcessor#removeBucket(int, Integer)}.
    */
   public void detachDiskStorage() {

      keyStore.detachDiskStorage();
   }


   /**
    * Returns the configuration parameter <code>lease duration</code> in millis.
    *
    * @return the configuration parameter <code>lease duration</code> in millis.
    */
   public long getLeaseDurationMillis() {

      return leaseDurationMillis;
   }


   /**
    * Returns expiration time of the read lease.
    *
    * @return the expiration time of the read lease.
    */
   public Time getLeaseExpirationTime() {

      return leaseExpirationTime;
   }


   /**
    * Sets expiration time of the read lease.
    *
    * @param leaseExpirationTime the expiration time of the read lease.
    */
   public void setLeaseExpirationTime(final Time leaseExpirationTime) {

      this.leaseExpirationTime = leaseExpirationTime;
   }


   public CacheStatistics getStatistics() {

      return keyStore.getStatistics();
   }


   /**
    * Sets the synchronized cluster clock.
    *
    * @param clock the clock to set.
    */
   public void setClock(final Clock clock) {

      keyStore.setClock(clock);
   }


   /**
    * Transfers the elements contained in the bucket's binary store to the receiver bucket. Modification listeners are
    * not not invoked.
    *
    * @param receiverBucket the receiver bucket.
    */
   public void transferTo(final Bucket receiverBucket) {

      keyStore.transferTo(receiverBucket.keyStore);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      leaseExpirationTime = SerializerUtils.readTime(in);
      leaseDurationMillis = in.readLong();
      bucketNumber = in.readShort();
      keyStore = new BinaryStore();
      keyStore.readWire(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      SerializerUtils.writeTime(leaseExpirationTime, out);
      out.writeLong(leaseDurationMillis);
      out.writeShort(bucketNumber);
      keyStore.writeWire(out);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final Bucket bucket = (Bucket) o;

      if (bucketNumber != bucket.bucketNumber) {
         return false;
      }
      if (reconfiguring != bucket.reconfiguring) {
         return false;
      }
      if (keyStore != null ? !keyStore.equals(bucket.keyStore) : bucket.keyStore != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = (reconfiguring ? 1 : 0);
      result = 31 * result + bucketNumber;
      result = 31 * result + (keyStore != null ? keyStore.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "Bucket{" +
              "bucketNumber=" + bucketNumber +
              ", keyStore.size()=" + (keyStore == null ? "null" : keyStore.size()) +
              ", reconfiguring=" + reconfiguring +
              '}';
   }


   /**
    * Creates a disconnected copy of a bucket suitable for wire transfers.
    *
    * @return a disconnected copy of a bucket suitable for wire transfers.
    * @throws RuntimeIOException if I/O error occured while copying the bucket.
    */
   Bucket copy() throws RuntimeIOException {

      try {

         final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         final DataOutputStream daos = new DataOutputStream(baos);
         SerializerUtils.writeBucket(daos, this);
         daos.flush();

         final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
         final DataInputStream dis = new DataInputStream(bis);

         return SerializerUtils.readBucket(dis);
      } catch (final RuntimeException e) {

         throw e;
      } catch (final Exception e) {

         throw new RuntimeIOException(e);
      }
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new Bucket();
      }
   }
}
