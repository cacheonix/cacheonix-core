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
package org.cacheonix.impl.cache.distributed.partitioned.subscriber;

import java.util.Collections;
import java.util.Set;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.util.array.HashSet;

/**
 * Tester for AddRemoteEntryModifiedSubscriberMessage.
 */
public final class AddRemoteEntryModifiedSubscriberMessageTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "test.cache";

   private static final int BUCKET_NUMBER = 555;

   private static final Binary KEY = toBinary("key");

   private static final EntryModifiedSubscription SUBSCRIPTION = new EntryModifiedSubscription(5432, TestUtils.createTestAddress(333), EntryModifiedNotificationMode.SINGLE, Collections.singletonList(EntryModifiedEventContentFlag.NEED_ALL), createModificationTypes());


   private AddRemoteEntryModifiedSubscriberMessage message;


   public void testSetBucketNumber() throws Exception {

      assertEquals(BUCKET_NUMBER, message.getBucketNumber());
   }


   public void testSetKey() throws Exception {

      assertEquals(KEY, message.getKey());
   }


   public void testSetSubscription() throws Exception {

      assertSame(SUBSCRIPTION, message.getSubscription());
   }


   public void testToString() throws Exception {

      assertNotNull(message.toString());
   }


   private static Set<EntryModifiedEventType> createModificationTypes() {

      final HashSet<EntryModifiedEventType> eventTypes = new HashSet<EntryModifiedEventType>(4, 0.75f);
      eventTypes.add(EntryModifiedEventType.ADD);
      eventTypes.add(EntryModifiedEventType.EVICT);
      eventTypes.add(EntryModifiedEventType.REMOVE);
      eventTypes.add(EntryModifiedEventType.UPDATE);
      return eventTypes;
   }


   protected void setUp() throws Exception {

      super.setUp();

      message = new AddRemoteEntryModifiedSubscriberMessage(CACHE_NAME);
      message.setBucketNumber(BUCKET_NUMBER);
      message.setKey(KEY);
      message.setSubscription(SUBSCRIPTION);
   }
}
