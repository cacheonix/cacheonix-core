/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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
import java.util.LinkedList;
import java.util.List;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * MarkerListRequest Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/08/2008</pre>
 */
public final class MarkerListRequestTest extends CacheonixTestCase {

   private MarkerListRequest message;

   private ClusterNodeAddress address;


   public MarkerListRequestTest(final String name) {

      super(name);
   }


   public void testGetRepresentative() {

      assertEquals(message.getClusterView().getRepresentative(), address);
   }


   public void testSetGetSender() {

      assertEquals(message.getSender(), address);
   }


   public void testToString() {

      assertNotNull(message.toString());
   }


   public void testSerialise() throws IOException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(message);
      final MarkerListRequest actual = (MarkerListRequest) serializer.deserialize(bytes);
      assertEquals(message, actual);
   }


   public void testGetMessageAssemblerParts() {

      assertEquals(1, message.getMessageAssemblerParts().size());
   }


   public void testDefaultConstructor() {

      assertEquals(Wireable.TYPE_CLUSTER_MARKER_LIST, new MarkerListRequest().getWireableType());
   }


   protected void setUp() throws Exception {

      super.setUp();

      address = TestUtils.createTestAddress(TestConstants.PORT_7676);

      final ClusterView clusterView = new ClusterViewImpl(UUID.randomUUID(), address);
      final ClusterView lastOperationalClusterView = new ClusterViewImpl(UUID.randomUUID(), address);

      final ReplicatedState replicatedState = new ReplicatedState();

      final List<Frame> messageAssemblerParts = new LinkedList<Frame>();
      final Frame frame = new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH);
      messageAssemblerParts.add(frame);

      message = new MarkerListRequest(address, clusterView, lastOperationalClusterView, replicatedState, messageAssemblerParts);
   }


   public String toString() {

      return "MarkerListRequestTest{" +
              "message=" + message +
              ", address=" + address +
              "} " + super.toString();
   }
}
