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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import org.cacheonix.impl.cache.CacheonixCache;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tests clustered cache
 *
 * @noinspection ProhibitedExceptionDeclared, ProhibitedExceptionDeclared, ConstantNamingConvention,
 * ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention,
 * JUnitTestCaseWithNonTrivialConstructors
 */
public abstract class PartitionedCachePrefetchTestDriver extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PartitionedCachePrefetchTestDriver.class); // NOPMD


   private static final String CACHE_NAME = "partitioned.distributed.cache";


   private final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);


   /**
    * List of cache managers.
    */
   private final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);

   /**
    * List of clustered caches.
    */
   private final List<Cache<String, String>> cacheList = new ArrayList<Cache<String, String>>(5);


   /**
    * Cacheonix configurations, one per cluster.
    */
   private final String[] configurations;


   /**
    * Constructor.
    *
    * @param configurations configurations.
    */
   PartitionedCachePrefetchTestDriver(final String[] configurations) {

      this.configurations = ArrayUtils.copy(configurations);
   }


   /**
    * Constructor.
    *
    * @param testName       test name.
    * @param configurations configurations.
    */
   PartitionedCachePrefetchTestDriver(final String testName, final String[] configurations) {

      super(testName);

      this.configurations = ArrayUtils.copy(configurations);
   }


   /**
    * This test uses a {@link PartitionedCachePrefetchTestDriver} that takes 50 ms to read the data while the expiration
    * time for the cache is 100 ms. The total read time is 500 ms, so the success means that there is a single read miss
    * (first one) that initiates the prefetch. After that the prefetch functionality keeps updating the cache in the
    * background.
    */
   public void testOnlyOneReadMiss() {

      final Cache<String, String> cache = cache(1);
      final long begin = System.currentTimeMillis();
      final long end = begin + 500;
      while (System.currentTimeMillis() < end) {

         final String actualValue = cache.get("test");
         assertEquals("test", actualValue);
      }

      assertEquals(1, cache.getStatistics().getReadMissCount());
   }


   private CacheonixCache<String, String> cache(final int index) {

      return (CacheonixCache<String, String>) cacheList.get(index);
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      LOG.debug("================================================================================================");
      LOG.debug("========== Starting up =========================================================================");
      LOG.debug("================================================================================================");
      super.setUp();
      assertTrue("This test makes sense only for sizes bigger than 2", configurations.length >= 2);
      savedSystemProperty.save();
      System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
      for (int i = 0; i < configurations.length; i++) {

         final String configurationPath = TestUtils.getTestFile(configurations[i]).toString();
         final Cacheonix manager = Cacheonix.getInstance(configurationPath);
         cacheManagerList.add(manager);
         @SuppressWarnings("unchecked")
         final Cache<String, String> cache = manager.getCache(CACHE_NAME);
         assertNotNull("Cache " + i + " should be not null", cache);
         cacheList.add(cache);
      }

      // Wait for cluster to form
      waitForClusterToForm(cacheManagerList);

      LOG.debug("================================================================================================");
      LOG.debug("========== Started up =========================================================================");
      LOG.debug("================================================================================================");
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      LOG.debug("================================================================================================");
      LOG.debug("========== Tearing down ========================================================================");
      LOG.debug("================================================================================================");

      for (int i = 0; i < configurations.length; i++) {

         final Cacheonix cacheonix = cacheManagerList.get(i);
         if (!cacheonix.isShutdown()) {
            cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         }
      }
      cacheList.clear();
      cacheManagerList.clear();
      savedSystemProperty.restore();
      super.tearDown();
      LOG.debug("================================================================================================");
      LOG.debug("========== Teared down =========================================================================");
      LOG.debug("================================================================================================");
   }


   public String toString() {

      return "CacheNodeTest{" +
              "cacheManagerList=" + cacheManagerList +
              ", cacheList=" + cacheList +
              '}';
   }
}
