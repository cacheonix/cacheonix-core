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
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.array.HashSet;

/**
 * Tester for AddEntryModifiedSubscriptionAnnouncement.
 */
public final class RemoveEntryModifiedSubscriptionAnnouncementTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "test.cache";

   private static final ClusterNodeAddress BUCKET_OWNER_ADDRESS = TestUtils.createTestAddress(999);

   private static final HashSet<Binary> KEY_SET = createKeySet();  // NOPMD

   private static final int SUBSCRIBER_IDENTITY = 777;

   private RemoveEntryModifiedSubscriptionAnnouncement announcement;


   public void testSetSubscriberIdentity() {

      assertEquals(SUBSCRIBER_IDENTITY, announcement.getSubscriberIdentity());
   }


   public void testDefaultConstructor() {

      assertNotNull(new RemoveEntryModifiedSubscriptionAnnouncement().toString());
   }


   public void testWriteWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(announcement, ser.deserialize(ser.serialize(announcement)));
   }


   public void testToString() {

      assertNotNull(announcement.toString());
   }


   public void testGetWireableType() {

      assertEquals(Wireable.TYPE_UNREGISTER_SUBSCRIPTION_ANNOUNCEMENT, announcement.getWireableType());
   }


   protected void setUp() throws Exception {

      super.setUp();

      announcement = new RemoveEntryModifiedSubscriptionAnnouncement(CACHE_NAME);
      announcement.setBucketOwnerAddress(BUCKET_OWNER_ADDRESS);
      announcement.setSubscriberIdentity(SUBSCRIBER_IDENTITY);
      announcement.setKeySet(KEY_SET);
   }


   private static HashSet<Binary> createKeySet() {  // NOPMD

      final HashSet<Binary> keySet = new HashSet<Binary>(2);
      keySet.add(toBinary("key1"));
      keySet.add(toBinary("key2"));
      return keySet;
   }
}
