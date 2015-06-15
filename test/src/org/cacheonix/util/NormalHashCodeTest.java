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
package org.cacheonix.util;

import junit.framework.TestCase;

/**
 * HashCode Tester.
 *
 * @author simeshev@cacheonix.com
 * @version 1.0
 * @since <pre>04/13/2008</pre>
 */
public final class NormalHashCodeTest extends TestCase {

   private HashCode hashCode;


   public void testToString() {

      assertNotNull(hashCode.toString());
   }


   public void testAdd() {

      hashCode.add(0);
      hashCode.add(1);
      hashCode.add(2);
      hashCode.add(3);
      hashCode.add(4);
      assertEquals(-1158159973, hashCode.getValue());
   }


   public void testThrowsExceptionIfLocked() {

      hashCode.add(0);
      hashCode.getValue();
      try {
         hashCode.add(1);
         fail("Expected exception but it was not thrown");
      } catch (final HashCodeLockedException ignored) { // NOPMD
      }
   }


   public void testDistrbution() {

      int geZero = 0;
      int ltZero = 0;
      for (int i = 0; i < 1000; i++) {
         final HashCode hashCode = new HashCode(HashCodeType.NORMAL);
         hashCode.add(new Object());
         if (hashCode.getValue() >= 0) {
            geZero++;
         } else {
            ltZero++;
         }
      }
      assertTrue(geZero > 0);
      assertTrue(ltZero > 0);
   }


   protected void setUp() throws Exception {

      super.setUp();
      hashCode = new HashCode(HashCodeType.NORMAL);
   }


   public String toString() {

      return "NormalHashCodeTest{" +
              "hashCode=" + hashCode +
              "} " + super.toString();
   }
}
