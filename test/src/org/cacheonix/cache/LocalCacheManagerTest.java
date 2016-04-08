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
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.config.SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE;

/**
 * Tests Cacheonix.
 */
public final class LocalCacheManagerTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalCacheManagerTest.class); // NOPMD

   private static final String TEST_LOCAL_CACHE = "local.test.cache";

   private static final String TEST_CACHE_DOES_NOT_EXIT = "test_cache_does_not_exist";

   private Cacheonix instance = null;

   private static final String NEW_TEST_CACHE = "new.test.cache";


   public void testGetInstance() {

      assertNotNull(instance);
   }


   public void testGetInstanceFromFile() throws ConfigurationException,
           IOException {

      final Cacheonix instanceFromFile = Cacheonix.getInstance(TestUtils.getTestFile(TestConstants.CACHEONIX_LOCAL_XML));
      final Collection caches = instanceFromFile.getCaches();
      assertEquals(6, caches.size());
      for (final Object cache : caches) {
         assertTrue(cache instanceof Cache);
      }
      instanceFromFile.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
   }


   public void testGetCaches() {

      final Collection caches = instance.getCaches();
      assertEquals(6, caches.size());
      for (final Object cache : caches) {
         assertTrue(cache instanceof Cache);
      }
   }


   /**
    * Test getting a named local cache.
    */
   public void testGetLocalCache() {

      final Cache cache = instance.getCache(TEST_LOCAL_CACHE);
      assertNotNull("Cache should be not null", cache);
   }


   /**
    * Test getting a named local cache.
    */
   public void testGetNonExistingCache() {

      final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);
      savedSystemProperty.save();
      try {
         System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
         assertNull(instance.getCache(TEST_CACHE_DOES_NOT_EXIT));
      } finally {
         savedSystemProperty.restore();
      }
   }


   public void testToString() {

      assertNotNull(instance.toString());
   }


   public void testDeleteCache() {

      final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);
      savedSystemProperty.save();
      try {
         System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
         instance.deleteCache(TEST_LOCAL_CACHE);
         assertNull(instance.getCache(TEST_LOCAL_CACHE));
      } finally {
         savedSystemProperty.restore();
      }
   }


   public void testCreateCache() {

      final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(NAME_CACHEONIX_AUTO_CREATE_CACHE);
      savedSystemProperty.save();
      try {

         System.setProperty(NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
         instance.deleteCache(TEST_LOCAL_CACHE);
         assertNotNull(instance.createCache(TEST_LOCAL_CACHE));
         assertNull(instance.getCache(TEST_CACHE_DOES_NOT_EXIT));
      } finally {

         savedSystemProperty.restore();
      }
   }


   public void testCreateCacheUsingTemplate() {

      final Cache cache = instance.createCache(NEW_TEST_CACHE, "default-local");
      assertNotNull(cache);
      assertEquals(NEW_TEST_CACHE, cache.getName());
      assertEquals(20, cache.getMaxSize());
   }


   protected void setUp() throws Exception {

      super.setUp();
      instance = Cacheonix.getInstance(TestConstants.CACHEONIX_LOCAL_XML);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Tearing down");
      }
      instance.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      instance = null;

      super.tearDown();
   }


   public String toString() {

      return "LocalCacheonixTest{" +
              "instance=" + instance +
              '}';
   }
}
