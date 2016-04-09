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
package org.cacheonix.impl.lock;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * Tester for LockQueueKey.
 */
public final class LockQueueKeyTest extends CacheonixTestCase {

   private static final String TEST_REGION = "test.region";

   private static final String KEY = "key";

   private LockQueueKey lockQueueKey;


   public void testGetLockRegionName() throws Exception {

      assertEquals(TEST_REGION, lockQueueKey.getLockRegionName());
   }


   public void testGetLockKey() throws Exception {

      assertEquals(toBinary(KEY), lockQueueKey.getLockKey());
   }


   public void testSerializeDeserialize() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(lockQueueKey, ser.deserialize(ser.serialize(lockQueueKey)));
   }


   public void testGetWireableType() throws Exception {

      assertEquals(Wireable.TYPE_LOCK_QUEUE_KEY, lockQueueKey.getWireableType());
   }


   public void testEquals() throws Exception {

      assertEquals(lockQueueKey, new LockQueueKey(TEST_REGION, toBinary(KEY)));
   }


   public void testHashCode() throws Exception {

      assertTrue(lockQueueKey.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(lockQueueKey.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      lockQueueKey = new LockQueueKey(TEST_REGION, toBinary(KEY));
   }
}
