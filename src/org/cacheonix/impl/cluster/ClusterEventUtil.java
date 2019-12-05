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
package org.cacheonix.impl.cluster;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cacheonix.cluster.ClusterConfiguration;
import org.cacheonix.cluster.ClusterMember;
import org.cacheonix.cluster.ClusterMemberAddress;
import org.cacheonix.cluster.ClusterState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterView;

import static org.cacheonix.cluster.ClusterState.BLOCKED;
import static org.cacheonix.cluster.ClusterState.OPERATIONAL;
import static org.cacheonix.cluster.ClusterState.RECONFIGURING;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_BLOCKED;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_CLEANUP;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_NORMAL;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_RECOVERY;

/**
 * Utility class.
 */
public final class ClusterEventUtil {

   private ClusterEventUtil() {

   }


   public static ClusterConfiguration getUserClusterConfiguration(final String clusterName, final int state,
           final ClusterView clusterView) {

      // Create cluster member list
      final List<ClusterNodeAddress> clusterNodeList = clusterView == null ? Collections.<ClusterNodeAddress>emptyList() : clusterView.getClusterNodeList();
      final ArrayList<ClusterMember> clusterMembers = new ArrayList<ClusterMember>(clusterNodeList.size());
      for (final ClusterNodeAddress clusterNodeAddress : clusterNodeList) {

         clusterMembers.add(createClusterMember(clusterName, clusterNodeAddress));
      }

      // Get UUID

      final String uuid = clusterView == null ? null : clusterView.getClusterUUID().toString();

      // Get user cluster state
      final ClusterState clusterState = convertStateMachineToUserClusterState(state);

      // Create cluster configuration
      return new ClusterConfigurationImpl(uuid, clusterState, clusterMembers);
   }


   public static ClusterMember createClusterMember(final String clusterName,
           final ClusterNodeAddress clusterNodeAddress) {

      final InetAddress[] inetAddresses = clusterNodeAddress.getAddresses();
      final List<ClusterMemberAddress> clusterMemberAddresses = new ArrayList<ClusterMemberAddress>(
              inetAddresses.length);
      for (final InetAddress inetAddress : inetAddresses) {

         clusterMemberAddresses.add(new ClusterMemberAddressImpl(inetAddress));
      }

      return new ClusterMemberImpl(clusterName, clusterMemberAddresses, clusterNodeAddress.getTcpPort());
   }


   /**
    * Converts internal state machine to
    *
    * @param state internal state machine to convert.
    * @return user cluster state.
    * @throws IllegalArgumentException if the state machine cannot be converted to the user cluster state.
    */
   public static ClusterState convertStateMachineToUserClusterState(final int state) throws IllegalArgumentException {

      switch (state) {
         case STATE_BLOCKED:
            return BLOCKED;
         case STATE_NORMAL:
            return OPERATIONAL;
         case STATE_RECOVERY:
            return RECONFIGURING;
         case STATE_CLEANUP:
            return RECONFIGURING;
         default:
            throw new IllegalArgumentException("Unknown state: " + state);
      }
   }
}
