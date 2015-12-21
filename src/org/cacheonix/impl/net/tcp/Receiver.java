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
package org.cacheonix.impl.net.tcp;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.Shutdownable;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.exception.StackTraceAtCreate;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.UserThreadFactory;

import static org.cacheonix.impl.util.IOUtils.closeHard;
import static org.cacheonix.impl.util.exception.ExceptionUtils.ignoreException;
import static org.cacheonix.impl.util.thread.ThreadUtils.interruptAndJoin;

/**
 * TCP server handles incoming TCP requests for cache configurations grouped in a cluster.
 *
 * @noinspection ConstantConditions, ProhibitedExceptionDeclared
 */
public final class Receiver implements Shutdownable {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Receiver.class); // NOPMD

   /**
    * Stack trace at create.
    */
   @SuppressWarnings("ThrowableInstanceNeverThrown")
   private final StackTraceAtCreate stackTraceAtCreate = new StackTraceAtCreate();

   private static final int OPERATIONAL_STATUS_NOT_STARTED = 0;

   private static final int OPERATIONAL_STATUS_SHUTDOWN = 1;

   private static final int OPERATIONAL_STATUS_STARTED = 2;

   /**
    * Operational status controls startup and shutdown preconditions.
    */
   private final AtomicInteger operationalStatus = new AtomicInteger(OPERATIONAL_STATUS_NOT_STARTED);


   /**
    * Host and port.
    */
   private final InetSocketAddress endpoint;


   /**
    * Server socket channel. This was made a field support closing at shutdown.
    */
   private final ServerSocketChannel serverSocketChannel;

   /**
    * A selector responsible for selecting keys.
    */
   private final Selector selector;

   /**
    * A thread that processes selected keys.
    */
   private final Thread selectorThread;


   /**
    * Constructor.
    *
    * @param clock                 the cluster clock.
    * @param address               an IP address this server accepts requests at
    * @param port                  a TCP tcpPort this server accepts requests at
    * @param requestDispatcher     the request dispatcher.
    * @param socketTimeoutMillis   a network timeout in milliseconds.
    * @param selectorTimeoutMillis a time the selector should block for while waiting for a channel to become ready,
    *                              must be greater than zero.
    * @throws IOException IO error when opening the socket.
    * @noinspection SocketOpenedButNotSafelyClosed, OverlyBroadCatchBlock
    */
   public Receiver(final Clock clock, final String address, final int port,
           final RequestDispatcher requestDispatcher, final long socketTimeoutMillis,
           final long selectorTimeoutMillis) throws IOException {

      this.endpoint = createEndpoint(address, port);

      this.selector = Selector.open();
      this.selectorThread = new UserThreadFactory("Receiver:" + port).newThread(
              new ReceiverSelectorWorker(selector, socketTimeoutMillis, selectorTimeoutMillis));

      // Create ServerSocketChannel
      serverSocketChannel = ServerSocketChannel.open();

      try {

         // Create receiver key handler
         final ReceiverKeyHandler receiverKeyHandler = new ReceiverKeyHandler(selector, requestDispatcher, clock,
                 socketTimeoutMillis);

         // Configure it as non-locking
         serverSocketChannel.configureBlocking(false);

         // Register receiver key handler with the server socket channel
         serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, receiverKeyHandler);
      } catch (final IOException e) {

         closeHard(serverSocketChannel);
         throw e;
      }
   }


   /**
    * Starts up the server. The server that has started accepts and handles TCP requests.
    *
    * @throws IOException if I/O error occurred at startup
    */
   public void startup() throws IOException {

      // Create endpoint and inform about starting
      LOG.info("Starting TCP server bound to " + StringUtils.toString(endpoint));

      // Check preconditions
      verifyStartable();

      // Bind sockets
      for (final SelectionKey selectionKey : selector.keys()) {

         // Bind server socket
         try {
            final ServerSocket serverSocket = ((ServerSocketChannel) selectionKey.channel()).socket();
            serverSocket.setReceiveBufferSize(SystemProperty.BUFFER_SIZE);
            serverSocket.setReuseAddress(true);
            serverSocket.bind(endpoint);
         } catch (final BindException e) {
            throw createDetailedBindException(e, endpoint);
         }
      }

      // Start selector thread
      selectorThread.start();
   }


   /**
    * Shuts down the server. The server that has been shutdown does not accept and does not handle requests. The
    * shutdown server cannot be restarted.
    *
    * @throws IllegalStateException if the server has already been shutdown.
    */
   public void shutdown() {

      //
      LOG.info("Shutting down TCP server: " + endpoint);

      // Rise shutdown flag
      if (!operationalStatus.compareAndSet(OPERATIONAL_STATUS_STARTED, OPERATIONAL_STATUS_SHUTDOWN)) {
         throw new IllegalStateException("Tried to shutdown server that has already been shutdown");
      }

      // Close the server socket channel first so that closing socket channels
      // wouldn't allow other nodes to re-establish the connection.
      closeHard(serverSocketChannel);
      try {
         selector.selectNow();
      } catch (final IOException e) {
         ignoreException(e, "Shutting down");
      }

      // Interrupt selector thread. This should unblock any NIO operations.
      interruptAndJoin(selectorThread, 1000L);

      // NOTE: simeshev@cacheonix.org - 2010-10-27 - I have observed on several
      // occasions that selector.select(); misses the interrupt. So, we try
      // again.
      if (!isShutDown()) {
         interruptAndJoin(selectorThread, 1000L);
      }

      // Close selector if still alive.
      if (!isShutDown()) {
         closeHard(selector);
      }

      LOG.info("TCP server has been shutdown: " + endpoint);
   }


   /**
    * @return <code>true</code> if {@link #shutdown()} } has been called for this method.
    */
   public boolean isShutDown() {

      return !selectorThread.isAlive();
   }


   /**
    * @return IP address this server accepts requests at.
    */
   final String getAddress() {

      return endpoint.getHostName();
   }


   private void verifyStartable() {

      if (operationalStatus.get() == OPERATIONAL_STATUS_SHUTDOWN) {
         throw new IllegalStateException("Cannot start the server that has been shutdown");
      }

      if (operationalStatus.get() == OPERATIONAL_STATUS_STARTED) {
         throw new IllegalStateException("Cannot start the server that has already been started");
      }

      if (!operationalStatus.compareAndSet(OPERATIONAL_STATUS_NOT_STARTED, OPERATIONAL_STATUS_STARTED)) {
         throw new IllegalStateException("Cannot start the server, operational status: " + operationalStatus.get());
      }
   }


   private static InetSocketAddress createEndpoint(final String host, final int tcpPort) throws UnknownHostException {

      final InetSocketAddress endpoint;
      if (StringUtils.isBlank(host)) {
         // bind to all addresses
         endpoint = new InetSocketAddress(tcpPort);
      } else {
         endpoint = new InetSocketAddress(InetAddress.getByName(host), tcpPort);
      }
      return endpoint;
   }


   protected void finalize() throws Throwable {

      if (!isShutDown()) {
         LOG.warn("TCPServer was GC-ed before it was shutdown", stackTraceAtCreate);
         try {
            shutdown();
         } catch (final Exception e) {
            ignoreException(e, "finalizer");
         }
      }
      super.finalize();
   }


   /**
    * Creates a BindException with added information on the address for that the exception occurred. The stack trace of
    * the resulting exception is set to the stack trace of the original exception.
    *
    * @param originalException original exception.
    * @param endpoint          address
    * @return BindException with added information on the address for that the exception occurred.
    */
   private static BindException createDetailedBindException(final BindException originalException,
           final InetSocketAddress endpoint) {

      final String newMessage = originalException.getMessage() + ". Address: " + endpoint;
      final BindException newBindException = new BindException(newMessage);
      newBindException.setStackTrace(originalException.getStackTrace());
      return newBindException;
   }


   /**
    * @noinspection ArithmeticOnVolatileField
    */
   public String toString() {

      return "TCPServer{" +
              "stackTraceAtCreate=" + stackTraceAtCreate +
              ", operationalStatus=" + operationalStatus +
              ", endpoint=" + endpoint +
              ", selector=" + selector +
              ", selectorThread=" + selectorThread +
              '}';
   }
}
