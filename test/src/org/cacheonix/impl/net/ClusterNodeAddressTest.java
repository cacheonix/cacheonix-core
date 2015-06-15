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
package org.cacheonix.impl.net;

import java.io.IOException;
import java.net.InetAddress;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Tests {@link ClusterNodeAddress}
 */
public final class ClusterNodeAddressTest extends TestCase {

   private static final String IP_ADDRESS_AS_STRING = "127.0.0.1";

   private static final InetAddress IP_ADDRESS = IOUtils.getInetAddress(IP_ADDRESS_AS_STRING);

   private static final int TCP_PORT = 9999;

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterNodeAddressTest.class); // NOPMD

   private static final int NUMBER_5 = 5;

   private static final int NUMBER_6 = 6;

   private static final String TEST_HOST_NAME = "test_host_name";

   private static final ClusterNodeAddress DIFFERENT_1 = new ClusterNodeAddress(TCP_PORT, TEST_HOST_NAME, (int) NUMBER_6, new InetAddress[]{IP_ADDRESS});

   private static final ClusterNodeAddress SAME_1 = new ClusterNodeAddress(TCP_PORT, TEST_HOST_NAME, (int) NUMBER_5, new InetAddress[]{IP_ADDRESS});

   private static final ClusterNodeAddress SAME_2 = new ClusterNodeAddress(TCP_PORT, TEST_HOST_NAME, (int) NUMBER_5, new InetAddress[]{IP_ADDRESS});


   public void testCompare() {

      assertEquals(SAME_1, SAME_2);
      assertEquals(0, SAME_1.compareTo(SAME_2));
      assertEquals(-1, SAME_1.compareTo(DIFFERENT_1));
   }


   public void testGetAddress() {

      CacheonixTestCase.assertEquals(new InetAddress[]{IP_ADDRESS}, SAME_1.getAddresses());
   }


   public void testGetNumber() {

      assertEquals(NUMBER_5, SAME_1.getNumber());
   }


   public void testGetPort() {

      assertEquals(TCP_PORT, SAME_1.getTcpPort());
   }


   public void testHashCode() {

      assertEquals(-1417435877, SAME_1.hashCode());
   }


   public void testEquals() {

      assertEquals(SAME_1, SAME_2);
   }


   public void testHostName() {

      assertEquals(TEST_HOST_NAME, SAME_1.getHostName());
   }


   public void testToString() {

      assertNotNull(SAME_1.toString());
   }


   public void testCreateProcessID() throws IOException {

      final ClusterNodeAddress clusterNodeAddress = ClusterNodeAddress.createAddress(IP_ADDRESS_AS_STRING, TCP_PORT);
      assertNotNull(clusterNodeAddress);
   }


   public void testCreateProcessIDWithEmptyListenAddress() throws IOException {

      final ClusterNodeAddress clusterNodeAddress = ClusterNodeAddress.createAddress("", TCP_PORT);
      assertNotNull(clusterNodeAddress);
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      final ClusterNodeAddress clusterNodeAddress = ClusterNodeAddress.createAddress(null, TCP_PORT);
      final SerializerFactory instance = SerializerFactory.getInstance();
      final Serializer serializer = instance.getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(clusterNodeAddress);
      assertNotNull(bytes);
      assertEquals(clusterNodeAddress, serializer.deserialize(bytes));
   }
}
