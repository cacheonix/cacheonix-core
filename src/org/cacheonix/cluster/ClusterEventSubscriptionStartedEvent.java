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

/**
 * An event that Cacheonix sends to a subscriber to cluster events when its subscription begins. This is the first event
 * that the subscriber receives after subscribing.
 *
 * @see ClusterEventSubscriber#notifyClusterEventSubscriptionStarted(ClusterEventSubscriptionStartedEvent)
 */
public interface ClusterEventSubscriptionStartedEvent {

   /**
    * Returns the cluster configuration at the time the subscription started.
    *
    * @return the cluster configuration at the time the subscription started.
    */
   ClusterConfiguration getClusterConfiguration();
}
