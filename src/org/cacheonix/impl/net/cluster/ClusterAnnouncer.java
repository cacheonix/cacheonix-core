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

import java.io.IOException;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.multicast.sender.MulticastSender;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Cluster announcer.
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Apr 16, 2008 11:50:26 PM
 */
final class ClusterAnnouncer {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterAnnouncer.class); // NOPMD

   /**
    * System clock.
    */
   private final Clock clock;

   private final MulticastSender multicastSender;

   private final ClusterNodeAddress senderAddress;

   private final String clusterName;


   /**
    * Constructs cluster announcer.
    *
    * @param clock           the system clock.
    * @param multicastSender the mcast sender.
    * @param clusterName     the cluster name.
    * @param senderAddress   the node address.
    */
   ClusterAnnouncer(final Clock clock, final MulticastSender multicastSender, final String clusterName,
                    final ClusterNodeAddress senderAddress) {

      this.clock = clock;
      this.multicastSender = multicastSender;
      this.clusterName = clusterName;
      this.senderAddress = senderAddress;
   }


   /**
    * Announces cluster.
    *
    * @param clusterUUID    a UUID of the cluster
    * @param representative representative address. Receivers may use it for lexicographical comparison in case cluster
    *                       sizes are the same.
    * @param markerListSize cluster's marker list size.
    * @param operational    <code>true</code> if the announcement is sent from an operational cluster
    * @throws IOException if an error occurs when sending an announcement
    */
   public void announce(final UUID clusterUUID, final ClusterNodeAddress representative, final int markerListSize,
                        final boolean operational) throws IOException {

      // Create announcement
      final ClusterAnnouncement ann = new ClusterAnnouncement(clusterName, senderAddress, operational, markerListSize, representative);
      ann.setTimestamp(clock.currentTime());
      ann.setClusterUUID(clusterUUID);

      // Create frame
      final Frame frame = new Frame();
      frame.setMaximumMessageLength(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH);
      frame.setPayload(Serializer.TYPE_JAVA, ann);
      frame.setClusterUUID(clusterUUID);
      frame.setSequenceNumber(-1L);


      // Send
      multicastSender.sendFrame(frame);
   }


   public String toString() {

      return "ClusterAnnouncer{" +
              "multicastSender=" + multicastSender +
              ", clusterNodeAddress=" + senderAddress +
              '}';
   }
}
