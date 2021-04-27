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
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.cacheonix.RequestTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.TestUtils.toInetAddresses;
import static org.mockito.Mockito.when;

/**
 * MulticastMarker Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>03/26/2008</pre>
 */
public final class MulticastMarkerTest extends RequestTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MulticastMarkerTest.class); // NOPMD

   private static final String IP_ADDRESS_AS_STRING = "127.0.0.1";

   private static final InetAddress IP_ADDRESS = IOUtils.getInetAddress(IP_ADDRESS_AS_STRING);

   private static final int TCP_PORT = 9999;

   private static final ClusterNodeAddress JOIN = new ClusterNodeAddress(TCP_PORT, toInetAddresses(IP_ADDRESS));

   private static final ClusterNodeAddress LEAVE = new ClusterNodeAddress(TCP_PORT, toInetAddresses(IP_ADDRESS));

   private static final ClusterNodeAddress ORIGINATOR = new ClusterNodeAddress(TCP_PORT, toInetAddresses(IP_ADDRESS));

   private static final ClusterNodeAddress PREDECESSOR = new ClusterNodeAddress(TCP_PORT, toInetAddresses(IP_ADDRESS));

   private static final int PROFILING_ITERATION_COUNT = 10000;

   private MulticastMarker marker;


   public void testSerialize() throws IOException {

      for (int i = 0; i < PROFILING_ITERATION_COUNT; i++) {
         final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
         final byte[] bytes = ser.serialize(marker);
         assertEquals(203, bytes.length);
      }
   }


   public void testSetGetNextAnnouncementTime() {

      final Time nextAnnouncementTime = clock.currentTime();
      marker.setNextAnnouncementTime(nextAnnouncementTime);
      assertEquals(nextAnnouncementTime, marker.getNextAnnouncementTime());
   }


   public void testSetGetOriginator() {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      marker.setOriginator(clusterNodeAddress);
      assertEquals(clusterNodeAddress, marker.getOriginator());
   }


   public void testSerializeDeserialize() throws IOException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(marker, ser.deserialize(ser.serialize(marker)));
   }


   public void testDefaultConstructor() {

      assertNotNull(new MulticastMarker().toString());
   }


   public void testGetSeqNum() {

      assertEquals(0L, marker.getSeqNum());
   }


   public void testProcessNormal() throws InterruptedException, IOException {

      //
      final Queue<Frame> value = new ArrayBlockingQueue<Frame>(1);
      when(clusterProcessor.getReceivedFrames()).thenReturn(value);

      //
      when(clusterProcessorState.getJoinStatus()).thenReturn(joinStatus);

      //
      marker.setSender(CLUSTER_NODE_ADDRESS);

      marker.processNormal();
   }


   public void testProcessBlocked() {

   }


   public void testProcessCleanup() {

   }


   public void testProcessRecovery() {

   }


   public void setUp() throws Exception {

      super.setUp();


      final JoiningNode joiningNode = new JoiningNode(JOIN);

      marker = new MulticastMarker(CLUSTER_UUID);
      marker.setNextAnnouncementTime(clock.currentTime());
      marker.setOriginator(ORIGINATOR);
      marker.setSeqNum(0L);
      marker.setCurrent(1L);
      marker.setPrevious(2L);
      marker.setJoiningNode(joiningNode);
      marker.setLeave(LEAVE);
      marker.setLeaveSeqNum(1000L);
      marker.setJoinSeqNum(2000L);
      marker.setPredecessor(PREDECESSOR);


      marker.setProcessor(clusterProcessor);
   }
}
