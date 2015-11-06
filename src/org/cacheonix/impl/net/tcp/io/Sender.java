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
package org.cacheonix.impl.net.tcp.io;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.AbstractProcessor;
import org.cacheonix.impl.net.processor.Command;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.logging.Logger;

/**
 * MessageSender  is a processor that is responsible for sending messages to the outside network. In other words,
 * message sender's input queue is a sink.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 18, 2009 3:24:31 PM
 */
public final class Sender extends AbstractProcessor {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Sender.class); // NOPMD

   /**
    * This cluster node's clock. The clock is used to time stamp sent messages.
    */
   private final Clock clock;


   /**
    * Local address.
    */
   private final ClusterNodeAddress localAddress;

   /**
    * Time the NIO selector should block for while waiting for a channel to become ready, must be greater than zero.
    * Majority of Cacheonix configuration should leave 'selectorTimeout' default.
    */
   private final long selectorTimeoutMillis;

   /**
    * Network timeout in milliseconds.
    */
   private final long networkTimeoutMillis;

   /**
    * Cluster service is responsible for sending reliable mcast messages and for managing cluster membership.
    */
   private Router router = null;

   /**
    * Selector.
    */
   private final Selector selector;


   /**
    * Message queue.
    */
   private final ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<Message>();


   /**
    * Creates message sender.
    *
    * @param localAddress          the localAddress.
    * @param networkTimeoutMillis  a network timeout in milliseconds.
    * @param selectorTimeoutMillis a time the selector should block for while waiting for a channel to become ready,
    *                              must be greater than zero.
    * @param clock                 the clock.
    */
   public Sender(final ClusterNodeAddress localAddress, final long networkTimeoutMillis,
           final long selectorTimeoutMillis, final Clock clock) throws IOException {

      super("Sender:" + localAddress.getTcpPort());
      this.selectorTimeoutMillis = selectorTimeoutMillis;
      this.networkTimeoutMillis = networkTimeoutMillis;
      this.localAddress = localAddress;
      this.selector = Selector.open();
      this.clock = clock;
   }


   public void enqueue(final Command command) {

      // Can only handle commands
      final Message message = (Message) command;

//      //noinspection ControlFlowStatementWithoutBraces
//      if (LOG.isDebugEnabled()) LOG.debug("Enqueueing (queue size is " + queue.size() + ") : " + message); // NOPMD

      Assert.assertNotNull(message.getClusterUUID(), "Cluster UUID must be set, message: {0}", message);

      // Enqueue
      queue.add(message);

      // Let selector know that there is work to do
      selector.wakeup();
   }


   /**
    * Sets the router used to send responses to failed request.
    *
    * @param router router used to send responses to failed request.
    */
   public void setRouter(final Router router) {

      this.router = router;
   }


   protected Runnable createWorker() {

      return new SenderSelectorWorker(localAddress, selector, queue, router, networkTimeoutMillis,
              selectorTimeoutMillis, clock);
   }


   public String toString() {

      return "MessageSender{" +
              "address=" + localAddress +
              ", selector=" + selector +
              "} " + super.toString();
   }
}
