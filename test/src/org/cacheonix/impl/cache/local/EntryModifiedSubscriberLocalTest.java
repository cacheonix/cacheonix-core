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
package org.cacheonix.impl.cache.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.exceptions.NotSubscribedException;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.configuration.ElementEventNotification;
import org.cacheonix.impl.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.cache.DummyObjectSizeCalculator;

/**
 * Tests {@link EntryModifiedSubscriberLocalTest}
 */
public final class EntryModifiedSubscriberLocalTest extends CacheonixTestCase {

   private static final String KEY = "key";

   private static final String VALUE = "value";

   private static final int MAX_SIZE = 3;

   private static final String VALUE_2 = VALUE + 1;


   private LocalCache<String, String> cache;


   public void testReceivesAll() {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_KEY);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_NEW_VALUE);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags);
      cache.addEventSubscriber(watchKeySet, subscriber);

      // Put first
      cache.put(KEY, VALUE);

      // Assert
      assertEquals(1, subscriber.getReceivedEvents().size());
      assertEquals(EntryModifiedEventType.ADD, subscriber.getReceivedEvents().get(0).getUpdateType());
      assertEquals(KEY, subscriber.getReceivedEvents().get(0).getUpdatedKey());
      assertEquals(VALUE, subscriber.getReceivedEvents().get(0).getNewValue());
      assertNull(subscriber.getReceivedEvents().get(0).getPreviousValue());

      // Update
      cache.put(KEY, VALUE_2);

      // Assert
      assertEquals(2, subscriber.getReceivedEvents().size());
      assertEquals(EntryModifiedEventType.UPDATE, subscriber.getReceivedEvents().get(1).getUpdateType());
      assertEquals(KEY, subscriber.getReceivedEvents().get(1).getUpdatedKey());
      assertEquals(VALUE_2, subscriber.getReceivedEvents().get(1).getNewValue());
      assertEquals(VALUE, subscriber.getReceivedEvents().get(1).getPreviousValue());
   }


   public void testSubscribesSingleKeyReceivesAdd() {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_KEY);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_NEW_VALUE);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE);
      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags);
      cache.addEventSubscriber(KEY, subscriber);

      // Put first
      cache.put(KEY, VALUE);

      // Assert
      while (subscriber.getReceivedEvents().isEmpty()) { // NOPMD
         // Crash-poll to wait for the event
      }
      final List<EntryModifiedEvent> receivedEvents = subscriber.getReceivedEvents();
      assertEquals(1, receivedEvents.size());
      assertEquals(EntryModifiedEventType.ADD, receivedEvents.get(0).getUpdateType());
      assertEquals(KEY, receivedEvents.get(0).getUpdatedKey());
      assertEquals(VALUE, receivedEvents.get(0).getNewValue());
      assertNull(receivedEvents.get(0).getPreviousValue());
   }


   public void testReceivesAllOnRemove() {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_KEY);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_NEW_VALUE);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags);
      cache.addEventSubscriber(watchKeySet, subscriber);

      // Put first
      cache.put(KEY, VALUE);

      // Assert
      assertEquals(1, subscriber.getReceivedEvents().size());
      assertEquals(EntryModifiedEventType.ADD, subscriber.getReceivedEvents().get(0).getUpdateType());
      assertEquals(KEY, subscriber.getReceivedEvents().get(0).getUpdatedKey());
      assertEquals(VALUE, subscriber.getReceivedEvents().get(0).getNewValue());
      assertNull(subscriber.getReceivedEvents().get(0).getPreviousValue());

      // Update
      cache.remove(KEY);

      // Assert
      assertEquals(2, subscriber.getReceivedEvents().size());
      assertEquals(EntryModifiedEventType.REMOVE, subscriber.getReceivedEvents().get(1).getUpdateType());
      assertEquals(KEY, subscriber.getReceivedEvents().get(1).getUpdatedKey());
      assertEquals(VALUE, subscriber.getReceivedEvents().get(1).getPreviousValue());
      assertNull(subscriber.getReceivedEvents().get(1).getNewValue());
   }


   public void testReceivesKeyOnly() {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_KEY);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags);
      cache.addEventSubscriber(watchKeySet, subscriber);

      // Put first
      cache.put(KEY, VALUE);

      // Assert
      assertEquals(1, subscriber.getReceivedEvents().size());
      assertEquals(EntryModifiedEventType.ADD, subscriber.getReceivedEvents().get(0).getUpdateType());
      assertEquals(KEY, subscriber.getReceivedEvents().get(0).getUpdatedKey());
      assertNull(subscriber.getReceivedEvents().get(0).getNewValue());
      assertNull(subscriber.getReceivedEvents().get(0).getPreviousValue());

      // Update
      cache.put(KEY, VALUE_2);

      // Assert
      assertEquals(2, subscriber.getReceivedEvents().size());
      assertEquals(EntryModifiedEventType.UPDATE, subscriber.getReceivedEvents().get(1).getUpdateType());
      assertEquals(KEY, subscriber.getReceivedEvents().get(1).getUpdatedKey());
      assertNull(subscriber.getReceivedEvents().get(1).getNewValue());
      assertNull(subscriber.getReceivedEvents().get(1).getPreviousValue());
   }


   public void testReceivesPreviousValueOnly() {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags);
      cache.addEventSubscriber(watchKeySet, subscriber);

      // Put first
      cache.put(KEY, VALUE);

      // Assert
      assertEquals(1, subscriber.getReceivedEvents().size());
      assertEquals(EntryModifiedEventType.ADD, subscriber.getReceivedEvents().get(0).getUpdateType());
      assertNull(subscriber.getReceivedEvents().get(0).getUpdatedKey());
      assertNull(subscriber.getReceivedEvents().get(0).getNewValue());
      assertNull(subscriber.getReceivedEvents().get(0).getPreviousValue());

      // Update
      cache.put(KEY, VALUE_2);

      // Assert
      assertEquals(2, subscriber.getReceivedEvents().size());
      assertEquals(EntryModifiedEventType.UPDATE, subscriber.getReceivedEvents().get(1).getUpdateType());
      assertNull(KEY, subscriber.getReceivedEvents().get(1).getUpdatedKey());
      assertNull(subscriber.getReceivedEvents().get(1).getNewValue());
      assertEquals(VALUE, subscriber.getReceivedEvents().get(1).getPreviousValue());
   }


   public void testUnSubscribe() {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_KEY);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_NEW_VALUE);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags);
      cache.addEventSubscriber(watchKeySet, subscriber);

      // Put first to populate element's reference to the subscriber
      cache.put(KEY, VALUE);

      // Assert
      assertEquals(1, subscriber.getReceivedEvents().size());

      // Remove subscriber
      cache.removeEventSubscriber(watchKeySet, subscriber);

      // Update
      cache.put(KEY, VALUE_2);

      // Assert has still one element
      assertEquals(1, subscriber.getReceivedEvents().size());
   }


   public void testDoubleUnSubscribe() {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_KEY);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_NEW_VALUE);
      contentFlags.add(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags);
      cache.addEventSubscriber(watchKeySet, subscriber);

      // Remove subscriber first time
      cache.removeEventSubscriber(watchKeySet, subscriber);

      // Remove subscriber second time
      boolean thrown = false;
      try {

         cache.removeEventSubscriber(watchKeySet, subscriber);
      } catch (final NotSubscribedException ignored) {

         thrown = true;
      }
      assertTrue("Exception should be thrown", thrown);
   }


   protected void setUp() throws Exception {

      super.setUp();
      cache = new LocalCache<String, String>(TestConstants.LOCAL_TEST_CACHE, MAX_SIZE, 0, 0, 0,
              getClock(), getEventNotificationExecutor(), new DummyDiskStorage(TestConstants.LOCAL_TEST_CACHE),
              new DummyObjectSizeCalculator(), new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);

   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      cache.shutdown();
      super.tearDown();
   }


   public String toString() {

      return "EntryModifiedSubscriberLocalTest{" +
              "cache=" + cache +
              "} " + super.toString();
   }


   private static class TestEntryModifiedSubscriber implements EntryModifiedSubscriber {

      private final List<EntryModifiedEvent> receivedEvents = new ArrayList<EntryModifiedEvent>(1);

      private final List<EntryModifiedEventContentFlag> contentFlags;


      public TestEntryModifiedSubscriber(final List<EntryModifiedEventContentFlag> contentFlags) {

         //noinspection AssignmentToCollectionOrArrayFieldFromParameter
         this.contentFlags = contentFlags;
      }


      public void notifyKeysUpdated(final List<EntryModifiedEvent> events) {

         receivedEvents.addAll(events);
      }


      public EntryModifiedNotificationMode getNotificationMode() {

         return EntryModifiedNotificationMode.SINGLE;
      }


      public Set<EntryModifiedEventType> getModificationTypes() {

         final HashSet<EntryModifiedEventType> eventTypes = new HashSet<EntryModifiedEventType>(4, 0.75f);
         eventTypes.add(EntryModifiedEventType.ADD);
         eventTypes.add(EntryModifiedEventType.EVICT);
         eventTypes.add(EntryModifiedEventType.REMOVE);
         eventTypes.add(EntryModifiedEventType.UPDATE);

         return eventTypes;
      }


      public List<EntryModifiedEventContentFlag> getEventContentFlags() {

         return contentFlags;
      }


      public List<EntryModifiedEvent> getReceivedEvents() {

         return new ArrayList<EntryModifiedEvent>(receivedEvents);
      }


      public String toString() {

         return "TestEntryModifiedSubscriber{" +
                 "receivedEvents=" + receivedEvents +
                 ", contentFlags=" + contentFlags +
                 '}';
      }
   }
}
