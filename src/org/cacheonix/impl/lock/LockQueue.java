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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;

/**
 * A queue containing pending lock requests.
 */
@SuppressWarnings("RedundantIfStatement")
public final class LockQueue implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private final LinkedList<AcquireLockRequest> pendingRequests = new LinkedList<AcquireLockRequest>(); // NOPMD

   /**
    * A write lock owner.
    */
   private LockOwner writeLockOwner = null;

   /**
    * A List of read lock owners.
    */
   private final List<LockOwner> readLockOwners = new LinkedList<LockOwner>();


   /**
    * A registry of forced release announcements. Contains both read and write lock owners.
    * <p/>
    * Note that it transient and makes sense only on the cluster representative.
    */
   private final transient LinkedList<LockOwner> forcedReleasesRegistry = new LinkedList<LockOwner>(); // NOPMD


   public LinkedList<AcquireLockRequest> getPendingRequests() { // NOPMD
      return pendingRequests;
   }


   public LockOwner getWriteLockOwner() {

      return writeLockOwner;
   }


   public List<LockOwner> getReadLockOwners() {

      return readLockOwners;
   }


   public boolean areReadLocksGranted() {

      return !readLockOwners.isEmpty();
   }


   @SuppressWarnings("SimplifiableIfStatement")
   public boolean isOnlyReadLockCameFrom(final AcquireLockRequest acquireLockRequest) {

      if (readLockOwners.isEmpty() || readLockOwners.size() > 1) {
         return false;
      }

      return readLockOwners.get(0).cameFromRequester(acquireLockRequest);
   }


   void grantLockRequest(final AcquireLockRequest request) {

      if (request.isReadLock()) {

         // This is a read lock

         // Validate
         Assert.assertTrue(writeLockOwner == null || writeLockOwner.cameFromRequester(request), "Cannot grant read lock while write lock is held by some other owner: {0}", writeLockOwner);

         // Find existing
         LockOwner readLockOwner = findReadLockOwner(request);

         // Register in granted read locks
         if (readLockOwner == null) {
            readLockOwner = new LockOwner(request.getOwnerThreadID(), request.getOwnerAddress(),
                    request.getOwnerThreadName(), request.getForcedUnlockTime(), true);
            readLockOwners.add(readLockOwner);
         }

         // Increment entries
         readLockOwner.incrementEntryCount();
      } else {

         // Write lock

         if (writeLockOwner == null) {

            // Upgrade?
            Assert.assertTrue(!areReadLocksGranted() || isOnlyReadLockCameFrom(request), "Cannot grant write lock while other read locks are present: {0}", request);

            // New write lock, no read or write locks present or only single our own read is present
            writeLockOwner = new LockOwner(request.getOwnerThreadID(), request.getOwnerAddress(),
                    request.getOwnerThreadName(), request.getForcedUnlockTime(), false);
            writeLockOwner.incrementEntryCount();

         } else {

            // Re-entrant write lock request
            Assert.assertTrue(writeLockOwner.cameFromRequester(request), "Write lock is already granted: {0}", writeLockOwner);
            writeLockOwner.incrementEntryCount();
         }
      }
   }


   private LockOwner findReadLockOwner(final LockRequest request) {

      LockOwner readLockOwner = null;
      for (final LockOwner existingReadLockOwner : readLockOwners) {
         if (existingReadLockOwner.cameFromRequester(request)) {
            readLockOwner = existingReadLockOwner;
            break;
         }
      }
      return readLockOwner;
   }


   public boolean releaseLock(final ReleaseLockRequest request) {

      // Clear from forced releases, if any
      for (final Iterator<LockOwner> iterator = forcedReleasesRegistry.iterator(); iterator.hasNext(); ) {
         final LockOwner lockOwner = iterator.next();
         if (lockOwner.cameFromRequester(request) && lockOwner.isReadLock() == request.isReadLock()) {
            iterator.remove();
            break;
         }
      }


      if (request.isReadLock()) {

         // Release read lock

         // Find and remove read lock
         LockOwner readLockOwner = null;
         for (final Iterator<LockOwner> iterator = readLockOwners.iterator(); iterator.hasNext(); ) {

            final LockOwner existingReadLockOwner = iterator.next();
            if (existingReadLockOwner.cameFromRequester(request)) {

               readLockOwner = existingReadLockOwner;
               readLockOwner.decrementEntryCount();
               if (readLockOwner.getEntryCount() == 0) {
                  iterator.remove();
               }
               break;
            }
         }

         return readLockOwner != null;


      } else {

         // Release write lock
         if (writeLockOwner == null) {

            // No write lock is held, lock is broken
            return false;
         } else {

            if (writeLockOwner.cameFromRequester(request)) {

               // Unlock

               writeLockOwner.decrementEntryCount();

               if (writeLockOwner.getEntryCount() == 0) {
                  clearWriteLockOwner();
               }
               return true;
            } else {

               // Broken lock
               return false;
            }
         }
      }
   }


   /**
    * Clears write lock owner.
    *
    * @see #releaseLock(ReleaseLockRequest)
    */
   public void clearWriteLockOwner() {

      writeLockOwner = null;
   }


   public int getLockEntryCount(final LockRequest lockRequest) {

      if (lockRequest.isReadLock()) {
         final LockOwner owner = findReadLockOwner(lockRequest);
         if (owner == null) {
            return 0;
         } else {
            return owner.getEntryCount();
         }
      } else {
         if (writeLockOwner == null) {
            return 0;
         } else {
            if (writeLockOwner.cameFromRequester(lockRequest)) {
               return writeLockOwner.getEntryCount();
            } else {
               return 0;
            }
         }
      }
   }


   /**
    * Adds a given owner to the registry of forced releases.
    *
    * @param owner lock owner for that a forced release announcement was posted.
    * @see ReleaseLockRequest
    */
   public void registerForcedRelease(final LockOwner owner) {

      forcedReleasesRegistry.add(owner);
   }


   /**
    * Returns <code>true</code> if a forced release had been initiated for the given lock owner. Otherwise returns
    * <code>false</code>.
    *
    * @param ownerToCheck the lock owner to check.
    * @return <code>true</code> if a forced release had been initiated for the given lock owner.
    */
   public boolean isRegisteredInForcedReleases(final LockOwner ownerToCheck) {

      for (final LockOwner registeredOwner : forcedReleasesRegistry) {
         if (registeredOwner.getThreadID() == ownerToCheck.getThreadID()
                 && registeredOwner.getAddress().equals(ownerToCheck.getAddress())) {
            return true;
         }
      }
      return false;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      if (writeLockOwner == null) {
         out.writeBoolean(true);
      } else {
         out.writeBoolean(false);
         writeLockOwner.writeWire(out);
      }
      out.writeInt(pendingRequests.size());
      for (final AcquireLockRequest lockRequest : pendingRequests) {
         lockRequest.writeWire(out);
      }
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      if (in.readBoolean()) {
         writeLockOwner = null;
      } else {
         writeLockOwner = new LockOwner();
         writeLockOwner.readWire(in);
      }
      final int size = in.readInt();
      for (int i = 0; i < size; i++) {
         final AcquireLockRequest lockRequest = new AcquireLockRequest();
         lockRequest.readWire(in);
         pendingRequests.add(lockRequest);
      }
   }


   public int getWireableType() {

      return TYPE_LOCK_QUEUE;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final LockQueue lockQueue = (LockQueue) o;

      if (pendingRequests != null ? !pendingRequests.equals(lockQueue.pendingRequests) : lockQueue.pendingRequests != null) {
         return false;
      }
      if (readLockOwners != null ? !readLockOwners.equals(lockQueue.readLockOwners) : lockQueue.readLockOwners != null) {
         return false;
      }
      if (writeLockOwner != null ? !writeLockOwner.equals(lockQueue.writeLockOwner) : lockQueue.writeLockOwner != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = pendingRequests != null ? pendingRequests.hashCode() : 0;
      result = 31 * result + (writeLockOwner != null ? writeLockOwner.hashCode() : 0);
      result = 31 * result + (readLockOwners != null ? readLockOwners.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "LockQueue{" +
              "pendingRequests=" + pendingRequests +
              ", writeLockOwner=" + writeLockOwner +
              ", readLockOwners=" + readLockOwners +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new LockQueue();
      }
   }
}
