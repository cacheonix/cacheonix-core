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
package org.cacheonix.util;

import org.cacheonix.impl.util.ArrayUtils;
import junit.framework.TestCase;

/**
 * MulticastTester Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/03/2008</pre>
 */
public final class MulticastTesterTest extends TestCase {

   private MulticastTester tester;


   public void testToString() {

      assertNotNull(tester.toString());
   }


   public void testHashCode() {

      assertTrue(tester.hashCode() != 0);
   }


   /**
    *
    */
   public void testTest() {

      tester.test();
   }


   protected void setUp() throws Exception {

      super.setUp();
      tester = new MulticastTester(ArrayUtils.EMPTY_STRING_ARRAY);
   }


   public String toString() {

      return "MulticastTesterTest{" +
              "tester=" + tester +
              "} " + super.toString();
   }
}
