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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Delivery queue is a queue that receives message parts and assembles a whole message.
 * <p/>
 * Once the whole message is assembled, the queue pushes it out by calling a listener.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 30, 2008 5:45:43 PM
 */
final class MessageAssembler {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MessageAssembler.class); // NOPMD

   /**
    * Serializer factory.
    */
   private final SerializerFactory serializerFactory = SerializerFactory.getInstance();

   /**
    * Parts.
    */
   private final LinkedList<Frame> parts = new LinkedList<Frame>(); // NOPMD LooseCoupling

   /**
    * Complete requests.
    */
   private final Queue<AssembledMessage> assembledMessages = new LinkedList<AssembledMessage>();


   /**
    * Adds a multicast packet to the end of the assembler.
    *
    * @param frame, possible partial.
    * @throws IOException if there was a problem adding a frame.
    */
   public void add(final Frame frame) throws IOException {

//      if (LOG.isDebugEnabled()) LOG.debug("Adding frame: " + frame);
      if (parts.isEmpty()) {

         // There are no parts waiting for completing the assembly.
         if (frame.getPartCount() == 1) {

            // Single-part message, deserialize
            final Message message = deserialize(frame.getSerializerType(), frame.getPayload());
            assembledMessages.add(new AssembledMessage(message, frame.getSequenceNumber()));
         } else {

            // Partial packet
            if (frame.getPartIndex() > 0) {

               // Out of order
               throw new IOException("Invalid packet order: " + frame);
            } else {

               // New multipart packet
               parts.add(frame);
            }
         }
      } else {

         // There are parts waiting for completing assembly.
         if (frame.getPartIndex() == parts.getLast().getPartIndex() + 1) {

            // Message in order
            parts.add(frame);

            if (frame.getPartIndex() == frame.getPartCount() - 1) {

               // This is the last frame of the multipart message, deliver

               // Remember start frame number
               final long firstFrameNumber = parts.getFirst().getSequenceNumber();

               // Calculate size
               int size = 0;
               for (final Frame partialPacket : parts) {
                  size += partialPacket.getPayload().length;
               }

               // OPTIMIZEME: simeshev@cacheonix.org - 2011-05-16 - Instead of creating a full-copy byte array below,
               // reduce memory consumption by creating a specialized input stream for the serializer that would pick
               // the data from the parts.

               // Copy data to array
               int destPos = 0;
               final byte[] whole = new byte[size];
               for (final Frame partialPacket : parts) {

                  final byte[] part = partialPacket.getPayload();

                  System.arraycopy(part, 0, whole, destPos, part.length);
                  destPos += part.length;
               }

               // Clear immediately to avoid holding frames in memory while assembling the message
               parts.clear();

               // Add to assembled requests
               final Message message = deserialize(frame.getSerializerType(), whole);
               assembledMessages.add(new AssembledMessage(message, firstFrameNumber));
            }
         } else {

            // Message out of order
            throw new IOException("Invalid packet order: " + frame);
         }
      }
   }


   /**
    * Retrieves and removes the head of this queue, or returns <tt>null</tt> if this queue is empty.
    *
    * @return the head of this queue, or <tt>null</tt> if this queue is empty
    */
   public AssembledMessage poll() {

      return assembledMessages.poll();
   }


   /**
    * @param serializerType a serializer type.
    * @param bytes          a message in a serialized form.
    * @return the deserialized message.
    * @noinspection OverlyBroadCatchBlock
    */
   private Message deserialize(final byte serializerType, final byte[] bytes) {

      try {
         return (Message) serializerFactory.getSerializer(serializerType).deserialize(bytes);
      } catch (final Exception e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   /**
    * Clears the queue.
    */
   public void clear() {

      assembledMessages.clear();
      parts.clear();
   }


   /**
    * Returns parts size.
    *
    * @return parts size.
    */
   int partsSize() {

      return parts.size();
   }


   /**
    * Returns an unmodifiable list of frames present in the message assembler.
    *
    * @return frames present in the message assembler.
    */
   public List<Frame> getParts() {

      return Collections.unmodifiableList(parts);
   }


   /**
    * Sets an initial list of frames present in the message assembler.
    *
    * @param messageAssemblerParts the initial list of frames present in the message assembler.
    */
   public void setParts(final List<Frame> messageAssemblerParts) {

      Assert.assertTrue(parts.isEmpty(), "Parts must be empty because setParts() can ony be called after reset: {0}", parts);
      parts.addAll(messageAssemblerParts);
   }


   public String toString() {

      return "PayloadAssembler{" +
              "serializerFactory=" + serializerFactory +
              ", parts=" + parts +
              ", completeRequests=" + assembledMessages +
              '}';
   }
}
