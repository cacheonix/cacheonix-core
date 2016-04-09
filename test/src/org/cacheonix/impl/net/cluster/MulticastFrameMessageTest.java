/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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
package org.cacheonix.impl.net.cluster;

import java.net.InetAddress;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * A tester for MulticastFrameMessage.
 */
public class MulticastFrameMessageTest extends CacheonixTestCase {


   private MulticastFrameMessage message;


   public void testSetFrame() throws Exception {

      assertNull("Default should be 'null'", message.getFrame());
      final Frame frame = new Frame();
      message.setFrame(frame);
      assertEquals(frame, message.getFrame());
   }


   public void testRequiresSameCluster() {

      assertFalse("Should not require same cluster", message.isRequiresSameCluster());
   }


   public void testSetOriginator() throws Exception {

      assertFalse("Default should be 'false'", message.isOriginator());
      message.setOriginator(true);
      assertTrue(message.isOriginator());
   }


   public void testSetSenderInetAddress() throws Exception {

      final InetAddress localHost = InetAddress.getLocalHost();
      message.setSenderInetAddress(localHost);
      assertEquals(localHost, message.getSenderInetAddress());
   }


   public void testSetSendToKnownAddresses() {

      assertTrue("Default should be 'true'", message.isSendToKnownAddresses());
      message.setSendToKnownAddresses(false);
      assertFalse(message.isSendToKnownAddresses());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(message, ser.deserialize(ser.serialize(message)));

   }


   public void testHashCode() throws Exception {

      assertTrue(message.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(message.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      message = new MulticastFrameMessage();
   }
}
