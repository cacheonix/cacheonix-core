package org.cacheonix.impl.cache.item;

import junit.framework.TestCase;

/**
 * A tester for {@link InvalidParameterException}.
 */
public final class InvalidParameterExceptionTest extends TestCase {


   private InvalidParameterException invalidParameterException;

   private Throwable cause;


   /**
    * Tests the the costructor correctly sets the cause.
    */
   public void testConstructor() {

      assertSame(cause, invalidParameterException.getCause());
   }


   public void setUp() throws Exception {

      super.setUp();

      cause = new Throwable();
      invalidParameterException = new InvalidParameterException(cause);
   }


   public void tearDown() throws Exception {

      cause = null;
      invalidParameterException = null;

      super.tearDown();
   }


   public String toString() {

      return "InvalidParameterExceptionTest{" +
              "cause=" + cause +
              ", invalidParameterException=" + invalidParameterException +
              "} " + super.toString();
   }
}