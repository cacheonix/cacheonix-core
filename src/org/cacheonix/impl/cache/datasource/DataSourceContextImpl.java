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
package org.cacheonix.impl.cache.datasource;

import java.util.Properties;

import org.cacheonix.cache.datasource.DataSourceContext;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * DataSourceContextImpl
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 30, 2008 12:13:48 AM
 */
public final class DataSourceContextImpl implements DataSourceContext {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DataSourceContextImpl.class); // NOPMD

   /**
    * The name of the cache that produced this context.
    */
   private final String cacheName;

   /**
    * Configuration properties.
    */
   private final Properties properties;


   /**
    * Creates cache data source context.
    *
    * @param cacheName  cache name
    * @param properties data source properties
    */
   public DataSourceContextImpl(final String cacheName, final Properties properties) {

      this.cacheName = cacheName;
      this.properties = CollectionUtils.copyProperties(properties);
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

      return CollectionUtils.copyProperties(properties);
   }


   public String toString() {

      return "DataSourceContextImpl{" +
              "cacheName='" + cacheName + '\'' +
              ", properties=" + properties +
              '}';
   }
}
