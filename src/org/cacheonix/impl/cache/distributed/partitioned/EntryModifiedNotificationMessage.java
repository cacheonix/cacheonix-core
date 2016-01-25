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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriber;
import org.cacheonix.impl.cache.subscriber.BinaryEntryModifiedEvent;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A message that <code>RemoteEntryModifiedSubscriber</code> sends to a subscriber to notify about cache entry
 * modifications.
 *
 * @see RemoteEntryModifiedSubscriber#notifyKeysUpdated(List)
 */
@SuppressWarnings("RedundantIfStatement")
public final class EntryModifiedNotificationMessage extends CacheMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(KeySetAnnouncement.class); // NOPMD


   private int subscriberIdentity = 0;

   private List<BinaryEntryModifiedEvent> events = null;


   /**
    * Required by Wireable.
    */
   private EntryModifiedNotificationMessage() {

   }


   /**
    * Creates an EntryModifiedMessage.
    *
    * @param cacheName a cache name.
    */
   public EntryModifiedNotificationMessage(final String cacheName) {

      super(TYPE_CACHE_ENTRY_MODIFIED_MESSAGE, cacheName);
   }


   /**
    * Sets subscriber identity.
    *
    * @param subscriberIdentity a subscriber identity to set,
    */
   public void setSubscriberIdentity(final int subscriberIdentity) {

      this.subscriberIdentity = subscriberIdentity;
   }


   int getSubscriberIdentity() {

      return subscriberIdentity;
   }


   /**
    * Creates an internal copy of the event list.
    *
    * @param events the events to set
    */
   public void setEvents(final List<BinaryEntryModifiedEvent> events) {

      this.events = new ArrayList<BinaryEntryModifiedEvent>(events);
   }


   List<BinaryEntryModifiedEvent> getEvents() {

      return events;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * <p/>
    * This implementation looks up a subscriber and calls {@link BinaryEntryModifiedSubscriber#notifyKeysUpdated(List)}.
    *
    * @throws InterruptedException
    */
   protected void executeOperational() {

      final CacheProcessor processor = getCacheProcessor();

      // Get subscriber map
      final Map<Integer, LocalSubscription> subscriptions = processor.getLocalEntryModifiedSubscriptions();

      // Look up subscriber
      final LocalSubscription binaryEntryModifiedSubscriber = subscriptions.get(subscriberIdentity);
      if (binaryEntryModifiedSubscriber == null) {

         // Subscriber was un-subscribed
         return;
      }

      // Notify subscriber
      binaryEntryModifiedSubscriber.getSubscriber().notifyKeysUpdated(events);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation simply calls <code>executeOperational()</code> because the notification was sent before the
    * notifying <code>CacheProcessor</code> entered Blocked state.
    *
    * @see #executeOperational()
    */
   protected void executeBlocked() {

      executeOperational();
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      subscriberIdentity = in.readInt();

      final int eventListSize = in.readInt();
      events = new ArrayList<BinaryEntryModifiedEvent>(eventListSize);
      for (int i = 0; i < eventListSize; i++) {

         final BinaryEntryModifiedEvent event = new BinaryEntryModifiedEvent();
         event.readWire(in);
         events.add(event);
      }
   }


   @SuppressWarnings("ForLoopReplaceableByForEach")
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      out.writeInt(subscriberIdentity);

      final int eventListSize = events.size();
      out.writeInt(eventListSize);
      for (int i = 0; i < eventListSize; i++) {
         events.get(i).writeWire(out);
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final EntryModifiedNotificationMessage that = (EntryModifiedNotificationMessage) o;

      if (subscriberIdentity != that.subscriberIdentity) {
         return false;
      }
      if (events != null ? !events.equals(that.events) : that.events != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (events != null ? events.hashCode() : 0);
      result = 31 * result + subscriberIdentity;
      return result;
   }


   public String toString() {

      return "EntryModifiedMessage{" +
              "events=" + events +
              ", subscriberIdentity=" + subscriberIdentity +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new EntryModifiedNotificationMessage(); // NOPMD
      }
   }
}
