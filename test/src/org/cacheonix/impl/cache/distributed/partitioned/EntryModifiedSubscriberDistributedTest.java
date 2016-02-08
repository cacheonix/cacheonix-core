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
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.NotSubscribedException;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.cacheonix.ShutdownMode.FORCED_SHUTDOWN;
import static org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag.NEED_ALL;
import static org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag.NEED_KEY;
import static org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag.NEED_NEW_VALUE;
import static org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE;
import static org.cacheonix.cache.subscriber.EntryModifiedEventType.ADD;
import static org.cacheonix.cache.subscriber.EntryModifiedEventType.EVICT;
import static org.cacheonix.cache.subscriber.EntryModifiedEventType.REMOVE;
import static org.cacheonix.cache.subscriber.EntryModifiedEventType.UPDATE;

/**
 * Tests EntryModifiedSubscriber in the distributed cache environment.
 *
 * @see EntryModifiedSubscriber
 */
public final class EntryModifiedSubscriberDistributedTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EntryModifiedSubscriberDistributedTest.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster.
    */
   private static final String[] CACHEONIX_CONFIGURATIONS = {
           "cacheonix-config-cluster-member-1.xml",
           "cacheonix-config-cluster-member-2.xml",
           "cacheonix-config-cluster-member-3.xml"
   };

   private static final String DISTRIBUTED_CACHE_NAME = "partitioned.distributed.cache";

   private static final String KEY = "key";

   private static final String KEY_PREFIX = "key_";

   private static final String VALUE = "value";

   private static final String VALUE_2 = VALUE + 1;

   /**
    * List of cache managers.
    */
   private final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);

   /**
    * List of clustered caches.
    */
   private final List<Cache<String, String>> cacheList = new ArrayList<Cache<String, String>>(5);


   public void testReceivesAdd() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 1);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Put first
      getCache(0).put(KEY, VALUE);

      // Assert
      final List<EntryModifiedEvent> receivedEvents = subscriber.getReceivedEvents();
      assertEquals(1, receivedEvents.size());
      assertEquals(ADD, receivedEvents.get(0).getUpdateType());
      assertEquals(KEY, receivedEvents.get(0).getUpdatedKey());
      assertEquals(VALUE, receivedEvents.get(0).getNewValue());
      assertNull(receivedEvents.get(0).getPreviousValue());
   }


   public void testSubscribesSingleKeyReceivesAdd() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);
      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 1);
      getCache(0).addEventSubscriber(KEY, subscriber);

      // Put first
      getCache(0).put(KEY, VALUE);

      // Assert
      final List<EntryModifiedEvent> receivedEvents = subscriber.getReceivedEvents();
      assertEquals(1, receivedEvents.size());
      assertEquals(ADD, receivedEvents.get(0).getUpdateType());
      assertEquals(KEY, receivedEvents.get(0).getUpdatedKey());
      assertEquals(VALUE, receivedEvents.get(0).getNewValue());
      assertNull(receivedEvents.get(0).getPreviousValue());
   }


   public void testReceivesAddAll() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_ALL);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 1);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Put first
      getCache(0).put(KEY, VALUE);

      // Assert
      final List<EntryModifiedEvent> receivedEvents = subscriber.getReceivedEvents();
      assertEquals(1, receivedEvents.size());
      assertEquals(ADD, receivedEvents.get(0).getUpdateType());
      assertEquals(KEY, receivedEvents.get(0).getUpdatedKey());
      assertEquals(VALUE, receivedEvents.get(0).getNewValue());
      assertNull(receivedEvents.get(0).getPreviousValue());
   }


   public void testReceivesAddForKeySet() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final int keyCount = 5000; // High enough to cover all buckets at least once
      final Set<String> watchKeySet = new HashSet<String>(1);
      for (int i = 0; i < keyCount; i++) {

         watchKeySet.add(KEY_PREFIX + Integer.toString(i));
      }

      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, keyCount);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Put first
      final HashMap<String, String> map = new HashMap<String, String>(keyCount);
      for (final String key : watchKeySet) {

         map.put(key, VALUE);
      }
      getCache(0).putAll(map);

      // Assert
      final List<EntryModifiedEvent> receivedEvents = subscriber.getReceivedEvents();
      assertEquals(keyCount, receivedEvents.size());

      // Confirm no key duplicates
      final List<EntryModifiedEvent> receivedEvents2 = subscriber.getReceivedEvents();
      for (final EntryModifiedEvent event : receivedEvents) {

         int count = 0;
         for (final EntryModifiedEvent event2 : receivedEvents2) {

            if (event.getUpdatedKey().equals(event2.getUpdatedKey())) {

               count++;
            }
         }
         assertEquals(1, count);
      }
   }


   public void testReceivesUpdateForKeySetWhenABucketOwnerIsGone() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final int keyCount = 5000; // High enough to cover all buckets at least once
      final Set<String> watchKeySet = new HashSet<String>(1);
      for (int i = 0; i < keyCount; i++) {

         watchKeySet.add(KEY_PREFIX + Integer.toString(i));
      }

      // Put first
      final HashMap<String, String> initialContent = new HashMap<String, String>(keyCount);
      for (final String key : watchKeySet) {

         initialContent.put(key, VALUE);
      }
      LOG.debug("================================================================================================");
      LOG.debug("============= Populate =========================================================================");
      LOG.debug("================================================================================================");

      // Populate
      getCache(0).putAll(initialContent);


      // Add subscriber
      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, keyCount);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);


      //
      // Shutdown forcibly
      //
      cacheManagerList.get(1).shutdown(FORCED_SHUTDOWN, true);


      //
      // Do updates
      //

      LOG.debug("================================================================================================");
      LOG.debug("=============== Update =========================================================================");
      LOG.debug("================================================================================================");

      // Update
      final HashMap<String, String> updateContent = new HashMap<String, String>(keyCount);
      for (final String key : watchKeySet) {

         updateContent.put(key, VALUE + "_new value");
      }
      getCache(0).putAll(updateContent);

      // Assert
      final List<EntryModifiedEvent> receivedEvents = subscriber.getReceivedEvents();
      assertEquals(keyCount, receivedEvents.size());
   }


   public void testSubscriberIsGone() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final int keyCount = 5000; // High enough to cover all buckets at least once
      final Set<String> watchKeySet = new HashSet<String>(1);
      for (int i = 0; i < keyCount; i++) {

         watchKeySet.add(KEY_PREFIX + Integer.toString(i));
      }

      // Put first
      final HashMap<String, String> initialContent = new HashMap<String, String>(keyCount);
      for (final String key : watchKeySet) {

         initialContent.put(key, VALUE);
      }
      LOG.debug("================================================================================================");
      LOG.debug("============= Populate =========================================================================");
      LOG.debug("================================================================================================");

      getCache(0).putAll(initialContent);


      final TestEntryModifiedSubscriber subscriber0 = new TestEntryModifiedSubscriber(contentFlags, keyCount);
      getCache(0).addEventSubscriber(watchKeySet, subscriber0);


      LOG.debug("================================================================================================");
      LOG.debug("=============== Shutdown =======================================================================");
      LOG.debug("================================================================================================");

      // Shutdown forcibly
      cacheManagerList.get(0).shutdown(FORCED_SHUTDOWN, true);

      LOG.debug("================================================================================================");
      LOG.debug("=============== Subscribe ======================================================================");
      LOG.debug("================================================================================================");

      // Add subscriber
      final TestEntryModifiedSubscriber subscriber1 = new TestEntryModifiedSubscriber(contentFlags, keyCount);
      getCache(1).addEventSubscriber(watchKeySet, subscriber1);


      // Do updates
      LOG.debug("================================================================================================");
      LOG.debug("=============== Update =========================================================================");
      LOG.debug("================================================================================================");

      // Update
      final HashMap<String, String> updateContent = new HashMap<String, String>(keyCount);
      for (final String key : watchKeySet) {

         updateContent.put(key, VALUE + "_new value");
      }
      getCache(1).putAll(updateContent);
      assertEquals(updateContent, getCache(1).getAll(watchKeySet));

      // Assert
      final List<EntryModifiedEvent> receivedEvents = subscriber1.getReceivedEvents();
      assertEquals(keyCount, receivedEvents.size());
   }


   public void testReceivesUpdate() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 2);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      LOG.debug("================================================================================================");
      LOG.debug("========== Put 1 =========================================================================");
      LOG.debug("================================================================================================");

      // Put (add)
      getCache(0).put(KEY, VALUE);

      LOG.debug("================================================================================================");
      LOG.debug("========== Put 2 =========================================================================");
      LOG.debug("================================================================================================");

      // Update
      getCache(0).put(KEY, VALUE_2);

      // Assert
      final EntryModifiedEvent updateEvent = subscriber.getReceivedEvents().get(1);
      assertEquals(2, subscriber.getReceivedEvents().size());
      assertEquals(UPDATE, updateEvent.getUpdateType());
      assertEquals(KEY, updateEvent.getUpdatedKey());
      assertEquals(VALUE_2, updateEvent.getNewValue());
      assertEquals(VALUE, updateEvent.getPreviousValue());
   }


   public void testReceivesKeyOnlyAdd() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 1);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Put first
      getCache(0).put(KEY, VALUE);

      // Assert
      assertEquals(1, subscriber.getReceivedEvents().size());
      assertEquals(ADD, subscriber.getReceivedEvents().get(0).getUpdateType());
      assertEquals(KEY, subscriber.getReceivedEvents().get(0).getUpdatedKey());
      assertNull(subscriber.getReceivedEvents().get(0).getNewValue());
      assertNull(subscriber.getReceivedEvents().get(0).getPreviousValue());
   }


   public void testReceivesKeyOnlyUpdate() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 2);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Put first (add)
      getCache(0).put(KEY, VALUE);

      // Update
      getCache(0).put(KEY, VALUE_2);

      // Assert
      final EntryModifiedEvent updateEvent = subscriber.getReceivedEvents().get(1);
      assertEquals(2, subscriber.getReceivedEvents().size());
      assertEquals(UPDATE, updateEvent.getUpdateType());
      assertEquals(KEY, updateEvent.getUpdatedKey());
      assertNull(updateEvent.getNewValue());
      assertNull(updateEvent.getPreviousValue());
   }


   public void testReceivesPreviousValueOnlyAdd() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 1);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Put first (add)
      getCache(0).put(KEY, VALUE);

      // Assert
      assertEquals(1, subscriber.getReceivedEvents().size());
      assertEquals(ADD, subscriber.getReceivedEvents().get(0).getUpdateType());
      assertNull(subscriber.getReceivedEvents().get(0).getUpdatedKey());
      assertNull(subscriber.getReceivedEvents().get(0).getNewValue());
      assertNull(subscriber.getReceivedEvents().get(0).getPreviousValue());
   }


   public void testReceivesPreviousValueOnlyUpdate() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 2);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Put first (add)
      getCache(0).put(KEY, VALUE);

      // Update
      getCache(0).put(KEY, VALUE_2);

      // Assert
      assertEquals(2, subscriber.getReceivedEvents().size());
      final EntryModifiedEvent updateEvent = subscriber.getReceivedEvents().get(1);
      assertEquals(UPDATE, updateEvent.getUpdateType());
      assertNull(KEY, updateEvent.getUpdatedKey());
      assertNull(updateEvent.getNewValue());
      assertEquals(VALUE, updateEvent.getPreviousValue());
   }


   public void testUnSubscribe() throws InterruptedException {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 1);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Put first to populate element's reference to the subscriber
      getCache(0).put(KEY, VALUE);

      // Assert
      assertEquals(1, subscriber.getReceivedEvents().size());

      // Remove subscriber
      getCache(0).removeEventSubscriber(watchKeySet, subscriber);

      // Update
      getCache(0).put(KEY, VALUE_2);

      // Assert has still one element
      assertEquals(1, subscriber.getReceivedEvents().size());
   }


   public void testDoubleUnSubscribe() {

      // Create subscriber
      final List<EntryModifiedEventContentFlag> contentFlags = new ArrayList<EntryModifiedEventContentFlag>(4);
      contentFlags.add(NEED_KEY);
      contentFlags.add(NEED_NEW_VALUE);
      contentFlags.add(NEED_PREVIOUS_VALUE);

      final Set<String> watchKeySet = new HashSet<String>(1);
      watchKeySet.add(KEY);


      final TestEntryModifiedSubscriber subscriber = new TestEntryModifiedSubscriber(contentFlags, 1);
      getCache(0).addEventSubscriber(watchKeySet, subscriber);

      // Remove subscriber first time
      getCache(0).removeEventSubscriber(watchKeySet, subscriber);

      // Remove subscriber second time
      boolean thrown = false;
      try {
         getCache(0).removeEventSubscriber(watchKeySet, subscriber);
      } catch (final NotSubscribedException e) {
         thrown = true;
      }
      assertTrue("Exception should be thrown", thrown);
   }


   Cache<String, String> getCache(final int cacheIndex) {

      return cacheList.get(cacheIndex);
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      LOG.debug("================================================================================================");
      LOG.debug("========== Starting up =========================================================================");
      LOG.debug("================================================================================================");
      super.setUp();

      for (int i = 0; i < CACHEONIX_CONFIGURATIONS.length; i++) {

         final String configurationPath = TestUtils.getTestFile(CACHEONIX_CONFIGURATIONS[i]).toString();
         final Cacheonix manager = Cacheonix.getInstance(configurationPath);
         cacheManagerList.add(manager);
         @SuppressWarnings("unchecked")
         final Cache<String, String> cache = manager.getCache(DISTRIBUTED_CACHE_NAME);
         assertNotNull("Cache " + i + " should be not null", cache);
         cacheList.add(cache);
      }

      // Wait for cluster to form
      waitForClusterToForm(cacheManagerList);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      LOG.debug("================================================================================================");
      LOG.debug("=============== Tearing down ===================================================================");
      LOG.debug("================================================================================================");
      for (int i = 0; i < CACHEONIX_CONFIGURATIONS.length; i++) {

         final Cacheonix cacheonix = cacheManagerList.get(i);
         if (!cacheonix.isShutdown()) {
            cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         }
      }
      cacheManagerList.clear();
      cacheList.clear();

      super.tearDown();
      LOG.debug("================================================================================================");
      LOG.debug("========== Teared down =========================================================================");
      LOG.debug("================================================================================================");
   }


   public String toString() {

      return "EntryModifiedSubscriberDistributedTest{" +
              "cacheManagerList=" + cacheManagerList +
              ", cacheList=" + cacheList +
              "} " + super.toString();
   }


   private static class TestEntryModifiedSubscriber implements EntryModifiedSubscriber {

      private final List<EntryModifiedEvent> receivedEvents = new ArrayList<EntryModifiedEvent>(2);

      private final List<EntryModifiedEventContentFlag> eventContentFlags;

      private final CountDownLatch latch;

      private final int expectedNumberOfEvents;


      public TestEntryModifiedSubscriber(final List<EntryModifiedEventContentFlag> eventContentFlags,
                                         final int expectedNumberOfEvents) {

         this.eventContentFlags = new ArrayList<EntryModifiedEventContentFlag>(eventContentFlags);
         this.expectedNumberOfEvents = expectedNumberOfEvents;
         this.latch = new CountDownLatch(expectedNumberOfEvents);
      }


      public void notifyKeysUpdated(final List<EntryModifiedEvent> events) {

         receivedEvents.addAll(events);
         latch.countDown();
      }


      public EntryModifiedNotificationMode getNotificationMode() {

         return EntryModifiedNotificationMode.SINGLE;
      }


      public Set<EntryModifiedEventType> getModificationTypes() {

         final HashSet<EntryModifiedEventType> eventTypes = new HashSet<EntryModifiedEventType>(4, 0.75f);
         eventTypes.add(ADD);
         eventTypes.add(EVICT);
         eventTypes.add(REMOVE);
         eventTypes.add(UPDATE);

         return eventTypes;
      }


      public List<EntryModifiedEventContentFlag> getEventContentFlags() {

         return eventContentFlags;
      }


      public List<EntryModifiedEvent> getReceivedEvents() throws InterruptedException {

         if (!latch.await(5000, MILLISECONDS)) {

            throw new IllegalStateException("Timed out waiting for " + expectedNumberOfEvents + " events, current count: " + latch.getCount());
         }

         return new ArrayList<EntryModifiedEvent>(receivedEvents);
      }


      public String toString() {

         return "TestEntryModifiedSubscriber{" +
                 "receivedEvents=" + receivedEvents +
                 ", flags=" + eventContentFlags +
                 '}';
      }
   }
}
