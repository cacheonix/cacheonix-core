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

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriber;
import org.cacheonix.impl.util.array.HashSet;

/**
 * A tracker of keys a subscriber is subscribed to.
 * <p/>
 * The main use of it is to track the situation when repeated partial un-subscriptions lead to the situation when a
 * subscriber is not subscribed to any keys and should be removed from the local CacheProcessor state.
 *
 * @see CacheProcessor#getLocalEntryModifiedSubscriptions()
 */
public final class LocalSubscription {

   private final HashSet<Binary> keys = new HashSet<Binary>(1); // NOPMD

   private BinaryEntryModifiedSubscriber subscriber = null;


   public BinaryEntryModifiedSubscriber getSubscriber() {

      return subscriber;
   }


   public void setSubscriber(final BinaryEntryModifiedSubscriber subscriber) {

      this.subscriber = subscriber;
   }


   public boolean hasKeys() {

      return !keys.isEmpty();
   }


   public void addKeys(final HashSet<Binary> keys) { // NOPMD

      this.keys.addAll(keys);
   }


   public boolean containsKey(final Binary keyToUnSubscribe) {

      return keys.contains(keyToUnSubscribe);
   }


   public void removeKey(final Binary keyToUnSubscribe) {

      keys.remove(keyToUnSubscribe);
   }


   public String toString() {

      return "LocalSubscription{" +
              "keys=" + keys +
              ", subscriber=" + subscriber +
              '}';
   }
}
