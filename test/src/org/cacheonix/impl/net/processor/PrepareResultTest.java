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
package org.cacheonix.impl.net.processor;

import junit.framework.TestCase;

/**
 * Tester for PrepareResult.
 */
public final class PrepareResultTest extends TestCase {

   public void testEquals() throws Exception {

      assertEquals(PrepareResult.BREAK, PrepareResult.BREAK);
      assertEquals(PrepareResult.EXECUTE, PrepareResult.EXECUTE);
      assertEquals(PrepareResult.ROUTE, PrepareResult.ROUTE);

      assertTrue(!PrepareResult.BREAK.equals(PrepareResult.EXECUTE));
      assertTrue(!PrepareResult.BREAK.equals(PrepareResult.ROUTE));
      assertTrue(!PrepareResult.EXECUTE.equals(PrepareResult.ROUTE));
   }


   public void testHashCode() throws Exception {

      assertEquals(PrepareResult.BREAK, PrepareResult.BREAK);
      assertEquals(PrepareResult.EXECUTE, PrepareResult.EXECUTE);
      assertEquals(PrepareResult.ROUTE, PrepareResult.ROUTE);

      assertTrue(PrepareResult.BREAK.hashCode() != 0);
      assertTrue(PrepareResult.EXECUTE.hashCode() != 0);
      assertTrue(PrepareResult.ROUTE.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(PrepareResult.BREAK.toString());
      assertNotNull(PrepareResult.EXECUTE.toString());
      assertNotNull(PrepareResult.ROUTE.toString());
   }
}
