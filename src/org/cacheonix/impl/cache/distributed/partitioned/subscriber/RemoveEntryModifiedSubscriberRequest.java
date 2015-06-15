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

import java.util.Map;
import java.util.Set;

import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.exceptions.NotSubscribedException;
import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessor;
import org.cacheonix.impl.cache.distributed.partitioned.LocalCacheRequest;
import org.cacheonix.impl.cache.distributed.partitioned.PartitionedCache;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.RouteByReferenceRequest;
import org.cacheonix.impl.net.processor.SimpleWaiter;
import org.cacheonix.impl.net.processor.Waiter;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashSet;

/**
 * A <b>local</b> cache request to unregister a subscriber from the local <code>CacheProcessor</code> and to initiate an
 * un-subscription sequence.
 * <p/>
 * This request initiates the un-subscription sequence by posting a reliable mcast message
 * <code>RemoveEntryModifiedSubscriptionAnnouncement</code>/
 * <p/>
 * This request is sent by <code>PartitionedCache.removeEventSubscriber()</code>.
 *
 * @see PartitionedCache#removeEventSubscriber(Set, EntryModifiedSubscriber)
 * @see RemoveEntryModifiedSubscriptionAnnouncement
 */
@SuppressWarnings("RedundantIfStatement")
public final class RemoveEntryModifiedSubscriberRequest extends LocalCacheRequest implements RouteByReferenceRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private HashSet<Binary> keys = null; // NOPMD

   private ClusterNodeAddress subscriberAddress;

   private transient EntryModifiedSubscriber subscriber = null;


   public RemoveEntryModifiedSubscriberRequest() {

   }


   public RemoveEntryModifiedSubscriberRequest(final String cacheName) {

      super(TYPE_CACHE_REMOVE_ENTRY_MODIFIED_SUBSCRIBER_REQUEST, cacheName);
   }


   public void setKeys(final HashSet<Binary> keys) { // NOPMD

      this.keys = new HashSet<Binary>(keys.size());
      this.keys.addAll(keys);
   }


   HashSet<Binary> getKeys() { // NOPMD

      return keys;
   }


   public void setSubscriber(final EntryModifiedSubscriber subscriber) {

      this.subscriber = subscriber;
   }


   EntryModifiedSubscriber getSubscriber() {

      return subscriber;
   }


   public void setSubscriberAddress(final ClusterNodeAddress subscriberAddress) {

      this.subscriberAddress = subscriberAddress;
   }


   ClusterNodeAddress getSubscriberAddress() {

      return subscriberAddress;
   }


   protected void executeOperational() {

      final CacheProcessor processor = getCacheProcessor();

      //
      // Remove local subscription
      //

      final Map<Integer, LocalSubscription> localSubscriptions = processor.getLocalEntryModifiedSubscriptions();
      final int subscriberIdentity = System.identityHashCode(subscriber);
      final LocalSubscription localSubscription = localSubscriptions.get(subscriberIdentity);
      if (localSubscription == null) {

         // Not subscribed, return error

         // NOTE: simeshev@cacheonix.com - 2011-02-09 - It's OK to return
         // an exception becuase this is a local request/response
         getProcessor().post(createErrorResponse(new NotSubscribedException(subscriber.toString())));

         // Nothing to do
         return;
      }


      // Remove keys
      NotSubscribedException notSubscribedException = null;
      final HashSet<Binary> keysToAnnounce = new HashSet<Binary>(keys.size());
      for (final Binary keyToUnSubscribe : keys) {

         if (localSubscription.containsKey(keyToUnSubscribe)) {

            localSubscription.removeKey(keyToUnSubscribe);
            keysToAnnounce.add(keyToUnSubscribe);
         } else {

            if (notSubscribedException == null) {

               notSubscribedException = new NotSubscribedException(subscriber.toString());
            }
         }
      }

      // Remove subscriber if it is not interested in any keys.
      if (!localSubscription.hasKeys()) {

         // No keys left, remove subscription
         localSubscriptions.remove(subscriberIdentity);
      }

      // Post a error response soon if there was over-on-subscription
      if (notSubscribedException != null) {

         getProcessor().post(createErrorResponse(notSubscribedException));
      }

      //
      // Send reliable mcast announcement to begin un-subscription process
      //

      // Create announcement
      final RemoveEntryModifiedSubscriptionAnnouncement announcement = new RemoveEntryModifiedSubscriptionAnnouncement(getCacheName());
      ((RemoveEntryModifiedSubscriptionAnnouncement.Waiter) announcement.getWaiter()).setParentRequest(this);
      announcement.setSubscriberIdentity(subscriberIdentity);
      announcement.setKeySet(keysToAnnounce);

      // Post announcement
      processor.post(announcement);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation simply posts <code>Response.RESULT_RETRY</code>.
    */
   protected void executeBlocked() {

      // If the CacheProcessor is in Blocked state, there is a good chance
      // the reliable multicast is disabled, so it is better simply to wait.
      getProcessor().post(createResponse(Response.RESULT_RETRY));
   }


   /**
    * Creates a error response.
    *
    * @param error an error.
    * @return a <code>Response<code> of code <code>Response.RESULT_ERROR</code> with result set to <code>error</code>
    */
   private Response createErrorResponse(final NotSubscribedException error) {

      final Response errorResponse = createResponse(Response.RESULT_ERROR);
      errorResponse.setResult(error);
      return errorResponse;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation returns {@link SimpleWaiter}.
    */
   protected Waiter createWaiter() {

      return new SimpleWaiter(this);
   }


   public String toString() {

      return "RemoveEntryModifiedSubscriberRequest{" +
              "keys=" + keys +
              ", subscriberAddress=" + subscriberAddress +
              ", subscriber=" + subscriber +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new RemoveRemoteEntryModifiedSubscriberMessage();
      }
   }
}
