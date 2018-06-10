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
package org.cacheonix.impl.clock;

import org.cacheonix.CacheonixTestCase;

/**
 * Tester for Clock.
 */
public final class ClockDiscontinuityTest extends CacheonixTestCase {

   private static final long MINIMAL_INTER_PROCESS_DELAY_NANOS = 1000L;

   private static final long JUMP_DETECTION_POLL_PERIOD_MILLIS = 1000L;

   private ClockImpl clock;

   private TestWallClock wallClock;


   @SuppressWarnings("SimplifiableJUnitAssertion")
   public void testCurrentTimeProgressesWithStoppedClock() {

      wallClock.setCurrentTimeMillis(0);

      final Time time1 = clock.currentTime();
      final Time time2 = clock.currentTime();

      assertTrue(!time1.equals(time2));
      assertEquals(0, time1.getMillis());
      assertEquals(0, time2.getMillis());
      assertTrue(time2.compareTo(time1) > 0);
      assertTrue(time1.compareTo(time1) == 0);
      assertTrue(time1.compareTo(time2) <= 0);
   }


   @SuppressWarnings("SimplifiableJUnitAssertion")
   public void testCurrentTimeToleratesForwardWallClockJumps() {

      final long startWallTimeMillis = 10000;
      wallClock.setCurrentTimeMillis(startWallTimeMillis);
      final Time time1 = clock.currentTime();
      assertEquals(10000L, time1.getMillis());

      // Jump farther then precision to trigger offset generation
      wallClock.setCurrentTimeMillis(startWallTimeMillis + JUMP_DETECTION_POLL_PERIOD_MILLIS * 3);
      final Time time2 = clock.currentTime();
      assertEquals(startWallTimeMillis + JUMP_DETECTION_POLL_PERIOD_MILLIS, time2.getMillis());

      final Time time3 = clock.currentTime();
      assertEquals(time2.getMillis(), time3.getMillis());

      assertTrue(!time1.equals(time2));
      assertTrue(time2.compareTo(time1) > 0);
      assertTrue(time1.compareTo(time1) == 0);
      assertTrue(time1.compareTo(time2) <= 0);
   }


   @SuppressWarnings("SimplifiableJUnitAssertion")
   public void testCurrentTimeToleratesBackwardWallClockJumps() {

      final long startWallTimeMillis = 10000L;
      wallClock.setCurrentTimeMillis(startWallTimeMillis);
      final Time time1 = clock.currentTime();
      assertEquals(10000L, time1.getMillis());

      // Jump back
      final long wallTimeMillis1 = startWallTimeMillis - 1000L;
      wallClock.setCurrentTimeMillis(wallTimeMillis1);
      final Time time2 = clock.currentTime();
      assertEquals(startWallTimeMillis, time2.getMillis());

      final Time time3 = clock.currentTime();
      assertEquals(time2.getMillis(), time3.getMillis());

      // Advance forward from jumped-back position
      final long wallTimeMillis2 = wallTimeMillis1 + 100L;
      wallClock.setCurrentTimeMillis(wallTimeMillis2);
      final Time time4 = clock.currentTime();
      assertEquals(startWallTimeMillis + 100L, time4.getMillis());

      assertTrue(!time1.equals(time2));
      assertTrue(time2.compareTo(time1) > 0);
      assertTrue(time1.compareTo(time1) == 0);
      assertTrue(time1.compareTo(time2) <= 0);
   }


   public void testToString() {

      assertNotNull(clock.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      wallClock = new TestWallClock();
      clock = new ClockImpl(MINIMAL_INTER_PROCESS_DELAY_NANOS, JUMP_DETECTION_POLL_PERIOD_MILLIS, wallClock);
   }


   /**
    * Test wall clock.
    */
   private static class TestWallClock implements WallClock {

      private long currentTimeMillis;


      public long currentTimeMillis() {

         return currentTimeMillis;
      }


      public void setCurrentTimeMillis(final long currentTimeMillis) {

         this.currentTimeMillis = currentTimeMillis;
      }


      public String toString() {

         return "TestWallClock{" +
                 "currentTimeMillis=" + currentTimeMillis +
                 '}';
      }
   }


   public String toString() {

      return "ClockDiscontinuityTest{" +
              "clock=" + clock +
              ", wallClock=" + wallClock +
              "} " + super.toString();
   }
}
