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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.cacheonix.cluster.ClusterState;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.lock.LockOwner;
import org.cacheonix.impl.lock.LockQueue;
import org.cacheonix.impl.lock.LockQueueKey;
import org.cacheonix.impl.lock.LockRegistry;
import org.cacheonix.impl.lock.ReleaseLockRequest;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.ObjectObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Multicast marker. The multicast marker circulates in the ring in normal mode.
 *
 * @noinspection SimplifiableIfStatement, NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode,
 * RedundantIfStatement
 */
public final class MulticastMarker extends OperationalMarker {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MulticastMarker.class); // NOPMD

   /**
    * Max multicast messages messages allowed to send.
    * <p/>
    * OPTIMIZEME: slava@cacheonix.org - 20090-12-08 - As for this writing MulticastReceiver is single threaded, so
    * setting the max number of messages too high causes receive buffer overflow in the MulticastReceiver because it
    * cannot pull the messages fast enough. This value should a function of flow control rather then a constant. The
    * function should minimize the number of retransmits.
    */
   private static final int MAX_MULTICAST_MESSAGES_ALLOWED_TO_SEND = 100;

   private ClusterNodeAddress originator = null;

   private long seqNum = 0L; // not set

   private Long current = null; // not set

   private Long previous = null; // not set

   /**
    * A sequence number of a the message reserved for leave operation
    */
   private Long leaveSeqNum = null;


   /**
    * A sequence number of a the message reserved for a join operation
    */
   private Long joinSeqNum = null;


   /**
    * Required by Externalizable.
    */
   public MulticastMarker() {

      super(TYPE_CLUSTER_MULTICAST_MARKER);
      setRequiresSameCluster(false);
   }


   /**
    * Required by Externalizable.
    *
    * @param clusterUUID UUID of the cluster
    */
   public MulticastMarker(final UUID clusterUUID) {

      super(TYPE_CLUSTER_MULTICAST_MARKER, clusterUUID);
      setRequiresSameCluster(false);
   }


   public ClusterNodeAddress getOriginator() {

      return originator;
   }


   public void setOriginator(final ClusterNodeAddress originator) {

      this.originator = originator;
   }


   public long getSeqNum() {

      return seqNum;
   }


   public void setSeqNum(final long seqNum) {

      this.seqNum = seqNum;
   }


   public Long getCurrent() {

      return current;
   }


   public void setCurrent(final Long current) {

      this.current = current;
   }


   public Long getPrevious() {

      return previous;
   }


   public void setPrevious(final Long previous) {

      this.previous = previous;
   }


   public void setLeaveSeqNum(final Long leaveSeqNum) {

      this.leaveSeqNum = leaveSeqNum;
   }


   public Long getLeaveSeqNum() {

      return leaveSeqNum;
   }


   public Long getJoinSeqNum() {

      return joinSeqNum;
   }


   void setJoinSeqNum(final Long joinSeqNum) {

      this.joinSeqNum = joinSeqNum;
   }


   public final void clearJoin() {

      // Clear operational marker fields
      super.clearJoin();

      // Clear join seq num.
      joinSeqNum = null;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation decrements the seqNum and calls <code>clearJoin()</code>.
    */
   public void rollbackJoin() {

      // NOTE: simeshev@cacheonix.org - 2011-02-24 - Storing the marker for forward rolls the seqNum forward
      // and it stayed untouched until this moment, so it is safe
      // to decrement.

      Assert.assertEquals(joinSeqNum, seqNum, "Should be the same, seqNum: {0}, joinSeqNum: {1}", seqNum, joinSeqNum);

      seqNum--;

      // Clear the join fields.
      clearJoin();
   }


   protected final void processClusterAnnouncements() {

      final ClusterProcessor processor = getClusterProcessor();

      final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();

      final ObservedClusterNode strongestObservedClusterNode = joinStatus.getStrongestObservedClusterNode();
      if (strongestObservedClusterNode == null) {
         return;
      }

      // Ignore announcements from blocked a cluster - we are operational
      // and do not want to disrupt our cluster. Members of the blocked
      // cluster may attempt to join us later.
      if (!strongestObservedClusterNode.isOperationalCluster()) {
         return;
      }

      // Check if this is an originator

      final ClusterView clusterView = processor.getProcessorState().getClusterView();
      final int ourSize = clusterView.getSize();
      if (clusterView.isRepresentative() && ourSize > 1) {

         // The representative will leave only when it is alone
         return;
      }

      // Ignore if we are joining
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

      // By now this node's cluster UUID might have changed

      if (processor.getProcessorState().getClusterView().getClusterUUID().equals(strongestObservedClusterNode.getClusterUUID())) {
         return;
      }

      // Check if still surveying the area
      if (!joinStatus.clusterSurveyTimeoutExpired()) {
         return;
      }


      // We will join if the observed cluster is a bigger cluster or a cluster
      // of the same size but their representative if bigger than ours
      final int theirMarkerListSize = strongestObservedClusterNode.getMarkerListSize();
      if (ourSize < theirMarkerListSize) {

         initiateJoin(strongestObservedClusterNode.getSenderAddress());
      } else if (ourSize == theirMarkerListSize) {

         if (strongestObservedClusterNode.getRepresentative().compareTo(clusterView.getRepresentative()) > 0) {

            initiateJoin(strongestObservedClusterNode.getSenderAddress());
         }
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation sends a join announcement to self to support ordering of configuration changes with messages
    * (a total order).
    */
   public final void finishJoin() {

      sendJoinedToSelf(joinSeqNum, getJoiningNode().getAddress());
   }


   public void forward() throws InterruptedException {

      final ClusterProcessor processor = getClusterProcessor();
      final ClusterNodeAddress self = processor.getAddress();

      setReceiver(processor.getProcessorState().getClusterView().getNextElement());

      if (LOG.isDebugEnabled() && isJoiningNodeSet() && getReceiver().isAddressOf(getJoiningNode().getAddress())) {
         LOG.debug("Sending first multicast marker to joined: " + this);
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Pre-process leaving
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      boolean left = false;
      if (isLeaveSet()) {

         // Marker is serving a leave

         if (LOG.isDebugEnabled()) {
            LOG.debug("Received leave: " + getLeave().getTcpPort() + ", my port: " + self.getTcpPort());
         }

         if (self.equals(getLeave())) {

            // Leave marker returned to us
            setLeave(null);
            leaveSeqNum = null;
            left = true;
         } else {

            // Clear the leave field if the leaving node is already gone.

            if (!processor.getProcessorState().getClusterView().contains(getLeave())) {

               // Leaving node is already gone
               setLeave(null);
               leaveSeqNum = null;
            }
         }
      } else {

         // Marker is *not* serving a leave

         if (processor.isShuttingDown()) {

            // We have to leave
            if (LOG.isDebugEnabled()) {
               LOG.debug("This node has to leave: " + self.getTcpPort());
            }
            setLeave(self);
            leaveSeqNum = seqNum + 1;
            seqNum = leaveSeqNum;
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Post marker to the next process
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // First post marker to guarantee that it is in the queue
      setResponseRequired(!left); // No point in asking for a response if leaving

      processor.post(this);

      logForward(this);

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Post-process leaving
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      if (left) {

         if (LOG.isDebugEnabled()) {
            LOG.debug("This node has left, enqueueing shutdown command: " + self.getTcpPort());
         }

         // Now post the shutdown command. Anything posted after this may not be executed.
         processor.enqueue(new ShutdownClusterProcessorCommand(processor));
      } else {

         if (isLeaveSet() && !self.equals(getLeave())) {

            // Other node left, adjust the cluster view

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("Other node has left: " + getLeave().getTcpPort()); // NOPMD

            final List<ClusterNodeAddress> nodesLeft = Collections.singletonList(getLeave());

            processor.getProcessorState().getClusterView().remove(getLeave());

            processor.getProcessorState().updateLastOperationalClusterView(processor.getProcessorState().getClusterView());
            processor.notifyNodesLeft(nodesLeft);

            // Post a message to self with a message ID equal the number reserved for the leave
            sendLeftToSelf(leaveSeqNum, getLeave());
         }
      }
   }


   protected void processNormal() throws IOException, InterruptedException {

      // Check if this is our our marker or if it comes from another cluster

      if (getClusterProcessor().getProcessorState().getClusterView().getClusterUUID().equals(getClusterUUID())) {

         // Receive frames
         receiveFrames();

         // Process distributed locks unlock timeouts
         processUnlockTimeouts();

         // Begin joining if there are proper cluster announcements.
         processClusterAnnouncements();

         // Process normal
         processNormalNormal();
      } else {
         processForeign();
      }
   }


   /**
    * If this is a cluster representative, posts unlock message if unlock has timed out
    */
   private void processUnlockTimeouts() {

      final ClusterProcessor processor = getClusterProcessor();

      final ClusterView clusterView = processor.getProcessorState().getClusterView();

      // This method is only executed at a representative so that
      // the unlock requests are sent from an only place
      if (!processor.getAddress().equals(clusterView.getRepresentative())) {

         // Not a representative
         return;
      }

      // This a representative

      final LockRegistry lockRegistry = processor.getProcessorState().getReplicatedState().getLockRegistry();
      final HashMap<LockQueueKey, LockQueue> lockQueues = lockRegistry.getLockQueues();
      if (lockQueues.isEmpty()) {

         // No locks
         return;
      }

      // Process each keyed lock region
      lockQueues.forEachEntry(new ObjectObjectProcedure<LockQueueKey, LockQueue>() {

         public boolean execute(final LockQueueKey lockQueueKey, final LockQueue lockQueue) {

            // Begin to release timed out write lock
            final LockOwner writeLockOwner = lockQueue.getWriteLockOwner();
            beginReleasingExpiredLocks(lockQueueKey, lockQueue, writeLockOwner);

            // Begin to release timed out read locks
            final List<LockOwner> lockOwners = lockQueue.getReadLockOwners();
            for (final LockOwner readLockOwner : lockOwners) {

               beginReleasingExpiredLocks(lockQueueKey, lockQueue, readLockOwner);
            }

            // Next entry
            return true;
         }
      });
   }


   /**
    * For a given lock owner, if its time to unlock expired, posts a reliable mcast message to release the lock.
    *
    * @param lockQueueKey the lock name.
    * @param lockQueue    the lock queue.
    * @param lockOwner    the lock owner.
    */
   private void beginReleasingExpiredLocks(final LockQueueKey lockQueueKey, final LockQueue lockQueue,
                                           final LockOwner lockOwner) {

      // Nothing to release
      if (lockOwner == null) {
         return;
      }

      // Check if already registered
      if (lockQueue.isRegisteredInForcedReleases(lockOwner)) {
         return;
      }

      // Lazy init unlock time

      final ClusterProcessor processor = getClusterProcessor();
      if (processor.getClock().currentTime().compareTo(lockOwner.getUnlockTimeout()) > 0) {

         // Register unlock announcement
         lockQueue.registerForcedRelease(lockOwner);

         // Announce unlock
         final ReleaseLockRequest announcement = new ReleaseLockRequest(lockQueueKey.getLockRegionName(),
                 lockQueueKey.getLockKey(), lockOwner.getAddress(), lockOwner.getThreadID(), lockOwner.getThreadName(),
                 lockOwner.isReadLock());
         announcement.setResponseRequired(false); // Important to avoid sending useless response
         processor.post(announcement);
      }
   }


   @SuppressWarnings("ForLoopReplaceableByForEach")
   protected void processNormalNormal() throws IOException, InterruptedException {

      final ClusterProcessor clusterProcessor = getClusterProcessor();
      final ClusterNodeAddress self = clusterProcessor.getAddress();
      final MulticastMarker multicastMarker = copy();

      // Response to previous node with success
      clusterProcessor.post(createResponse(ClusterResponse.RESULT_SUCCESS));


      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Handle Join
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      if (multicastMarker.getPredecessor() != null) {

         if (self.equals(multicastMarker.getPredecessor())) {

            // Marker for join served by us returned to us
            multicastMarker.clearJoin();
         } else {

            // This is a join served by someone else, change our list.
            // REVIEWME: simeshev@cahceonix.com - what about a predecessor being gone and not
            // cleaned up the predecessor?
            final JoiningNode joiningNode = multicastMarker.getJoiningNode();
            if (!joiningNode.getAddress().equals(self)) {

               // Insert joined to the cluster view

               clusterProcessor.getProcessorState().getClusterView().insert(multicastMarker.getPredecessor(), joiningNode);

               clusterProcessor.getProcessorState().updateLastOperationalClusterView(clusterProcessor.getProcessorState().getClusterView());

               // Sends a join mcast announcement to self to support ordering of
               // configuration changes with messages (a total order).
               sendJoinedToSelf(multicastMarker.joinSeqNum, joiningNode.getAddress());
            }
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Send cluster announcement if necessary
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

      if (!clusterProcessor.isShuttingDown() && !clusterProcessor.getProcessorState().getJoinStatus().isJoining()) {

         final Time currentTime = clusterProcessor.getClock().currentTime();
         if (currentTime.compareTo(multicastMarker.getNextAnnouncementTime()) >= 0) {

            // Reached next announcement time

            multicastMarker.setNextAnnouncementTime(currentTime.add(clusterProcessor.getProcessorState().getClusterAnnouncementTimeoutMillis()));
            clusterProcessor.announceCluster(true);
         }
      }

      // Init allowed number of mcast messages
      int messagesAllowedToSend = MAX_MULTICAST_MESSAGES_ALLOWED_TO_SEND;

      // Ri

      final Long highestContinuousNumberReceived = clusterProcessor.getProcessorState().getReceivedList().getHighestContinuousNumberReceived();

      // Di
      final Long highestSequenceNumberDelivered = clusterProcessor.getProcessorState().getHighestSequenceNumberDelivered();

      if (multicastMarker.current != null) {

         if (self.equals(multicastMarker.originator)) {

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            //
            // Marker returns to stub i (we are originators of the delivery round) (4)
            //
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            if (multicastMarker.current < clusterProcessor.getProcessorState().getCurrent()) {

               // Current decreased, retransmit
               final long seqNumBegin = multicastMarker.current + 1L;

               final long seqNumEnd = clusterProcessor.getProcessorState().getCurrent();

               // REVIEWME: simeshev@cacheonix.org - 2008-03-29 - we send the whole bunch,
               // though the original TMP description mentions only one message.

               if (LOG.isDebugEnabled()) {

                  LOG.debug("Current decreased, re-transmitting from " + seqNumBegin + " to " + seqNumEnd + ". Current: " + multicastMarker.current + ", saved Current: " + clusterProcessor.getProcessorState().getCurrent() + ", messagesAllowedToSend: " + messagesAllowedToSend);
               }

               final ReceivedList receivedList = clusterProcessor.getProcessorState().getReceivedList();
               for (long resubmitNum = seqNumBegin; resubmitNum <= seqNumEnd; resubmitNum++) {

                  // Send packet
                  final Frame frame = receivedList.getMessage(resubmitNum);
                  clusterProcessor.sendMulticastFrame(frame);

                  // Decrement to implement basic flow control. If lesser or
                  // equal zero, multicasting code below should not send.
                  messagesAllowedToSend--;
               }
            }


            if (highestSequenceNumberDelivered == null || multicastMarker.current > highestSequenceNumberDelivered) {

               // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
               //
               // This indicates that messaged Di+1 through current has been received by all
               // group members. The previous field is used to notify about this fact. Copy
               // Current to Previous and circulate the marker once more. (5)
               //
               // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
               multicastMarker.previous = multicastMarker.current;

               if (multicastMarker.current < highestContinuousNumberReceived) {

                  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                  //
                  // In addition to setting the previous field, if Current < Ri, stub i again
                  // sets the Current field to Ri. (6)
                  //
                  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                  multicastMarker.current = highestContinuousNumberReceived;

                  clusterProcessor.getProcessorState().setCurrent(highestContinuousNumberReceived);
               }
            } else if (multicastMarker.current.equals(highestSequenceNumberDelivered)) {

               // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
               //
               // On the other hand, if Current == Di, then the originator has no additional
               // outstanding messages ready to be delivered, and it sets the Current field to
               // NULL, indicating that another stub can begin a delivery round if it wishes.
               // (7)
               //
               // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
               multicastMarker.current = null;
               multicastMarker.originator = null;

               clusterProcessor.getProcessorState().setCurrent(null);
            }
         } else {

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            //
            // We are not originators of the delivery round
            //
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            if (highestContinuousNumberReceived < multicastMarker.current) {

               if (LOG.isDebugEnabled()) {
                  LOG.debug("Requesting retransmit, highestContinuousNumberReceived: " + highestContinuousNumberReceived + ", marker.current: " + multicastMarker.current + ", marker: " + multicastMarker);
               }

               // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
               //
               // Indicate that we (stub j) not received message(s) (3)
               //
               // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
               multicastMarker.current = highestContinuousNumberReceived;
            }
         }

         // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
         //
         // When a stub received the marker with the previous field set, it knows that all messages
         // with sequence numbers less than or equal to the value of the Previous field may be
         // delivered (8)
         //
         // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
         if (multicastMarker.previous != null) {

            // Deliver a low level message to the assembler

            final long startDeliver = highestSequenceNumberDelivered == null ? 1 : highestSequenceNumberDelivered + 1L;
            final long endDeliver = multicastMarker.previous;

            if (LOG.isDebugEnabled() && startDeliver <= endDeliver) {
               LOG.debug("Delivering from " + startDeliver + " to " + endDeliver); // NOPMD
            }

            for (long frameNumberToDeliver = startDeliver; frameNumberToDeliver <= endDeliver; frameNumberToDeliver++) {

               final Frame frame = clusterProcessor.getProcessorState().getReceivedList().poll(frameNumberToDeliver);
               if (frame == null) {

                  LOG.warn("=============== NullPointerException ======================================");
                  LOG.warn("messageNumToDeliver: " + frameNumberToDeliver);
                  LOG.warn("marker: " + multicastMarker);

                  LOG.warn("receivedList: " + clusterProcessor.getProcessorState().getReceivedList());
                  LOG.warn("highestContinuousNumberReceived: " + highestContinuousNumberReceived);
                  LOG.warn("highestSequenceNumberDelivered: " + highestSequenceNumberDelivered);
                  LOG.warn("===========================================================================");
                  throw new IllegalStateException("Expected packet to be in the received queue, "
                          + "but it was missing. Expected: " + frameNumberToDeliver);
               }

               if (frameNumberToDeliver != frame.getSequenceNumber()) {

                  throw new IllegalStateException("Receive queue contains a packet with unexpected number. " +
                          "Expected: " + frameNumberToDeliver + ", found: " + frame.getSequenceNumber());
               }
               //                     if (LOG.isDebugEnabled()) LOG.debug("Deliver low-level packet");

               // Add the packet to the delivery queue. The queue will assemble partial messageParts
               // into a message.
               clusterProcessor.getMessageAssembler().add(frame);

               // Mark that a message has been delivered.
               clusterProcessor.getProcessorState().setHighestSequenceNumberDelivered(frameNumberToDeliver);
            }

            // Deliver assembled requests if any
            clusterProcessor.deliverAssembledMulticastMessages();

            // We are here because the delivery round is in progress. The indication that all
            // nodes delivered messages up to Previous is that Current > Previous. This happens
            // when the coordinator of the round has extended the current round past Previous.
            // And this happens when all up the Previous have been delivered.
            //
            // The other possibility is that the originator of the delivery round finished it (7)
            // and set current to null.

            if (multicastMarker.current == null || multicastMarker.current > multicastMarker.previous) {

               clusterProcessor.notifyDeliveredToAll(multicastMarker.previous);
            }
         }
      } else {


         // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
         //
         // Current is not set, begin delivery round (2)
         //
         // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
         if (!self.equals(multicastMarker.getLeave()) && !clusterProcessor.isShuttingDown() && highestContinuousNumberReceived != null && (highestSequenceNumberDelivered == null || highestContinuousNumberReceived > highestSequenceNumberDelivered)) {

            if (LOG.isDebugEnabled()) {
               LOG.debug("Current is not set, begin delivery round, highestSequenceNumberDelivered (Di): " + highestSequenceNumberDelivered + ", highestContinuousNumberReceived (Ri): " + highestContinuousNumberReceived);
            }

            clusterProcessor.getProcessorState().setCurrent(highestContinuousNumberReceived);
            multicastMarker.current = highestContinuousNumberReceived;
            multicastMarker.originator = self;
         }

         // Notify messages waiting for the delivery notification
         if (multicastMarker.previous != null) {

            clusterProcessor.notifyDeliveredToAll(multicastMarker.previous);
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Broadcast maximum allowed (1)
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

      final Queue<List<Frame>> submittalQueue = clusterProcessor.getProcessorState().getSubmittalQueue();
      for (List<Frame> frames; messagesAllowedToSend > 0 && (frames = submittalQueue.poll()) != null; ) {

         //         if (LOG.isDebugEnabled()) LOG.debug("Parts to send: " + messageParts);
         for (int i = 0; i < frames.size(); i++) {

            // Get (possible partial) packet
            final Frame frame = frames.get(i);
            // Increment seqNum obtained from the marker
            final long newSeqNum = multicastMarker.seqNum + 1L;
            // Set new seqNum to the packet
            frame.setSequenceNumber(newSeqNum);

            frame.setClusterUUID(clusterProcessor.getProcessorState().getClusterView().getClusterUUID());
            // Send packet
//            if (LOG.isDebugEnabled()) {
//               LOG.debug("Sending, Ri: " + clusterService.getHighestSequenceNumberReceived() + ", newSeqNum: " + newSeqNum + ", i: " + i + ", frame: " + frame);
//            }
            clusterProcessor.sendMulticastFrame(frame);

            // Receive our own packet

            clusterProcessor.getProcessorState().getReceivedList().add(frame);

            // Set new seqNum to the marker
            multicastMarker.seqNum = newSeqNum;

            // Decrement number of packet allowed to send. It can go negative because we may
            // have to over-send to send all parts of an object.
            messagesAllowedToSend--;
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Handle join request(s) pending at this node.
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

      // NOTE: simeshev@cacheonix.org - 2010-08-25 - Join request can be handled only when there
      // is no delivery round or all frames in the current delivery round has been delivered.
      if (multicastMarker.originator == null || (multicastMarker.current != null && multicastMarker.previous != null && multicastMarker.current.equals(multicastMarker.previous))) {

         // NOPMD
         final LinkedList<JoiningNode> joinRequests = clusterProcessor.getProcessorState().getJoinRequests();
         if (!joinRequests.isEmpty()) {

            // NOTE: simeshev@cacheonix.org - 2011-04-13 - It is important to check if this node is in 'Leave' becuase
            // it means that it is in the second state of leaving, and ir shouldn't become a join coordinator because
            // it won't be able to forward the stored marker becuase it may be dead by the time joining node response
            // to the MarkerListRequest. See bug CACHEONIX-307 for more information.
            if (!self.equals(multicastMarker.getLeave())) {

               if (!multicastMarker.isJoiningNodeSet()) {

                  // Add joining node to the cluster view
                  final JoiningNode joiningNode = joinRequests.removeFirst();

                  // Check if already joined
                  final ClusterNodeAddress joiningNodeAddress = joiningNode.getAddress();

                  if (!clusterProcessor.getProcessorState().getClusterView().contains(joiningNodeAddress)) {

                     // Insert immediately after ourselves

                     clusterProcessor.getProcessorState().getClusterView().insert(self, joiningNode);

                     clusterProcessor.getProcessorState().updateLastOperationalClusterView(clusterProcessor.getProcessorState().getClusterView());

                     // Set up join in the marker
                     final long joinSeqNum = multicastMarker.seqNum + 1;
                     multicastMarker.setJoiningNode(joiningNode);
                     multicastMarker.joinSeqNum = joinSeqNum;
                     multicastMarker.seqNum = joinSeqNum;
                     multicastMarker.setPredecessor(self);
                     multicastMarker.setProcessor(clusterProcessor); // May be needed at join finishing

                     // Create marker list

                     final MarkerListRequest markerListRequest = new MarkerListRequest(self, clusterProcessor.getProcessorState().getClusterView(),
                             clusterProcessor.getProcessorState().getLastOperationalClusterView(), clusterProcessor.getProcessorState().getReplicatedState(),
                             clusterProcessor.getMessageAssembler().getParts());
                     markerListRequest.setReceiver(joiningNodeAddress);

                     // Remember marker to forward
                     ((MarkerListRequest.Waiter) markerListRequest.getWaiter()).setMarkerToForward(multicastMarker);

                     // The joining node will respond with success which will forward a stored marker to forward
                     clusterProcessor.post(markerListRequest);
                     if (LOG.isDebugEnabled()) {
                        LOG.debug("Posted marker list to " + joiningNodeAddress);
                     }


                     // We return instead of forwarding the marker because the marker will be sent
                     // upon receiving a response MarkerListRequest. See MarkerListRequest.Waiter
                     // for more information.
                     return;
                  }
               }
            }
         }
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      //
      // Forward marker
      //
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//      if (clusterService.getHighestSequenceNumberDelivered() >=3)  {
//         throw new IllegalStateException("Thrown to simulate loss of a marker");
//      }

      multicastMarker.setProcessor(getProcessor());
      multicastMarker.forward();
   }


   /**
    * Creates a copy of this multicast marker suitable for forwarding.
    *
    * @return the copy of this multicast marker suitable for forwarding.
    */
   private MulticastMarker copy() {

      final MulticastMarker result = new MulticastMarker();
      result.setNextAnnouncementTime(getNextAnnouncementTime());
      result.setRequiresSameCluster(isRequiresSameCluster());
      result.setPredecessor(getPredecessor());
      result.setLeave(getLeave());
      result.setJoiningNode(getJoiningNode());
      result.current = current;
      result.originator = originator;
      result.seqNum = seqNum;
      result.previous = previous;
      result.leaveSeqNum = leaveSeqNum;
      result.joinSeqNum = joinSeqNum;
      return result;
   }


   /**
    * Processes multicast marker from another cluster. It is possible that we are joining that cluster because network
    * communication was restored after between two sub-clusters.
    */
   private void processForeign() {

      final ClusterProcessor processor = getClusterProcessor();
      final ClusterNodeAddress self = processor.getAddress();

      final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();
      final MulticastMarker marker = copy();

      // Check if this is a marker from a cluster we are joining
      if (!(joinStatus.isJoining() && joinStatus.isReceivedMarkerList())) {

         // Log
         final String errorResult = "Refused to join foreign cluster, marker: " + this;


         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         // Respond
         final Response response = createResponse(Response.RESULT_ERROR);
         response.setResult(errorResult);
         processor.post(response);
         return;
      }

      // Check if the marker is from the expected cluster.
      if (!joinStatus.getJoiningToCluster().getClusterUUID().equals(getClusterUUID())) {

         // Log
         final String errorDescription = "First foreign marker is NOT from the expected cluster: " + getClusterUUID()
                 + ", expected: " + joinStatus.getJoiningToCluster().getClusterUUID();
         LOG.warn(errorDescription);

         // Respond
         final Response response = createResponse(Response.RESULT_ERROR);
         response.setResult(errorDescription);
         processor.post(response);
         return;
      }

      // Respond with success
      processor.post(createResponse(Response.RESULT_SUCCESS));

      if (LOG.isDebugEnabled()) {
         LOG.debug("+++++++++++++++++++++++++++++++++ Node " + self.getTcpPort() + " joining another cluster: " + this);
      }

//      context.getMulticastMessageListeners().notifyReset();
//
      processor.reset();
      processor.getProcessorState().setHighestSequenceNumberDelivered(marker.previous);

      processor.getProcessorState().getReceivedList().setHighestContinuousNumberReceived(marker.previous);

      processor.getProcessorState().getReceivedList().setHighestSequenceNumberReceived(marker.previous);

      processor.getProcessorState().setClusterView(joinStatus.getJoiningToCluster());
      processor.getRouter().setClusterUUID(joinStatus.getJoiningToCluster().getClusterUUID());

      processor.getProcessorState().getReplicatedState().reset(joinStatus.getReplicatedState());
      processor.getMessageAssembler().setParts(joinStatus.getMessageAssemblerParts());
      joinStatus.clear();

      // Notify
//      final ClusterView previousClusterView = context.getLastOperationalClusterView();

      final ClusterView currentClusterView = processor.getProcessorState().getClusterView();

      // Update last operational cluster view

      processor.getProcessorState().updateLastOperationalClusterView(currentClusterView);

      // Left consists of all nodes of the previous operational cluster view
//      final int nodesLeftSize = previousClusterView == null ? 0 : previousClusterView.getSize();
//      final Set<ClusterNodeAddress> nodesLeft = new HashSet<ClusterNodeAddress>(nodesLeftSize);
//      if (previousClusterView != null) {
//         nodesLeft.addAll(previousClusterView.getClusterNodeList());
//      }

      // Join consists of all nodes of the new operational (current) cluster view
//      final int nodesJoinedSize = currentClusterView.getSize();
//      final Set<ClusterNodeAddress> nodesJoined = new HashSet<ClusterNodeAddress>(nodesJoinedSize);
//      nodesJoined.addAll(currentClusterView.getClusterNodeList());
//
//      context.getMulticastMessageListeners().notifyNodesLeft(nodesLeft);
//      context.getMulticastMessageListeners().notifyNodesJoined(nodesJoined);

      sendJoinedToSelf(marker.joinSeqNum, marker.getJoiningNode().getAddress());

      // Forward marker

      LOG.info("Switched to majority cluster: " + processor.getProcessorState().getClusterView());

      // REVIEWME: simeshev@cacheonix.org - 2010-07-07 - What are the implications
      // of sending the new marker to self instead of just forwarding it?

      marker.setReceiver(processor.getAddress());
      processor.post(marker);
   }


   /**
    * Executes this request while context is in the blocked state.
    */
   protected void processBlocked() {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Received multicast marker: " + this);
      }

      final ClusterProcessor processor = getClusterProcessor();

      final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();
      final MulticastMarker marker = copy();


      if (joinStatus.isJoining() && joinStatus.isReceivedMarkerList()) {

         // Receive marker in normal state
         processor.post(createResponse(ClusterResponse.RESULT_SUCCESS));

         // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
         //
         // We joined the majority.
         //
         // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

         if (LOG.isDebugEnabled()) {
            LOG.debug("joinStatus: " + joinStatus);
         }

         // Check if the marker is from the expected cluster.
         if (!joinStatus.getJoiningToCluster().getClusterUUID().equals(getClusterUUID())) {

            final String errorDescription = "First marker is NOT from the expected cluster: " + getClusterUUID()
                    + ", expected: " + joinStatus.getJoiningToCluster().getClusterUUID();
            LOG.warn(errorDescription);
            final Response errorResponse = createResponse(ClusterResponse.RESULT_ERROR);
            errorResponse.setResult(errorDescription);
            processor.post(errorResponse);

            return;
         }

         processor.reset();

         processor.getProcessorState().getReplicatedState().reset(joinStatus.getReplicatedState());
         processor.getMessageAssembler().setParts(joinStatus.getMessageAssemblerParts());

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled())
            LOG.debug("Setting highest number delivered to marker.previous: " + marker.previous); // NOPMD
         processor.getProcessorState().setHighestSequenceNumberDelivered(marker.previous);

         //noinspection ControlFlowStatementWithoutBraces
         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled())
            LOG.debug("Setting highest number received to marker.previous: " + marker.previous); // NOPMD

         processor.getProcessorState().getReceivedList().setHighestContinuousNumberReceived(marker.previous);

         processor.getProcessorState().getReceivedList().setHighestSequenceNumberReceived(marker.previous);

         processor.getProcessorState().setClusterView(joinStatus.getJoiningToCluster());
         processor.getRouter().setClusterUUID(joinStatus.getJoiningToCluster().getClusterUUID());

         processor.getProcessorState().updateLastOperationalClusterView(joinStatus.getLastOperationalClusterView());
         joinStatus.clear();

         // Change state
         processor.getProcessorState().setState(ClusterProcessorState.STATE_NORMAL);

         // Cancel 'home alone' timeout
         processor.getProcessorState().getHomeAloneTimeout().cancel();

         // Notify cluster event subscribers
         notifySubscribersClusterStateChanged(ClusterState.OPERATIONAL);


         // Calculate nodes left and joined
//         final ClusterView previousClusterView = context.getLastOperationalClusterView();

         final ClusterView currentClusterView = processor.getProcessorState().getClusterView();

         // Update last operational cluster view

         processor.getProcessorState().updateLastOperationalClusterView(currentClusterView);

         // Left consists of all nodes of the previous operational cluster view
//         final int nodesLeftSize = previousClusterView == null ? 0 : previousClusterView.getSize();
//         final Set<ClusterNodeAddress> nodesLeft = new HashSet<ClusterNodeAddress>(nodesLeftSize);
//         if (previousClusterView != null) {
//            nodesLeft.addAll(previousClusterView.getClusterNodeList());
//         }

         // Join consists of all nodes of the new operational (current) cluster view
//         final int nodesJoinedSize = currentClusterView.getSize();
//         final Set<ClusterNodeAddress> nodesJoined = new HashSet<ClusterNodeAddress>(nodesJoinedSize);
//         nodesJoined.addAll(currentClusterView.getClusterNodeList());

         // Notify
//         context.getMulticastMessageListeners().notifyNodesLeft(nodesLeft);
//         context.getMulticastMessageListeners().notifyNodesJoined(nodesJoined);

         sendJoinedToSelf(marker.joinSeqNum, marker.getJoiningNode().getAddress());

         // REVIEWME: simeshev@cacheonix.org - 2010-07-04 - What are the implications
         // of sending the new marker to self instead of just forwarding it?
         marker.setReceiver(processor.getAddress());
         processor.post(marker);

         LOG.info("Joined majority cluster: " + processor.getProcessorState().getClusterView());
      } else {

         final String errorDescription = "Blocked state should not receive a multicast marker it is " +
                 "not in the joining state. Our address: " + processor.getAddress() + ", marker: " + marker;
         if (LOG.isDebugEnabled()) {
            LOG.debug(errorDescription);
         }
         final Response errorResponse = createResponse(ClusterResponse.RESULT_ERROR);
         errorResponse.setResult(errorDescription);
         processor.post(errorResponse);
      }
   }


   /**
    * Executes this request while context is in the cleanup state.
    */
   protected void processCleanup() {


      final ClusterProcessor processor = getClusterProcessor();

      // It is possible that this is a delayed first multicast marker from some other
      // cluster that we previously tried to join, or for that matter, any roving
      // multicast marker. We need to make sure that this is a marker coming in
      // as a result of the normal completion of the cleanup process.
      //
      // This is detected as the marker having the same clusterUUID as this
      // node becuase the cluster view was stabilized as a part of recovery.

      if (!getClusterProcessor().getProcessorState().getClusterView().getClusterUUID().equals(getClusterUUID())) {

         // Respond with success
         final String errorResult = "Received a marker from a foreign cluster: " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response response = createResponse(ClusterResponse.RESULT_ERROR);
         response.setResult(errorResult);
         processor.post(response);

         return;
      }

      // Respond with success
      processor.post(createResponse(ClusterResponse.RESULT_SUCCESS));

      if (LOG.isDebugEnabled()) {
         LOG.debug("Received multicast marker: " + this);
      }
      // Notify subscribers about changes in the configuration

      // REVIEWME: slava@cacheonix.org - 2009-12-23 - This is a cut and paste
      // everywhere where new NormalState() is found. Consider re-using.

      final ClusterView previousClusterView = processor.getProcessorState().getLastOperationalClusterView();

      final ClusterView currentClusterView = processor.getProcessorState().getClusterView();

      // Update last operational cluster view

      processor.getProcessorState().updateLastOperationalClusterView(currentClusterView);

      final Set<ClusterNodeAddress> nodesLeft = currentClusterView.calculateNodesLeft(previousClusterView);
      final Set<ClusterNodeAddress> nodesJoined = currentClusterView.calculateNodesJoined(previousClusterView);

      // Notify about nodes left to adjust waiters
      processor.notifyNodesLeft(nodesLeft);

      // Account for the originator of the cleanup node posting left and join to self
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("seqNum at the beginning: " + seqNum); // NOPMD

      long adjustedSeqNum = seqNum - (nodesLeft.size() + nodesJoined.size());

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Adjusted seqNum at the beginning: " + adjustedSeqNum); // NOPMD

      // Send left to self
      for (final ClusterNodeAddress left : nodesLeft) {

         sendLeftToSelf(++adjustedSeqNum, left);
      }

      // Send joined to self
      for (final ClusterNodeAddress joined : nodesJoined) {

         sendJoinedToSelf(++adjustedSeqNum, joined);
      }

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Adjusted seqNum at the end: " + adjustedSeqNum); // NOPMD

      // Reset join state. Switching to a stable state should clear join state possibly
      // acquired before going through recovery process. See CACHEONIX-279 for details.
      processor.getProcessorState().getJoinStatus().clear();

      // Change state
      processor.getProcessorState().setState(ClusterProcessorState.STATE_NORMAL);

      // Cancel 'home alone' timeout
      processor.getProcessorState().getHomeAloneTimeout().cancel();

      // Notify cluster event subscribers
      notifySubscribersClusterStateChanged(ClusterState.OPERATIONAL);

      // Forward instead of receiving itself to support synchronous delivery of configuration messages.
      final MulticastMarker marker = copy();

      marker.setReceiver(processor.getProcessorState().getClusterView().getNextElement());
      marker.seqNum = adjustedSeqNum;
      processor.post(marker);
   }


   /**
    * Executes this request while context is in the recovery state.
    */
   protected void processRecovery() {

      final ClusterProcessor processor = getClusterProcessor();

      if (processor.getProcessorState().getClusterView().getClusterUUID().equals(getClusterUUID())) {

         // This cluster processor is in the Recovery state, and this is a
         // MulticastMarker from the same cluster UUID. This means that most
         // likely this is a marker from the node that took too long to forward
         // the normal marker that most likely initiated the recovery. We swallow
         // this marker by returning RESULT_SUCCESS and then doing nothing in hope 
         // that the sender will soon receive a recovery marker. Otherwise the sender
         // will initiate its own recovery.

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Destroying a marker that took too long to forward: " + this); // NOPMD

         processor.post(createResponse(ClusterResponse.RESULT_SUCCESS));
      } else {

         // Respond with error. The sender will initiate its own recovery.
         final String errorResult = "Recovery mode at " + getProcessor().getAddress() + " does not support multicast markers: " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response response = createResponse(ClusterResponse.RESULT_ERROR);
         response.setResult(errorResult);
         processor.post(response);
      }
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
      seqNum = in.readLong();
      current = SerializerUtils.readLong(in);
      previous = SerializerUtils.readLong(in);
      leaveSeqNum = SerializerUtils.readLong(in);
      joinSeqNum = SerializerUtils.readLong(in);
      originator = SerializerUtils.readAddress(in);
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
      out.writeLong(seqNum);
      SerializerUtils.writeLong(out, current);
      SerializerUtils.writeLong(out, previous);
      SerializerUtils.writeLong(out, leaveSeqNum);
      SerializerUtils.writeLong(out, joinSeqNum);
      SerializerUtils.writeAddress(originator, out);
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

      final MulticastMarker that = (MulticastMarker) o;

      if (seqNum != that.seqNum) {
         return false;
      }
      if (current != null ? !current.equals(that.current) : that.current != null) {
         return false;
      }
      if (joinSeqNum != null ? !joinSeqNum.equals(that.joinSeqNum) : that.joinSeqNum != null) {
         return false;
      }
      if (leaveSeqNum != null ? !leaveSeqNum.equals(that.leaveSeqNum) : that.leaveSeqNum != null) {
         return false;
      }
      if (originator != null ? !originator.equals(that.originator) : that.originator != null) {
         return false;
      }
      if (previous != null ? !previous.equals(that.previous) : that.previous != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (originator != null ? originator.hashCode() : 0);
      result = 31 * result + (int) (seqNum ^ (seqNum >>> 32));
      result = 31 * result + (current != null ? current.hashCode() : 0);
      result = 31 * result + (previous != null ? previous.hashCode() : 0);
      result = 31 * result + (leaveSeqNum != null ? leaveSeqNum.hashCode() : 0);
      result = 31 * result + (joinSeqNum != null ? joinSeqNum.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "MulticastMarker{" +
              "sender=" + getSender() +
              ", seqNum=" + seqNum +
              ", originator=" + originator +
              ", current=" + current +
              ", previous=" + previous +
              ", joinSeqNum=" + joinSeqNum +
              ", leaveSeqNum=" + leaveSeqNum +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new MulticastMarker();
      }
   }
}
