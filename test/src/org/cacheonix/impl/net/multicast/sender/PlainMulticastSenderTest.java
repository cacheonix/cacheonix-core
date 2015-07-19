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
package org.cacheonix.impl.net.multicast.sender;

import java.io.IOException;

import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

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

   private PlainMulticastSender sender;


   public void testToString() {

      assertNotNull(sender.toString());
   }


   public void testSend() throws IOException {

      final Frame frame = new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH);
      frame.setPayload(Serializer.TYPE_JAVA, "test");
      sender.sendFrame(frame);
   }


   public void testSendMany() throws IOException {

      final long end = System.currentTimeMillis() + 1000L;
      do {
         final String obj = "testttttttttttttttttttttttttttttttt";
         final Frame frame = new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH);
         frame.setPayload(Serializer.TYPE_JAVA, obj);
         sender.sendFrame(frame);
      } while (System.currentTimeMillis() <= end);
   }


   protected void setUp() throws Exception {

      super.setUp();
      sender = new PlainMulticastSender(TestUtils.getInetAddress(TestConstants.MULTICAST_ADDRESS),
              TestConstants.MULTICAST_PORT, TestConstants.MULTICAST_TTL);
   }


   public String toString() {

      return "PlainMulticastSenderTest{" +
              "sender=" + sender +
              "} " + super.toString();
   }
}
