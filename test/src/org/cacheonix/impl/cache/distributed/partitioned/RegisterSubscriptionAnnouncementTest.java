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

import java.util.Collections;
import java.util.Set;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.array.HashSet;

/**
 * Tester for AddEntryModifiedSubscriptionAnnouncement.
 */
public final class RegisterSubscriptionAnnouncementTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "test.cache";

   private static final ClusterNodeAddress BUCKET_OWNER_ADDRESS = TestUtils.createTestAddress(999);

   private static final HashSet<Binary> KEY_SET = createKeySet();  // NOPMD

   private static final Set<EntryModifiedEventType> MODIFICATION_TYPES = createModificationTypes();

   private static final EntryModifiedSubscription SUBSCRIPTION = new EntryModifiedSubscription(5432, TestUtils.createTestAddress(333), EntryModifiedNotificationMode.SINGLE, Collections.singletonList(EntryModifiedEventContentFlag.NEED_ALL), MODIFICATION_TYPES);

   private AddEntryModifiedSubscriptionAnnouncement announcement;


   public void testSetSubscription() throws Exception {

      assertEquals(SUBSCRIPTION, announcement.getSubscription());
   }


   public void testWriteWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(announcement, ser.deserialize(ser.serialize(announcement)));
   }


   public void testToString() throws Exception {

      assertNotNull(announcement.toString());
   }


   public void testGetWireableType() {

      assertEquals(Wireable.TYPE_REGISTER_SUBSCRIPTION_ANNOUNCEMENT, announcement.getWireableType());
   }


   private static Set<EntryModifiedEventType> createModificationTypes() {

      final HashSet<EntryModifiedEventType> eventTypes = new HashSet<EntryModifiedEventType>(4, 0.75f);
      eventTypes.add(EntryModifiedEventType.ADD);
      eventTypes.add(EntryModifiedEventType.EVICT);
      eventTypes.add(EntryModifiedEventType.REMOVE);
      eventTypes.add(EntryModifiedEventType.UPDATE);
      return eventTypes;
   }


   protected void setUp() throws Exception {

      super.setUp();

      announcement = new AddEntryModifiedSubscriptionAnnouncement(CACHE_NAME);
      announcement.setBucketOwnerAddress(BUCKET_OWNER_ADDRESS);
      announcement.setSubscription(SUBSCRIPTION);
      announcement.setKeySet(KEY_SET);
   }


   private static HashSet<Binary> createKeySet() {  // NOPMD

      final HashSet<Binary> keySet = new HashSet<Binary>(2);
      keySet.add(toBinary("key1"));
      keySet.add(toBinary("key2"));
      return keySet;
   }
}
