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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Request is sent to the receiver. Request holds requests data and meta data associated with it. Request is an
 * equivalent of the procedure call in RPC.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection PointlessArithmeticExpression, RedundantIfStatement, NonFinalFieldReferenceInEquals,
 * NonFinalFieldReferencedInHashCode, NonFinalFieldReferencedInHashCode, ParameterNameDiffersFromOverriddenParameter,
 * ConstantNamingConvention, TransientFieldInNonSerializableClass
 * @since Mar 27, 2008 12:18:08 AM
 */
public abstract class Message extends Command implements Wireable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Message.class); // NOPMD

   /**
    * Request type.
    */
   private int type = TYPE_UNDEFINED;


   /**
    * A reading of the system clock at the time of sending this request. This reading is used to synchronize clocks of
    * the cluster nodes.
    */
   private Time timestamp = null;


   /**
    * UUID of the cluster.
    */
   private UUID clusterUUID = null;

   /**
    * Sender address.
    */
   private ClusterNodeAddress sender = null;

   /**
    * Receiver of this message.
    *
    * @see ClusterNodeAddress
    */
   private ReceiverAddress receiver = null; // NOPMD


   private transient RequestProcessor processor = null;

   /**
    * If true, this message can be processed only inside the same cluster. If the message requires the same cluster and
    * it is not, it will either be ignored or sent a retry response.
    */
   private boolean requiresSameCluster = true;


   /**
    * Required by Externalizable.
    */
   public Message() {

   }


   /**
    * Constructor.
    *
    * @param type type
    */
   public Message(final int type) {

      this.type = type;
   }


   /**
    * Returns a key of the destination processor.
    *
    * @return the key of the destination processor. Router uses it to locate a local processor.
    */
   protected abstract ProcessorKey getProcessorKey();


   /**
    * Returns a time stamp that was set using <code>setTimeStamp()</code> before a message was sent.
    *
    * @return the time stamp that was set using <code>setTimeStamp()</code> before a message was sent.
    */
   public final Time getTimestamp() {

      return timestamp;
   }


   /**
    * Sets the current time.
    *
    * @param timestamp the current time.
    */
   public final void setTimestamp(final Time timestamp) {

      this.timestamp = timestamp;
   }


   /**
    * Return cluster ID.
    *
    * @return cluster ID.
    */
   public final UUID getClusterUUID() {

      return clusterUUID;
   }


   /**
    * Sets cluster ID.
    *
    * @param clusterUUID to set
    */
   public final void setClusterUUID(final UUID clusterUUID) {

      this.clusterUUID = clusterUUID;
   }


   /**
    * Returns a sender address.
    *
    * @return the sender address.
    */
   public final ClusterNodeAddress getSender() {

      return sender;
   }


   public final void setSender(final ClusterNodeAddress sender) {

      this.sender = sender;
   }


   /**
    * Returns wireable type.
    *
    * @return wireable type.
    */
   public final int getWireableType() {

      return type;
   }


   /**
    * Returns destination of this request.
    *
    * @return destination type.
    */
   public final int getDestination() {

      return convertTypeToDestination(type);
   }


   protected static int convertTypeToDestination(final int type) {

      return type >> 8;
   }


   /**
    * Returns <code>true</code> if sender is set.
    *
    * @return <code>true</code> if sender is set.
    */
   public final boolean isSenderSet() {

      return sender != null;
   }


   public final boolean isContextSet() {

      return processor != null;
   }


   /**
    * Validates the message. This callback method is executed right after when this message is posted to message
    * processor. The main purpose of this method to provide a formal protection from predictable programmer's errors.
    * Children of <code>Message</code> may extend it to provide a more specific validation. When overriding validate(),
    * children must call super first.
    *
    * @throws InvalidMessageException if this message is invalid.
    */
   public void validate() throws InvalidMessageException {

      if (type == TYPE_UNDEFINED) {
         throw new InvalidMessageException("Message type is undefined: " + type);
      }
      if (clusterUUID == null) {
         throw new InvalidMessageException("Cluster UUID is not set: " + this);
      }
   }


   /**
    * Sets a single receiver.
    *
    * @param receiverAddress the single receiver.
    */
   public final void setReceiver(final ReceiverAddress receiverAddress) {

      this.receiver = receiverAddress;
   }


   /**
    * Returns an unmodifiable set of receivers of this message.
    *
    * @return a set of receivers.
    * @see ClusterNodeAddress
    */
   public final ReceiverAddress getReceiver() {

      return receiver;
   }


   public void setReceiver(final ClusterNodeAddress clusterNodeAddress) {

      final int tcpPort = clusterNodeAddress.getTcpPort();
      final InetAddress[] addresses = clusterNodeAddress.getAddresses();
      receiver = new ReceiverAddress(addresses, tcpPort);
   }


   /**
    * Sets a context processor.
    *
    * @param processor the context processor.
    * @see RequestProcessor#processMessage(Message)
    */
   public final void setProcessor(final RequestProcessor processor) {

      this.processor = processor;
   }


   /**
    * Returns a context processor.
    *
    * @return the context processor.
    */
   public final RequestProcessor getProcessor() {

      return processor;
   }


   /**
    * Returns <code>true</code> if at least one receiver is set.
    *
    * @return <code>true</code> if at least one receiver is set.
    */
   public final boolean isReceiverSet() {

      return receiver != null;
   }


   /**
    * Returns <code>true</code> if this message can be processed only inside the same cluster. If the message requires
    * the same cluster and it is not, it will either be ignored or sent a retry response.
    * <p/>
    * Default is true.
    *
    * @return <code>true</code> if this message can be processed only inside the same cluster. If the message requires
    *         the same cluster and it is not, it will either be ignored or sent a retry response.
    */
   public final boolean isRequiresSameCluster() {

      return requiresSameCluster;
   }


   /**
    * Set to <code>true</code> if this message can be processed only inside the same cluster. If the message requires
    * the same cluster and it is not, it will either be ignored or sent a retry response.
    *
    * @param requiresSameCluster <code>true</code> if this message can be processed only inside the same cluster.
    */
   public final void setRequiresSameCluster(final boolean requiresSameCluster) {

      this.requiresSameCluster = requiresSameCluster;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.writeInt(type);
      out.writeBoolean(requiresSameCluster);
      SerializerUtils.writeTime(timestamp, out);
      SerializerUtils.writeUuid(clusterUUID, out);
      SerializerUtils.writeAddress(sender, out);
      SerializerUtils.writeReceiverAddress(receiver, out);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      type = in.readInt();
      requiresSameCluster = in.readBoolean();
      timestamp = SerializerUtils.readTime(in);
      clusterUUID = SerializerUtils.readUuid(in);
      sender = SerializerUtils.readAddress(in);
      receiver = SerializerUtils.readReceiverAddress(in);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final Message message = (Message) o;

      if (requiresSameCluster != message.requiresSameCluster) {
         return false;
      }
      if (type != message.type) {
         return false;
      }
      if (clusterUUID != null ? !clusterUUID.equals(message.clusterUUID) : message.clusterUUID != null) {
         return false;
      }
      if (receiver != null ? !receiver.equals(message.receiver) : message.receiver != null) {
         return false;
      }
      if (sender != null ? !sender.equals(message.sender) : message.sender != null) {
         return false;
      }
      if (timestamp != null ? !timestamp.equals(message.timestamp) : message.timestamp != null) {
         return false;
      }
      return true;
   }


   public int hashCode() {

      int result = type;
      result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
      result = 31 * result + (clusterUUID != null ? clusterUUID.hashCode() : 0);
      result = 31 * result + (sender != null ? sender.hashCode() : 0);
      result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
      result = 31 * result + (requiresSameCluster ? 1 : 0);
      return result;
   }


   public String toString() {

      return "Message{" +
              "sender=" + sender +
              ", receiver=" + receiver +
              ", clusterUUID=" + clusterUUID +
              ", type=" + type +
              ", timestamp=" + timestamp +
              '}';
   }
}
