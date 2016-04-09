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
package org.cacheonix.impl.lock;

import java.util.Iterator;
import java.util.LinkedList;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * A reliable mcast message to cancel a wait for a lock. This announcement may arrive after the lock was granted. If so,
 * it is ignored.
 */
public final class WaitForLockExpiredAnnouncement extends LockRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();


   public WaitForLockExpiredAnnouncement() {

   }


   public WaitForLockExpiredAnnouncement(final String lockRegionName, final Binary lockKey,
                                         final ClusterNodeAddress ownerAddress, final int threadID,
                                         final String threadName, final boolean readLock) {

      super(TYPE_WAIT_FOR_LOCK_EXPIRED_ANNOUNCEMENT, lockRegionName, lockKey, ownerAddress, threadID, threadName, readLock);
   }


   public void execute() {

      // Find lock being cancelled

      final ClusterProcessor processor = getClusterProcessor();

      final ReplicatedState state = processor.getProcessorState().getReplicatedState();
      final LockRegistry lockRegistry = state.getLockRegistry();
      final LockQueue lockQueue = lockRegistry.getLockQueue(getLockRegionName(), getLockKey());

      final LinkedList<AcquireLockRequest> lockRequests = lockQueue.getPendingRequests();
      for (final Iterator<AcquireLockRequest> iterator = lockRequests.iterator(); iterator.hasNext(); ) {

         final AcquireLockRequest request = iterator.next();
         if (request.isReadLock() == isReadLock() && request.getOwnerThreadID() == getOwnerThreadID()
                 && request.getOwnerAddress().equals(getOwnerAddress())) {

            // Found - remove from waiting
            iterator.remove();

            // Respond
            if (processor.getAddress().equals(request.getOwnerAddress())) {
               processor.post(request.createLockWaitExpiredResponse());
            }
         }
      }
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new WaitForLockExpiredAnnouncement();
      }
   }
}
