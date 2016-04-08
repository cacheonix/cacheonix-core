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
package org.cacheonix.impl.cache.local;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cacheonix.locks.Lock;
import org.cacheonix.locks.ReadWriteLock;

/**
 * An implementation of <code>cacheonix.locks.ReadWriteLock</code> used by LocalCache. LocalReadWriteLock delegates its
 * operations to <code>java.util.concurrent.locks.ReentrantReadWriteLock</code>.
 *
 * @see LocalCache#getReadWriteLock() ()
 * @see ReentrantLock
 */
public final class LocalReadWriteLock implements ReadWriteLock {

   /**
    * Delegate.
    */
   private final ReentrantReadWriteLock delegate = new ReentrantReadWriteLock();

   private final Serializable lockKey;


   public LocalReadWriteLock(final Serializable lockKey) {

      this.lockKey = lockKey;
   }


   public Lock readLock() {

      return new LocalLock(delegate.readLock());
   }


   public Lock writeLock() {

      return new LocalLock(delegate.writeLock());
   }


   public Serializable getLockKey() {

      return lockKey;
   }


   public String toString() {

      return "LocalReadWriteLock{" +
              "lock=" + delegate +
              '}';
   }
}
