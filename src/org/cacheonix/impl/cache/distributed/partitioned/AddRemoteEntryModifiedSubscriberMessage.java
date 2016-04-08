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

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;

/**
 * A local CacheMessage that registers a RemoteEntryModifiedSubscriber with a bucket that owns a key of interest.
 * <p/>
 * The message is sent by AddEntryModifiedSubscriptionAnnouncement when it is at the node that owns the bucket.
 * <p/>
 * This message does not require a response because if AddEntryModifiedSubscriptionAnnouncement determines that the
 * bucket is not being transferred out, that the bucket should not be reconfiguring by way of order of bucket ownership
 * change commands.
 *
 * @see KeySetAnnouncement#processKeys(Integer, IntHashSet, IntObjectHashMap
 */
@SuppressWarnings("RedundantIfStatement")
public final class AddRemoteEntryModifiedSubscriberMessage extends LocalCacheMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * A bucket number used for quick bucket lookup.
    */
   private int bucketNumber;

   /**
    * A key of interest.
    */
   private Binary key;

   /**
    * A subscription information.
    */
   private EntryModifiedSubscription subscription;


   /**
    * Required by Wireable.
    */
   private AddRemoteEntryModifiedSubscriberMessage() {

   }


   /**
    * Creates RegisterSubscriptionMessage
    *
    * @param cacheName a cache name.
    */
   public AddRemoteEntryModifiedSubscriberMessage(final String cacheName) {

      super(TYPE_CACHE_ADD_REMOTE_SUBSCRIBER_MESSAGE, cacheName);
   }


   /**
    * Sets a bucket number used for quick bucket lookup.
    *
    * @param bucketNumber the bucket number used for quick bucket lookup.
    */
   public void setBucketNumber(final int bucketNumber) {

      this.bucketNumber = bucketNumber;
   }


   int getBucketNumber() {

      return bucketNumber;
   }


   /**
    * Sets a key of interest.
    *
    * @param key the key of interest to set.
    */
   public void setKey(final Binary key) {

      this.key = key;
   }


   Binary getKey() {

      return key;
   }


   /**
    * Sets a subscription definition.
    *
    * @param subscription the subscription definition.
    */
   public void setSubscription(final EntryModifiedSubscription subscription) {

      this.subscription = subscription;
   }


   EntryModifiedSubscription getSubscription() {

      return subscription;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation creates a RemoteEntryModifiedSubscriber and sets it to the bucket.
    *
    * @see RemoteEntryModifiedSubscriber
    */
   protected void executeOperational() {

      final CacheProcessor cacheProcessor = getCacheProcessor();

      // Check if bucket owner
      if (!cacheProcessor.isBucketOwner(0, bucketNumber)) {

         return;
      }

      if (cacheProcessor.hasBucket(0, bucketNumber)) {

         // Create a message-sending subscriber
         final RemoteEntryModifiedSubscriber subscriber = new RemoteEntryModifiedSubscriber();
         subscriber.setSubscription(subscription);
         subscriber.setCacheName(getCacheName());
         subscriber.setProcessor(getProcessor());

         // Add subscriber to the bucket


         // REVIEWME: simeshev@cacheonix.org - 2011-02-10 -> This may lead to over-subscription if this is a new bucket
         // because getOrCreateBucket() also sets subscriptions to the new bucket from the replicated state (group).
         // This message is executed by the local cache processor *after* the replicated state was modified, so the
         // new bucket gets subscriptions from the replicated state *and* from the code below. Right now the binary
         // store ignores over-subscription attempts at addEventSubscriber() instead of throwing an
         // AlreadySubscribedException. Once this issue is addressed, the ignoring should be replaced with
         // throwing exception.
         //
         // Solutions:
         //
         // Don't draw subscription information from the replicated state. Instead, have it replicated at
         // the CacheProcessor.
         //

         final Bucket bucket = cacheProcessor.getBucket(0, bucketNumber);
         bucket.addEventSubscriber(key, subscriber);
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void executeBlocked() {

      executeOperational();
   }


   public String toString() {

      return "AddRemoteEntryModifiedSubscriberMessage{" +
              "bucketNumber=" + bucketNumber +
              ", key=" + key +
              ", subscription=" + subscription +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new AddRemoteEntryModifiedSubscriberMessage(); // NOPMD
      }
   }
}
