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
package org.cacheonix.impl.cache.local;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import org.cacheonix.ShutdownException;
import org.cacheonix.locks.BrokenLockException;
import org.cacheonix.locks.DeadlockException;
import org.cacheonix.locks.Lock;

/**
 * A local lock.
 */
public final class LocalLock implements Lock {

   private final java.util.concurrent.locks.Lock delegate;


   public LocalLock(final java.util.concurrent.locks.Lock delegate) {

      this.delegate = delegate;
   }


   @SuppressWarnings("LockAcquiredButNotSafelyReleased")
   public void lock() throws DeadlockException, ShutdownException {

      delegate.lock();
   }


   @SuppressWarnings("LockAcquiredButNotSafelyReleased")
   public void lockInterruptibly() throws InterruptedException {

      delegate.lockInterruptibly();
   }


   @SuppressWarnings("LockAcquiredButNotSafelyReleased")
   public void lock(final long unlockTimeoutMillis) {

      delegate.lock();
   }


   public boolean tryLock() {

      return delegate.tryLock();
   }


   public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {

      return delegate.tryLock(time, unit);
   }


   public boolean tryLock(final long timeMillis) throws InterruptedException {

      return delegate.tryLock(timeMillis, TimeUnit.MILLISECONDS);
   }


   public void unlock() throws BrokenLockException {

      delegate.unlock();
   }


   public Condition newCondition() {

      return delegate.newCondition();
   }


   public String toString() {

      return "LocalLock{" +
              "delegate=" + delegate +
              '}';
   }
}
