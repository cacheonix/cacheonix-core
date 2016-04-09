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
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * Tester for AggregatingAnnouncementResponse.
 */
public final class AggregatingAnnouncementResponseTest extends CacheonixTestCase {

   private static final int[] PROCESSED_BUCKETS = createProcessedBuckets();

   private AggregatingAnnouncementResponse response;


   public void testHandOffRejectedBuckets() throws Exception {

      assertEquals(PROCESSED_BUCKETS, response.handOffProcessedBuckets());
      assertTrue(response.isProcessedBucketsEmpty());
   }


   public void testIsRejectedBucketsEmpty() throws Exception {

      response.handOffProcessedBuckets();
      assertTrue(response.isProcessedBucketsEmpty());
   }


   public void testSetRejectedBuckets() throws Exception {

      assertEquals(PROCESSED_BUCKETS, response.handOffProcessedBuckets());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(response, ser.deserialize(ser.serialize(response)));
   }


   public void testToString() throws Exception {

      assertNotNull(response.toString());
   }


   public void testHashCode() throws Exception {

      assertTrue(response.hashCode() != 0);
   }


   protected void setUp() throws Exception {

      super.setUp();

      response = new AggregatingAnnouncementResponse();
      response.setProcessedBuckets(PROCESSED_BUCKETS);
   }


   private static int[] createProcessedBuckets() {

      return new int[]{1, 2};
   }
}
