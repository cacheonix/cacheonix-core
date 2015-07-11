/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.net.processor;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.cacheonix.ShutdownException;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Processor is a state machine that processes incoming messages. Processor maintenance an internal thread and an input
 * message queue.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 10, 2009 10:46:34 PM
 */
public class SimpleProcessor extends AbstractProcessor {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SimpleProcessor.class); // NOPMD


   private static final int MAX_BATCH_SIZE = 1000;

   /**
    * Input request queue.
    */
   private final BlockingQueue<Command> inputQueue = new LinkedBlockingQueue<Command>();


   /**
    * Creates a processor.
    *
    * @param processorName a processor name. It is used to create a name of the executor thread.
    */
   public SimpleProcessor(final String processorName) {

      super(processorName);
   }


   /**
    * The exec thread calls this method after pulling a message from the input queue. This method executes the request
    * with the processor's context and notifies the waiter list that a message was received.
    *
    * @param command the command to process
    * @throws InterruptedException if the processing thread was interrupted.
    * @throws IOException          if an I/O error occurred.
    */
   protected void processCommand(final Command command) throws InterruptedException, IOException {

      command.execute();
   }


   /**
    * @return an input queue.
    * @noinspection ReturnOfCollectionOrArrayField
    */
   protected final BlockingQueue<Command> getInputQueue() {

      return inputQueue;
   }


   /**
    * Enqueues the command for execution by this processor.
    *
    * @param command the command to enqueue.
    * @throws InterruptedException if the caller thread has been interrupted.
    * @throws ShutdownException    if the processor is shutdown.
    */
   public void enqueue(final Command command) throws InterruptedException, ShutdownException {

      // Check if already shutdown
      if (isShutdown()) {
         throw new ShutdownException();
      }

      // Enqueue
      inputQueue.put(command);
   }


   /**
    * Creates a runnable executed by the worker thread. The runnable takes a message from the input queue and calls
    * {@link #processCommand(Command)} .
    *
    * @return Runnable
    */
   protected Runnable createWorker() {

      return new Worker();
   }


   public String toString() {

      return "Processor{" +
              "inputQueue=" + inputQueue +
              ", thread=" + thread +
              '}';
   }


   /**
    * @noinspection NonStaticInnerClassInSecureContext
    */
   private final class Worker implements Runnable {

      // DELETEME: simeshev@cacheonix.org 2011-06-06 - Deleted when debugging is done.
      private final Random random = new Random(System.currentTimeMillis());

      /**
       * Number of processed messages.
       */
      private long messagesProcessed = 0L;

      /**
       * Average time to process
       */
      private double averageTimeToProcessNanos = 0;

      /**
       * Maximum number of messages in the queue.
       */
      private int maximumBacklog = 0;


      public final void run() {

         try {

            while (true) {
               try {

                  // Update backlog statistics
                  maximumBacklog = Math.max(maximumBacklog, inputQueue.size());

                  // Wait for message
                  process(inputQueue.take());

                  // Process batch
                  while (!inputQueue.isEmpty()) {

                     final int batchSize = Math.min(MAX_BATCH_SIZE, inputQueue.size());
                     if (batchSize == 1) {

                        // Just use the first (single) one
                        final Command command = inputQueue.poll();
                        if (command != null) {

                           process(command);
                        }
                     } else {

                        // Process as a batch
                        final List<Command> batch = new ArrayList<Command>(batchSize);
                        inputQueue.drainTo(batch, batchSize);

                        for (int i = 0; i < batchSize; i++) {

                           process(batch.get(i));
                        }
                     }
                  }

               } catch (final InterruptedException e) {

                  // Current thread hit a blocking interrupt-aware
                  // method. Terminate this processor.
                  LOG.debug("Processor " + getProcessorName() + " exited by interrupt");

                  // Exit
                  return;

               } catch (final Exception e) {
                  // REVIEWME: simeshev@cacheonix.org - 2009-07-14 -> Decide what to do
                  LOG.error(e, e);
               }
            }
         } finally {

            if (LOG.isDebugEnabled()) {
               final long uptimeSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000L;
               LOG.debug("At worker exit, total messages processed: " + messagesProcessed
                       + ", input queue size: " + inputQueue.size()
                       + ", uptime seconds: " + uptimeSeconds
                       + ", throughput requests/sec: " + messagesProcessed / (uptimeSeconds == 0 ? 1 : uptimeSeconds)
                       + ", average execute time, ms: " + new DecimalFormat("#.###").format(averageTimeToProcessNanos / 1000000)
                       + ", max backlog: " + maximumBacklog
               );
            }
         }
      }


      private void process(final Command command) throws IOException, InterruptedException {

//         Thread.sleep(Math.abs(random.nextInt()) % 5);

         // Increment message counter
         messagesProcessed++;

         // Remember start time for further calculation of average processing time

         final long startTimeNanos = System.nanoTime();
         try {

            // Process
            processCommand(command);
         } finally {

            // Update average time to process
            averageTimeToProcessNanos = ((averageTimeToProcessNanos * (messagesProcessed - 1)) + (System.nanoTime() - startTimeNanos)) / messagesProcessed;
         }
      }


      public String toString() {

         return "Worker{" +
                 "messagesProcessed=" + messagesProcessed +
                 ", averageTimeToProcessNanos=" + averageTimeToProcessNanos +
                 ", maximumBacklog=" + maximumBacklog +
                 ", random=" + random +
                 '}';
      }
   }
}
