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
package org.cacheonix.impl.cache.datastore;

import org.cacheonix.CacheonixTestCase;

/**
 * StorableImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>08/13/2008</pre>
 */
public final class StorableImplTest extends CacheonixTestCase {

   private static final String KEY = "key";

   private static final String VALUE = "value";

   private StorableImpl storable = null;


   public void testGetValue() {

      assertEquals(storable.getValue(), VALUE);
   }


   public void testGetKey() {

      assertEquals(storable.getKey(), KEY);
   }


   public void testToString() {

      assertNotNull(storable.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      storable = new StorableImpl(toBinary(KEY), toBinary(VALUE));
   }


   public String toString() {

      return "StorableImplTest{" +
              "storable=" + storable +
              "} " + super.toString();
   }
}
