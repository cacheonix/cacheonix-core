package org.cacheonix.impl.net.tcp;

import java.io.IOException;
import java.nio.channels.Selector;

import junit.framework.TestCase;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tester for {@link SelectorWorker}.
 */
public final class SelectorWorkerTest extends TestCase {


   private static final long SELECTOR_TIMEOUT_MILLIS = 2000L;

   private static final long NETWORK_TIMEOUT_MILLIS = 1000L;

   /**
    * Object under test.
    */
   private SelectorWorker selectorWorker;

   private Selector selector;


   public void testRunClosesSelectorOnExit() throws IOException {

      // Make selector throw an IOException
      when(selector.select(anyLong())).thenThrow(IOException.class);

      // Run
      selectorWorker.run();

      // Verify
      verify(selector).close();
   }


   public void testProcessSelection() {

   }


   public void testToString() {

      assertNotNull(selectorWorker.toString());
   }


   /**
    * Set up the test.
    */
   public void setUp() throws Exception {

      selector = mock(Selector.class);
      selectorWorker = new SelectorWorker(selector, NETWORK_TIMEOUT_MILLIS, SELECTOR_TIMEOUT_MILLIS);

      super.setUp();
   }


   /**
    * Tear down the test.
    */
   public void tearDown() throws Exception {

      selectorWorker = null;

      super.tearDown();
   }
}