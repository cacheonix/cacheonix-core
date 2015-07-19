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
import java.util.Set;

import org.cacheonix.cluster.ClusterState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Cleanup marker. The cleanup marker circulates in the ring in during the second stage of recovery.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
@SuppressWarnings("RedundantIfStatement")
public final class CleanupMarker extends MarkerRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CleanupMarker.class); // NOPMD

   /**
    * Originator of the cleanup round.
    */
   private ClusterNodeAddress originator = null;

   private Long first = null; // not set

   private Long current = null; // not set

   private Long previous = null; // not set

   private Long highSeqNum = null; // not set


   /**
    * Default constructor required by Externalizable.
    */
   public CleanupMarker() { // NOPMD

      super(TYPE_CLUSTER_CLEANUP_MARKER);
   }


   /**
    * Creates a new recovery marker with fields set according to the protocol and set self as an originator.
    *
    * @param processor the cluster processor creating the marker.
    * @return a new recovery marker with fields set according to the protocol and set self as an originator.
    */
   public static CleanupMarker originate(final ClusterProcessor processor) {

      // Si

      final Long smallestNumberReceivedButUndelivered = processor.getProcessorState().getReceivedList().getSmallestNumberReceivedButNotDelivered();

      // Ti

      final Long highestContinuousNumberReceivedButUndelivered = processor.getProcessorState().getReceivedList().getHighestContinuousNumberReceivedButNotDelivered();

      // (1) During a clean-up round, the largest sequence number of any received message is
      //     determined as follows: The originator sets HighSeqNum to the highest sequence number
      //     of any message it has received but not delivered. Each stub that receives the cleanup
      //     marker compares the largest sequence number l of any message that it has received with
      //     the number in the HighSeqNum field. If HighSeqNum is smaller, the stub overwrites the
      //     field with l.

      final Long highestNumberReceivedButNotDelivered = processor.getProcessorState().getReceivedList().getHighestNumberReceivedButNotDelivered();

      final CleanupMarker cleanupMarker = new CleanupMarker();

      cleanupMarker.setClusterUUID(processor.getProcessorState().getClusterView().getClusterUUID());
      cleanupMarker.first = smallestNumberReceivedButUndelivered;
      cleanupMarker.current = highestContinuousNumberReceivedButUndelivered;
      cleanupMarker.previous = null;
      cleanupMarker.originator = processor.getAddress();
      cleanupMarker.highSeqNum = highestNumberReceivedButNotDelivered;

      // Return the result
      return cleanupMarker;
   }


   /**
    * {@inheritDoc}
    */
   protected void processNormal() {

      final String errorResult = "Normal state does not accept Cleanup marker: " + this;

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

      final Response errorResponse = createResponse(ClusterResponse.RESULT_ERROR);
      errorResponse.setResult(errorResult);
      getProcessor().post(errorResponse);
   }


   /**
    * {@inheritDoc}
    */
   protected void processBlocked() {

      final ClusterProcessor processor = getClusterProcessor();

      if (!processor.getProcessorState().getClusterView().contains(originator)) {

         final String errorResult = "Cleanup marker from an unknown originator: " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response errorResponse = createResponse(ClusterResponse.RESULT_ERROR);
         errorResponse.setResult(errorResult);
         processor.post(errorResponse);

         return;
      }

      // Respond with success
      processor.post(createResponse(ClusterResponse.RESULT_SUCCESS));

      // Set Cleanup state
      processor.getProcessorState().setState(ClusterProcessorState.STATE_CLEANUP);

      // Cancel 'home alone' timeout
      processor.getProcessorState().getHomeAloneTimeout().cancel();

      // Notify cluster event subscribers
      notifySubscribersClusterStateChanged(ClusterState.RECONFIGURING);

      // REVIEWME: simeshev@cacheonix.org - 2010-07-07 - What are the implications
      // of sending the new marker to self instead of just forwarding it?

      // Receive marker in recovery state
      final CleanupMarker cleanupMarker = copy();
      cleanupMarker.setReceiver(processor.getAddress());
      processor.post(cleanupMarker);
   }


   /**
    * {@inheritDoc}
    */
   protected void processRecovery() {

      final ClusterProcessor processor = getClusterProcessor();

      // Respond with success
      processor.post(createResponse(ClusterResponse.RESULT_SUCCESS));

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Joining cleanup round: " + processor.getAddress());

      // Set Cleanup state
      processor.getProcessorState().setState(ClusterProcessorState.STATE_CLEANUP);

      // Cancel 'home alone' timeout
      processor.getProcessorState().getHomeAloneTimeout().cancel();

      // REVIEWME: simeshev@cacheonix.org - 2010-07-07 - What are the implications
      // of sending the new marker to self instead of just forwarding it?

      // Receive marker in recovery state
      final CleanupMarker cleanupMarker = copy();
      cleanupMarker.setReceiver(processor.getAddress());
      processor.post(cleanupMarker);
   }


   /**
    * {@inheritDoc}
    */
   protected void processCleanup() throws IOException {

      final ClusterProcessor processor = getClusterProcessor();
      final CleanupMarker cleanupMarker = copy();
      final ClusterNodeAddress self = processor.getAddress();

      // Respond with success
      processor.post(createResponse(ClusterResponse.RESULT_SUCCESS));

      // Receive frames
      receiveFrames();

      // S

      final Long smallestNumberReceivedButNotDelivered = processor.getProcessorState().getReceivedList().getSmallestNumberReceivedButNotDelivered();

      // T

      final Long highestContinuousNumberReceivedButNotDelivered = processor.getProcessorState().getReceivedList().getHighestContinuousNumberReceivedButNotDelivered();

      // D
      final Long highestSequenceNumberDelivered = processor.getProcessorState().getHighestSequenceNumberDelivered();

      // REVIEWME: simeshev@cacheonix.org - 2008-04-09 - Let's assume for now that we can process
      // the returned marker inline with the fist time marker.

      if (self.equals(cleanupMarker.originator)) {

         // (11) When the originator of a cleanup round finds that all stubs have delivered
         //      messages up to HighSeqNum, that is, Previous = HighSeqNum, it generates a
         //      multicast marker and the normal mode of operation is resumed.
         if (cleanupMarker.isAllMessagesDelivered()) {

            LOG.debug("Coordinator found that all nodes delivered messages up to " + cleanupMarker.highSeqNum + ", switching to Normal");

            // Notify subscribers about changes in the configuration

            final ClusterView previousClusterView = processor.getProcessorState().getLastOperationalClusterView();

            final ClusterView currentClusterView = processor.getProcessorState().getClusterView();

            // Update last operational cluster view

            processor.getProcessorState().updateLastOperationalClusterView(currentClusterView);

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("previousClusterView: " + previousClusterView); // NOPMD

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("currentClusterView: " + currentClusterView); // NOPMD


            // Calculate nodes left and joined. All nodes finishing
            // cleanup should produce the same nodes left and joined
            final Set<ClusterNodeAddress> nodesLeft = currentClusterView.calculateNodesLeft(previousClusterView);
            final Set<ClusterNodeAddress> nodesJoined = currentClusterView.calculateNodesJoined(previousClusterView);

            // Notify processor tha all nodes left to release waiters
            processor.notifyNodesLeft(nodesLeft);

            // Create a seq num for left and joined nodes

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("highSeqNum before adjustment: " + cleanupMarker.highSeqNum); // NOPMD

            long adjustedSeqNum = cleanupMarker.highSeqNum == null ? 0L : cleanupMarker.highSeqNum;

            // Send left to self
            for (final ClusterNodeAddress left : nodesLeft) {

               sendLeftToSelf(++adjustedSeqNum, left);
            }

            // Send joined to self
            for (final ClusterNodeAddress joined : nodesJoined) {

               sendJoinedToSelf(++adjustedSeqNum, joined);
            }

            // Reset join state. Switching to a stable state should clear join state possibly
            // acquired before going through recovery process. See CACHEONIX-279 for details.

            processor.getProcessorState().getJoinStatus().clear();

            // Change state
            processor.getProcessorState().setState(ClusterProcessorState.STATE_NORMAL);

            // Cancel 'home alone' timeout
            processor.getProcessorState().getHomeAloneTimeout().cancel();

            // Notify cluster event subscribers
            notifySubscribersClusterStateChanged(ClusterState.OPERATIONAL);

            // Receive marker
            final MulticastMarker multicastMarker = new MulticastMarker(currentClusterView.getClusterUUID());
            multicastMarker.setNextAnnouncementTime(processor.getClock().currentTime());

            // NOTE: simeshev@cacheonix.org - 2010-02-09 - We set it to zero
            // if it is null because null means "there were no messages"
            multicastMarker.setSeqNum(adjustedSeqNum);
            multicastMarker.setPrevious(cleanupMarker.highSeqNum);

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("Forwarding first multicast marker: " + multicastMarker); // NOPMD

            multicastMarker.setReceiver(processor.getProcessorState().getClusterView().getNextElement());
            processor.post(multicastMarker);
         } else {

            // marker.getPrevious() != marker.getHighSeqNum()

            // (4) Retransmit missed messages. We do this before delivery (3) because delivery cleans
            //     up the received list.
            if (cleanupMarker.current < highestContinuousNumberReceivedButNotDelivered) {

               final long retransmitNumBegin = cleanupMarker.current + 1L;

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled())
                  LOG.debug("Retransmitting missed messages from " + retransmitNumBegin + " to " + highestContinuousNumberReceivedButNotDelivered);

               for (long num = retransmitNumBegin; num <= highestContinuousNumberReceivedButNotDelivered; num++) {

                  // Send packet - note re-setting cluster UUID because it was received from the previous cluster
                  // configuration. REVIEWME: slava@cacheonix.org - 2009-12-28 - consider re-setting it once
                  // when switching to the recovery mode.

                  final Frame frame = processor.getProcessorState().getReceivedList().getMessage(num);

                  frame.setClusterUUID(processor.getProcessorState().getClusterView().getClusterUUID());
                  processor.sendMulticastFrame(frame);
               }
            }

            // (12) Finally, if the originator has no more undelivered messages, but Previous
            //      != HighSeqNum, it sets Originator = NULL and forwards the clean-up marker to
            //      the next stub.

            if (processor.getProcessorState().getReceivedList().isEmpty()) {

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled())
                  LOG.debug("Coordinator does not have any more undelivered messages, setting Coordinator to null ");

               cleanupMarker.originator = null;

               forwardCleanupMarker(cleanupMarker);
            } else {

               // (3) Originator delivers any outstanding messages with sequence numbers less then or
               //     equal to Current.
               final long startDeliveryNum = highestSequenceNumberDelivered == null ? 1L : highestSequenceNumberDelivered + 1L;
               final long endDeliveryNum = cleanupMarker.current;
               deliver(startDeliveryNum, endDeliveryNum);

               // (5) Copies Current to Previous, sets First to Si, Current to Ti, and begins another
               //     round
               //
               cleanupMarker.previous = cleanupMarker.current;

               cleanupMarker.first = processor.getProcessorState().getReceivedList().getSmallestNumberReceivedButNotDelivered();

               cleanupMarker.current = processor.getProcessorState().getReceivedList().getHighestContinuousNumberReceivedButNotDelivered();

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled())
                  LOG.debug("Si: " + processor.getProcessorState().getReceivedList().getSmallestNumberReceivedButNotDelivered() + " / Ti: " + processor.getProcessorState().getReceivedList().getHighestContinuousNumberReceivedButNotDelivered());

               forwardCleanupMarker(cleanupMarker);
            }
         }
      } else {

         if (LOG.isDebugEnabled()) {

            LOG.debug("Not an originator, smallestNumberReceivedButNotDelivered: " + processor.getProcessorState().getReceivedList().getSmallestNumberReceivedButNotDelivered()
                    + ", highestContinuousNumberReceivedButNotDelivered: " + processor.getProcessorState().getReceivedList().getHighestContinuousNumberReceivedButNotDelivered()
                    + ", highestNumberReceivedButNotDelivered: " + processor.getProcessorState().getReceivedList().getHighestNumberReceivedButNotDelivered()
                    + ", highestContinuousNumberReceived: " + processor.getProcessorState().getReceivedList().getHighestContinuousNumberReceived()
                    + ", highestSequenceNumberReceived: " + processor.getProcessorState().getReceivedList().getHighestSequenceNumberReceived()
                    + ", highestSequenceNumberDelivered: " + processor.getProcessorState().getHighestSequenceNumberDelivered()
            );
         }


         // (1) During a clean-up round, the largest sequence number of any received message is
         //     determined as follows: The originator sets HighSeqNum to the highest sequence number
         //     of any message it has received but not delivered. Each stub that receives the cleanup
         //     marker compares the largest sequence number L of any message that it has received with
         //     the number in the HighSeqNum field. If HighSeqNum is smaller, the stub overwrites the
         //     field with L.

         final Long highestSequenceNumberReceived = processor.getProcessorState().getReceivedList().getHighestSequenceNumberReceived();
         if (highestSequenceNumberReceived != null && (cleanupMarker.highSeqNum == null || (highestSequenceNumberReceived > cleanupMarker.highSeqNum))) {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled())
               LOG.debug("Setting highest sequence number to : " + highestSequenceNumberReceived);

            cleanupMarker.highSeqNum = highestSequenceNumberReceived;
         }

         // Check if originator is set

         if (cleanupMarker.originator != null) {

            //
            // (13) Upon receiving a clean-up maker, a stub j compares Sj with first.
            //
            if (smallestNumberReceivedButNotDelivered == null) {

               // There are no received but undelivered

               if (highestSequenceNumberDelivered != null && cleanupMarker.current != null && highestSequenceNumberDelivered < cleanupMarker.current) {

                  // (10) If, on other hand, Sj == NULL and Dj < Current, then J sets Current Dj
                  cleanupMarker.current = highestSequenceNumberDelivered;
               }
            } else {

               // There are received but undelivered

               if (cleanupMarker.first == null || smallestNumberReceivedButNotDelivered < cleanupMarker.first) {

                  // (7) Stub takes over as originator because it has more
                  // undelivered messages then the previous originator
                  LOG.debug("Stub takes over as coordinator because it has more undelivered (" + smallestNumberReceivedButNotDelivered + ") messages then the previous coordinator (" + cleanupMarker.first + ')');
                  cleanupMarker.first = smallestNumberReceivedButNotDelivered;
                  cleanupMarker.current = highestContinuousNumberReceivedButNotDelivered;
                  cleanupMarker.originator = self;

                  // Forward cleanup marker and exit now.
                  forwardCleanupMarker(cleanupMarker);

                  return;
               } else if (smallestNumberReceivedButNotDelivered > cleanupMarker.first
                       && smallestNumberReceivedButNotDelivered > highestSequenceNumberDelivered + 1L) {

                  // (8) Missed the message with sequence number First

                  cleanupMarker.current = cleanupMarker.first - 1L;

                  if (LOG.isDebugEnabled()) {
                     LOG.debug("Missed the message with sequence number First (" + cleanupMarker.first + ')' +
                             ", have set Current to: " + cleanupMarker.current
                             + ", Sj: " + smallestNumberReceivedButNotDelivered
                             + ", Dj + 1: " + (highestSequenceNumberDelivered + 1L));
                  }

               } else if ((smallestNumberReceivedButNotDelivered.equals(cleanupMarker.first)
                       || smallestNumberReceivedButNotDelivered == highestSequenceNumberDelivered + 1L)
                       && highestContinuousNumberReceivedButNotDelivered < cleanupMarker.current) {

                  // (9) Because it has not received a message between First and Current


                  //noinspection ControlFlowStatementWithoutBraces
                  if (LOG.isDebugEnabled())
                     LOG.debug("Has not received a message between First and Current, setting Current to: " + highestContinuousNumberReceivedButNotDelivered);

                  cleanupMarker.current = highestContinuousNumberReceivedButNotDelivered;
               }
            }

            // (6) When a stub receives the clean-up marker with previous not NULL, it delivers those
            //     messages whose sequence numbers are less then or equal to Previous

            if (cleanupMarker.previous != null) {

               final long startDeliveryNum = highestSequenceNumberDelivered == null ? 1L : highestSequenceNumberDelivered + 1L;
               final long endDeliveryNum = cleanupMarker.previous;
               deliver(startDeliveryNum, endDeliveryNum);

               // We are here because the delivery round is in progress. The indication that all
               // nodes delivered messages up to Previous is that Current > Previous. This happens
               // when the coordinator of the round has extended the current round past Previous.
               // And this happens when all up the Previous have been delivered.
               //
               // The other possibility is that the originator of the delivery round finished it (7)
               // and set current to null.

               if (cleanupMarker.current == null || cleanupMarker.current > cleanupMarker.previous) {

                  processor.notifyDeliveredToAll(cleanupMarker.previous);
               }
            }
         } else {

            // Originator is *not* set.

            // Take over as originator if there are undelivered messages

            if (!processor.getProcessorState().getReceivedList().isEmpty()) {

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled())
                  LOG.debug("Taking over as coordinator because the coordinator is null and this node has undelivered messages");

               cleanupMarker.first = smallestNumberReceivedButNotDelivered;
               cleanupMarker.current = highestContinuousNumberReceivedButNotDelivered;
               cleanupMarker.originator = self;
            }

            // Notify messages waiting for the delivery notification
            if (cleanupMarker.previous != null) {

               processor.notifyDeliveredToAll(cleanupMarker.previous);
            }
         }

         //
         forwardCleanupMarker(cleanupMarker);
      }
   }


   /**
    * Returns <code>true</code> if coordinator found that all nodes delivered messages up to HighSeqNum.
    *
    * @return <code>true</code> if coordinator found that all nodes delivered messages up to HighSeqNum.
    */
   private boolean isAllMessagesDelivered() {

      // NOTE: simeshev@cacheonix.org - 2010-12-18 - The first part ensures completion when: a) there have been messages
      // and all have been delivered (e.g normal completion); b) there have not been any messages. The second part
      // ensures completion when there have not been undelivered messages but there were previous messages.

      return ((first == null) && (current == null) && (previous == null) && highSeqNum != null) || ((previous == null && highSeqNum == null) || (previous != null && previous != null && previous.equals(highSeqNum)));
   }


   /**
    * Delivers messages.
    *
    * @param startDeliveryNum message number to begin delivery with
    * @param endDeliveryNum   message number to deliver messages up to
    * @throws IOException if an I/O error occurs during delivery
    */
   private void deliver(final long startDeliveryNum, final long endDeliveryNum) throws IOException {

      if (startDeliveryNum > endDeliveryNum) {
         return;
      }

      final ClusterProcessor processor = getClusterProcessor();

      if (LOG.isDebugEnabled()) {
         LOG.debug("Delivering messages from " + startDeliveryNum + " to " + endDeliveryNum);
      }

      // Move to the payload assembler.

      final ReceivedList receivedList = processor.getProcessorState().getReceivedList();
      final MessageAssembler messageAssembler = processor.getMessageAssembler();
      for (long messageNum = startDeliveryNum; messageNum <= endDeliveryNum; messageNum++) {

         final Frame part = receivedList.poll(messageNum);
         Assert.assertNotNull(part, "Received null frame for message number {0}, the frame shouldn't be null", messageNum);
         validateMessageIsInSequence(messageNum, part);

         // Add to assembler
         messageAssembler.add(part);

         // Mark that a message has been delivered.
         processor.getProcessorState().setHighestSequenceNumberDelivered(messageNum);
      }

      // Notify
      processor.deliverAssembledMulticastMessages();
   }


   private static void validateMessageIsInSequence(final long messageNumToDeliver, final Frame frame) {

      if (messageNumToDeliver != frame.getSequenceNumber()) {
         throw new IllegalStateException("Receive queue contains a packet with "
                 + "unexpected number. Expected: " + messageNumToDeliver +
                 ", found: " + frame.getSequenceNumber());
      }
   }


   /**
    * Creates a copy of this <code>CleanupMarker</code> suitable for forwarding.
    *
    * @return the copy of this <code>CleanupMarker</code> suitable for forwarding.
    */
   private CleanupMarker copy() {

      final CleanupMarker result = new CleanupMarker();
      result.setRequiresSameCluster(isRequiresSameCluster());
      result.originator = originator;
      result.first = first;
      result.current = current;
      result.previous = previous;
      result.highSeqNum = highSeqNum;
      return result;
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      first = SerializerUtils.readLong(in);
      current = SerializerUtils.readLong(in);
      previous = SerializerUtils.readLong(in);
      highSeqNum = SerializerUtils.readLong(in);
      originator = SerializerUtils.readAddress(in);
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeLong(out, first);
      SerializerUtils.writeLong(out, current);
      SerializerUtils.writeLong(out, previous);
      SerializerUtils.writeLong(out, highSeqNum);
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

      final CleanupMarker that = (CleanupMarker) o;

      if (current != null ? !current.equals(that.current) : that.current != null) {
         return false;
      }
      if (first != null ? !first.equals(that.first) : that.first != null) {
         return false;
      }
      if (highSeqNum != null ? !highSeqNum.equals(that.highSeqNum) : that.highSeqNum != null) {
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
      result = 31 * result + (first != null ? first.hashCode() : 0);
      result = 31 * result + (current != null ? current.hashCode() : 0);
      result = 31 * result + (previous != null ? previous.hashCode() : 0);
      result = 31 * result + (highSeqNum != null ? highSeqNum.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "CleanupMarker{" +
              "originator=" + originator +
              ", first=" + first +
              ", current=" + current +
              ", previous=" + previous +
              ", highSeqNum=" + highSeqNum +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new CleanupMarker();
      }
   }
}
