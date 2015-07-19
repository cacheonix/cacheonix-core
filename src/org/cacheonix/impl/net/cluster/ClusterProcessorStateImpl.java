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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.UserThreadFactory;
import org.cacheonix.impl.util.time.Timeout;

/**
 * Cluster processor state.
 */
final class ClusterProcessorStateImpl implements ClusterProcessorState {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterProcessorStateImpl.class); // NOPMD

   /**
    * A set of receiver addresses known to a cluster processor that is used to broadcast messages on TCP-only networks.
    */
   private final HashSet<ClusterNodeAddress> clusterNodeAddresses = new HashSet<ClusterNodeAddress>(); // NOPMD


   /**
    * Cluster processor's state machine.
    */
   private volatile int state = 0;

   /**
    * Target majority size.
    */
   private int targetMajoritySize = 1;

   /**
    * A flag indicating if this processor is an originator of the recovery round.
    */
   private boolean recoveryOriginator = false;

   private ClusterNodeAddress address;


   private String clusterName;

   /**
    * Worst case latency for a host-to-host communication, in milliseconds.
    */
   private long worstCaseLatencyMillis;


   /**
    * Time interval that defines how often Cacheonix cluster announces itself using multicast. Use system property
    * 'cacheonix.cluster.announcement.timeout' to override clusterAnnouncementTimeout from the command line.
    */
   private long clusterAnnouncementTimeoutMillis;


   /**
    * Cluster view.
    */
   private final AtomicReference<ClusterView> clusterView = new AtomicReference<ClusterView>(null);


   /**
    * Highest sequence number delivered by this stub, or Di
    */
   private Long highestSequenceNumberDelivered = null;


   /**
    *
    */
   private Long current = null;

   /**
    * Submittal queue containing logical messages split into frames ready to be sent.
    */
   private final Queue<List<Frame>> submittalQueue = new ConcurrentLinkedQueue<List<Frame>>();


   /**
    * This list is used to detect if a new list (a) has a majority and also (b) cases when the cluster is split in two
    * equally sized parts and we need to decide which one is the majority.
    */
   private volatile ClusterView lastOperationalClusterView = null;


   /**
    * A counter that keeps track of the number of received multicast markers.
    *
    * @see #incrementMarkerCounter()
    */
   private long markerCounter = 0L;


   /**
    * Join status. A node in NORMAL_STATE may join other cluster if the other cluster is bigger or if its representative
    * is greater then ours.
    */
   private JoinStatus joinStatus;

   /**
    * Join requests we are handling. Empty means there are no join requests.
    */
   private final LinkedList<JoiningNode> joinRequests = new LinkedList<JoiningNode>(); // NOPMD LooseCoupling

   /**
    * This cluster processor replicated state.
    */
   private ReplicatedState replicatedState = null;

   /**
    * An async executor of events sent to API clients.
    */
   private final ThreadPoolExecutor userEventExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new UserThreadFactory("CacheonixEventExecutor"));

   /**
    * A list of cluster event subscribers.
    */
   private final List<ClusterEventSubscriber> clusterEventSubscribers = new ArrayList<ClusterEventSubscriber>(1);


   /**
    * {@inheritDoc}
    */
   public HashSet<ClusterNodeAddress> getKnownReceivers() { // NOPMD

      return clusterNodeAddresses;
   }


   /**
    * Returns target majority size.
    *
    * @return target majority size.
    */
   public int getTargetMajoritySize() {

      return targetMajoritySize;
   }


   /**
    * Sets target majority size.
    *
    * @param targetMajoritySize the target majority size to set.
    */
   public void setTargetMajoritySize(final int targetMajoritySize) {

      this.targetMajoritySize = targetMajoritySize;
   }


   /**
    * Returns <code>true</code> if this processor is an originator of the recovery round.
    *
    * @return <code>true</code> if this processor is an originator of the recovery round.
    */
   public boolean isRecoveryOriginator() {

      return recoveryOriginator;
   }


   /**
    * Sets the flag indicating if this processor is an originator of the recovery round.
    *
    * @param recoveryOriginator <code>true</code> if this processor is an originator of the recovery round.
    */
   public void setRecoveryOriginator(final boolean recoveryOriginator) {

      this.recoveryOriginator = recoveryOriginator;
   }


   /**
    * Timeout after that a process considers that there is no communication path to other processes and forms a
    * single-process cluster.
    * <p/>
    * The default value is 30000 milliseconds or 30 seconds.
    */
   private Timeout homeAloneTimeout;


   /**
    * We used priority queue for receive list in order to sort incoming messages by the message id. This does not
    * guarantee the total ordering, but at least it allows to avoid pseudo-gaps in processing.
    */
   private final ReceivedList receivedList = new ReceivedList();


   /**
    * Returns the state machine's state.
    *
    * @return the state machine's state.
    */
   public int getState() {

      return state;
   }


   /**
    * Sets the state machine's state.
    *
    * @param state the state machine's state.
    */
   public void setState(final int state) {


      if (LOG.isDebugEnabled()) {
         LOG.debug("Changing state of " + address.getTcpPort() + ": " + " from " + convertStateToString(this.state) + " to " + convertStateToString(state));
      }
      this.state = state;
   }


   /**
    * Returns state machine's state as a string.
    *
    * @return state machine's state as a string.
    */
   public String getStateName() {

      return convertStateToString(state);
   }


   /**
    * {@inheritDoc}
    */
   public void setAddress(final ClusterNodeAddress address) {

      this.address = address;
   }


   /**
    * {@inheritDoc}
    */
   public String getClusterName() {

      return clusterName;
   }


   /**
    * {@inheritDoc}
    */
   public void setClusterName(final String clusterName) {

      this.clusterName = clusterName;
   }


   /**
    * Returns worst case latency for a host-to-host communication, in milliseconds.
    *
    * @return worst case latency for a host-to-host communication, in milliseconds.
    */
   public long getWorstCaseLatencyMillis() {

      return worstCaseLatencyMillis;
   }


   /**
    * Sets the worst case latency for a host-to-host communication, in milliseconds.
    *
    * @param worstCaseLatencyMillis the worst case latency for a host-to-host communication, in milliseconds.
    */
   public void setWorstCaseLatencyMillis(final long worstCaseLatencyMillis) {

      this.worstCaseLatencyMillis = worstCaseLatencyMillis;
   }


   /**
    * Returns the time interval that defines how often Cacheonix cluster announces itself using multicast.
    *
    * @return the time interval that defines how often Cacheonix cluster announces itself using multicast.
    */
   public long getClusterAnnouncementTimeoutMillis() {

      return clusterAnnouncementTimeoutMillis;
   }


   /**
    * Sets the time interval that defines how often Cacheonix cluster announces itself using multicast.
    *
    * @param clusterAnnouncementTimeoutMillis
    *         the time interval that defines how often Cacheonix cluster announces itself using multicast.
    */
   public void setClusterAnnouncementTimeoutMillis(final long clusterAnnouncementTimeoutMillis) {

      this.clusterAnnouncementTimeoutMillis = clusterAnnouncementTimeoutMillis;
   }


   /**
    * Sets new marker list. This method is called when a cluster node [re-]joins a cluster.
    *
    * @param clusterView the cluster view to set.
    */
   public void setClusterView(final ClusterView clusterView) {

      // REVIEWME: simeshev@cacheonix.org - 2009-03-06 - This essentially operates on the parameter - consider creating
      // a copy instead. Review usages of setClusterView. Maybe implement hand-off protocol by clearing owner's
      // reference.

      // Get previous group membership subscribers.
      this.clusterView.set(clusterView);
   }


   /**
    * Returns marker list.
    *
    * @return marker list
    */
   public ClusterView getClusterView() {

      return clusterView.get();
   }


   public Long getHighestSequenceNumberDelivered() {

      return highestSequenceNumberDelivered;
   }


   public void setHighestSequenceNumberDelivered(final Long highestSequenceNumberDelivered) {

      this.highestSequenceNumberDelivered = highestSequenceNumberDelivered;
   }


   public void setCurrent(final Long current) {

      this.current = current;
   }


   public Long getCurrent() {

      return current;
   }


   /**
    * Calculates heuristic wost-case multicast marker timeout.
    *
    * @return calculated wost-case multicast marker timeout.
    */
   @SuppressWarnings("UnsecureRandomNumberGeneration")
   public long calculateMarkerTimeout() {

      // We try to *slightly* randomize the timeout to decrease a chance
      // of all nodes starting the recovery protocol in the case of marker
      // loss timeout
      final long randomMills = new Random().nextInt(1000);

      final long clusterViewSize = getClusterView().getSize();

      return (worstCaseLatencyMillis * clusterViewSize) + randomMills;
   }


   /**
    * Calculates a heuristic wost-case leave timeout.
    *
    * @return the wost-case leave timeout.
    */
   public long calculateLeaveTimeout() {

      return calculateMarkerTimeout() * 2;
   }


   /**
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public Queue<List<Frame>> getSubmittalQueue() {

      return submittalQueue;
   }


   public ClusterView getLastOperationalClusterView() {

      return lastOperationalClusterView;
   }


   /**
    * Updates last operational cluster view by creating a copy.
    *
    * @param clusterView a cluster view to update
    */
   public void updateLastOperationalClusterView(final ClusterView clusterView) {

      this.lastOperationalClusterView = clusterView == null ? null : clusterView.copy();
   }


   public long incrementMarkerCounter() {

      return ++markerCounter;
   }


   public long getMarkerCounter() {

      return markerCounter;
   }


   /**
    * Returns join status.
    *
    * @return join status.
    */
   public JoinStatus getJoinStatus() {

      return joinStatus;
   }


   /**
    * Sets join status.
    *
    * @param joinStatus the join status to set.
    */
   public void setJoinStatus(final JoinStatus joinStatus) {

      this.joinStatus = joinStatus;
   }


   /**
    * Returns join requests we are handling. Empty means there are no join requests
    *
    * @return join requests we are handling. Empty means there are no join requests
    */
   public LinkedList<JoiningNode> getJoinRequests() { // NOPMD

      return joinRequests;
   }


   public ReplicatedState getReplicatedState() {

      return replicatedState;
   }


   /**
    * Sets replicated state instance. This method can be called only before the service has been started.
    *
    * @param replicatedState replicate state to set.
    */
   public void setReplicateState(final ReplicatedState replicatedState) {

      this.replicatedState = replicatedState;
   }


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
   public Timeout getHomeAloneTimeout() {

      return homeAloneTimeout;
   }


   /**
    * Sets a timer that measures time for how long a node stayed alone without other nodes present to form a cluster.
    *
    * @param homeAloneTimeout a timer that measures time for how long a node stayed alone without other nodes present to
    *                         form a cluster.
    */
   public void setHomeAloneTimeout(final Timeout homeAloneTimeout) {

      this.homeAloneTimeout = homeAloneTimeout;
   }


   /**
    * Returns messages received from the network and pending ordering and delivery.
    *
    * @return messages received from the network and pending ordering and delivery.
    */
   public ReceivedList getReceivedList() {

      return receivedList;
   }


   /**
    * Adds user's cluster event subscriber. User's event subscribers are notified about cluster asynchronously to
    * prevent the cluster thread from blocking.
    *
    * @param clusterEventSubscriber the subscriber to add.
    */
   public boolean addUserClusterEventSubscriber(final ClusterEventSubscriber clusterEventSubscriber) {

      // First check if it is already there
      for (final ClusterEventSubscriber registeredClusterEventSubscriber : clusterEventSubscribers) {

         //noinspection ObjectEquality
         if (registeredClusterEventSubscriber == clusterEventSubscriber) {

            // Don't add
            return false;
         }
      }

      // Add the new subscriber
      clusterEventSubscribers.add(clusterEventSubscriber);

      // Added
      return true;
   }


   /**
    * {@inheritDoc}
    */
   public boolean removeUserClusterEventSubscriber(final ClusterEventSubscriber userClusterEventSubscriber) {

      for (final Iterator<ClusterEventSubscriber> iterator = clusterEventSubscribers.iterator(); iterator.hasNext(); ) {

         //noinspection ObjectEquality
         if (iterator.next() == userClusterEventSubscriber) {

            // Remove
            iterator.remove();

            // Found
            return true;
         }
      }


      // Subscriber not found
      return false;
   }


   /**
    * Returns a list of user cluster event subscribers.
    *
    * @return a list of user cluster event subscribers.
    */
   public List<ClusterEventSubscriber> getClusterEventSubscribers() {

      return clusterEventSubscribers;
   }


   /**
    * Returns an executor responsible for executing user event notifications.
    *
    * @return the executor responsible for executing user event notifications.
    */
   public Executor getUserEventExecutor() {

      return userEventExecutor;
   }


   /**
    * Converts number state to its String presentation.
    *
    * @param numState numeric state.
    * @return state's String presentation.
    */
   private static String convertStateToString(final int numState) {

      switch (numState) {
         case STATE_BLOCKED:
            return "Blocked";
         case STATE_CLEANUP:
            return "Cleanup";
         case STATE_NORMAL:
            return "Normal";
         case STATE_RECOVERY:
            return "Recovery";
         default:
            return "Unknown: " + numState;
      }
   }
}
