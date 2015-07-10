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

import org.cacheonix.impl.net.processor.Frame;
import junit.framework.TestCase;

/**
 * ReceivedList Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>03/31/2008</pre>
 */
public final class ReceivedListTest extends TestCase {

   private ReceivedList receivedList;

   private static final int PART_COUNT = 1;

   private static final int PART_INDEX = 0;

   private static final byte[] PART = {(byte) 0};

   private static final byte SERIALIZER_TYPE = (byte) 1;


   public void testGetHighestSequenceNumberReceived() throws Exception {

      receivedList.add(makePacket(0));
      assertEquals(Long.valueOf(0L), receivedList.getHighestContinuousNumberReceived());

      receivedList.add(makePacket(1));
      assertEquals(Long.valueOf(1L), receivedList.getHighestContinuousNumberReceived());
   }


   public void testGetMessage() throws Exception {
      // Create packets
      final Frame frame1 = makePacket(1);
      final Frame frame2 = makePacket(2);
      // Add packets to the list
      receivedList.add(frame1);
      receivedList.add(frame2);
      // Get message # 1
      final Frame message1 = receivedList.getMessage(1L);
      assertEquals(frame1, message1);
      assertEquals(1L, message1.getSequenceNumber());
      // Get message # 2
      final Frame message2 = receivedList.getMessage(2L);
      assertEquals(frame2, message2);
      assertEquals(2L, message2.getSequenceNumber());
   }


   public void testGetSmallestNumberReceivedButIndelivered() throws Exception {
      // Add first
      receivedList.add(makePacket(1));
      assertEquals(Long.valueOf(1L), receivedList.getSmallestNumberReceivedButNotDelivered());

      // Add second
      receivedList.add(makePacket(2));
      receivedList.poll(1L);
      assertEquals(Long.valueOf(2L), receivedList.getSmallestNumberReceivedButNotDelivered());
   }


   public void testHighestContinuousNumberReceviedButUndelivered() throws Exception {
      // Add first
      receivedList.add(makePacket(1));
      assertEquals(Long.valueOf(1L), receivedList.getHighestContinuousNumberReceivedButNotDelivered());

      // Add second
      receivedList.add(makePacket(2));
      assertEquals(Long.valueOf(2L), receivedList.getHighestContinuousNumberReceivedButNotDelivered());

      // Add fourth (out of order)
      receivedList.add(makePacket(4));
      assertEquals(Long.valueOf(2L), receivedList.getHighestContinuousNumberReceivedButNotDelivered());

      // Take out first two
      receivedList.poll(1L);
      receivedList.poll(2L);
      assertEquals(Long.valueOf(4L), receivedList.getSmallestNumberReceivedButNotDelivered());
   }


   public void testAdd() {

      receivedList.add(makePacket(0));
      assertEquals(Long.valueOf(0L), receivedList.getHighestContinuousNumberReceived());

      receivedList.add(makePacket(1));
      assertEquals(Long.valueOf(1L), receivedList.getHighestContinuousNumberReceived());
   }


   private static Frame makePacket(final int sequenceNumber) {

      return new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH, SERIALIZER_TYPE,
              (long) sequenceNumber, PART_COUNT, PART_INDEX,
              PART);
   }


   public void testHashCode() {

      assertTrue(receivedList.hashCode() != 0);
   }


   public void testToString() {

      assertNotNull(receivedList.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      receivedList = new ReceivedList();
   }


   public String toString() {

      return "ReceivedListTest{" +
              "receivedList=" + receivedList +
              "} " + super.toString();
   }
}
