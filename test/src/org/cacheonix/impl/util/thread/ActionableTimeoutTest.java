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
package org.cacheonix.impl.util.thread;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.util.MutableBoolean;

/**
 * ActionableTimeout Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/06/2008</pre>
 */
public final class ActionableTimeoutTest extends CacheonixTestCase {

   private final MutableBoolean called = new MutableBoolean();

   private ActionableTimeout actionableTimeout;

   private static final int DELAY = 100;


   public void testToString() {

      assertNotNull(actionableTimeout.toString());
   }


   public void testReset() throws InterruptedException {

      actionableTimeout.reset();
      final long delay = (long) (DELAY << 2);
      Thread.sleep(delay);
      assertTrue(called);
   }


   public void testNoTimeout() throws InterruptedException {

      Thread.sleep((long) (DELAY << 1));
      assertFalse(called);
   }


   protected void setUp() throws Exception {

      super.setUp();

      actionableTimeout = new ActionableTimeout(getTimer()) {

         protected TimeoutAction createTimeoutAction() {

            return new TimeoutAction(DELAY) {

               public void run() {

                  called.set(true);
               }
            };
         }
      };
   }


   public String toString() {

      return "ActionableTimeoutTest{" +
              "actionableTimeout=" + actionableTimeout +
              ", called=" + called +
              "} " + super.toString();
   }
}
