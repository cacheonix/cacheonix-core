package org.cacheonix.clock;

/**
 * An immutable reading of a system-wide physical clock.
 */
public interface Time extends Comparable<org.cacheonix.impl.clock.Time> {

   long getMillis();

   /**
    * Returns an event count used to advance the monotonic clock in cases repeated reads of the wall clock return the
    * same value.
    *
    * @return the event count used to advance the monotonic clock in cases repeated reads of the wall clock return the
    * same value.
    */
   long getCount();
}
