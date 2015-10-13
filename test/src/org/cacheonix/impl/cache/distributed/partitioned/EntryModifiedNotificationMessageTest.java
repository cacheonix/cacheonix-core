/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
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

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.impl.cache.subscriber.BinaryEntryModifiedEvent;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * Tester for EntryModifiedNotificationMessage.
 */
public final class EntryModifiedNotificationMessageTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "test.cache";

   private static final int SUBSCRIBER_IDENTITY = 555;

   private EntryModifiedNotificationMessage message;

   private static final List<BinaryEntryModifiedEvent> EVENTS = createEvents();


   public void testSetSubscriberIdentity() throws Exception {

      assertEquals(SUBSCRIBER_IDENTITY, message.getSubscriberIdentity());
   }


   public void testSetEvents() throws Exception {

      assertEquals(EVENTS, message.getEvents());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(message, ser.deserialize(ser.serialize(message)));
   }


   public void testToString() throws Exception {

      assertNotNull(message.toString());
   }


   public void testGetWireableType() {

      assertEquals(Wireable.TYPE_CACHE_ENTRY_MODIFIED_MESSAGE, message.getWireableType());
   }


   private static List<BinaryEntryModifiedEvent> createEvents() {

      final List<BinaryEntryModifiedEvent> events = new ArrayList<BinaryEntryModifiedEvent>(2);
      events.add(new BinaryEntryModifiedEvent(EntryModifiedEventType.ADD, toBinary("key"), toBinary("new value"),
              toBinary("previous value"), new TimeImpl(999L, 0), 1, null));
      events.add(new BinaryEntryModifiedEvent(EntryModifiedEventType.UPDATE, toBinary("key"), toBinary("new value1"),
              toBinary("previous value1"), new TimeImpl(999L, 0), 1, null));
      return events;
   }


   protected void setUp() throws Exception {

      super.setUp();

      message = new EntryModifiedNotificationMessage(CACHE_NAME);
      message.setSubscriberIdentity(SUBSCRIBER_IDENTITY);
      message.setEvents(EVENTS);
   }
}
