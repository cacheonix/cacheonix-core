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
package org.cacheonix.impl.cache.subscriber;

import java.io.Serializable;
import java.util.List;

import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriber;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An implementation of the {@link EntryModifiedEvent} that is sent to local subscribers.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see BinaryEntryModifiedSubscriber#notifyKeysUpdated(List)
 * @since Aug 13, 2008 4:20:21 PM
 */
public final class LocalEntryModifiedEvent implements EntryModifiedEvent {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalEntryModifiedEvent.class); // NOPMD

   /**
    * Key of the updated element or null if the key was not requested.
    */
   private Binary updatedKey = null;

   /**
    * Value associated with the key of the updated element or null if the value was null or if the value was not
    * requested.
    */
   private Binary newValue = null;

   /**
    * Previous value associated with the key of the updated element or null if the value was null, this is a
    * notification about an added element or if the previous value was not requested.
    */
   private Binary previousValue = null;

   /**
    * Type of the update operation.
    */
   private EntryModifiedEventType updateType = null;

   /**
    * Time of the last update of the element according to the element's local clock.
    */
   private final Time lastUpdateTime;

   /**
    * Version of the element. The version is equivalent of an update counter.
    */
   private final long version;


   /**
    * Updater of the element or null if the updater is not available.
    */
   private CacheMember updater = null;


   /**
    * Constructor.
    *
    * @param updateType     update type
    * @param updatedKey     updated key
    * @param newValue       updated value
    * @param previousValue  previous value
    * @param lastUpdateTime lat time the entry was updated, milliseconds
    * @param version        entry version
    * @param updater        cache member that performed an update.
    */
   public LocalEntryModifiedEvent(final EntryModifiedEventType updateType, final Binary updatedKey,
                                  final Binary newValue, final Binary previousValue, final Time lastUpdateTime,
                                  final long version, final CacheMember updater) {

      this.updatedKey = updatedKey;
      this.newValue = newValue;
      this.previousValue = previousValue;
      this.updateType = updateType;
      this.lastUpdateTime = lastUpdateTime;
      this.version = version;
      this.updater = updater;
   }


   /**
    * {@inheritDoc}
    */
   public EntryModifiedEventType getUpdateType() {

      return updateType;
   }


   /**
    * {@inheritDoc}
    */
   public Serializable getUpdatedKey() {

      return toObject(updatedKey);
   }


   /**
    * {@inheritDoc}
    */
   public Serializable getNewValue() {

      return toObject(newValue);
   }


   /**
    * {@inheritDoc}
    */
   public Serializable getPreviousValue() {

      return toObject(previousValue);
   }


   /**
    * {@inheritDoc}
    */
   public Time getLastUpdateTime() {

      return lastUpdateTime;
   }


   /**
    * {@inheritDoc}
    */
   public long getVersion() {

      return version;
   }


   /**
    * {@inheritDoc}
    */
   public CacheMember getUpdater() {

      return updater;
   }


   private static Serializable toObject(final Binary binary) {

      if (binary == null) {

         return null;
      }

      return (Serializable) binary.getValue();
   }


   public String toString() {

      return "LocalEntryModifiedEvent{" +
              "updatedKey=" + updatedKey +
              ", newValue=" + newValue +
              ", previousValue=" + previousValue +
              ", updateType=" + updateType +
              ", lastUpdateTimeMillis=" + lastUpdateTime +
              ", version=" + version +
              ", updater=" + updater +
              '}';
   }
}
