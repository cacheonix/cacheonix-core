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
import java.util.LinkedList;
import java.util.List;

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * MarkerListMessage
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferenceInEquals, RedundantIfStatement @since Apr 1, 2008 10:04:47 PM
 */
public final class MarkerListRequest extends ClusterRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MarkerListRequest.class); // NOPMD

   private ClusterView clusterView = null;

   private ReplicatedState replicatedState = null;

   /**
    * Frames present in the message assembler at the time of creating MarkerListRequest.
    */
   private List<Frame> messageAssemblerParts = null;

   /**
    * A last operational cluster view to send to the joining node. Having the same last operational cluster view ensures
    * that all nodes produce the same left and joined lists on recovery and join.
    */
   private ClusterView lastOperationalClusterView = null;


   /**
    * Required by Externalizable.
    *
    * @noinspection WeakerAccess
    */
   public MarkerListRequest() {

      super(TYPE_CLUSTER_MARKER_LIST);
   }


   /**
    * @param sender                     sender.
    * @param clusterView                cluster view to send to the joining node.
    * @param lastOperationalClusterView last operational cluster view to send to the joining node. Having the same last
    *                                   operational cluster view ensures that all nodes produce the same left and joined
    *                                   lists on recovery and join.
    * @param replicatedState            the replicated state.
    * @param messageAssemblerParts      parts a list of frames present in the message assembler at the time of creating
    *                                   the
    */
   public MarkerListRequest(final ClusterNodeAddress sender, final ClusterView clusterView,
           final ClusterView lastOperationalClusterView, final ReplicatedState replicatedState,
           final List<Frame> messageAssemblerParts) {

      super(TYPE_CLUSTER_MARKER_LIST);
      setRequiresSameCluster(false);
      this.setSender(sender);
      this.clusterView = clusterView.copy();
      this.lastOperationalClusterView = lastOperationalClusterView == null ? null : lastOperationalClusterView.copy();
      this.replicatedState = replicatedState.copy();
      this.messageAssemblerParts = new LinkedList<Frame>(messageAssemblerParts);
   }


   public ClusterView getClusterView() {

      return clusterView;
   }


   /**
    * Returns a reference to an internal list with frames present in the message assembler at the time of creating
    * MarkerListRequest.
    *
    * @return the reference to the internal list frames present in the message assembler at the time of creating
    * MarkerListRequest.
    */
   public List<Frame> getMessageAssemblerParts() {

      return messageAssemblerParts;
   }


   /**
    * {@inheritDoc}
    */
   protected void processNormal() {

      processBlocked();
   }


   /**
    * {@inheritDoc}
    */
   protected void processBlocked() {

      final ClusterProcessor processor = getClusterProcessor();
      final ClusterNodeAddress self = processor.getAddress();

      final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();

      if (!joinStatus.isJoining()) {

         final String errorResult = "Received a marker list while join target was not set, respond with error: " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response response = createResponse(Response.RESULT_ERROR);
         response.setResult(errorResult);
         processor.post(response);

         return;
      }

      if (!joinStatus.getJoiningToProcess().equals(getSender())) {

         final String errorResult = "Received a marker list from a sender that we are not joining to: " + this;

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

         final Response response = createResponse(Response.RESULT_ERROR);
         response.setResult(errorResult);
         processor.post(response);

         return;
      }

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Received marker list: " + this); // NOPMD

      // Everything is fine - set clusterView to join status
      clusterView.setOwner(self);
      joinStatus.setJoiningToCluster(clusterView);
      joinStatus.setLastOperationalCluster(lastOperationalClusterView);

      // Set replicated state to join status
      final ReplicatedState replicatedState = this.replicatedState.copy();
      joinStatus.setReplicatedState(replicatedState);
      joinStatus.getTimeout().cancel();

      // Set parts
      joinStatus.setMessageAssemblerParts(messageAssemblerParts);

      // Respond OK
      processor.post(createResponse(Response.RESULT_SUCCESS));

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Posted success response: " + this); // NOPMD
   }


   /**
    * {@inheritDoc}
    */
   protected void processRecovery() {

      final String errorResult = "Node in Recovery state should never receive a marker list: " + getProcessor().getAddress();

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

      final Response response = createResponse(Response.RESULT_ERROR);
      response.setResult(errorResult);
      getProcessor().post(response);
   }


   /**
    * {@inheritDoc}
    */
   protected void processCleanup() {

      final String errorResult = "Node in Cleanup state should never receive a marker list: " + getProcessor().getAddress();

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

      final Response response = createResponse(Response.RESULT_ERROR);
      response.setResult(errorResult);
      getProcessor().post(response);
   }


   /**
    * {@inheritDoc}
    */
   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   /**
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      clusterView.writeWire(out);
      SerializerUtils.writeString(replicatedState.getClass().getName(), out);
      replicatedState.writeWire(out);

      //
      out.writeInt(messageAssemblerParts.size());
      for (final Frame frame : messageAssemblerParts) {
         frame.write(out);
      }

      //
      if (lastOperationalClusterView == null) {

         out.writeBoolean(false);
      } else {

         out.writeBoolean(true);
         lastOperationalClusterView.writeWire(out);
      }
   }


   /**
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      clusterView = new ClusterViewImpl();
      clusterView.readWire(in);
      final String repStateClassName = SerializerUtils.readString(in);
      replicatedState = (ReplicatedState) SerializerUtils.newInstance(Class.forName(repStateClassName));
      replicatedState.readWire(in);

      final int size = in.readInt();
      messageAssemblerParts = new ArrayList<Frame>(size);
      for (int i = 0; i < size; i++) {
         final Frame frame = new Frame();
         frame.readWire(in);
         messageAssemblerParts.add(frame);
      }

      //
      final boolean lastOperationalClusterViewIsSet = in.readBoolean();
      if (lastOperationalClusterViewIsSet) {

         lastOperationalClusterView = new ClusterViewImpl();
         lastOperationalClusterView.readWire(in);
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

      final MarkerListRequest that = (MarkerListRequest) o;

      if (clusterView != null ? !clusterView.equals(that.clusterView) : that.clusterView != null) {
         return false;
      }
      if (lastOperationalClusterView != null ? !lastOperationalClusterView.equals(
              that.lastOperationalClusterView) : that.lastOperationalClusterView != null) {
         return false;
      }
      if (messageAssemblerParts != null ? !messageAssemblerParts.equals(
              that.messageAssemblerParts) : that.messageAssemblerParts != null) {
         return false;
      }
      if (replicatedState != null ? !replicatedState.equals(that.replicatedState) : that.replicatedState != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (clusterView != null ? clusterView.hashCode() : 0);
      result = 31 * result + (replicatedState != null ? replicatedState.hashCode() : 0);
      result = 31 * result + (messageAssemblerParts != null ? messageAssemblerParts.hashCode() : 0);
      result = 31 * result + (lastOperationalClusterView != null ? lastOperationalClusterView.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "MarkerListRequest{" +
              "clusterView=" + clusterView +
              ", replicatedState=" + replicatedState +
              ", messageAssemblerParts=" + messageAssemblerParts +
              ", lastOperationalClusterView=" + lastOperationalClusterView +
              "} " + super.toString();
   }


   // ------------------------------------------------------------------------------------------------------------------
   //
   // Waiter
   //
   // ------------------------------------------------------------------------------------------------------------------

   /**
    * Waiter for MarkerList.
    */
   public static final class Waiter extends org.cacheonix.impl.net.processor.Waiter {

      /**
       * Marker to forward on success.
       */
      private OperationalMarker markerToForward = null;


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

         final ClusterProcessor processor = (ClusterProcessor) getRequest().getProcessor();

         // Check if this response received while we have the same
         // cluster view as we had when we stored the marker to forward
         // to avoid a situation of re-introducing old marker while our
         // configuration has moved on since we stored the marker.

         if (getRequest().getClusterUUID().equals(processor.getProcessorState().getClusterView().getClusterUUID())) {

            if (response.getResultCode() == Response.RESULT_SUCCESS) {

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled()) LOG.debug("Marker list was successfuly sent: " + getRequest()); // NOPMD

               // Call back the marker to forward to execute action
               // in case of successful sending of the marker list
               markerToForward.finishJoin();

            } else {

               // Cannot contact
               rollbackJoin();
            }

            // Forward (the first marker to the joined node, or the original to the previous node)

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("Forwarding marker: " + markerToForward); // NOPMD

            // NOTE: simeshev@cacheonix.org - It's critical to use an advanced marker-specific forward
            // that incorporates handling left. Failing to do so leads to not removing a leaving node
            // when handling a join. See CACHEONIX-307 for more information.

            markerToForward.forward();
         } else {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("Ignored response to the marker list because " + // NOPMD
                    "the current configuration has changed since the request was sent");
         }

         //
         super.notifyResponseReceived(response);
      }


      private void rollbackJoin() {


         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Rolling back join: " + getRequest()); // NOPMD

         final ClusterProcessor processor = (ClusterProcessor) getRequest().getProcessor();

         processor.getProcessorState().getClusterView().remove(markerToForward.getJoiningNode().getAddress());

         processor.getProcessorState().updateLastOperationalClusterView(processor.getProcessorState().getClusterView());
         markerToForward.rollbackJoin();
      }


      /**
       * Sets a marker to forward after finishing sending the MarkerListRequest.
       * <p/>
       * If everything is fine, this marker is forwarded to the just joined node.
       * <p/>
       * If there was a problem, the join is rolled back and the marker is forwarded in a form as if join never
       * happened.
       *
       * @param markerToForward a marker to forward.
       * @see #notifyResponseReceived(Response)
       * @see #rollbackJoin()
       */
      public void setMarkerToForward(final OperationalMarker markerToForward) {

         this.markerToForward = markerToForward;
      }
   }

   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new MarkerListRequest();
      }
   }
}
