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
package org.cacheonix.impl.cache.local;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;

/**
 * A test for prefetch functionality for local cache.
 */
public final class LocalCachePrefetchTest extends CacheonixTestCase {


   private static final String CACHEONIX_CONFIG = "cacheonix-config-local-with-prefetch.xml";

   private static final String CACHE_NAME = "cache.with.prefetch";

   private Cache<String, String> cache;

   private Cacheonix cacheonix;


   /**
    * This test uses a {@link LocalCachePrefetchTestDataSource} that takes 50 ms to read the data while the expiration
    * time for the cache is 100 ms. The total read time is 500 ms, so the success means that there is a single read miss
    * (first one) that initiates the prefetch. After that the prefetch functionality keeps updating the cache in the
    * background.
    */
   public void testOnlyOneReadMiss() {

      final long begin = System.currentTimeMillis();
      final long end = begin + 500;
      while (System.currentTimeMillis() < end) {

         final String actualValue = cache.get("test");
         assertEquals("test", actualValue);
      }

      assertEquals(1, cache.getStatistics().getReadMissCount());
   }


   public void setUp() throws Exception {

      super.setUp();

      //
      cacheonix = Cacheonix.getInstance(TestUtils.getTestFile(CACHEONIX_CONFIG));

      //
      cache = cacheonix.getCache(CACHE_NAME);
   }


   public void tearDown() throws Exception {

      // Clear cache reference
      cache = null;

      // Explicitly shutdown Cacheonix
      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);

      //
      super.tearDown();
   }
}
