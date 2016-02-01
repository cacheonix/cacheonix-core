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

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.processor.RequestProcessor;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A subscriber to configuration events happening to a list of subscribers to cache entry events.
 * <p/>
 * This subscriber is notified synchronously when a replicate state is modified.
 *
 * @see #notifySubscriptionAdded(Binary, EntryModifiedSubscription, int)
 * @see #notifySubscriptionRemoved(Binary, EntryModifiedSubscription, int)
 */
public final class EntryEventSubscriptionConfigurationSubscriber {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AddEntryModifiedSubscriptionAnnouncement.class); // NOPMD

   /**
    * A local cache processor that should receive messages generated in response to configuration events.
    *
    * @see AddRemoteEntryModifiedSubscriberMessage
    * @see RemoveRemoteEntryModifiedSubscriberMessage
    */
   private final RequestProcessor processor;

   /**
    * Cache name.
    */
   private final String cacheName;


   /**
    * Creates new EntryEventSubscriptionConfigurationSubscriber.
    *
    * @param cacheName a cache name
    * @param processor a local cache processor.
    */
   public EntryEventSubscriptionConfigurationSubscriber(final String cacheName, final RequestProcessor processor) {

      this.cacheName = cacheName;

      this.processor = processor;
   }


   /**
    * Returns a cache name.
    *
    * @return the cache name.
    */
   public String getCacheName() {

      return cacheName;
   }


   /**
    * Notifies this subscriber that a subscription was added.
    * <p/>
    * This implementation posts <code>AddRemoteEntryModifiedSubscriberMessage</code> to the local processor in order to
    * add the subscription to its bucket storage.
    *
    * @param key          a key of interest.
    * @param subscription the subscription.
    * @param bucketNumber a pre-calculated bucket number.
    * @see AddRemoteEntryModifiedSubscriberMessage#executeOperational()
    */
   public void notifySubscriptionAdded(final Binary key, final EntryModifiedSubscription subscription,
                                       final int bucketNumber) {

      try {

         // Post message
         final AddRemoteEntryModifiedSubscriberMessage message = new AddRemoteEntryModifiedSubscriberMessage(cacheName);
         message.setReceiver(processor.getAddress());
         message.setBucketNumber(bucketNumber);
         message.setSubscription(subscription);
         message.setKey(key);
         processor.post(message);
      } catch (final Exception e) {

         // This is a synchronous call from replicated
         // state,  so log and ignore the exception.

         LOG.error("Unexpected error while processing notifySubscriptionAdded(): " + e, e);
      }
   }


   /**
    * Notifies this subscriber that a subscription was removed.
    * <p/>
    * This implementation posts <code>RemoveRemoteEntryModifiedSubscriberMessage</code> to the local processor in order
    * to remove the subscription from its bucket storage.
    *
    * @param key          a key of interest.
    * @param subscription the subscription.
    * @param bucketNumber a pre-calculated bucket number.
    */
   public void notifySubscriptionRemoved(final Binary key, final EntryModifiedSubscription subscription,
                                         final int bucketNumber) {

      try {

         // Post message
         final RemoveRemoteEntryModifiedSubscriberMessage message = new RemoveRemoteEntryModifiedSubscriberMessage(cacheName);
         message.setSubscriberIdentity(subscription.getSubscriberIdentity());
         message.setReceiver(processor.getAddress());
         message.setBucketNumber(bucketNumber);
         message.setKey(key);
         processor.post(message);
      } catch (final Exception e) {

         // This is a synchronous call from replicated
         // state,  so log and ignore the exception.

         LOG.error("Unexpected error while processing notifySubscriptionAdded(): " + e, e);
      }
   }


   public String toString() {

      return "EntryEventSubscriptionConfigurationSubscriber{" +
              "localCacheProcessor=" + processor +
              ", cacheName='" + cacheName + '\'' +
              '}';
   }
}
