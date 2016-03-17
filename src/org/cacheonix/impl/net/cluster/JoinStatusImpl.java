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
import java.util.List;

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.config.ClusterConfiguration;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.time.Timeout;
import org.cacheonix.impl.util.time.TimeoutImpl;

import static org.cacheonix.impl.config.ConfigurationConstants.DEFAULT_JOIN_TIMEOUT_MILLIS;

/**
 * Join status is a bean that holds information related to join process that a process in Blocked state maybe
 * participating in.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 20, 2008 2:35:12 PM
 */
final class JoinStatusImpl implements JoinStatus {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(JoinStatusImpl.class); // NOPMD


   /**
    * Process that we are trying to join.
    */
   private ClusterNodeAddress joiningToProcess = null;

   /**
    * Marker list that we received from the {@link #joiningToProcess}.
    */
   private ClusterView joiningToCluster = null;


   /**
    * A timeout to join.
    */
   private final Timeout timeout = new TimeoutImpl(DEFAULT_JOIN_TIMEOUT_MILLIS);

   /**
    * Timeout to wait to identify all available Cacheonix nodes before stating a join procedure.
    *
    * @see ClusterConfiguration#getClusterSurveyTimeoutMillis()
    */
   private final Timeout clusterSurveyTimeout;

   /**
    * Replicated state that we received from the {@link #joiningToProcess}.
    */
   private ReplicatedState replicatedState = null;

   /**
    * Message assembler parts at the moment of join.
    */
   private List<Frame> messageAssemblerParts = null;

   private ClusterView lastOperationalClusterView;

   /**
    * Observed cluster nodes are the nodes that this cluster node heard from.
    */
   private ObservedClusterNode strongestObservedClusterNode;


   /**
    * Constructs join status object.
    *
    * @param clusterSurveyTimeoutMillis the time that a new Cacheonix node waits for to identify all available Cacheonix
    *                                   nodes before stating a join procedure.
    * @see ClusterConfiguration#getClusterSurveyTimeoutMillis()
    */
   JoinStatusImpl(final long clusterSurveyTimeoutMillis) {

      this.clusterSurveyTimeout = new TimeoutImpl(clusterSurveyTimeoutMillis);

      clear();
   }


   public ClusterNodeAddress getJoiningToProcess() {

      return joiningToProcess;
   }


   public void setJoiningTo(final ClusterNodeAddress process) {

      this.joiningToProcess = process;
   }


   public ClusterView getJoiningToCluster() {

      return joiningToCluster;
   }


   public void setJoiningToCluster(final ClusterView joiningToCluster) {

      this.joiningToCluster = joiningToCluster;
   }


   public ClusterView getLastOperationalClusterView() {

      return lastOperationalClusterView;
   }


   public void setLastOperationalCluster(final ClusterView lastOperationalClusterView) {

      this.lastOperationalClusterView = lastOperationalClusterView;
   }


   public void setReplicatedState(final ReplicatedState replicatedState) {

      this.replicatedState = replicatedState;
   }


   public ReplicatedState getReplicatedState() {

      return replicatedState;
   }


   public Timeout getTimeout() {

      return timeout;
   }


   public void setMessageAssemblerParts(final List<Frame> messageAssemblerParts) {

      this.messageAssemblerParts = new ArrayList<Frame>(messageAssemblerParts);
   }


   public List<Frame> getMessageAssemblerParts() {

      return messageAssemblerParts;
   }


   public boolean isJoining() {

      if (joiningToProcess == null) {
         return false;
      }
      if (timeout.isExpired()) {
         clear();
         return false;
      }
      return true;
   }


   public boolean isReceivedMarkerList() {

      return joiningToCluster != null && joiningToCluster.getSize() > 0;
   }


   public void registerObservation(final ObservedClusterNode newNode) {

      // Create the registry if not created already.
      if (strongestObservedClusterNode == null) {

         strongestObservedClusterNode = newNode;
         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("strongestObservedClusterNode: " + strongestObservedClusterNode); // NOPMD
         clusterSurveyTimeout.reset();
      } else {

         if (newNode.strongerThan(strongestObservedClusterNode)) {

            strongestObservedClusterNode = newNode;
            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled())
               LOG.debug("strongestObservedClusterNode: " + strongestObservedClusterNode); // NOPMD
         }
      }
   }


   public ObservedClusterNode getStrongestObservedClusterNode() {

      return strongestObservedClusterNode;
   }


   public boolean clusterSurveyTimeoutExpired() {

      return clusterSurveyTimeout.isExpired();
   }


   public void clear() {

      strongestObservedClusterNode = null;
      lastOperationalClusterView = null;
      messageAssemblerParts = null;
      joiningToProcess = null;
      joiningToCluster = null;
      replicatedState = null;

      clusterSurveyTimeout.cancel();
      timeout.cancel();
   }


   public String toString() {

      return "JoinStatus{" +
              "joiningToProcess=" + joiningToProcess +
              ", joiningToCluster=" + joiningToCluster +
              ", timeout=" + clusterSurveyTimeout +
              ", replicatedState=" + replicatedState +
              ", messageAssemblerParts=" + messageAssemblerParts +
              ", lastOperationalClusterView=" + lastOperationalClusterView +
              '}';
   }
}
