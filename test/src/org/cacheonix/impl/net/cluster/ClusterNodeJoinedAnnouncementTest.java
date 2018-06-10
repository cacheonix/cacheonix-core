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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * Tester for ClusterNodeJoinedAnnouncement.
 */
public final class ClusterNodeJoinedAnnouncementTest extends CacheonixTestCase {

   private static final ClusterNodeAddress SENDER = TestUtils.createTestAddress(1);

   private static final ClusterNodeAddress JOIN = TestUtils.createTestAddress(1);

   private ClusterNodeJoinedAnnouncement message;


   public void testSetJoin() {

      assertEquals(JOIN, message.getJoined());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] serialized = ser.serialize(message);
      assertEquals(87, serialized.length);
      assertEquals(message, ser.deserialize(serialized));
   }


   public void testToString() {

      assertNotNull(message.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      message = new ClusterNodeJoinedAnnouncement();
      message.setTimestamp(getClock().currentTime());
      message.setSender(SENDER);
      message.setJoined(JOIN);
   }


   public String toString() {

      return "ClusterNodeJoinedAnnouncementTest{" +
              "message=" + message +
              "} " + super.toString();
   }
}
