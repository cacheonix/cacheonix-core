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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.AssertionException;

/**
 * MarkerListImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @noinspection ConstantNamingConvention, ResultOfObjectAllocationIgnored
 * @since <pre>04/01/2008</pre>
 */
public final class ClusterViewImplTest extends CacheonixTestCase {

   private static final ClusterNodeAddress PROCESS_ID_0 = TestUtils.createTestAddress(TestConstants.PORT_7676);

   private static final ClusterNodeAddress PROCESS_ID_1 = TestUtils.createTestAddress(TestConstants.PORT_7676 + 1);

   private static final ClusterNodeAddress PROCESS_ID_2 = TestUtils.createTestAddress(TestConstants.PORT_7676 + 2);

   private ClusterView clusterView;

   private List<JoiningNode> list;

   private static final ClusterNodeAddress REPRESENTATIVE = PROCESS_ID_0;


   public void testGetList() {

      final List<ClusterNodeAddress> clusterNodeList = new ArrayList<ClusterNodeAddress>(list.size());
      for (final JoiningNode joiningNode : list) {
         clusterNodeList.add(joiningNode.getAddress());
      }

      assertTrue(TestUtils.equals(clusterNodeList, clusterView.getClusterNodeList()));
   }


   public void testCreateEmpty() {

      try {
         new ClusterViewImpl(UUID.randomUUID(), PROCESS_ID_0, Collections.<JoiningNode>emptyList(), PROCESS_ID_0);
         fail("Expected exception but it was not thrown");
      } catch (final Exception ignored) { // NOPMD
      }
   }


   public void testCreateSingle() {

      assertNotNull(new ClusterViewImpl(UUID.randomUUID(), PROCESS_ID_0));
   }


   public void testGetRepresentative() {

      assertEquals(REPRESENTATIVE, clusterView.getRepresentative());
   }


   public void testRemove() {

      final int initial = clusterView.getSize();
      clusterView.remove(PROCESS_ID_1);
      assertEquals(initial - 1, clusterView.getSize());
   }


   public void testInsert() {

      final JoiningNode joiningNode = new JoiningNode(PROCESS_ID_2);
      clusterView.insert(PROCESS_ID_0, joiningNode.getAddress());
      assertEquals(PROCESS_ID_2, clusterView.getNextElement());
   }


   public void testIsRepresentative() {

      assertTrue(clusterView.isRepresentative());
   }


   public void testInsertAfter() {

      final JoiningNode joiningNode = new JoiningNode(PROCESS_ID_2);
      clusterView.insert(PROCESS_ID_0, joiningNode.getAddress());
      assertEquals(PROCESS_ID_2, clusterView.getNextElement());
   }


   public void testGetNextElement() {

      assertEquals(PROCESS_ID_1, clusterView.getNextElement());
      assertEquals(PROCESS_ID_1, clusterView.getNextElement(PROCESS_ID_0));
      assertEquals(PROCESS_ID_0, clusterView.getNextElement(PROCESS_ID_1));
      try {
         clusterView.getNextElement(PROCESS_ID_2);
         fail("Expected exception but it was not thrown");
      } catch (final AssertionException ignored) { // NOPMD
      }
   }


   public void testToString() {

      assertNotNull(clusterView.toString());
   }


   public void testSerializeDeserialize() throws IOException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(clusterView, ser.deserialize(ser.serialize(clusterView)));
   }


   public void testCalculateNodesLeft() {
      //
      assertEmpty("Nodes left should be empty when previous view is  null", clusterView.calculateNodesLeft(null));

      //
      final ArrayList<JoiningNode> nodeList = new ArrayList<JoiningNode>(3);
      nodeList.add(new JoiningNode(PROCESS_ID_1));
      nodeList.add(new JoiningNode(PROCESS_ID_2));
      final ClusterViewImpl previousClusterView = new ClusterViewImpl(UUID.randomUUID(), REPRESENTATIVE, nodeList,
              PROCESS_ID_1);
      final Set<ClusterNodeAddress> nodesLeft = clusterView.calculateNodesLeft(previousClusterView);
      assertEquals(1, nodesLeft.size());
      assertEquals(PROCESS_ID_2, nodesLeft.iterator().next());
   }


   public void testCalculateNodesJoined() {
      //
      assertEquals(clusterView.getSize(), clusterView.calculateNodesJoined(null).size());

      //
      final ArrayList<JoiningNode> nodeList = new ArrayList<JoiningNode>(3);
      nodeList.add(new JoiningNode(PROCESS_ID_0));
      final ClusterViewImpl previousClusterView = new ClusterViewImpl(UUID.randomUUID(), REPRESENTATIVE, nodeList,
              PROCESS_ID_0);
      final Collection<ClusterNodeAddress> nodesJoined = clusterView.calculateNodesJoined(previousClusterView);
      assertEquals(1, nodesJoined.size());
      assertEquals(PROCESS_ID_1, nodesJoined.iterator().next());
   }


   public void testConstructorPreservesOrder() {

      //
      // Prepare
      //
      final ArrayList<ClusterNodeAddress> addressList = new ArrayList<ClusterNodeAddress>(3);
      addressList.add(PROCESS_ID_0);
      addressList.add(PROCESS_ID_1);
      addressList.add(PROCESS_ID_2);

      final ArrayList<JoiningNode> nodeList = new ArrayList<JoiningNode>(3);
      for (final ClusterNodeAddress address : addressList) {

         nodeList.add(new JoiningNode(address));
      }

      //
      // Assert by trying every address as an owner
      //
      for (final ClusterNodeAddress owner : addressList) {

         final ClusterViewImpl view = new ClusterViewImpl(UUID.randomUUID(), REPRESENTATIVE, nodeList, owner);
         assertEquals(addressList, view.getClusterNodeList());
      }
   }


   protected void setUp() throws Exception {

      list = new ArrayList<JoiningNode>(3);
      list.add(new JoiningNode(PROCESS_ID_0));
      list.add(new JoiningNode(PROCESS_ID_1));
      clusterView = new ClusterViewImpl(UUID.randomUUID(), REPRESENTATIVE, list, PROCESS_ID_0);
      super.setUp();
   }


   public String toString() {

      return "ClusterViewImplTest{" +
              "clusterView=" + clusterView +
              ", list=" + list +
              "} " + super.toString();
   }
}
