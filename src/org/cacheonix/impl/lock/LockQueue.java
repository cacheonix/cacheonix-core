package org.cacheonix.impl.lock;

import java.util.LinkedList;
import java.util.List;

import org.cacheonix.impl.net.serializer.Wireable;

/**
 * A queue containing pending lock requests.
 */
public interface LockQueue extends Wireable {

   /**
    * Returns a list of pending lock requests.
    *
    * @return a list of pending lock requests.
    */
   LinkedList<AcquireLockRequest> getPendingRequests();

   /**
    * Returns the owner of the write request.
    *
    * @return the owner of the write request.
    */
   LockOwner getWriteLockOwner();

   /**
    * Returns a list of read lock owners.
    *
    * @return the list of read lock owners.
    */
   List<LockOwner> getReadLockOwners();

   /**
    * Returns true if there are read locks granted.
    *
    * @return true if there are read locks granted.
    */
   boolean areReadLocksGranted();

   /**
    * Return true if there is only read request and it belongs to a particular lock request.
    *
    * @param acquireLockRequest the lock request to check.
    * @return true if there is only read request and it belongs to a particular lock request.
    */
   boolean isOnlyReadLockCameFrom(AcquireLockRequest acquireLockRequest);

   /**
    * Begins tracking granted lock requests.
    *
    * @param request the request to track.
    */
   void grantLockRequest(AcquireLockRequest request);

   /**
    * Releases a lock.
    *
    * @param request the request to release a lock.
    * @return false if the lock was broken (non-existent).
    */
   boolean releaseLock(ReleaseLockRequest request);

   /**
    * Clears write lock owner.
    *
    * @see #releaseLock(ReleaseLockRequest)
    */
   void clearWriteLockOwner();

   /**
    * Returns the number of, possibly re-entrant, locks.
    *
    * @param lockRequest the lock request.
    * @return the number of, possibly re-entrant, locks.
    */
   int getLockEntryCount(LockRequest lockRequest);

   /**
    * Adds a given owner to the registry of forced releases.
    *
    * @param owner lock owner for that a forced release announcement was posted.
    * @see ReleaseLockRequest
    */
   void registerForcedRelease(LockOwner owner);

   /**
    * Returns <code>true</code> if a forced release had been initiated for the given lock owner. Otherwise returns
    * <code>false</code>.
    *
    * @param ownerToCheck the lock owner to check.
    * @return <code>true</code> if a forced release had been initiated for the given lock owner.
    */
   boolean isRegisteredInForcedReleases(LockOwner ownerToCheck);
}
