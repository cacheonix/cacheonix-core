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
package org.cacheonix.impl.net.cluster;

import java.util.List;

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.util.time.Timeout;

/**
 * Join status is a bean that holds information related to join process that a process in Blocked state maybe
 * participating in.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface JoinStatus {

   /**
    * Returns the process we are joining to.
    *
    * @return the process we are joining to.
    */
   ClusterNodeAddress getJoiningToProcess();

   /**
    * Sets the process we are joining to.
    *
    * @param process the address the node is joining.
    */
   void setJoiningTo(ClusterNodeAddress process);

   /**
    * Returns the marker list that we received from the {@link #getJoiningToProcess()}.
    *
    * @return the marker list that we received from the {@link #getJoiningToProcess()}.
    */
   ClusterView getJoiningToCluster();

   /**
    * Sets a marker list that has been received from the {@link #getJoiningToProcess()}.
    *
    * @param joiningToCluster the marker list that has been received from the {@link #getJoiningToProcess()}.
    */
   void setJoiningToCluster(ClusterView joiningToCluster);

   ClusterView getLastOperationalClusterView();

   void setLastOperationalCluster(ClusterView lastOperationalClusterView);

   /**
    * Sets a replicated state of the process we are joining.
    *
    * @param replicatedState the replicated state of the process we are joining.
    */
   void setReplicatedState(ReplicatedState replicatedState);

   /**
    * Returns a replicated state of the process we are joining.
    *
    * @return the replicated state of the process we are joining.
    */
   ReplicatedState getReplicatedState();

   /**
    * Join timeout.
    *
    * @return join timeout.
    */
   Timeout getTimeout();

   /**
    * Sets frames present in the message assembler at the time of creating the {@link MarkerListRequest}.
    *
    * @param messageAssemblerParts a list of frames present in the message assembler at the time of creating the {@link
    *                              MarkerListRequest}.
    * @see MarkerListRequest
    */
   void setMessageAssemblerParts(List<Frame> messageAssemblerParts);

   List<Frame> getMessageAssemblerParts();

   /**
    * Returns <code>true</code> if joining. Returns <code>false</code> if not joining. If was joining but the timeout
    * has expired, will clear joining status and return <code>false</code>.
    *
    * @return <code>true</code> if joining. Returns <code>false</code> if not joining. If was joining but the timeout
    * has expired, will clear joining status and return <code>false</code>.
    */
   boolean isJoining();

   /**
    * Returns <code>true</code> if we received marker list.
    *
    * @return <code>true</code> if we received marker list.
    */
   boolean isReceivedMarkerList();

   void registerObservation(ObservedClusterNode newNode);

   ObservedClusterNode getStrongestObservedClusterNode();

   boolean clusterSurveyTimeoutExpired();

   /**
    * Clears the join status.
    */
   void clear();
}
