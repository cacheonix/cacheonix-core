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
package org.cacheonix.impl.cluster.node;

import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessor;
import org.cacheonix.impl.cache.distributed.partitioned.subscriber.AddRemoteEntryModifiedSubscriberMessage;
import org.cacheonix.impl.cache.distributed.partitioned.subscriber.EntryModifiedSubscription;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.IntObjectProcedure;
import org.cacheonix.impl.util.array.ObjectObjectProcedure;

/**
 * Who reacts to removal?
 */
public final class EntryModifiedSubscriptionRemover {


   @SuppressWarnings("MethodMayBeStatic")
   public void remove(final CacheProcessor cacheProcessor,
                      final IntObjectHashMap<HashMap<Binary, HashSet<EntryModifiedSubscription>>> modificationSubscriptions,
                      final ClusterNodeAddress addressToRemove) {

      //
      // Remove from entry modification subscriptions
      //
      modificationSubscriptions.retainEntries(new IntObjectProcedure<HashMap<Binary, HashSet<EntryModifiedSubscription>>>() {

         public boolean execute(final int bucketNumber,
                                final HashMap<Binary, HashSet<EntryModifiedSubscription>> subscriptionMap) {  // NOPMD

            // Remove entries that have subscriber address as leaving
            subscriptionMap.retainEntries(new ObjectObjectProcedure<Binary, HashSet<EntryModifiedSubscription>>() {

               public boolean execute(final Binary key,
                                      final HashSet<EntryModifiedSubscription> subscriptions) {  // NOPMD

                  // Remove subscribers that have subscriber address as leaving
                  for (final EntryModifiedSubscription subscription : subscriptions) {

                     if (addressToRemove.equals(subscription.getSubscriberAddress())) {

                        // Remove subscription
                        subscriptions.remove(subscription);

                        // Post to the processor
                        final AddRemoteEntryModifiedSubscriberMessage registerSubscriptionMessage = new AddRemoteEntryModifiedSubscriberMessage(cacheProcessor.getCacheName());
                        registerSubscriptionMessage.setBucketNumber(bucketNumber);
                        registerSubscriptionMessage.setSubscription(subscription);
                        registerSubscriptionMessage.setKey(key);

                        cacheProcessor.post(registerSubscriptionMessage);
                     }
                  }

                  return !subscriptions.isEmpty();
               }
            });

            return !subscriptionMap.isEmpty();
         }
      });


   }
}
