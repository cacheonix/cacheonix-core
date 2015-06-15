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

import org.cacheonix.impl.net.processor.Message;

/**
 * MulticastMessageListener
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Mar 30, 2008 6:14:21 PM
 */
public interface MulticastMessageListener {

   /**
    * Receives a multicast message.
    *
    * @param message to receive
    * @see #notifyClusterNodeJoined(ClusterNodeJoinedEvent)
    * @see #notifyClusterNodeLeft(ClusterNodeLeftEvent)
    */
   void receive(final Message message);

   /**
    * Notifies this subscriber that nodes joined the cluster.
    *
    * @param event an event telling that there was a cluster node joined
    * @see ClusterNodeJoinedEvent
    */
   void notifyClusterNodeJoined(final ClusterNodeJoinedEvent event);

   /**
    * Notifies this subscriber that nodes left the cluster.
    *
    * @param event an event telling that there was a cluster node left
    * @see ClusterNodeLeftEvent
    */
   void notifyClusterNodeLeft(ClusterNodeLeftEvent event);

   /**
    * Notifies subscribers that this cluster node entered a blocked state. This happens when cluster partitions and the
    * number of remaining members in the cluster is smaller than majority. The cluster then enters blocked state and
    * stop sending mcast messages until it unblocks. The cluster unblocks either becuase a timeout expires or nodes join
    * the blocked cluster and the majority is restored.
    *
    * @see #notifyClusterNodeUnblocked()
    */
   void notifyClusterNodeBlocked();


   /**
    * Notifies subscribers that this cluster node exited a blocked state and has become operational. The cluster
    * unblocks either becuase a timeout expires or nodes join the blocked cluster and the majority is restored. This
    * event is always preceded by {@link #notifyClusterNodeBlocked()}.
    *
    * @see #notifyClusterNodeBlocked ()
    */
   void notifyClusterNodeUnblocked();

   /**
    * This notification is sent when this cluster node leaves a previous cluster and joins another cluster. The
    * subscriber <b>must</b> stop sending multicast messages and reset its state until it receives a notification that
    * this node joined the cluster. Any messages that the subscriber has sent before receiving the joined cluster
    * notification are going to be lost.
    *
    * @see #notifyClusterNodeJoined(ClusterNodeJoinedEvent)
    */
   void notifyReset();
}
