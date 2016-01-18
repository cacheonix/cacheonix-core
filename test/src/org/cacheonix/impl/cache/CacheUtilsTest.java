package org.cacheonix.impl.cache;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;
import org.mockito.Mockito;

import static org.cacheonix.impl.cache.CacheUtils.createExpirationTime;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A tester for {@link CacheUtils}.
 */
public final class CacheUtilsTest extends TestCase {

   /**
    * Tests {@link CacheUtils#createExpirationTime(Clock, long, TimeUnit)}.
    */
   public void testCreateExpirationTime() {

      final Clock clock = Mockito.mock(Clock.class);
      final Time currentTime = mock(Time.class);
      final Time newTime = mock(Time.class);
      when(currentTime.add(anyLong())).thenReturn(newTime);
      when(clock.currentTime()).thenReturn(currentTime);

      final Time expirationTime = createExpirationTime(clock, 1000L, TimeUnit.MILLISECONDS);
      assertEquals(expirationTime, newTime);
      verify(currentTime).add(1000L);
   }


   /**
    * Tests that {@link CacheUtils#createExpirationTime(Clock, long, TimeUnit)} when the delay is set to -1L.
    */
   public void testCreateNoExpirationTime() throws Exception {

      final Clock clock = Mockito.mock(Clock.class);

      final Time expirationTime = createExpirationTime(clock, -1, TimeUnit.MILLISECONDS);
      assertNull(expirationTime);
   }
}