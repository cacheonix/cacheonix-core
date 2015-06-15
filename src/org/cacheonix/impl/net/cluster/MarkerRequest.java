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
package org.cacheonix.impl.net.cluster;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;

import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.cluster.ClusterState;
import org.cacheonix.impl.cluster.event.ClusterStateChangedEventImpl;
import org.cacheonix.impl.lock.AcquireLockRequest;
import org.cacheonix.impl.lock.LockOwner;
import org.cacheonix.impl.lock.LockQueue;
import org.cacheonix.impl.lock.LockQueueKey;
import org.cacheonix.impl.lock.LockRegistry;
import org.cacheonix.impl.lock.NextLockRequestGranter;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Marker request is a marker that sends
 */
public abstract class MarkerRequest extends ClusterRequest {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MarkerRequest.class); // NOPMD


   /**
    * Default constructor. Required by <code>Wireable</code>.
    *
    * @see Wireable
    */
   protected MarkerRequest() {

   }


   protected MarkerRequest(final int wireableType) {

      super(wireableType);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation enhances the default behaviour by incrementing number of markers received by a cluster node.
    */
   public void execute() throws InterruptedException {

      //
      final ClusterProcessor processor = getClusterProcessor();

      // Increment marker counter

      processor.getProcessorState().incrementMarkerCounter();

      // Stop timeout
      processor.getObtainMarkerTimeout().cancel();

      try {

         super.execute();
      } finally {
         processor.getObtainMarkerTimeout().reset();
      }
   }


   public void validate() throws InvalidMessageException {

      super.validate();

      if (!isSenderSet()) {
         throw new InvalidMessageException("Marker does not have a receiver");
      }

      if (!isReceiverSet()) {
         throw new InvalidMessageException("Marker does not have a receiver");
      }
   }


   /**
    * Sends a left mcast announcement to self to support ordering of configuration changes with messages (a total
    * order).
    *
    * @param reservedLeftSeqNum a seqNum supplied by the leaving node.
    * @param left               the address of the leaving node.
    */
   protected final void sendLeftToSelf(final long reservedLeftSeqNum, final ClusterNodeAddress left) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled())
         LOG.debug("Sending left to self, reservedLeftSeqNum: " + reservedLeftSeqNum + ", left: " + left); // NOPMD

      final ClusterProcessor processor = getClusterProcessor();

      // Create message
      final ClusterNodeLeftAnnouncement nodeLeftMessage = new ClusterNodeLeftAnnouncement();

      nodeLeftMessage.setClusterUUID(processor.getProcessorState().getClusterView().getClusterUUID());
      nodeLeftMessage.setSender(left);
      nodeLeftMessage.setLeave(left);

      // Convert to a frame
      final PayloadPartitioner payloadPartitioner = new PayloadPartitioner();
      final List<Frame> frames = payloadPartitioner.partition(nodeLeftMessage);
      Assert.assertTrue(frames.size() == 1, "ClusterNodeLeftAnnouncement should be partitioned to a single frame");

      // Set reserved sequence number
      final Frame nodeLeftFrame = frames.get(0);
      nodeLeftFrame.setSequenceNumber(reservedLeftSeqNum);

      nodeLeftFrame.setClusterUUID(processor.getProcessorState().getClusterView().getClusterUUID());

      // Send to ourselves

      processor.getProcessorState().getReceivedList().add(nodeLeftFrame);
   }


   /**
    * Sends a join mcast announcement to self to support ordering of configuration changes with messages (a total
    * order).
    *
    * @param reservedJoinSeqNum a seqNum supplied by the join coordinator (AKA predecessor).
    * @param joined             the address of the joined node.
    */
   protected final void sendJoinedToSelf(final long reservedJoinSeqNum, final ClusterNodeAddress joined) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled())
         LOG.debug("Sending joined to self, reservedJoinSeqNum: " + reservedJoinSeqNum + ", joined: " + joined); // NOPMD

      final ClusterProcessor processor = getClusterProcessor();

      // Create message
      final ClusterNodeJoinedAnnouncement nodeJoinedAnnouncement = new ClusterNodeJoinedAnnouncement();

      nodeJoinedAnnouncement.setClusterUUID(processor.getProcessorState().getClusterView().getClusterUUID());
      nodeJoinedAnnouncement.setSender(joined);
      nodeJoinedAnnouncement.setJoined(joined);

      // Convert to a frame
      final PayloadPartitioner payloadPartitioner = new PayloadPartitioner();
      final List<Frame> frames = payloadPartitioner.partition(nodeJoinedAnnouncement);
      Assert.assertTrue(frames.size() == 1, "ClusterNodeJoinedAnnouncement should be partitioned to a single frame");

      // Set reserved sequence number
      final Frame nodeJoinedFrame = frames.get(0);
      nodeJoinedFrame.setSequenceNumber(reservedJoinSeqNum);

      nodeJoinedFrame.setClusterUUID(processor.getProcessorState().getClusterView().getClusterUUID());

      // Send to ourselves

      processor.getProcessorState().getReceivedList().add(nodeJoinedFrame);
   }


   /**
    * Removes left nodes from the replicated lock queue. Grants pending lock requests if the left nodes held locks.
    *
    * @param nodesLeft a collection of addresses of nodes that left the cluster.
    */
   protected void removeFromLockQueue(final Collection<ClusterNodeAddress> nodesLeft) {

      final ClusterProcessor processor = getClusterProcessor();

      final LockRegistry lockRegistry = processor.getProcessorState().getReplicatedState().getLockRegistry();
      for (final Map.Entry<LockQueueKey, LockQueue> entry : lockRegistry.getLockQueues().entrySet()) {

         // Process a particular lock queue
         final LockQueue lockQueue = entry.getValue();
         for (final ClusterNodeAddress leftAddress : nodesLeft) {

            // Clear write lock owner
            final LockOwner writeLockOwner = lockQueue.getWriteLockOwner();
            if (writeLockOwner != null) {
               if (writeLockOwner.getAddress().equals(leftAddress)) {
                  lockQueue.clearWriteLockOwner();
               }
            }

            // Clear read lock owner
            for (final Iterator<LockOwner> iter = lockQueue.getReadLockOwners().iterator(); iter.hasNext(); ) {
               final LockOwner readLockOwner = iter.next();
               if (readLockOwner.getAddress().equals(leftAddress)) {
                  iter.remove();
               }
            }

            // Remove address from the request queue
            for (final Iterator<AcquireLockRequest> iter = lockQueue.getPendingRequests().iterator(); iter.hasNext(); ) {
               final AcquireLockRequest request = iter.next();
               if (request.getOwnerAddress().equals(leftAddress)) {
                  iter.remove();
               }
            }
         }

         // Grant next request(s). The granter is capable of detecting if there is work do do
         final NextLockRequestGranter nextLockRequestGranter = new NextLockRequestGranter(processor, lockQueue);
         nextLockRequestGranter.grantNextLockRequests();
      }
   }


   /**
    * Moves frames accumulated in the received queue to the received list.
    */
   protected final void receiveFrames() {

      final ClusterProcessor processor = getClusterProcessor();

      final UUID contextClusterUUID = processor.getProcessorState().getClusterView().getClusterUUID();

      final ReceivedList receivedList = processor.getProcessorState().getReceivedList();
      final Queue<Frame> receivedFrames = processor.getReceivedFrames();
      for (Frame frame = receivedFrames.poll(); frame != null; frame = receivedFrames.poll()) {

         if (frame.getClusterUUID().equals(contextClusterUUID)) {
            receivedList.add(frame);
         }
      }
   }


   protected void logForward(final MulticastMarker marker) {

      if (LOG.isDebugEnabled() && getClusterProcessor().getProcessorState().incrementMarkerCounter() % 100L == 0L) {
         LOG.debug("Forwarded marker, seqNum: " + marker.getSeqNum()
                 + ", My port: " + getClusterProcessor().getAddress().getTcpPort()
                 + ", To port: " + marker.getReceiver().getTcpPort()
                 + ", Current: " + marker.getCurrent()
                 + ", Previous: " + marker.getPrevious()
                 + ", Originator: " + (marker.getOriginator() != null ? String
                 .valueOf(marker.getOriginator().getTcpPort()) : "null"));
      }
   }


   /**
    * Begins blocking the minority cluster by changing state to blocked and sending the blocked marker.
    *
    * @param targetMajorityMarkerListSize target majority marker list size
    */
   protected final void beginBlocking(final int targetMajorityMarkerListSize) {

      // Set context state
      final ClusterProcessor processor = getClusterProcessor();

      // Reset join state. Switching to a stable state should clear join state possibly
      // acquired before going through recovery process. See CACHEONIX-279 for details.

      processor.getProcessorState().getJoinStatus().clear();

      // REVIEWME: simeshev@cacheonix.com - 2008-04-17 -> markerListBeforeRecovery.size() can change
      // after multiple Recovery -> Cleanup -> Recovery


      processor.getProcessorState().setState(ClusterProcessorState.STATE_BLOCKED);

      processor.getProcessorState().setTargetMajoritySize(targetMajorityMarkerListSize);
      processor.getMulticastMessageListeners().notifyNodeBlocked();

      processor.getProcessorState().getHomeAloneTimeout().reset();

      // Notify cluster event subscribers
      notifySubscribersClusterStateChanged(ClusterState.BLOCKED);

      // Create and forward new BlockedMarker
      final BlockedMarker blockedMarker = new BlockedMarker(processor.getProcessorState().getClusterView().getClusterUUID());

      blockedMarker.setReceiver(processor.getProcessorState().getClusterView().getNextElement());
      processor.post(blockedMarker);
   }


   protected final void forwardCleanupMarker(final CleanupMarker cleanupMarker) {

      final ClusterProcessor processor = getClusterProcessor();

      final ClusterNodeAddress nextElement = processor.getProcessorState().getClusterView().getNextElement();
      cleanupMarker.setReceiver(nextElement);

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Forwarding cleanup marker to " + nextElement + " : " + cleanupMarker);

      processor.post(cleanupMarker);
   }


   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("RedundantMethodOverride")
   protected org.cacheonix.impl.net.processor.Waiter createWaiter() {

      return new Waiter(this);
   }


   protected void notifySubscribersClusterStateChanged(final ClusterState newClusterState) {

      final ClusterProcessorState processorState = getClusterProcessor().getProcessorState();
      final List<ClusterEventSubscriber> clusterEventSubscribers = processorState.getClusterEventSubscribers();
      final Executor userEventExecutor = processorState.getUserEventExecutor();
      for (final ClusterEventSubscriber clusterEventSubscriber : clusterEventSubscribers) {
         userEventExecutor.execute(new Runnable() {

            public void run() {

               try {

                  clusterEventSubscriber.notifyClusterStateChanged(new ClusterStateChangedEventImpl(newClusterState));
               } catch (final Throwable e) { // NOPMD A catch statement should never catch throwable since it includes errors.

                  // Isolate errors possibly thrown by a call to a user API
                  LOG.warn("Error while notifying subscriber" + clusterEventSubscriber + "that cluster state changed: " + e, e);
               }
            }
         });
      }
   }


   /**
    * Waiter for MarkerMessage.
    */
   public static class Waiter extends org.cacheonix.impl.net.processor.Waiter {

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
      public void notifyResponseReceived(final Response response) throws InterruptedException {

         if (response.getResultCode() != ClusterResponse.RESULT_SUCCESS) {

            beginRecovery(response);
         }

         super.notifyResponseReceived(response);
      }


      /**
       * Begins recovery by shifting to recovery state and forwarding recovery marker the the next process after the
       * failed process as defined by the process parameter.
       * <p/>
       * Overwriting methods must call this method.
       *
       * @param errorResponse error response to the marker that causes the beginning of the recovery
       */
      protected final void beginRecovery(final Response errorResponse) {


         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Error response to marker request: " + errorResponse); // NOPMD

         final ClusterProcessor processor = getClusterProcessor();

         if (getRequest().getClusterUUID().equals(processor.getProcessorState().getClusterView().getClusterUUID())) {

            // The configuration is the same as when the request was sent. This means
            // that the receiver of the request is still there - process normally.

            final ReceiverAddress failedNodeAddress = getRequest().getReceiver();

            final ClusterNodeAddress beginRecoveryWith = processor.getProcessorState().getClusterView().getNextElement(failedNodeAddress);

//            // Ignore self who has changed the cluster while waiting for response
//
//            if (context.getAddress().equals(errorResponse.getSender())) {
//
//               if (LOG.isDebugEnabled()) {
//                  LOG.debug("This is self who changed the cluster while waiting for response, " +
//                          "won't begin recovery, request: " + getRequest());
//               }
//            } else {

            // Error response from an other node that changed its cluster
            // while this request waited for response, initiate recovery
            beginRecovery(beginRecoveryWith);
//            }
         } else {

            // The cluster configuration has changed while we were waiting for the response.
            // The configuration can change via recovery process or via joining another cluster.
            // In this case recovery should not commence.

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) {

               LOG.debug("Won't start recovery because configuration has changed since marker was sent"); // NOPMD
            }
         }
      }


      private void beginRecovery(final ClusterNodeAddress beginRecoveryWith) {

         //noinspection ControlFlowStatementWithoutBraces
         final MarkerRequest request = (MarkerRequest) getRequest();
         if (LOG.isDebugEnabled()) {
            LOG.debug("Failed to forward marker to " + request.getReceiver()
                    + ", initiating recovery round, originator: " + getClusterProcessor().getAddress()
                    + ", marker: " + request);
         }

         final ClusterProcessor processor = getClusterProcessor();

         // Begin recovery with the node next after failed.
         final ClusterNodeAddress self = processor.getAddress();

         if (LOG.isDebugEnabled()) {
            LOG.debug("RRRRRRRRRRRRRRRRRRRRRR Begin recovery starting with: " + beginRecoveryWith);
         }

         // Change state to recovery, with us as an Originator
         if (LOG.isDebugEnabled()) {
            LOG.debug("<><><><><><><><><><><><><><> Created recovery state: " + self.getTcpPort() + ", originator: " + true);
         }

         processor.getProcessorState().setState(ClusterProcessorState.STATE_RECOVERY);

         processor.getProcessorState().setRecoveryOriginator(true);

         // Notify cluster event subscribers
         request.notifySubscribersClusterStateChanged(ClusterState.RECONFIGURING);

         // Notify cluster event subscribers

         // Post new recovery marker with self as an originator
         final UUID newClusterUUID = UUID.randomUUID();

         final List<JoiningNode> currentList = CollectionUtils.createList(new JoiningNode(self));
         final List<JoiningNode> previousList = Collections.emptyList();
         final RecoveryMarker recoveryMarker = new RecoveryMarker(newClusterUUID, self, currentList, previousList);
         recoveryMarker.setReceiver(beginRecoveryWith);

         processor.post(recoveryMarker);
      }


      /**
       * Re-usable shortcut method to obtain a typed context ClusterService from the waiter's request.
       *
       * @return waiter's context ClusterService.
       */
      protected final ClusterProcessor getClusterProcessor() {

         return ((ClusterRequest) getRequest()).getClusterProcessor();
      }
   }
}
