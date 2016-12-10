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
package org.cacheonix.impl.net.tcp;

import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.util.array.HashMap;

final class SenderSelectorWorker extends SelectorWorker {

   /**
    * This cluster node's clock. The clock is used to time stamp sent messages.
    */
   private final Clock clock;


   /**
    * Map of addresses to senders.
    */
   private final Map<ReceiverAddress, SenderKeyHandler> senders = new HashMap<ReceiverAddress, SenderKeyHandler>(111);

   /**
    * Local address.
    */
   private final ClusterNodeAddress localAddress;

   /**
    * Cluster processor to post failed requests back to.
    */
   private final Router router;

   /**
    * The message queue to process.
    */
   private final ConcurrentLinkedQueue<Message> queue;


   /**
    * @param localAddress          the local address.
    * @param selector              the selector to process.
    * @param queue                 the  queue with messages to send.
    * @param router                the cluster processor.
    * @param networkTimeoutMillis  the network timeout in milliseconds.
    * @param selectorTimeoutMillis the time the selector should block for while waiting for a channel to become ready,
    *                              must be greater than zero.
    * @param clock                 the cluster node's clock.
    */
   @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
   SenderSelectorWorker(final ClusterNodeAddress localAddress, final Selector selector,
           final ConcurrentLinkedQueue<Message> queue, final Router router,
           final long networkTimeoutMillis, final long selectorTimeoutMillis, final Clock clock) {

      super(selector, networkTimeoutMillis, selectorTimeoutMillis);
      this.localAddress = localAddress;
      this.router = router;
      this.queue = queue;
      this.clock = clock;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation pre-processes selection call by dispatching messages in the input queue to corresponding
    * message senders.
    * @param selectedKeyCount
    */
   protected final void processSelection(final int selectedKeyCount) throws InterruptedException {

      // Dispatch messages from the input queue to senders.
      dispatchToSenders();

      // Process selection
      super.processSelection(selectedKeyCount);
   }


   /**
    * Dispatches messages in the input queue to senders.
    */
   private final void dispatchToSenders() {

      if (queue.isEmpty()) {
         return;
      }

      for (final Iterator<Message> iterator = queue.iterator(); iterator.hasNext(); ) {

         // Get next message
         final Message message = iterator.next();

         // Dispatch the message
         dispatchToSender(message);

         // Remove
         iterator.remove();
      }
   }


   /**
    * Dispatches a message by equeuing it to senders according to it receiver list.
    *
    * @param message a message to dispatch.
    */
   private void dispatchToSender(final Message message) {


      // Don't dispatch local messages, they are handled by the router.
      final ReceiverAddress receiverAddress = message.getReceiver();
      if (localAddress.equals(message.getSender()) && receiverAddress.isAddressOf(localAddress)) {

         return;
      }


      SenderKeyHandler senderKeyHandler = senders.get(receiverAddress);
      if (senderKeyHandler == null) {

         // SenderKeyHandler does not exist - create
         senderKeyHandler = new SenderKeyHandler(selector, receiverAddress, router, networkTimeoutMillis, clock);

         // Register the senderKeyHandler
         senders.put(receiverAddress, senderKeyHandler);
      }

      // Enqueue message
      senderKeyHandler.enqueue(message);
   }
}
