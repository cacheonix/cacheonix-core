package org.cacheonix.impl.net.tcp;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import junit.framework.TestCase;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.util.time.Timeout;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A tester for {@link KeyHandler}.
 */
public final class KeyHandlerTest extends TestCase {


   private static final long NETWORK_TIMEOUT = 1000L;

   /**
    * Object under test.
    */
   private KeyHandler keyHandler;

   /**
    * Mock timeout.
    */
   private Timeout timeout;

   /**
    * Mock selector.
    */
   private Selector selector;


   public void testRegisterActivity() {

      keyHandler.registerActivity();

      verify(timeout, times(1)).reset();
   }


   public void testRegisterInactivity() {

      when(timeout.isExpired()).thenReturn(Boolean.TRUE);

      keyHandler.registerInactivity(mock(SelectionKey.class));

      verify(timeout, times(1)).reset();
   }


   public void testGetNetworkTimeoutMillis() {

      when(timeout.getDuration()).thenReturn(NETWORK_TIMEOUT);

      assertEquals(NETWORK_TIMEOUT, keyHandler.getNetworkTimeoutMillis());

      verify(timeout, times(1)).getDuration();
   }


   public void testSelector() {

      assertEquals(selector, keyHandler.selector());
   }


   public void testSocketChannel() {

      final SelectionKey selectionKey = mock(SelectionKey.class);
      final SocketChannel selectableChannel = mock(SocketChannel.class);
      when(selectionKey.channel()).thenReturn(selectableChannel);

      assertEquals(selectableChannel, KeyHandler.socketChannel(selectionKey));

   }


   public void testToString() {

      assertNotNull(keyHandler.toString());
   }


   /**
    * Set up the test.
    */
   public void setUp() throws Exception {

      selector = mock(Selector.class);
      final Clock clock = mock(Clock.class);
      timeout = mock(Timeout.class);
      keyHandler = mock(KeyHandler.class, Mockito.withSettings().useConstructor(selector, timeout, clock));
      super.setUp();
   }
}