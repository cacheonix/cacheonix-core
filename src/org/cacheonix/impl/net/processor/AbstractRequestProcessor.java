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

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.Timer;

import org.cacheonix.exceptions.CacheonixException;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Server is a top processor that is responsible for servicing input request coming from the network and output requests
 * coming to the network.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection ThrowableResultOfMethodCallIgnored
 * @since Jul 15, 2009 3:40:14 PM
 */
public abstract class AbstractRequestProcessor extends SimpleProcessor implements RequestProcessor {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AbstractRequestProcessor.class); // NOPMD

   /**
    * Stateless no-waiter waiter.
    */
   protected static final NowaitWaiter NOWAIT_WAITER = new NowaitWaiter();

   /**
    * Process ID of this cluster member.
    */
   private final ClusterNodeAddress address;


   private final Set<InetAddress> localInetAddresses;

   /**
    * Waiter list.
    */
   private final WaiterList waiterList;

   /**
    * Timer.
    */
   private final Timer timer;

   /**
    * The router that is responsible for placing a message being sent to an input queue of a proper local processor or
    * to a processor that serves sending messages out.
    */
   private final Router router;

   /**
    * Cluster-wide clock.
    */
   private final Clock clock;


   protected AbstractRequestProcessor(final Clock clock, final Timer timer, final String name,
           final ClusterNodeAddress address,
           final Router router) {

      super(name);
      this.localInetAddresses = createLocalInetAddresses();
      this.waiterList = new WaiterList(timer);
      this.address = address;
      this.router = router;
      this.timer = timer;
      this.clock = clock;
   }


   public final ClusterNodeAddress getAddress() {

      return address;
   }


   public final Router getRouter() {

      return router;
   }


   public Set<InetAddress> getLocalInetAddresses() {

      return localInetAddresses;
   }


   public final WaiterList getWaiterList() {

      return waiterList;
   }


   /**
    * Processes command by dispatching command processing to <code>super.processCommand()</code> and message processing
    * to <code>processMessage()</code>.
    *
    * @param command the command to process
    * @throws InterruptedException
    * @throws IOException
    * @see #processMessage(Message)
    * @see Message
    */
   protected final void processCommand(final Command command) throws InterruptedException, IOException {

      if (command instanceof Message) {
         processMessage((Message) command);
      } else {
         super.processCommand(command);
      }
   }


   public void processMessage(final Message message) throws InterruptedException, IOException {

      //noinspection ControlFlowStatementWithoutBraces
//      if (LOG.isDebugEnabled()) LOG.debug("message: " + message); // NOPMD

      // Set context
      message.setProcessor(this);


      try {

         if (message instanceof Prepareable) {

            // Prepare
            final Prepareable prepareableMessage = (Prepareable) message;

            if (prepareableMessage.isPrepared()) {

               // Validate
               message.validate();

               // Execute
               message.execute();
            } else {

               // Not prepared yet

               final PrepareResult prepareResult = prepareableMessage.prepare();
               prepareableMessage.markPrepared();

               // Process according to prepare results
               if (prepareResult.equals(PrepareResult.EXECUTE)) {

                  // Validate
                  message.validate();

                  // Execute
                  message.execute();
               } else if (prepareResult.equals(PrepareResult.BREAK)) {

                  //noinspection UnnecessaryReturnStatement
                  return; // NOPMD
               } else if (prepareResult.equals(PrepareResult.ROUTE)) {

                  route(message);
               } else {
                  throw new CacheonixException("Unknown result of prepare: " + prepareResult);
               }
            }

         } else {

            // Validate
            message.validate();

            // Execute
            message.execute();
         }
      } catch (final InvalidMessageException e) {

         LOG.warn("Invalid message: " + e.toString(), e);
         if (message instanceof Request) {

            final Request request = (Request) message;
            if (request.isResponseRequired()) {

               final Response response = request.createResponse(Response.RESULT_ERROR);
               response.setResult(e.toString());
               post(response);
            }
         }
      }
   }


   public final <T> T execute(final Message message) throws RetryException {

      Assert.assertFalse(isProcessorThread(),
              "This method cannot be called from the processor thread because it blocks");

      final ResponseWaiter responseWaiter = route(message);

      //noinspection unchecked
      return (T) responseWaiter.waitForResult();
   }


   public final void post(final Message message) {

      route(message);
   }


   public final void post(final Collection<? extends Message> messages) {

      for (final Message message : messages) {

         post(message);
      }
   }


   public ResponseWaiter route(final Message message) {

      return router.route(message);
   }


   public final void notifyNodeLeft(final ClusterNodeAddress nodeLeftAddress) {

      waiterList.notifyNodeLeft(nodeLeftAddress);
   }


   public final void notifyNodesLeft(final Collection<ClusterNodeAddress> addresses) {

      for (final ClusterNodeAddress address1 : addresses) {
         notifyNodeLeft(address1);
      }
   }


   public void shutdown() {

      // Terminate the processor thread
      super.shutdown();

      // Post-process by having all waiters to return result ShutdownException
      waiterList.notifyShutdown();
   }


   public final Timer getTimer() {

      return timer;
   }


   public Clock getClock() {

      return clock;
   }


   /**
    * Creates an unmodifiable local IP address set.
    *
    * @return an unmodifiable local IP address set.
    */
   private static Set<InetAddress> createLocalInetAddresses() {

      final Set<InetAddress> result = new HashSet<InetAddress>(11);

      try {

         final Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
         while (enumeration.hasMoreElements()) {

            final NetworkInterface networkInterface = enumeration.nextElement();
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {

               result.add(inetAddresses.nextElement());
            }
         }
      } catch (final SocketException ignored) {

         ExceptionUtils.ignoreException(ignored, "Couldn't obtain local addresses");
      }

      return Collections.unmodifiableSet(result);
   }


   public String toString() {

      return "Service{" +
              "clusterNodeAddress=" + address +
              "} " + super.toString();
   }
}
