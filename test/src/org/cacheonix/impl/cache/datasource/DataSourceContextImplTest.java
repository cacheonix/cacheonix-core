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
package org.cacheonix.impl.cache.datasource;

import java.util.Properties;

import org.cacheonix.TestConstants;
import junit.framework.TestCase;

/**
 * DataSourceContextImpl Tester.
 *
 * @author simeshev@cacheonix.com
 * @version 1.0
 * @since <pre>07/30/2008</pre>
 */
public final class DataSourceContextImplTest extends TestCase {

   private static final String LOCAL_TEST_CACHE = TestConstants.LOCAL_TEST_CACHE;

   private Properties properties;

   private DataSourceContextImpl context = null;


   public void testGetCacheName() throws Exception {

      assertEquals(LOCAL_TEST_CACHE, context.getCacheName());
   }


   public void testGetProperties() throws Exception {

      assertEquals(properties, context.getProperties());
   }


   public void testToString() {

      assertNotNull(context.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      properties = new Properties();
      context = new DataSourceContextImpl(LOCAL_TEST_CACHE, properties);
   }


   public String toString() {

      return "DataSourceContextImplTest{" +
              "context=" + context +
              ", properties=" + properties +
              "} " + super.toString();
   }
}
