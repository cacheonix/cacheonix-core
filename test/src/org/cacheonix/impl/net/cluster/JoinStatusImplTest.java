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
package org.cacheonix.impl.net.cluster;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.configuration.ClusterConfiguration;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.UUID;

import static org.cacheonix.impl.configuration.ConfigurationConstants.DEFAULT_JOIN_TIMEOUT_MILLIS;

/**
 * JoinStatus Tester.
 *
 * @author simeshev@cacheonix.org
 * @since <pre>04/26/2008</pre>
 */
public final class JoinStatusImplTest extends CacheonixTestCase {

   private JoinStatusImpl joinStatus = null;


   public void testSetGetJoiningTo() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      joinStatus.setJoiningTo(clusterNodeAddress);
      assertEquals(clusterNodeAddress, joinStatus.getJoiningToProcess());
   }


   public void testSetGetJoiningToMarkerList() throws Exception {

      final ClusterView clusterView = createClusterView();
      joinStatus.setJoiningToCluster(clusterView);
      assertEquals(clusterView, joinStatus.getJoiningToCluster());
   }


   public void testSetGetReplicatedState() {

      final ReplicatedState replicatedState = new ReplicatedState();
      joinStatus.setReplicatedState(replicatedState);
      assertEquals(replicatedState, joinStatus.getReplicatedState());
   }


   public void testGetTimeout() throws Exception {

      assertEquals(DEFAULT_JOIN_TIMEOUT_MILLIS, joinStatus.getTimeout().getDuration());
   }


   public void testIsJoining() {

      assertFalse(joinStatus.isJoining());
      joinStatus.setJoiningTo(TestUtils.createTestAddress());
      assertTrue(joinStatus.isJoining());
   }


   public void testIsReceivedMarkerList() {

      assertFalse(joinStatus.isReceivedMarkerList());
      joinStatus.setJoiningToCluster(createClusterView());
      assertTrue(joinStatus.isReceivedMarkerList());
   }


   public void testToString() {

      assertNotNull(joinStatus.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      joinStatus = new JoinStatusImpl(ClusterConfiguration.DEFAULT_CLUSTER_SURVEY_TIMEOUT_MILLS);
   }


   private static ClusterView createClusterView() {

      final ClusterNodeAddress proc0 = TestUtils.createTestAddress(TestConstants.PORT_7676);
      final ClusterNodeAddress proc1 = TestUtils.createTestAddress(TestConstants.PORT_7676 + 1);
      final ClusterNodeAddress proc2 = TestUtils.createTestAddress(TestConstants.PORT_7676 + 2);
      final List<JoiningNode> list = new ArrayList<JoiningNode>(3);
      list.add(new JoiningNode(proc0));
      list.add(new JoiningNode(proc1));
      list.add(new JoiningNode(proc2));
      return new ClusterView(UUID.randomUUID(), proc1, list, proc2);
   }


   public String toString() {

      return "JoinStatusImplTest{" +
              "joinStatus=" + joinStatus +
              "} " + super.toString();
   }
}
