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
package org.cacheonix.impl.cluster.node.state.group;

import java.io.IOException;
import java.util.Collections;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.distributed.partitioned.BucketTransferCompletedAnnouncement;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.logging.Logger;

/**
 * AnnounceBucketOwnerMessageTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 14, 2009 1:48:30 AM
 */
public final class BucketTransferCompletedAnnouncementTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketTransferCompletedAnnouncementTest.class); // NOPMD

   private static final int BUCKET_NUMBER = 2;

   private static final byte SOURCE_STORAGE_NUMBER = 3;

   private static final byte DESTINATION_STORAGE_NUMBER = 4;

   private static final ClusterNodeAddress SENDER = TestUtils.createTestAddress();

   private static final ClusterNodeAddress NEW_OWNER = TestUtils.createTestAddress();

   private static final String CACHE_NAME = "my.cache";

   private BucketTransferCompletedAnnouncement message;


   public void testSetGetNewOwner() {

      assertEquals(NEW_OWNER, message.getNewOwnerAddress());
   }


   public void testSetGetSourceStorageNumber() {

      message.setSourceStorageNumber(SOURCE_STORAGE_NUMBER);
      assertEquals(SOURCE_STORAGE_NUMBER, message.getSourceStorageNumber());
   }


   public void testSetGetDestinationStorageNumber() {

      message.setDestinationStorageNumber(DESTINATION_STORAGE_NUMBER);
      assertEquals(DESTINATION_STORAGE_NUMBER, message.getDestinationStorageNumber());
   }


   public void testHashCode() {

      assertTrue(message.hashCode() != 0);
   }


   public void testToString() {

      assertNotNull(message.toString());
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      message.addTransferredBucketNumbers(Collections.singletonList(BUCKET_NUMBER));
      message.setSourceStorageNumber(SOURCE_STORAGE_NUMBER);
      message.setDestinationStorageNumber(DESTINATION_STORAGE_NUMBER);
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(message, ser.deserialize(ser.serialize(message)));
   }


   protected void setUp() throws Exception {

      super.setUp();
      message = new BucketTransferCompletedAnnouncement(CACHE_NAME);
      message.setSender(SENDER);
      message.setNewOwnerAddress(NEW_OWNER);
      message.setPreviousOwnerAddress(SENDER);
   }


   public String toString() {

      return "BucketTransferCompletedAnnouncementTest{" +
              "message=" + message +
              "} " + super.toString();
   }
}
