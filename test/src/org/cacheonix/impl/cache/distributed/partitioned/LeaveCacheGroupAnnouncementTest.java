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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import junit.framework.TestCase;

/**
 * Tester for LeaveCacheGroupAnnouncement.
 */
public final class LeaveCacheGroupAnnouncementTest extends TestCase {

   private static final ClusterNodeAddress LEAVING_ADDRESS = TestUtils.createTestAddress(1);

   private static final String TEST_CACHE = "test.cache";

   private LeaveCacheGroupAnnouncement announcement = null;


   public void testDefaultConstructor() throws Exception {

      assertNotNull(new LeaveCacheGroupAnnouncement().toString());
   }


   public void testGetLeavingAddress() throws Exception {

      assertEquals(LEAVING_ADDRESS, announcement.getLeavingAddress());
   }


   public void testIsGracefulLeave() throws Exception {

      assertFalse(announcement.isGracefulLeave());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(announcement, ser.deserialize(ser.serialize(announcement)));
   }


   protected void setUp() throws Exception {

      super.setUp();
      announcement = new LeaveCacheGroupAnnouncement(TEST_CACHE, LEAVING_ADDRESS, false);
   }


   public String toString() {

      return "LeaveCacheGroupAnnouncementTest{" +
              "announcement=" + announcement +
              "} " + super.toString();
   }
}
