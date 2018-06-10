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
package org.cacheonix.impl.cache.subscriber;

import java.util.List;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.clock.TimeImpl;

/**
 * Tester for LocalEntryModifiedEvent.
 */
public final class LocalEntryModifiedEventTest extends CacheonixTestCase {

   private static final EntryModifiedEventType UPDATE_TYPE = EntryModifiedEventType.UPDATE;

   private static final String UPDATED_KEY = "updated-key";

   private static final String NEW_VALUE = "new-value";

   private static final String PREVIOUS_VALUE = "previous-value";

   private static final Time LAST_UPDATE_TIME_MILLIS = new TimeImpl(777, 0);

   private static final int VERSION = 888;

   private LocalEntryModifiedEvent event;

   private CacheMember updater;


   public void testGetUpdateType() {

      assertEquals(UPDATE_TYPE, event.getUpdateType());
   }


   public void testGetUpdatedKey() {

      assertEquals(UPDATED_KEY, event.getUpdatedKey());
   }


   public void testGetNewValue() {

      assertEquals(NEW_VALUE, event.getNewValue());
   }


   public void testGetPreviousValue() {

      assertEquals(PREVIOUS_VALUE, event.getPreviousValue());
   }


   public void testGetLastUpdateTimeMillis() {

      assertEquals(LAST_UPDATE_TIME_MILLIS, event.getLastUpdateTime());
   }


   public void testGetVersion() {

      assertEquals(VERSION, event.getVersion());
   }


   public void testGetUpdater() {

      assertEquals(updater, event.getUpdater());
   }


   public void testToString() {

      assertNotNull(event.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      updater = new CacheMember() {

         public List getInetAddresses() {

            return null;
         }


         public String getCacheName() {

            return null;
         }
      };

      event = new LocalEntryModifiedEvent(UPDATE_TYPE, toBinary("updated-key"), toBinary("new-value"), toBinary("previous-value"), LAST_UPDATE_TIME_MILLIS, VERSION, updater);
   }
}
