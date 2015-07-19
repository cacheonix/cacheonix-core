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
package org.cacheonix.cache.subscriber;

import java.util.List;
import java.util.Set;

import org.cacheonix.cache.Cache;

/**
 * A listener for <code>EntryModifiedEvent</code>. <code>EntryModifiedSubscriber</code>  receives
 * <code>EntryModifiedEvent</code> after an entry was added to the cache, deleted or updated.
 * <p/>
 * <b>Important:</b> Processing of the events performed by <code>notifyKeysUpdated()</code> should be done as fast as
 * possible. <code>notifyKeysUpdated()</code> must not call any blocking operation such as I/O operations. If the
 * processing of events  involves blocking operations or it is expected to take long time, the cache should be
 * configured to use an asynchronous notification mode. Example:
 * <p/>
 * <pre>
 * &lt;localCache name="CacheWithAsynchronousEventNotification"&gt;
 *    &lt;store&gt;
 *       &lt;lru maxElements="1000" maxBytes="10mb"/&gt;
 *       &lt;overflowToDisk maxOverflowBytes="1mb"/&gt;
 *       &lt;expiration timeToLive="1s"/&gt;
 *       <b>&lt;elementEvents notification="asynchronous"/&gt;</b>
 *    &lt;/store&gt;
 * &lt;/localCache&gt;
 * </pre>
 * Use {@link Cache#addEventSubscriber(Set, EntryModifiedSubscriber)} to add an <code>EntryModifiedSubscriber</code> to
 * the cache.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see EntryModifiedEvent
 */
public interface EntryModifiedSubscriber {


   /**
    * Processes <code>EntryModifiedEvent</code> that Cacheonix sends when an entry or a set of entries were added,
    * removed, updated or evicted.
    * <p/>
    * <b>Important:</b> Processing should be done as fast as possible. <code>notifyKeysUpdated()</code> must not call
    * any blocking operation such as I/O operations. If the processing of events  involves blocking operations or it is
    * expected to take long time, the cache should be configured to use an asynchronous notification mode. Example:
    * <p/>
    * <pre>
    * &lt;localCache name="CacheWithAsynchronousEventNotification"&gt;
    *    &lt;store&gt;
    *       &lt;lru maxElements="1000" maxBytes="10mb"/&gt;
    *       &lt;overflowToDisk maxOverflowBytes="1mb"/&gt;
    *       &lt;expiration timeToLive="1s"/&gt;
    *       <b>&lt;elementEvents notification="asynchronous"/&gt;</b>
    *    &lt;/store&gt;
    * &lt;/localCache&gt;
    * </pre>
    * <p/>
    * If batching is enabled by returning <code>EntryModifiedNotificationMode.BATCH_UPDATE</code> from
    * <code>getNotificationMode()</code>, the <code>events</list> may contain a list of events. If batching is disabled,
    * the list will contain a single event.
    * <p/>
    * Cacheonix will call this method if batching of the event is enabled by <code>EntryModifiedNotificationMode.BATCH_UPDATE</code>.
    * Classes implementing this method make take advantage of the batched notification by delivering specialized
    * handling for the batch.
    *
    * @param events a list of {@link EntryModifiedEvent}.
    * @see #getNotificationMode()
    * @see EntryModifiedNotificationMode#BATCH
    */
   void notifyKeysUpdated(final List<EntryModifiedEvent> events);


   /**
    * Returns notification mode.
    * <p/>
    * <b>Implementation example:</b>
    * <pre>
    *    public EntryModifiedNotificationMode getNotificationMode() {
    *       return EntryModifiedNotificationMode.SINGLE_UPDATE;
    *    }
    * </pre>
    *
    * @return notification mode.
    * @see EntryModifiedNotificationMode#SINGLE
    * @see EntryModifiedNotificationMode#BATCH
    */
   EntryModifiedNotificationMode getNotificationMode();


   /**
    * Returns a list of key modification types this subscriber is interested in.
    * <p/>
    * <b>Example:</b> An implementation returning a list of update-only operations:
    * <pre>
    * public Set<EntryModifiedEventType> getModificationTypes() {
    *
    *    final Set<EntryModifiedEventType> eventTypes = new HashSet<EntryModifiedEventType>(3);
    *    eventTypes.add(EntryModifiedEventType.ADD);
    *    eventTypes.add(EntryModifiedEventType.REMOVE);
    *    eventTypes.add(EntryModifiedEventType.UPDATE);
    *    return eventTypes;
    * }
    * </pre>
    *
    * @return the list of key modification types this subscriber is interested in.
    * @see EntryModifiedEventType#ADD
    * @see EntryModifiedEventType#REMOVE
    * @see EntryModifiedEventType#UPDATE
    * @see EntryModifiedEventType#EVICT
    */
   Set<EntryModifiedEventType> getModificationTypes();


   /**
    * Returns a list of flags that are used to determine what information should be provided by
    * <code>EntryModifiedEvent</code>.
    * <p/>
    * <b>Implementation example:</b>
    * <pre>
    *    public List getEventContentFlags() {
    *      // Request to provide value of the updated entry
    *      final List flags = new ArrayList(1);
    *      flags.add(EntryModifiedEventContentFlag.NEED_VALUE);
    *      return flags;
    *   }
    * </pre>
    *
    * @return list of flags that are used to determine what information should be provided by
    *         <code>EntryModifiedEvent</code>.
    * @see EntryModifiedEventContentFlag#NEED_ALL
    * @see EntryModifiedEventContentFlag#NEED_KEY
    * @see EntryModifiedEventContentFlag#NEED_NEW_VALUE
    * @see EntryModifiedEventContentFlag#NEED_PREVIOUS_VALUE
    */
   List<EntryModifiedEventContentFlag> getEventContentFlags();
}
