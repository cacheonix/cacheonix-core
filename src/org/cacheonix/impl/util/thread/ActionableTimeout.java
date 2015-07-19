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

import java.util.Timer;

import org.cacheonix.impl.util.Shutdownable;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Actionable timeout is a timeout that executes an action upon timeout.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 6, 2008 12:07:26 AM
 */
public abstract class ActionableTimeout implements Shutdownable {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ActionableTimeout.class); // NOPMD

   /**
    * Timer
    */
   private Timer timer = null;

   /**
    * Scheduled action.
    */
   private TimeoutAction timeoutAction = null;


   /**
    * Constructor.
    *
    * @param timer the timer.
    */
   public ActionableTimeout(final Timer timer) {

      this.timer = timer;
   }


   /**
    * Cancels current waiting cycle. This method may be called whether or not this Timeout is actually waiting or not.
    */
   public final void cancel() {

      if (timeoutAction != null) {

         timeoutAction.cancel();
         timeoutAction = null;
         timer.purge();
      }
   }


   /**
    * Begins a new waiting cycle. This method starts a new waiting cycle regardless of whether this timeout is already
    * waiting.
    */
   public final void reset() {

      // Cancel
      cancel();

      // Create action
      timeoutAction = createTimeoutAction();

      // Schedule
      timer.schedule(timeoutAction, timeoutAction.getTimeoutMillis());
   }


   /**
    * Creates a Runnable that will be invoked when timeout is reached.
    *
    * @return the Runnable that will be invoked when timeout is reached.
    */
   protected abstract TimeoutAction createTimeoutAction();


   /**
    * Shutdowns the internal timeout thread.
    */
   public final void shutdown() {

      cancel();
   }


   public String toString() {

      return "ActionableTimeout{" +
              "timeoutAction=" + timeoutAction +
              ", timer=" + timer +
              '}';
   }
}
