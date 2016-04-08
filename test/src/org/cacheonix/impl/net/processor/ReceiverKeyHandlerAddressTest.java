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
package org.cacheonix.impl.net.processor;

import java.net.InetAddress;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * A tester for ReceiverAddress.
 */
public class ReceiverKeyHandlerAddressTest extends CacheonixTestCase {


   private ClusterNodeAddress clusterNodeAddress;

   private ReceiverAddress receiverAddress;

   private InetAddress[] addresses;

   private int tcpPort;


   public void testGetTcpPort() throws Exception {

      assertEquals(tcpPort, receiverAddress.getTcpPort());
   }


   public void testGetAddresses() throws Exception {

      assertEquals(addresses, receiverAddress.getAddresses());

   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(receiverAddress, ser.deserialize(ser.serialize(receiverAddress)));
   }


   public void testGetWireableType() throws Exception {

      assertEquals(Wireable.TYPE_RECEIVER_ADDRESS, receiverAddress.getWireableType());
   }


   public void testEquals() throws Exception {

      assertEquals(new ReceiverAddress(addresses, tcpPort), receiverAddress);
   }


   public void testHashCode() throws Exception {

      assertTrue(receiverAddress.hashCode() != 0);
   }


   public void testIsAddressOf() throws Exception {

      assertTrue(receiverAddress.isAddressOf(clusterNodeAddress));
   }


   public void testToString() throws Exception {

      assertNotNull(receiverAddress.toString());
      assertNotNull(new ReceiverAddress());
   }


   public void setUp() throws Exception {

      super.setUp();

      clusterNodeAddress = TestUtils.createTestAddress();
      tcpPort = clusterNodeAddress.getTcpPort();
      addresses = clusterNodeAddress.getAddresses();
      receiverAddress = new ReceiverAddress(addresses, tcpPort);
   }


   public void tearDown() throws Exception {

      receiverAddress = null;
      clusterNodeAddress = null;

      super.tearDown();
   }
}
