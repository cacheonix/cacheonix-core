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
package org.cacheonix.impl.cluster;

import java.io.Serializable;

import org.cacheonix.cluster.Cluster;
import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.impl.cache.distributed.partitioned.Retrier;
import org.cacheonix.impl.cache.distributed.partitioned.Retryable;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.BinaryUtils;
import org.cacheonix.impl.cluster.event.AddClusterEventSubscriberRequest;
import org.cacheonix.impl.cluster.event.RemoveClusterEventSubscriberRequest;
import org.cacheonix.impl.lock.DistributedReadWriteLock;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.util.ArgumentValidator;
import org.cacheonix.locks.ReadWriteLock;

/**
 * An implementation of the API interface for object that represents an API view of a cluster that a Cacheonix is a
 * member of.
 */
public final class ClusterImpl implements Cluster {

   /**
    * Cluster processor.
    */
   private final ClusterProcessor clusterProcessor;

   private final long defaultUnlockTimeoutMillis;


   /**
    * Creates ClusterImpl.
    *
    * @param clusterProcessor           a cluster processor.
    * @param defaultUnlockTimeoutMillis the default unlock timeout.
    */
   public ClusterImpl(final ClusterProcessor clusterProcessor, final long defaultUnlockTimeoutMillis) {

      this.defaultUnlockTimeoutMillis = defaultUnlockTimeoutMillis;
      this.clusterProcessor = clusterProcessor;
   }


   /**
    * {@inheritDoc}
    */
   public ReadWriteLock getReadWriteLock() {

      return getReadWriteLock("default");
   }


   /**
    * {@inheritDoc}
    *
    * @param lockKey
    */
   public ReadWriteLock getReadWriteLock(final Serializable lockKey) {

      final Binary binaryLockKey = BinaryUtils.toBinary(lockKey);
      return new DistributedReadWriteLock(clusterProcessor, "cluster", binaryLockKey, defaultUnlockTimeoutMillis);
   }


   /**
    * {@inheritDoc}
    */
   public void addClusterEventSubscriber(final ClusterEventSubscriber clusterEventSubscriber) {

      // Validate subscriber
      ArgumentValidator.validateArgumentNotNull(clusterEventSubscriber, "clusterEventSubscriber");

      // Execute request
      final Retrier retrier = new Retrier();
      retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            // Create the local subscription request
            final AddClusterEventSubscriberRequest request = new AddClusterEventSubscriberRequest(clusterEventSubscriber);

            // Enqueue directly to the cluster processor as this message is designated for the cluster processor only
            clusterProcessor.execute(request);

            // This method doesn't return a result
            return null;
         }


         public String description() {

            return "addClusterEventSubscriber";
         }
      });
   }


   /**
    * {@inheritDoc}
    */
   public void removeClusterEventSubscriber(
           final ClusterEventSubscriber clusterEventSubscriber) throws IllegalArgumentException {

      // Validate subscriber
      ArgumentValidator.validateArgumentNotNull(clusterEventSubscriber, "clusterEventSubscriber");

      final Retrier retrier = new Retrier();
      retrier.retryUntilDone(new Retryable() {

         public Object execute() throws RetryException {

            // Create the local un-subscription request
            final RemoveClusterEventSubscriberRequest request = new RemoveClusterEventSubscriberRequest(clusterEventSubscriber);

            // Enqueue directly to the cluster processor as this message is designated for the cluster processor only
            clusterProcessor.execute(request);

            // This method doesn't return a result
            return null;
         }


         public String description() {

            return "removeClusterEventSubscriber";
         }
      });
   }
}
