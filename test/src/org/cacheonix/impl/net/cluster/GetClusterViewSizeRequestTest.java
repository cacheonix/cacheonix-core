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

import java.io.IOException;

import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import junit.framework.TestCase;

/**
 * GetClusterViewSizeRequest Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/08/2008</pre>
 */
public final class GetClusterViewSizeRequestTest extends TestCase {

   private ClusterNodeAddress address;


   private GetClusterViewSizeRequest message;


   public GetClusterViewSizeRequestTest(final String name) {

      super(name);
   }


   public void testSetGetSender() throws Exception {

      assertEquals(message.getSender(), address);
   }


   public void testSetGetReceiver() throws Exception {

      assertTrue(message.getReceiver().isAddressOf(address));
   }


   public void testToString() {

      assertNotNull(message.toString());
   }


   public void testSerialise() throws IOException, ClassNotFoundException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(message);
      final GetClusterViewSizeRequest actual = (GetClusterViewSizeRequest) serializer.deserialize(bytes);
      assertEquals(message, actual);
   }


   public void testDefaultConstructor() {

      final GetClusterViewSizeRequest defaultConstructed = new GetClusterViewSizeRequest();
      assertEquals(Wireable.TYPE_CLUSTER_GET_CLUSTER_VIEW_SIZE, defaultConstructed.getWireableType());
      assertNotNull(defaultConstructed.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      address = TestUtils.createTestAddress(TestConstants.PORT);

      message = new GetClusterViewSizeRequest();
      message.setSender(address);
      message.setReceiver(address);
   }


   public String toString() {

      return "GetClusterViewSizeRequestTest{" +
              "address=" + address +
              ", message=" + message +
              "} " + super.toString();
   }
}
