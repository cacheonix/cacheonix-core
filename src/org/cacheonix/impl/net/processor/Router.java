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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.cacheonix.ShutdownException;
import org.cacheonix.exceptions.RuntimeInterruptedException;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Routes a message to one of the local processors.
 */
public class Router {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Router.class); // NOPMD

   /**
    * Stateless no-waiter waiter.
    */
   protected static final NowaitWaiter NOWAIT_WAITER = new NowaitWaiter();

   private final ConcurrentHashMap<ProcessorKey, RequestProcessor> localProcessors = new ConcurrentHashMap<ProcessorKey, RequestProcessor>(
           73);

   private final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);

   private final AtomicReference<UUID> clusterUUID = new AtomicReference<UUID>();

   private final ClusterNodeAddress localAddress;

   private Processor output;


   public Router(final ClusterNodeAddress address) {

      this.localAddress = address;
   }


   public void setOutput(final Processor output) {

      this.output = output;
   }


   public void setClusterUUID(final UUID clusterUUID) {

      this.clusterUUID.set(clusterUUID);
   }


   public void register(final ProcessorKey processorKey, final RequestProcessor processor) {

      localProcessors.put(processorKey, processor);
   }


   public void unregister(final ProcessorKey processorKey) {

      localProcessors.remove(processorKey);
   }


   /**
    * When this method is called, routing-wise the message can be in one of the states:
    * <p/>
    * 1. Fully routed: It is a point-to-point message and the destination is set.
    * <p/>
    * 2. It is a multicast message.
    * <p/>
    * 3. It is a point-to-point message and it is not routed.
    *
    * @param message message to route.
    * @return returns a waiter for this message.
    */
   public ResponseWaiter route(final Message message) {


      try {

         // Set sender if not set
         if (!message.isSenderSet()) {

            message.setSender(localAddress);
         }


         // Set cluster UUID if not set
         if (message.getClusterUUID() == null) {

            message.setClusterUUID(clusterUUID.get());
         }


         // Find local processor
         final ProcessorKey processorKey = message.getProcessorKey();
         final RequestProcessor processor = localProcessors.get(processorKey);

         // ---------------------------------------------------
         //
         // If processor not found, respond with error and exit
         //
         // ---------------------------------------------------
         if (processor == null) {

            final Request request = Request.toRequest(message);
            if (request == null) {

               // Node wasn't there, but, because this is a message,
               // not a request, this is silently ignored.
               return NOWAIT_WAITER;
            }

            // Because the actual process is not present, we notify the waiter without registering it.
            if (localAddress.equals(request.getSender())) {

               // ---------------------------------------------------------------------
               //
               // Processor is not found and the sender is *local*. Because the actual
               // process is not present, we notify the waiter without registering from
               // *all* addresses.
               //
               // ---------------------------------------------------------------------

               final Waiter waiter = request.getWaiter();
               if (request.isReceiverSet()) {

                  try {

                     // Use receiver address as a sender address
                     final Response response = createProcessNotFoundResponse(request);

                     // Notify the waiter
                     waiter.notifyResponseReceived(response);
                  } catch (final InterruptedException ignored) {

                     // Restore the flag
                     Thread.currentThread().interrupt();
                  }
               } else {

                  // It is possible when a request was not prepared, so the receiver was not set.
                  // REVIEWME: simeshev@cacheonix.org - 2011-08-08 -> use direct finish instead ?
                  waiter.notifyResponseReceived(createProcessNotFoundResponse(request));
               }

               return waiter;
            } else {

               // ---------------------------------------------------------------------
               //
               // Processor is not found and the sender is *remote*. Becuase this is a
               // request to this address that has failed, we respond only using our
               // address as a sender.
               //
               // ---------------------------------------------------------------------

               final Response response = createProcessNotFoundResponse(request);

               output.enqueue(response);

               return NOWAIT_WAITER;
            }
         }


         // ---------------------------------------------------------------------
         //
         // Processor found, proceed normally
         //
         // ---------------------------------------------------------------------

         // Get waiter list
         final WaiterList waiterList = processor.getWaiterList();

         // Set processor
         message.setProcessor(processor);

         if (message.isReceiverSet()) {


            // ---------------------------------------------------------------------
            //
            // Case 1: Receiver is set. This means that the message must be properly
            // dispatched to its receivers, message sender if remote or/and a local
            // processor if local.
            //
            // --------------------------------------------------------------------

            if (localAddress.equals(message.getSender())) {

               // --------------------------------------------------------------------
               //
               // The sender is local
               //
               // --------------------------------------------------------------------

               Waiter waiter = null;
               if (message instanceof Request) {

                  final Request request = (Request) message;
                  if (request.isResponseRequired()) {

                     waiter = request.getWaiter();
                     waiterList.register(waiter);
                  }
               }

               // Enqueue if not finished
               if (waiter == null || !waiter.isFinished()) {

                  // Optimize for local single-node delivery.
                  if (message.getReceiver().isAddressOf(localAddress)) {

                     // REVIEWME: simeshev@cacheonix.org - 2011-06-07 - Is a local-only request really such a bad thing?
                     if (message instanceof Request && !(message instanceof RouteByReferenceRequest)) {

                        // A deep copy must be enqueued to the local processor made
                        // if this is a multiple receiver-request or a copy-able local request
                        try {

                           processor.enqueue((Command) serializer.deserialize(serializer.serialize(message)));
                        } catch (final Exception e) {

                           respondWithError((Request) message, processor, e.toString());

                           return waiter == null ? NOWAIT_WAITER : waiter;
                        }
                     } else {

                        // Local-only non-request message
                        enqueue(processor, message);
                     }
                  } else {

                     // A message to a remote destination-only
                     output.enqueue(message);
                  }
               }

               // Return waiter
               return waiter == null ? NOWAIT_WAITER : waiter;
            } else {

               // --------------------------------------------------------------------
               //
               // The sender is remote
               //
               // --------------------------------------------------------------------

               enqueue(processor, message);

               // Remove messages cannot have waiters
               return NOWAIT_WAITER;
            }
         } else {

            if (isReliableMcast(message)) {

               // ---------------------------------------------------------------------
               //
               // Case 2: Receiver is not set and this is an announcement AKA reliable
               // mcast message.
               //
               // ---------------------------------------------------------------------
               Waiter waiter = null;
               if (message instanceof Request) {

                  final Request request = (Request) message;
                  if (request.isResponseRequired()) {

                     waiter = request.getWaiter();
                     waiterList.register(waiter);
                  }
               }

               // Add to submittal queue

               // Enqueue if not finished
               if (waiter == null || !waiter.isFinished()) {

                  enqueue(processor, message);
               }

               // Return waiter
               return waiter == null ? NOWAIT_WAITER : waiter;


            } else {

               // ---------------------------------------------------------------------
               //
               // Case 3: Receiver is not set and this is a point-to-point request.
               // The processor must resolve the receiver on its own. This is needed
               // to support a use case where a message may need to access process state
               // to make a decision where for send the message such as a local cache in
               // the cache processor.
               //
               // ---------------------------------------------------------------------

               Waiter waiter = null;
               if (message instanceof Request) {

                  final Request request = (Request) message;
                  if (request.isResponseRequired()) {

                     waiter = request.getWaiter();
                     waiterList.register(waiter);
                  }
               }

               // Enqueue if not finished
               if (waiter == null || !waiter.isFinished()) {

                  enqueue(processor, message);
               }

               // Return waiter
               return waiter == null ? NOWAIT_WAITER : waiter;
            }
         }

      } catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new RuntimeInterruptedException(e);
      }
   }


   private void respondWithError(final Request message, final RequestProcessor processor,
           final String result) throws InterruptedException {

      final Response response = message.createResponse(Response.RESULT_ERROR, result);
      response.setClusterUUID(clusterUUID.get());
      processor.enqueue(response);
   }


   /**
    * Enqueues the message to the processor while handling the possibility of the processor being shutdown by responding
    * to requests with an error response.
    *
    * @param processor the processor to enqueue the message to.
    * @param message   the message to enqueue.
    */
   private void enqueue(final RequestProcessor processor, final Message message) {

      try {

         processor.enqueue(message);
      } catch (final InterruptedException ignored) {

         // Respond if possible
         respondWithShutdownError(message);

         // Restore interrupt status
         Thread.currentThread().interrupt();
      } catch (final ShutdownException ignored) {

         respondWithShutdownError(message);
      }
   }


   private void respondWithShutdownError(final Message message) {

      final Request request = Request.toRequest(message);
      if (request == null) {
         return;
      }

      try {

         //
         if (localAddress.equals(request.getSender())) {

            // This is a local request, respond with an ERROR
            request.getWaiter().notifyShutdown();
         } else {

            // This is a request from the remote sender, try to respond with an INACCESSIBLE result.
            // We use localAddress as as sender because that us who failed to process the request.
            final Response response = request.createResponse(Response.RESULT_INACCESSIBLE);
            response.setResult("Processor has been shutdown: " + request.getProcessorKey());
            response.setClusterUUID(clusterUUID.get());
            response.setSender(localAddress);

            output.enqueue(response);
         }
      } catch (final RuntimeException e) {

         // Do nothing - our attempt to respond failed
         // noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Could not respond with an error: " + e, e); // NOPMD
      } catch (final InterruptedException ignored) {

         // Restore interrupt status
         Thread.currentThread().interrupt();
      }
   }


   protected void initSender(final Message message) {

      if (!message.isSenderSet()) {

         message.setSender(localAddress);
      }
   }


   private static boolean isReliableMcast(final Message message) {

      switch (message.getDestination()) {

         // REVIEWME: simeshev@cacheonix.org - 2010-07-31 -> These destinations may need to be eliminated
         // to unify handling message preparation and responseRequired for reliable mcast messages
         // that require local response (send-to-all-wait-for-response-from local. If so, it would
         // always need to be Message.DESTINATION_CLUSTER
         case Wireable.DESTINATION_REPLICATED_STATE:
         case Wireable.DESTINATION_MULTICAST_CLIENT:
            return true;
         default:
            return false;
      }
   }


   private Response createProcessNotFoundResponse(final Request request) {

      final String errorResult = "Processor not found, key: " + request.getProcessorKey();
      final Response response = request.createResponse(Response.RESULT_RETRY);
      response.setClusterUUID(clusterUUID.get());
      response.setSender(localAddress);
      response.setResult(errorResult);
      return response;
   }
}
