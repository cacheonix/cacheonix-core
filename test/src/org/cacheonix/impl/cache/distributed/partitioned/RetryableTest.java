package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;
import org.cacheonix.impl.net.processor.RetryException;

/**
 * Tester for {@link Retryable}.
 */
public final class RetryableTest extends TestCase {

   private static final String TEST_DESCRIPTION = "Test description";

   private static final Object TEST_RESULT = new Object();

   /**
    * Object under test.
    */
   private Retryable retryable;


   /**
    * Tests for {@link Retryable#execute()}.
    */
   public void testExecute() throws RetryException {

      assertEquals(TEST_RESULT, retryable.execute());
   }


   /**
    * Tests for {@link Retryable#description()}.
    */
   public void testDescription() {

      assertEquals(TEST_DESCRIPTION, retryable.description());
   }


   /**
    * Tests for {@link Retryable#toString()}.
    */
   public void testToString() {

      assertNotNull(retryable.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      retryable = new Retryable(TEST_DESCRIPTION) {

         public Object execute() {

            return TEST_RESULT;
         }
      };
   }


   @Override
   public String toString() {

      return "RetryableTest{" +
              "retryable=" + retryable +
              '}';
   }
}