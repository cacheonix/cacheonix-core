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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriber;
import org.cacheonix.impl.util.array.HashSet;
import org.mockito.Mockito;

/**
 * Tester for LocalSubscriptionTest.
 */
public final class LocalSubscriptionTest extends CacheonixTestCase {

   private LocalSubscription subscription;


   public void testSetGetSubscriber() throws Exception {

      final BinaryEntryModifiedSubscriber subscriber = Mockito.mock(BinaryEntryModifiedSubscriber.class);
      subscription.setSubscriber(subscriber);
      assertSame(subscriber, subscription.getSubscriber());
   }


   public void testAddKeys() throws Exception {

      final HashSet<Binary> keys = new HashSet<Binary>(1);
      final Binary key = toBinary("key");
      keys.add(key);
      subscription.addKeys(keys);
      assertTrue(subscription.containsKey(key));
   }


   public void testContainsKey() throws Exception {

      final HashSet<Binary> keys = new HashSet<Binary>(1);
      final Binary key = toBinary("key");
      keys.add(key);
      subscription.addKeys(keys);
      assertTrue(subscription.containsKey(key));
   }


   public void testRemoveKey() throws Exception {

      final HashSet<Binary> keys = new HashSet<Binary>(1);
      final Binary key = toBinary("key");
      keys.add(key);
      subscription.addKeys(keys);
      subscription.removeKey(key);
      assertTrue(!subscription.containsKey(key));
   }


   public void testToString() throws Exception {

      assertNotNull(subscription.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      subscription = new LocalSubscription();
   }
}
