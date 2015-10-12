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
package org.cacheonix.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.configuration.SystemProperty;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.tcp.server.KeyHandler;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A message sender associated with a particular ClusterNodeAddress. The sender lives until the server is shutdown.
 */
final class Sender extends KeyHandler {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Sender.class); // NOPMD

   /**
    * In the initial state the sender is unconnected with no unfinished messages.
    */
   private static final int INIT = 0;

   /**
    * In the connecting state the sender is waiting for finishing the connection.
    *
    * @see #handleFinishConnect(SelectionKey)
    */
   private static final int CONNECTING = 1;

   /**
    * In writing state the sender is operational and is writing messages when the channel is ready for write.
    *
    * @see #handleWrite(SelectionKey)
    */
   private static final int WRITING = 2;

   /**
    * Serializer.
    */
   private final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);

   /**
    * This cluster node's clock. The clock is used to time stamp sent messages.
    */
   private final Clock clock;


   /**
    * A list of InetAddresses to try to connect to.
    *
    * @see #beginConnecting(boolean)
    */
   private final LinkedList<InetAddress> addressesToTry = new LinkedList<InetAddress>(); // NOPMD

   /**
    * A queue of messages to send.
    */
   private final LinkedList<Message> messages = new LinkedList<Message>(); // NOPMD

   /**
    * An address of the message receiver.
    */
   private final ReceiverAddress receiverAddress;

   /**
    * A router to respond to with requests failures.
    */
   private final Router router;

   /**
    * A state machine.
    */
   private int state = INIT;

   /**
    * A byte buffer containing an unfinished write.
    */
   private ByteBuffer leftover;


   /**
    * Constructor.
    *
    * @param selector             the selector
    * @param receiverAddress      receiver node address.
    * @param router               cluster processor.
    * @param networkTimeoutMillis network timeout in milliseconds.
    * @param clock                this cluster node's clock.
    */
   public Sender(final Selector selector, final ReceiverAddress receiverAddress,
                 final Router router, final long networkTimeoutMillis, final Clock clock) {

      super(selector, networkTimeoutMillis);
      this.receiverAddress = receiverAddress;
      this.router = router;
      this.clock = clock;
   }


   /**
    * Sender does not handle accept.
    *
    * @param key a key.
    */
   public void handleAccept(final SelectionKey key) {

      throw new IllegalStateException("Sender doesn't handle accept");
   }


   /**
    * Processes readiness for OP_CONNECT
    *
    * @param key key to process
    * @throws InterruptedException if this thread was interrupted.
    */
   public void handleFinishConnect(final SelectionKey key) throws InterruptedException {

      final SocketChannel channel = socketChannel(key);

      switch (state) {

         case CONNECTING:


            //
            // Finish connecting
            //

            try {

               // Finish channel connect
               channel.finishConnect();
               channel.socket().setSendBufferSize(SystemProperty.BUFFER_SIZE);
               channel.socket().setReceiveBufferSize(SystemProperty.BUFFER_SIZE);

               // Unregister interest in OP_CONNECT and register in OP_READ. OP_READ provides
               // information about the other side closing the channel
               key.interestOps(SelectionKey.OP_READ);

               //
               //noinspection ControlFlowStatementWithoutBraces
//               if (LOG.isDebugEnabled()) LOG.debug("Connected to: " + channel.socket().getRemoteSocketAddress()); // NOPMD

            } catch (final IOException e) {

//               //noinspection ControlFlowStatementWithoutBraces
               //noinspection ControlFlowStatementWithoutBraces
//               if (LOG.isDebugEnabled()) LOG.debug("messages: " + messages); // NOPMD
//               if (LOG.isDebugEnabled()) //noinspection ThrowableResultOfMethodCallIgnored
//                  LOG.debug("e: " + ExceptionUtils.enhanceExceptionWithAddress(channel, e)); // NOPMD

               // Try to connect to the next address
               beginConnecting(false);

               // Return early as there is nothing else we do
               return;
            }

            //
            // Successfully connected, now is ready to write, go ahead and write
            //

            // Set state to writing
            state = WRITING;

            // Call handleWrite() because connected channel means
            // that it is available for write
            handleWrite(key);

            break;
         default:

            throw new IllegalStateException("handleFinishConnect() can only be called in CONNECTING state: " + state);
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation writes messages or writes leftover.
    */
   public void handleWrite(final SelectionKey key) throws InterruptedException {

      switch (state) {

         case WRITING:

            //
            // Process write
            //

            try {

               //noinspection VariableNotUsedInsideIf
               if (leftover == null) {

                  //
                  // No unfinished writes, write messages
                  //

                  writeMessages(key);

               } else {

                  //
                  // Continue/finish writing a message write in progress
                  //

                  writeLeftover(key);
               }
            } catch (final IOException e) {

               // Write failed. It may fail because there was an intermittent
               // connection failure, or because the host went down. In either
               // case we need to try to re-connect before giving up.

               // Close the channel formally so that the server side knows
               // that there won't be valid input from this side on this channel.
               IOUtils.closeHard(socketChannel(key));

               // Clear partial message

               leftover = null;

               // Begin connecting from the beginning of the address list
               beginConnecting(true);
            }

            break;

         default:
            throw new IllegalStateException("Illegal state: " + state);
      }
   }


   public void handleIdle(final SelectionKey idleKey) throws InterruptedException {

      switch (state) {

         case WRITING:

            if (!messages.isEmpty()) {

               // There are messages that may need sending
               handleWrite(idleKey);
            }
         default:
            break;
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * 1. Connection timeout occurs after the connection was initiated but before OP_CONNECT is ready, (Sender is in the
    * CONNECTING state) but did not complete in time. Actions on the connection timeout:
    * <p/>
    * a) Close the channel
    * <p/>
    * b) Set sender state to INIT.
    * <p/>
    * c) Respond to requests in the queue with an error
    * <p/>
    * 2. Write timeout occurs when the Sender is in the Writing state and there are messages in the send queue. The
    * write timeout means that the write channel, that should be ready for write almost always, has slowed down to a
    * halt. The slow down is indistinguishable from a failure of the receiving host. In general, the write timeout
    * defines a minimum acceptable write throughput. For instance, if there wasn't write activity for 1 second, it means
    * that the write throughput dropped under 1 Byte/sec. In Cacheonix, in addition to the network conditions,  the
    * slowdown may be caused by garbage collection (GC) on the receiver side. During GC the receiver may become
    * unresponsive which is indistinguishable from network slowdown. The GC can reach tens of seconds. This means that
    * the timeout should be set to the maximum expected time for GC. The default timeout should be set to 30 secs.
    * <p/>
    * Actions on the write timeout:
    * <p/>
    * a) Close the channel
    * <p/>
    * b) Set sender state to INIT.
    * <p/>
    * c) Respond to requests in the queue with an error
    */
   protected void handleTimeout(final SelectionKey key) {

      //noinspection ControlFlowStatementWithoutBraces
//      if (LOG.isDebugEnabled()) LOG.debug("Timeout for key: " + key + ", channel: " + key.channel()); // NOPMD

      // Close channel
      IOUtils.closeHard(key);

      if (leftover == null) {

         if (messages.isEmpty()) {

            // There are no messages to send. We just close the channel.

//            //noinspection ControlFlowStatementWithoutBraces
//            if (LOG.isDebugEnabled()) {
//               LOG.debug("TTTTTTTTTTTTTTTT There are no more messages, won't reconnect to " + receiverNodeAddress + " : " + key + ", channel: " + key.channel()); // NOPMD
//            }

            // Setting state to init will cause the Sender to begin to reconnect
            // when the selector worker thread enqueues the message to this sender.
            state = INIT;

         } else {

            // The fact that Sender timed out usually means that the Receiver on the other side
            // has just closed the channel due to Sender's inactivity which manifest itself as
            // inability to write (channel.write() returns zero bytes written. Usually it is
            // enough to re-connect. Begin connecting from the beginning of the address list.

            //noinspection ControlFlowStatementWithoutBraces
//            if (LOG.isDebugEnabled()) LOG.debug("TTTTTTTTTTTTTTTT There are messages, beginning to connect to " + receiverNodeAddress); // NOPMD
            beginConnecting(true);
         }
      } else {

         // This means that write stuck in the middle, which is usually means that the other
         // side stopped reading. It is appropriate to respond with an early error.

         // Clear partial message
         leftover = null;

         // Remove failed message
         final Message message = messages.removeFirst();

         //noinspection ControlFlowStatementWithoutBraces
//         if (LOG.isDebugEnabled()) {
//            LOG.debug("TTTTTTTTTTTTTTTT There is leftover, responding with failure, then reconnecting to " + receiverNodeAddress + " : " + message); // NOPMD
//         }

         // Respond with failure
         respondWithFailure(message, "Operation timed out after " + getNetworkTimeoutMillis());

         // It still makes sense to try to reconnect in a less likely case that was an intermittent error.

         beginConnecting(true);
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * The sender is interested in read only for the purpose of detecting this channel closed by the other side.
    *
    * @param key the key to process.
    */
   public void handleRead(final SelectionKey key) {

      switch (state) {

         case WRITING:

            // Read
            final SocketChannel channel = socketChannel(key);
            final ByteBuffer buffer = ByteBuffer.allocate(1);
            String error = null;
            try {

               if (channel.read(buffer) == -1) {

                  error = "Connection was broken";
               }
            } catch (final IOException e) {

               error = e.toString();
            }

            // Handle if error
            if (error != null) {

               // Close the channel formally so that the server side knows
               // that there won't be valid input from this side on this channel.
               IOUtils.closeHard(socketChannel(key));

               // Clear partial message
               leftover = null;

               state = INIT;

               respondToAllWithFailure(error);
            }
            break;
         default:
            throw new IllegalStateException("Read readiness should not be received in this state " + state);
      }
   }


   /**
    * Puts the message into an internal queue for actual sending.
    *
    * @param message the message to enqueue.
    */
   public void enqueue(final Message message) {


      // Enqueue the message
//      //noinspection ControlFlowStatementWithoutBraces
//      if (LOG.isDebugEnabled() && message instanceof RecoveryMarker) LOG.debug("Enqueued: " + message); // NOPMD
      messages.add(message);

      // Begin connecting if this is the initial state
      if (state == INIT) {

         // Create a non-blocking socket channel and register the channel and the sender
         beginConnecting(true);
      }
   }


   private void writeLeftover(final SelectionKey key) throws IOException, InterruptedException {

      final SocketChannel channel = socketChannel(key);

      final int bytesToWrite = leftover.remaining();
      final int bytesWritten = write(channel, leftover);
      if (bytesWritten == bytesToWrite) {

         // Completely wrote the message, remove the written from the queue
         messages.removeFirst();

//         //noinspection ControlFlowStatementWithoutBraces
//         if (LOG.isDebugEnabled()) LOG.debug("Sent: " + message); // NOPMD

         // Mark that no leftovers
         leftover = null;

         // Unregister interest in write
         key.interestOps(SelectionKey.OP_READ);

         // Call self again just in case there is more space in the output socket buffer
         handleWrite(key);
      }
   }


   /**
    * Writes messages to a channel associated with the key.
    *
    * @param key a key carrying the channel.
    * @throws IOException if I/O error occurs.
    */
   private void writeMessages(final SelectionKey key) throws IOException {

      long totalBytesWritten = 0L;

      final SocketChannel channel = socketChannel(key);

      for (final Iterator<Message> iter = messages.iterator(); iter.hasNext(); ) {

         final Message message = iter.next();

         // Time stamp the message
         message.setTimestamp(clock.currentTime());

         // Convert the message to a byte array
         final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
         final Frame requestFrame = new Frame(Integer.MAX_VALUE, message.getWireableType(),
                 serializer, Frame.NO_COMPRESSION, 0L, message);
         requestFrame.write(baos);
         baos.flush();
         final byte[] bytes = baos.toByteArray();

         // Write
         final ByteBuffer buffer = ByteBuffer.wrap(bytes);
         final int bytesToWrite = buffer.remaining();
         final int bytesWritten = write(channel, buffer);
         totalBytesWritten += bytesWritten;

         // Process write results
         if (bytesWritten < bytesToWrite) {

            // Did not finish writing, register interest in write
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);

            // Register leftover buffer
            leftover = buffer;

            // Exit message writing loop to wait for write readiness
            break;
         } else {

//            //noinspection ControlFlowStatementWithoutBraces
//            if (LOG.isDebugEnabled()) LOG.debug("Sent: " + message); // NOPMD

            // Completely wrote the message, process next
            iter.remove();
         }
      }

      // Register activity because handleWrite() can
      // be called outside of the main selector cycle
      if (totalBytesWritten == 0) {

         registerInactivity(key);
      } else {

         registerActivity();
      }
   }


   private void respondToAllWithFailure(final String errorDescription) {

//      if (!messages.isEmpty() && LOG.isDebugEnabled()) {
//         LOG.debug("Responding to all messages with failure, message count: " + messages.size()); // NOPMD
//      }

      for (final Message message : messages) {

         respondWithFailure(message, errorDescription);
      }

      // Clear all messages
      messages.clear();
   }


   private void respondWithFailure(final Message message, final String errorDescription) {

      final Request request = Request.toRequest(message);
      if (request != null) {

         final Response errorResponse = request.createResponse(Response.RESULT_INACCESSIBLE, errorDescription);
         errorResponse.setClusterUUID(request.getClusterUUID());
         router.route(errorResponse);
      }
   }


   /**
    * Begins connecting to a given inet address.
    *
    * @param initAddressesToTry if the connect attempt must start from the beginning of the list of available
    *                           addresses.
    */
   private void beginConnecting(final boolean initAddressesToTry) {

      // Re-populate addresses to try if needed
      if (initAddressesToTry) {
         addressesToTry.clear();
         addressesToTry.addAll(Arrays.asList(receiverAddress.getAddresses()));
      }

      //noinspection ControlFlowStatementWithoutBraces
//      if (LOG.isDebugEnabled()) {
//         LOG.debug("Beginning to connect to " + receiverNodeAddress + ", addresses to try: " + addressesToTry); // NOPMD
//      }

      // Go through available addresses to try.
      while (!addressesToTry.isEmpty()) {

         // Try next address
         final InetAddress address = addressesToTry.removeFirst();
         SocketChannel socketChannel = null;

         try {

            final int tcpPort = receiverAddress.getTcpPort();
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(address, tcpPort);
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector(), SelectionKey.OP_CONNECT, this);
            socketChannel.connect(inetSocketAddress);

            // Register activity because it is possible that the request to begin
            // connection came from a dormant sender that was disconnected because
            // of inactivity.
            registerActivity();

            // An attempt to begin to connect was successful,
            // switch to connecting state
            state = CONNECTING;

            // Return immediately
            return;
         } catch (final IOException e) {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("e: " + e, e); // NOPMD

            //
            // Failed to begin to connect, will try next
            // address if any
            //
            IOUtils.closeHard(socketChannel);
         }
      }

      // The fact that we are here means we failed to
      // initiate a connect at all available addresses.

      // Switch to initial state until someone
      // enqueues a message to send.
      state = INIT;

      // Respond as a failure to all messages
      respondToAllWithFailure("Cannot connect to receiver at " + receiverAddress);
   }


   /**
    * Writes a buffer to the channel as normal Channel.write(), but enhances exception's message with a channel's socket
    * address.
    *
    * @param channel the channel.
    * @param buffer  the buffer.
    * @return the number of bytes written.
    * @throws IOException if IO error occurred.
    * @see SocketChannel#write(ByteBuffer)
    */
   private static int write(final SocketChannel channel, final ByteBuffer buffer) throws IOException {

      try {
         return channel.write(buffer);

      } catch (final IOException e) {
         throw ExceptionUtils.enhanceExceptionWithAddress(channel, e);
      }
   }


   public String toString() {

      return "Sender{" +
              "receiverNodeAddress=" + receiverAddress +
              ", messages=" + messages.size() +
              ", addressesToTry=" + addressesToTry +
              ", state=" + state +
              ", leftover=" + leftover +
              '}';
   }
}
