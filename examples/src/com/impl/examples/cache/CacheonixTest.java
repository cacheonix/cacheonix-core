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
package com.impl.examples.cache;

import junit.framework.TestCase;
import org.cacheonix.Cacheonix;
import org.cacheonix.ShutdownMode;
import org.cacheonix.cache.Cache;
import org.cacheonix.impl.storage.disk.StorageException;

/**
 * Tester for Cacheonix.
 */
@SuppressWarnings("JavaDoc")
public final class CacheonixTest extends TestCase {

   private static final String NEW_LOCAL_TEST_CACHE = "new.local.test.cache";

   private static final String LOCAL_TEST_CACHE = "local.test.cache";

   private static final String NAMED_TEMPLATE = "named.template";

   private Cacheonix cacheonix;


   /**
    * Tests getting an instance of Cacheonix using a default Cacheonix configuration.
    */
   public void testGetInstance() {

      assertNotNull("Cacheonix created in setUp() method should not be null", cacheonix);
   }


   /**
    * Tests getting an instance of Cache. The configuration for the cache is defined in the cacheonix configuration
    * file.
    */
   public void testGetCache() {

      final Cache cache = cacheonix.getCache(LOCAL_TEST_CACHE);
      assertNotNull("Cache returned by cacheonix should be not null", cache);
      assertEquals(LOCAL_TEST_CACHE, cache.getName());
   }


   /**
    * Tests creating a Cache using a default template.
    */
   public void testCreateCacheFromDefaultTemplate() throws StorageException {

      final Cache cache = cacheonix.createCache(NEW_LOCAL_TEST_CACHE);
      assertNotNull("Cache returned by cacheonix should be not null", cache);
      assertEquals(NEW_LOCAL_TEST_CACHE, cache.getName());
   }


   /**
    * Tests creating a Cache using a named template.
    */
   public void testCreateCacheFromNamedTemplate() throws StorageException {

      final Cache cache = cacheonix.createCache(NEW_LOCAL_TEST_CACHE, NAMED_TEMPLATE);
      assertNotNull("Cache returned by cacheonix should be not null", cache);
      assertEquals(NEW_LOCAL_TEST_CACHE, cache.getName());
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

      return "CacheonixTest{" +
              "cacheonix=" + cacheonix +
              '}';
   }
}
