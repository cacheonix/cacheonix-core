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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import org.cacheonix.locks.BrokenLockException;
import org.cacheonix.locks.DeadlockException;
import org.cacheonix.locks.Lock;
import org.cacheonix.impl.cache.distributed.partitioned.Retrier;
import org.cacheonix.impl.cache.distributed.partitioned.Retryable;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A distributed lock.
 */
public final class DistributedLock implements Lock {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DistributedLock.class); // NOPMD

   private final long defaultUnlockTimeoutMillis;

   private final ClusterProcessor clusterProcessor;

   private final Retrier retrier = new Retrier();

   private final boolean readLock;

   /**
    * A name of the region where this lock is going to be placed. The region name is used to separate cluster-wide and
    * cache-specific locks.
    */
   private final String lockRegionName;

   private final Binary lockKey;


   public DistributedLock(final boolean readLock, final ClusterProcessor clusterProcessor, final String lockRegionName,
                          final Binary lockKey, final long defaultUnlockTimeoutMillis) {

      this.defaultUnlockTimeoutMillis = defaultUnlockTimeoutMillis;
      this.clusterProcessor = clusterProcessor;
      this.lockRegionName = lockRegionName;
      this.readLock = readLock;
      this.lockKey = lockKey;
   }


   /**
    * {@inheritDoc}
    */
   public void lock() {

      acquireLock(null, defaultUnlockTimeoutMillis);
   }


   public void lockInterruptibly() {

      acquireLock(null, defaultUnlockTimeoutMillis);
   }


   /**
    * {@inheritDoc}
    */
   public void lock(final long unlockTimeoutMillis) {

      acquireLock(null, unlockTimeoutMillis);
   }


   /**
    * {@inheritDoc}
    */
   public boolean tryLock() {

      return acquireLock(0L, defaultUnlockTimeoutMillis);
   }


   public boolean tryLock(final long time, final TimeUnit unit) {

      return acquireLock(unit.toMillis(time), defaultUnlockTimeoutMillis);
   }


   /**
    * {@inheritDoc}
    */
   public boolean tryLock(final long timeMillis) {

      return acquireLock(timeMillis, defaultUnlockTimeoutMillis);
   }


   /**
    * Acquires a distributed lock.
    *
    * @param waitForLockTime     time to wait. Null means wait forever.
    * @param unlockTimeoutMillis the unlock timeout
    * @return <code>true</code> if the lock was granted. <code>false</code> if the lock couldn't be acquired in the
    *         given wait time.
    * @throws DeadlockException if a deadlock is detected.
    */
   private boolean acquireLock(final Long waitForLockTime,
                               final long unlockTimeoutMillis) throws DeadlockException {

      // Request
      final Thread thread = Thread.currentThread();
      final String ownerThreadName = thread.getName();
      final int ownerThreadID = System.identityHashCode(thread);
      final ClusterNodeAddress ownerAddress = clusterProcessor.getAddress();

      final Integer result = (Integer) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final Time forcedUnlockTime = clusterProcessor.getClock().currentTime().add(unlockTimeoutMillis);
            final AcquireLockRequest request = new AcquireLockRequest(lockRegionName, lockKey, ownerAddress,
                    ownerThreadID, ownerThreadName, readLock, forcedUnlockTime);

            if (waitForLockTime != null) {
               request.setTimeoutMillis(waitForLockTime);
            }

            return clusterProcessor.execute(request);
         }


         public String description() {

            return "acquireLock";
         }
      });

      if (AcquireLockRequest.RESULT_DETECTED_DEADLOCK.equals(result)) {
         throw new DeadlockException();
      }

      // Return result
      return AcquireLockRequest.RESULT_LOCK_GRANTED.equals(result);
   }


   /**
    * {@inheritDoc}
    */
   public void unlock() throws BrokenLockException {

      final Thread thread = Thread.currentThread();
      final int threadID = System.identityHashCode(thread);
      final String threadName = thread.getName();

      // Request
      final Integer result = (Integer) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final ReleaseLockRequest request = new ReleaseLockRequest(lockRegionName, lockKey, clusterProcessor.getAddress(),
                    threadID, threadName, readLock);
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "unlock";
         }
      });

      if (ReleaseLockRequest.RESULT_RELEASED.equals(result)) {
         return;
      }

      if (ReleaseLockRequest.RESULT_LOCK_BROKEN.equals(result)) {
         throw new BrokenLockException();
      }
   }


   /**
    * {@inheritDoc}
    */
   public Condition newCondition() {

      // REVIEWME: simeshev@cacheonix.org - 2011-08-10 -> Implement.
      throw new UnsupportedOperationException();
   }


   public int getEntryCount() {

      final Thread thread = Thread.currentThread();
      final int threadID = System.identityHashCode(thread);
      final String threadName = thread.getName();

      return (Integer) retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            final EntryCountRequest request = new EntryCountRequest(lockRegionName, lockKey, clusterProcessor.getAddress(),
                    threadID, threadName, readLock);
            request.setReceiver(clusterProcessor.getAddress()); // Set receiver to self.
            return clusterProcessor.execute(request);
         }


         public String description() {

            return "entryCount";
         }
      });
   }


   public String toString() {

      return "DistributedLock{" +
              "lockRegionName='" + lockRegionName + '\'' +
              ", lockKey=" + lockKey +
              ", readLock=" + readLock +
              ", clusterProcessor=" + clusterProcessor +
              ", retrier=" + retrier +
              '}';
   }
}
