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

import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import junit.framework.TestCase;

/**
 * ClusterAnnouncement Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/01/2008</pre>
 */
public final class ClusterAnnouncementTest extends TestCase {

   private static final String TEST_CLUSTER_NAME = "Test Cluster Name";

   private static final int MARKER_LIST_SIZE = 2;

   private static final int PORT = TestConstants.PORT;

   private ClusterAnnouncement clusterAnnouncement;

   private ClusterNodeAddress process;

   private ClusterNodeAddress representative;

   private UUID clusterUUID = null;


   public ClusterAnnouncementTest(final String name) {

      super(name);
   }


   public void testGetMember() throws Exception {

      assertEquals(process, clusterAnnouncement.getSender());
   }


   public void testGetRepresentative() {

      assertEquals(representative, clusterAnnouncement.getRepresentative());
   }


   public void testGetMarkerListSize() {

      assertEquals(MARKER_LIST_SIZE, clusterAnnouncement.getMarkerListSize());
   }


   public void testIsMajority() throws Exception {

      assertTrue(clusterAnnouncement.isOperationalCluster());
   }


   public void testToString() {

      assertNotNull(clusterAnnouncement.toString());
   }


   public void getClusterUUID() {

      assertNotNull(clusterAnnouncement.getClusterUUID());
   }


   public void testSerialize() throws IOException, ClassNotFoundException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(clusterAnnouncement);
      assertTrue("Sterilized length must be smaller than mcast frame size for it is always sent " +
              "as a single frame", bytes.length <= Frame.MAXIMUM_MCAST_PAYLOAD_LENGTH);
      final ClusterAnnouncement actual = (ClusterAnnouncement) serializer.deserialize(bytes);
      assertEquals(clusterAnnouncement, actual);
      assertEquals(clusterUUID, actual.getClusterUUID());
   }


   public void testHashCode() {

      assertTrue(clusterAnnouncement.hashCode() != 0);
   }


   public void testDefaultConstructor() {

      assertEquals(new ClusterAnnouncement().getWireableType(), Wireable.TYPE_CLUSTER_ANNOUNCEMENT);
   }


   protected void setUp() throws Exception {

      super.setUp();
      process = TestUtils.createTestAddress(PORT);
      representative = TestUtils.createTestAddress(PORT + 1);
      clusterAnnouncement = new ClusterAnnouncement(TEST_CLUSTER_NAME, process, true, MARKER_LIST_SIZE, representative);
      clusterUUID = UUID.randomUUID();
      clusterAnnouncement.setClusterUUID(clusterUUID);
   }


   public String toString() {

      return "ClusterAnnouncementTest{" +
              "clusterAnnouncement=" + clusterAnnouncement +
              ", process=" + process +
              ", representative=" + representative +
              "} " + super.toString();
   }
}
