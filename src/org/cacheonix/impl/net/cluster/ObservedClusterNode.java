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

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A cluster announcement is sent periodically by members of the cluster to announce its belonging to a  live cluster.
 * <p/>
 * The cluster announcement is used by new cluster members to obtain information about availability of a live cluster
 * and members of the live cluster that can be used to join it.
 * <p/>
 * The Cluster announcement can be set by clusters in Normal and in Blocked state.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 1, 2008 6:20:20 PM
 */
public final class ObservedClusterNode {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ObservedClusterNode.class); // NOPMD


   /**
    * Cluster UUID.
    */
   private final UUID clusterUUID;

   /**
    * Marker list representative.
    */
   private final ClusterNodeAddress representative;


   /**
    * Indicates if the sending cluster is a member
    */
   private final boolean operationalCluster;

   /**
    * Marker list size.
    */
   private final int markerListSize;

   /**
    * Sender's address to use if the node decides to join.
    */
   private final ClusterNodeAddress senderAddress;


   public ObservedClusterNode(final UUID clusterUUID, final int markerListSize, final ClusterNodeAddress representative,
                              final boolean operationalCluster, final ClusterNodeAddress senderAddress) {

      this.clusterUUID = clusterUUID;
      this.representative = representative;
      this.operationalCluster = operationalCluster;
      this.markerListSize = markerListSize;
      this.senderAddress = senderAddress;
   }


   public ClusterNodeAddress getRepresentative() {

      return representative;
   }


   public boolean isOperationalCluster() {

      return operationalCluster;
   }


   public int getMarkerListSize() {

      return markerListSize;
   }


   public ClusterNodeAddress getSenderAddress() {

      return senderAddress;
   }


   public UUID getClusterUUID() {

      return clusterUUID;
   }


   public boolean strongerThan(final ObservedClusterNode other) {

      return operationalCluster && !other.operationalCluster || markerListSize > other.markerListSize;
   }


   public String toString() {

      return "ClusterSurvey{" +
              "representative=" + representative +
              ", operationalCluster=" + operationalCluster +
              ", markerListSize=" + markerListSize +
              ", senderInetAddress=" + senderAddress +
              '}';
   }
}
