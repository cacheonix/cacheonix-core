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
package org.cacheonix.impl.lock;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.processor.RequestProcessor;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * A request to release a lock.
 */
@SuppressWarnings("RedundantIfStatement")
public final class ReleaseLockRequest extends LockRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   public static final Integer RESULT_RELEASED = Integer.valueOf(0);

   public static final Integer RESULT_LOCK_BROKEN = Integer.valueOf(1);


   /**
    * Required by Wireable.
    */
   @SuppressWarnings("UnusedDeclaration")
   public ReleaseLockRequest() {

   }


   /**
    * Creates ReleaseLockRequest.
    *
    * @param lockRegionName  a name of the region where this lock is going to be placed. The region name is used to
    *                        separate cluster-wide and cache-specific locks.
    * @param lockKey         a lock identifier.
    * @param ownerAddress    a lock owner's address
    * @param ownerThreadID   owner's thread ID.
    * @param ownerThreadName owner's thread name.
    * @param readLock        a read lock flag. If false, it is a write lock.
    */
   public ReleaseLockRequest(final String lockRegionName, final Binary lockKey, final ClusterNodeAddress ownerAddress,
                             final int ownerThreadID,
                             final String ownerThreadName, final boolean readLock) {

      super(TYPE_RELEASE_LOCK_REQUEST, lockRegionName, lockKey, ownerAddress, ownerThreadID, ownerThreadName, readLock);
   }


   /**
    * {@inheritDoc}
    */
   public void execute() {

      final ClusterProcessor processor = getClusterProcessor();

      final ReplicatedState state = processor.getProcessorState().getReplicatedState();
      final LockRegistry lockRegistry = state.getLockRegistry();
      final LockQueue lockQueue = lockRegistry.getLockQueue(getLockRegionName(), getLockKey());

      // Release lock
      final boolean released = lockQueue.releaseLock(this);
      if (released) {

         // Successfully unlocked
         respondUnlocked();
      } else {

         // Could not find the lock. This is possible if:
         //
         // a) Number of calls to unlock() is greater than number of locks().
         //
         // b) This was a lock cancel request caused by the timeout that
         //    came after the lock was explicitly released.
         //

         respondLockBroken();

         // Done
         return;
      }

      // Lock was released.  Try to grant next lock(s) request in queue
      final NextLockRequestGranter nextLockRequestGranter = new NextLockRequestGranter(processor, lockQueue);
      nextLockRequestGranter.grantNextLockRequests();
   }


   private void respondLockBroken() {

      // This is possible if this mcast message was
      // sent from the MulticastMarker as a result of a timeout.
      if (!isResponseRequired()) {
         return;
      }


      // Respond if requester is a local node.
      final RequestProcessor processor = getProcessor();
      if (processor.getAddress().equals(getOwnerAddress())) {
         final Response errorResponse = createResponse(Response.RESULT_SUCCESS);
         errorResponse.setResult(RESULT_LOCK_BROKEN);
         processor.post(errorResponse);
      }
   }


   private void respondUnlocked() {

      // This is possible if this mcast message was
      // sent from the MulticastMarker as a result of a timeout.
      if (!isResponseRequired()) {
         return;
      }


      // Respond if requester is a local node.
      final RequestProcessor processor = getProcessor();
      if (processor.getAddress().equals(getOwnerAddress())) {
         final Response response = createResponse(Response.RESULT_SUCCESS);
         response.setResult(RESULT_RELEASED);
         processor.post(response);
      }
   }


   public String toString() {

      return "ReleaseLockRequest{" +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ReleaseLockRequest();
      }
   }
}
