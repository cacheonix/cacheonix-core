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
package org.cacheonix.impl.net.cluster;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.SenderInetAddressAware;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A message carrying a multicast frame when Cacheonix uses TCP broadcast.
 */
@SuppressWarnings("RedundantIfStatement")
public final class MulticastFrameMessage extends Message implements SenderInetAddressAware {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MulticastFrameMessage.class); // NOPMD

   /**
    * A collection of nodes already visited by this message. It is used to avoid circular sending.
    */
   private HashSet<ClusterNodeAddress> visited = null; // NOPMD

   /**
    * Sender's InetAddress.
    *
    * @see SenderInetAddressAware
    */
   private transient InetAddress senderInetAddress;

   /**
    * A flag indicating that this message comes from the originator of the frame.
    */
   private boolean originator;

   /**
    * The multicast frame carries by this MulticastFrameMessage.
    */
   private Frame frame = null;

   /**
    * The flag indicating if the receiver of the message must send the message to known addresses. This flag is used to
    * prevent double forwarding of loopback messages.
    */
   private boolean sendToKnownAddresses = true;


   /**
    * Required by Externalizable.
    */
   public MulticastFrameMessage() {

      super(TYPE_MULTICAST_FRAME_MESSAGE);
      setRequiresSameCluster(false);
   }


   protected ProcessorKey getProcessorKey() {

      return ClusterProcessorKey.getInstance();
   }


   /**
    * When a cluster processor executes the message, it sends it to all known receivers except those that this message
    * has visited.
    */
   public void execute() {


      // Set sender's Inet address
      if (originator) {

         // Sender address means only when the sender send the frame
         frame.setSenderInetAddress(senderInetAddress);
      }


      // Receive it self
      final ClusterProcessor clusterProcessor = (ClusterProcessor) getProcessor();
      clusterProcessor.receiveFrame(frame);


      // Send to known processors
      final ClusterProcessorState processorState = clusterProcessor.getProcessorState();
      final HashSet<ClusterNodeAddress> knownReceivers = processorState.getKnownReceivers();
      if (sendToKnownAddresses) {

         knownReceivers.forEach(new ObjectProcedure<ClusterNodeAddress>() {

            public boolean execute(final ClusterNodeAddress knownReceiver) {

               if (getSender().equals(knownReceiver) || getVisited().contains(knownReceiver)) {
                  return true; // Continue
               }

               // Create message
               final MulticastFrameMessage multicastFrameMessage = new MulticastFrameMessage();
               multicastFrameMessage.getVisited().add(clusterProcessor.getAddress());
               multicastFrameMessage.getVisited().addAll(getVisited());
               multicastFrameMessage.setReceiver(knownReceiver);
               multicastFrameMessage.setOriginator(false);
               multicastFrameMessage.setFrame(frame);

               //
               clusterProcessor.post(multicastFrameMessage);

               return true;
            }
         });
      }


      // Register sender in known receivers
      knownReceivers.add(getSender());
   }


   public void setFrame(final Frame frame) {

      this.frame = frame;
   }


   Frame getFrame() {

      return frame;
   }


   /**
    * Returns or creates visited set if it was not initialized.
    *
    * @return a visited set.
    */
   private HashSet<ClusterNodeAddress> getVisited() { // NOPMD

      if (visited == null) {

         visited = new HashSet<ClusterNodeAddress>(1);
      }

      return visited;
   }


   public void setOriginator(final boolean originator) {

      this.originator = originator;
   }


   boolean isOriginator() {

      return originator;
   }


   /**
    * Sets an IP address of the machine that sent this frame. The IP address of the sender is set only if this is a
    * received message and it was received from a remote machine.
    *
    * @param senderInetAddress the IP address of the machine that sent this message.
    */
   public void setSenderInetAddress(final InetAddress senderInetAddress) {

      this.senderInetAddress = senderInetAddress;
   }


   InetAddress getSenderInetAddress() {

      return senderInetAddress;
   }


   /**
    * Sets the flag indicating if the receiver of the message must send the message to known addresses.
    *
    * @param sendToKnownAddresses the flag indicating if the receiver of the message must send the message to known
    *                             addresses.
    */
   public void setSendToKnownAddresses(final boolean sendToKnownAddresses) {

      this.sendToKnownAddresses = sendToKnownAddresses;
   }


   /**
    * Returns the flag indicating if the receiver of the message must send the message to known addresses.
    *
    * @return the flag indicating if the receiver of the message must send the message to known addresses.
    */
   boolean isSendToKnownAddresses() {

      return sendToKnownAddresses;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      out.writeBoolean(originator);
      out.writeBoolean(sendToKnownAddresses);

      if (frame == null) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);
         frame.write(out);
      }


      if (visited == null) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);
         out.writeInt(visited.size());
         for (final ClusterNodeAddress visitedAddress : visited) {

            SerializerUtils.writeAddress(visitedAddress, out);
         }
      }
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      originator = in.readBoolean();
      sendToKnownAddresses = in.readBoolean();

      if (in.readBoolean()) {

         frame = null;
      } else {

         frame = new Frame();
         frame.read(in);
      }


      if (in.readBoolean()) {

         visited = null;
      } else {

         final int visitedSize = in.readInt();
         visited = new HashSet<ClusterNodeAddress>(visitedSize);
         for (int i = 0; i < visitedSize; i++) {

            visited.add(SerializerUtils.readAddress(in));
         }
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final MulticastFrameMessage message = (MulticastFrameMessage) o;

      if (originator != message.originator) {
         return false;
      }
      if (sendToKnownAddresses != message.sendToKnownAddresses) {
         return false;
      }
      if (frame != null ? !frame.equals(message.frame) : message.frame != null) {
         return false;
      }
      if (senderInetAddress != null ? !senderInetAddress.equals(message.senderInetAddress) : message.senderInetAddress != null) {
         return false;
      }
      if (visited != null ? !visited.equals(message.visited) : message.visited != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (visited != null ? visited.hashCode() : 0);
      result = 31 * result + (senderInetAddress != null ? senderInetAddress.hashCode() : 0);
      result = 31 * result + (originator ? 1 : 0);
      result = 31 * result + (frame != null ? frame.hashCode() : 0);
      result = 31 * result + (sendToKnownAddresses ? 1 : 0);
      return result;
   }


   public String toString() {

      return "MulticastFrameMessage{" +
              "originator=" + originator +
              ", sendToKnownAddresses=" + sendToKnownAddresses +
              ", senderInetAddress=" + senderInetAddress +
              ", visited=" + visited +
              ", frame=" + frame +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new MulticastFrameMessage();
      }
   }
}
