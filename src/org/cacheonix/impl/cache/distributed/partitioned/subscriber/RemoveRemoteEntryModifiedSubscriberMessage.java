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

import org.cacheonix.impl.cache.distributed.partitioned.Bucket;
import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessor;
import org.cacheonix.impl.cache.distributed.partitioned.LocalCacheMessage;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A local CacheMessage that removes a RemoteEntryModifiedSubscriber from a bucket that owns a key of interest.
 * <p/>
 * The message is sent by <code>EntryModificationSubscriptionEventSubscriberImpl</code> a subscription is removed due to
 * a cache member leaving cache group.
 */
@SuppressWarnings("RedundantIfStatement")
public final class RemoveRemoteEntryModifiedSubscriberMessage extends LocalCacheMessage {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RemoveRemoteEntryModifiedSubscriberMessage.class); // NOPMD

   /**
    * A bucket number used for quick bucket lookup.
    */
   private int bucketNumber;

   /**
    * A key of interest.
    */
   private Binary key;

   private int subscriberIdentity;


   /**
    * Required by Wireable.
    */
   public RemoveRemoteEntryModifiedSubscriberMessage() {

   }


   /**
    * Creates RegisterSubscriptionMessage
    *
    * @param cacheName a cache name.
    */
   public RemoveRemoteEntryModifiedSubscriberMessage(final String cacheName) {

      super(TYPE_CACHE_REMOVE_REMOTE_SUBSCRIBER_MESSAGE, cacheName);
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
    * Sets a subscriber identity.
    *
    * @param subscriberIdentity the subscription definition.
    */
   public void setSubscriberIdentity(final int subscriberIdentity) {

      this.subscriberIdentity = subscriberIdentity;
   }


   int getSubscriberIdentity() {

      return subscriberIdentity;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation removes a RemoteEntryModifiedSubscriber from to the bucket.
    */
   protected void executeOperational() {

      final CacheProcessor cacheProcessor = getCacheProcessor();

      // Check if bucket owner

      // NOTE: simeshev@cacheonix.org - 2011-02-11 - This check should work even if the bucket
      // is currently being transferred out because the notification about completion has not
      // been received yet, but the leave message causing this call has already been received.
      if (!cacheProcessor.isBucketOwner(0, bucketNumber)) {

         return;
      }

      // Remove subscriber from the bucket
      if (cacheProcessor.hasBucket(0, bucketNumber)) {

         final Bucket bucket = cacheProcessor.getBucket(0, bucketNumber);
         bucket.removeEventSubscriber(key, subscriberIdentity);
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void executeBlocked() {

      executeOperational();
   }


   public String toString() {

      return "RemoveRemoteEntryModifiedSubscriberMessage{" +
              "bucketNumber=" + bucketNumber +
              ", key=" + key +
              ", subscriberIdentity=" + subscriberIdentity +
              "} " + super.toString();
   }
}
