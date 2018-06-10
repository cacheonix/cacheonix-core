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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.multicast.sender.MulticastSender;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * ClusterAnnouncer Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @noinspection ParameterHidesMemberVariable
 * @since <pre>04/17/2008</pre>
 */
public final class ClusterAnnouncerTest extends CacheonixTestCase {

   private static final String TEST_CLUSTER_NAME = "Test Cluster Name";

   private ClusterAnnouncer announcer;

   private TestMulticastSender multicastSender;

   private ClusterNodeAddress clusterNodeAddress;


   public void testToString() {

      assertNotNull(announcer.toString());
   }


   public void testAnnounce() throws IOException {

      announcer.announce(UUID.randomUUID(), TestUtils.createTestAddress(TestConstants.PORT_7676 + 1), 2, true);

      final Frame frame = multicastSender.getFrame();
      assertEquals(-1L, frame.getSequenceNumber());
      assertEquals(1, frame.getPartCount());
      assertEquals(0, frame.getPartIndex());
      final Serializer ser = SerializerFactory.getInstance().getSerializer(frame.getSerializerType());
      final ClusterAnnouncement o = (ClusterAnnouncement) ser.deserialize(frame.getPayload());
      assertTrue(o.isOperationalCluster());

      assertEquals(clusterNodeAddress, o.getSender());
   }


   protected void setUp() throws Exception {

      super.setUp();
      multicastSender = new TestMulticastSender();
      clusterNodeAddress = TestUtils.createTestAddress(TestConstants.PORT_7676);
      announcer = new ClusterAnnouncer(getClock(), multicastSender, TEST_CLUSTER_NAME, clusterNodeAddress);
   }


   private static final class TestMulticastSender implements MulticastSender {

      private Frame frame = null;


      public final void sendFrame(final Frame frame) {

         this.frame = frame;
      }


      final Frame getFrame() {

         return frame;
      }


      public final String toString() {

         return "TestMulticastSender{" +
                 "frame=" + frame +
                 '}';
      }
   }


   public String toString() {

      return "ClusterAnnouncerTest{" +
              "announcer=" + announcer +
              ", clusterNodeAddress=" + clusterNodeAddress +
              ", multicastSender=" + multicastSender +
              "} " + super.toString();
   }
}
