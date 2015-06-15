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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.NetUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An event that is sent to subscribers when a node joins the cluster. This event is sent both when a local node joins
 * the cluster and remote nodes join the cluster.
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Mar 16, 2009 1:47:14 AM
 */
final class ClusterNodeJoinedEventImpl implements ClusterNodeJoinedEvent {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterNodeJoinedEventImpl.class); // NOPMD

   private final List<ClusterNodeAddress> nodes;


   ClusterNodeJoinedEventImpl(final Collection<ClusterNodeAddress> nodes) {

      this.nodes = new ArrayList<ClusterNodeAddress>(nodes);
   }


   ClusterNodeJoinedEventImpl(final ClusterNodeAddress address) {

      this(NetUtils.addressToList(address));
   }


   @SuppressWarnings("unchecked")
   public List<ClusterNodeAddress> getNodes() {

      return Collections.unmodifiableList(nodes);
   }


   public String toString() {

      return "ClusterNodesJoinedEventImpl{" +
              "nodes=" + nodes +
              '}';
   }
}
