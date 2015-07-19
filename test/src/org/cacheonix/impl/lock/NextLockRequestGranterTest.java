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
package org.cacheonix.impl.lock;

import java.net.InetAddress;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.configuration.ClusterConfiguration;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.multicast.sender.PlainMulticastSender;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.net.processor.UUID;

/**
 */
public final class NextLockRequestGranterTest extends CacheonixTestCase {


   private InetAddress mcastAddress;


   public void testToString() throws Exception {

      final LockQueue lockQueue = new LockQueue();
      final ClusterNodeAddress testAddress = TestUtils.createTestAddress(1);

      final PlainMulticastSender multicastSender = new PlainMulticastSender(mcastAddress,
              TestConstants.MULTICAST_PORT,
              TestConstants.MULTICAST_TTL);

      //
      final UUID initialClusterUUID = UUID.randomUUID();

      //
      final Router router = new Router(testAddress);
      router.setClusterUUID(initialClusterUUID);

      final ClusterProcessor clusterProcessor = new ClusterProcessor("Cacheonix", getClock(), getTimer(),
              router, multicastSender, testAddress, 1000L, 30000, 30000L,
              ClusterConfiguration.DEFAULT_CLUSTER_SURVEY_TIMEOUT_MILLS,
              ClusterConfiguration.DEFAULT_CLUSTER_ANNOUNCEMENT_TIMEOUT_MILLS, initialClusterUUID);
      final NextLockRequestGranter nextLockRequestGranter = new NextLockRequestGranter(clusterProcessor, lockQueue);
      assertNotNull(nextLockRequestGranter.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      mcastAddress = InetAddress.getByName(TestConstants.MULTICAST_ADDRESS);
   }
}
