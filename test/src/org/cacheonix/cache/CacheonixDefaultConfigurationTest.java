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
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;

/**
 * Tests Cacheonix.
 */
public final class CacheonixDefaultConfigurationTest extends CacheonixTestCase {

   private Cacheonix instance = null;

   private static final String NEW_TEST_CACHE = "new_test_cache";


   public void testCreateCacheUsingDefault() {

      final Cache cache = instance.createCache(NEW_TEST_CACHE);
      assertNotNull(cache);
      assertEquals(NEW_TEST_CACHE, cache.getName());
      assertEquals(10, cache.getMaxSize());
   }


   public void testCreateCacheUsingTemplate() {

      final Cache cache = instance.createCache(NEW_TEST_CACHE, "default-local");
      assertNotNull(cache);
      assertEquals(NEW_TEST_CACHE, cache.getName());
      assertEquals(20, cache.getMaxSize());
   }


   protected void setUp() throws Exception {

      super.setUp();
      instance = Cacheonix.getInstance("cacheonix-config-CACHEONIX-45.xml");
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

      return "CacheonixDefaultConfigurationTest{" +
              "instance=" + instance +
              '}';
   }
}
