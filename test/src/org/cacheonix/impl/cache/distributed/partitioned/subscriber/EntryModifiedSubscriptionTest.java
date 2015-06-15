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
package org.cacheonix.impl.cache.distributed.partitioned.subscriber;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.array.HashSet;

/**
 * Tester for EntryModifiedSubscription
 */
public final class EntryModifiedSubscriptionTest extends CacheonixTestCase {

   private static final int SUBSCRIBER_IDENTITY = 43093;

   private static final ClusterNodeAddress SUBSCRIBER_ADDRESS = TestUtils.createTestAddress();

   private static final EntryModifiedNotificationMode NOTIFICATION_MODE = EntryModifiedNotificationMode.SINGLE;

   private static final List<EntryModifiedEventContentFlag> EVENT_CONTENT_FLAGS = Collections.singletonList(EntryModifiedEventContentFlag.NEED_KEY);

   private EntryModifiedSubscription subscription = null;

   private static final Set<EntryModifiedEventType> EVENT_TYPES = createEventTypes();


   public void testGetSubscriberAddress() throws Exception {

      assertEquals(SUBSCRIBER_ADDRESS, subscription.getSubscriberAddress());
   }


   public void testGetNotificationMode() throws Exception {

      assertEquals(NOTIFICATION_MODE, subscription.getNotificationMode());
   }


   public void testGetEventFlags() throws Exception {

      assertEquals(Collections.singletonList(EntryModifiedEventContentFlag.NEED_KEY), subscription.getEventContentFlags());
   }


   public void testGetSubscriberIdentity() throws Exception {

      assertEquals(SUBSCRIBER_IDENTITY, subscription.getSubscriberIdentity());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final EntryModifiedSubscription newSubscription = (EntryModifiedSubscription) ser.deserialize(ser.serialize(subscription));
      assertEquals(subscription.getSubscriberAddress(), newSubscription.getSubscriberAddress());
      assertEquals(subscription.getNotificationMode(), newSubscription.getNotificationMode());
      assertEquals(subscription.getEventContentFlags(), newSubscription.getEventContentFlags());
      assertEquals(subscription.getSubscriberIdentity(), newSubscription.getSubscriberIdentity());
   }


   public void testGetWireableType() throws Exception {

      assertEquals(Wireable.TYPE_ENTRY_MODIFICATION_SUBSCRIPTION, subscription.getWireableType());
   }


   public void testEquals() throws Exception {

      assertEquals(subscription, new EntryModifiedSubscription(SUBSCRIBER_IDENTITY, SUBSCRIBER_ADDRESS, NOTIFICATION_MODE, EVENT_CONTENT_FLAGS, EVENT_TYPES));
   }


   private static Set<EntryModifiedEventType> createEventTypes() {

      final Set<EntryModifiedEventType> eventTypes = new HashSet<EntryModifiedEventType>(3, 0.75f);
      eventTypes.add(EntryModifiedEventType.ADD);
      eventTypes.add(EntryModifiedEventType.REMOVE);
      eventTypes.add(EntryModifiedEventType.UPDATE);
      return eventTypes;
   }


   public void testHashCode() throws Exception {

      assertEquals(SUBSCRIBER_IDENTITY, subscription.hashCode());
   }


   public void testToString() {

      assertNotNull(subscription.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      subscription = new EntryModifiedSubscription(SUBSCRIBER_IDENTITY, SUBSCRIBER_ADDRESS, NOTIFICATION_MODE, EVENT_CONTENT_FLAGS, EVENT_TYPES);
   }
}
