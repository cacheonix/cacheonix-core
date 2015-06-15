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
package org.springmodules.cache.provider.cacheonix;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;
import org.cacheonix.cache.Cache;
import org.cacheonix.impl.util.logging.Logger;

public final class CacheManagerProxyTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheManagerProxyTest.class); // NOPMD


   private Cacheonix cacheonix;


   public void testGetInstance() throws Exception {

      final Object instance = CacheManagerProxy.getInstance();
      assertEquals(instance, cacheonix);
      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
   }


   public void testGetInstanceWithName() {

      final String configName = "stealth-cacheonix-config.xml";
      final Object instance = CacheManagerProxy.getInstance(configName);
      assertSame(instance, cacheonix);
      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
   }


   public void testGetCache() {

      final Object instance = CacheManagerProxy.getInstance();
      assertSame(instance, cacheonix);
      final String cacheName = "testCache";
      final Object cache = CacheManagerProxy.getCache(cacheonix, cacheName);
      final Cache cache2 = cacheonix.getCache(cacheName);
      assertSame(cache, cache2);
      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
   }


   public void testShutdownCacheManager() {

      final Object instance = CacheManagerProxy.getInstance();
      assertSame(instance, cacheonix);
      CacheManagerProxy.shutdowmCacheManager(instance);
      final Cacheonix cacheManager2 = Cacheonix.getInstance();
      assertNotSame(instance, cacheManager2);
      cacheManager2.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
   }


   protected void setUp() throws Exception {

      super.setUp();

      cacheonix = Cacheonix.getInstance();
   }


   protected void tearDown() throws Exception {

      // Cache manager has be be shutdown upon application exit.
      // Note that call to shutdown() here uses unregisterSingleton
      // set to true. This is necessary to support clean restart on setUp()
      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      cacheonix = null;

      //
      super.tearDown();
   }
}
