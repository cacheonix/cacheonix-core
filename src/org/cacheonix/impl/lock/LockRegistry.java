package org.cacheonix.impl.lock;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.array.HashMap;

public interface LockRegistry extends Wireable {

   /**
    * Returns a lock queue for a lock identified by the combination of <code>lockRegionName</code> and
    * <code>lockKey</code>. If the lock queue does not exist, creates and registers it.
    *
    * @param lockRegionName a name of the region where this lock is going to be placed. The region name is used to
    *                       separate cluster-wide and cache-specific locks.
    * @param lockKey @return a lock queue.
    * @return the lock queue.
    */
   LockQueue getLockQueue(String lockRegionName, Binary lockKey);

   HashMap<LockQueueKey, LockQueue> getLockQueues();
}
