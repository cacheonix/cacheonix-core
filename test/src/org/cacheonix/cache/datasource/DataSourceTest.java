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
package org.cacheonix.cache.datasource;

import org.cacheonix.Cacheonix;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import junit.framework.TestCase;

/**
 * Tester for local cache data source.
 */
public final class DataSourceTest extends TestCase {

   /**
    * Cacheonix instance.
    */
   private Cacheonix cacheonix;

   /**
    * Cache backed by a data source.
    */
   private Cache<String, String> propertyCache;


   /**
    * Tests that a cache backed by the data source gets the key from the data source.
    */
   public void testGetsExistingProperty() {

      // Gets a property defined in the property file DataSourceTest.properties
      final String property = propertyCache.get("test.property.one");

      // Assert
      assertEquals("test.value.one", property);
   }


   /**
    * Tests that a cache backed by the data source does not gets the missing key from the data source.
    */
   public void testDoesntGetNonExistingProperty() {

      // Gets a property defined in the property file DataSourceTest.properties
      final String property = propertyCache.get("never.existed.property");

      // Assert
      assertNull(property);
   }


   public void setUp() throws Exception {

      super.setUp();

      // Start Cacheonix instance using "cacheonix-config-CacheDatasourceLocalCacheTest.xml"
      cacheonix = Cacheonix.getInstance(TestUtils.getTestFile("cacheonix-config-CacheDataSourceLocalCacheTest.xml"));

      // Get cache
      propertyCache = cacheonix.getCache("property.cache");
   }


   public void tearDown() throws Exception {

      propertyCache = null;

      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      cacheonix = null;

      super.tearDown();
   }
}
