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
package org.cacheonix.impl.cache.loader;

import java.util.Properties;

import junit.framework.TestCase;
import org.cacheonix.TestConstants;

/**
 * CacheLoaderContextImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>09/09/2008</pre>
 */
public final class CacheLoaderContextImplTest extends TestCase {

   private static final String CACHE_NAME = TestConstants.LOCAL_TEST_CACHE;

   private CacheLoaderContextImpl context = null;

   private Properties properties;


   public void testGetCacheName() {

      assertEquals(CACHE_NAME, context.getCacheName());
   }


   public void testGetProperties() {

      assertEquals(properties, context.getProperties());
      assertNotSame(properties, context.getProperties());
   }


   public void testToString() {

      assertNotNull(context.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      properties = new Properties();
      context = new CacheLoaderContextImpl(CACHE_NAME, properties);
   }


   public String toString() {

      return "CacheLoaderContextImplTest{" +
              "context=" + context +
              ", properties=" + properties +
              "} " + super.toString();
   }
}
