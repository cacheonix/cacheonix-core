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
package org.cacheonix.plugin.hibernate.v32;

import java.io.Serializable;
import java.util.Map;

import org.cacheonix.Cacheonix;
import org.cacheonix.locks.Lock;
import org.cacheonix.locks.ReadWriteLock;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

/**
 * Cacheonix plugin for Hibernate.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
@SuppressWarnings("unchecked")
public final class HibernateCacheonixCache implements Cache {

   private final Cacheonix cacheonix;

   private final org.cacheonix.cache.Cache delegate;

   private final int lockTimeoutMillis;

   private boolean destroyed = false;


   /**
    * Constructor.
    *
    * @param cacheonix         Cacheonix instance.
    * @param delegate          to be used by this implementation of Hibernate's abstraction for caches.
    * @param lockTimeoutMillis timeout in milliseconds for obtaining a lock.
    * @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   public HibernateCacheonixCache(final Cacheonix cacheonix, final org.cacheonix.cache.Cache delegate,
                                  final int lockTimeoutMillis) {

      this.cacheonix = cacheonix;
      this.delegate = delegate;
      this.lockTimeoutMillis = lockTimeoutMillis;
   }


   /**
    * Get an item from the cache
    *
    * @param key key to read.
    * @return the cached object or <tt>null</tt>
    * @throws CacheException
    */
   public Object read(final Object key) throws CacheException {

      return delegate.get(key);
   }


   /**
    * Get an item from the cache, in a non-transactional fashion.
    *
    * @param key key to get.
    * @return the cached object or <tt>null</tt>
    * @throws CacheException
    */
   public Object get(final Object key) throws CacheException {

      return delegate.get(key);
   }


   /**
    * Add an item to the cache, in a non-transactional fashion, with fail-fast semantics
    *
    * @param key   key to put.
    * @param value key value to put.
    * @throws CacheException
    */
   public void put(final Object key, final Object value) throws CacheException {

      delegate.put((Serializable) key, (Serializable) value);
   }


   /**
    * Add an item to the cache
    *
    * @param key   key to update.
    * @param value key value to update.
    * @throws CacheException
    */
   public void update(final Object key, final Object value) throws CacheException {

      delegate.put((Serializable) key, (Serializable) value);
   }


   /**
    * Remove an item from the cache
    */
   public void remove(final Object key) throws CacheException {

      delegate.remove(key);
   }


   /**
    * Clear the cache
    */
   public void clear() throws CacheException {

      delegate.clear();
   }


   /**
    * Clean up.
    */
   public void destroy() throws CacheException {

      cacheonix.deleteCache(getRegionName());
      destroyed = true;
   }


   /**
    * If this is a clustered cache, lock the item
    */
   @SuppressWarnings("LockAcquiredButNotSafelyReleased")
   public void lock(final Object key) throws CacheException {

      final ReadWriteLock readWriteLock = delegate.getReadWriteLock((Serializable) key);
      final Lock lock = readWriteLock.writeLock();
      lock.lock();
   }


   /**
    * If this is a clustered cache, unlock the item
    */
   public void unlock(final Object key) throws CacheException {

      final ReadWriteLock readWriteLock = delegate.getReadWriteLock((Serializable) key);
      final Lock lock = readWriteLock.writeLock();
      lock.unlock();
   }


   /**
    * Generate a timestamp
    */
   public long nextTimestamp() {
      // REVIEWME: simeshev@cacheonix.org - 2008-01-22 -
      // consider effect of this time stamping mechanism on
      // clustered caches.
      return Timestamper.next();
   }


   /**
    * Get a reasonable "lock timeout"
    */
   public int getTimeout() {

      return lockTimeoutMillis;
   }


   /**
    * Get the name of the cache region
    */
   public String getRegionName() {

      return delegate.getName();
   }


   /**
    * The number of bytes is this cache region currently consuming in memory.
    *
    * @return The number of bytes consumed by this region; -1 if unknown or unsupported.
    */
   public long getSizeInMemory() {

      return -1L;
   }


   /**
    * The count of entries currently contained in the regions in-memory store.
    *
    * @return The count of entries in memory; -1 if unknown or unsupported.
    */
   public long getElementCountInMemory() {

      return (long) delegate.size();
   }


   /**
    * The count of entries currently contained in the regions disk store.
    *
    * @return The count of entries on disk; -1 if unknown or unsupported.
    */
   public long getElementCountOnDisk() {

      return delegate.getSizeOnDisk();
   }


   /**
    * optional operation
    */
   public Map toMap() {

      return delegate;
   }


   public String toString() {

      return "HibernateCacheonixCache{" +
              "cacheonix=" + cacheonix +
              ", delegate=" + delegate +
              ", lockTimeoutMillis=" + lockTimeoutMillis +
              '}';
   }


   boolean isDestroyed() {

      return destroyed;
   }
}
