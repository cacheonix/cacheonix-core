package org.cacheonix.impl.util.time;

/**
 * A tracker for timeouts.
 */
public interface Timeout {

   /**
    * Returns the intended duration of the Timeout in milliseconds.
    */
   long getDuration();

   /**
    * Returns <code>true</code> if the timeout has expired.
    */
   boolean isExpired();

   /**
    * Begins a new waiting cycle. This method starts a new waiting cycle regardless of whether this Timeout is already
    * waiting.
    */
   Timeout reset();

   /**
    * Cancels current waiting cycle. This method may be called whether or not this Timeout is actually waiting or not.
    */
   void cancel();
}
