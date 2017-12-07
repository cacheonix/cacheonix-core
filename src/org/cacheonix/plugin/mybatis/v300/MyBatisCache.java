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
package org.cacheonix.plugin.mybatis.v300;


import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

import org.cacheonix.Cacheonix;
import org.cacheonix.impl.plugin.mybatis.v300.DummyMyBatisCacheAdapter;
import org.cacheonix.impl.plugin.mybatis.v300.MyBatisCacheAdapter;
import org.cacheonix.impl.plugin.mybatis.v300.MyBatisCacheAdapterImpl;
import org.cacheonix.impl.plugin.mybatis.v300.MyBatisCacheKey;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.cache.CacheKey;

/**
 * Cacheonix implementation of MyBatis cache adapter.
 * <p/>
 * Cacheonix MyBatisCache offers unlimited scalability to applications using MyBatis by providing reliable distributed
 * data management, sharing and replicating of cached data in a cluster of servers connected by a high-speed local
 * network.
 * <p/>
 * Cacheonix MyBatisCache takes few minutes to set up and is very easy to configure. For detailed information on
 * configuring <a href="http://wiki.cacheonix.org/display/CCHNX20/Configuring+Distributed+MyBatis+Cache">distributed
 * MyBatis cache</a> including ready-to-use examples please visit <a href="http://wiki.cacheonix.org/display/CCHNX20/Configuring+Distributed+MyBatis+Cache">online
 * Cacheonix documentation</a>.
 */
@SuppressWarnings("RedundantIfStatement")
public final class MyBatisCache implements Cache {

   /**
    * Key description.
    */
   private static final String CACHE_KEY_DESCRIPTION = "Cache key";

   /**
    * Value description.
    */
   private static final String CACHE_VALUE_DESCRIPTION = "Cache value";

   /**
    * A dummy cache adapter to use when caching is disabled.
    */
   private static final DummyMyBatisCacheAdapter DUMMY_MY_BATIS_CACHE_ADAPTER = new DummyMyBatisCacheAdapter();

   /**
    * The cache id.
    */
   private final String id;

   /**
    * If true, the namespace-wide caching is enabled and a namespace-wide cache will be created. If false, the
    * namespace-wide caching is disabled and only per-statement caches are created. By default the property
    * 'enableNamespaceCaching' is set to true (enable a  namespace-wide cache). When accessing a namespace-wide cache,
    * Cacheonix will first search its configurations for a name that is the same as the namespace. If Cacheonix cannot
    * find the cache, it will create the namespace-wide cache using a template named 'default'.
    */
   private String enableNamespaceCaching = Boolean.TRUE.toString();

   /**
    * A flag indicating if the namespace updates and inserts must  invalidate per-select caches. If true, namespace
    * updates and inserts will invalidate per-select caches. If false, the updates and inserts won't invalidate
    * per-select caches. By default the property 'namespaceUpdatesInvalidateSelectCaches' is set to true (the inserts
    * and updates will invalidate per-select caches).
    */
   private String namespaceUpdatesInvalidateSelectCaches = Boolean.TRUE.toString();

   /**
    * A flag indicating if the per-select caching is enabled and per-select caches will be created. If false, the
    * per-select caching is disabled. By default 'enablePerSelectCaching' is set to false (disable per-select caching).
    * When accessing a select cache, Cacheonix will first search its configurations for a name that conforms the
    * following convention: the namespace plus '.' (dot) plus the select ID. If Cacheonix cannot find the proper select
    * cache, it will create the select cache using a template with a name provided by a property
    * 'selectCacheTemplateName'. If the template name is not set, and if the namespace-wide caching is enabled,
    * Cacheonix will cache the select results in the namespace-wide cache.
    */
   private String enablePerSelectCaching = Boolean.FALSE.toString();

   /**
    * An optional name of the Cacheonix template cache configuration to use to create select caches that are not
    * explicitly configured.
    */
   private String selectCacheTemplateName = null;


   /**
    * A map holding select caches, one per select statement ID.
    */
   private final Map<String, MyBatisCacheAdapter> selectCacheMap = new HashMap<String, MyBatisCacheAdapter>(11);

   /**
    * Cacheonix cache.
    */
   private final org.cacheonix.cache.Cache<Serializable, Serializable> namespaceCache;

   /**
    * A namespace cache adapter.
    */
   private MyBatisCacheAdapter namespaceCacheAdapter = null;

   /**
    * Read-write lock.
    */
   private final ReadWriteLock readWriteLock;

   /**
    * Cacheonix instance.
    */
   private final Cacheonix cacheonix;

   /**
    * // Lock to protect internal structures.
    */
   private final ReentrantLock lock = new ReentrantLock();


   /**
    * Creates a new MyBatisCache with the given cache ID.
    * <p/>
    * This constructor is required by MyBatis.
    *
    * @param id the ID of the cache.
    */
   public MyBatisCache(final String id) {

      this(id, Cacheonix.getInstance());
   }


   /**
    * Creates a new MyBatis cache using given ID and an instance of Cacheonix.
    *
    * @param id        the ID of the cache.
    * @param cacheonix the Cacheonix instance.
    */
   MyBatisCache(final String id, final Cacheonix cacheonix) {

      // Check if the cache ID is a valid ID
      if (StringUtils.isBlank(id)) {

         throw new IllegalArgumentException("Cache ID is not set");
      }

      // Remember the ID
      this.id = id;

      // Set cacheonix references
      this.cacheonix = cacheonix;

      // Get cache
      namespaceCache = cacheonix.getCache(id);

      // Get the lock
      readWriteLock = namespaceCache.getReadWriteLock();
   }


   /**
    * Removes all mappings from this cache.
    */
   public void clear() {

      getNamespaceCacheAdapter().clear();

      if (isNamespaceUpdatesInvalidateSelectCaches()) {

         final Map<String, MyBatisCacheAdapter> map = copySelectCacheAdapters();
         for (final MyBatisCacheAdapter myBatisCacheAdapter : map.values()) {
            myBatisCacheAdapter.clear();
         }
      }
   }


   private Map<String, MyBatisCacheAdapter> copySelectCacheAdapters() {

      lock.lock();
      try {

         return new HashMap<String, MyBatisCacheAdapter>(selectCacheMap);
      } finally {
         lock.unlock();
      }
   }


   /**
    * Returns the cache ID.
    *
    * @return the cache ID.
    */
   public String getId() {

      return this.id;
   }


   /**
    * Returns the value to which this cache maps the specified key.  Returns <tt>null</tt> if the cache contains no
    * mapping for this key.  A return value of <tt>null</tt> does not <i>necessarily</i> indicate that the cache
    * contains no mapping for the key; it's also possible that the cache explicitly maps the key to <tt>null</tt>.  The
    * <tt>containsKey</tt> operation may be used to distinguish these two cases.
    * <p/>
    * <p>More formally, if this cache contains a mapping from a key <tt>k</tt> to a value <tt>v</tt> such that
    * <tt>(key==null ? k==null : key.equals(k))</tt>, then this method returns <tt>v</tt>; otherwise it returns
    * <tt>null</tt>.  (There can be at most one such mapping.)
    *
    * @param key key whose associated value is to be returned. The key must implement <code>java.io.Serializable</code>.
    * @return the value to which this cache maps the specified key, or <tt>null</tt> if the cache contains no mapping
    *         for this key.
    * @throws NullPointerException key is <tt>null</tt> and this cache does not permit <tt>null</tt> keys (optional).
    * @throws CacheException       if an error occured while getting the object.
    */
   public Object getObject(final Object key) {

      if (key instanceof CacheKey) {

         // Get
         final MyBatisCacheKey myBatisCacheKey = new MyBatisCacheKey((CacheKey) key);
         final MyBatisCacheAdapter cacheAdapter = getCacheAdapter(myBatisCacheKey.getSelectID());
         return cacheAdapter.get(myBatisCacheKey);

      } else {


         // Cast to serializable
         final Serializable serializableKey = castToSerializable(key, "Cache key");

         // Get
         final MyBatisCacheAdapter cacheAdapter = getCacheAdapter(null);
         return cacheAdapter.get(serializableKey);

      }
   }


   /**
    * Returns read-write lock associated with this cache.
    */
   public ReadWriteLock getReadWriteLock() {

      return readWriteLock;
   }


   /**
    * Returns the number of key-value mappings in this cache.  If the cache contains more than
    * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
    *
    * @return the number of key-value mappings in this cache.
    */
   public int getSize() {

      final MyBatisCacheAdapter namespaceCacheAdapter = getNamespaceCacheAdapter();
      final Map<String, MyBatisCacheAdapter> stringMyBatisCacheAdapterMap = copySelectCacheAdapters();
      int size = namespaceCacheAdapter.size();
      for (final MyBatisCacheAdapter myBatisCacheAdapter : stringMyBatisCacheAdapterMap.values()) {
         size += myBatisCacheAdapter.size();
      }
      return size;
   }


   /**
    * Associates the specified value with the specified key in this cache.  If the cache previously contained a mapping
    * for this key, the old value is replaced by the specified value.
    *
    * @param key   key with which the specified value is to be associated. The key must implement
    *              <code>java.io.Serializable</code>.
    * @param value value to be associated with the specified key. The value must implement
    *              <code>java.io.Serializable</code>.
    * @throws CacheException if an error occured while putting the key to the cache.
    */
   public void putObject(final Object key, final Object value) throws CacheException {

      final Serializable serializableValue = castToSerializable(value, CACHE_VALUE_DESCRIPTION);

      if (key instanceof CacheKey) {

         final MyBatisCacheKey myBatisCacheKey = new MyBatisCacheKey((CacheKey) key);
         final MyBatisCacheAdapter cacheAdapter = getCacheAdapter(myBatisCacheKey.getSelectID());
         cacheAdapter.put(myBatisCacheKey, serializableValue);
      } else {

         final Serializable serializableKey = castToSerializable(key, CACHE_KEY_DESCRIPTION);
         final MyBatisCacheAdapter cacheAdapter = getCacheAdapter(null);
         cacheAdapter.put(serializableKey, serializableValue);
      }
   }


   /**
    * Removes the mapping for this key from this cache if it is present.   More formally, if this cache contains a
    * mapping from key <tt>k</tt> to value <tt>v</tt> such that <code>(key==null ? k==null : key.equals(k))</code>, that
    * mapping is removed.  (The cache can contain at most one such mapping.)
    * <p/>
    * <p>Returns the value to which the cache previously associated the key, or <tt>null</tt> if the cache contained no
    * mapping for this key.  (A <tt>null</tt> return can also indicate that the cache previously associated
    * <tt>null</tt> with the specified key if the implementation supports <tt>null</tt> values.)  The cache will not
    * contain a mapping for the specified  key once the call returns.
    *
    * @param key key whose mapping is to be removed from the cache. The key must implement
    *            <code>java.io.Serializable</code>.
    * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
    * @throws CacheException if an error occured while removing the object from the cache.
    */
   public Object removeObject(final Object key) {

      if (key instanceof CacheKey) {

         // Remove object from the cache.
         final MyBatisCacheKey myBatisCacheKey = new MyBatisCacheKey((CacheKey) key);
         final MyBatisCacheAdapter cacheAdapter = getCacheAdapter(myBatisCacheKey.getSelectID());
         return cacheAdapter.remove(myBatisCacheKey);

      } else {

         // Cast to serializable cache key
         final Serializable serializableCacheKey = castToSerializable(key, CACHE_KEY_DESCRIPTION);

         // Remove object from the cache.
         final MyBatisCacheAdapter cacheAdapter = getCacheAdapter(null);
         return cacheAdapter.remove(serializableCacheKey);
      }
   }


   /**
    * Returns a flag indicating if the name space caching should be enabled.
    *
    * @return true if the namespace-wide caching is enabled and a namespace-wide cache will be created. If false, the
    *         namespace-wide caching is disabled and only per-statement caches are created. When accessing a
    *         namespace-wide cache, Cacheonix will first search its configurations for a name that is the same as the
    *         namespace. If Cacheonix cannot find the cache, it will create the cache using a template with the name
    *         provided by an optional property 'namespaceCacheTemplateName'. Otherwise Cacheonix will create the
    *         namespace-wide cache using a template named 'default'.
    */
   public String getEnableNamespaceCaching() {

      return enableNamespaceCaching;
   }


   /**
    * Sets the flag indicating if the name space caching should be enabled.
    *
    * @param enableNamespaceCaching the a flag indicating if the name space caching should be enabled. If true, the
    *                               namespace-wide caching is enabled and a namespace-wide cache will be created. If
    *                               false, the namespace-wide caching is disabled and only per-statement caches are
    *                               created. By default the property 'enableNamespaceCaching' is set to true (enable a
    *                               namespace-wide cache). When accessing a namespace-wide cache, Cacheonix will first
    *                               search its configurations for a name that is the same as the namespace. If Cacheonix
    *                               cannot find the cache, it will create the cache using a template with the name
    *                               provided by an optional property 'namespaceCacheTemplateName'. Otherwise Cacheonix
    *                               will create the namespace-wide cache using a template named 'default'.
    */
   public void setEnableNamespaceCaching(final String enableNamespaceCaching) {

      this.enableNamespaceCaching = enableNamespaceCaching;
   }


   /**
    * Returns the flag indicating if the namespace updates and inserts must invalidate per-select caches.
    *
    * @return the flag indicating if the namespace updates and inserts must  invalidate per-select caches. If true,
    *         namespace updates and inserts will invalidate per-select caches. If false, the updates and inserts won't
    *         invalidate per-select caches. By default the property 'namespaceUpdatesInvalidateSelectCaches' is set to
    *         true (the inserts and updates will invalidate per-select caches).
    */
   public String getNamespaceUpdatesInvalidateSelectCaches() {

      return namespaceUpdatesInvalidateSelectCaches;
   }


   /**
    * Sets the flag indicating if the namespace updates and inserts must invalidate per-select caches.
    *
    * @param namespaceUpdatesInvalidateSelectCaches
    *         the flag indicating if the namespace updates and inserts must  invalidate per-select caches. If true,
    *         namespace updates and inserts will invalidate per-select caches. If false, the updates and inserts won't
    *         invalidate per-select caches. By default the property 'namespaceUpdatesInvalidateSelectCaches' is set to
    *         true (the inserts and updates will invalidate per-select caches).
    */
   public void setNamespaceUpdatesInvalidateSelectCaches(final String namespaceUpdatesInvalidateSelectCaches) {

      this.namespaceUpdatesInvalidateSelectCaches = namespaceUpdatesInvalidateSelectCaches;
   }


   /**
    * Returns the flag indicating if the per-select caching is enabled and per-select caches will be created.
    *
    * @return the flag indicating if the per-select caching is enabled and per-select caches will be created. If false,
    *         the per-select caching is disabled. By default 'enablePerSelectCaching' is set to false (disable
    *         per-select caching). When accessing a select cache, Cacheonix will first search its configurations for a
    *         name that conforms the following convention: the namespace plus '.' (dot) plus the select ID. If Cacheonix
    *         cannot find the proper select cache, it will create the select cache using a template with a name provided
    *         by a property 'selectCacheTemplateName'. If the template name is not set, and if the namespace-wide
    *         caching is enabled, Cacheonix will cache the select results in the namespace-wide cache.
    */
   public String getEnablePerSelectCaching() {

      return enablePerSelectCaching;
   }


   /**
    * Sets the flag indicating if the per-select caching is enabled and per-select caches will be created.
    *
    * @param enablePerSelectCaching the flag indicating if the per-select caching is enabled and per-select caches will
    *                               be created. If false, the per-select caching is disabled. By default
    *                               'enablePerSelectCaching' is set to false (disable per-select caching). When
    *                               accessing a select cache, Cacheonix will first search its configurations for a name
    *                               that conforms the following convention: the namespace plus '.' (dot) plus the select
    *                               ID. If Cacheonix cannot find the proper select cache, it will create the select
    *                               cache using a template with a name provided by a property 'selectCacheTemplateName'.
    *                               If the template name is not set, and if the namespace-wide caching is enabled,
    *                               Cacheonix will cache the select results in the namespace-wide cache.
    */
   public void setEnablePerSelectCaching(final String enablePerSelectCaching) {

      this.enablePerSelectCaching = enablePerSelectCaching;
   }


   /**
    * Returns the optional name of the Cacheonix template cache configuration to use to create select caches that are
    * not explicitly configured.
    *
    * @return the optional name of the Cacheonix template cache configuration to use to create select caches that are
    *         not explicitly configured.
    */
   public String getSelectCacheTemplateName() {

      return selectCacheTemplateName;
   }


   /**
    * Sets he optional name of the Cacheonix template cache configuration to use to create select caches that are not
    * explicitly configured.
    *
    * @param selectCacheTemplateName the optional name of the Cacheonix template cache configuration to use to create
    *                                select caches that are not explicitly configured.
    */
   public void setSelectCacheTemplateName(final String selectCacheTemplateName) {

      this.selectCacheTemplateName = selectCacheTemplateName;
   }


   /**
    * Indicates whether some other object is "equal to" this one.
    *
    * @param obj the reference object with which to compare.
    * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
    */
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || !obj.getClass().equals(getClass())) {
         return false;
      }

      final MyBatisCache that = (MyBatisCache) obj;

      if (!id.equals(that.id)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return id.hashCode();
   }


   /**
    * Utility method.
    *
    * @param object      object to cast to Serializable.
    * @param description the object description.
    * @return a serializable object.
    * @throws CacheException if the object is not serializable
    */
   private static Serializable castToSerializable(final Object object, final String description) throws CacheException {

      if (object == null) {

         return null;
      }

      if (object instanceof Serializable) {

         return (Serializable) object;
      } else {

         throw new CacheException(description + " must implement java.io.Serializable or java.io.Externalizable: " + object);
      }
   }


   private MyBatisCacheAdapter getCacheAdapter(final String selectID) {

      // Cover null select
      if (selectID == null) {

         return getNamespaceCacheAdapter();
      }

      if (isPerSelectCachingEnabled()) {

         // Return per-select cache
         return getSelectCacheAdapter(selectID);
      } else {

         return getNamespaceCacheAdapter();
      }
   }


   private MyBatisCacheAdapter getSelectCacheAdapter(final String selectID) {

      lock.lock();
      try {

         final MyBatisCacheAdapter myBatisCacheAdapter = selectCacheMap.get(selectID);
         if (myBatisCacheAdapter == null) {

            if (cacheonix.cacheExists(selectID)) {

               return registerCacheAdapter(selectID, new MyBatisCacheAdapterImpl(cacheonix.getCache(selectID)));
            } else {

               if (StringUtils.isBlank(selectCacheTemplateName)) {

                  throw new IllegalArgumentException("Cacheonix could find configuration for cache '" + selectID + "' and property 'selectCacheTemplateName' is not set. Either configure the cache in cacheonix-config.xml or create a template.");
               } else {

                  final org.cacheonix.cache.Cache cache = cacheonix.createCache(selectID, selectCacheTemplateName);
                  return registerCacheAdapter(selectID, new MyBatisCacheAdapterImpl(cache));
               }
            }
         } else {

            // Returns existing
            return myBatisCacheAdapter;
         }
      } finally {

         lock.unlock();
      }
   }


   private MyBatisCacheAdapter registerCacheAdapter(final String selectID, final MyBatisCacheAdapter cacheAdapter) {

      selectCacheMap.put(selectID, cacheAdapter);
      return cacheAdapter;
   }


   private MyBatisCacheAdapter getNamespaceCacheAdapter() {


      lock.lock();
      try {

         if (namespaceCacheAdapter == null) {

            if (isNamespaceCachingEnabled()) {

               namespaceCacheAdapter = new MyBatisCacheAdapterImpl(namespaceCache);
            } else {

               namespaceCacheAdapter = DUMMY_MY_BATIS_CACHE_ADAPTER;
            }
         }

         return namespaceCacheAdapter;
      } finally {

         lock.unlock();
      }
   }


   private boolean isPerSelectCachingEnabled() {

      return Boolean.TRUE.toString().equals(enablePerSelectCaching);
   }


   private boolean isNamespaceCachingEnabled() {

      return Boolean.TRUE.toString().equals(enableNamespaceCaching);
   }


   private boolean isNamespaceUpdatesInvalidateSelectCaches() {

      return Boolean.TRUE.toString().equals(namespaceUpdatesInvalidateSelectCaches);
   }


   public String toString() {

      return "MyBatisCache{" +
              "id='" + id + '\'' +
              ", enableNamespaceCaching='" + enableNamespaceCaching + '\'' +
              ", namespaceUpdatesInvalidateSelectCaches='" + namespaceUpdatesInvalidateSelectCaches + '\'' +
              ", enablePerSelectCaching='" + enablePerSelectCaching + '\'' +
              ", selectCacheTemplateName='" + selectCacheTemplateName + '\'' +
              '}';
   }
}
