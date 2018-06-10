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
package org.cacheonix.impl.net.cluster;

import junit.framework.TestCase;
import org.cacheonix.impl.cache.distributed.partitioned.ClearFrontCacheBucketAnnouncement;
import org.cacheonix.impl.net.processor.Request;

/**
 * Tester for DeliverNotificationEntry.
 */
public final class DeliveryNotificationEntryTest extends TestCase {

   private static final int START_FRAME_NUMBER = 7777;

   private Request request;

   private DeliveryNotificationEntry entry;


   public void testGetCreate() {

      assertEquals(request, entry.getRequest());
      assertFalse(entry.hasStartFrameNumber());


   }


   public void testSetGetStartFrameNumber() {

      entry.setStartFrameNumber(START_FRAME_NUMBER);
      assertTrue(entry.hasStartFrameNumber());
      assertEquals(START_FRAME_NUMBER, entry.getStartFrameNumber());
   }


   public void setUp() throws Exception {

      super.setUp();

      request = new ClearFrontCacheBucketAnnouncement("cache.name");

      entry = new DeliveryNotificationEntry(request);
   }


   public void tearDown() throws Exception {

      request = null;
      entry = null;

      super.tearDown();
   }
}
