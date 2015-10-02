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
package org.cacheonix.impl.cache.datasource;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.clock.TimeImpl;

/**
 * Tester for BinaryStoreDataSourceObjectImpl.
 */
public class BinaryStoreDataSourceObjectImplTest extends CacheonixTestCase {


   private static final String TEST_OBJECT = "test.object";

   private BinaryStoreDataSourceObjectImpl binaryStoreDataSourceObject;

   private TimeImpl timeToRead;


   public void testGetObject() throws Exception {

      assertEquals(TEST_OBJECT, binaryStoreDataSourceObject.getObject());
   }


   public void testGetTimeToRead() throws Exception {

      assertEquals(timeToRead, binaryStoreDataSourceObject.getTimeToRead());
   }


   public void testToString() throws Exception {

      assertNotNull(binaryStoreDataSourceObject.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      timeToRead = new TimeImpl(10, 0);
      binaryStoreDataSourceObject = new BinaryStoreDataSourceObjectImpl(TEST_OBJECT, timeToRead);
   }


   public void tearDown() throws Exception {

      super.tearDown();

      timeToRead = null;
      binaryStoreDataSourceObject = null;
   }


   public String toString() {

      return "BinaryStoreDataSourceObjectImplTest{" +
              "binaryStoreDataSourceObject=" + binaryStoreDataSourceObject +
              ", timeToRead=" + timeToRead +
              "} " + super.toString();
   }
}
