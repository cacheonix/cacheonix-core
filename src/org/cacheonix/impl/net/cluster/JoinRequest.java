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

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * JoinRequest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 1, 2008 7:36:28 PM
 */
public final class JoinRequest extends ClusterRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(JoinRequest.class); // NOPMD


   /**
    * Required by Externalizable.
    *
    * @noinspection WeakerAccess
    */
   public JoinRequest() {

   }


   /**
    * Creates JoinMessage.
    *
    * @param joinToMember a process that we would like to join.
    */
   public JoinRequest(final ClusterNodeAddress joinToMember) {

      super(TYPE_CLUSTER_JOIN_REQUEST);
      super.setReceiver(joinToMember);

      this.setRequiresSameCluster(false);
   }


   /**
    * {@inheritDoc}
    */
   protected void processNormal() {

      final ClusterProcessor clusterProcessor = getClusterProcessor();

      final JoinStatus joinStatus = clusterProcessor.getProcessorState().getJoinStatus();

      // We cannot serve join if we are ourselves in the process of joining
      if (joinStatus.isJoining()) {

         final String errorDescription = "Ignoring join request from " + getSender()
                 + " because we are joining node " + joinStatus.getJoiningToProcess();

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorDescription); // NOPMD

         final Response errorResponse = createResponse(ClusterResponse.RESULT_ERROR);
         errorResponse.setResult(errorDescription);
         clusterProcessor.post(errorResponse);

         return;
      }

      // We cannot serve join if we are in process of shutting down. Generally it is not possible
      // to guarantee that a node serving joining won't start leaving, but, at least, it is possible
      // to avoid situation when a node know to be leaving responds to the join request with 'SUCCESS'.
      if (clusterProcessor.isShuttingDown()) {

         final String errorDescription = "Ignoring join request from " + getSender()
                 + " because we are shutting down";

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug(errorDescription); // NOPMD

         final Response errorResponse = createResponse(ClusterResponse.RESULT_ERROR);
         errorResponse.setResult(errorDescription);
         clusterProcessor.post(errorResponse);

         return;
      }

      final JoiningNode joiningNode = new JoiningNode(getSender());

      // Reset 'home alone' timeout because it looks like we are not alone
      clusterProcessor.getProcessorState().getHomeAloneTimeout().reset();

      // Add to join request list. The requests from this list
      // will be serviced when next blocked marker is received.
      boolean alreadyJoining = false;
      // NOPMD
      for (final JoiningNode registeredJoinRequest : clusterProcessor.getProcessorState().getJoinRequests()) {

         if (registeredJoinRequest.getAddress().equals(getSender())) {

            alreadyJoining = true;

            break;
         }
      }

      if (alreadyJoining) {

         LOG.warn("Ignored join request from node '" + getSender() + "' because it is already registered as joining");
      } else {

         // Register join request
         // NOPMD
         clusterProcessor.getProcessorState().getJoinRequests().add(joiningNode);

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Added join request to the list: " + this); // NOPMD
      }

      // Respond with success
      clusterProcessor.post(createResponse(ClusterResponse.RESULT_SUCCESS));
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This method behaves exactly the same as in the normal mode. See {@link #processNormal()}.
    */
   protected void processBlocked() {

      processNormal();
   }


   /**
    * {@inheritDoc}
    * <p/>
    * JoinMessage simply posts an error response because it cannot be processed while the node is in recovery state.
    */
   protected void processRecovery() {

      final String errorResult = "Recovery mode does not support join requests: " + getProcessor().getAddress();

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

      final Response response = createResponse(ClusterResponse.RESULT_ERROR);
      response.setResult(errorResult);
      getProcessor().post(response);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * JoinMessage simply posts an error response because it cannot be processed while the node is in recovery state.
    */
   protected void processCleanup() {

      final String errorResult = "Cleanup mode does not support join requests: " + getProcessor().getAddress();

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

      final Response response = createResponse(ClusterResponse.RESULT_ERROR);
      response.setResult(errorResult);
      getProcessor().post(response);
   }


   /**
    * {@inheritDoc}
    */
   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   public String toString() {

      return "JoinMessage{" +
              "} " + super.toString();
   }


   /**
    * Waiter for JoinMessage.
    */
   public static final class Waiter extends org.cacheonix.impl.net.processor.Waiter {

      /**
       * Creates waiter.
       *
       * @param request request UUID
       */
      public Waiter(final Request request) {

         super(request);
      }


      public void notifyResponseReceived(final Response response) throws InterruptedException {

         if (response instanceof ClusterResponse && response.getResultCode() == ClusterResponse.RESULT_SUCCESS) {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("Our join request was accepted by " + response.getSender()); // NOPMD

            // Re-start homeAlone timer because join process has progressed
            final ClusterProcessor processor = (ClusterProcessor) getRequest().getProcessor();

            processor.getProcessorState().getHomeAloneTimeout().reset();

         } else {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("Request to join failed: " + response);  // NOPMD

            //
            setResult(response.getResult());

            //
            cancelJoin(response.getSender());
         }
         super.notifyResponseReceived(response);
      }


      /**
       * Clears join status so that next cluster announcement can initiate the join.
       *
       * @param joiningToNode the node this node tried to join.
       */
      private void cancelJoin(final ClusterNodeAddress joiningToNode) {

         final ClusterProcessor processor = (ClusterProcessor) getRequest().getProcessor();

         final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();

         // Make sure we are waiting for this same node we started with.
         if (joiningToNode.equals(joinStatus.getJoiningToProcess())) {
            joinStatus.clear();
         }
      }
   }

   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new JoinRequest();
      }
   }
}