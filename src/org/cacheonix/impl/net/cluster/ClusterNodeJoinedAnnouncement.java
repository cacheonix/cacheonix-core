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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.cacheonix.cluster.ClusterConfiguration;
import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.cluster.ClusterMember;
import org.cacheonix.cluster.ClusterMemberJoinedEvent;
import org.cacheonix.impl.cluster.ClusterMemberJoinedEventImpl;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

import static java.util.Collections.singletonList;
import static org.cacheonix.impl.cluster.ClusterEventUtil.createClusterMember;
import static org.cacheonix.impl.cluster.ClusterEventUtil.getUserClusterConfiguration;

/**
 * A message that every cluster processor sends to self when a cluster node joins.
 * <p/>
 * This message is used to support total order where the reliable mcast messages are ordered with changes in the cluster
 * configuration.
 *
 * @see CleanupMarker#processCleanup()
 * @see MulticastMarker#finishJoin()
 * @see MulticastMarker#processNormalNormal()
 * @see MulticastMarker#processCleanup()
 * @see MulticastMarker#sendJoinedToSelf(long, ClusterNodeAddress)
 */
public final class ClusterNodeJoinedAnnouncement extends Message {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MarkerRequest.class); // NOPMD


   private ClusterNodeAddress joined = null;


   /**
    * Required by Wireable
    */
   public ClusterNodeJoinedAnnouncement() {

      super(TYPE_NODE_JOINED_MESSAGE);
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return ReplicatedStateProcessorKey.getInstance();
   }


   public void setJoined(final ClusterNodeAddress joined) {

      this.joined = joined;
   }


   ClusterNodeAddress getJoined() {

      return joined;
   }


   public void execute() {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Executing cluster node joined message: " + joined); // NOPMD

      final ClusterProcessor processor = (ClusterProcessor) getProcessor();
      final MulticastMessageListenerList multicastMessageListeners = processor.getMulticastMessageListeners();

      // Request reset of the joined node is self
      if (processor.getAddress().equals(joined)) {

         multicastMessageListeners.notifyReset();
      }

      // Notify mcast listeners about join
      multicastMessageListeners.notifyNodesJoined(singletonList(joined));

      // Notify cluster event subscribers
      notifyClusterEventSubscribersMemberJoined();

      // Request reset of the joined node is self
      if (processor.getAddress().equals(joined)) {

         // REVIEWME: simeshev@cacheonix.org - 2011-12-12 -> Not necessarily
         // was blocked, for instance, moved from a smaller cluster.
         multicastMessageListeners.notifyNodeUnblocked();
      }
   }


   /**
    * Notifies cluster event subscribers that a member joined.
    */
   private void notifyClusterEventSubscribersMemberJoined() {

      // Get cluster processor state
      final ClusterProcessor processor = (ClusterProcessor) getProcessor();
      final ClusterProcessorState processorState = processor.getProcessorState();

      // Create cluster configuration
      final ClusterView clusterView = processorState.getClusterView();
      final String clusterName = processorState.getClusterName();
      final int state = processorState.getState();
      final ClusterConfiguration clusterConfiguration = getUserClusterConfiguration(clusterName, state, clusterView);

      // Populate a list of joined members
      final ClusterMember clusterMember = createClusterMember(clusterName, joined);
      final List<ClusterMember> joinedMembers = singletonList(clusterMember);

      final List<ClusterEventSubscriber> clusterEventSubscribers = processorState.getClusterEventSubscribers();
      for (final ClusterEventSubscriber clusterEventSubscriber : clusterEventSubscribers) {

         try {

            final ClusterMemberJoinedEvent clusterMemberJoinedEvent = new ClusterMemberJoinedEventImpl(
                    clusterConfiguration, joinedMembers);
            clusterEventSubscriber.notifyClusterMemberJoined(clusterMemberJoinedEvent);
         } catch (final Throwable e) { // NOPMD A catch statement should never catch throwable since it includes errors.

            // Catch user all errors
            LOG.warn("Error while notifying a subscriber that a cluster member joined: " + e, e);
         }
      }
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      joined = SerializerUtils.readAddress(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      SerializerUtils.writeAddress(joined, out);
   }


   @SuppressWarnings("RedundantIfStatement")
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

      final ClusterNodeJoinedAnnouncement that = (ClusterNodeJoinedAnnouncement) o;

      if (joined != null ? !joined.equals(that.joined) : that.joined != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (joined != null ? joined.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "ClusterNodeJoinedAnnouncement{" +
              "joined=" + joined +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClusterNodeJoinedAnnouncement();
      }
   }
}
