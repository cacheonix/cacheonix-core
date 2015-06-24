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
package org.cacheonix.impl.clock;

import java.util.Timer;

import org.cacheonix.CacheonixTestCase;

/**
 * Tester for Clock.
 */
public final class ClockImplTest extends CacheonixTestCase {

   private ClockImpl clock;

   private Timer timer;


   @SuppressWarnings("SimplifiableJUnitAssertion")
   public void testCurrentTime() throws Exception {

      final Time time1 = clock.currentTime();
      final Time time2 = clock.currentTime();

      assertTrue(!time1.equals(time2));
      assertTrue(time2.compareTo(time1) > 0);
      assertTrue(time1.compareTo(time1) == 0);
      assertTrue(time1.compareTo(time2) <= 0);
   }


   public void testAdjust() throws Exception {

      final Time timeBeforeAdjustment = clock.currentTime();
      final Time newTime = timeBeforeAdjustment.add(1000);
      clock.adjust(newTime);

      assertTrue(clock.currentTime().compareTo(newTime) >= 0);
   }


   public void testToString() throws Exception {

      assertNotNull(clock.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      timer = new Timer("Test timer");
      clock = new ClockImpl(100L);
      clock.attachTo(timer);
   }


   public void tearDown() throws Exception {

      timer.cancel();

      super.tearDown();
   }


   public String toString() {

      return "ClockTest{" +
              "clock=" + clock +
              ", timer=" + timer +
              "} " + super.toString();
   }
}
