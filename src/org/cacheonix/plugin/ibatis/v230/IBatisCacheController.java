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
package org.cacheonix.plugin.ibatis.v230;

import java.io.Serializable;
import java.util.Properties;

import org.cacheonix.cache.Cache;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;
import com.ibatis.sqlmap.engine.cache.CacheController;
import com.ibatis.sqlmap.engine.cache.CacheModel;

/**
 * Cacheonix implementation of iBatis' cache controller. <p/> <h2>Cache Controller Lifecycle</h2> <p/> <ol> <li>iBatis
 * creates IBatisCacheController by calling its default constructor {@link IBatisCacheController#IBatisCacheController()}
 * .</li> <li>iBatis calls {@link IBatisCacheController#configure(Properties)} </li> </ol> <p/> <h2>Configuring iBatis
 * Cache Controller</h2> <p>Visit Cacheonix documentation, section <a href="http://wiki.cacheonix.org/display/CCHNX20/Configuring+Distributed+iBatis+Cache">Configuring
 * Distributed iBatis Cache</a> for detailed information on configuring Cacheonix iBatis cache controller.</p>
 */
public final class IBatisCacheController implements CacheController {

   /**
    * Logger.
    */
   private static final Logger LOG = Logger.getLogger(IBatisCacheController.class);

   /**
    * Default cache factory is an <b>immutable, stateless</b> implementation of {@link IBatisCacheFactory}.
    */
   private static final IBatisCacheFactory DEFAULT_IBATIS_CACHE_FACTORY = new IBatisDefaultCacheFactory();

   /**
    * Defines a name of the cache that was configured in cacheonix-config.xml.
    */
   static final String PROPERTY_CACHEONIX_CACHE_NAME = "cacheonix.cache.name";

   /**
    * Defines a path to cacheonix-config.xml. It could be in CLASSPATH or it could be a file path.
    */
   static final String PROPERTY_CACHEONIX_CONFIGURATION = "cacheonix.configuration";

   /**
    * Instance of Cacheonix' {@link Cache} that IBatisCacheController delegates servicing cache requests.
    */
   private Cache cache = null;

   /**
    * A factory that is used to get a instance of Cacheonix Cache.
    *
    * @see #IBatisCacheController(IBatisCacheFactory)
    * @see #configure(Properties)
    */
   private final IBatisCacheFactory IBatisCacheFactory;


   /**
    * Default constructor. This constructor is called by iBatis.
    *
    * @noinspection ControlFlowStatementWithoutBraces
    */
   public IBatisCacheController() {

      this(DEFAULT_IBATIS_CACHE_FACTORY);
   }


   /**
    * Constructor. This constructor is made package-visible to support testing.
    *
    * @param IBatisCacheFactory to use to get an instance of {@link Cache}
    * @see #IBatisCacheController()
    * @see #configure(Properties)
    */
   IBatisCacheController(final IBatisCacheFactory IBatisCacheFactory) {

      if (LOG.isInfoEnabled()) {
         LOG.info("Creating " + getClass().getName());
      }
      this.IBatisCacheFactory = IBatisCacheFactory;
   }


   /**
    * Flush a cache model
    *
    * @param cacheModel - the model to flush
    */
   public void flush(final CacheModel cacheModel) {

      cache.clear();
   }


   /**
    * Get an object from a cache model
    *
    * @param cacheModel - the model
    * @param key        - the key to the object
    * @return the object if in the cache, or null(?)
    */
   public Object getObject(final CacheModel cacheModel, final Object key) {

      return cache.get(key);
   }


   /**
    * Remove an object from a cache model
    *
    * @param cacheModel - the model to remove the object from
    * @param key        - the key to the object
    * @return the removed object(?)
    */
   public Object removeObject(final CacheModel cacheModel, final Object key) {

      return cache.remove(key);
   }


   /**
    * Put an object into a cache model
    *
    * @param cacheModel - the model to add the object to
    * @param key        - the key to the object
    * @param object     - the object to add
    */
   public void putObject(final CacheModel cacheModel, final Object key, final Object object) {

      cache.put((Serializable) key, (Serializable) object);
   }


   /**
    * Configure a cache controller
    *
    * @param props - the properties object containing configuration information
    * @noinspection OverlyBroadCatchBlock, ProhibitedExceptionThrown
    */
   public void configure(final Properties props) {

      try {
         final String configuration = props.getProperty(PROPERTY_CACHEONIX_CONFIGURATION, "cacheonix-config.xml");
         final String cacheName = props.getProperty(PROPERTY_CACHEONIX_CACHE_NAME);
         cache = IBatisCacheFactory.getCache(configuration, cacheName);
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   /**
    * Returns cache size.
    *
    * @return cache size.
    */
   int size() {

      return cache.size();
   }


   public String toString() {

      return "IBatisCacheController{" +
              "cache=" + cache +
              ", IBatisCacheFactory=" + IBatisCacheFactory +
              '}';
   }
}
