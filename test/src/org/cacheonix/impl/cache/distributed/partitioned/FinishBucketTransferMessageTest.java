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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * ChangeBucketOwnershipMessageTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection ConstantNamingConvention
 * @since Aug 14, 2009 3:10:23 PM
 */
public final class FinishBucketTransferMessageTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(FinishBucketTransferMessageTest.class); // NOPMD

   private static final ClusterNodeAddress ADDR_1 = TestUtils.createTestAddress(1);

   private static final ClusterNodeAddress ADDR_2 = TestUtils.createTestAddress(2);

   private static final ClusterNodeAddress ADDR_3 = TestUtils.createTestAddress(3);

   private static final String CACHE_NAME = "my.cache";

   private static final int BUCKET_NUMBER = 1;

   private static final byte SOURCE_STORAGE_NUMBER = 2;

   private static final byte DESTINATION_STORAGE_NUMBER = 2;

   private FinishBucketTransferMessage message;


   public void testSetGetBucketNumber() {

      final List bucketNumbers = new ArrayList(11);
      bucketNumbers.add(Integer.valueOf(BUCKET_NUMBER));
      message.setBucketNumbers(bucketNumbers);
      assertEquals(bucketNumbers, message.getBucketNumbers());
   }


   public void testSetGetNewOwner() {

      final ClusterNodeAddress newOwner = ADDR_1;
      message.setNewOwner(newOwner);
      assertEquals(newOwner, message.getNewOwner());
   }


   public void testSetGetPreviousOwner() {

      final ClusterNodeAddress prevOwner = ADDR_2;
      message.setPreviousOwner(prevOwner);
      assertEquals(prevOwner, message.getPreviousOwner());
   }


   public void testSetGetSourceStorageNumber() {

      message.setSourceStorageNumber(SOURCE_STORAGE_NUMBER);
      assertEquals(SOURCE_STORAGE_NUMBER, message.getSourceStorageNumber());
   }


   public void testSetGetDestinationStorageNumber() {

      message.setSourceStorageNumber(DESTINATION_STORAGE_NUMBER);
      assertEquals(DESTINATION_STORAGE_NUMBER, message.getSourceStorageNumber());
   }


   public void testToString() {

      message.setPreviousOwner(ADDR_1);
      message.setNewOwner(ADDR_2);
      message.setSender(ADDR_3);
      message.setReceiver(ADDR_3);
      assertNotNull(message.toString());
   }


   public void testCreate() {

      assertEquals(-1, message.getSourceStorageNumber());
      assertEquals(0, message.getBucketNumbers().size());
      assertNull(message.getNewOwner());
   }


   protected void setUp() throws Exception {

      super.setUp();
      message = new FinishBucketTransferMessage(CACHE_NAME);
   }


   public String toString() {

      return "FinishBucketTransferMessageTest{" +
              "message=" + message +
              "} " + super.toString();
   }
}
