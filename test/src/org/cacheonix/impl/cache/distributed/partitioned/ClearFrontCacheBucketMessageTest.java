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

import java.io.NotSerializableException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * Tester for ClearFrontCacheBucketMessage.
 */
public class ClearFrontCacheBucketMessageTest extends CacheonixTestCase {


   private static final int[] BUCKET_NUMBERS = {1234};

   private ClearFrontCacheBucketMessage message;


   public void testCreate() {

      assertEquals(BUCKET_NUMBERS, message.getBucketNumbers());
   }


   public void testWriteReadWire() throws Exception {

      boolean thrown = false;
      try {
         SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA).serialize(message);
      } catch (final NotSerializableException e) {
         thrown = true;
      }

      assertTrue(thrown);
   }


   public void testHashCode() {

      assertTrue(message.hashCode() != 0);
   }


   public void testToString() {

      assertNotNull(message.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      message = new ClearFrontCacheBucketMessage("test.cache", BUCKET_NUMBERS);
   }


   protected void tearDown() throws Exception {

      message = null;

      super.tearDown();
   }


   public String toString() {

      return "ClearFrontCacheBucketMessageTest{" +
              "message=" + message +
              "} " + super.toString();
   }
}
