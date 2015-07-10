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
package org.cacheonix.impl.util;

import junit.framework.TestCase;

/**
 * CyclicCountDown Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/03/2008</pre>
 */
public final class CyclicCountDownTest extends TestCase {

   private static final int COUNTER_3 = 3;

   private CyclicCountDown countDown;


   public void testDecrement() {

      assertEquals(2, countDown.decrement());
      assertEquals(1, countDown.decrement());
      assertEquals(0, countDown.decrement());
      assertEquals(2, countDown.decrement());
   }


   public void testDecrementZeroCount() {

      final CyclicCountDown zeroCountDown = new CyclicCountDown(0);
      assertEquals(0, zeroCountDown.decrement());
      assertEquals(0, zeroCountDown.decrement());
   }


   public void testDecrementOneCount() {

      final CyclicCountDown zeroCountDown = new CyclicCountDown(1);
      assertEquals(0, zeroCountDown.decrement());
      assertEquals(0, zeroCountDown.decrement());
   }


   public void testToString() {

      assertNotNull(countDown.toString());
   }


   public void testHashCode() {

      assertTrue(countDown.hashCode() > 0);
   }


   protected void setUp() throws Exception {

      super.setUp();
      countDown = new CyclicCountDown(COUNTER_3);
   }


   public String toString() {

      return "CyclicCountDownTest{" +
              "countDown=" + countDown +
              "} " + super.toString();
   }
}
