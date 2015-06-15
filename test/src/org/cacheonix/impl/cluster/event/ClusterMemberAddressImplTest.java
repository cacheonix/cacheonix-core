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

import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import junit.framework.TestCase;

/**
 * Tester for {@link ClusterMemberAddressImpl}.
 */
public final class ClusterMemberAddressImplTest extends TestCase {


   private InetAddress inetAddress;

   private ClusterMemberAddressImpl clusterMemberAddress;


   public void testGetInetAddress() throws Exception {

      assertEquals(inetAddress, clusterMemberAddress.getInetAddress());
   }


   public void testWriteReadExternal() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(clusterMemberAddress, ser.deserialize(ser.serialize(clusterMemberAddress)));

   }


   public void testEquals() throws Exception {

      assertEquals(clusterMemberAddress, new ClusterMemberAddressImpl(inetAddress));
   }


   public void testHashCode() throws Exception {

      assertTrue(clusterMemberAddress.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(clusterMemberAddress.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      inetAddress = InetAddress.getByName("1.1.1.1");
      clusterMemberAddress = new ClusterMemberAddressImpl(inetAddress);
   }


   public void tearDown() throws Exception {

      clusterMemberAddress = null;
      inetAddress = null;

      super.tearDown();
   }


   public String toString() {

      return "ClusterMemberAddressImplTest{" +
              "inetAddress=" + inetAddress +
              ", clusterMemberAddress=" + clusterMemberAddress +
              "} " + super.toString();
   }
}
