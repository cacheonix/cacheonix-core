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
package org.cacheonix.impl.cache.invalidator;

import java.util.Properties;

import org.cacheonix.cache.invalidator.CacheInvalidatorContext;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * {@inheritDoc}
 */
public final class CacheInvalidatorContextImpl implements CacheInvalidatorContext {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheInvalidatorContextImpl.class); // NOPMD

   private final Properties properties;

   private final String cacheName;


   public CacheInvalidatorContextImpl(final String cacheName, final Properties properties) {

      this.properties = CollectionUtils.copyProperties(properties);
      this.cacheName = cacheName;
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

      return "CacheInvalidatorContextImpl{" +
              "properties=" + properties +
              ", cacheName='" + cacheName + '\'' +
              '}';
   }
}
