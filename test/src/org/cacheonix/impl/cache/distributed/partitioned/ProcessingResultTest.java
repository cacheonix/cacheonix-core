package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;


/**
 * A tester for {@link ProcessingResult}.
 */
public final class ProcessingResultTest extends TestCase {


   private static final Object RESULT = new Object();

   private ProcessingResult processingResult;

   private Binary modifiedKey;


   public void testHasModifiedKey() {

      assertTrue(processingResult.hasModifiedKey());
   }


   public void testDoesntHaveNullModifiedKey() {

      assertFalse(new ProcessingResult(RESULT, null).hasModifiedKey());
   }


   public void testGetResult() {

      assertEquals(RESULT, processingResult.getResult());
   }


   public void testGetModifiedKey() {

      assertEquals(modifiedKey, processingResult.getModifiedKey());
   }


   public void testToString() {

      assertNotNull(processingResult.toString());
   }


   public void setUp() throws Exception {


      super.setUp();

      modifiedKey = TestUtils.toBinary("test key");
      processingResult = new ProcessingResult(RESULT, modifiedKey);
   }


   public void tearDown() throws Exception {

      processingResult = null;

      super.tearDown();
   }


   public String toString() {

      return "ProcessingResultTest{" +
              "processingResult=" + processingResult +
              ", modifiedKey=" + modifiedKey +
              '}';
   }
}