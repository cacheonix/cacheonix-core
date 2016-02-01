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
package org.cacheonix.impl.lock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.SimpleWaiter;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.MutableBoolean;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A reliable multicast message to acquire a lock.
 * <p/>
 * This message supports a sending a response to a sender. The response is sent when the replicated state is processed
 * at the requester ClusterProcessor.
 */
@SuppressWarnings("RedundantIfStatement")
public final class AcquireLockRequest extends LockRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AcquireLockRequest.class); // NOPMD

   /**
    * Returned if the lock was granted.
    */
   public static final Integer RESULT_LOCK_GRANTED = Integer.valueOf(0);

   /**
    * Returned if lock wait has expired.
    */
   private static final Integer RESULT_LOCK_WAIT_EXPIRED = Integer.valueOf(1);

   /**
    * Returned if a deadlock is detected
    */
   public static final Integer RESULT_DETECTED_DEADLOCK = Integer.valueOf(2);


   /**
    * Time after that a cluster representative will begin a forced lock release in milliseconds.
    */
   private Time forcedUnlockTime = null;


   /**
    * Required by Wireable.
    */
   @SuppressWarnings("UnusedDeclaration")
   public AcquireLockRequest() {

   }


   /**
    * Create a lock request.
    *
    * @param lockRegionName   a name of the region where this lock is going to be placed. The region name is used to
    *                         separate cluster-wide and cache-specific locks.
    * @param lockKey          a lock identifier.
    * @param ownerAddress     a lock owner's address
    * @param ownerThreadID    a ID of the owner thread.
    * @param ownerThreadName  a name of the owner thread.
    * @param readLock         a read lock flag. If false, it is a write lock.
    * @param forcedUnlockTime the absolute cluster time after that the lock will be forcibly released
    */
   public AcquireLockRequest(final String lockRegionName, final Binary lockKey, final ClusterNodeAddress ownerAddress,
                             final int ownerThreadID, final String ownerThreadName, final boolean readLock,
                             final Time forcedUnlockTime) {
      // Call super
      super(TYPE_ACQUIRE_LOCK_REQUEST, lockRegionName, lockKey, ownerAddress, ownerThreadID, ownerThreadName, readLock);

      // Set unlock timeout
      this.forcedUnlockTime = forcedUnlockTime;
   }


   /**
    * Returns the time duration after that a cluster representative will begin a forced lock release.
    *
    * @return the time duration after that a cluster representative will begin a forced lock release.
    */
   public Time getForcedUnlockTime() {

      return forcedUnlockTime;
   }


   public void validate() throws InvalidMessageException {

      super.validate();

      // Check if this is an mcast message
      if (isReceiverSet()) {

         throw new InvalidMessageException("This is a reliable mcast message, so receivers should always be empty");
      }
   }


   /**
    * {@inheritDoc}
    */
   public void execute() {

      final ClusterProcessor processor = getClusterProcessor();

      final LockRegistry lockRegistry = processor.getProcessorState().getReplicatedState().getLockRegistry();
      final LockQueue lockQueue = lockRegistry.getLockQueue(getLockRegionName(), getLockKey());


      if (isReadLock()) {

         // Read lock request

         processesReadLock(lockQueue);

      } else {

         // Write lock request

         processWriteLock(lockQueue);
      }
   }


   private void processWriteLock(final LockQueue lockQueue) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Processing write lock request: " + this); // NOPMD

      final LockOwner writeLockOwner = lockQueue.getWriteLockOwner();
      if (writeLockOwner == null) {

         // There is no write lock

         if (lockQueue.areReadLocksGranted()) {

            // Check if it is only our read lock. If so, upgrade
            if (lockQueue.isOnlyReadLockCameFrom(this)) {

               // New write lock, no read or write locks present,
               // upgrade to write (1 or more read locks from the same owner + 1 write lock)
               grant();
            } else {

               tryToEnqueue();
            }
         } else {

            // New write lock, no read or write locks present
            grant();
         }
      } else {

         // Write lock is already granted

         if (writeLockOwner.cameFromRequester(this)) {

            // Reentrant lock
            grant();
         } else {

            tryToEnqueue();
         }
      }
   }


   private void processesReadLock(final LockQueue lockQueue) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Processing read lock request: " + this); // NOPMD

      final LockOwner writeLockOwner = lockQueue.getWriteLockOwner();
      if (writeLockOwner == null) {

         // Write lock is not granted

         // Grant read
         grant();
      } else {

         // Write lock is already granted

         // Is granted to us?
         if (writeLockOwner.cameFromRequester(this)) {

            // Lock is granted to requester, piggyback on write lock
            grant();
         } else {

            tryToEnqueue();
         }
      }
   }


   /**
    * Grants a lock.
    */
   private void grant() {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Granting lock: " + this); // NOPMD

      final ClusterProcessor processor = getClusterProcessor();

      final LockRegistry lockRegistry = processor.getProcessorState().getReplicatedState().getLockRegistry();
      final LockQueue lockQueue = lockRegistry.getLockQueue(getLockRegionName(), getLockKey());

      lockQueue.grantLockRequest(this);

      // Respond
      respondLockGranted();
   }


   /**
    * Tries to enqueue this request. This method may immediately respond with <code>RESULT_LOCK_WAIT_EXPIRED</code> or
    * <code>RESULT_DETECTED_DEADLOCK</code>.
    *
    * @see #RESULT_DETECTED_DEADLOCK
    * @see #RESULT_LOCK_WAIT_EXPIRED
    */
   private void tryToEnqueue() {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Trying to enqueue: " + this); // NOPMD

      final ClusterProcessor processor = getClusterProcessor();

      final LockRegistry lockRegistry = processor.getProcessorState().getReplicatedState().getLockRegistry();
      final LockQueue lockQueue = lockRegistry.getLockQueue(getLockRegionName(), getLockKey());

      if (hasTimeout() && getTimeoutMillis() == 0) {

         respondLockWaitExpired();
      }

      if (isDeadlock()) {

         respondDetectedDeadlock();
      } else {

         // Enqueue

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Enqueueing: " + this); // NOPMD

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Lock queue: " + lockQueue); // NOPMD

         lockQueue.getPendingRequests().add(this);
      }
   }


   /**
    * Detects if enqueueing this request will cause a deadlock.
    * <p/>
    * The algorithm is the following:
    * <p/>
    * 1. Get the current owner of the desired lock.
    * <p/>
    * 2. For each lock we own, find if there is the owner of the desired lock in the pending request queue.
    * <p/>
    * 3. If there is, the deadlock is detected.
    *
    * @return returns <code>true</code> if the deadlock is detected.
    */
   private boolean isDeadlock() {

      final LockRegistry lockRegistry = getClusterProcessor().getProcessorState().getReplicatedState().getLockRegistry();

      final MutableBoolean result = new MutableBoolean(false);

      // For all lock queues
      lockRegistry.getLockQueues().forEachValue(new ObjectProcedure<LockQueue>() {

         public boolean execute(final LockQueue lockQueue) {

            // Check write lock owner
            final LockOwner writeLockOwner = lockQueue.getWriteLockOwner();
            if (waitsForOurLocks(writeLockOwner)) {

               // Deadlock
               result.set(true);

               // Stop iterating
               return false;
            }

            // Check read lock owners
            final List<LockOwner> readLockOwners = lockQueue.getReadLockOwners();
            for (final LockOwner readLockOwner : readLockOwners) {

               if (waitsForOurLocks(readLockOwner)) {

                  // Deadlock
                  result.set(true);

                  // Stop iterating
                  return false;

               }
            }

            // Next lock queue
            return true;
         }
      });

      return result.get();
   }


   private boolean waitsForOurLocks(final LockOwner lockOwner) {

      if (lockOwner == null) {
         return false;
      }


      final LockRequest thisRequest = this;
      final MutableBoolean result = new MutableBoolean(false);

      final LockRegistry lockRegistry = getClusterProcessor().getProcessorState().getReplicatedState().getLockRegistry();
      lockRegistry.getLockQueues().forEachValue(new ObjectProcedure<LockQueue>() {

         public boolean execute(final LockQueue lockQueue) {


            // Check write lock owner
            final LockOwner writeLockOwner = lockQueue.getWriteLockOwner();

            if (writeLockOwner != null && writeLockOwner.cameFromRequester(thisRequest)) {

               // We own write lock in this queue
               final LinkedList<AcquireLockRequest> pendingRequests = lockQueue.getPendingRequests();
               for (final AcquireLockRequest pendingRequest : pendingRequests) {

                  if (lockOwner.cameFromRequester(pendingRequest)) {

                     //
                     result.set(true);

                     // Stop iterating
                     return false;
                  }
               }
            }

            final List<LockOwner> readLockOwners = lockQueue.getReadLockOwners();
            for (final LockOwner readLockOwner : readLockOwners) {

               if (readLockOwner.cameFromRequester(thisRequest)) {

                  final LinkedList<AcquireLockRequest> pendingRequests = lockQueue.getPendingRequests();
                  for (final AcquireLockRequest pendingRequest : pendingRequests) {

                     if (lockOwner.cameFromRequester(pendingRequest)) {

                        //
                        result.set(true);

                        // Stop iterating
                        return false;

                     }
                  }
               }
            }

            // Continue iterating
            return true;
         }
      });


      return result.get();
   }


   /**
    * Posts a response that the lock request is granted.
    */
   private void respondLockGranted() {

      final ClusterProcessor processor = getClusterProcessor();
      if (processor.getAddress().equals(getOwnerAddress())) {
         processor.post(createLockGrantedResponse());
      }
   }


   /**
    * Posts a response that a deadlock is detected.
    */
   private void respondDetectedDeadlock() {

      final ClusterProcessor processor = getClusterProcessor();
      if (processor.getAddress().equals(getOwnerAddress())) {
         final Response response = createResponse(Response.RESULT_SUCCESS);
         response.setResult(RESULT_DETECTED_DEADLOCK);
         processor.post(response);
      }
   }


   /**
    * Creates a response that the lock request is granted.
    *
    * @return the response that the lock request is granted.
    */
   Response createLockGrantedResponse() {

      final Response response = createResponse(Response.RESULT_SUCCESS);
      response.setResult(RESULT_LOCK_GRANTED);
      return response;
   }


   /**
    * Posts a response that wait for a lock has expired.
    */
   private void respondLockWaitExpired() {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Responding lock wait expired: " + this); // NOPMD

      final ClusterProcessor processor = getClusterProcessor();
      if (processor.getAddress().equals(getOwnerAddress())) {

         processor.post(createLockWaitExpiredResponse());
      }
   }


   Response createLockWaitExpiredResponse() {

      final Response response = createResponse(Response.RESULT_SUCCESS);
      response.setResult(RESULT_LOCK_WAIT_EXPIRED);
      return response;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      SerializerUtils.writeTime(forcedUnlockTime, out);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      forcedUnlockTime = SerializerUtils.readTime(in);
   }


   public Waiter createWaiter() {

      return new Waiter(this);
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

      final AcquireLockRequest that = (AcquireLockRequest) o;

      if (forcedUnlockTime != null ? !forcedUnlockTime.equals(that.forcedUnlockTime) : that.forcedUnlockTime != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (forcedUnlockTime != null ? forcedUnlockTime.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "AcquireLockRequest{" +
              "unlockTimeout=" + forcedUnlockTime +
              "} " + super.toString();
   }


   public static class Waiter extends SimpleWaiter {

      Waiter(final Request request) {

         super(request);
      }


      public synchronized void notifyTimeout() {

         // Timeout means that the lock has not been satisfied within
         // a requested period of time. Post the announcement
         // to release/remove the lock.
         final AcquireLockRequest request = (AcquireLockRequest) getRequest();
         final WaitForLockExpiredAnnouncement announcement = new WaitForLockExpiredAnnouncement(request.getLockRegionName(),
                 request.getLockKey(), request.getOwnerAddress(), request.getOwnerThreadID(), request.getOwnerThreadName(),
                 request.isReadLock());
         announcement.setSender(getRequest().getProcessor().getAddress());
         announcement.setResponseRequired(false);
         getRequest().getProcessor().post(announcement);
      }
   }

   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new AcquireLockRequest();
      }
   }
}
