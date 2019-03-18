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
package org.cacheonix.impl.net.multicast.sender;

import java.io.IOException;

import junit.framework.TestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.util.logging.Logger;

/**
 * MulticastSender Tester.
 *
 * @since <pre>03/28/2008</pre>
 */
public final class PlainMulticastSenderTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PlainMulticastSenderTest.class); // NOPMD

   private static final String TOO_BIG_OBJECT = buildTooBigObject();

   private static final int TOO_BIG_SIZE = Frame.MAXIMUM_MCAST_MESSAGE_LENGTH * 2;


   private PlainMulticastSender sender;


   public void testSendFrame() throws IOException {

      final Frame frame = new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH);
      frame.setPayload(Serializer.TYPE_JAVA, TestConstants.OBJECT_TO_MULTICAST);
      sender.sendFrame(frame);
   }


   public void testSendTooBigFails() throws IOException {

      try {
         final Frame frame = new Frame(TOO_BIG_SIZE);
         frame.setPayload(Serializer.TYPE_JAVA, TOO_BIG_OBJECT);
         sender.sendFrame(frame);
      } catch (final IllegalArgumentException ignored) {
      }
      fail("Expected an exception but it wasn't thrown.");
   }


   public void testSendMany() throws IOException {

      final long end = System.currentTimeMillis() + 1000L;
      do {
         final String obj = TestConstants.OBJECT_TO_MULTICAST;
         final Frame frame = new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH);
         frame.setPayload(Serializer.TYPE_JAVA, obj);
         sender.sendFrame(frame);
      } while (System.currentTimeMillis() <= end);
   }


   public void testToString() {

      assertNotNull(sender.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      sender = new PlainMulticastSender(TestUtils.getInetAddress(TestConstants.MULTICAST_ADDRESS),
              TestConstants.MULTICAST_PORT, TestConstants.MULTICAST_TTL);
   }


   public void tearDown() throws Exception {

      sender = null;
      super.tearDown();
   }


   /**
    * Builds an object too large to send.
    *
    * @return an object too large to send.
    * @see #TOO_BIG_SIZE
    */
   private static String buildTooBigObject() {

      final StringBuilder sb = new StringBuilder(TOO_BIG_SIZE);
      while (sb.length() < TOO_BIG_SIZE) {
         sb.append(TestConstants.OBJECT_TO_MULTICAST).append(' ');
      }

      return sb.toString();
   }


   public String toString() {

      return "PlainMulticastSenderTest{" +
              "sender=" + sender +
              "} " + super.toString();
   }
}
