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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * Tester for ClearFrontCacheBucketAnnouncement
 */
public final class ClearFrontCacheBucketAnnouncementTest extends CacheonixTestCase {


   private static final String TEST_CACHE = "test.cache";

   private static final int BUCKET_NUMBER = 1234;

   private ClearFrontCacheBucketAnnouncement announcement;


   public void testCreate() {

      assertEquals(new int[]{BUCKET_NUMBER}, announcement.getBucketNumbers());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(announcement, ser.deserialize(ser.serialize(announcement)));
   }


   public void testHashCode() throws Exception {

      assertTrue(announcement.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(announcement.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      announcement = new ClearFrontCacheBucketAnnouncement(TEST_CACHE, BUCKET_NUMBER);
   }


   protected void tearDown() throws Exception {

      announcement = null;

      super.tearDown();
   }


   public String toString() {

      return "ClearFrontCacheBucketAnnouncementTest{" +
              "announcement=" + announcement +
              "} " + super.toString();
   }
}
