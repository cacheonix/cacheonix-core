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
package org.cacheonix.impl.cluster.event;

import org.cacheonix.cluster.ClusterConfiguration;
import org.cacheonix.cluster.ClusterEventSubscriptionStartedEvent;

/**
 * An implementation of ClusterEventSubscriptionStartedEvent.
 */
public final class ClusterEventSubscriptionStartedEventImpl implements ClusterEventSubscriptionStartedEvent {

   private final ClusterConfiguration clusterConfiguration;


   /**
    * Creates a new instance of ClusterEventSubscriptionStartedEventImpl.
    *
    * @param clusterConfiguration the cluster configuration.
    */
   public ClusterEventSubscriptionStartedEventImpl(final ClusterConfiguration clusterConfiguration) {

      this.clusterConfiguration = clusterConfiguration;
   }


   /**
    * Returns the cluster configuration at the time that subscription started.
    *
    * @return the cluster configuration at the time that subscription started.
    */
   public ClusterConfiguration getCurrentClusterConfiguration() {

      return clusterConfiguration;
   }


   public String toString() {

      return "ClusterEventSubscriptionStartedEventImpl{" +
              "clusterConfiguration=" + clusterConfiguration +
              '}';
   }
}
