package org.cacheonix.impl.net.tcp;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

/**
 * A tester for {@link ChunkedBuffer}.
 */
public final class ChunkedBufferTest extends TestCase {

   /**
    * Object under test.
    */
   private ChunkedBuffer chunkedBuffer;


   /**
    * Tests {@link ChunkedBuffer#addChunk(ByteBuffer)}.
    */
   public void testAddChunk() {

      final int remaining1 = 10;
      final ByteBuffer byteBuffer1 = ByteBuffer.wrap(new byte[remaining1]);
      chunkedBuffer.addChunk(byteBuffer1);
      assertEquals(remaining1, chunkedBuffer.available());

      final int remaining2 = 20;
      final ByteBuffer byteBuffer2 = ByteBuffer.wrap(new byte[remaining2]);
      chunkedBuffer.addChunk(byteBuffer2);
      assertEquals(remaining1 + remaining2, chunkedBuffer.available());
   }


   /**
    * Tests {@link ChunkedBuffer#}.
    */
   public void testZeroAvailableInEmpty() {

      // Initial zero size
      assertEquals(0, chunkedBuffer.available());
   }


   /**
    * Tests {@link ChunkedBuffer#available()}.
    */
   public void testAvailable() {

      // Add 10 bytes
      final int remaining1 = 10;
      final ByteBuffer byteBuffer1 = ByteBuffer.wrap(new byte[remaining1]);
      chunkedBuffer.addChunk(byteBuffer1);

      // Add 20 bytes
      final int remaining2 = 20;
      final ByteBuffer byteBuffer2 = ByteBuffer.wrap(new byte[remaining2]);
      chunkedBuffer.addChunk(byteBuffer2);
      assertEquals(remaining1 + remaining2, chunkedBuffer.available());
   }


   /**
    * Tests {@link ChunkedBuffer#get()}.
    */
   public void testGet() {

      final int remaining = 10;
      final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[remaining]);
      chunkedBuffer.addChunk(byteBuffer);

      assertEquals(0x0, chunkedBuffer.get());
      assertEquals(remaining - 1, chunkedBuffer.available());
   }


   /**
    * Tests {@link ChunkedBuffer#getInt()}.
    */
   public void testGetInt() {

      final int remaining = 10;
      final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[remaining]);
      chunkedBuffer.addChunk(byteBuffer);

      assertEquals(0x0, chunkedBuffer.getInt());
      assertEquals("Expect avaiable bytes to drop by 4 due to int size", remaining - 4, chunkedBuffer.available());
   }


   /**
    * Tests {@link ChunkedBuffer#clear()}.
    */
   public void testClear() {

      // Test clear empty
      chunkedBuffer.clear();
      assertEquals(0, chunkedBuffer.available());

      // Test clear non-empty.
      final int remaining = 10;
      final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[remaining]);
      chunkedBuffer.addChunk(byteBuffer);
      chunkedBuffer.clear();
      assertEquals(0, chunkedBuffer.available());
   }


   /**
    * Tests {@link ChunkedBuffer#toString()}.
    */
   public void testToString() {

      assertNotNull(chunkedBuffer.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      chunkedBuffer = new ChunkedBuffer();
   }


   public void tearDown() throws Exception {

      chunkedBuffer = null;

      super.tearDown();
   }
}