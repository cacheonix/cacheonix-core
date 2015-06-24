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
import java.util.TimerTask;

import org.cacheonix.impl.util.logging.Logger;

/**
 * A monotonic physical clock.
 */
public final class ClockImpl implements Clock {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClockImpl.class); // NOPMD

   private static final long JUMP_DETECTION_PERIOD_MILLIS = 1000L;


   /**
    * Mutex.
    */
   private final Object lock = new Object();


   /**
    * Time source.
    */
   private final WallClock wallClock;

   private final long minimalInterProcessDelayNanos;

   /**
    * Frequency of checks for jumps.
    */
   private final long jumpDetectionPeriodMillis;


   /**
    * How far the clock can jump forward between two jump detection intervals before being deemed as jumped. It is set
    * to a jump detection period multiplied by two to protect from possible delays in timer responsible for polling.
    */
   private final long maxDiscontinuityMillis;


   /**
    * Offset set by the dump detection protocol.
    */
   private long offsetMillis = 0L;

   /**
    * System clock reading set by the last call to {@link #readClock()}.
    */
   private long lastClockReadingMillis = 0;

   /**
    */
   private boolean lastClockReadingMillisIsSet = false;

   /**
    * Offset set by the clock synchronization protocol {@link #adjust(Time)}.
    */
   private long adjustmentMillis = 0L;

   private long lastReturnedClockMillis;

   /**
    * Event count used to advance the monotonic clock in cases repeated reads of the wall clock return the same value.
    */
   private long count;


   ClockImpl(final long minimalInterProcessDelayNanos, final long jumpDetectionPeriodMillis,
           final WallClock wallClock) {

      this.minimalInterProcessDelayNanos = minimalInterProcessDelayNanos;
      this.jumpDetectionPeriodMillis = jumpDetectionPeriodMillis;
      this.maxDiscontinuityMillis = jumpDetectionPeriodMillis << 1;
      this.wallClock = wallClock;
   }


   public ClockImpl(final long minimalInterProcessDelayNanos) {

      this(minimalInterProcessDelayNanos, JUMP_DETECTION_PERIOD_MILLIS, new SystemWallClock());
   }


   /**
    * Initializes time interval-based polling of this clock to enable protection from jumps back or forward. This method
    * should be called before first call to currentTime(), ideally immediately after creating the clock.
    *
    * @param timer the timer to use to poll this clock.
    * @return this to enabled call cascading.
    */
   public Clock attachTo(final Timer timer) {

      timer.schedule(new TimerTask() {

         public void run() {

            readClock();
         }
      }, jumpDetectionPeriodMillis, jumpDetectionPeriodMillis);

      return this;
   }


   /**
    * This method must be called at least twice as often as {@link #JUMP_DETECTION_PERIOD_MILLIS}. If system time moved
    * farther than that, or if it moved backwards, there was a discontinuation and this method will set up an offset.
    *
    * @return current monotonic clock reading.
    */
   private long readClock() {

      final long currentTimeMillis = wallClock.currentTimeMillis();

      synchronized (lock) {

         final long currentClockReadingMillis = currentTimeMillis + offsetMillis;

         // Init
         if (!lastClockReadingMillisIsSet) {

            lastClockReadingMillis = currentTimeMillis;
            lastClockReadingMillisIsSet = true;
         }

         final long differential = currentClockReadingMillis - lastClockReadingMillis;
         if (differential > maxDiscontinuityMillis) {

            // The clock jumped forward, should not be more than jumpDetectionPrecisionMillis
            offsetMillis += jumpDetectionPeriodMillis - differential;
            lastClockReadingMillis = currentTimeMillis + offsetMillis;

         } else if (differential < 0) {

            // The clock jumped backwards

            // Expected: lastSystemClockReadingMillis + jumpDetectionPeriodMillis
            offsetMillis -= differential;
         } else {

            lastClockReadingMillis = currentClockReadingMillis;
         }

         return lastClockReadingMillis;
      }
   }


   /**
    * {@inheritDoc}
    */
   public Time currentTime() {

      synchronized (lock) {

         // Correct jumps if any
         final long currentClockMillis = readClock();
         if (currentClockMillis == lastReturnedClockMillis) {

            // The millisecond time is the same, increment event counter
            count++;

         } else {

            // Update last returned
            lastReturnedClockMillis = currentClockMillis;

            // Reset counter
            count = 0;

         }

         return new Time(currentClockMillis + adjustmentMillis, count);
      }
   }


   /**
    * {@inheritDoc}
    */
   public void adjust(final Time time) {

      if (time == null) {

         // Do nothing
         return;
      }

      synchronized (lock) {

         final Time currentTime = currentTime();
         if (time.compareTo(currentTime) > 0) {

            adjustmentMillis = time.getMillis() - currentTime.getMillis();

            if (adjustmentMillis == 0) {

               count = time.getCount() + 1;
            } else {

               count = time.getCount();
            }
         }
      }
   }


   public String toString() {

      synchronized (lock) {

         return "Clock{" +
                 "lastClockReadingMillis=" + lastClockReadingMillis +
                 ", count=" + count +
                 ", offsetMillis=" + offsetMillis +
                 ", adjustmentMillis=" + adjustmentMillis +
                 ", minimalInterProcessDelayNanos=" + minimalInterProcessDelayNanos +
                 ", jumpDetectionPeriodMillis=" + jumpDetectionPeriodMillis +
                 ", maxDiscontinuityMillis=" + maxDiscontinuityMillis +
                 ", lastReturnedClockMillis=" + lastReturnedClockMillis +
                 ", wallClock=" + wallClock +
                 '}';
      }
   }
}
