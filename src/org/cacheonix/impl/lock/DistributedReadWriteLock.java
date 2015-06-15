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

import java.io.Serializable;

import org.cacheonix.locks.Lock;
import org.cacheonix.locks.ReadWriteLock;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.cluster.ClusterProcessor;

/**
 * A distributed implementation of a read-write lock.
 */
public final class DistributedReadWriteLock implements ReadWriteLock {

   private final ClusterProcessor clusterProcessor;

   private final long defaultUnlockTimeoutMillis;


   /**
    * A name of the region where this lock is going to be placed. The region name is used to separate cluster-wide and
    * cache-specific locks.
    */
   private final String lockRegionName;

   /**
    * A lock identifier.
    */
   private final Binary lockKey;


   /**
    * Constructor.
    *
    * @param clusterProcessor           the cluster processor.
    * @param lockRegionName             a name of the region where this lock is going to be placed. The region name is
    *                                   used to separate cluster-wide and cache-specific locks.
    * @param lockKey                    a lock identifier.
    * @param defaultUnlockTimeoutMillis
    */
   public DistributedReadWriteLock(final ClusterProcessor clusterProcessor, final String lockRegionName,
                                   final Binary lockKey, final long defaultUnlockTimeoutMillis) {

      this.clusterProcessor = clusterProcessor;
      this.lockRegionName = lockRegionName;
      this.lockKey = lockKey;
      this.defaultUnlockTimeoutMillis = defaultUnlockTimeoutMillis;
   }


   /**
    * {@inheritDoc}
    */
   public Lock readLock() {

      return new DistributedLock(true, clusterProcessor, lockRegionName, lockKey, defaultUnlockTimeoutMillis);
   }


   /**
    * {@inheritDoc}
    */
   public Lock writeLock() {

      return new DistributedLock(false, clusterProcessor, lockRegionName, lockKey, defaultUnlockTimeoutMillis);
   }


   public Serializable getLockKey() {

      return lockKey;
   }


   public String toString() {

      return "DistributedReadWriteLock{" +
              "lockRegionName='" + lockRegionName + '\'' +
              ", lockKey=" + lockKey +
              '}';
   }
}
