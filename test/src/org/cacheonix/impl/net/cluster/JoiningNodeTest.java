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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * A tester for JoiningNode.
 */
public class JoiningNodeTest extends CacheonixTestCase {


   private JoiningNode node;

   private ClusterNodeAddress address;


   public void testGetAddress() throws Exception {

      assertEquals(address, node.getAddress());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(node, ser.deserialize(ser.serialize(node)));
   }


   public void testGetWireableType() throws Exception {

      assertEquals(Wireable.TYPE_JOINING_NODE, node.getWireableType());
   }


   public void testEquals() throws Exception {

      assertEquals(node, new JoiningNode(address));
   }


   public void testHashCode() throws Exception {

      assertTrue(node.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(node.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      address = TestUtils.createTestAddress(1);
      node = new JoiningNode(address);
   }


   public void tearDown() throws Exception {

      node = null;
      address = null;

      super.tearDown();
   }


   public String toString() {

      return "JoiningNodeTest{" +
              "node=" + node +
              ", address=" + address +
              "} " + super.toString();
   }
}
