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
package org.cacheonix.impl.cluster.event;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.cacheonix.cluster.ClusterMemberAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import junit.framework.TestCase;

/**
 * Tester for {@link ClusterMemberImpl}.
 */
public final class ClusterMemberImplTest extends TestCase {


   private static final String CLUSTER_NAME = "TestClusterName";

   private static final String HOST = "1.1.1.1";

   private static final int PORT = 7777;

   private ClusterMemberImpl clusterMember;


   public void testGetClusterName() throws Exception {

      assertEquals(CLUSTER_NAME, clusterMember.getClusterName());
   }


   public void testGetClusterMemberAddresses() throws Exception {

      assertEquals(clusterMemberAddresses(HOST), clusterMember.getClusterMemberAddresses());

   }


   public void testWriteReadExternal() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(clusterMember, ser.deserialize(ser.serialize(clusterMember)));

   }


   public void testEquals() throws Exception {

      assertEquals(clusterMember, new ClusterMemberImpl(CLUSTER_NAME, clusterMemberAddresses(HOST), PORT));
   }


   public void testHashCode() throws Exception {

      assertTrue(clusterMember.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(clusterMember.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      clusterMember = new ClusterMemberImpl(CLUSTER_NAME, clusterMemberAddresses(HOST), PORT);
   }


   private static List<ClusterMemberAddress> clusterMemberAddresses(final String host) throws UnknownHostException {

      final ClusterMemberAddress clusterMemberAddress = new ClusterMemberAddressImpl(InetAddress.getByName(host));

      final List<ClusterMemberAddress> clusterMemberAddresses = new ArrayList<ClusterMemberAddress>(1);
      clusterMemberAddresses.add(clusterMemberAddress);
      return clusterMemberAddresses;
   }


   public void tearDown() throws Exception {

      clusterMember = null;

      super.tearDown();
   }
}
