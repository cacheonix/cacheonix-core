package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;


/**
 * A tester for {@link ProcessingResult}.
 */
public final class ProcessingResultTest extends TestCase {


   public static final Object RESULT = new Object();

   private ProcessingResult processingResult;

   private Binary modifiedKey;


   public void testHasModifiedKey() throws Exception {

      assertTrue(processingResult.hasModifiedKey());
   }


   public void testDoesntHaveNullModifiedKey() throws Exception {

      assertFalse(new ProcessingResult(RESULT, null).hasModifiedKey());
   }


   public void testGetResult() throws Exception {

      assertEquals(RESULT, processingResult.getResult());
   }


   public void testGetModifiedKey() throws Exception {

      assertEquals(modifiedKey, processingResult.getModifiedKey());
   }


   public void testToString() throws Exception {

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