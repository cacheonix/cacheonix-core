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
package org.cacheonix.cache;

import org.cacheonix.Cacheonix;

/**
 * An exception that signals that an attempt to create the cache denoted by a specified name has failed.
 * <p/>
 * This exception will be thrown by the {@link Cacheonix#getInstance(String)}, when a cache with the specified name
 * already exists in the given configuration.
 *
 * @noinspection FieldNotUsedInToString, ClassWithoutToString
 */
public final class CacheExistsException extends ConfigurationException {

   private final String cacheName;

   private static final long serialVersionUID = 7905415279637428178L;


   /**
    * Constructs a new exception with the name of the cache.  The cause is not initialized, and may subsequently be
    * initialized by a call to {@link #initCause}.
    *
    * @param cacheName the name of the cache
    */
   public CacheExistsException(final String cacheName) {

      super(cacheName);
      this.cacheName = cacheName;
   }


   /**
    * Returns the name of the cache.
    *
    * @return the name of the cache.
    */
   public String getCacheName() {

      return cacheName;
   }
}
