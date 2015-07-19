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
package org.cacheonix.impl.cache.distributed.partitioned.subscriber;

import java.util.List;
import java.util.Set;

import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.impl.cache.distributed.partitioned.Bucket;
import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessor;
import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriber;
import org.cacheonix.impl.cache.subscriber.BinaryEntryModifiedEvent;
import org.cacheonix.impl.net.processor.RequestProcessor;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Message-sending implementation of <code>BinaryEntryModifiedSubscriber</code>
 *
 * @see AddRemoteEntryModifiedSubscriberMessage#executeOperational()
 * @see CacheProcessor#setBucket(int, Integer, Bucket)
 */
public final class RemoteEntryModifiedSubscriber implements BinaryEntryModifiedSubscriber {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(KeySetAnnouncement.class); // NOPMD

   /**
    * Processor used to send the notification message.
    */
   private RequestProcessor processor = null;

   /**
    * Subscription description.
    */
   private EntryModifiedSubscription subscription;


   /**
    * A name of the cache that should be receiving notifications.
    */
   private String cacheName = null;


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation creates and posts a notification message to the subscriber described by
    * <code>subscription</code>.
    *
    * @see #subscription
    */
   public void notifyKeysUpdated(final List<BinaryEntryModifiedEvent> events) {

      final EntryModifiedNotificationMessage entryModifiedMessage = new EntryModifiedNotificationMessage(cacheName);
      entryModifiedMessage.setSubscriberIdentity(subscription.getSubscriberIdentity());
      entryModifiedMessage.setReceiver(subscription.getSubscriberAddress());
      entryModifiedMessage.setEvents(events);

      try {

         processor.post(entryModifiedMessage);
      } catch (final Exception e) {

         LOG.warn("Error while posting an entry modified event message: " + e.toString(), e);
      }
   }


   /**
    * {@inheritDoc}
    */
   public EntryModifiedNotificationMode getNotificationMode() {

      return subscription.getNotificationMode();
   }


   /**
    * {@inheritDoc}
    */
   public List<EntryModifiedEventContentFlag> getEventContentFlags() {

      return subscription.getEventContentFlags();
   }


   /**
    * {@inheritDoc}
    */
   public Set<EntryModifiedEventType> getModificationTypes() {

      return subscription.getModificationTypes();
   }


   public int getIdentity() {

      return subscription.getSubscriberIdentity();
   }


   /**
    * {@inheritDoc}
    */
   public void setProcessor(final RequestProcessor processor) {

      this.processor = processor;
   }


   /**
    * {@inheritDoc}
    */
   public void setSubscription(final EntryModifiedSubscription subscription) {

      this.subscription = subscription;
   }


   /**
    * Sets a name of the cache that should be receiving notifications.
    *
    * @param cacheName the name of the cache that should be receiving notifications.
    * @see AddRemoteEntryModifiedSubscriberMessage#executeOperational()
    * @see CacheProcessor#setBucket(int, Integer, Bucket)
    */
   public void setCacheName(final String cacheName) {

      this.cacheName = cacheName;
   }


   public String toString() {

      return "RemoteEntryModifiedSubscriber{" +
              "processor=" + processor +
              ", subscription=" + subscription +
              '}';
   }
}
