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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.DaemonThreadFactory;

/**
 * Multicast server that receives multicast packets.
 */
public final class MulticastServerImpl implements Runnable, MulticastServer {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MulticastServerImpl.class); // NOPMD

   /**
    * Thread factory name.
    */
   private static final String MCAST_SERVER = "McastServer";

   /**
    * A packet size guard.
    */
   private static final int MAXIMUM_MULTICAST_PACKET_SIZE_PLUS_ONE = Frame.MAXIMUM_MULTICAST_PACKET_SIZE + 1;

   /**
    * Receive buffer size.
    */
   private static final int RECEIVE_BUFFER_SIZE = 128 * MAXIMUM_MULTICAST_PACKET_SIZE_PLUS_ONE;

   /**
    * Multicast address this server accepts requests at.
    */
   private final InetAddress multicastAddress;

   /**
    * Multicast port this server accepts requests at
    */
   private final int multicastPort;

   /**
    * Socket the multicast server is listening on.
    */
   private volatile MulticastSocket multicastSocket = null;


   /**
    * True if the server has been shutdown.
    */
   private volatile boolean shutdown = false;

   /**
    * True if the server has been started.
    */
   private volatile boolean started = false;

   /**
    * A list of {@link MulticastServerListener} objects.
    */
   private final List<MulticastServerListener> listeners = new CopyOnWriteArrayList<MulticastServerListener>();

   /**
    * A descriptive thread factory name that allows to recognize the multicast server threads in a log output and thread
    * dumps.
    */
   private final String threadFactoryName;

   /**
    * String address and port.
    */
   private final String addressAndPort;


   /**
    * Constructor.
    *
    * @param multicastAddress multicast address this server accepts requests at
    * @param multicastPort    multicast port this server accepts requests at
    *                         <p/>
    *                         A value of zero indicates that a message should not be forwarded out of the current host.
    *                         This value should be used in development mode to avoid unintentional communications with
    *                         cluster nodes running on other developer's machines.
    *                         <p/>
    *                         A value of one means that the message is not forwarded out of the current subnet. This
    *                         value is suitable for QA and for most production environments.
    *                         <p/>
    *                         Values of two or greater mean larger, scopes as shown in the table below:
    *                         <p/>
    *                         <table> <tr> <td>Multicast TTL</td> <td>Scope</td> </tr> <tr> <td>0</td>
    *                         <td>Node-local</td> </tr> <tr> <td>1</td> <td>Link-local</td> </tr> <tr> <td>Lesser than
    *                         32</td> <td>Site-local</td> </tr> <tr> <td>Lesser than 64</td> <td>Region-local</td> </tr>
    *                         <tr> <td>Lesser than 128</td> <td>Continent-local</td> </tr> <tr> <td>Lesser than 255</td>
    *                         <td>Global</td> </tr> </table>
    * @throws IllegalArgumentException if the multicast address is invalid
    */
   MulticastServerImpl(final String multicastAddress, final int multicastPort)
           throws IllegalArgumentException {

      this.multicastAddress = validateMulticastAddress(multicastAddress);
      this.multicastPort = multicastPort;
      this.threadFactoryName = MCAST_SERVER;
      this.addressAndPort = createAddressAndPort(this.multicastAddress, this.multicastPort);
   }


   /**
    * Constructor.
    *
    * @param multicastAddress
    * @param multicastPort
    * @param tcpPort
    * @throws IllegalArgumentException if not a multicast address.
    */
   public MulticastServerImpl(final InetAddress multicastAddress, final int multicastPort, final int tcpPort) {

      this.multicastAddress = IOUtils.validateMulticastAddress(multicastAddress);
      this.multicastPort = multicastPort;
      this.threadFactoryName = MCAST_SERVER + ':' + tcpPort;
      this.addressAndPort = createAddressAndPort(multicastAddress, multicastPort);
   }


   /**
    * @return multicast address this server accepts requests at.
    */
   public InetAddress getMulticastAddress() {

      return multicastAddress;
   }


   /**
    * @return multicast port this server accepts requests at
    */
   public int getMulticastPort() {

      return multicastPort;
   }


   /**
    * Adds a listener for arriving multicast messages.
    *
    * @param listener to add.
    */
   public void addListener(final MulticastServerListener listener) {

      listeners.add(listener);
   }


   /**
    * Starts up the server. The server that has started accepts and handles multicast datagram packets.
    *
    * @throws IllegalStateException if the server has already started.
    * @throws IOException           if IOException occurred at startup
    * @noinspection OverlyBroadCatchBlock, ThrowCaughtLocally, SocketOpenedButNotSafelyClosed
    */
   public void startup() throws IOException {

      assureNotStarted();

      // Issue an information notification.
      informServerStarting();

      try {

         // Initialize socket
         multicastSocket = new MulticastSocket(multicastPort);
         multicastSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
         multicastSocket.joinGroup(multicastAddress);

         // Notify that started
         informServerStarted();

         // Start listener thread
         final Thread listenerThread = new DaemonThreadFactory(threadFactoryName).newThread(this);
         listenerThread.start();

         started = true;
      } catch (final IOException e) {

         IOUtils.closeHard(multicastSocket);

         throw e;
      }
   }


   /**
    * @noinspection OverlyBroadCatchBlock, ConstantConditions
    */
   public void run() {

      while (!shutdown) {

         try {

            // Wait for packet
            final byte[] buffer = new byte[MAXIMUM_MULTICAST_PACKET_SIZE_PLUS_ONE];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            multicastSocket.receive(packet);

            // Notify listeners
            notifyListeners(packet);
         } catch (final Exception e) {

            if ("socket closed".equalsIgnoreCase(e.getMessage())) {

               // No point to send to ignore - part of shutdown
               return;
            }
            ExceptionUtils.ignoreException(e, "Nothing we can do");
         }
      }
   }


   /**
    * Shuts down the server. The server that has been shutdown does not accept and does not handle multicast datagram
    * packets.
    *
    * @throws IllegalStateException if the server has already been shutdown.
    * @noinspection OverlyBroadCatchBlock
    */
   public void shutdown() {

      assureNotShutdown();

      // Mark as shutdown
      shutdown = true;

      try {

         if (multicastSocket != null) {

            multicastSocket.leaveGroup(multicastAddress);
            multicastSocket.close();
         }
      } catch (final IOException e) {

         ExceptionUtils.ignoreException(e, "Shutdown procedure, nothing we can do");
      } finally {

         IOUtils.closeHard(multicastSocket);
      }

      LOG.info("Multicast server has been shutdown: " + addressAndPort());
   }


   /**
    * @return <code>true</code> if the server was shutdown.
    */
   public boolean isShutdown() {

      return shutdown;
   }


   /**
    * Notifies listeners about receiving the packet.
    *
    * @param datagramPacket packet to notify listeners about
    */
   private void notifyListeners(final DatagramPacket datagramPacket) {

      // Make sure listeners are there
      if (listeners.isEmpty()) {

         return;
      }

      // Make sure this is a valid packet
      if (datagramPacket.getLength() >= MAXIMUM_MULTICAST_PACKET_SIZE_PLUS_ONE) {

         return;
      }

      // Send to listeners
      try {

         // Get frame out of the packet
         final Frame mcastFrame = Frame.fromBytes(datagramPacket.getData(),
                 datagramPacket.getOffset(), datagramPacket.getLength());

         // Set sender address
         mcastFrame.setSenderInetAddress(datagramPacket.getAddress());

         // Notify listeners
         for (final MulticastServerListener listener : listeners) {

            try {

               listener.receiveFrame(mcastFrame);
            } catch (final Exception e) {

               ExceptionUtils.ignoreException(e, "Ignored to let other listeners to process");
            }
         }
      } catch (final IOException e) {

         ExceptionUtils.ignoreException(e, "Bad packet");
      }
   }


   /**
    * Helper method.
    */
   private void assureNotStarted() {

      if (started) {

         throw new IllegalStateException("This multicast server has already been started: " + addressAndPort());
      }
   }


   /**
    * Helper method.
    */
   private void assureNotShutdown() {

      if (shutdown) {

         throw new IllegalStateException("This multicast server has already been shutdown");
      }
   }


   /**
    * Helper method.
    *
    * @return annotated string with address and port.
    */
   private String addressAndPort() {

      return addressAndPort;
   }


   /**
    * Creates a descriptive string containing a multicast address and a port.
    *
    * @param multicastAddress the multicast address.
    * @param multicastPort    the multicast port.
    * @return the descriptive string containing a multicast address and a port
    */
   private static String createAddressAndPort(final InetAddress multicastAddress, final int multicastPort) {

      return multicastAddress.getHostAddress() + ':' + multicastPort;
   }


   /**
    * Helper method to validate a multicast address.
    *
    * @param address to validate.
    * @return valid multicast address.
    * @throws IllegalArgumentException if the address is invalid
    */
   private static InetAddress validateMulticastAddress(final String address)
           throws IllegalArgumentException {

      try {

         return IOUtils.validateMulticastAddress(InetAddress.getByName(address));
      } catch (final UnknownHostException e) {

         throw ExceptionUtils.createIllegalArgumentException(e);
      }
   }


   /**
    * Prints an informational message that the server started.
    */
   private void informServerStarted() {

      String bufferSize;
      try {

         bufferSize = Integer.toString(multicastSocket.getReceiveBufferSize());
      } catch (final SocketException e) {

         bufferSize = "Cannot identify - " + StringUtils.toString(e);
      }
      LOG.info("Started multicast server, address: " + addressAndPort()
              + ", receive buffer size: " + bufferSize);
   }


   /**
    * Prints an informational message that the server is starting.
    */
   private void informServerStarting() {

      LOG.info("Starting multicast server, " + addressAndPort());
   }


   /**
    * @noinspection ObjectToString, ArithmeticOnVolatileField
    */
   public String toString() {

      return "MulticastServerImpl{" +
              ", multicastAddress='" + multicastAddress + '\'' +
              ", multicastPort=" + multicastPort +
              ", multicastSocket=" + multicastSocket +
              ", shutdown=" + shutdown +
              ", started=" + started +
              '}';
   }
}
