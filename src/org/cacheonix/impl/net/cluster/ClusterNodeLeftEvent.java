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
package org.cacheonix.impl.net.cluster;

import java.util.Collection;

import org.cacheonix.impl.net.ClusterNodeAddress;

/**
 * An event that is sent to subscribers when nodes leave the cluster. This event sent both when a local node leaves the
 * cluster and remote nodes leave the cluster.
 * <p/>
 * A local node receives this event when: <ol> <li>The local node leaves the cluster because a shutdown() method has
 * called.</li> <li>The local node belongs to an operational cluster. Then, as a result of network partitioning, the
 * cluster goes to a <code>BlockedState</code> after a reconfiguration because it is no longer a majority cluster. Then
 * the local node joins the majority cluster after the communication is restored. The local node receives events
 * <code>ClusterNodeLeftEvent</code> and <code>ClusterMemberJoinedEvent</code> it this is the case. </ol>
 */
public interface ClusterNodeLeftEvent {


   /**
    * Returns an unmodifiable list of nodes that left the cluster.
    *
    * @return nodes that left the cluster.
    * @see ClusterNodeAddress
    */
   Collection<ClusterNodeAddress> getNodes();
}