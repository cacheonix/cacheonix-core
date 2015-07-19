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
package org.cacheonix.impl.net.processor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * WaiterList
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 8, 2009 7:08:25 PM
 */
public final class WaiterList {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(WaiterList.class); // NOPMD

   /**
    * Timer used to kill requests that have a timeout set.
    */
   private final Timer timer;


   /**
    * A registry of waiters.
    */
   private final Map<UUID, Waiter> waiters = new ConcurrentHashMap<UUID, Waiter>(1);


   /**
    * Creates a new waiter list.
    *
    * @param timer a timer.
    */
   public WaiterList(final Timer timer) {

      this.timer = timer;
   }


   /**
    * Registers this waiter. Upon return the waiter may have already finished.
    * <p/>
    * <b>Concurrency:</b> <br/> While other waiter's methods are called by the processor thread, <code>register()</code<
    * one can be called by a client thread. That's why we have to use <code>ConcurrentHashMap</code>.
    *
    * @param waiter waiter to register
    */
   public void register(final Waiter waiter) {

      // NOTE: simeshev@cacheonix.org - 2011-08-08 - Sending the message to failed process
      // will cause the sender to respond with failure so the waiter will finish even if
      // the destination is gone.

      if (waiter.isFinished()) {

         return;
      }

      // Register waiter
      final UUID requestUUID = waiter.getRequestUUID();
      waiters.put(requestUUID, waiter);

      // Schedule timeout
      if (waiter.getRequest().hasTimeout() && waiter.getRequest().getTimeoutMillis() > 0L) {

         final RequestProcessor processor = waiter.getRequest().getProcessor();
         final TimeoutTask timerTask = new TimeoutTask(processor, requestUUID);
         timer.schedule(timerTask, waiter.getRequest().getTimeoutMillis());
      }
   }


   public final void unregister(final Waiter waiter) {

      waiters.remove(waiter.getRequestUUID());
   }


   public void notifyReceived(final Response response) throws InterruptedException {

      // Check if this is a response
      final UUID responseToUUID = response.getResponseToUUID();
      if (responseToUUID == null) {
         return;
      }

      // Try to find the waiter
      final Waiter waiter = waiters.get(responseToUUID);
      if (waiter == null) {
         return;
      }

      // Notify the waiter the a response is received
      waiter.notifyResponseReceived(response);

      // If waiter is done, remove it from the waiter list
      if (waiter.isFinished()) {

         unregister(waiter);
      }
   }


   /**
    * Notifies the waiter list that a given node left.
    *
    * @param nodeAddress an address of the cluster node that has left.
    */
   public void notifyNodeLeft(final ClusterNodeAddress nodeAddress) {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Received a notice that a node left: " + nodeAddress);
      }

      // Get a copy of the waiter list
      final LinkedList<Map.Entry<UUID, Waiter>> copy = new LinkedList<Map.Entry<UUID, Waiter>>(waiters.entrySet());

      // Remove waiters that have finished as a result of a node leaving
      while (!copy.isEmpty()) {

         final Waiter waiter = copy.removeFirst().getValue();
         waiter.notifyNodeLeft(nodeAddress);
         if (waiter.isFinished()) {

            waiters.remove(waiter.getRequestUUID());
         }
      }
   }


   public void notifyTimeout(final UUID requestUUID) {

      // Try to find the waiter
      final Waiter waiter = waiters.get(requestUUID);
      if (waiter == null) {
         return;
      }

      // Notify the waiter the timeout is received
      waiter.notifyTimeout();

      // If waiter is done, remove it from the waiter list
      if (waiter.isFinished()) {

         waiters.remove(requestUUID);
      }
   }


   /**
    * Finished all waiters with ShutdownException.
    *
    * @see RequestProcessor#shutdown()
    */
   @SuppressWarnings("ThrowableInstanceNeverThrown")
   public final void notifyShutdown() {

      // Finish all waiters left with ShutdownException
      for (final Iterator<Map.Entry<UUID, Waiter>> iterator = waiters.entrySet().iterator(); iterator.hasNext(); ) {
         final Waiter waiter = iterator.next().getValue();
         waiter.notifyShutdown();
         iterator.remove();
      }
   }


   /**
    * Finished all waiters with ShutdownException.
    *
    * @see RequestProcessor#shutdown()
    */
   public void notifyReset() {

      // Finish all waiters left with ReconfigurationException
      for (final Iterator<Map.Entry<UUID, Waiter>> iterator = waiters.entrySet().iterator(); iterator.hasNext(); ) {

         iterator.next().getValue().notifyReset();
         iterator.remove();
      }
   }


   public String toString() {

      return "WaiterList{" +
              "waiters=" + waiters +
              '}';
   }
}
