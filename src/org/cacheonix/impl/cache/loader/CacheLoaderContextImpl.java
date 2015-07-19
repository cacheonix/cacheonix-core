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
package org.cacheonix.impl.cache.loader;

import java.util.Properties;

import org.cacheonix.cache.loader.CacheLoaderContext;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.util.CollectionUtils.copyProperties;

/**
 * An implmenetation of CacheLoaderContext.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 11, 2008 10:31:21 PM
 */
public final class CacheLoaderContextImpl implements CacheLoaderContext {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheLoaderContextImpl.class); // NOPMD

   /**
    * Loader cache name.
    */
   private final String cacheName;


   /**
    * Loader properties.
    */
   private final Properties properties;


   /**
    * Creates CacheLoaderContextImpl.
    *
    * @param cacheName  cache name.
    * @param properties loader configuration properties.
    */
   public CacheLoaderContextImpl(final String cacheName, final Properties properties) {

      this.cacheName = cacheName;
      this.properties = copyProperties(properties);
   }


   /**
    * {@inheritDoc}
    */
   public String getCacheName() {

      return cacheName;
   }


   /**
    * {@inheritDoc}
    */
   public Properties getProperties() {

      return copyProperties(properties);
   }


   public String toString() {

      return "CacheLoaderContextImpl{" +
              "cacheName='" + cacheName + '\'' +
              ", properties=" + properties +
              '}';
   }
}
