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
package org.cacheonix.cache.subscriber;

import java.io.Serializable;
import java.util.List;

import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.clock.Time;

/**
 * An event that occurs when an entry was added to the cache or updated.
 * <p/>
 * Cacheonix sends <code>EntryModifiedEvent</code> to <code>EntryModifiedSubscriber</code> after the entry was added to
 * the cache or updated.
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @see EntryModifiedSubscriber#notifyKeysUpdated(List)
 */
public interface EntryModifiedEvent {

   /**
    * Returns the type of the update operation.
    *
    * @return the type of the update operation.
    */
   EntryModifiedEventType getUpdateType();


   /**
    * Return the key of the updated entry or null if the key was not requested.
    *
    * @return key of the updated entry.
    * @see EntryModifiedEventContentFlag#NEED_KEY
    * @see EntryModifiedSubscriber#getEventContentFlags()
    */
   Serializable getUpdatedKey();


   /**
    * Return the value associated with the key of the updated entry.
    * <p/>
    * This method will return null if the value was null or if the value was not requested.
    *
    * @return the new value of the updated entry. This method returns <code>null</code> if the new value was
    *         <code>null</code>, if the value was not requested, if the entry was removed (getUpdateType() type returns
    *         <code>EntryModifiedEventType.REMOVE</code>) or if the entry was evicted (getUpdateType() type returns
    *         <code>EntryModifiedEventType.EVICT</code>).
    * @see EntryModifiedEventContentFlag#NEED_NEW_VALUE
    * @see EntryModifiedSubscriber#getEventContentFlags()
    * @see EntryModifiedEventType#REMOVE
    * @see EntryModifiedEventType#EVICT
    */
   Serializable getNewValue();


   /**
    * Return the previous value associated with the key of the updated entry. This method will return <code>null</code>
    * if the previous value was <code>null</code>, if this is a notification about a new entry or if the previous value
    * was not requested.
    *
    * @return the previous value of the updated entry.
    * @see EntryModifiedEventType#ADD
    * @see EntryModifiedEventContentFlag#NEED_PREVIOUS_VALUE
    * @see EntryModifiedSubscriber#getEventContentFlags()
    */
   Object getPreviousValue();


   /**
    * Returns the time of the last update of the entry according to the entry's local clock.
    *
    * @return the time of the last update of the entry according to the entry's local clock.
    */
   Time getLastUpdateTime();


   /**
    * Return the version of the entry. The version is equivalent of an update counter.
    *
    * @return the version of the entry.
    */
   long getVersion();


   /**
    * Returns the updater of the entry or null if the updater is not available.
    *
    * @return the updater of the entry or null if the updater is not available.
    */
   CacheMember getUpdater();
}
