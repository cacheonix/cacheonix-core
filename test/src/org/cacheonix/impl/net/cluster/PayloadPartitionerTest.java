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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import junit.framework.TestCase;

/**
 * PayloadPartitioner Tester.
 *
 * @noinspection JUnitTestMethodWithNoAssertions
 * @since <pre>03/28/2008</pre>
 */
public final class PayloadPartitionerTest extends TestCase {

   private static final int MULTIPART_OBJECT_SIZE = 5000;

   private static final int SINGLE_PART_OBJECT_SIZE = 10;

   private PayloadPartitioner partitioner;


   public PayloadPartitionerTest(final String name) {

      super(name);
   }


   public void testPartitionMultipart() throws IOException, ClassNotFoundException {

      runPartitionTest(MULTIPART_OBJECT_SIZE, 4);
   }


   public void testPartitionNull() {

      final List list = partitioner.partition(null);
      assertTrue(list.isEmpty());
   }


   public void test() throws IOException, ClassNotFoundException {

      runPartitionTest(SINGLE_PART_OBJECT_SIZE, 1);
   }


   void runPartitionTest(final int objectSize, final int expectedPartCount)
           throws IOException, ClassNotFoundException {

      final TestMessage objectToPartition = makeObject(objectSize);
      final List parts = partitioner.partition(objectToPartition);
      assertEquals(expectedPartCount, parts.size());

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(MULTIPART_OBJECT_SIZE);
      for (int i = 0; i < parts.size(); i++) {
         final Frame frame = (Frame) parts.get(i);
         assertEquals(expectedPartCount, frame.getPartCount());
         assertEquals(i, frame.getPartIndex());
         assertEquals(-1L, frame.getSequenceNumber());
         assertEquals(Serializer.TYPE_JAVA, frame.getSerializerType());
         if (i < parts.size() - 1) {
            assertEquals(Frame.MAXIMUM_MCAST_PAYLOAD_LENGTH, frame.getPayload().length);
            // control call to make sure no exceptions thrown
            assertEquals(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH, frame.toBytes().length);
         } else {
            assertTrue(frame.getPayload().length < Frame.MAXIMUM_MCAST_PAYLOAD_LENGTH);
         }
         baos.write(frame.getPayload());
      }

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(objectToPartition, serializer.deserialize(baos.toByteArray()));
   }


   private static TestMessage makeObject(final int size) {

      return new TestMessage(1, 1, TestUtils.makeTestObject(size));
   }


   protected void setUp() throws Exception {

      super.setUp();
      partitioner = new PayloadPartitioner();
   }


   public String toString() {

      return "PayloadPartitionerTest{" +
              "partitioner=" + partitioner +
              "} " + super.toString();
   }
}
