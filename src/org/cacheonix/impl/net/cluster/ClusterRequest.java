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

import java.io.IOException;

import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Contains functions common for messages used to maintain the cluster.
 */
public abstract class ClusterRequest extends Request {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterRequest.class); // NOPMD


   /**
    * Required by Wireable.
    */
   public ClusterRequest() {

   }


   public ClusterRequest(final int wireableType) {

      super(wireableType);
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return ClusterProcessorKey.getInstance();
   }


   /**
    * {@inheritDoc}
    */
   public void execute() throws InterruptedException {

      // DELETEME: simeshev@cacheonix.org - when debugging done
//      if (logReceive && LOG.isDebugEnabled()) {
//         LOG.debug("Received request: " + this);
//      }

      final ClusterProcessor processor = getClusterProcessor();
      try {

         switch (processor.getProcessorState().getState()) {

            case ClusterProcessorState.STATE_NORMAL:

               processNormal();
               break;
            case ClusterProcessorState.STATE_BLOCKED:

               processBlocked();
               break;
            case ClusterProcessorState.STATE_RECOVERY:

               processRecovery();
               break;
            case ClusterProcessorState.STATE_CLEANUP:

               processCleanup();
               break;
            default:

               final String errorResult = "Unknown state: " + processor.getProcessorState().getState();

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled()) LOG.debug(errorResult); // NOPMD

               final Response errorResponse = createResponse(ClusterResponse.RESULT_ERROR);
               errorResponse.setResult(errorResult);
               getProcessor().post(errorResponse);

               break;
         }
      } catch (final IOException e) {

         // Log
         LOG.error("Error while executing cluster request: " + e.toString(), e);

         // Respond with error
         final Response errorResponse = createResponse(ClusterResponse.RESULT_ERROR);
         errorResponse.setResult(e);
         getProcessor().post(errorResponse);
      }
   }


   /**
    * Processes this message while it is at the cluster service that is in a Normal (operational) state.
    *
    * @throws IOException          if I/O error occurred.
    * @throws InterruptedException if the thread was interrupted.
    */
   protected abstract void processNormal() throws IOException, InterruptedException;


   /**
    * Processes this message while it is at the cluster service that is in a Blocked state.
    *
    * @throws IOException          if I/O error occurred.
    * @throws InterruptedException if the processor thread was shutdown.
    */
   protected abstract void processBlocked() throws IOException, InterruptedException;


   /**
    * Processes this message while it is at the cluster service that is in a Recovery state.
    *
    */
   protected abstract void processRecovery();


   /**
    * Processes this message while it is at the cluster service that is in a Cleanup state.
    *
    * @throws IOException          if I/O error occurred.
    */
   protected abstract void processCleanup() throws IOException;


   /**
    * Returns a context cluster service.
    *
    * @return the context cluster service.
    */
   public final ClusterProcessor getClusterProcessor() {

      return (ClusterProcessor) getProcessor();
   }


   /**
    * Creates a response with receiver set to the sender of this message and a populated responseToUUID.
    *
    * @param resultCode the result code.
    * @return a response with receiver set to the sender of this message and a populated responseToUUID.
    */
   public final Response createResponse(final int resultCode) {

      final ClusterResponse response = new ClusterResponse();
      response.setResponseToClass(getClass());
      response.setResponseToUUID(getUuid());
      response.setResultCode(resultCode);
      response.setReceiver(getSender());
      return response;
   }


   public String toString() {

      return "ClusterRequest{" +
              "} " + super.toString();
   }
}