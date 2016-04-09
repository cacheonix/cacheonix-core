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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Payload partitioner splits an object into a set of wire-level MulticastPacket objects.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 28, 2008 6:06:06 PM
 */
final class PayloadPartitioner {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PayloadPartitioner.class); // NOPMD

   private static final int MAXIMUM_PART_LENGTH = Frame.MAXIMUM_MCAST_PAYLOAD_LENGTH; // NOPMD

   private final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);


   /**
    * Splits a message into a list of <code>Frame</code> objects.
    *
    * @param message the message to partition.
    * @return a list of {@link Frame} objects.
    * @throws NotPartitionableException if the message cannot be partitioned.
    */
   public List<Frame> partition(final Message message) {

      try {
         // Check for null
         if (message == null) {
            return Collections.emptyList();
         }

         // Serialize
         final byte[] bytes = serialize(message);

         // Get counters
         final int partLength = MAXIMUM_PART_LENGTH;
         final int lastPartLength = bytes.length % partLength;
         final int completePartCount = bytes.length / partLength;
         final int incompletePartCount = lastPartLength > 0 ? 1 : 0;
         final int totalPartCount = completePartCount + incompletePartCount;
         final List<Frame> result = new ArrayList<Frame>(totalPartCount);

         // Get full parts
         int partIndex = 0;
         for (; partIndex < completePartCount; partIndex++) {
            final int from = partIndex * partLength;
            final int to = from + partLength;
            final byte[] part = ArrayUtils.copyOfRange(bytes, from, to);
            result.add(makeFrame(totalPartCount, partIndex, part));
         }

         // Get incomplete part
         final byte[] part = ArrayUtils.copyOfRange(bytes, bytes.length - lastPartLength, bytes.length);
         result.add(makeFrame(totalPartCount, partIndex, part));

         // Return result
         return result;
      } catch (final IOException e) {
         throw new NotPartitionableException(message, e);
      }
   }


   /**
    * Helper method to create a packet using given set of parameters.
    */
   private static Frame makeFrame(final int totalPartCount, final int partIndex, final byte[] part) {

      return new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH, Serializer.TYPE_JAVA, -1L, totalPartCount, partIndex, part);
   }


   /**
    * @noinspection IOResourceOpenedButNotSafelyClosed
    */
   private byte[] serialize(final Object object) throws IOException {

      return serializer.serialize(object);
   }


   public String toString() {

      return "PayloadPartitioner{" +
              '}';
   }
}
