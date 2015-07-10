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
package org.cacheonix.impl.cache.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * SafeEntryUpdateSubscriber.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 13, 2008 4:54:58 PM
 */
public final class SafeEntryUpdateSubscriber implements IdentityEntryModifiedSubscriber {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SafeEntryUpdateSubscriber.class); // NOPMD

   private final EntryModifiedSubscriber delegate;

   private final List<EntryModifiedEventContentFlag> eventContentFlags;

   private final EntryModifiedNotificationMode notificationMode;


   /**
    * Constructor.
    *
    * @param delegate delegate.
    */
   public SafeEntryUpdateSubscriber(final EntryModifiedSubscriber delegate) {
      // Init delegate
      this.delegate = delegate;
      // Create safe immutable configuration
      this.eventContentFlags = createSafeEventFlags(delegate);
      this.notificationMode = createSafeNotificationMode(delegate);
   }


   /**
    * {@inheritDoc}
    */
   public void notifyKeysUpdated(final List<EntryModifiedEvent> events) {

      try {
         delegate.notifyKeysUpdated(events);
      } catch (final Exception e) {
         ignoreExpectedException(e);
      }
   }


   /**
    * {@inheritDoc}
    */
   public EntryModifiedNotificationMode getNotificationMode() {

      return notificationMode;
   }


   public Set<EntryModifiedEventType> getModificationTypes() {

      try {
         return delegate.getModificationTypes();

      } catch (final Exception e) {

         final HashSet<EntryModifiedEventType> eventTypes = new HashSet<EntryModifiedEventType>(3, 0.75f);
         eventTypes.add(EntryModifiedEventType.ADD);
         eventTypes.add(EntryModifiedEventType.REMOVE);
         eventTypes.add(EntryModifiedEventType.UPDATE);

         return eventTypes;
      }
   }


   /**
    * {@inheritDoc}
    */
   public List<EntryModifiedEventContentFlag> getEventContentFlags() {

      return eventContentFlags;
   }


   public int getIdentity() {

      return System.identityHashCode(delegate);
   }


   private static void ignoreExpectedException(final Exception e) {

      ExceptionUtils.ignoreException(e,
              "Caught and stopped from propagating as intended by the semantics of the class");
   }


   private static EntryModifiedNotificationMode createSafeNotificationMode(final EntryModifiedSubscriber delegate) {

      try {
         return delegate.getNotificationMode();
      } catch (final Exception e) {
         ignoreExpectedException(e);
         return EntryModifiedNotificationMode.SINGLE;
      }
   }


   private static List<EntryModifiedEventContentFlag> createSafeEventFlags(final EntryModifiedSubscriber delegate) {

      try {
         return new ArrayList<EntryModifiedEventContentFlag>(delegate.getEventContentFlags());
      } catch (final Exception e) {
         ignoreExpectedException(e);
         return Collections.emptyList();
      }
   }


   public String toString() {

      return "SafeEntryUpdateSubscriber{" +
              "delegate=" + delegate +
              ", eventContentFlags=" + eventContentFlags +
              ", notificationMode=" + notificationMode +
              '}';
   }
}
