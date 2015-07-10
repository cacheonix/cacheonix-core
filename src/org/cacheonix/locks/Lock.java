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
package org.cacheonix.locks;

import org.cacheonix.ShutdownException;

/**
 * Lock.
 * <p/>
 * Acquiring a distributed lock in Cacheonix is very easy. To obtains a read call {@link ReadWriteLock#readLock()}. To
 * obtains a write lock call {@link ReadWriteLock#writeLock()}. Below is an example that shows a normal use pattern for
 * distributed locks:
 * <p/>
 * <pre>
 *    Cacheonix cacheonix = Cacheonix.getInstance();
 *    ReadWriteLock readWriteLock = cacheonix.getCluster().getReadWriteLock();
 *    Lock writeLock = readWriteLock.readLock();
 *    writeLock.lock();
 *    try {
 *       // Critical section protected by the lock
 *       ...
 *    } finally {
 *       writeLock.unlock();
 *    }
 * </pre>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection MarkerInterface
 */
public interface Lock extends java.util.concurrent.locks.Lock {

   /**
    * Acquires the lock.
    * <p/>
    * If the lock is not available then the current thread becomes disabled for thread scheduling purposes and lies
    * dormant until the lock has been acquired.
    * <p/>
    * The lock is held for the duration of the unlock timeout.
    * <p/>
    * <b>Setting Default Unlock Timeout</b>
    * <p/>
    * The default lock timeout is set using <code>cacheonix-config.xml</code>.
    * <p/>
    * <b>Example:</b>
    * <pre>
    *   &lt;server defaultUnlockTimeout="60s"&gt;
    *      Rest of the server configuration ...
    *   &lt;/server&gt;
    * </pre>
    * <p/>
    * Use system property <code>cacheonix.default.unlock.timeout</code> to override the lock timeout from the command
    * line. The following example: sets the lock timeout to 10 seconds.
    * <p/>
    * <b>Example:</b>
    * <pre>
    *   java -Dcacheonix.default.unlock.timeout=10s
    * </pre>
    *
    * @throws DeadlockException if Cacheonix detects a deadlock when trying to acquire a lock. A deadlock is an
    *                           inability to proceed due to two threads both requiring to release a lock held by the
    *                           other thread.
    * @see #unlock()
    */
   void lock() throws DeadlockException, ShutdownException;

   /**
    * Acquires the lock with the promise to release it in <code>lockTimeoutMillis</code>.
    * <p/>
    * If the lock is not available then the current thread becomes disabled for thread scheduling purposes and lies
    * dormant until the lock has been acquired.
    * <p/>
    * The lock is held for the duration of <code>unlockTimeoutMillis</code>. The lock is automatically released upon the
    * timeout expiration.
    * <p/>
    * Note: Locks obtained from a local cache ignore the timeout.
    *
    * @param unlockTimeoutMillis timeout in milliseconds after that the lock is automatically released.
    * @see #unlock()
    */
   void lock(final long unlockTimeoutMillis);

   /**
    * Acquires the lock only if it is free at the time of invocation.
    * <p/>
    * Acquires the lock if it is available and returns immediately with the value {@code true}. If the lock is not
    * available then this method will return immediately with the value {@code false}.
    * <p/>
    * A typical usage idiom for this method would be:
    * <pre>
    *      Lock lock = ...;
    *      if (lock.tryLock()) {
    *          try {
    *              // Manipulate protected state
    *          } finally {
    *              lock.unlock();
    *          }
    *      } else {
    *          // Perform alternative actions
    *      }
    * </pre>
    * This usage ensures that the lock is unlocked if it was acquired, and doesn't try to unlock if the lock was not
    * acquired.
    * <p/>
    * If the lock is acquired, it is held for the duration of the default lock timeout.
    *
    * @return {@code true} if the lock was acquired and {@code false} otherwise
    */
   boolean tryLock();


   /**
    * Acquires the lock if it is free within the given waiting timeMillis and the current thread has not been
    * {@linkplain Thread#interrupt interrupted}.
    * <p/>
    * If the lock is available this method returns immediately with the value {@code true}.
    * <p/>
    * If the lock is acquired then the value {@code true} is returned.
    * <p/>
    * If the specified waiting timeMillis elapses then the value {@code false} is returned. If the timeMillis is less
    * than or equal to zero, the method will not wait at all.
    *
    * @param timeMillis the maximum timeMillis to wait for the lock
    * @return {@code true} if the lock was acquired and {@code false} if the waiting timeMillis elapsed before the lock
    *         was acquired
    * @throws InterruptedException if the current thread is interrupted while acquiring the lock.
    */
   boolean tryLock(long timeMillis) throws InterruptedException;


   /**
    * Releases the lock.
    *
    * @throws BrokenLockException if the lock has already been unlocked, a number unlocks greater than a number of locks
    *                             or cluster re-configuration has occurred
    */
   void unlock() throws BrokenLockException;
}
