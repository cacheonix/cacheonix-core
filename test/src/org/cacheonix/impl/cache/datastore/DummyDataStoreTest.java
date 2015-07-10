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
package org.cacheonix.impl.cache.datastore;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;

/**
 * DummyCacheDataStoreTester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>08/13/2008</pre>
 */
public final class DummyDataStoreTest extends CacheonixTestCase {

   private static final String KEY = "key";

   private static final String VALUE = "value";

   private DummyDataStore dataStore = null;


   public void testSetContext() throws Exception {
      // Just make sure nothing happens
      final Properties properties = new Properties();
      final DataStoreContextImpl context = new DataStoreContextImpl(TestConstants.LOCAL_TEST_CACHE, properties);
      dataStore.setContext(context);
      assertNotNull(dataStore.toString());
   }


   public void testToString() {

      assertNotNull(dataStore.toString());
   }


   public void testStoreSingle() {

      final Properties properties = new Properties();
      final DataStoreContextImpl context = new DataStoreContextImpl(TestConstants.LOCAL_TEST_CACHE, properties);
      final StorableImpl storable = new StorableImpl(toBinary(KEY), toBinary(VALUE));

      dataStore.setContext(context);
      dataStore.store(storable);
      assertNotNull(dataStore.toString());
   }


   public void testStoreCollection() {

      final Properties properties = new Properties();
      final DataStoreContextImpl context = new DataStoreContextImpl(TestConstants.LOCAL_TEST_CACHE, properties);
      final StorableImpl storable = new StorableImpl(toBinary(KEY), toBinary(VALUE));

      final List<StorableImpl> list = new ArrayList<StorableImpl>(1);
      list.add(storable);
      dataStore.setContext(context);
      dataStore.store(list);
      assertNotNull(dataStore.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      dataStore = new DummyDataStore();
   }


   public String toString() {

      return "DummyDataStoreTest{" +
              "dataStore=" + dataStore +
              "} " + super.toString();
   }
}
