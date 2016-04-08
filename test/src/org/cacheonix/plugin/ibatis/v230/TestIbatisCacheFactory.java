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
package org.cacheonix.plugin.ibatis.v230;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.ConfigurationException;
import org.cacheonix.impl.util.logging.Logger;

/**
 * TestIbatisCacheFactory
 * <p/>
 * Created on Mar 8, 2008 2:01:20 PM
 *
 * @author <a href="mailto:slava@cacheonix.org">Slava Imeshev</a>
 */
final class TestIbatisCacheFactory implements IBatisCacheFactory {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TestIbatisCacheFactory.class); // NOPMD

   private boolean getCacheCalled = false;


   public Cache getCache(final String configuration, final String cacheName) throws ConfigurationException {

      getCacheCalled = true;
      if (LOG.isDebugEnabled()) {
         LOG.debug("configuration: " + configuration);
      }
      if (LOG.isDebugEnabled()) {
         LOG.debug("cacheName: " + cacheName);
      }
      final Cache cache = Cacheonix.getInstance(configuration).getCache(cacheName);
      if (LOG.isDebugEnabled()) {
         LOG.debug("cache: " + cache);
      }
      return cache;
   }


   public boolean isGetCacheCalled() {

      return getCacheCalled;
   }


   public String toString() {

      return "TestIbatisCacheFactory{" +
              "getCacheCalled=" + getCacheCalled +
              '}';
   }
}


