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
package org.cacheonix.impl.cache.datasource;

import org.cacheonix.CacheonixTestCase;

/**
 * DummyBinaryStoreDataSource Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>07/30/2008</pre>
 */
public final class DummyDataSourceTest extends CacheonixTestCase {

   private DummyBinaryStoreDataSource dummyBinaryStoreDataSource = null;


   public void testGet() throws Exception {

      assertNull(dummyBinaryStoreDataSource.get(toBinary("key")));
   }


   public void testToString() {

      assertNotNull(dummyBinaryStoreDataSource.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      // To ensure it can be created through a default constructor.
      dummyBinaryStoreDataSource = DummyBinaryStoreDataSource.class.newInstance();
   }


   public String toString() {

      return "DummyDataSourceTest{" +
              "dataSource=" + dummyBinaryStoreDataSource +
              "} " + super.toString();
   }
}
