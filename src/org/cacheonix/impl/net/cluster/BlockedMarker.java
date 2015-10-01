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
import java.util.LinkedList;

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_BLOCKED;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_CLEANUP;
import static org.cacheonix.impl.net.processor.Response.RESULT_ERROR;
import static org.cacheonix.impl.net.processor.Response.RESULT_SUCCESS;

/**
 * Blocked marker is sent in a ring that transitioned from the Recover state to blocked state. The goal of the blocked
 * marker is to maintain the marker list for the situation when a new member joins the ring and forms the majority or a
 * majority ring becomes accessible.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 16, 2008 9:26:04 PM
 */
public final class BlockedMarker extends OperationalMarker {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BlockedMarker.class); // NOPMD

   private int targetMajorityClusterSize = Integer.MAX_VALUE;


   public BlockedMarker() {

      super(TYPE_CLUSTER_BLOCKED_MARKER);
      setRequiresSameCluster(false);
   }


   public BlockedMarker(final UUID clusterUUID) {

      super(TYPE_CLUSTER_BLOCKED_MARKER, clusterUUID);
      setRequiresSameCluster(false);
   }


   public int getTargetMajorityClusterSize() {

      return targetMajorityClusterSize;
   }


   public void setTargetMajorityClusterSize(final int targetMajorityClusterSize) {

      this.targetMajorityClusterSize = targetMajorityClusterSize;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation does nothing.
    */
   public void finishJoin() {

      // Do nothing
   }


   public void forward() throws InterruptedException {

      final ClusterProcessor processor = getClusterProcessor();
      final ClusterNodeAddress self = processor.getAddress();

      final ClusterNodeAddress nextElement = processor.getProcessorState().getClusterView().getNextElement();
      setReceiver(nextElement);

      if (isJoiningNodeSet() && nextElement.equals(getJoiningNode().getAddress())) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Sending first blocked marker to joined: " + this);
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Process leaving
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      boolean left = false;
      if (isLeaveSet()) {

         if (self.equals(getLeave())) {

            // This node is leaving and Leave marker has returned back to us
            if (LOG.isDebugEnabled()) {
               LOG.debug("Leave marker returned to us:" + getLeave());
            }

            left = true;
            setLeave(null);
         } else {

            // Track progress of the other node leaving
            if (!processor.getProcessorState().getClusterView().contains(getLeave())) {
               // Leaving node is already gone
               setLeave(null);
            }
         }
      } else {

         // Marker is not serving a leave
         if (processor.isShuttingDown()) {

            // The shutdown has been requested and we have to begin the process of gracefully leaving the cluster
            if (LOG.isDebugEnabled()) {
               LOG.debug("This node is shutting down, initiate leave: " + self.getTcpPort());
            }

            setLeave(self);
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Forward
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

      // The response is not if this node has left becuase it won't be able to process it.
      // This may cause unnecessary timeouts.
      setResponseRequired(!left);

      // First post marker to guarantee that it is in the queue. This is needed because
      // the post-processing logic may post shutdown command which may prevent this marker
      // being forwarded.
      processor.post(this);

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Post-process leaving
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      if (left) {

         if (LOG.isDebugEnabled()) {
            LOG.debug("This node has left: " + self.getTcpPort());
         }


         // Now enqueue the shutdown command. Anything posted after this may not be executed.
         processor.enqueue(new ShutdownClusterProcessorCommand(processor));

      } else {
         if (isLeaveSet() && !self.equals(getLeave())) {
            // Other left, adjust the cluster view
            if (LOG.isDebugEnabled()) {
               LOG.debug("Other node has left: " + getLeave().getTcpPort());
            }

            processor.getProcessorState().getClusterView().remove(getLeave());
         }
      }
   }


   /**
    * {@inheritDoc}
    */
   public void rollbackJoin() {

      clearJoin();
   }


   /**
    * {@inheritDoc}
    */
   protected void processClusterAnnouncements() {

      final ClusterProcessor processor = getClusterProcessor();

      final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();

      final ObservedClusterNode strongestObservedClusterNode = joinStatus.getStrongestObservedClusterNode();
      if (strongestObservedClusterNode == null) {
         return;
      }

      // Ignore if we are already joining
      if (joinStatus.isJoining()) {
         return;
      }

      // Ignore if we are servicing a join
      // NOPMD
      if (!processor.getProcessorState().getJoinRequests().isEmpty()) {
         return;
      }

      // Check if already leaving
      if (processor.isShuttingDown()) {
         return;
      }

      // Check if this is an originator

      final ClusterView clusterView = processor.getProcessorState().getClusterView();
      final int ourMarkerListSize = clusterView.getSize();
      if (clusterView.isRepresentative() && ourMarkerListSize > 1) {

         // Ignore, the representative will leave only when it is alone
         return;
      }

      // Check if this is an announcement from our own cluster

      if (strongestObservedClusterNode.getClusterUUID().equals(
              processor.getProcessorState().getClusterView().getClusterUUID())) {
         return;
      }

      // Check if still surveying the area
      if (!joinStatus.clusterSurveyTimeoutExpired()) {
         return;
      }


      // Process
      final ClusterNodeAddress theirSenderAddress = strongestObservedClusterNode.getSenderAddress();
      if (strongestObservedClusterNode.isOperationalCluster()) {

         // This node is blocked and their cluster is operational
         initiateJoinTo(theirSenderAddress);
      } else {

         // Do we have members other than us?
         final int theirMarkerListSize = strongestObservedClusterNode.getMarkerListSize();
         if (ourMarkerListSize > 1) {

            // Check if the announcer is a singleton. It will join us when it hears our announcement.
            if (theirMarkerListSize > 1) {

               // This is other blocked ring.  Check if our representative is bigger then theirs.
               // Their guys will try to join us after they hear our announcement.
               final ClusterNodeAddress ourRepresentative = clusterView.getRepresentative();
               final ClusterNodeAddress theirRepresentative = strongestObservedClusterNode.getRepresentative();
               if (ourRepresentative.compareTo(theirRepresentative) < 0) {

                  // Their representative is bigger then ours, join them
                  initiateJoinTo(theirSenderAddress);
               }
            }
         } else {

            // We are a singleton
            if (theirMarkerListSize > 1) {

               // The announcer is not a singleton. Join the existing ring.
               initiateJoinTo(theirSenderAddress);
            } else {

               // Check if we are bigger then the other guy. The other guy will
               // try to join us after it hears our announcement.
               final ClusterNodeAddress self = processor.getAddress();
               if (self.compareTo(theirSenderAddress) < 0) {

                  // We are smaller then the other guy, join
                  initiateJoinTo(theirSenderAddress);
               }
            }
         }
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void processNormal() {

      // Normal state may receive a blocked marker the node that was
      // a member of a blocked cluster just joined to the majority.
      // We need to respond with an error if the sender is not this
      // cluster node.
      //
      // We need to destroy this marker by not doing anything if
      // the sender is this cluster node. This is possible when
      // the node was alone before joining other cluster.
      if (getProcessor().getAddress().equals(getSender())) {

         getProcessor().post(createResponse(RESULT_SUCCESS));
      } else {

         final String errorResult = "Received Blocked marker while in Normal state: " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response errorResponse = createResponse(RESULT_ERROR);
         errorResponse.setResult(errorResult);
         getProcessor().post(errorResponse);
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * Proof that it is not possible that the Predecessor is gone while Joining is still set:
    * <p/>
    * Precondition:
    * <p/>
    * -------------
    * <p/>
    * An operational marker with Predecessor and Joining set.
    * <p/>
    * Proof for Predecessor death:
    * <p/>
    * ----------------------------
    * <p/>
    * If the Predecessor dies, the recovery protocol will begin to work thus destroying the operational marker with
    * Predecessor and Leaving set.
    * <p/>
    * Proof to Predecessor gracefully leaving:
    * <p/>
    * ----------------------------------------
    * <p/>
    * When the marker with Join set returns to the leaving Predecessor, it processes the join first and resets the Join.
    * Then it processes leave and forwards the marker with leave already reset. See {@link BlockedMarker#forward} for
    * details.
    */
   protected void processBlocked() throws IOException, InterruptedException {

      final ClusterProcessor processor = getClusterProcessor();
      final BlockedMarker blockedMarker = copy();
      final ClusterNodeAddress self = processor.getAddress();

      final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();

      // Begin joining if there are proper cluster announcements.
      processClusterAnnouncements();

      // NOTE: simeshev@cacheonix.org - 2010-02-09 - When joining, it is possible that there
      // are two blocked markers circulating. One is of previous smaller cluster and one is
      // for the bigger that the node has just joined. This can lead to problems. The node
      // ignores a marker that has now become a foreign marker.

      if (!processor.getProcessorState().getClusterView().getClusterUUID().equals(getClusterUUID())) {


         if (joinStatus.isJoining() && joinStatus.isReceivedMarkerList()
                 && joinStatus.getJoiningToCluster().getClusterUUID().equals(getClusterUUID())) {

            LOG.debug("Received first blocked marker from the cluster we are joining: " + blockedMarker);
         } else {

            if (getProcessor().getAddress().equals(getSender())) {

               LOG.debug("Destroyed old blocked marker: " + this);
               getProcessor().post(createResponse(RESULT_SUCCESS));
            } else {

               LOG.warn("Received a marker from a foreign cluster: " + blockedMarker);
               final Response errorResponse = createResponse(RESULT_ERROR);
               errorResponse.setResult("Received a marker from a foreign cluster: " + blockedMarker);
               processor.post(errorResponse);
            }

            return;
         }
      }


      // Response to marker sender with success
      processor.post(createResponse(RESULT_SUCCESS));

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // Add joined process
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      if (blockedMarker.getPredecessor() != null) {

         if (self.equals(blockedMarker.getPredecessor())) {

            // Join marker returned to us
            if (LOG.isDebugEnabled()) {
               LOG.debug("Blocked marker returned to us: " + self);
            }
            blockedMarker.clearJoin();

            // Check if we reached majority
            if (LOG.isDebugEnabled()) {
               LOG.debug("Checking if we reached majority: " + self);
            }

            if (processor.getProcessorState().getClusterView().getSize() >= targetMajorityClusterSize) {

               // Yes, we have reached majority. Reset the received queue, delivery counters,
               // notify about revival the application and forward the normal marker.
               if (LOG.isDebugEnabled()) {

                  LOG.info(
                          "BBBBBBBBBBBBBBBBBBBb We have majority, new member list size is " + processor.getProcessorState().getClusterView().getSize() + ": " + processor.getProcessorState().getClusterView());
               }

               // Blocked cluster has to run through cleanup because there may be gaps in the
               // message sequence that were created by left nodes.

               beginCleanup();

               // done
               return;
            }
         } else {

            // Predecessor is not this node

            if (blockedMarker.getJoiningNode().getAddress().equals(self)) {

               // We are joining, and this is the first mcast marker we have received. This
               // should happen only after the predecessor has sent us the cluster view.
               processor.getProcessorState().setClusterView(joinStatus.getJoiningToCluster());
               processor.getRouter().setClusterUUID(joinStatus.getJoiningToCluster().getClusterUUID());

               processor.getProcessorState().updateLastOperationalClusterView(
                       joinStatus.getLastOperationalClusterView());

               processor.getProcessorState().getReplicatedState().reset(joinStatus.getReplicatedState());
               processor.getMessageAssembler().setParts(joinStatus.getMessageAssemblerParts());
               joinStatus.clear();


               LOG.debug(
                       "Joined blocked cluster, new cluster configuration: " + processor.getProcessorState().getClusterView());
            } else {

               // Amend the marker list only if this is not us who is joining
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Add joining to our list: " + blockedMarker);
               }

               processor.getProcessorState().getClusterView().insert(blockedMarker.getPredecessor(),
                       blockedMarker.getJoiningNode());
            }
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // Handle join request(s)
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // REVIEWME: simeshev@cacheonix.org - 2008-04-20 - Consider using a single joinRequest
      // instead of a queue. Currently of the majority got formed, pending requests will be abandoned.

      // NOPMD
      final LinkedList<JoiningNode> joinRequests = processor.getProcessorState().getJoinRequests();
      if (!joinRequests.isEmpty()) {

         // NOTE: simeshev@cacheonix.org - 2011-04-13 - It is important to check if this node is in 'Leave' becuase
         // it means that it is in the second state of leaving, and ir shouldn't become a join coordinator because
         // it won't be able to forward the stored marker becuase it may be dead by the time joining node response
         // to the MarkerListRequest. See bug CACHEONIX-307 for more information.
         if (!self.equals(blockedMarker.getLeave())) {

            if (!blockedMarker.isJoiningNodeSet()) {

               final JoiningNode joiningNode = joinRequests.removeFirst();
               final ClusterNodeAddress joiningNodeAddress = joiningNode.getAddress();

               // Check if not already joined

               if (!processor.getProcessorState().getClusterView().contains(joiningNodeAddress)) {

                  // Insert immediately after ourselves

                  processor.getProcessorState().getClusterView().insert(self, joiningNode);

                  // Set up join in the marker
                  blockedMarker.setJoiningNode(joiningNode);
                  blockedMarker.setProcessor(processor); // May be needed at join finishing
                  blockedMarker.setPredecessor(self);

                  // Create marker list

                  final MarkerListRequest markerListRequest = new MarkerListRequest(self,
                          processor.getProcessorState().getClusterView(),
                          processor.getProcessorState().getLastOperationalClusterView(),
                          processor.getProcessorState().getReplicatedState(),
                          processor.getMessageAssembler().getParts());
                  markerListRequest.setReceiver(joiningNodeAddress);

                  // Remember marker to forward
                  ((MarkerListRequest.Waiter) markerListRequest.getWaiter()).setMarkerToForward(blockedMarker);

                  // REVIEWME: slava@cacheonix.org - 2009-12-12 - The request has been made
                  // synchronous to avoid it arriving after the first mcast marker is sent
                  // to the joining node. This would not be necessary if the channel was FIFO.
                  // Right now multiple threads process outbound messages, so violation
                  // of order is possible in async processing. See CACHEONIX-141 for
                  // more information.
                  processor.post(markerListRequest);

                  if (LOG.isDebugEnabled()) {
                     LOG.debug("Posted marker list to " + joiningNodeAddress);
                  }

                  // We return because the marker will be sent upon receiving a response
                  // to MarkerListRequest. See MarkerListMessage.Waiter for more information.
                  return;
               }
            }
         }
      }


      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Send cluster announcement if necessary
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

      if (!processor.isShuttingDown() && !processor.getProcessorState().getJoinStatus().isJoining()) {

         final Time currentTime = processor.getClock().currentTime();
         if (currentTime.compareTo(blockedMarker.getNextAnnouncementTime()) >= 0) {

            // Reached next announcement time

            blockedMarker.setNextAnnouncementTime(
                    currentTime.add(processor.getProcessorState().getClusterAnnouncementTimeoutMillis()));
            processor.announceCluster(false);
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // Decide if we waited enough to try to form the cluster.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

      if (!joinStatus.isJoining() && processor.getProcessorState().getClusterView().getSize() == 1 && processor.getProcessorState().getHomeAloneTimeout().isExpired()) {

         // Stayed alone enough - switch to operational mode
         if (LOG.isDebugEnabled()) {

            LOG.debug(
                    "Found ourselves alone, begin cleanup: " + self + ", clusterView: " + processor.getProcessorState().getClusterView());
         }

         beginCleanup();

         // Exit
         return;
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // Forward marker
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      blockedMarker.setProcessor(getProcessor());
      blockedMarker.forward();
   }


   /**
    * {@inheritDoc}
    */
   protected void processRecovery() {

      // Recovery state may receive a blocked marker  in two cases:
      // a) Recovery complete but majority was not reached b) The
      // node that was a member of a blocked cluster just
      // joined to the majority. We need to respond with
      // an error if the sender is not this cluster node.
      //
      // We need to destroy this marker by not doing anything if
      // the sender is this cluster node. This is possible when
      // the node was alone before joining other cluster.

      final ClusterProcessor processor = getClusterProcessor();

      if (processor.getProcessorState().getClusterView().getClusterUUID().equals(getClusterUUID())) {

         processor.post(createResponse(RESULT_SUCCESS));

         // Reset join state. Switching to a stable state should clear join state possibly
         // acquired before going through recovery process. See CACHEONIX-279 for details.

         processor.getProcessorState().getJoinStatus().clear();

         // Change state

         final int newState = STATE_BLOCKED;
         processor.getProcessorState().setState(newState);

         processor.getProcessorState().setTargetMajoritySize(targetMajorityClusterSize);
         processor.getMulticastMessageListeners().notifyNodeBlocked();

         processor.getProcessorState().getHomeAloneTimeout().reset();

         // Notify cluster event subscribers
         notifySubscribersClusterStateChanged(newState);

         // REVIEWME: simeshev@cacheonix.org - 2010-07-07 - What are the implications
         // of sending the new marker to self instead of just forwarding it?

         final BlockedMarker blockedMarker = copy();
         blockedMarker.setReceiver(processor.getAddress());
         processor.post(blockedMarker);

      } else {

         if (processor.getAddress().equals(getSender())) {

            LOG.debug("Destroyed old blocked marker: " + this);
            getProcessor().post(createResponse(RESULT_SUCCESS));
         } else {

            final String errorResult = "Ignored blocked marker: " + this;

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

            final Response errorResponse = createResponse(RESULT_ERROR);
            errorResponse.setResult(errorResult);
            getProcessor().post(errorResponse);
         }
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void processCleanup() {

      // Ignore the marker. It is possible that CleanupState receives it, for example if this
      // node was in blocked state alone. It could send the marker to itself and, while it was
      // travelling through the wire, join another blocked node and move to cleanup state.

      if (getProcessor().getAddress().equals(getSender())) {

         LOG.debug("Destroyed old blocked marker: " + this);

         getProcessor().post(createResponse(RESULT_SUCCESS));
      } else {

         final String errorResult = "Ignored blocked marker: " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response errorResponse = createResponse(RESULT_ERROR);
         errorResponse.setResult(errorResult);
         getProcessor().post(errorResponse);
      }
   }


   /**
    * Begins cleanup.
    */
   private void beginCleanup() {

      final ClusterProcessor processor = getClusterProcessor();

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Beginning cleanup round: " + processor.getAddress());

      // Create cleanup marker
      final CleanupMarker cleanupMarker = CleanupMarker.originate(processor);

      // Change state to Cleanup, set as originator.
      final int newState = STATE_CLEANUP;
      processor.getProcessorState().setState(newState);

      // Cancel 'home alone' timeout
      processor.getProcessorState().getHomeAloneTimeout().cancel();

      // Notifies cluster subscribers
      notifySubscribersClusterStateChanged(newState);

      // Forward cleanup marker
      forwardCleanupMarker(cleanupMarker);
   }


   /**
    * Creates a copy of this <code>CleanupMarker</code> suitable for forwarding.
    *
    * @return the copy of this <code>CleanupMarker</code> suitable for forwarding.
    */
   private BlockedMarker copy() {

      final BlockedMarker result = new BlockedMarker();
      result.setTargetMajorityClusterSize(targetMajorityClusterSize);
      result.setNextAnnouncementTime(getNextAnnouncementTime());
      result.setRequiresSameCluster(isRequiresSameCluster());
      result.setPredecessor(getPredecessor());
      result.setJoiningNode(getJoiningNode());
      result.setLeave(getLeave());
      return result;
   }


   /**
    * The object implements the readExternal method to restore its contents by calling the methods of DataInput for
    * primitive types and readObject for objects, strings and arrays.  The readExternal method must read the values in
    * the same sequence and with the same types as were written by writeExternal.
    *
    * @param in the stream to read data from in order to restore the object
    * @throws IOException            if I/O errors occur
    * @throws ClassNotFoundException If the class for an object being restored cannot be found.
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      targetMajorityClusterSize = in.readInt();
   }


   /**
    * The object implements the writeExternal method to save its contents by calling the methods of DataOutput for its
    * primitive values or calling the writeObject method of ObjectOutput for objects, strings, and arrays.
    *
    * @param out the stream to write the object to
    * @throws IOException Includes any I/O exceptions that may occur
    * @serialData Overriding methods should use this tag to describe the data layout of this Externalizable object. List
    * the sequence of element types and, if possible, relate the element to a public/protected field and/or method of
    * this Externalizable class.
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeInt(targetMajorityClusterSize);
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

      final BlockedMarker that = (BlockedMarker) o;

      if (targetMajorityClusterSize != that.targetMajorityClusterSize) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + targetMajorityClusterSize;
      return result;
   }


   public String toString() {

      return "BlockedMarker{" +
              "targetMajorityClusterSize=" + targetMajorityClusterSize +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new BlockedMarker();
      }
   }
}
