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

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

import static org.cacheonix.TestConstants.PORT_7676;
import static org.cacheonix.TestConstants.PORT_7677;
import static org.cacheonix.TestConstants.PORT_7678;
import static org.cacheonix.TestUtils.createTestAddress;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_BLOCKED;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_RECOVERY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BlockedMarker Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>04/17/2008</pre>
 */
public final class BlockedMarkerTest extends CacheonixTestCase {

   public static final int TARGET_MAJORITY_CLUSTER_SIZE = 2;

   public static final UUID CLUSTER_UUID = UUID.randomUUID();

   public static final ClusterNodeAddress ADDRESS_7676 = createTestAddress(PORT_7676);

   public static final ClusterNodeAddress ADDRESS_7677 = createTestAddress(PORT_7677);

   public static final ClusterNodeAddress ADDRESS_7678 = createTestAddress(PORT_7678);

   private BlockedMarker blockedMarker;


   public void testSetGetNextAnnouncementTimeout() throws Exception {

      final Time nextAnnouncementTime = getClock().currentTime();
      blockedMarker.setNextAnnouncementTime(nextAnnouncementTime);
      assertEquals(nextAnnouncementTime, blockedMarker.getNextAnnouncementTime());
   }


   public void testDefaultIsResponseRequired() {

      assertTrue(blockedMarker.isResponseRequired());
   }


   public void testSetGetJoin() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = createTestAddress();

      final JoiningNode joiningNode = new JoiningNode(clusterNodeAddress);
      blockedMarker.setJoiningNode(joiningNode);
      assertEquals(joiningNode, blockedMarker.getJoiningNode());
   }


   public void testSetGetLeave() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = createTestAddress();
      blockedMarker.setLeave(clusterNodeAddress);
      assertEquals(clusterNodeAddress, blockedMarker.getLeave());
   }


   public void testSetGetPredecessor() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = createTestAddress();
      blockedMarker.setPredecessor(clusterNodeAddress);
      assertEquals(clusterNodeAddress, blockedMarker.getPredecessor());
   }


   public void testSetGetTargetMajorityMarkerListSize() throws Exception {

      blockedMarker.setTargetMajorityClusterSize(TARGET_MAJORITY_CLUSTER_SIZE);
      assertEquals(TARGET_MAJORITY_CLUSTER_SIZE, blockedMarker.getTargetMajorityClusterSize());
   }


   /**
    * Tests that a blocked cluster enters recovery when a node joins a single-node cluster.
    *
    * @throws InterruptedException
    */
   public void testEnterRecoveryByReachingMajorityClusterSize() throws InterruptedException {

      // The initial state is a single node with address ADDRESS_7676.
      // The node with the ADDRESS_7677 is joining.
      // This is the first marker received from the joined node.


      //
      final JoinStatus joinStatus = mock(JoinStatus.class);

      //
      final ClusterView clusterView = new ClusterViewImpl(CLUSTER_UUID, ADDRESS_7676);

      //
      final ClusterProcessorState clusterProcessorState = mock(ClusterProcessorState.class);
      when(clusterProcessorState.getClusterView()).thenReturn(clusterView);
      when(clusterProcessorState.getJoinStatus()).thenReturn(joinStatus);
      when(clusterProcessorState.getState()).thenReturn(STATE_BLOCKED);

      //
      final Time time = mock(Time.class);

      //
      final Clock clock = mock(Clock.class);
      when(clock.currentTime()).thenReturn(time);

      //
      final ClusterProcessor clusterProcessor = mock(ClusterProcessor.class);
      when(clusterProcessor.getProcessorState()).thenReturn(clusterProcessorState);
      when(clusterProcessor.getAddress()).thenReturn(ADDRESS_7676);
      when(clusterProcessor.getClock()).thenReturn(clock);

      blockedMarker.setReceiver(ADDRESS_7676);
      blockedMarker.setSender(ADDRESS_7677);
      blockedMarker.setProcessor(clusterProcessor);
      blockedMarker.execute();
      verify(clusterProcessorState).setState(STATE_RECOVERY);
   }


   public void testToString() {

      assertNotNull(blockedMarker.toString());
   }


   public void testSerializeDeserialize() throws IOException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(blockedMarker);
      assertEquals(blockedMarker, serializer.deserialize(bytes));
   }


   public void testDefaultConstructor() {

      assertNotNull(new BlockedMarker().toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      blockedMarker = new BlockedMarker(CLUSTER_UUID);
   }


   public String toString() {

      return "BlockedMarkerTest{" +
              "marker=" + blockedMarker +
              "} " + super.toString();
   }
}
