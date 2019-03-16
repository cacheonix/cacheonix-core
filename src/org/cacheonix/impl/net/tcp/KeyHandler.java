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

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.util.time.Timeout;
import org.cacheonix.impl.util.time.TimeoutImpl;

/**
 * A handler of NIO key events.
 */
abstract class KeyHandler {


   /**
    * Timeout tracker.
    */
   private final Timeout networkTimeout;

   /**
    * A selector this handler is associated with.
    */
   protected final Selector selector;


   /**
    * This cluster node's clock. The clock is used to synchronise cluster time by timestamping sent messages and
    * adjusting the clock based on the time stamps of received messages.
    */
   protected final Clock clock;


   /**
    * Creates a Key handler.
    *
    * @param selector             a selector this key is associated with. The selector is available to implementing
    *                             classes through {@link #selector}.
    * @param networkTimeoutMillis the network timeout in milliseconds.
    * @param clock                This cluster node's clock. The clock is used to synchronise cluster time by
    *                             timestamping sent messages and adjusting the clock based on the time stamps of
    *                             received messages.
    */
   KeyHandler(final Selector selector, final long networkTimeoutMillis, final Clock clock) {

      this(selector, new TimeoutImpl(networkTimeoutMillis).reset(), clock);
   }


   /**
    * Creates a Key handler.
    *
    * @param selector a selector this key is associated with. The selector is available to implementing classes through
    *                 {@link #selector}.
    * @param timeout  a timeout to manage idle and inactivity timeouts.
    * @param clock    This cluster node's clock. The clock is used to synchronise cluster time by timestamping sent
    */
   KeyHandler(final Selector selector, final Timeout timeout, final Clock clock) {


      this.networkTimeout = timeout;
      this.selector = selector;
      this.clock = clock;
   }


   public abstract void handleKey(final SelectionKey key) throws InterruptedException;


   /**
    * Processes a key being idle
    *
    * @param idleKey key to process
    * @throws InterruptedException if this thread was interrupted.
    */
   public abstract void handleIdle(SelectionKey idleKey) throws InterruptedException;


   /**
    * Registers the fact that there was an activity on this channel by resetting the timeout.
    */
   final void registerActivity() {

      // Start the new timeout cycle
      networkTimeout.reset();
   }


   /**
    * Registers the fact that there wasn't any activity for some time. This method fires <code>handleTimeout()</code> if
    * the inactivity timeout has been reached. The timeout is reset after calling <code>handleTimeout()</code>.
    *
    * @param key the key this key handler is associated with.
    */
   final void registerInactivity(final SelectionKey key) {

      if (networkTimeout.isExpired()) {

         networkTimeout.reset();

         handleTimeout(key);
      }
   }


   /**
    * Returns a configured network timeout.
    *
    * @return a configured network timeout.
    */
   final long getNetworkTimeoutMillis() {

      return networkTimeout.getDuration();
   }


   /**
    * Processes timeout.
    *
    * @param key a key to process.
    */
   protected abstract void handleTimeout(final SelectionKey key);


   /**
    * Returns a selector this handler is associated with.
    *
    * @return the selector this handler is associated with.
    * @see #KeyHandler(Selector, long, Clock)
    */
   protected final Selector selector() {

      return selector;
   }


   /**
    * Returns a socket channel associated with a given selection key.
    *
    * @param key the key to get the associated socket channel from.
    * @return the socket channel.
    */
   static SocketChannel socketChannel(final SelectionKey key) {

      return (SocketChannel) key.channel();
   }


   public String toString() {

      return "KeyHandler{" +
              "networkTimeout=" + networkTimeout +
              '}';
   }
}
