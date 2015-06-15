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
package org.cacheonix.plugin.ibatis.v230;

import org.cacheonix.TestConstants;
import org.cacheonix.cache.Cache;
import junit.framework.TestCase;

/**
 * Tests IBatisDefaultCacheFactoryTest for iBatis.
 */
public final class IBatisDefaultCacheFactoryTest extends TestCase {

   private static final String CACHE_CONTROLLER_CACHEONIX_CONFIG_XML = TestConstants.CACHE_CONTROLLER_CACHEONIX_CONFIG_XML;

   private static final String TEST_IBATIS_CACHE = TestConstants.TEST_IBATIS_CACHE;

   private IBatisDefaultCacheFactory factoryIBatis;


   /**
    * Tests {@link IBatisDefaultCacheFactoryTest}.
    */
   public void testGetCache() {

      final Cache cache = factoryIBatis.getCache(CACHE_CONTROLLER_CACHEONIX_CONFIG_XML, TEST_IBATIS_CACHE);
      assertNotNull(cache);
      assertEquals(cache.getName(), TEST_IBATIS_CACHE);
   }


   /**
    * Tests {@link IBatisDefaultCacheFactoryTest}.
    */
   public void testToString() {

      assertNotNull(factoryIBatis.toString());
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      factoryIBatis = new IBatisDefaultCacheFactory();
   }


   public String toString() {

      return "IBatisDefaultCacheFactoryTest{" +
              "factoryIBatis=" + factoryIBatis +
              '}';
   }
}
