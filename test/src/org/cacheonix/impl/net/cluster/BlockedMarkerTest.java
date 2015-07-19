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
import org.cacheonix.TestUtils;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * BlockedMarker Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>04/17/2008</pre>
 */
public final class BlockedMarkerTest extends CacheonixTestCase {

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

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();

      final JoiningNode joiningNode = new JoiningNode(clusterNodeAddress);
      blockedMarker.setJoiningNode(joiningNode);
      assertEquals(joiningNode, blockedMarker.getJoiningNode());
   }


   public void testSetGetLeave() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      blockedMarker.setLeave(clusterNodeAddress);
      assertEquals(clusterNodeAddress, blockedMarker.getLeave());
   }


   public void testSetGetPredecessor() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      blockedMarker.setPredecessor(clusterNodeAddress);
      assertEquals(clusterNodeAddress, blockedMarker.getPredecessor());
   }


   public void testSetGetTargetMajorityMarkerListSize() throws Exception {

      blockedMarker.setTargetMajorityMarkerListSize(222);
      assertEquals(222, blockedMarker.getTargetMajorityMarkerListSize());
   }


   public void testToString() {

      assertNotNull(blockedMarker.toString());
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(blockedMarker);
      assertEquals(blockedMarker, serializer.deserialize(bytes));
   }


   public void testDefaultConstructor() {

      assertNotNull(new BlockedMarker().toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      blockedMarker = new BlockedMarker(UUID.randomUUID());
   }


   public String toString() {

      return "BlockedMarkerTest{" +
              "marker=" + blockedMarker +
              "} " + super.toString();
   }
}
