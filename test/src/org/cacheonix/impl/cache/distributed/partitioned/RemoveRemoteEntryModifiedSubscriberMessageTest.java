/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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

import java.io.NotSerializableException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * Tester for RemoveRemoteEntryModifiedSubscriberMessage.
 */
public final class RemoveRemoteEntryModifiedSubscriberMessageTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "test.cache";

   private static final int BUCKET_NUMBER = 1;

   private static final Binary KEY = toBinary("key");

   private static final int SUBSCRIBER_IDENTITY = 2;

   private RemoveRemoteEntryModifiedSubscriberMessage message;


   public void testSetBucketNumber() {

      assertEquals(BUCKET_NUMBER, message.getBucketNumber());
   }


   public void testDefaultConstructor() {

      assertNotNull(new RemoveRemoteEntryModifiedSubscriberMessage().toString());
   }


   public void testSetKey() {

      assertEquals(KEY, message.getKey());
   }


   public void testSetSubscriberIdentity() {

      assertEquals(SUBSCRIBER_IDENTITY, message.getSubscriberIdentity());
   }


   public void testWriteWire() throws Exception {

      boolean thrown = false;
      try {
         SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA).serialize(message);
      } catch (final NotSerializableException e) {
         thrown = true;
      }

      assertTrue("RemoveRemoteEntryModifiedSubscriberMessage is a strictly local object", thrown);
   }


   public void testToString() {

      assertNotNull(message.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      message = new RemoveRemoteEntryModifiedSubscriberMessage(CACHE_NAME);
      message.setSubscriberIdentity(SUBSCRIBER_IDENTITY);
      message.setBucketNumber(BUCKET_NUMBER);
      message.setKey(KEY);
   }
}
