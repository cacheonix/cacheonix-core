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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.MulticastFrameMessage;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Multicast Frame sender that uses a list of known TCP addresses of machines as broadcasters.
 */
public final class TCPMulticastSender implements MulticastSender {


   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TCPMulticastSender.class); // NOPMD

   private final ReceiverAddress loopbackReceiverAddress;

   private final List<ReceiverAddress> receiverAddresses;

   private final ClusterNodeAddress localAddress;

   private final Router router;


   public TCPMulticastSender(final Router router, final ClusterNodeAddress localAddress,
           final List<ReceiverAddress> receiverAddresses) {

      this.loopbackReceiverAddress = new ReceiverAddress(localAddress.getAddresses(), localAddress.getTcpPort());
      this.receiverAddresses = new ArrayList<>(receiverAddresses);
      this.localAddress = localAddress;
      this.router = router;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation wraps the frame into a MulticastFrameMessage and sends the MulticastFrameMessage to the known
    * address.
    */
   public void sendFrame(final Frame frame) {

      // OPTIMIZEME: simeshev@cacheonix.org - There is an opportunity here to pre-populate MulticastFrameMessage's
      // visited list to reduce re-broadcasts. Consider it once the processor's state known receivers are available.

      // Send the frame message to receivers
      boolean sentToSelf = false;
      for (final ReceiverAddress receiverAddress : receiverAddresses) {

         // Send
         final MulticastFrameMessage multicastFrameMessage = createMulticastFrameMessage(frame, receiverAddress);
         router.route(multicastFrameMessage);

         // Raise the sentToSelf flag for further use by send-to-self logic
         if (receiverAddress.isAddressOf(localAddress)) {

            sentToSelf = true;
         }
      }

      // Send to self if it wasn't sent by the main send loop
      if (!sentToSelf) {

         try {

            // Create a frame copy
            final ByteArrayOutputStream out = new ByteArrayOutputStream(
                    frame.getPayload().length + Frame.HEADER_LENGTH);
            frame.write(out);
            out.flush();
            final Frame frameCopy = new Frame();
            frameCopy.read(new ByteArrayInputStream(out.toByteArray()));

            // Send to self, no need to broadcast
            final MulticastFrameMessage loopbackMessage = createMulticastFrameMessage(frameCopy,
                    loopbackReceiverAddress);
            loopbackMessage.setSendToKnownAddresses(false);
            router.route(loopbackMessage);
         } catch (final IOException e) {

            LOG.warn("Error while sending a frame to self: " + e, e);
         }
      }
   }


   private static MulticastFrameMessage createMulticastFrameMessage(final Frame frame,
           final ReceiverAddress receiverAddress) {

      final MulticastFrameMessage message = new MulticastFrameMessage();
      message.setReceiver(receiverAddress);
      message.setOriginator(true);
      message.setFrame(frame);
      return message;
   }


   public String toString() {

      return "TCPMulticastSender{" +
              "loopbackReceiverAddress=" + loopbackReceiverAddress +
              ", receiverAddresses=" + receiverAddresses +
              ", localAddress=" + localAddress +
              ", router=" + router +
              '}';
   }
}
