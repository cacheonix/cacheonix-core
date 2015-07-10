/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.net.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import org.cacheonix.impl.net.Protocol;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Frame Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>08/15/2008</pre>
 */
public final class FrameTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(FrameTest.class); // NOPMD

   private Frame frame = null;

   private static final byte[] EMPTY_BYTE_ARRAY = {};


   public void testValidateHeaderLength() throws Exception {

      final Frame emptyFrame = new Frame();
      emptyFrame.setMaximumMessageLength(Integer.MAX_VALUE);
      emptyFrame.setSenderInetAddress(InetAddress.getByAddress(new byte[16]));

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
      emptyFrame.write(baos);
      baos.flush();
      assertEquals(Frame.HEADER_LENGTH, baos.size());
   }


   public void testValidateHeaderLengthNullSenderInetAddress() throws Exception {

      final Frame emptyFrame = new Frame();
      emptyFrame.setMaximumMessageLength(Integer.MAX_VALUE);
      emptyFrame.setSenderInetAddress(null);

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
      emptyFrame.write(baos);
      baos.flush();
      assertEquals(Frame.HEADER_LENGTH, baos.size());
   }


   public void testReadWriteEmptyFrame() throws IOException {

      final Frame emptyFrame = new Frame();
      emptyFrame.setMaximumMessageLength(Integer.MAX_VALUE);
      emptyFrame.setSenderInetAddress(InetAddress.getByAddress(new byte[16]));

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
      emptyFrame.write(baos);
      baos.flush();

      final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      final Frame newFrame = new Frame();
      newFrame.read(bais);
      assertEquals(emptyFrame, newFrame);
   }


   public void testToString() {

      assertNotNull(frame.toString());
   }


   public void testProtocolSignature() {

      assertTrue("Protocol signature is not allowed to be changed!",
              Arrays.equals("cchnx".getBytes(), Protocol.getProtocolSignature())); // NOPMD
   }


   public void testProtocolMagicNumber() {

      assertEquals("Protocol magic number is not allowed to be changed!", 65973751, Protocol.getProtocolMagicNumber());
   }


   public void testCreate() {

      final Frame otherFrame = new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH, Serializer.TYPE_JAVA, 0, 1, 0,
              EMPTY_BYTE_ARRAY);
      assertEquals(Serializer.TYPE_JAVA, otherFrame.getSerializerType());
   }


   protected void setUp() throws Exception {

      super.setUp();
      frame = new Frame();
   }


   public String toString() {

      return "FrameTest{" +
              "frame=" + frame +
              "} " + super.toString();
   }
}
