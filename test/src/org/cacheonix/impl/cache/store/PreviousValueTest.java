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
package org.cacheonix.impl.cache.store;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.cache.item.Binary;

/**
 * Tester for PreviousValue.
 */
public final class PreviousValueTest extends CacheonixTestCase {

   private static final String OBJECT = "Previous value";


   public void testGetValue() throws Exception {

      final Binary binaryValue = toBinary(OBJECT);
      final PreviousValue previousValue = new PreviousValue(binaryValue, true);
      assertEquals(binaryValue, previousValue.getValue());
   }


   public void testIsPreviousValuePresent() throws Exception {

      final Binary binaryValue = toBinary(OBJECT);
      final PreviousValue previousValue = new PreviousValue(binaryValue, true);
      assertTrue(previousValue.isPreviousValuePresent());
   }


   public void testToString() throws Exception {

      final Binary binaryValue = toBinary(OBJECT);
      final PreviousValue previousValue = new PreviousValue(binaryValue, true);
      assertNotNull(previousValue.toString());
   }


   public void testIsPreviousValueNotPresent() throws Exception {

      final PreviousValue previousValue = new PreviousValue(null, false);
      assertTrue(!previousValue.isPreviousValuePresent());
   }
}
