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
package org.cacheonix.cache;

import java.io.IOException;
import java.util.Collection;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.local.LocalCache;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.config.SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE;

/**
 * Tests Cacheonix.
 */
public final class ClusteredCacheManagerTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusteredCacheManagerTest.class); // NOPMD

   private static final String TEST_CACHE_DOES_NOT_EXIT = "test_cache_does_not_exist";

   private Cacheonix instance = null;


   public void testGetInstance() {

      assertNotNull(instance);
   }


   public void testGetInstanceFromFile() throws ConfigurationException, IOException {

      final Cacheonix instanceFromFile = Cacheonix.getInstance(TestUtils.getTestFile(TestConstants.CACHEONIX_CLUSTER_XML));
      final Collection caches = instanceFromFile.getCaches();
      assertEquals(1, caches.size());
      for (final Object cache : caches) {
         assertTrue(cache instanceof Cache);
      }
   }


   public void testGetCaches() {

      final Collection caches = instance.getCaches();
      assertEquals(1, caches.size());
      for (final Object cache : caches) {
         assertTrue(cache instanceof Cache);
      }
   }


   /**
    * Test getting a named local cache.
    */
   public void testGetNonExistingCache() {

      final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(NAME_CACHEONIX_AUTO_CREATE_CACHE);
      savedSystemProperty.save();
      try {

         System.setProperty(NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
         assertNull(instance.getCache(TEST_CACHE_DOES_NOT_EXIT));
      } finally {

         savedSystemProperty.restore();
      }
   }


   /**
    * Test getting a distributed cache.
    *
    * @noinspection InstanceofInterfaces
    */
   public void testGetDistributedCache() {

      final Cache cache = instance.getCache("distributed_test_cache");
      assertNotNull("Cache should be not null", cache);
      assertTrue("Cache should not be an instance of LocalCache interface", !(cache instanceof LocalCache));
   }


   public void testToString() {

      assertNotNull(instance.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      instance = Cacheonix.getInstance(TestConstants.CACHEONIX_CLUSTER_XML);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      instance.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      instance = null;
      super.tearDown();
   }


   public String toString() {

      return "ClusteredCacheManagerTest{" +
              "instance=" + instance +
              '}';
   }
}