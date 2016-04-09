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

import java.util.List;

import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.CollectionUtils;
import junit.framework.TestCase;

/**
 * Tester for TransferBucketResult.
 */
public final class TransferBucketResultTest extends TestCase {


   private TransferBucketResult result;

   private List<Integer> completed;

   private List<Integer> rejected;


   public void testSetGetRejectedBucketNumbers() throws Exception {

      assertEquals(rejected, result.getRejectedBucketNumbers());
   }


   public void testSetGetCompletedBucketNumbers() throws Exception {

      assertEquals(completed, result.getTransferredBucketNumbers());
   }


   public void testGetWireableType() throws Exception {

      assertEquals(Wireable.TYPE_TRANSFER_BUCKET_RESULT, result.getWireableType());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(result, ser.deserialize(ser.serialize(result)));
   }


   public void testEquals() throws Exception {

      final List<Integer> completed1 = CollectionUtils.createList(1);
      final List<Integer> rejected1 = CollectionUtils.createList(2);
      final TransferBucketResult result1 = new TransferBucketResult();
      result1.setTransferredBucketNumbers(completed1);
      result1.setRejectedBucketNumbers(rejected1);

      assertEquals(result, result1);
   }


   public void testNotEquals() throws Exception {

      final List<Integer> completed1 = CollectionUtils.createList(555);
      final List<Integer> rejected1 = CollectionUtils.createList(777);
      final TransferBucketResult result1 = new TransferBucketResult();
      result1.setTransferredBucketNumbers(completed1);
      result1.setRejectedBucketNumbers(rejected1);

      assertFalse(result.equals(result1));
   }


   public void testHashCode() throws Exception {

      assertTrue(result.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(result.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      completed = CollectionUtils.createList(1);
      rejected = CollectionUtils.createList(2);

      result = new TransferBucketResult();
      result.setTransferredBucketNumbers(completed);
      result.setRejectedBucketNumbers(rejected);
   }
}
