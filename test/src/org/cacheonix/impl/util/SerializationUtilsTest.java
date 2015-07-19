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
package org.cacheonix.impl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * SerializationUtilsTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 1, 2009 8:36:38 PM
 */
public final class SerializationUtilsTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SerializationUtilsTest.class); // NOPMD

   private static final int SERIALIZED_UUID_LENGTH = 17;

   private static final Object SERIALIZED_TIME_LENGTH = 17;


   public void testWriteReadUuid() throws IOException {

      final UUID original = UUID.randomUUID();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
      SerializerUtils.writeUuid(original, new DataOutputStream(baos));
      final byte[] bytes = baos.toByteArray();
      assertEquals(SERIALIZED_UUID_LENGTH, bytes.length);
      final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
      final UUID copy = SerializerUtils.readUuid(in);
      assertEquals(0, in.available());
      assertEquals(original, copy);
   }


   public void testWriteReadTime() throws IOException {

      final Time original = new Time(1000, 2000);
      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
      SerializerUtils.writeTime(original, new DataOutputStream(baos));
      final byte[] bytes = baos.toByteArray();
      assertEquals(SERIALIZED_TIME_LENGTH, bytes.length);
      final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
      final Time copy = SerializerUtils.readTime(in);
      assertEquals(0, in.available());
      assertEquals(original, copy);
   }


   public void testWriteReadIP4InetAddress() throws IOException {

      final InetAddress original = InetAddress.getByAddress(new byte[]{1, 1, 1, 1});

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);

      SerializerUtils.writeInetAddress(original, new DataOutputStream(baos), false);

      final byte[] bytes = baos.toByteArray();
      assertEquals(6, bytes.length);

      final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
      final InetAddress copy = SerializerUtils.readInetAddress(in, false);
      assertEquals(0, in.available());
      assertEquals(original, copy);
   }


   public void testWriteReadIP6InetAddress() throws IOException {

      final InetAddress original = InetAddress.getByAddress(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);

      SerializerUtils.writeInetAddress(original, new DataOutputStream(baos), false);

      final byte[] bytes = baos.toByteArray();
      assertEquals(18, bytes.length);

      final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

      final InetAddress copy = SerializerUtils.readInetAddress(in, false);

      assertEquals(0, in.available());
      assertEquals(original, copy);
   }


   public void testWriteReadNullInetAddress() throws IOException {

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
      SerializerUtils.writeInetAddress(null, new DataOutputStream(baos), false);
      final byte[] bytes = baos.toByteArray();
      assertEquals(1, bytes.length);
      final InetAddress copy = SerializerUtils.readInetAddress(new DataInputStream(new ByteArrayInputStream(bytes)), false);
      assertNull(copy);
   }


   public void testWriteReadIP4InetAddressFixedLength() throws IOException {

      final InetAddress original = InetAddress.getByAddress(new byte[]{1, 1, 1, 1});

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);

      SerializerUtils.writeInetAddress(original, new DataOutputStream(baos), true);

      final byte[] bytes = baos.toByteArray();
      assertEquals(18, bytes.length);

      final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
      final InetAddress copy = SerializerUtils.readInetAddress(in, true);
      assertEquals(0, in.available());
      assertEquals(original, copy);
   }


   public void testWriteReadIP6InetAddressFixedLength() throws IOException {

      final InetAddress original = InetAddress.getByAddress(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);

      SerializerUtils.writeInetAddress(original, new DataOutputStream(baos), true);

      final byte[] bytes = baos.toByteArray();
      assertEquals(18, bytes.length);

      final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

      final InetAddress copy = SerializerUtils.readInetAddress(in, true);

      assertEquals(0, in.available());
      assertEquals(original, copy);
   }


   public void testWriteReadNullInetAddressFixedLength() throws IOException {

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
      SerializerUtils.writeInetAddress(null, new DataOutputStream(baos), true);
      final byte[] bytes = baos.toByteArray();
      assertEquals(18, bytes.length);
      final InetAddress copy = SerializerUtils.readInetAddress(new DataInputStream(new ByteArrayInputStream(bytes)), true);
      assertNull(copy);
   }


   public void testWriteReadNull() throws IOException {

      final UUID nullUUID = null;
      final ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
      SerializerUtils.writeUuid(nullUUID, new DataOutputStream(baos));
      final byte[] bytes = baos.toByteArray();
      assertEquals(SERIALIZED_UUID_LENGTH, bytes.length);
      final UUID copy = SerializerUtils.readUuid(new DataInputStream(new ByteArrayInputStream(bytes)));
      assertNull(copy);
   }
}
