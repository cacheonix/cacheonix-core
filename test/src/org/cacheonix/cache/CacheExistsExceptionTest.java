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
package org.cacheonix.cache;

import junit.framework.TestCase;

/**
 * Tests CacheExistsException
 *
 * @noinspection ClassWithoutLogger
 */
public final class CacheExistsExceptionTest extends TestCase {

   private static final String TEST_CACHE_NAME = "test_cach_name";

   private CacheExistsException exception = null;


   /**
    * Tests constructor.
    */
   public void testCreate() {

      assertEquals(TEST_CACHE_NAME, exception.getCacheName());
   }


   /**
    * Sets up the fixture. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      exception = new CacheExistsException(TEST_CACHE_NAME);
   }


   public String toString() {

      return "CacheExistsExceptionTest{" +
              "shutdownHook=" + exception +
              '}';
   }
}
