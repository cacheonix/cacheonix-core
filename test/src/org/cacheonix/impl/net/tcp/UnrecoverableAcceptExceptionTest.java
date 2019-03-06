package org.cacheonix.impl.net.tcp;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * A tester for {@link UnrecoverableAcceptException}.
 */
public final class UnrecoverableAcceptExceptionTest extends TestCase {


   private static final String TEST_IO_EXCEPTION = "Test IO Exception";

   /**
    * Object under test.
    */
   private UnrecoverableAcceptException exception;

   private IOException cause;


   public void testCause() {

      assertEquals(cause, exception.getCause());
   }


   /**
    * Set up the test.
    */
   public void setUp() throws Exception {

      super.setUp();

      cause = new IOException(TEST_IO_EXCEPTION);

      exception = new UnrecoverableAcceptException(cause);
   }


   public void tearDown() throws Exception {

      cause = null;
      exception = null;

      super.tearDown();
   }
}