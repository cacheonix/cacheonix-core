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
package org.cacheonix.impl.cache.local;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cacheonix.cluster.Cluster;
import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.locks.ReadWriteLock;
import org.cacheonix.impl.util.array.HashMap;

/**
 * An implementation of the cluster object specific to the local Cacheonix.
 */
public final class LocalCluster implements Cluster {

   /**
    * Holds read/write lock.
    */
   private final java.util.concurrent.locks.ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

   /**
    * Convenience reference for write lock.
    */
   private final Lock writeLock = readWriteLock.writeLock();

   /**
    * A registry of read-write locks.
    */
   private final Map<Serializable, ReadWriteLock> lockRegistry = new HashMap<Serializable, ReadWriteLock>(1);


   /**
    * Returns a cluster-wide, distributed lock. This method is equavalent to <code>getReadWriteLock("default")</code>.
    * The created lock is accessible by all members of the cluster.
    *
    * @return the created lock.
    */
   public ReadWriteLock getReadWriteLock() {

      return getReadWriteLock("default");
   }


   /**
    * Returns a cluster-wide, distributed lock. The created lock is accessible by all members of the cluster.
    *
    * @param lockKey the key that uniquely identifies the lock.
    * @return the created lock.
    */
   public ReadWriteLock getReadWriteLock(final Serializable lockKey) {

      writeLock.lock();
      try {

         ReadWriteLock lock = lockRegistry.get(lockKey);

         if (lock == null) {

            lock = new LocalReadWriteLock(lockKey);
            lockRegistry.put(lockKey, lock);
         }
         return lock;

      } finally {

         writeLock.unlock();
      }
   }


   /**
    * Adds a subscriber to cluster events. Example:
    * <p/>
    * <pre>
    *    ClusterEventSubscriber clusterEventSubscriber = new ...;
    *    Cacheonix cacheonix = Cacheonix.getInstance();
    *    Cluster cluster = cacheonix.getCluster();
    *    cluster.addClusterEventSubscriber(clusterEventSubscriber);
    * </pre>
    * <p/>
    * Subscribers to Cacheonix cluster events receive the events in the same total order. This allows developers to use
    * the cluster events to modify application behaviour to reflect changes in the cluster configuration or to monitor
    * cluster status.
    *
    * @param clusterEventSubscriber the subscriber to cluster events to add.
    */
   public void addClusterEventSubscriber(final ClusterEventSubscriber clusterEventSubscriber) {

      // REVIEWME: simeshev@cacheonix.com - 2012-03-10 - Do we need some
      // sort of an implementation or does this method just do nothing.
   }


   /**
    * Unsubscribes a previously-subscribed subscriber to cluster events.
    *
    * @param clusterEventSubscriber the subscriber to unsubscribe.
    * @throws IllegalArgumentException if the subscriber is not subscribed.
    */
   public void removeClusterEventSubscriber(final ClusterEventSubscriber clusterEventSubscriber) {

      // REVIEWME: simeshev@cacheonix.com - 2012-03-10 - Do we need some
      // sort of an implementation or does this method just do nothing.
   }
}
