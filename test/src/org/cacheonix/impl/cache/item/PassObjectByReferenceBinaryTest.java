/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.org/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.cache.item;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import junit.framework.TestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.TestUtils.getBytes;

/**
 * Tester for  PartitionElementValueByCopy.
 */
public final class PassObjectByReferenceBinaryTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PassObjectByReferenceBinaryTest.class); // NOPMD

   private static final String TEST_VALUE = "test_value";

   private PassObjectByReferenceBinary binary;


   public void testSetValue() {

      assertEquals(TEST_VALUE, binary.getValue());
      assertSame(TEST_VALUE, binary.getValue());
   }


   public void testSetNullValue() {

      final PassObjectByReferenceBinary nullBinary = new PassObjectByReferenceBinary(null);
      assertNull(nullBinary.getValue());
      assertEquals(0, nullBinary.hashCode());
   }


   public void testSerializeDeserialize() throws IOException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(binary, ser.deserialize(ser.serialize(binary)));
   }


   public void testSerializeDeserializeInteger() throws IOException {

      final PassObjectByReferenceBinary intBinary = new PassObjectByReferenceBinary(Integer.valueOf(5000));
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(intBinary, ser.deserialize(ser.serialize(intBinary)));
   }


   public void testSerializeDeserializeUsingUtils() throws IOException, ClassNotFoundException {

      // Write
      final PassObjectByReferenceBinary intBinary = new PassObjectByReferenceBinary(Integer.valueOf(5000));
      final ByteArrayOutputStream baos = new ByteArrayOutputStream(111);
      final DataOutputStream dos = new DataOutputStream(baos);
      SerializerUtils.writeBinary(dos, intBinary);
      dos.flush();

      // Read
      final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      final DataInputStream dis = new DataInputStream(bais);
      final Binary newBinary = SerializerUtils.readBinary(dis);
      assertEquals(intBinary, newBinary);
   }


   public void testReadWriteExternal() throws IOException, ClassNotFoundException {

      final byte[] bytes = getBytes(binary);
      assertEquals(binary, new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
   }


   public void testEquals() {

      assertEquals(new PassObjectByReferenceBinary(new Serializable[]{0, 1, 2}),
              new PassObjectByReferenceBinary(new Serializable[]{0, 1, 2}));
   }



   public void testEqualsNulls() {

      assertEquals(new PassObjectByReferenceBinary(null), new PassObjectByReferenceBinary(null));
   }



   protected final void setUp() throws Exception {

      super.setUp();
      binary = new PassObjectByReferenceBinary(TEST_VALUE);
   }


   public final String toString() {

      return "PassByReferenceItemTest{" +
              "item=" + binary +
              '}';
   }
}