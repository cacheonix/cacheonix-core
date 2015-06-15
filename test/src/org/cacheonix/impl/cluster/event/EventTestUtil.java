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
package org.cacheonix.impl.cluster.event;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.cacheonix.cluster.ClusterMemberAddress;

/**
 * Utility class.
 */
final class EventTestUtil {


   private EventTestUtil() {

   }


   public static ClusterMemberImpl clusterMember(final String testClusterName, final String host,
                                                 final int clusterMemberPort) throws UnknownHostException {

      final ClusterMemberAddress clusterMemberAddress = new ClusterMemberAddressImpl(InetAddress.getByName(host));

      final ArrayList<ClusterMemberAddress> clusterMemberAddresses = new ArrayList<ClusterMemberAddress>(1);
      clusterMemberAddresses.add(clusterMemberAddress);

      return new ClusterMemberImpl(testClusterName, clusterMemberAddresses, clusterMemberPort);
   }
}
