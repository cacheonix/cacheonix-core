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
package org.cacheonix.impl.util.thread;

import org.cacheonix.impl.RuntimeInterruptedException;
import org.cacheonix.impl.util.logging.Logger;

/**
 * ThreadUtils
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 19, 2009 11:10:06 PM
 */
public final class ThreadUtils {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ThreadUtils.class); // NOPMD


   private ThreadUtils() {

   }


   /**
    * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of
    * milliseconds. The thread does not lose ownership of any monitors.
    *
    * @param millis the length of time to sleep in milliseconds.
    * @throws RuntimeInterruptedException if another thread has interrupted the current thread.  The <i>interrupted
    *                                     status</i> of the current thread is preserved when this exception is thrown.
    */
   public static void sleep(final long millis) throws RuntimeInterruptedException {

      try {
         Thread.sleep(millis);
      } catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new RuntimeInterruptedException(e);
      }
   }


   /**
    * Interrupts a given thread and tries to join it.
    *
    * @param thread            the thread to interrupt.
    * @param joinTimeoutMillis a join timeout.
    */
   public static void interruptAndJoin(final Thread thread, final long joinTimeoutMillis) {

      thread.interrupt();
      try {

         thread.join(joinTimeoutMillis);
      } catch (final InterruptedException ignored) {

         Thread.currentThread().interrupt();
      }
   }
}
