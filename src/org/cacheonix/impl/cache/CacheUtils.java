package org.cacheonix.impl.cache;

import java.util.concurrent.TimeUnit;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;

/**
 * A utility class to support cache operations.
 */
public final class CacheUtils {

   /**
    * A utility class constructor.
    */
   private CacheUtils() {

   }


   /**
    * Creates a cache element expiration time based on the
    *
    * @param clock    the system clock.
    * @param delay    the delay. -1 means that there is no expiration.
    * @param timeUnit the time unit of the delay.
    * @return the expiration time as measured by the <code>clock</code> or <code>null</code> if the <code>delay</code>
    * is set to -1.
    */
   public static Time createExpirationTime(final Clock clock, final long delay, final TimeUnit timeUnit) {

      return delay > 0 ? clock.currentTime().add(timeUnit.toMillis(delay)) : null;
   }
}
