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
package org.cacheonix.impl.net.tcp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.net.Protocol;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.SenderInetAddressAware;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.config.SystemProperty.BUFFER_SIZE;


/**
 * Handles a readable channel by reading Messages from it.
 */
final class ReceiverKeyHandler extends KeyHandler {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Receiver.class); // NOPMD

   /**
    * Protocol signature as a byte array.
    */
   private static final byte[] PROTOCOL_SIGNATURE_BYTES = Protocol.getProtocolSignature();


   private static final int READING_SIGNATURE = 0;

   private static final int READING_MAGIC_NUMBER = 1;

   private static final int READING_PROTOCOL_VERSION = 2;

   private static final int READING_FRAME_SIZE = 3;

   private static final int READING_FRAME = 4;


   /**
    * State.
    */
   private int state = READING_SIGNATURE;


   /**
    * Read frame size.
    *
    * @see #READING_FRAME_SIZE
    * @see #handleRead(SelectionKey)
    */
   private int frameSize = 0;


   /**
    * Read buffer.
    */
   private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

   /**
    * Buffer accumulating chunks.
    */
   private final ChunkedBuffer chunkedBuffer = new ChunkedBuffer();


   private final RequestDispatcher requestDispatcher;


   ReceiverKeyHandler(final Selector selector, final RequestDispatcher requestDispatcher, final Clock clock,
           final long socketTimeoutMillis) {

      super(selector, socketTimeoutMillis, clock);
      this.requestDispatcher = requestDispatcher;
   }


   public void handleKey(final SelectionKey key) throws InterruptedException {

      if (key.isAcceptable()) {

         handleAccept(key);
      } else if (key.isReadable()) {

         // Socket is ready for read
         handleRead(key);

      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation accepts the connection request and registers and new Reader with the selector.
    *
    * @see #selector()
    */
   private void handleAccept(final SelectionKey key) throws UnrecoverableAcceptException {
      // Server channel is ready to accept

      final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
      try {
         final SocketChannel socketChannel = serverSocketChannel.accept();
         if (socketChannel != null) {

            // Create receiverKeyHandler
            final ReceiverKeyHandler receiverKeyHandler = new ReceiverKeyHandler(selector, requestDispatcher, clock,
                    getNetworkTimeoutMillis());

            // Configure channel for non-blocking operation
            socketChannel.configureBlocking(false);

            // Configure the socket
            socketChannel.socket().setReceiveBufferSize(BUFFER_SIZE);
            socketChannel.register(selector, SelectionKey.OP_READ, receiverKeyHandler);
         }
      } catch (final IOException e) {
         throw new UnrecoverableAcceptException(e);
      }
   }


   /**
    * {@inheritDoc}
    */
   private void handleRead(final SelectionKey key) throws InterruptedException {

      final SocketChannel channel = (SocketChannel) key.channel();

      try {

         byteBuffer.clear();

         // Write available chunks from the channel to the buffer
         int bytesRead = channel.read(byteBuffer);

         // Make buffer available for reading
         byteBuffer.flip();

         while (bytesRead > 0) {

            // Get new chunk
            final ByteBuffer chunk = ByteBuffer.allocate(bytesRead);
            chunk.put(byteBuffer);
            chunk.flip();
            chunkedBuffer.addChunk(chunk);

            // Consume new chunk
            for (boolean hasMore = true; hasMore; ) {

               switch (state) {

                  case READING_SIGNATURE:

                     if (chunkedBuffer.available() >= Protocol.getProtocolSignatureLength()) {

                        // Consume signature
                        for (int i = 0; i < Protocol.getProtocolSignatureLength(); i++) {

                           if (PROTOCOL_SIGNATURE_BYTES[i] != chunkedBuffer.get()) {

                              // Throw en exception to exit the while loop

                              //noinspection ThrowCaughtLocally
                              throw new IOException("Invalid frame signature");
                           }
                        }

                        // Successfully have read the signature, switch to reading the magic number
                        state = READING_MAGIC_NUMBER;
                     } else {
                        hasMore = false;
                     }
                     break;
                  case READING_MAGIC_NUMBER:

                     if (chunkedBuffer.available() >= 4) {

                        // Consume magic number
                        final int magicNumber = chunkedBuffer.getInt();
                        if (magicNumber != Protocol.getProtocolMagicNumber()) {

                           // Throw en exception to exit the while loop

                           //noinspection ThrowCaughtLocally
                           throw new IOException("Invalid magic number: " + magicNumber);
                        }


                        // Successfully have read the signature, switch to reading the magic number
                        state = READING_PROTOCOL_VERSION;
                     } else {
                        hasMore = false;
                     }
                     break;
                  case READING_PROTOCOL_VERSION:

                     if (chunkedBuffer.available() >= 4) {

                        // Consume protocol version
                        final int protocolVersion = chunkedBuffer.getInt();
                        if (protocolVersion != Protocol.getProtocolVersion()) {

                           // Throw en exception to exit the while loop

                           //noinspection ThrowCaughtLocally
                           throw new IOException("Invalid protocol version: " + protocolVersion);
                        }

                        // Successfully have read the signature, switch to reading the magic number
                        state = READING_FRAME_SIZE;
                     } else {
                        hasMore = false;
                     }
                     break;
                  case READING_FRAME_SIZE:

                     if (chunkedBuffer.available() >= 4) {

                        // Consume frame size
                        this.frameSize = chunkedBuffer.getInt();

                        // Successfully have read the size, switch to reading the frame
                        state = READING_FRAME;
                     } else {
                        hasMore = false;
                     }
                     break;
                  case READING_FRAME:

                     if (chunkedBuffer.available() >= frameSize) {

                        // Consume frame
                        final byte[] frameBytes = new byte[frameSize];
                        for (int i = 0; i < frameSize; i++) {
                           frameBytes[i] = chunkedBuffer.get();
                        }
                        final ByteArrayInputStream bais = new ByteArrayInputStream(frameBytes);
                        final DataInputStream dis = new DataInputStream(bais);
                        final Frame frame = new Frame();
                        frame.readFrame(dis);

                        // Get message
                        final Message message = (Message) Frame.getPayload(frame);

                        // Synchronize time
                        clock.adjust(message.getTimestamp());

                        // Set sender's inet address
                        if (message instanceof SenderInetAddressAware) {

                           ((SenderInetAddressAware) message).setSenderInetAddress(channel.socket().getInetAddress());
                        }

                        // Dispatch
//                        //noinspection ControlFlowStatementWithoutBraces
//                        if (LOG.isDebugEnabled()) LOG.debug("Received: " + message); // NOPMD
                        requestDispatcher.dispatch(message);

                        // Begin reading new frame
                        state = READING_SIGNATURE;
                     } else {
                        hasMore = false;
                     }

                     break;
                  default:

                     // Unknown state, throw en exception to exit the while loop

                     //noinspection ThrowCaughtLocally
                     throw new IOException("Unknown receiver state: " + state);
               }
            }

            // Read next chunk
            bytesRead = channel.read(byteBuffer);
         }

         // Close channel if necessary
         if (bytesRead == -1) {
            IOUtils.closeHard(channel);
         }

      } catch (final IOException ignored) {

         // Closing channel will cancel the key
         IOUtils.closeHard(key);
      }
   }


   /**
    * {@inheritDoc}
    */
   public void handleIdle(final SelectionKey idleKey) {

      registerInactivity(idleKey);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * If there are unfinished incoming messages, clears the buffers and closes the channel.
    */
   protected void handleTimeout(final SelectionKey key) {

      if (chunkedBuffer.available() > 0) {

         // Discard partially read buffer
         chunkedBuffer.clear();

         // Close/discard channel
         IOUtils.closeHard(key);

         // Should not be necessary because the chanel and the key
         // are discarded, but for the symmetry sake we have it.
         state = READING_SIGNATURE;
      }
   }


   public String toString() {

      return "ReceiverKeyHandler{" +
              "state=" + state +
              ", frameSize=" + frameSize +
              ", byteBuffer=" + byteBuffer +
              ", chunkedBuffer=" + chunkedBuffer +
              "} " + super.toString();
   }
}