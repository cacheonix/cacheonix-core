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
package org.cacheonix.impl.concept.time;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Checks the differential between two readings of the physical nanos clock.
 */
public final class NanosTimeTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(NanosTimeTest.class); // NOPMD


   /**
    * Note: Experiment shows that on  on MAC OS X  the tick size is 1000ns == 1mks.
    */
   public void testIncrement() {

      final long n1 = System.nanoTime();

      //noinspection StatementWithEmptyBody
      for (long i = 0; i < 100; i++) {
      }

      final long n2 = System.nanoTime();

      assertEquals("Experiment shows that on  on MAC OS X  the tick size is 1000ns == 1mks", 0, (n2 - n1) % 1000L);
      assertTrue(Long.toString(n2 - n1), n2 - n1 > 1000);
   }
}
