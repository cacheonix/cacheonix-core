package org.cacheonix.impl.net.tcp.io;

import junit.framework.TestCase;

/**
 * A tester for {@link FrameFormatException}.
 */
public final class FrameFormatExceptionTest extends TestCase {


   public static final String MESSAGE = "Test message";

   private FrameFormatException frameFormatException;


   public void testGetMessage() {

      assertEquals(MESSAGE, frameFormatException.getMessage());
   }


   public void setUp() throws Exception {

      super.setUp();

      frameFormatException = new FrameFormatException(MESSAGE);
   }


   public void tearDown() throws Exception {

      frameFormatException = null;

      super.tearDown();
   }


   public String toString() {

      return "FrameFormatExceptionTest{" +
              "frameFormatException=" + frameFormatException +
              '}';
   }
}