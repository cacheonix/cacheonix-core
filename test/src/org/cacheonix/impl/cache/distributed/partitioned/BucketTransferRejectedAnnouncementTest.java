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

import java.io.IOException;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * BucketTransferCompletedAnnouncementTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Nov 9, 2009 7:52:32 PM
 */
public final class BucketTransferRejectedAnnouncementTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketTransferRejectedAnnouncementTest.class); // NOPMD

   private static final String TEST_CACHE = "test.cache";

   private static final ClusterNodeAddress ADDR_1 = TestUtils.createTestAddress(1);

   private static final ClusterNodeAddress ADDR_2 = TestUtils.createTestAddress(2);

   private BucketTransferRejectedAnnouncement announcement;


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      announcement.setSender(ADDR_1);
      announcement.setNewOwner(ADDR_2);
      announcement.setPreviousOwner(ADDR_1);
      announcement.setSourceStorageNumber((byte) 0);
      announcement.setDestinationStorageNumber((byte) 1);
      announcement.setBucketNumbers(CollectionUtils.createList(Integer.valueOf(3)));
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(announcement, ser.deserialize(ser.serialize(announcement)));
   }


   protected void setUp() throws Exception {

      super.setUp();
      announcement = new BucketTransferRejectedAnnouncement(TEST_CACHE);
   }


   public String toString() {

      return "BucketTransferRejectedAnnouncementTest{" +
              "announcement=" + announcement +
              "} " + super.toString();
   }
}