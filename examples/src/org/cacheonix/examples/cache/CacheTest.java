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
package org.cacheonix.examples.cache;

import org.cacheonix.Cacheonix;
import org.cacheonix.ShutdownMode;
import org.cacheonix.cache.Cache;
import junit.framework.TestCase;

/**
 * Tester for cache.
 */
public final class CacheTest extends TestCase {

   /**
    * MAX_SIZE is configured for {@link #TEST_CACHE} in the cacheonix-config.xml.
    */
   private static final int MAX_SIZE = 1000;

   /**
    * Test cache is configured in the cacheonix-config.xml.
    */
   private static final String TEST_CACHE = "local.test.cache";

   private static final String KEY = "key";

   private static final String VALUE = "value";

   private static final String VALUE_1 = "value1";

   private static final String VALUE_2 = "value2";

   private Cacheonix cacheonix;


   /**
    * Tests putting an object to the cache.
    */
   public void testPut() {

      final Cache<String, String> cache = cacheonix.getCache(TEST_CACHE);
      assertNotNull(cache);

      // Put an object to the cache
      final Object replacedValue1 = cache.put(KEY, VALUE_1);
      assertNull(replacedValue1);
      assertEquals(VALUE_1, cache.get(KEY));

      // Put another object using the same key
      final Object replacedValue2 = cache.put(KEY, VALUE_2);
      assertEquals(VALUE_1, replacedValue2);
      assertEquals(VALUE_2, cache.get(KEY));
   }


   /**
    * Tests putting an object to the cache evicts objects when number of unique puts exceeds maximum cache size.
    */
   public void testPutEvictsObjects() {

      final Cache<String, String> cache = cacheonix.getCache(TEST_CACHE);

      // Put objects to the cache with number objects exceeding maximum cache size
      for (int i = 0; i < MAX_SIZE << 1; i++) {
         final String index = Integer.toString(i);
         cache.put(KEY + index, VALUE + index);
      }

      // Assert that the number of elements in the cache didn't go above the cache size
      assertEquals(MAX_SIZE, cache.size());
   }


   /**
    * Sets up the fixture. This method is called before a test is executed.
    * <p/>
    * Cacheonix receives the default configuration from a <code>cacheonix-config.xml</code> found in a class path or
    * using a file that name is defined by system parameter <code>cacheonix.config.xml<code>.
    */
   protected void setUp() throws Exception {

      super.setUp();

      // Get Cacheonix using a default Cacheonix configuration. The configuration
      // is stored in the conf/cacheonix-config.xml
      cacheonix = Cacheonix.getInstance();
   }


   /**
    * Tears down the fixture. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      // Cache manager has be be shutdown upon application exit.
      // Note that call to shutdown() here uses unregisterSingleton
      // set to true. This is necessary to support clean restart on setUp()
      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      cacheonix = null;

      super.tearDown();
   }


   public String toString() {

      return "CacheTest{" +
              "cacheonix=" + cacheonix +
              '}';
   }
}
