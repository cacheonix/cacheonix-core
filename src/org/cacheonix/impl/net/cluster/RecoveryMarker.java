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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_BLOCKED;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_RECOVERY;
import static org.cacheonix.impl.net.processor.Response.RESULT_ERROR;
import static org.cacheonix.impl.net.processor.Response.RESULT_SUCCESS;
import static org.cacheonix.impl.util.CollectionUtils.createList;
import static org.cacheonix.impl.util.CollectionUtils.same;

/**
 * RecoveryMarker
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 5, 2008 6:36:35 PM
 */
@SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
public final class RecoveryMarker extends MarkerRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RecoveryMarker.class); // NOPMD

   private ClusterNodeAddress originator = null;

   private List<JoiningNode> currentList = null;

   private List<JoiningNode> previousList = null;

   private UUID newClusterUUID = null;


   /**
    * Empty constructor required by <code>Wireable</code>.
    *
    * @noinspection WeakerAccess
    * @see Wireable
    */
   public RecoveryMarker() {

      super(TYPE_CLUSTER_RECOVERY_MARKER);
      setRequiresSameCluster(false);
   }


   public RecoveryMarker(final UUID newClusterUUID, final ClusterNodeAddress originator) {

      super(TYPE_CLUSTER_RECOVERY_MARKER);
      this.setRequiresSameCluster(false);
      this.newClusterUUID = newClusterUUID;
      this.originator = originator;
      this.currentList = new ArrayList<JoiningNode>(createList(new JoiningNode(originator)));
      this.previousList = new ArrayList<JoiningNode>(0);
   }


   /**
    * Returns originator of the recovery round.
    *
    * @return originator of the recovery round.
    */
   public ClusterNodeAddress getOriginator() {

      return originator;
   }


   /**
    * Sets originator.
    *
    * @param originator originator.
    */
   public void setOriginator(final ClusterNodeAddress originator) {

      this.originator = originator;
   }


   /**
    * @return the current list
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public List<JoiningNode> getCurrentList() {

      return currentList;
   }


   /**
    * @return the previous list
    * @noinspection ReturnOfCollectionOrArrayField
    */
   List<JoiningNode> getPreviousList() {

      return previousList;
   }


   public void validate() throws InvalidMessageException {

      super.validate();

      if (!isSenderSet()) {
         throw new InvalidMessageException("Sender should be set");
      }

      if (!isReceiverSet()) {
         throw new InvalidMessageException("Receiver should be set");
      }
   }


   protected void processNormal() {

      // NOTE: simeshev@cacheonix.org - 2016-02-22 - Recovery can start at any time by any member
      // of the cluster. We should not check this recovery marker's cluster UUID in the NORMAL
      // state because the recovery marker is created with a new cluster UUID.

      final ClusterProcessor processor = getClusterProcessor();

      // NOTE: slava@cacheonix.org - 2009-12-22 - Make sure that the recovery marker is coming from
      // a member of our cluster. Otherwise it is possible to enter into the RecoveryState with no
      // originator present. This leads into infinite forwarding of the recovery marker. See
      // CACHEONIX-144 for more information.
      final RecoveryMarker recoveryMarker = copy();

      if (!processor.getProcessorState().getClusterView().contains(recoveryMarker.originator)) {

         final String errorResult = "Recovery marker from an unknown originator with address " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response errorResponse = createResponse(RESULT_ERROR);
         errorResponse.setResult(errorResult);
         processor.post(errorResponse);
         return;
      }

      // Respond with success
      processor.post(createResponse(RESULT_SUCCESS));

      if (LOG.isDebugEnabled()) {
         LOG.debug("<><><><><><><><><><><><><><> Created recovery state: " + processor.getAddress().getTcpPort()
                 + ", originator: " + originator);
      }

      final int newState = STATE_RECOVERY;
      processor.getProcessorState().setState(newState);

      // Cancel 'home alone' timeout
      processor.getProcessorState().getHomeAloneTimeout().cancel();

      // Notify cluster event subscribers
      processor.getProcessorState().notifySubscribersClusterStateChanged(newState);

      // NOTE: simeshev@cacheonix.org - 2010-12-23 - it is possible that a node in a Normal
      // state receives a recovery marker with self as an originator. This may happen when
      // a marker timeout occurs while this node is alone, thus initiating recovery for a
      // single node.
      //
      // It is a separate question how a node in a Normal state can lose its own marker. This
      // should not be possible, but, according to CACHEONIX-214, it did happen, see the log
      // at 09:11:29,123. Though there that was a Blocked state.
      //

      processor.getProcessorState().setRecoveryOriginator(processor.getAddress().equals(recoveryMarker.originator));

      // REVIEWME: simeshev@cacheonix.org - 2010-07-06 - What are the implications
      // of sending the new marker to self instead of just forwarding it?

      // Receive marker in recovery state
      recoveryMarker.setReceiver(processor.getAddress());
      processor.post(recoveryMarker);
   }


   /**
    * Process RecoveryMarker while this cluster processor is in Blocked state.
    */
   protected void processBlocked() {

      final ClusterProcessor processor = getClusterProcessor();
      final RecoveryMarker recoveryMarker = copy();

      // NOTE: slava@cacheonix.org - 2009-12-22 - Check if the originator belongs to our cluster.
      // If not, we should ignore the marker because we won't be able to forward it to a node
      // that we don't know about and thus won't be able to complete the recovery. See
      // CACHEONIX-144 for more information.

      if (!processor.getProcessorState().getClusterView().contains(recoveryMarker.originator)) {

         final String errorResult = "Recovery marker from an unknown originator:" + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response errorResponse = createResponse(RESULT_ERROR);
         errorResponse.setResult(errorResult);
         processor.post(errorResponse);
         return;
      }


      // Respond with success
      processor.post(createResponse(RESULT_SUCCESS));

      // Switch to Recovery state
      if (LOG.isDebugEnabled()) {
         LOG.debug("<><><><><><><><><><><><><><> Created recovery state: " + processor.getAddress().getTcpPort()
                 + ", originator: " + originator);
      }

      final int newState = STATE_RECOVERY;
      processor.getProcessorState().setState(newState);

      // Cancel 'home alone' timeout
      processor.getProcessorState().getHomeAloneTimeout().cancel();

      // Notify cluster subscribers
      processor.getProcessorState().notifySubscribersClusterStateChanged(newState);

      // NOTE: simeshev@cacheonix.org - 2010-12-23 - it is possible that a node in a blocked
      // state receives a recovery marker with self as an originator. This may happen when
      // a marker timeout occurs while this node is alone, thus initiating recovery for a
      // single node.
      //
      // It is a separate question how a node in blocked state can lose its own marker. This
      // should not be possible, but, according to CACHEONIX-214, it did happen, see the log
      // at 09:11:29,123.
      //

      processor.getProcessorState().setRecoveryOriginator(processor.getAddress().equals(recoveryMarker.originator));

      // REVIEWME: simeshev@cacheonix.org - 2010-07-06 - What are the implications
      // of sending the new marker to self instead of just forwarding it?

      // Post marker to self in recovery state
      recoveryMarker.setReceiver(processor.getAddress());
      processor.post(recoveryMarker);
   }


   protected void processRecovery() {

      final ClusterNodeAddress self = getProcessor().getAddress();
      final ClusterProcessor processor = getClusterProcessor();
      final RecoveryMarker recoveryMarker = copy();


      // NOTE: slava@cacheonix.org - 2009-12-22 - Check if the originator belongs to our cluster.
      // If not, we should ignore the marker because we won't be able to forward it to a node
      // that we don't know about and thus won't be able to complete the recovery. See
      // CACHEONIX-144 for more information.

      final ClusterProcessorState processorState = processor.getProcessorState();
      if (!processorState.getClusterView().contains(recoveryMarker.originator)) {

         final String errorResult = "Recovery marker from an unknown originator " + originator;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response errorResponse = createResponse(RESULT_ERROR);
         errorResponse.setResult(errorResult);
         processor.post(errorResponse);

         return;
      }

      // Respond with success
      processor.post(createResponse(RESULT_SUCCESS));

      // When a stub receives the marker with a non-empty previousList,
      // it stores Previous list as the marker list
      if (!recoveryMarker.previousList.isEmpty()) {

         if (LOG.isDebugEnabled()) {
            LOG.debug("Store Previous list as the marker list: " + recoveryMarker.previousList);
         }

         processorState.setClusterView(new ClusterViewImpl(recoveryMarker.newClusterUUID,
                 recoveryMarker.originator, recoveryMarker.previousList, self));
         processor.getRouter().setClusterUUID(recoveryMarker.newClusterUUID);
      }

      if (processorState.isRecoveryOriginator()) {

         // Originator of the recovery round
         if (LOG.isDebugEnabled()) {
            LOG.debug(">>>>>>>>>>>>>> This node is an originator of the recovery round: " + self.getTcpPort());
         }

         if (recoveryMarker.originator.equals(self)) {

            // Marker returned to us after one or more rounds
            if (LOG.isDebugEnabled()) {
               LOG.debug("Marker returned to us (" + self.getTcpPort() + ") after one or more rounds: " + this);
            }
            if (same(recoveryMarker.currentList, recoveryMarker.previousList)) {

               LOG.debug("New member list of " + recoveryMarker.currentList.size()
                       + " members has been formed: " + recoveryMarker.currentList);
               // New list formed
               //
               // Check if we have a majority

               if (processorState.getClusterView().hasMajorityOver(
                       processorState.getLastOperationalClusterView()) || processorState.getClusterView().getSize() >= processorState.getTargetMajoritySize()) {

                  // We have majority, begin stage 2 of recovery by creating and forwarding
                  // cleanup marker
                  if (LOG.isDebugEnabled()) {

                     LOG.info("We have majority, new member list size is " + recoveryMarker.currentList.size() + ": "
                             + processorState.getClusterView());
                  }

                  // Begin cleanup
                  beginCleanup();

                  return;
               } else {

                  if (LOG.isDebugEnabled()) {

                     LOG.debug("We do not have majority (target size is " + processorState.getTargetMajoritySize()
                             + ") , new marker list size is " + processorState.getClusterView().getSize()
                             + ": " + processorState.getClusterView());
                  }

                  // Switch to blocked state

                  beginBlocking(processorState.getTargetMajoritySize());

                  return;
               }
            } else {

               // Not the same, start another recovery round
               if (LOG.isDebugEnabled()) {
                  LOG.debug("R-R-R-R-R-R-R-R-R Current and Previous are not the same, starting another recovery round: "
                          + self.getTcpPort());
               }
               recoveryMarker.previousList.clear();
               recoveryMarker.previousList.addAll(recoveryMarker.currentList);
               processorState.setClusterView(
                       new ClusterViewImpl(recoveryMarker.newClusterUUID, self, recoveryMarker.previousList, self));
               processor.getRouter().setClusterUUID(recoveryMarker.newClusterUUID);
            }
         } else if (self.compareTo(recoveryMarker.originator) > 0) {

            // Destroy other recovery marker
            if (LOG.isDebugEnabled()) {
               LOG.debug("R-R-R-R-R-R-R-R-R Destroyed other recovery marker: " + recoveryMarker);
            }

            return;
         } else {

            // Not an originator of this recovery round, *our* marker will be destroyed some time
            // later
            if (LOG.isDebugEnabled()) {
               LOG.debug("Not an originator (" + recoveryMarker.originator.getTcpPort() + ')'
                       + " of this recovery round, *our* marker will be destroyed some time, later: " + self.getTcpPort());
            }
            if (recoveryMarker.previousList.isEmpty()) {

               // Append to forward list and forward

               recoveryMarker.currentList.add(new JoiningNode(self));

               if (LOG.isDebugEnabled()) {
                  LOG.debug("Appended self to the current list for forward: " + recoveryMarker.currentList);
               }
            }
         }
      } else {

         // Not an originator of the recovery round
         if (LOG.isDebugEnabled()) {
            LOG.debug("Not an originator (" + recoveryMarker.originator.getTcpPort() + ')'
                    + " of the recovery round: " + self.getTcpPort());
         }


         if (recoveryMarker.previousList.isEmpty()) {

            // Means that this is the round of *detecting* the new list - append to the current list and forward

            recoveryMarker.currentList.add(new JoiningNode(self));

            if (LOG.isDebugEnabled()) {
               LOG.debug("Appended self to the current list for forward: " + recoveryMarker.currentList);
            }
         }
      }

      // ----------------------------------------------------
      // Forward recovery marker
      // ----------------------------------------------------

      final ClusterNodeAddress nextProcess = processorState.getClusterView().getNextElement();

//      if (nextProcess.equals(getContext().getAddress())) {
//         // Handle when only us left
//         beginBlocking(context.getTargetMajoritySize());
//      } else {
      // Just forward to next
      recoveryMarker.setReceiver(nextProcess);
      processor.post(recoveryMarker);
//      }
   }


   /**
    * {@inheritDoc}
    */
   protected void processCleanup() {

      final ClusterProcessor processor = getClusterProcessor();

      // NOTE: slava@cacheonix.org - 2009-12-22 - Check if the originator belongs to our cluster.
      // If not, we should ignore the marker because we won't be able to forward it to a node
      // that we don't know about and thus won't be able to complete the recovery. See
      // CACHEONIX-144 for more information.

      if (!processor.getProcessorState().getClusterView().contains(originator)) {

         final String errorResult = "Recovery marker from an unknown coordinator with address  " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response errorResponse = createResponse(RESULT_ERROR);
         errorResponse.setResult(errorResult);
         processor.post(errorResponse);

         return;
      }

      // Respond with success
      processor.post(createResponse(RESULT_SUCCESS));

      if (LOG.isDebugEnabled()) {
         LOG.debug("<><><><><><><><><><><><><><> Created recovery state: " + processor.getAddress().getTcpPort()
                 + ", coordinator: " + originator);
      }

      processor.getProcessorState().setState(STATE_RECOVERY);

      // NOTE: simeshev@cacheonix.org - 2010-12-23 - it is possible that a node in a Cleanup
      // state receives a recovery marker with self as an originator. This may happen when
      // a marker timeout occurs while this node is alone, thus initiating recovery for a
      // single node.
      //
      // It is a separate question how a node in Cleanup state can lose its own marker. This
      // should not be possible, but, according to CACHEONIX-214, it did happen, see the log
      // at 09:11:29,123. Though there that was a Blocked state.
      //
      final RecoveryMarker recoveryMarker = copy();

      final boolean recoveryOriginator = processor.getAddress().equals(recoveryMarker.originator);

      // DELETEME: simeshev@cacheonix.org - 2011-02-14
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Is recovery originator: " + recoveryOriginator); // NOPMD

      processor.getProcessorState().setRecoveryOriginator(recoveryOriginator);

      // REVIEWME: simeshev@cacheonix.org - 2010-07-07 - What are the implications
      // of sending the new marker to self instead of just forwarding it?

      // Post marker to self in Cleanup state
      recoveryMarker.setReceiver(processor.getAddress());
      processor.post(recoveryMarker);
   }


   /**
    * Begins cleanup
    */
   private void beginCleanup() {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Beginning cleanup round: " + getProcessor().getAddress());
      }

      final ClusterProcessor processor = getClusterProcessor();

      // Create cleanup marker
      final CleanupMarker cleanupMarker = CleanupMarker.originate(processor);

      // Change state to Clean-up, set as originator.
      processor.getProcessorState().setState(ClusterProcessorState.STATE_CLEANUP);

      // Cancel 'home alone' timeout
      processor.getProcessorState().getHomeAloneTimeout().cancel();

      // Forward cleanup marker

      final ClusterNodeAddress nextElement = processor.getProcessorState().getClusterView().getNextElement();
      if (LOG.isDebugEnabled()) {
         LOG.debug("Forwarding cleanup marker to " + nextElement + " : " + cleanupMarker);
      }
      cleanupMarker.setReceiver(nextElement);
      processor.post(cleanupMarker);
   }


   /**
    * Begins blocking the minority cluster by changing state to blocked and sending the blocked marker.
    *
    * @param targetMajorityMarkerListSize target majority marker list size
    */
   private final void beginBlocking(final int targetMajorityMarkerListSize) {

      // Set context state
      final ClusterProcessor processor = getClusterProcessor();

      // Reset join state. Switching to a stable state should clear join state possibly
      // acquired before going through recovery process. See CACHEONIX-279 for details.

      processor.getProcessorState().getJoinStatus().clear();

      // REVIEWME: simeshev@cacheonix.org - 2008-04-17 -> markerListBeforeRecovery.size() can change
      // after multiple Recovery -> Cleanup -> Recovery

      final int newState = STATE_BLOCKED;
      processor.getProcessorState().setState(newState);

      processor.getProcessorState().setTargetMajoritySize(targetMajorityMarkerListSize);
      processor.notifyNodeBlocked();

      processor.getProcessorState().getHomeAloneTimeout().reset();

      // Notify cluster event subscribers
      processor.getProcessorState().notifySubscribersClusterStateChanged(newState);

      // Create and forward new BlockedMarker
      final BlockedMarker blockedMarker = new BlockedMarker(
              processor.getProcessorState().getClusterView().getClusterUUID());

      blockedMarker.setReceiver(processor.getProcessorState().getClusterView().getNextElement());
      processor.post(blockedMarker);
   }


   /**
    * Creates a copy of this <code>RecoveryMarker</code> suitable for forwarding.
    *
    * @return the copy of this <code>RecoveryMarker</code> suitable for forwarding.
    */
   private RecoveryMarker copy() {

      final RecoveryMarker result = new RecoveryMarker();
      result.setRequiresSameCluster(isRequiresSameCluster());
      result.currentList = new ArrayList<JoiningNode>(currentList);
      result.previousList = new ArrayList<JoiningNode>(previousList);
      result.newClusterUUID = newClusterUUID;
      result.originator = originator;
      return result;
   }


   /**
    * {@inheritDoc}
    */
   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      //
      newClusterUUID = SerializerUtils.readUuid(in);

      // Originator
      originator = SerializerUtils.readAddress(in);

      // current list
      final int currentListSize = in.readInt();
      currentList = new ArrayList<JoiningNode>(currentListSize + 1); // +1 because we know we will add ourselves
      for (int i = 0; i < currentListSize; i++) {
         currentList.add(SerializerUtils.readJoiningNode(in));
      }

      // previous list
      final int previousListSize = in.readInt();
      previousList = new ArrayList<JoiningNode>(previousListSize);
      for (int i = 0; i < previousListSize; i++) {
         previousList.add(SerializerUtils.readJoiningNode(in));
      }
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      //
      SerializerUtils.writeUuid(newClusterUUID, out);

      // originator
      SerializerUtils.writeAddress(originator, out);

      // current list
      out.writeInt(currentList.size());
      for (final JoiningNode joiningNode : currentList) {
         SerializerUtils.writeJoiningNode(joiningNode, out);
      }

      // previous list
      out.writeInt(previousList.size());
      for (final JoiningNode joiningNode : previousList) {
         SerializerUtils.writeJoiningNode(joiningNode, out);
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final RecoveryMarker that = (RecoveryMarker) o;

      if (currentList != null ? !currentList.equals(that.currentList) : that.currentList != null) {
         return false;
      }
      if (newClusterUUID != null ? !newClusterUUID.equals(that.newClusterUUID) : that.newClusterUUID != null) {
         return false;
      }
      if (originator != null ? !originator.equals(that.originator) : that.originator != null) {
         return false;
      }
      if (previousList != null ? !previousList.equals(that.previousList) : that.previousList != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (originator != null ? originator.hashCode() : 0);
      result = 31 * result + (currentList != null ? currentList.hashCode() : 0);
      result = 31 * result + (previousList != null ? previousList.hashCode() : 0);
      result = 31 * result + (newClusterUUID != null ? newClusterUUID.hashCode() : 0);
      return result;
   }


   /**
    * {@inheritDoc}
    */
   public String toString() {

      return "RecoveryMarker{" +
              "sender=" + getSender() +
              ", originator=" + originator +
              ", currentList=" + currentList +
              ", previousList=" + previousList +
              ", newClusterUUID=" + newClusterUUID +
              "} " + super.toString();
   }

   // ------------------------------------------------------------------------------------------------------------------
   //
   // Waiter
   //
   // ------------------------------------------------------------------------------------------------------------------

   /**
    * Waiter for RecoveryMarker.
    */
   protected static final class Waiter extends org.cacheonix.impl.net.processor.Waiter {


      /**
       * Creates waiter.
       *
       * @param request request UUID
       */
      public Waiter(final Request request) {

         super(request);
      }


      /**
       * {@inheritDoc}
       */
      public void notifyResponseReceived(final Response message) throws InterruptedException {

         final ClusterResponse response = (ClusterResponse) message;
         if (response.getResultCode() != RESULT_SUCCESS) {

            // Forward to the next node failed - handle next process failure.

            final ClusterProcessor processor = (ClusterProcessor) getRequest().getProcessor();
            final ReceiverAddress failedProcess = getRequest().getReceiver();
            final ClusterNodeAddress self = processor.getAddress();

            // NOTE: simeshev@cacheonix.org - 2010-12-23 - we check that the failed processes is not self because
            // it is possible that recovery marker was rejected because the node was alone and then it joined
            // another cluster while the recovery marker was still in flight.

            if (failedProcess.isAddressOf(self)) {

               LOG.warn("Received error response from self, ignoring: " + response);
            } else {

               if (processor.getProcessorState().getClusterView().contains(failedProcess)) {

                  final ClusterNodeAddress processNextAfterFailed = processor.getProcessorState().getClusterView().getNextElement(
                          failedProcess);

                  if (LOG.isDebugEnabled()) {
                     LOG.debug("Amend current list by removing the failed process " + failedProcess.getTcpPort()
                             + ", failed request: " + getRequest() + ", response: " + message);
                  }

                  // Check if the failed process was originator
                  final RecoveryMarker recoveryMarker;
                  final RecoveryMarker request = (RecoveryMarker) getRequest();

                  if (failedProcess.isAddressOf(request.getOriginator())) {

                     // Originator is gone with the next-process failure,
                     // we have to initiate a new recovery round
                     if (LOG.isDebugEnabled()) {
                        LOG.debug("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRR Originator " + failedProcess
                                + " is gone with the next-process failure. Initiating a new recovery round");
                     }

                     processor.getProcessorState().setRecoveryOriginator(true);

                     // Create new marker
                     final UUID newClusterUUID = UUID.randomUUID();
                     recoveryMarker = new RecoveryMarker(newClusterUUID, self);
                  } else {

                     //
                     // Amend Current list by removing the failed process
                     //

                     // Create a copy of the request
                     recoveryMarker = request.copy();

                     // Removing the failed process from the current list
                     for (final Iterator<JoiningNode> iter = recoveryMarker.currentList.iterator(); iter.hasNext(); ) {

                        final JoiningNode node = iter.next();

                        if (failedProcess.isAddressOf(node.getAddress())) {

                           iter.remove();

                           break;
                        }
                     }
                  }

                  // Send recovery marker to next
                  recoveryMarker.setReceiver(processNextAfterFailed);
                  processor.post(recoveryMarker);
               } else {

                  // Ignore failure if the cluster has already adjusted
                  // to the loss of the member
                  LOG.debug("Ignored failure because cluster has already adjusted to the member loss: " + message);
               }
            }
         }
         super.notifyResponseReceived(message);
      }
   }

   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new RecoveryMarker();
      }
   }
}
