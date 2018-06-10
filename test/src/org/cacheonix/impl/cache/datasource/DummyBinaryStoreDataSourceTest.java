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

import java.util.ArrayList;
import java.util.Collection;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.cache.item.Binary;

/**
 * Tester for DummyBinaryStoreDataSource.
 */
public final class DummyBinaryStoreDataSourceTest extends CacheonixTestCase {


   private DummyBinaryStoreDataSource dummyBinaryStoreDataSource;


   public void testGet() {

      assertNull(dummyBinaryStoreDataSource.get(toBinary("key")));
   }


   public void testGetCollection() {

      final Collection<Binary> keys = new ArrayList<Binary>(2);
      keys.add(toBinary("key.0"));
      keys.add(toBinary("key.1"));

      final Collection<BinaryStoreDataSourceObject> objects = dummyBinaryStoreDataSource.get(keys);

      assertNotNull(objects);
      assertEquals(2, objects.size());

      for (final BinaryStoreDataSourceObject object : objects) {
         assertNull(object);
      }
   }


   public void testToString() {

      assertNotNull(dummyBinaryStoreDataSource.toString());
   }


   public void setUp() throws Exception {

      dummyBinaryStoreDataSource = new DummyBinaryStoreDataSource();

      super.setUp();
   }


   public void tearDown() throws Exception {

      dummyBinaryStoreDataSource = null;

      super.tearDown();
   }
}
