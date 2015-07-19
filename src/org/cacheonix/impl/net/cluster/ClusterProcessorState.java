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
package org.cacheonix.impl.net.cluster;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.ProcessorState;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.time.Timeout;

/**
 * Cluster processor state.
 */
public interface ClusterProcessorState extends ProcessorState {

   /**
    * This cluster node is in the Normal state.
    */
   int STATE_NORMAL = 1;
   /**
    * This cluster node is in the Recovery state.
    */
   int STATE_RECOVERY = 2;
   /**
    * This cluster node is in the Blocked state.
    */
   int STATE_BLOCKED = 3;
   /**
    * This cluster node is in the Cleanup state.
    */
   int STATE_CLEANUP = 4;

   /**
    * Returns a set of receiver addresses known to a cluster processor that is used to broadcast messages on TCP-only
    * networks.
    *
    * @return a set of receiver addresses known to a cluster processor that is used to broadcast messages on TCP-only
    *         networks.
    */
   HashSet<ClusterNodeAddress> getKnownReceivers(); // NOPMD

   /**
    * Returns target majority size.
    *
    * @return target majority size.
    */
   int getTargetMajoritySize();

   /**
    * Sets target majority size.
    *
    * @param targetMajoritySize the target majority size to set.
    */
   void setTargetMajoritySize(int targetMajoritySize);

   /**
    * Returns <code>true</code> if this processor is an originator of the recovery round.
    *
    * @return <code>true</code> if this processor is an originator of the recovery round.
    */
   boolean isRecoveryOriginator();

   /**
    * Sets the flag indicating if this processor is an originator of the recovery round.
    *
    * @param recoveryOriginator <code>true</code> if this processor is an originator of the recovery round.
    */
   void setRecoveryOriginator(boolean recoveryOriginator);

   /**
    * Returns the state machine's state.
    *
    * @return the state machine's state.
    */
   int getState();

   /**
    * Sets the state machine's state.
    *
    * @param state the state machine's state.
    */
   void setState(int state);

   /**
    * Returns state machine's state as a string.
    *
    * @return state machine's state as a string.
    */
   String getStateName();

   /**
    * Sets this cluster processor's address.
    *
    * @param address this cluster processor's address.
    */
   void setAddress(ClusterNodeAddress address);

   /**
    * Returns the cluster name.
    *
    * @return the cluster name.
    */
   String getClusterName();

   /**
    * Sets cluster name.
    *
    * @param clusterName the cluster name to set. The cluster name is set once when the ClusterProcessor is created.
    */
   void setClusterName(String clusterName);

   /**
    * Returns worst case latency for a host-to-host communication, in milliseconds.
    *
    * @return worst case latency for a host-to-host communication, in milliseconds.
    */
   long getWorstCaseLatencyMillis();

   /**
    * Sets the worst case latency for a host-to-host communication, in milliseconds.
    *
    * @param worstCaseLatencyMillis the worst case latency for a host-to-host communication, in milliseconds.
    */
   void setWorstCaseLatencyMillis(long worstCaseLatencyMillis);

   /**
    * Returns the time interval that defines how often Cacheonix cluster announces itself using multicast.
    *
    * @return the time interval that defines how often Cacheonix cluster announces itself using multicast.
    */
   long getClusterAnnouncementTimeoutMillis();

   /**
    * Sets the time interval that defines how often Cacheonix cluster announces itself using multicast.
    *
    * @param clusterAnnouncementTimeoutMillis
    *         the time interval that defines how often Cacheonix cluster announces itself using multicast.
    */
   void setClusterAnnouncementTimeoutMillis(long clusterAnnouncementTimeoutMillis);

   /**
    * Sets new marker list. This method is called when a cluster node [re-]joins a cluster.
    *
    * @param clusterView the cluster view to set.
    */
   void setClusterView(ClusterView clusterView);

   /**
    * Returns marker list.
    *
    * @return marker list
    */
   ClusterView getClusterView();

   Long getHighestSequenceNumberDelivered();

   void setHighestSequenceNumberDelivered(Long highestSequenceNumberDelivered);

   void setCurrent(Long current);

   Long getCurrent();

   /**
    * Calculates heuristic wost-case multicast marker timeout.
    *
    * @return calculated wost-case multicast marker timeout.
    */
   @SuppressWarnings("UnsecureRandomNumberGeneration")
   long calculateMarkerTimeout();

   /**
    * Calculates a heuristic wost-case leave timeout.
    *
    * @return the wost-case leave timeout.
    */
   long calculateLeaveTimeout();

   /**
    * @noinspection ReturnOfCollectionOrArrayField
    */
   Queue<List<Frame>> getSubmittalQueue();

   ClusterView getLastOperationalClusterView();

   /**
    * Updates last operational cluster view by creating a copy.
    *
    * @param clusterView a cluster view to update
    */
   void updateLastOperationalClusterView(ClusterView clusterView);

   long incrementMarkerCounter();

   long getMarkerCounter();

   /**
    * Returns join status.
    *
    * @return join status.
    */
   JoinStatus getJoinStatus();

   /**
    * Sets join status.
    *
    * @param joinStatus the join status to set.
    */
   void setJoinStatus(JoinStatus joinStatus);

   /**
    * Returns join requests we are handling. Empty means there are no join requests
    *
    * @return join requests we are handling. Empty means there are no join requests
    */
   LinkedList<JoiningNode> getJoinRequests(); // NOPMD

   /**
    * Returns replicated state.
    *
    * @return replicated state.
    */
   ReplicatedState getReplicatedState();

   /**
    * Sets replicated state instance. This method can be called only before the service has been started.
    *
    * @param replicatedState replicate state to set.
    */
   void setReplicateState(ReplicatedState replicatedState);

   /**
    * Returns a timer that measures time for how long a node stayed alone without other nodes present to form a
    * cluster.
    * <p/>
    * The timer is <b>set</b>:
    * <p/>
    * 1. First time is when a node is created [in Blocked state].
    * <p/>
    * 2. When a node enters a blocked state after recovery and the number of nodes is 1.
    * <p/>
    * The timer is <b>canceled</b> when a node leaves the Blocked state.
    * <p/>
    * The timer is <b>re-started</b> when a node sends a request to join and when it receives a success response to join
    * request.
    *
    * @return a timer that measures time for how long a node stayed alone without other nodes present to form a
    *         cluster.
    */
   Timeout getHomeAloneTimeout();

   /**
    * Sets a timer that measures time for how long a node stayed alone without other nodes present to form a cluster.
    *
    * @param homeAloneTimeout a timer that measures time for how long a node stayed alone without other nodes present to
    *                         form a cluster.
    */
   void setHomeAloneTimeout(Timeout homeAloneTimeout);

   /**
    * Returns messages received from the network and pending ordering and delivery.
    *
    * @return messages received from the network and pending ordering and delivery.
    */
   ReceivedList getReceivedList();

   /**
    * Adds user's cluster event subscriber. User's event subscribers are notified about cluster asynchronously to
    * prevent the cluster thread from blocking.
    *
    * @param clusterEventSubscriber the subscriber to add.
    * @return true if the subscriber was added.
    */
   boolean addUserClusterEventSubscriber(ClusterEventSubscriber clusterEventSubscriber);

   /**
    * Removes user cluster event subscriber.
    *
    * @param userClusterEventSubscriber a user cluster event subscriber to remove.
    * @return true if the subscriber was removed. false if the subscriber didn't exist.
    */
   boolean removeUserClusterEventSubscriber(ClusterEventSubscriber userClusterEventSubscriber);

   /**
    * Returns an executor responsible for executing user event notifications.
    *
    * @return the executor responsible for executing user event notifications.
    */
   Executor getUserEventExecutor();

   /**
    * Returns a list of user cluster event subscribers.
    *
    * @return a list of user cluster event subscribers.
    */
   List<ClusterEventSubscriber> getClusterEventSubscribers();
}
