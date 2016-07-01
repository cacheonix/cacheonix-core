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
 * Cacheonix offers a cache provider for PrimeFaces, a UI framework for Java EE. The PrimeFaces cache provider allows
 * developers to increase performance and scalability of their web applications by avoiding repeatedly generating
 * content that changes infrequently. Instead, Cacheonix serves the read-mostly content from a fast in-memory cache.
 * Switching between local and distributed cache configurations is done by simply changing Cacheonix' configuration
 * file. Cache provider for PrimeFaces is available beginning with Cacheonix 2.3.0
 * <p/>
 * <strong>Adding Cache to PrimeFaces Application</strong>
 * <p/>
 * <strong>First</strong>, add Cacheonix to your Maven project by adding the following code to the dependencies section
 * of your <i>pom.xml</i>:
 * <pre>
 * &lt;dependency&gt;
 *    &lt;groupId&gt;org.cacheonix&lt;/groupId&gt;
 *    &lt;artifactId&gt;cacheonix-core&lt;/artifactId&gt;
 *    &lt;version&gt;2.3.0&lt;/version&gt;
 * &lt;dependency&gt;
 * </pre>
 * Alternatively, you can add Cacheonix to your project manually by downloading Cacheonix jar directly from <a
 * href="http://downloads.cacheonix.org">Cacheonix downloads website</a>.
 * <p/>
 * <strong>Second</strong>, set up the cache provider in _web.xml_ by setting context parameter
 * <i>primefaces.CACHE_PROVIDER</i> to <i>org.cacheonix.plugin.primefaces.PrimefacesCacheProvider</i>:
 * <p/>
 * <pre>
 * &lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
 * &lt;web-app xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 * version="3.0"xmlns="http://java.sun.com/xml/ns/j2ee"&gt;
 *    &lt;display-name&gt;PrimeFaces Cache Provider Example&lt;/display-name&gt;
 *    &lt;context-param&gt;
 *       &lt;param-name&gt;primefaces.CACHE_PROVIDER&lt;/param-name&gt;
 *       &lt;param-value&gt;org.cacheonix.plugin.primefaces.PrimefacesCacheProvider&lt;/param-value&gt;
 *    &lt;/context-param&gt;
 *    &lt;servlet&gt;
 *       &lt;servlet-name&gt;FacesServlet&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;javax.faces.webapp.FacesServlet&lt;/servlet-class&gt;
 *       &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *    &lt;/servlet&gt;
 *    &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;FacesServlet&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/faces/*&lt;/url-pattern&gt;
 *    &lt;/servlet-mapping&gt;
 *    &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;FacesServlet&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;*.xhtml&lt;/url-pattern&gt;
 *    &lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;
 * </pre>
 * <p/>
 * <strong>Third</strong>, enable caching a segment of your PrimeFaces code by wrapping the PrimeFaces code in the
 * &lt;cache&gt; tag. It's important that the key attribute identifies the cached segment of code uniquely:
 * <p/>
 * <pre>
 * &lt;h:form&gt;
 *    &lt;p:cache region="primefaces" key="toolbar"&gt;
 *       &lt;p:toolbar&gt;
 *       &lt;p:toolbarGroup align="left"&gt;
 *       &lt;p:commandButton type="button" value="Save"/&gt;
 *       &lt;p:commandButton type="button" value="Cancel"/&gt;
 *       &lt;/p:toolbarGroup&gt;
 *       &lt;/p:toolbar&gt;
 *       &lt;/p:cache&gt;
 * &lt;/h:form&gt;
 * </pre>
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