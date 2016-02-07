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

import org.cacheonix.cluster.ClusterConfiguration;
import org.cacheonix.cluster.ClusterEventSubscriptionStartedEvent;

/**
 * An implementation of {@link ClusterEventSubscriptionStartedEvent}.
 */
final class ClusterEventSubscriptionStartedEventImpl implements ClusterEventSubscriptionStartedEvent {

   /**
    * A cluster configuration at the time the subscription started.
    */
   private final ClusterConfiguration clusterConfiguration;


   /**
    * Creates a new instance of ClusterEventSubscriptionStartedEventImpl.
    *
    * @param clusterConfiguration the cluster configuration.
    */
   ClusterEventSubscriptionStartedEventImpl(final ClusterConfiguration clusterConfiguration) {

      this.clusterConfiguration = clusterConfiguration;
   }


   /**
    * Returns the cluster configuration at the time the subscription started.
    *
    * @return the cluster configuration at the time the subscription started.
    */
   public ClusterConfiguration getClusterConfiguration() {

      return clusterConfiguration;
   }


   public String toString() {

      return "ClusterEventSubscriptionStartedEventImpl{" +
              "clusterConfiguration=" + clusterConfiguration +
              '}';
   }
}
