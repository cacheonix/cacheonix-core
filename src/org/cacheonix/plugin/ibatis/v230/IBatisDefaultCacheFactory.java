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
package org.cacheonix.plugin.ibatis.v230;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Factory class to produce a cache instance. This implementation returns cache instance by its name in
 * cacheonix-config.xml.
 */
final class IBatisDefaultCacheFactory implements IBatisCacheFactory {

   private static final Logger LOG = Logger.getLogger(IBatisDefaultCacheFactory.class);


   /**
    * Returns cache instance by its name. This implementations returns cache instance by its name in
    * cacheonix-config.xml.
    *
    * @param configuration
    * @param cacheName     cache name
    * @return cache instance
    * @noinspection OverlyBroadCatchBlock, ProhibitedExceptionThrown
    */
   public Cache getCache(final String configuration, final String cacheName) {

      LOG.info("Getting cache: " + cacheName + " from configuration: " + configuration);
      try {
         return Cacheonix.getInstance(configuration).getCache(cacheName);
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   public String toString() {

      return "IBatisDefaultCacheFactory{}";
   }
}
