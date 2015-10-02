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
package org.cacheonix.impl.cache.listener;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.subscriber.BinaryEntryModifiedEvent;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * BinaryEntryModifiedEvent Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>08/27/2008</pre>
 */
public final class BinaryEntryModifiedEventTest extends CacheonixTestCase {

   private static final TimeImpl LAST_UPDATE_TIME_MILLIS = new TimeImpl(1L, 0);

   private static final long VERSION = 2L;

   private static final CacheMember UPDATER = null;

   private static final EntryModifiedEventType UPDATE_TYPE = EntryModifiedEventType.ADD;

   private static final Binary BINARY_KEY = toBinary("key");

   private static final Binary BINARY_NEW_VALUE = toBinary("value");

   private static final Binary BINARY_PREVIOUS_VALUE = toBinary("previous_value");

   private BinaryEntryModifiedEvent event = null;


   public void testGetUpdateType() throws Exception {

      assertEquals(UPDATE_TYPE, event.getUpdateType());
   }


   public void testGetKey() throws Exception {

      assertEquals(BINARY_KEY, event.getUpdatedKey());
   }


   public void testGetValue() throws Exception {

      assertEquals(BINARY_NEW_VALUE, event.getNewValue());
   }


   public void testGetPreviousValue() throws Exception {

      assertEquals(BINARY_PREVIOUS_VALUE, event.getPreviousValue());
   }


   public void testGetLastUpdateTimeMillis() throws Exception {

      assertEquals(LAST_UPDATE_TIME_MILLIS, event.getLastUpdateTime());
   }


   public void testGetVersion() throws Exception {

      assertEquals(VERSION, event.getVersion());
   }


   public void testGetUpdater() throws Exception {

      assertEquals(UPDATER, event.getUpdater());
   }


   public void testToString() {

      assertNotNull(event.toString());
   }


   public void testHashCode() {

      assertTrue(event.hashCode() != 0);
   }


   public void testWriteReadExternal() throws IOException, ClassNotFoundException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(event);
      assertEquals(event, serializer.deserialize(bytes));
   }


   protected void setUp() throws Exception {

      super.setUp();
      event = new BinaryEntryModifiedEvent(UPDATE_TYPE, BINARY_KEY, BINARY_NEW_VALUE, BINARY_PREVIOUS_VALUE, LAST_UPDATE_TIME_MILLIS, VERSION, UPDATER);
   }


   public String toString() {

      return "BinaryEntryModifiedEventTest{" +
              "event=" + event +
              "} " + super.toString();
   }
}
