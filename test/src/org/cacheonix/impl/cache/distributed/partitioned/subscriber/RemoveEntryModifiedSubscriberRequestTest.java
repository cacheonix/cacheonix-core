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
import org.cacheonix.TestUtils;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.array.HashSet;
import org.mockito.Mockito;

/**
 * Tester for AddEntryModifiedSubscriberRequest.
 */
public final class RemoveEntryModifiedSubscriberRequestTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "test.cache";

   private static final ClusterNodeAddress SUBSCRIBER_ADDRESS = TestUtils.createTestAddress();

   private static final HashSet<Binary> KEYS = createKeys();  // NOPMD

   private static final EntryModifiedSubscriber SUBSCRIBER = Mockito.mock(EntryModifiedSubscriber.class);

   private RemoveEntryModifiedSubscriberRequest request;


   public void testSetKeys() throws Exception {

      assertEquals(KEYS, request.getKeys());
   }


   public void testSetSubscriber() throws Exception {

      assertSame(SUBSCRIBER, request.getSubscriber());
   }


   public void testSetSubscriberAddress() throws Exception {

      assertEquals(SUBSCRIBER_ADDRESS, request.getSubscriberAddress());
   }


   public void testToString() throws Exception {

      assertNotNull(request.toString());
   }


   public void testGetWireableType() {

      assertEquals(Wireable.TYPE_CACHE_REMOVE_ENTRY_MODIFIED_SUBSCRIBER_REQUEST, request.getWireableType());
   }


   protected void setUp() throws Exception {

      super.setUp();

      request = new RemoveEntryModifiedSubscriberRequest(CACHE_NAME);
      request.setSubscriber(SUBSCRIBER);
      request.setSubscriberAddress(SUBSCRIBER_ADDRESS);
      request.setKeys(KEYS);
   }


   private static HashSet<Binary> createKeys() {  // NOPMD

      final HashSet<Binary> result = new HashSet<Binary>(1);
      result.add(toBinary("key"));
      return result;
   }
}
