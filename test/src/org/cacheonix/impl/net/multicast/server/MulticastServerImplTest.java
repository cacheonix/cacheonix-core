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
package org.cacheonix.impl.net.multicast.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.cacheonix.TestConstants;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import junit.framework.TestCase;

/**
 */
public final class MulticastServerImplTest extends TestCase {

   private static final String MULTICAST_ADDRESS = TestConstants.MULTICAST_ADDRESS;

   private static final int MULTICAST_PORT = TestConstants.MULTICAST_PORT;


   private MulticastServerImpl server = null;


   public void testToString() {

      assertNotNull(server.toString());
   }


   public void testGetMulticastAddress() {

      assertEquals(MULTICAST_ADDRESS, StringUtils.toString(server.getMulticastAddress()));
   }


   public void testGetMulticastPort() {

      assertEquals(MULTICAST_PORT, server.getMulticastPort());
   }


   /**
    * @noinspection SocketOpenedButNotSafelyClosed
    */
   public void testStartupAndShutdown() throws IOException {
      // Establish connection
      DatagramSocket socket = null;
      try {
         server.startup();
         final byte[] data = new byte[1];
         final DatagramPacket packet = new DatagramPacket(data, data.length,
                 InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT);
         socket = new DatagramSocket();
         socket.send(packet);
      } finally {
         final InputStream inputStream = null;
         IOUtils.closeHard(inputStream);
         IOUtils.closeHard(socket);
         server.shutdown();
         assertTrue(server.isShutdown());
      }
   }


   public void testStartupDoesntBindToInvalidMulticastAddress() {

      try {
         server = new MulticastServerImpl("1.2.3.4", MULTICAST_PORT);
         fail("Expected exception but it was not thrown");
      } catch (final IllegalArgumentException e) {
         ExceptionUtils.ignoreException(e, "expected");
      } finally {
         server.shutdown();
         assertTrue(server.isShutdown());
      }
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      server = new MulticastServerImpl(MULTICAST_ADDRESS, MULTICAST_PORT);
   }


   public String toString() {

      return "MulticastServerImplTest{" +
              "server=" + server +
              '}';
   }
}
