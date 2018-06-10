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
package org.cacheonix.impl.util.hashcode;

import junit.framework.TestCase;

/**
 * HashCode Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>05/03/2008</pre>
 */
public final class HashCodeNormalTest extends TestCase {

   private HashCode hashCode = null;


   public void testAdd() {

      hashCode.add((byte) 1);
      hashCode.add(1);
      hashCode.add((long) 1);
      hashCode.add((short) 1);
      hashCode.add("string");
      hashCode.add(true);
      final int value = hashCode.getValue();
      assertEquals(-299334689, value);
      assertEquals(value, hashCode.hashCode());
   }


   public void testAddBooleanTrue() {

      hashCode.add(true);
      final int value = hashCode.getValue();
      assertEquals(16777619, value);
      assertEquals(value, hashCode.hashCode());
   }


   public void testAddBooleanFalse() {

      hashCode.add(false);
      final int value = hashCode.getValue();
      assertEquals(0, value);
      assertEquals(value, hashCode.hashCode());
   }


   public void testToString() {

      assertNotNull(hashCode.toString());
   }


   public void testHashCode() {

      assertEquals(0, hashCode.hashCode());
   }


   protected void setUp() throws Exception {

      super.setUp();
      hashCode = new HashCode(HashCodeType.NORMAL);
   }


   public String toString() {

      return "HashCodeNormalTest{" +
              "hashCode=" + hashCode +
              "} " + super.toString();
   }
}
