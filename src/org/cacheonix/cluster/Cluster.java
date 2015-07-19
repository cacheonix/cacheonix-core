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
package org.cacheonix.cluster;

import java.io.Serializable;

import org.cacheonix.Cacheonix;
import org.cacheonix.locks.ReadWriteLock;

/**
 * A Cacheonix cluster consists of multiple Cacheonix instances running simultaneously and working together to provide
 * increased scalability and reliability. The Cacheonix instances can be located on different machines or run on the
 * same machine or inside the same JVM. You can increase a capacity of Cacheonix cluster by adding Cacheonix instances
 * to the cluster. Each Cacheonix instance in a cluster must run the same version of Cacheonix.
 *
 * @see Cacheonix#getCluster()
 */
public interface Cluster {

   /**
    * Returns a cluster-wide, distributed lock. This method is equavalent to <code>getReadWriteLock("default")</code>.
    * The created lock is accessible by all members of the cluster.
    *
    * @return the created lock.
    */
   ReadWriteLock getReadWriteLock();

   /**
    * Returns a cluster-wide, distributed lock. The created lock is accessible by all members of the cluster.
    *
    * @param lockKey the key that uniquely identifies the lock.
    * @return the created lock.
    */
   ReadWriteLock getReadWriteLock(final Serializable lockKey);

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
   void addClusterEventSubscriber(ClusterEventSubscriber clusterEventSubscriber);

   /**
    * Unsubscribes a previously-subscribed subscriber to cluster events.
    *
    * @param clusterEventSubscriber the subscriber to unsubscribe.
    * @throws IllegalArgumentException if the subscriber is not subscribed.
    */
   void removeClusterEventSubscriber(ClusterEventSubscriber clusterEventSubscriber) throws IllegalArgumentException;
}
