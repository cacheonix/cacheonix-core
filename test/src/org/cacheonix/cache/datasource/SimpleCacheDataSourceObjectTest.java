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
package org.cacheonix.cache.datasource;

import java.io.Serializable;

import junit.framework.TestCase;

/**
 * SimpleCacheDataSourceObject Tester.
 *
 * @author simeshev@cacheonix.com
 * @version 1.0
 * @since <pre>08/05/2008</pre>
 */
public final class SimpleCacheDataSourceObjectTest extends TestCase {

   private static final Serializable OBJECT = "object";

   private SimpleDataSourceObject cacheDataSourceObject = null;


   public void testGetObject() throws Exception {

      assertEquals(OBJECT, cacheDataSourceObject.getObject());
   }


   public void testToString() {

      assertNotNull(cacheDataSourceObject.toString());
   }


   public void testHashCode() {

      assertEquals(OBJECT.hashCode(), cacheDataSourceObject.getObject().hashCode());
   }


   public void testEquals() {

      assertEquals(cacheDataSourceObject, new SimpleDataSourceObject(OBJECT));
   }


   public void testEqualsSame() {

      assertTrue(cacheDataSourceObject.equals(cacheDataSourceObject));
   }


   public void testEqualsDifferentClass() {

      assertFalse(cacheDataSourceObject.equals(new Object()));
   }


   protected void setUp() throws Exception {

      super.setUp();
      cacheDataSourceObject = new SimpleDataSourceObject(OBJECT);
   }


   public String toString() {

      return "SimpleCacheDataSourceObjectTest{" +
              "cacheDataSourceObject=" + cacheDataSourceObject +
              "} " + super.toString();
   }
}
