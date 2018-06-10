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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BeginBucketTransferMessageTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Dec 18, 2009 9:18:54 PM
 */
public final class BeginBucketTransferMessageTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BeginBucketTransferMessageTest.class); // NOPMD

   private static final byte SOURCE_STORAGE_NUMBER = 2;

   private static final byte DESTINATION_STORAGE_NUMBER = 3;

   private static final ClusterNodeAddress CURRENT_OWNER = TestUtils.createTestAddress(1);

   private static final ClusterNodeAddress NEW_OWNER = TestUtils.createTestAddress(SOURCE_STORAGE_NUMBER);

   private static final Integer BUCKET_NUMBER = Integer.valueOf(5);

   private static final String TEST_CACHE = "test.cache";

   private BeginBucketTransferMessage message;


   /**
    * Tests that no exceptions occur when using a default constructor.
    */
   public void testDefaultConstructor() {

      final BeginBucketTransferMessage newMessage = new BeginBucketTransferMessage();
      assertNotNull(newMessage.toString());
   }


   public void testGetBucketNumber() {

      assertEquals(BUCKET_NUMBER, message.getBucketNumbers().iterator().next());
   }


   public void testGetCurrentOwner() {

      assertEquals(CURRENT_OWNER, message.getCurrentOwner());
   }


   public void testNewOwner() {

      assertEquals(NEW_OWNER, message.getNewOwner());
   }


   public void testGetSourceStorageNumber() {

      assertEquals(SOURCE_STORAGE_NUMBER, message.getSourceStorageNumber());
   }


   public void testGetDestinationStorageNumber() {

      assertEquals(DESTINATION_STORAGE_NUMBER, message.getDestinationStorageNumber());
   }


   public void testGetType() {

      assertEquals(Wireable.TYPE_CACHE_BEGIN_BUCKET_TRANSFER_MESSAGE, message.getWireableType());
   }


   protected void setUp() throws Exception {

      super.setUp();
      message = new BeginBucketTransferMessage(TEST_CACHE);
      message.setBucketNumbers(CollectionUtils.createList(BUCKET_NUMBER));
      message.setCurrentOwner(CURRENT_OWNER);
      message.setNewOwner(NEW_OWNER);
      message.setSourceStorageNumber(SOURCE_STORAGE_NUMBER);
      message.setDestinationStorageNumber(DESTINATION_STORAGE_NUMBER);
   }


   public String toString() {

      return "BeginBucketTransferMessageTest{" +
              "message=" + message +
              "} " + super.toString();
   }
}
