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
package org.cacheonix.impl.cache.invalidator;

import java.util.Properties;

import junit.framework.TestCase;
import org.cacheonix.TestConstants;

/**
 * CacheInvalidatorContextImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>07/30/2008</pre>
 */
public final class CacheInvalidatorContextImplTest extends TestCase {

   private static final String TEST_CACHE_NAME = TestConstants.LOCAL_TEST_CACHE;

   private CacheInvalidatorContextImpl context = null;

   private Properties properties = null;


   public void testGetCacheName() {

      assertEquals(TEST_CACHE_NAME, context.getCacheName());
   }


   public void testGetProperties() {

      assertEquals(properties, context.getProperties());
   }


   public void testToString() {

      assertNotNull(context.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      properties = new Properties();
      properties.setProperty("test.property", "test.value");
      context = new CacheInvalidatorContextImpl(TEST_CACHE_NAME, properties);
   }


   public String toString() {

      return "CacheInvalidatorContextImplTest{" +
              "context=" + context +
              ", properties=" + properties +
              "} " + super.toString();
   }
}
