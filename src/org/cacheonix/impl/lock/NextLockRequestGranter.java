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

import java.util.LinkedList;

import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A functor that grants next pending lock requests.
 */
@SuppressWarnings("RedundantIfStatement")
public final class NextLockRequestGranter {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(NextLockRequestGranter.class); // NOPMD

   private final ClusterProcessor clusterProcessor;

   private final LockQueue lockQueue;


   public NextLockRequestGranter(final ClusterProcessor clusterProcessor, final LockQueue lockQueue) {

      this.clusterProcessor = clusterProcessor;
      this.lockQueue = lockQueue;
   }


   /**
    * Grants next lock request(s) while possible. The granter is capable of detecting if there is work to do.
    */
   public void grantNextLockRequests() {

      // Grant next lock request(s) while possible.
      do {
      } while (grantNextLockRequest());
   }


   /**
    * Tries to grant a next lock request.
    *
    * @return <code>true</code> if granted the next lock request. <code>false</code> if couldn't
    * @see #grantNextLockRequests()
    */
   private boolean grantNextLockRequest() {

      // Check if there are any requests
      final LinkedList<AcquireLockRequest> pendingRequests = lockQueue.getPendingRequests();
      if (pendingRequests.isEmpty()) {
         return false;
      }

      // Try to grant
      final AcquireLockRequest acquireLockRequest = pendingRequests.peek();

      if (acquireLockRequest.isReadLock()) {

         // Try to grant a read lock

         final LockOwner writeLockOwner = lockQueue.getWriteLockOwner();
         if (writeLockOwner == null) {

            // Not locked with a write lock, just grant a read lock
            grant(acquireLockRequest);
         } else {

            // Right now locked by write lock
            if (writeLockOwner.cameFromRequester(acquireLockRequest)) {

               grant(acquireLockRequest);
            } else {

               // Current write lock is being hold by the same host and
               // thread. BTW, this should not be possible anyway. Not
               // sure if this if will ever be true.
               return false;
            }
         }

      } else {

         // Try to grant a write lock

         final LockOwner writeLockOwner = lockQueue.getWriteLockOwner();
         if (writeLockOwner == null) {

            // Check if there are read locks
            if (lockQueue.areReadLocksGranted()) {

               // Try to escalate read lock
               if (lockQueue.isOnlyReadLockCameFrom(acquireLockRequest)) {

                  // There is our only read lock, grant write request as well
                  grant(acquireLockRequest);
               } else {

                  // Other owner's lock is already granted
                  return false;
               }

            } else {

               // No read or write locks granted
               grant(acquireLockRequest);
            }

         } else {

            // There is already a write lock granted
            if (writeLockOwner.cameFromRequester(acquireLockRequest)) {

               // Re-entrant write request
               grant(acquireLockRequest);
            } else {

               // Write lock is granted to some other owner
               return false;
            }
         }
      }

      return true;
   }


   private void grant(final AcquireLockRequest request) {

      // Remove from pending
      final AcquireLockRequest removed = lockQueue.getPendingRequests().removeFirst();

      //noinspection ObjectEquality
      Assert.assertTrue(request == removed, "Removed and granted requests should be the same {0}, {1}", removed, request); // NOPMD

      // Grant
      lockQueue.grantLockRequest(request);

      // Respond
      respondLockGranted(request);
   }


   private void respondLockGranted(final AcquireLockRequest request) {

      if (clusterProcessor.getAddress().equals(request.getOwnerAddress())) {
         clusterProcessor.post(request.createLockGrantedResponse());
      }
   }


   public String toString() {

      return "NextLockRequestGranter{" +
              "clusterProcessor=" + clusterProcessor.getAddress() +
              ", lockQueue=" + lockQueue +
              '}';
   }
}
