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

import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;

/**
 * An event that is sent to subscribers when a node joins the cluster. This event is sent both when a local node joins
 * the cluster and remote nodes join the cluster.
 */
public interface ClusterNodeJoinedEvent {


   /**
    * Returns an unmodifiable list of nodes that joined the cluster.
    *
    * @return nodes that joined the cluster.
    * @see ClusterNodeAddress
    */
   List<ClusterNodeAddress> getNodes();
}