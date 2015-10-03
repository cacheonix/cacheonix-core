package org.cacheonix.impl.clock;

import org.cacheonix.impl.net.serializer.Wireable;

/**
 * An immutable reading of a physical clock.
 */
public interface Time extends Comparable<Time>, Wireable {

   long getMillis();

   /**
    * Returns an event count used to advance the monotonic clock in cases repeated reads of the wall clock return the
    * same value.
    *
    * @return the event count used to advance the monotonic clock in cases repeated reads of the wall clock return the
    * same value.
    */
   long getCount();

   /**
    * Subtracts time from this time and returns result.
    *
    * @param time time to subtract.
    * @return a new Time object representing time differential.
    */
   Time subtract(Time time);

   /**
    * Adds time in milliseconds.
    *
    * @param timeMillis millisecond interval.
    * @return a new Time object representing the result of the addition.
    */
   Time add(long timeMillis);

   /**
    * Adds time in milliseconds.
    *
    * @param time time interval.
    * @return a new Time object representing the result of the addition.
    */
   Time add(Time time);

   Time divide(int size);
}
