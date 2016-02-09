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

import java.util.Collections;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A local message sent to a cluster node upon marker timeout.
 */
public final class MarkerTimeoutMessage extends ClusterMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MarkerTimeoutMessage.class); // NOPMD


   /**
    * Timeout after that this message is supposed to be kick-in by {@link MarkerTimeoutAction#run()}. This field here is
    * for information purposes only, to use it in the debug print.
    */
   private long timeoutMillis;

   /**
    * Holds the number of markers received by the cluster node at the time the timeout was created. When the processor
    * executes the message, the compares this counter with cluster processor's number of markers. It message's  behind
    * it means that there have been markers forwarded after the timeout and the timeout action is no longer valid and
    * the recovery should not be initiated.
    */
   private long markerCounterAtTimeoutCreate = 0L;


   public MarkerTimeoutMessage() {

      super(TYPE_CLUSTER_MARKER_TIMEOUT);
   }


   /**
    * Sets a number of markers received by the cluster node at the time the timeout was created. When the processor
    * executes the message, the compares this counter with cluster processor's number of markers. It message's  behind
    * it means that there have been markers forwarded after the timeout and the timeout action is no longer valid and
    * the recovery should not be initiated.
    *
    * @param markerCounterAtTimeoutCreate the number of markers received by the cluster node at the time the timeout was
    *                                     created.
    */
   public void setMarkerCounterAtTimeoutCreate(final long markerCounterAtTimeoutCreate) {

      this.markerCounterAtTimeoutCreate = markerCounterAtTimeoutCreate;
   }


   /**
    * Sets the timeout after that this message is supposed to be kick-in by {@link MarkerTimeoutAction#run()}.
    *
    * @param timeoutMillis the timeout to set.
    */
   public void setTimeoutMillis(final long timeoutMillis) {

      this.timeoutMillis = timeoutMillis;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This extension validates that it is sent only to local cluster node.
    */
   public void validate() throws InvalidMessageException {

      // Default validation
      super.validate();

      // Locality validation
      if (!getReceiver().isAddressOf(getSender())) {
         throw new InvalidMessageException("Shutdown can be sent only to the local cluster node");
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This extension enhances the default behaviour by skipping execution all together if cluster processor's counter
    * has moved ahead.
    */
   public void execute() throws InterruptedException {

      // When the processor executes the message, the compares this counter with cluster processor's number of markers.
      // It message's  behind it means that there have been markers forwarded after the timeout and the timeout action
      // is no longer valid and the recovery should not be initiated.

      if (markerCounterAtTimeoutCreate != getClusterProcessor().getProcessorState().getMarkerCounter()) {

         // There have been markers received, do nothing
         return;
      }


      // Check if shutting down
      if (getClusterProcessor().isShuttingDown()) {
         // No point to process because it is going down
         return;
      }

      // Proceed normally
      super.execute();
   }


   protected void processNormal() {

      // Begin recovery
      beginRecovery();
   }


   protected void processBlocked() {

      // Begin recovery
      beginRecovery();
   }


   protected void processRecovery() {

      // Remove our predecessor on the ring. REVIEWME: slava@cacheonix.org - 2010-01-23 - This is a temporary work
      // around the problem where a receiver of the RecoveryMarker receives the marker but actively refuses to processes
      // it, such as in the case when another cluster was formed and they no longer interested in getting this node's
      // recovery marker. Right now we don't have a way to know it, so we just remove nodes one by one on each timeout.
      // This works but may lead to a slow reconfiguration. In future we should develop a facility to receive responses
      // to the forwarded marker and recognize refusal to process.
      final ClusterProcessor processor = getClusterProcessor();

      final ClusterNodeAddress nextNode = processor.getProcessorState().getClusterView().getNextElement();
      if (!processor.getAddress().equals(nextNode)) {

         processor.getProcessorState().getClusterView().remove(nextNode);
      }

      // Begin recovery
      beginRecovery();
   }


   protected void processCleanup() {

      // Begin recovery
      beginRecovery();
   }


   private void beginRecovery() {

      LOG.debug("Timed out in '" + timeoutMillis + "' millis while waiting for a marker, initiating recovery round," +
              " originator: " + getClusterProcessor().getAddress());

      final ClusterProcessor processor = getClusterProcessor();

      // Begin recovery with the node next after failed.

      final ClusterNodeAddress nextNode = processor.getProcessorState().getClusterView().getNextElement();
      final ClusterNodeAddress self = processor.getAddress();

      if (LOG.isDebugEnabled()) {
         LOG.debug("RRRRRRRRRRRRRRRRRRRRRR Begin recovery starting with: " + nextNode);
      }

      // Change state to recovery, with us as an Originator
      if (LOG.isDebugEnabled()) {
         LOG.debug("<><><><><><><><><><><><><><> Created recovery state: " + self.getTcpPort() + ", originator: " + true);
      }

      processor.getProcessorState().setState(ClusterProcessorState.STATE_RECOVERY);

      // Cancel 'home alone' timeout
      processor.getProcessorState().getHomeAloneTimeout().cancel();

      processor.getProcessorState().setRecoveryOriginator(true);

      // Post new recovery marker with self as an originator
      final UUID newClusterUUID = UUID.randomUUID();

      final List<JoiningNode> currentList = CollectionUtils.createList(new JoiningNode(self));
      final List<JoiningNode> previousList = Collections.emptyList();
      final RecoveryMarker recoveryMarker = new RecoveryMarker(newClusterUUID, self, currentList, previousList);
      recoveryMarker.setReceiver(nextNode);

      processor.post(recoveryMarker);
   }


   public String toString() {

      return "MarkerTimeoutMessage{" +
              "timeoutMillis=" + timeoutMillis +
              ", markerCounterAtTimeoutCreate=" + markerCounterAtTimeoutCreate +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new MarkerTimeoutMessage();
      }
   }
}