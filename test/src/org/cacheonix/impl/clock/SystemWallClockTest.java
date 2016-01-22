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
package org.cacheonix.impl.clock;

import org.cacheonix.CacheonixTestCase;

/**
 * Tester for SystemWallClock.
 */
public final class SystemWallClockTest extends CacheonixTestCase {

   private static final long TIMEOUT_MILLIS = 1000L;

   private SystemWallClock clock;


   public void testCurrentTimeMillis() throws Exception {

      // Check that it returns a meaningful value
      assertTrue(clock.currentTimeMillis() > 0);

      // Check that it's ticking
      final long timeoutTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
      final long initialReading = clock.currentTimeMillis();
      while (System.currentTimeMillis() < timeoutTime) {
         if (clock.currentTimeMillis() != initialReading) {

            // Clock ticked
            break;
         }
      }

      if (initialReading == clock.currentTimeMillis()) {
         fail("The clock didn't change for the period of " + TIMEOUT_MILLIS + "ms");
      }
   }


   public void setUp() throws Exception {

      super.setUp();

      clock = new SystemWallClock();
   }


   public void tearDown() throws Exception {

      clock = null;

      super.tearDown();
   }


   public String toString() {

      return "SystemWallClockTest{" +
              "} " + super.toString();
   }
}
