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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.cluster.ClusterMember;
import org.cacheonix.impl.cluster.ClusterEventUtil;
import org.cacheonix.impl.cluster.ClusterMemberLeftEventImpl;
import org.cacheonix.impl.lock.AcquireLockRequest;
import org.cacheonix.impl.lock.LockOwner;
import org.cacheonix.impl.lock.LockQueue;
import org.cacheonix.impl.lock.LockQueueKey;
import org.cacheonix.impl.lock.LockRegistry;
import org.cacheonix.impl.lock.NextLockRequestGranter;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A message that every cluster processor sends to self when a cluster node leaves.
 * <p/>
 * This message is used to support total order where the reliable mcast messages are ordered with changes in the cluster
 * configuration.
 *
 * @see CleanupMarker#processCleanup()
 * @see MarkerRequest#processCleanup()
 * @see MarkerRequest#sendLeftToSelf(long, ClusterNodeAddress)
 */
public final class ClusterNodeLeftAnnouncement extends Message {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MarkerRequest.class); // NOPMD


   private ClusterNodeAddress leave = null;


   /**
    * Required by Wireable
    */
   public ClusterNodeLeftAnnouncement() {

      super(TYPE_NODE_LEFT_MESSAGE);
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return ReplicatedStateProcessorKey.getInstance();
   }


   public void setLeave(final ClusterNodeAddress leave) {

      this.leave = leave;
   }


   ClusterNodeAddress getLeave() {

      return leave;
   }


   public void execute() {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Executing cluster node left message: " + leave); // NOPMD

      final List<ClusterNodeAddress> nodesLeft = Collections.singletonList(leave);
      final ClusterProcessor processor = (ClusterProcessor) getProcessor();

      processor.getProcessorState().getReplicatedState().notifyClusterNodesLeft(nodesLeft);
      processor.getMulticastMessageListeners().notifyNodesLeft(nodesLeft);

      // Notify event subscribers
      notifyClusterEventSubscribersMemberLeft();

      // Process the effect of left nodes on the replicated lock queue.
      removeFromLockQueue(nodesLeft);
   }


   /**
    * Notifies cluster event subscribers that a member left.
    */
   private void notifyClusterEventSubscribersMemberLeft() {

      final ClusterProcessor processor = (ClusterProcessor) getProcessor();
      final ClusterProcessorState processorState = processor.getProcessorState();
      final List<ClusterEventSubscriber> clusterEventSubscribers = processorState.getClusterEventSubscribers();

      for (final ClusterEventSubscriber clusterEventSubscriber : clusterEventSubscribers) {

         final List<ClusterMember> leftMembers = new ArrayList<ClusterMember>(1);
         leftMembers.add(ClusterEventUtil.createClusterMember(processorState.getClusterName(), leave));
         try {

            clusterEventSubscriber.notifyClusterMemberLeft(new ClusterMemberLeftEventImpl(leftMembers));
         } catch (final Throwable e) { // NOPMD A catch statement should never catch throwable since it includes errors.

            // Catch user all errors
            LOG.warn("Error while notifying a subscriber that a cluster member left: " + e, e);
         }
      }
   }


   /**
    * Removes left nodes from the replicated lock queue. Grants pending lock requests if the left nodes held locks.
    *
    * @param nodesLeft a collection of addresses of nodes that left the cluster.
    */
   protected void removeFromLockQueue(final Collection<ClusterNodeAddress> nodesLeft) {

      final ClusterProcessor processor = (ClusterProcessor) getProcessor();

      final LockRegistry lockRegistry = processor.getProcessorState().getReplicatedState().getLockRegistry();
      for (final Entry<LockQueueKey, LockQueue> entry : lockRegistry.getLockQueues().entrySet()) {

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


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      leave = SerializerUtils.readAddress(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      SerializerUtils.writeAddress(leave, out);
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

      final ClusterNodeLeftAnnouncement that = (ClusterNodeLeftAnnouncement) o;

      if (leave != null ? !leave.equals(that.leave) : that.leave != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (leave != null ? leave.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "ClusterNodeLeftAnnouncement{" +
              "leave=" + leave +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClusterNodeLeftAnnouncement();
      }
   }
}
