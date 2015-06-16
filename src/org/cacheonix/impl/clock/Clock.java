package org.cacheonix.impl.clock;

import java.util.Timer;

/**
 * A monotonic physical clock.
 */
public interface Clock {

   /**
    * Initializes time interval-based polling of this clock to enable protection from jumps back or forward. This method
    * should be called before first call to currentTime(), ideally immediately after creating the clock.
    *
    * @param timer the timer to use to poll this clock.
    */
   void attachTo(Timer timer);

   /**
    * Returns current physical time.
    *
    * @return current physical time.
    */
   Time currentTime();

   /**
    * Adjusts the current time to the given time. This method is used when synchronizing clocks. The clock is adjusted
    * if the current local time is behind. Does nothing if the parameter <code>time</code> is null.
    *
    * @param time time to use to adjust the clock if the time is ahead of the clock. Can be <code>null</code>.
    */
   void adjust(Time time);
}
