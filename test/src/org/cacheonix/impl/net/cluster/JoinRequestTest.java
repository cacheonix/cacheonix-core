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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * JoinRequest Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/11/2008</pre>
 */
public final class JoinRequestTest extends CacheonixTestCase {

   private ClusterNodeAddress clusterNodeAddress;

   private JoinRequest request;


   public void testToString() {

      assertNotNull(request.toString());
   }


   public void testHashCode() {

      assertTrue(request.hashCode() != 0);
   }


   public void testSerializeDeserialize() throws IOException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
   }


   public void testDefaultConstructor() {

      assertNotNull(new JoinRequest().toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      clusterNodeAddress = TestUtils.createTestAddress();
      request = new JoinRequest(clusterNodeAddress);
   }


   public String toString() {

      return "JoinRequestTest{" +
              "clusterNodeAddress=" + clusterNodeAddress +
              ", joinRequest=" + request +
              "} " + super.toString();
   }
}
