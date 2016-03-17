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
package org.cacheonix.impl.net.cluster;


import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.util.logging.Logger;

/**
 * ReceivedList
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 31, 2008 2:09:16 PM
 */
@SuppressWarnings({"unchecked", "AutoUnboxing"})
public final class ReceivedListImpl implements ReceivedList {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ReceivedListImpl.class); // NOPMD

   /**
    * A list ordered by the message sequence number.
    */
   private final SortedMap<Long, Frame> received = new TreeMap<Long, Frame>();

   /**
    * Highest continuous sequence number received by this protocol stub, or Ri
    */
   private Long highestContinuousNumberReceived = null;


   /**
    * Highest sequence number received by this protocol stub, or L
    */
   private Long highestSequenceNumberReceived = null;


   public void add(final Frame frame) {

      final Long sequenceNumber = frame.getSequenceNumber();

      if (highestContinuousNumberReceived == null) {

         // Receive first packet
         received.put(sequenceNumber, frame);

         // Set highest continuous number received
         highestContinuousNumberReceived = sequenceNumber;

         // Set highest sequence number received
         highestSequenceNumberReceived = sequenceNumber;
      } else {

         // Check if this is a duplicate
         if (sequenceNumber <= highestContinuousNumberReceived) {
            return;
         }

         // This is a new packet
         if (sequenceNumber == highestContinuousNumberReceived + 1L) {

            // Packet is in order receive
            received.put(sequenceNumber, frame);

            // Advance highest continuous number received
            highestContinuousNumberReceived = sequenceNumber;

            // Advance highest sequence number received
            highestSequenceNumberReceived = sequenceNumber;

            // Check if this is the last one
            if (received.lastKey().equals(sequenceNumber)) {
//            if (LOG.isDebugEnabled()) {
//               LOG.debug("Is last key: " + longSequenceNumber + ", sequenceNumber: " + sequenceNumber + ", own: " + ourOwn);
//            }
               return;
            }

            // Check if the packet closes the gap
            for (final Long next : received.tailMap(sequenceNumber + 1L).keySet()) {

               if (next == highestContinuousNumberReceived + 1L) {

                  // Closed gap
                  highestContinuousNumberReceived = next;

                  if (LOG.isDebugEnabled()) {
                     LOG.debug("<<<<<<<<<<<<< CLOSED THE GAP! <<<<< sequenceNumber: " + sequenceNumber + ", highestSequenceNumberReceived: " + highestContinuousNumberReceived);
                  }
               } else {

                  // Hit the gap
                  break;
               }
            }
         } else {

            // Packet has a gap
            // Check if already received
            if (received.containsKey(sequenceNumber)) {

               // Do nothing
               return;
            }


            if (LOG.isDebugEnabled()) {
               LOG.debug(">>>> GAP >>>>>>>>>>>>> Adding packet to receiveList, sequenceNumber: " + sequenceNumber
                       + ", highestSequenceNumberReceived: " + highestContinuousNumberReceived
                       + ", smallestNumberReceivedButNotDelivered: " + getSmallestNumberReceivedButNotDelivered()
                       + ", highestContinuousNumberReceivedButNotDelivered: " + getHighestContinuousNumberReceivedButNotDelivered()
                       + ", highestNumberReceivedButNotDelivered: " + getHighestNumberReceivedButNotDelivered()

               );
            }

            // Receive first packet
            received.put(sequenceNumber, frame);

            // Set highest sequence number received
            highestSequenceNumberReceived = sequenceNumber;
         }
      }
   }


   public Long getHighestContinuousNumberReceived() {

      return highestContinuousNumberReceived;
   }


   public Long getHighestSequenceNumberReceived() {

      return highestSequenceNumberReceived;
   }


   public Frame getMessage(final long messageSequenceNumber) {

      final Frame frame = received.get(Long.valueOf(messageSequenceNumber));
      if (frame == null) {

         throw new IllegalStateException("Messaged not found in received list: "
                 + messageSequenceNumber + ", list: " + received);
      }
      return frame;
   }


   public Frame poll(final long messageNumToDeliver) {

      final Long key = Long.valueOf(messageNumToDeliver);
      final Frame frame = received.get(key);
      if (frame == null) {

         return null;
      } else {

         final Long firstKey = received.firstKey();
         if (key.equals(firstKey)) {

            received.remove(key);
            return frame;
         } else {

            return null;
         }
      }
   }


   public void setHighestContinuousNumberReceived(final Long sequenceNumber) {

      // This op is allowed only if the list is empty, as a part of joining the cluster.
      if (!received.isEmpty()) {

         throw new IllegalStateException("A forced setting of the sequence number " +
                 "is allowed only for an empty receive list: " + this);
      }

      highestContinuousNumberReceived = sequenceNumber;
   }


   public void setHighestSequenceNumberReceived(final Long sequenceNumber) {

      // This op is allowed only if the list is empty, as a part of joining the cluster.
      if (!received.isEmpty()) {

         throw new IllegalStateException("A forced setting of the sequence number " +
                 "is allowed only for an empty receive list: " + this);
      }

      highestSequenceNumberReceived = sequenceNumber;
   }


   public Long getSmallestNumberReceivedButNotDelivered() {

      // This should be the head of the queue.
      if (received.isEmpty()) {

         return null;
      }

      return received.firstKey();
   }


   public Long getHighestContinuousNumberReceivedButNotDelivered() {

      if (received.isEmpty()) {

         return null;
      }

      // Find the number gap if any
      final Iterator numberIter = received.keySet().iterator();
      Long number = (Long) numberIter.next();
      while (numberIter.hasNext()) {

         final Long nextNumber = (Long) numberIter.next();
         if (nextNumber - number == 1L) {

            // Next please
            number = nextNumber;
         } else {

            // Hit the gap
            break;
         }
      }
      return number;
   }


   public Long getHighestNumberReceivedButNotDelivered() {

      if (received.isEmpty()) {
         return null;
      }

      return received.lastKey();
   }


   public boolean isEmpty() {

      return received.isEmpty();
   }


   public void clear() {

      this.highestContinuousNumberReceived = null;
      this.highestSequenceNumberReceived = null;
      this.received.clear();
   }


   public String toString() {

      return "ReceivedList{" +
              "highestContinuousNumberReceived=" + highestContinuousNumberReceived +
              ", highestSequenceNumberReceived=" + highestSequenceNumberReceived +
              ", received.size=" + Integer.toString(received.size()) +
              '}';
   }
}
