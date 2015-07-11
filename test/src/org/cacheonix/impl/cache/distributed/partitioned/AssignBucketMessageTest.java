package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;

/**
 * A tester for {@link AssignBucketMessage}.
 */
public final class AssignBucketMessageTest extends TestCase {


   private static final byte STORAGE_NUMBER = (byte) 1;

   private static final Integer BUCKET_NUMBER = 2;

   private static final String CACHE_NAME = "test.cache.name";

   private AssignBucketMessage assignBucketMessage;

   public void testGetStorageNumber() throws Exception {

      assertEquals(STORAGE_NUMBER, assignBucketMessage.getStorageNumber());
   }


   public void testGetBucketNumber() throws Exception {

      assertEquals(BUCKET_NUMBER, assignBucketMessage.getBucketNumber());
   }


   public void testToString() throws Exception {

      assertNotNull(toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      //
      assignBucketMessage = new AssignBucketMessage(CACHE_NAME, STORAGE_NUMBER, BUCKET_NUMBER);
   }


   public void tearDown() throws Exception {

      assignBucketMessage = null;
      super.tearDown();
   }


   public String toString() {

      return "AssignBucketMessageTest{" +
              "assignBucketMessage=" + assignBucketMessage +
              "} " + super.toString();
   }
}