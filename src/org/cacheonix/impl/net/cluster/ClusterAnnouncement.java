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
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.SenderInetAddressAware;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A cluster announcement is sent periodically by members of the cluster to announce its belonging to a  live cluster.
 * <p/>
 * The cluster announcement is used by new cluster members to obtain information about availability of a live cluster
 * and members of the live cluster that can be used to join it.
 * <p/>
 * The Cluster announcement can be set by clusters in Normal and in Blocked state.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 1, 2008 6:20:20 PM
 */
@SuppressWarnings("RedundantIfStatement")
public final class ClusterAnnouncement extends ClusterMessage implements SenderInetAddressAware {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterAnnouncement.class); // NOPMD


   /**
    * Marker list representative.
    */
   private ClusterNodeAddress representative = null;


   /**
    * Indicates if the sending cluster is a member
    */
   private boolean operationalCluster = false;

   /**
    * Marker list size.
    */
   private int markerListSize;

   /**
    * An IP address of the machine that sent this frame. This field is set only if this is a received frame and it was
    * received from a remote machine.
    */
   private InetAddress senderInetAddress = null;

   /**
    * Name of the cluster that sent the cluster announcement.
    */
   private String clusterName = null;


   /**
    * Required by Externalizable.
    *
    * @noinspection WeakerAccess
    */
   public ClusterAnnouncement() {

      super(TYPE_CLUSTER_ANNOUNCEMENT);
   }


   public ClusterAnnouncement(final String clusterName, final ClusterNodeAddress announcer,
                              final boolean operationalCluster, final int markerListSize,
                              final ClusterNodeAddress representative) {

      super(TYPE_CLUSTER_ANNOUNCEMENT);
      setRequiresSameCluster(false);
      setSender(announcer);
      this.operationalCluster = operationalCluster;
      this.markerListSize = markerListSize;
      this.representative = representative;
      this.clusterName = clusterName;
   }


   /**
    * {@inheritDoc}
    */
   public void setSenderInetAddress(final InetAddress senderInetAddress) {

      this.senderInetAddress = senderInetAddress;
   }


   public ClusterNodeAddress getRepresentative() {

      return representative;
   }


   public boolean isOperationalCluster() {

      return operationalCluster;
   }


   public int getMarkerListSize() {

      return markerListSize;
   }


   /**
    * {@inheritDoc}
    */
   public void validate() throws InvalidMessageException {

      super.validate();
      if (isReceiverSet()) {
         throw new InvalidMessageException("Receivers should be empty for this is multicast message.");
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * Processes a cluster announcement. We will join other cluster if the announcer if it is a bigger cluster or it is
    * of the same size but their representative if bigger than ours.
    */
   protected void processNormal() {

      final ClusterProcessor processor = getClusterProcessor();

      final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();

      // Check if can evaluate viability of join
      if (senderInetAddress == null) {
         return;
      }

      // Check if it has a proper cluster name

      if (!processor.getProcessorState().getClusterName().equals(clusterName)) {
         return;
      }

      if (getClusterUUID().equals(processor.getProcessorState().getClusterView().getClusterUUID())) {
         return;
      }

      // Ignore our own announcement
      if (getSender().equals(processor.getAddress())) {
         return;
      }

      // Check if both are non-local, or both local and actual sender address is in the list of local addresses
      final boolean processorIsLoopbackOnly = processor.getAddress().isLoopbackOnly();
      final boolean announcerIsLoopbackOnly = getSender().isLoopbackOnly();
      final boolean bothAreRemote = !(processorIsLoopbackOnly || announcerIsLoopbackOnly);
      final boolean bothAreLocal = processorIsLoopbackOnly && announcerIsLoopbackOnly;
      if (!(bothAreRemote || (bothAreLocal && processor.getLocalInetAddresses().contains(senderInetAddress)))) {

         // Ignore if this processor is loopback-only, and this is a message from an non-local machine
         return;
      }

      // Register the announcement detail in the join status
      joinStatus.registerObservation(new ObservedClusterNode(getClusterUUID(), markerListSize,
              representative, operationalCluster, getSender()));
   }


   /**
    * {@inheritDoc}
    */
   protected void processBlocked() {

      processNormal();

   }


   /**
    * {@inheritDoc}
    */
   protected void processRecovery() {

      processNormal();
   }


   /**
    * {@inheritDoc}
    */
   protected void processCleanup() {

      processNormal();
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      representative = SerializerUtils.readAddress(in);
      clusterName = SerializerUtils.readString(in);
      operationalCluster = in.readBoolean();
      markerListSize = in.readInt();
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeAddress(representative, out);
      SerializerUtils.writeString(clusterName, out);

      out.writeBoolean(operationalCluster);
      out.writeInt(markerListSize);
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

      final ClusterAnnouncement that = (ClusterAnnouncement) o;

      if (markerListSize != that.markerListSize) {
         return false;
      }
      if (operationalCluster != that.operationalCluster) {
         return false;
      }
      if (clusterName != null ? !clusterName.equals(that.clusterName) : that.clusterName != null) {
         return false;
      }
      if (representative != null ? !representative.equals(that.representative) : that.representative != null) {
         return false;
      }
      if (senderInetAddress != null ? !senderInetAddress.equals(that.senderInetAddress) : that.senderInetAddress != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (representative != null ? representative.hashCode() : 0);
      result = 31 * result + (operationalCluster ? 1 : 0);
      result = 31 * result + markerListSize;
      result = 31 * result + (senderInetAddress != null ? senderInetAddress.hashCode() : 0);
      result = 31 * result + (clusterName != null ? clusterName.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "ClusterAnnouncement{" +
              "announcer=" + getSender() +
              ", representative=" + representative +
              ", operationalCluster=" + operationalCluster +
              ", markerListSize=" + markerListSize +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClusterAnnouncement();
      }
   }
}
