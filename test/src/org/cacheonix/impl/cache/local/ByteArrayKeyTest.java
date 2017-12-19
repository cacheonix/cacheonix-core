package org.cacheonix.impl.cache.local;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

/**
 * A tester for {@link ByteArrayKey}. ByteArrayKey itself belongs to the test package, still worth testing.
 */
public final class ByteArrayKeyTest extends TestCase {


   private static final String TEST_BYTE_ARRAY_KEY_CONTENT = "Test ByteArrayKey context";

   /**
    * Object under test.
    */
   private ByteArrayKey byteArrayKey;


   public void testReadWriteExternal() throws IOException, ClassNotFoundException {

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
      final ObjectOutput oos = new ObjectOutputStream(baos);
      oos.writeObject(byteArrayKey);
      oos.flush();

      assertEquals(byteArrayKey, new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject());
   }


   public void testEquals() {

      assertEquals(byteArrayKey, new ByteArrayKey(TEST_BYTE_ARRAY_KEY_CONTENT.getBytes()));
   }


   public void testHashCode() {

      assertEquals(-1493308438, byteArrayKey.hashCode());
   }


   public void testToString() {

      assertNotNull(byteArrayKey.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      byteArrayKey = new ByteArrayKey(TEST_BYTE_ARRAY_KEY_CONTENT.getBytes());
   }


   public void tearDown() throws Exception {

      byteArrayKey = null;

      super.tearDown();
   }
}