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

import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Contains functions common for messages used to maintain the cluster.
 */
public abstract class ClusterMessage extends Message {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterMessage.class); // NOPMD


   protected ClusterMessage(final int wireableType) {

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

      switch (getClusterProcessor().getProcessorState().getState()) {
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

            LOG.error("Unknown state: " + getClusterProcessor().getProcessorState().getState());
            break;
      }
   }


   /**
    * Processes this message while it is at the cluster service that is in a Normal (operational) state.
    */
   protected abstract void processNormal();


   /**
    * Processes this message while it is at the cluster service that is in a Blocked state.
    */
   protected abstract void processBlocked();


   /**
    * Processes this message while it is at the cluster service that is in a Recovery state.
    */
   protected abstract void processRecovery();


   /**
    * Processes this message while it is at the cluster service that is in a Cleanup state.
    */
   protected abstract void processCleanup();


   /**
    * Returns a context cluster service.
    *
    * @return the context cluster service.
    */
   protected final ClusterProcessor getClusterProcessor() {

      return (ClusterProcessor) getProcessor();
   }


   public String toString() {

      return "ClusterMessage{" +
              "} " + super.toString();
   }
}
