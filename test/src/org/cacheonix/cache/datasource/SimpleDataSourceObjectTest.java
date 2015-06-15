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

import org.cacheonix.CacheonixTestCase;

/**
 * Tester for SimpleDataSourceObject.
 */
public final class SimpleDataSourceObjectTest extends CacheonixTestCase {

   private static final String OBJECT_2 = createTestObject(2);

   private SimpleDataSourceObject dataSourceObject;

   private static final String OBJECT_1 = createTestObject(1);


   public void testGetObject() throws Exception {

      assertEquals(OBJECT_1, dataSourceObject.getObject());
   }


   public void testEquals() throws Exception {

      assertEquals(dataSourceObject, new SimpleDataSourceObject(OBJECT_1));
      assertTrue(!new SimpleDataSourceObject(OBJECT_2).equals(dataSourceObject));
   }


   public void testHashCode() throws Exception {

      assertTrue(dataSourceObject.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(dataSourceObject.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      dataSourceObject = new SimpleDataSourceObject(OBJECT_1);
   }
}
