package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * A tester for {@link AssignBucketMessage}.
 */
public final class AssignBucketMessageTest extends TestCase {


   private static final byte STORAGE_NUMBER = (byte) 1;

   private static final Integer BUCKET_NUMBER = 2;

   private static final String CACHE_NAME = "test.cache.name";

   private AssignBucketMessage assignBucketMessage;

   private CacheProcessor cacheProcessor;


   public void testGetStorageNumber() {

      assertEquals(STORAGE_NUMBER, assignBucketMessage.getStorageNumber());
   }


   public void testGetBucketNumber() {

      assertEquals(BUCKET_NUMBER, assignBucketMessage.getBucketNumber());
   }


   public void testExecuteOperational() {

      assignBucketMessage.setProcessor(cacheProcessor);
      assignBucketMessage.executeOperational();
      verify(cacheProcessor).createBucket(STORAGE_NUMBER, BUCKET_NUMBER);
   }


   public void testExecuteBlocked() throws Exception {

      testExecuteOperational();
   }


   public void testToString() {

      assertNotNull(toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      //
      cacheProcessor = mock(CacheProcessor.class);

      //
      assignBucketMessage = new AssignBucketMessage(CACHE_NAME, STORAGE_NUMBER, BUCKET_NUMBER);
      assignBucketMessage.setProcessor(cacheProcessor);
   }


   public void tearDown() throws Exception {

      assignBucketMessage = null;
      super.tearDown();
   }


   public String toString() {

      return "AssignBucketMessageTest{" +
              "assignBucketMessage=" + assignBucketMessage +
              ", cacheProcessor=" + cacheProcessor +
              "} " + super.toString();
   }
}