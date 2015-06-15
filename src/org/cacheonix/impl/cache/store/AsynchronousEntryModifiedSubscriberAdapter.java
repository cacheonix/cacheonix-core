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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.impl.util.logging.Logger;

/**
 * This adapter converts a given subscriber to an asynchronous one.
 */
public final class AsynchronousEntryModifiedSubscriberAdapter implements IdentityEntryModifiedSubscriber {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Cacheonix.class); // NOPMD

   /**
    * Asynchronous executor.
    */
   private final Executor executor;

   /**
    * The delegate that's notifyKeysUpdated() will be called asynchronously.
    */
   private final IdentityEntryModifiedSubscriber delegate;


   /**
    * Creates a new AsynchronousEntryModifiedSubscriberAdapter.
    *
    * @param executor the executor to use.
    * @param delegate the actual subscriber.
    */
   public AsynchronousEntryModifiedSubscriberAdapter(final Executor executor,
           final IdentityEntryModifiedSubscriber delegate) {

      this.executor = executor;
      this.delegate = delegate;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation executes delegate's notifyKeysUpdated() asynchronously using the executor.
    */
   public void notifyKeysUpdated(final List<EntryModifiedEvent> events) {

      executor.execute(new Runnable() {

         public void run() {

            try {

               delegate.notifyKeysUpdated(events);
            } catch (final Exception e) {

               LOG.error(e, e);
            }
         }
      });
   }


   /**
    * {@inheritDoc}
    */
   public EntryModifiedNotificationMode getNotificationMode() {

      return delegate.getNotificationMode();
   }


   /**
    * {@inheritDoc}
    */
   public Set<EntryModifiedEventType> getModificationTypes() {

      return delegate.getModificationTypes();
   }


   /**
    * {@inheritDoc}
    */
   public List<EntryModifiedEventContentFlag> getEventContentFlags() {

      return delegate.getEventContentFlags();
   }


   public int getIdentity() {

      return delegate.getIdentity();
   }


   public String toString() {

      return "AsynchronousEntryModifiedSubscriberAdapter{" +
              "delegate=" + delegate +
              '}';
   }
}
