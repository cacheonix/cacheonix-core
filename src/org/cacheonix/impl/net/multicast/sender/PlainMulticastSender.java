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
package org.cacheonix.impl.net.multicast.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Low-level multicast Frame sender that uses plain multicast media to send frames to a particular multicast group
 * defined by the multicast address and multicast port.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 28, 2008 4:04:18 PM
 */
public final class PlainMulticastSender implements MulticastSender {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PlainMulticastSender.class); // NOPMD

   /**
    * Send buffer size.
    */
   private static final int SEND_BUFFER_SIZE = 4096 * (Frame.MAXIMUM_MULTICAST_PACKET_SIZE + 1);

   public static final String NO_BUFFER_SPACE_AVAILABLE = "No buffer space available";

   /**
    * Multicast address.
    */
   private final InetAddress mcastAddress;

   /**
    * Multicast port.
    */
   private final int mcastPort;

   /**
    * Multicast sockets to send on, one per network interface.
    */
   private final MulticastSocket[] mcastSockets;

   /**
    * Sent messages.
    */
   private long sentMessages;


   /**
    * Constructs multicast message sender.
    *
    * @param router
    * @param mcastAddress multicast address
    * @param mcastPort    multicast port
    * @param mcastTTL     multicast TTL
    * @throws IOException if I/O error occurred while creating a multicast socket.
    */
   public PlainMulticastSender(final InetAddress mcastAddress, final int mcastPort,
           final int mcastTTL) throws IOException {

      this.mcastAddress = mcastAddress;
      this.mcastPort = mcastPort;
      this.mcastSockets = createSockets(mcastTTL);
   }


   /**
    * Creates an array of sockets with TTL and network interface set.
    *
    * @param mcastTTL multicast TTL.
    * @return an array of multicast sockets to broadcast on.
    * @throws IOException if I/O error occurred while creating a multicast socket.
    * @noinspection SocketOpenedButNotSafelyClosed, ConstantConditions
    */
   private static MulticastSocket[] createSockets(final int mcastTTL) throws IOException {

      Exception lastException = null; // Records last error in case we could not create any sockets
      final List<MulticastSocket> socketList = new ArrayList<MulticastSocket>(11);
      final Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
      while (enumeration.hasMoreElements()) {
         try {
            final NetworkInterface netIf = enumeration.nextElement();
            if (netIf.supportsMulticast()) {

               final MulticastSocket socket = new MulticastSocket(); // NOPMD
               socket.setTimeToLive(mcastTTL);
               socket.setNetworkInterface(netIf);
               socket.setSendBufferSize(SEND_BUFFER_SIZE);
               socketList.add(socket);
            }
         } catch (final Exception e) {

            lastException = e;
            ExceptionUtils.ignoreException(e, "continue to connect to those we can");
         }
      }
      if (socketList.isEmpty()) {
         throw new IOException("Could not create at least one multicast socket. Last error: " + lastException);
      }
      return socketList.toArray(new MulticastSocket[socketList.size()]);
   }


   public void sendFrame(final Frame frame) throws IOException {

      final byte[] message = toValidMessage(frame);
      final DatagramPacket packet = new DatagramPacket(message, 0, message.length, mcastAddress, mcastPort);
      for (final MulticastSocket mcastSocket : mcastSockets) {

         try {

            sentMessages++;
            mcastSocket.send(packet);
         } catch (final IOException e) {
            if (e.getMessage().endsWith(NO_BUFFER_SPACE_AVAILABLE)) {

               final NetworkInterface networkInterface = mcastSocket.getNetworkInterface();
               final InetAddress intf = mcastSocket.getInterface();
               LOG.warn(createNoBufferSpaceAvailableWarning(networkInterface, intf));
            } else {

               throw e;
            }
         }
      }
   }


   private static byte[] toValidMessage(final Frame frame) throws IOException {

      final byte[] message = frame.toBytes();
      if (message.length > Frame.MAXIMUM_MULTICAST_PACKET_SIZE) {
         throw new IOException("Message size " + message.length + " exceeds maximum allowed "
                 + Frame.MAXIMUM_MULTICAST_PACKET_SIZE);
      }
      return message;
   }


   /**
    * Creates a warning message about no buffer space available.
    *
    * @param intf        the interface this happened to
    * @param inetAddress the inet address it happened to.
    * @return a new warning message about no buffer space available.
    */
   private String createNoBufferSpaceAvailableWarning(final NetworkInterface intf, final InetAddress inetAddress) {

      return NO_BUFFER_SPACE_AVAILABLE + ": "
              + "interface: " + inetAddress + ", "
              + "network interface: " + intf.getDisplayName() + ", "
              + "total messages sent: " + sentMessages;
   }


   public String toString() {

      return "MulticastSender{" +
              "mcastAddress=" + mcastAddress +
              ", mcastPort=" + mcastPort +
              '}';
   }
}
