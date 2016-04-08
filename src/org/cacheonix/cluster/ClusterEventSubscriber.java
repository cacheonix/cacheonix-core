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
package org.cacheonix.cluster;

/**
 * A subscriber to cluster events. The event subscribers in an operational Cacheonix cluster receive the cluster events
 * in the same guaranteed total order. Use <code>Cluster.addClusterEventSubscriber()</code> to subscribe to the events.
 * Example:
 * <p/>
 * <pre>
 *    ClusterEventSubscriber clusterEventSubscriber = new ...;
 *    Cacheonix cacheonix = Cacheonix.getInstance();
 *    Cluster cluster = cacheonix.getCluster();
 *    cluster.addClusterEventSubscriber(clusterEventSubscriber);
 * </pre>
 * <p/>
 * The first event <code>ClusterEventSubscriber</code> receives is <code>ClusterEventSubscriptionStartedEvent</code>
 * that is followed by a set of <code>ClusterMemberJoinedEvent</code>, <code>ClusterMemberLeftEvent</code> and
 * <code>clusterStateChangedEvent</code>. If the subscriber was explicitly unsubscribed, Cacheonix will also send
 * <code>ClusterEventSubscriptionEndedEvent</code> as a last event.
 *
 * @see Cluster#addClusterEventSubscriber(ClusterEventSubscriber)
 * @see #notifyClusterEventSubscriptionStarted(ClusterEventSubscriptionStartedEvent)
 * @see #notifyClusterMemberJoined(ClusterMemberJoinedEvent)
 * @see #notifyClusterMemberLeft(ClusterMemberLeftEvent)
 * @see #notifyClusterStateChanged(ClusterStateChangedEvent)
 * @see #notifyClusterEventSubscriptionEnded(ClusterEventSubscriptionEndedEvent)
 */
public interface ClusterEventSubscriber {

   /**
    * Notifies this subscriber that its subscription to cluster events has began.
    *
    * @param clusterEventSubscriptionStartedEvent
    *         the event that Cacheonix sends to this subscriber  when its subscription begins. This is the first
    *         notification that Cacheonix sends to a subscriber after it was subscribed.
    */
   void notifyClusterEventSubscriptionStarted(
           ClusterEventSubscriptionStartedEvent clusterEventSubscriptionStartedEvent);

   /**
    * Notifies this subscriber that new members joined the cluster.
    *
    * @param clusterMemberJoinedEvent the event that Cacheonix sends to this subscriber when members joins a Cacheonix
    *                                 cluster.
    */
   void notifyClusterMemberJoined(ClusterMemberJoinedEvent clusterMemberJoinedEvent);

   /**
    * Notifies this subscriber that members left the cluster.
    *
    * @param clusterMemberLeftEvent the event that Cacheonix sends to this subscriber when members leave a Cacheonix
    *                               cluster.
    */
   void notifyClusterMemberLeft(ClusterMemberLeftEvent clusterMemberLeftEvent);


   /**
    * Notifies this subscriber that the cluster state has changed. For more information on cluster state changes please
    * see {@link ClusterState}.
    *
    * @param clusterStateChangedEvent the event that Cacheonix sends to this subscriber when the cluster state changes.
    */
   void notifyClusterStateChanged(ClusterStateChangedEvent clusterStateChangedEvent);


   /**
    * Notifies this subscriber that its subscription to cluster events has ended. Use {@link
    * Cluster#removeClusterEventSubscriber(ClusterEventSubscriber)} to unsubscribe from cluster events.
    *
    * @param clusterEventSubscriptionEndedEvent
    *         the event that Cacheonix sends to this subscriber  when its subscription ends. This is the last
    *         notification that Cacheonix sends to a subscriber before it is un-subscribed.
    */
   void notifyClusterEventSubscriptionEnded(ClusterEventSubscriptionEndedEvent clusterEventSubscriptionEndedEvent);
}
