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

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.util.AssertionException;

/**
 * DeliveryQueue Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>03/30/2008</pre>
 */
public final class MessageAssemblerImplTest extends TestCase {

   private static final int MULTIPART_OBJECT_SIZE = Frame.MAXIMUM_MCAST_PAYLOAD_LENGTH * 3;

   private static final int SINGLE_PART_OBJECT_SIZE = Frame.MAXIMUM_MCAST_PAYLOAD_LENGTH / 3;

   private MessageAssemblerImpl messageAssembler;


   public void testAddSinglePartMessage() throws IOException {

      partitionAndAssert(SINGLE_PART_OBJECT_SIZE);
   }


   public void testAddMultiplePartMessage() throws IOException {

      partitionAndAssert(MULTIPART_OBJECT_SIZE);
   }


   public void testToString() {

      assertNotNull(messageAssembler.toString());
   }


   public void testClear() throws IOException {

      // Prepare by adding an incomplete message
      final TestMessage messageToPartition = new TestMessage(1, 1, TestUtils.makeTestObject(MULTIPART_OBJECT_SIZE));
      final List parts = new PayloadPartitioner().partition(messageToPartition);
      for (int i = 0; i < parts.size() - 1; i++) {
         messageAssembler.add((Frame) parts.get(i));
      }
      assertEquals(3, messageAssembler.partsSize());

      // Clear and assert
      messageAssembler.clear();
      assertEquals(0, messageAssembler.partsSize());
   }


   public void testSetPartsWithNonEmptyPartsListThrowsException() throws IOException {

      // Prepare by adding an incomplete message
      final List parts = new PayloadPartitioner().partition(new TestMessage(1, 1, TestUtils.makeTestObject(MULTIPART_OBJECT_SIZE)));
      for (int i = 0; i < parts.size() - 1; i++) {
         messageAssembler.add((Frame) parts.get(i));
      }

      boolean thrown = false;
      final TestMessage messageToPartition = new TestMessage(2, 2, TestUtils.makeTestObject(MULTIPART_OBJECT_SIZE));
      try {
         messageAssembler.setParts(new PayloadPartitioner().partition(messageToPartition));
      } catch (final AssertionException e) {
         thrown = true;
      }

      assertTrue(thrown);
   }


   private void partitionAndAssert(final int singlePartObjectSize) throws IOException {

      final TestMessage messageToPartition = new TestMessage(1, 1, TestUtils.makeTestObject(singlePartObjectSize));
      final List parts = new PayloadPartitioner().partition(messageToPartition);
      for (final Object part : parts) {
         messageAssembler.add((Frame) part);
      }
      assertEquals(new AssembledMessageImpl(messageToPartition, -1), messageAssembler.poll());
      assertNull(messageAssembler.poll());
   }


   protected void setUp() throws Exception {

      super.setUp();
      messageAssembler = new MessageAssemblerImpl();
   }


   public String toString() {

      return "MessageAssemblerImplTest{" +
              "messageAssembler=" + messageAssembler +
              "} " + super.toString();
   }
}
