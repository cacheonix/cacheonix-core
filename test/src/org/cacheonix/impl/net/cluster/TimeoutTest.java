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
package org.cacheonix.impl.net.cluster;

import junit.framework.TestCase;
import org.cacheonix.impl.util.time.TimeoutImpl;

/**
 * Timeout Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/26/2008</pre>
 */
public final class TimeoutTest extends TestCase {

   private static final int TIMEOUT_DURATION = 100;

   private static final long SLEEP_DURATION = (long) ((double) TIMEOUT_DURATION * 1.2);

   private TimeoutImpl timeout = null;


   public void testSetGetDuration() throws Exception {
      // Tests duration set at construction
      assertEquals((long) TIMEOUT_DURATION, timeout.getDuration());
   }


   public void testCancel() throws InterruptedException {

      timeout.cancel();
      Thread.sleep(SLEEP_DURATION);
      assertFalse(timeout.isExpired());
   }


   public void testIsExpired() throws InterruptedException {

      timeout.reset();
      Thread.sleep(SLEEP_DURATION);
      assertTrue(timeout.isExpired());
   }


   public void testToString() {

      assertNotNull(timeout.toString());
   }


   public void testHashCode() {
      //TODO: Test goes here...
   }


   protected void setUp() throws Exception {

      super.setUp();
      timeout = new TimeoutImpl((long) TIMEOUT_DURATION);
   }


   public String toString() {

      return "TimeoutTest{" +
              "timeout=" + timeout +
              "} " + super.toString();
   }
}
