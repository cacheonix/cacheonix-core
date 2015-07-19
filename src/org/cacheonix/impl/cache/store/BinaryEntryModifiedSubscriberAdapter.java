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
package org.cacheonix.impl.cache.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.impl.cache.subscriber.BinaryEntryModifiedEvent;
import org.cacheonix.impl.cache.subscriber.LocalEntryModifiedEvent;

/**
 * An adaptor used to convert EntryModifiedSubscriber to BinaryEntryModifiedSubscriber.
 */
public final class BinaryEntryModifiedSubscriberAdapter implements BinaryEntryModifiedSubscriber {

   private final IdentityEntryModifiedSubscriber localSubscriber;


   /**
    * Creates BinaryEntryModifiedSubscriberAdapter.
    *
    * @param localSubscriber the API's EntryModifiedSubscriber.
    */
   public BinaryEntryModifiedSubscriberAdapter(final IdentityEntryModifiedSubscriber localSubscriber) {

      this.localSubscriber = localSubscriber;
   }


   /**
    * {@inheritDoc}
    */
   public void notifyKeysUpdated(final List<BinaryEntryModifiedEvent> events) {

      // Convert BinaryEntryModifiedEvent to EntryModifiedEvent
      final List<EntryModifiedEvent> result = new ArrayList<EntryModifiedEvent>(events.size());

      for (final BinaryEntryModifiedEvent binaryEvent : events) {

         final LocalEntryModifiedEvent localEvent = new LocalEntryModifiedEvent(binaryEvent.getUpdateType(),
                 binaryEvent.getUpdatedKey(), binaryEvent.getNewValue(), binaryEvent.getPreviousValue(),
                 binaryEvent.getLastUpdateTime(), binaryEvent.getVersion(), binaryEvent.getUpdater());
         result.add(localEvent);
      }

      // Call local subscriber
      localSubscriber.notifyKeysUpdated(result);
   }


   /**
    * {@inheritDoc}
    */
   public EntryModifiedNotificationMode getNotificationMode() {

      return localSubscriber.getNotificationMode();
   }


   /**
    * {@inheritDoc}
    */
   public List<EntryModifiedEventContentFlag> getEventContentFlags() {

      return localSubscriber.getEventContentFlags();
   }


   /**
    * {@inheritDoc}
    */
   public Set<EntryModifiedEventType> getModificationTypes() {

      return localSubscriber.getModificationTypes();
   }


   public int getIdentity() {

      return localSubscriber.getIdentity();
   }


   public String toString() {

      return "BinaryEntryModifiedSubscriberAdapter{" +
              "localSubscriber=" + localSubscriber +
              '}';
   }
}
