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
package org.cacheonix.plugin.primefaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;
import org.primefaces.cache.CacheProvider;

/**
 * Cacheonix cache provider for <a href="http://primefaces.org/">Primefaces</a>.
 */
public final class PrimefacesCacheProvider implements CacheProvider {

   private static final Logger log = Logger.getLogger(PrimefacesCacheProvider.class.getName());

   /**
    * The initial capacity of the {@link #cacheRegistry}.
    */
   private static final int INITIAL_CACHE_REGISTRY_SIZE = 111;

   /**
    * A registry of the caches created throughout the lifetime of this cache provider. This registry is used to track
    * what caches to clear and to teardown.
    */
   private final ConcurrentHashMap<String, Map<String, Serializable>> cacheRegistry = new ConcurrentHashMap<String, Map<String, Serializable>>(
           INITIAL_CACHE_REGISTRY_SIZE);


   /**
    * Returns a cached object identified by a key or <code>null</code> if not found.
    *
    * @param region a region (cache) name.
    * @param key    the key.
    * @return object identified by a key or null if not found.
    */
   public Object get(final String region, final String key) {

      final Map<String, Serializable> cacheRegion = getCache(region);
      return cacheRegion.get(key);
   }


   /**
    * Puts an object into the cache.
    *
    * @param region a region (cache) name.
    * @param key    object's key.
    * @param object the object to cache.
    */
   public void put(final String region, final String key, final Object object) {

      final Map<String, Serializable> cache = getCache(region);
      if (object instanceof Serializable) {

         cache.put(key, (Serializable) object);
      } else if (object != null) {

         log.warning("Cacheonix doesn't support caching objects that don't implement java.io.Serializable: "
                 + object.getClass().getName());
      }
   }


   /**
    * Removes an object from the cache.
    *
    * @param region a region (cache) name.
    * @param key    object's key.
    */
   public void remove(final String region, final String key) {

      final Map<String, Serializable> cache = getCache(region);
      cache.remove(key);
   }


   /**
    * Clears all cache regions.
    */
   public void clear() {

      final Collection<Map<String, Serializable>> caches = cacheRegistry.values();
      for (final Map<String, Serializable> cache : caches) {

         cache.clear();
      }
   }


   /**
    * Return an existing cache instance or creates a new cache.
    *
    * @param cacheName the name of the cache.
    * @return a cache with name <code>cacheName</code>
    */
   private Map<String, Serializable> getCache(final String cacheName) {

      final Map<String, Serializable> existingCache = cacheRegistry.get(cacheName);
      if (existingCache != null) {
         return existingCache;
      }

      final Cacheonix cacheonix = getCacheonix();
      final Cache<String, Serializable> newCache = cacheonix.getCache(cacheName);
      cacheRegistry.putIfAbsent(cacheName, newCache);
      return newCache;
   }


   /**
    * Deletes all caches allocated throughout the lifetime of this PrimefacesCacheProvider.
    */
   void tearDown() {

      final Cacheonix cacheonix = getCacheonix();
      final Set<String> cacheNames = cacheRegistry.keySet();
      for (final String cacheName : cacheNames) {

         cacheonix.deleteCache(cacheName);
      }
      cacheRegistry.clear();
   }


   /**
    * Returns Cacheonix instance. This method uses <code>cacheonix-config.xml</code> that must be present in the
    * classpath.
    *
    * @return a Cacheonix instance.
    */
   private static Cacheonix getCacheonix() {

      return Cacheonix.getInstance();
   }


   public String toString() {

      return "PrimefacesCacheProvider{" +
              "cacheRegistry=" + cacheRegistry +
              '}';
   }
}