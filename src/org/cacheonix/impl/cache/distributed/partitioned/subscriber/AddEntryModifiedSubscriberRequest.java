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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessor;
import org.cacheonix.impl.cache.distributed.partitioned.LocalCacheRequest;
import org.cacheonix.impl.cache.distributed.partitioned.PartitionedCache;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.BinaryUtils;
import org.cacheonix.impl.cache.store.AsynchronousEntryModifiedSubscriberAdapter;
import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriberAdapter;
import org.cacheonix.impl.cache.store.SafeEntryUpdateSubscriber;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.RouteByReferenceRequest;
import org.cacheonix.impl.net.processor.SimpleWaiter;
import org.cacheonix.impl.net.processor.Waiter;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashSet;

/**
 * A <b>local</b> cache request to register a subscriber in the local <code>CacheProcessor</code> and to initiate
 * subscription sequence.
 * <p/>
 * This request initiates the subscription sequence by posting a reliable mcast message
 * <code>AddEntryModifiedSubscriptionAnnouncement</code>/
 * <p/>
 * This request is sent by <code>PartitionedCache.addEventSubscriber()</code>.
 *
 * @see PartitionedCache#addEventSubscriber(Set, EntryModifiedSubscriber)
 * @see AddEntryModifiedSubscriptionAnnouncement
 */
@SuppressWarnings("RedundantIfStatement")
public final class AddEntryModifiedSubscriberRequest extends LocalCacheRequest implements RouteByReferenceRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private HashSet<Binary> keys = null; // NOPMD

   /**
    * A local subscriber as provided by the client.
    */
   private transient EntryModifiedSubscriber localSubscriber;

   private ClusterNodeAddress subscriberAddress;


   public AddEntryModifiedSubscriberRequest() {

   }


   public AddEntryModifiedSubscriberRequest(final String cacheName) {

      super(TYPE_CACHE_ADD_ENTRY_MODIFIED_SUBSCRIBER_REQUEST, cacheName);
   }


   public void setKeys(final HashSet<Binary> keys) { // NOPMD

      this.keys = BinaryUtils.copy(keys);
   }


   HashSet<Binary> getKeys() { // NOPMD

      return keys;
   }


   /**
    * Sets the local subscriber as provided by the client.
    *
    * @param localSubscriber the local subscriber as provided by the client.
    */
   public void setLocalSubscriber(final EntryModifiedSubscriber localSubscriber) {

      this.localSubscriber = localSubscriber;
   }


   /**
    * Returns the local subscriber as provided by the client.
    *
    * @return the local subscriber as provided by the client.
    */
   EntryModifiedSubscriber getLocalSubscriber() {

      return localSubscriber;
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
      // Register local receiver of notification messages
      //
      final Executor eventNotificationExecutor = processor.getEventNotificationExecutor();
      final SafeEntryUpdateSubscriber safeSubscriber = new SafeEntryUpdateSubscriber(localSubscriber);
      final AsynchronousEntryModifiedSubscriberAdapter asynchronousSubscriber = new AsynchronousEntryModifiedSubscriberAdapter(
              eventNotificationExecutor, safeSubscriber);
      final BinaryEntryModifiedSubscriberAdapter binarySubscriber = new BinaryEntryModifiedSubscriberAdapter(
              asynchronousSubscriber);
      final int subscriberIdentity = System.identityHashCode(localSubscriber);
      final Map<Integer, LocalSubscription> localSubscriptions = processor.getLocalEntryModifiedSubscriptions();
      LocalSubscription localSubscription = localSubscriptions.get(subscriberIdentity);
      if (localSubscription == null) {

         // Subscriber not found, create subscription
         localSubscription = new LocalSubscription();
         localSubscription.setSubscriber(binarySubscriber);
         localSubscriptions.put(subscriberIdentity, localSubscription);
      }
      localSubscription.addKeys(keys);

      //
      // Send reliable mcast announcement to begin subscription process
      //

      // Create subscription information
      final EntryModifiedSubscription subscription = new EntryModifiedSubscription(subscriberIdentity,
              subscriberAddress, localSubscriber.getNotificationMode(), localSubscriber.getEventContentFlags(),
              localSubscriber.getModificationTypes());

      // Create announcement
      final AddEntryModifiedSubscriptionAnnouncement announcement = new AddEntryModifiedSubscriptionAnnouncement(
              getCacheName());
      ((AddEntryModifiedSubscriptionAnnouncement.Waiter) announcement.getWaiter()).setParentRequest(this);
      announcement.setSubscription(subscription);
      announcement.setKeySet(keys);

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
    * {@inheritDoc}
    * <p/>
    * This implementation returns {@link SimpleWaiter}.
    */
   protected Waiter createWaiter() {

      return new SimpleWaiter(this);
   }


   public String toString() {

      return "AddEntryModifiedSubscriberRequest{" +
              "keys=" + StringUtils.sizeToString((Collection) keys) +
              ", subscriber=" + localSubscriber +
              ", subscriberAddress=" + subscriberAddress +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new AddEntryModifiedSubscriberRequest();
      }
   }
}
