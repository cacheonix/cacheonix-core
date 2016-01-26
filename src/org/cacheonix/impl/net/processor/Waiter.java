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

import org.cacheonix.ReconfigurationException;
import org.cacheonix.ShutdownException;
import org.cacheonix.impl.RuntimeInterruptedException;
import org.cacheonix.impl.RuntimeTimeoutException;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Waiter
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection ALL @since Jul 8, 2009 7:58:06 PM
 */
public class Waiter implements ResponseWaiter {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Waiter.class); // NOPMD

   /**
    * Default time after that a wait in <code>waitForResult()<code> should re-accuire monitor and check for the
    * condition.
    *
    * @see #waitForResult(long)
    */
   private static final long RETRY_WAIT_TIME_MILLIS = 1000L;

   private volatile Object result = null;

   private volatile boolean finished = false;

   /**
    * Originator.
    *
    * @noinspection FieldCanBeLocal
    */
   private Request request = null;

   /**
    * True if reponse was received. The waiter may finish with reponseReceived set to false when the receiver was
    * reported as failed.
    */
   private boolean responseReceived = false;


   /**
    * Creates waiter.
    *
    * @param request request UUID
    */
   public Waiter(final Request request) {

      this.request = request;
   }


   /**
    * Returns UUID of the request for that this waiter was created.
    *
    * @return UUID of the request for that this waiter was created.
    */
   public final UUID getRequestUUID() {

      return request.getUuid();
   }


   /**
    * Returns request for that this waiter was created.
    *
    * @return request for that this waiter was created.
    */
   public final Request getRequest() {

      return request;
   }


   /**
    * Sets result.
    *
    * @param result to set.
    */
   public final void setResult(final Object result) {

      this.result = result;
   }


   /**
    * Returns result object.
    *
    * @return result object.
    */
   protected final Object getResult() {

      return result;
   }


   /**
    * Returns result when waiting is finished.
    *
    * @return result when waiting is finished.
    */
   public final Object waitForResult() throws RetryException, RuntimeTimeoutException {

      return waitForResult(SystemProperty.getClientRequestTimeoutMillis());
   }


   /**
    * Returns result when waiting is finished.
    *
    * @return result when waiting is finished.
    * @throws RuntimeTimeoutException if request timed out while waiting for a result
    */
   public final Object waitForResult(final long timeoutMillis) throws RetryException, RuntimeTimeoutException {

      final long timeoutExpirationTime = calculateTimeoutExpirationTime(timeoutMillis);
      final long wakeupTimeoutMillis = calculateWakeupTimeoutMillis(timeoutMillis);

      synchronized (this) {

         while (!finished) {

            try {
               wait(wakeupTimeoutMillis);
            } catch (InterruptedException e) {
               throw new RuntimeInterruptedException(e);
            }

            // Check for timeout. We first compare with the max value so that we don't have to go to System.
            if (timeoutExpirationTime != Long.MAX_VALUE && System.currentTimeMillis() >= timeoutExpirationTime) {
               if (!finished) {

                  finished = true;

                  final RequestProcessor processor = getRequest().getProcessor();

                  // NOTE: simeshev@cacheonix.org - 2011-06-20 - We need this check because, per CACHEONIX-372,
                  // it is possible to timeout without ever be assigned a processor.
                  if (processor != null) {

                     processor.getWaiterList().unregister(this);
                  }
               }
               result = new RuntimeTimeoutException(createTimeoutMessage(timeoutMillis));
            }
         }

         if (result instanceof RetryException) {
            throw (RetryException) result;
         }
         if (result instanceof RuntimeException) {
            throw (RuntimeException) result;
         }
         if (result instanceof Error) {
            throw (Error) result;
         }
         if (result instanceof Throwable) {
            throw new RuntimeException((Throwable) result);
         }
         return result;
      }
   }


   /**
    * Stops waiting.
    */
   public final synchronized void finish() {

      if (!finished) {

         // Set finished flag
         finished = true;

         notifyFinished();

         notifyAll();
      }
   }


   /**
    * This method is called by {@link WaiterList} when a response corresponding this waiter is received. It is
    * guaranteed that Message.responseToUUID is equal this waiter's requestUUID. Inheritors can overwrite this method.
    *
    * @param response
    */
   public void notifyResponseReceived(final Response response) throws InterruptedException {

      // Guard
      if (finished) {
         return;
      }

      // Set response received flag
      responseReceived = true;

      // Finish
      finish();
   }


   /**
    * Notifies those waiting that the wait is over. The default behaviour is to call </code>notifyAll()</code>.
    * <p/>
    * Inheritors can overwrite this method to implement actions to be performed on wait finish. This method should be be
    * called in the end of the overwriting method because it will wakeup a thread waiting for the result.
    */
   protected void notifyFinished() {

   }


   /**
    * Processes the norification that a given node left.
    * <p/>
    * The default implementation calls finish() if the node left is the node waiter for.
    *
    * @param address the addresss of the node that left.
    */
   public void notifyNodeLeft(final ClusterNodeAddress address) {

      // Guard
      if (finished) {

         return;
      }


      // Check if node gone is the node we are waiting for
      if (request.isReceiverSet() && request.getReceiver().isAddressOf(address)) {

         // Finish
         finish();
      }
   }


   /**
    * Returns <code>true</code> if this waiter has finished waiting.
    *
    * @return <code>true</code> if this waiter has finished waiting.
    */
   public final boolean isFinished() {

      return finished;
   }


   /**
    * Returns <code>true</code> if request receiver is set.
    * <p/>
    * It can be empty if this is a reliable multicast message.
    *
    * @return
    */
   public final boolean isReceiverSet() {

      return request.isReceiverSet();
   }


   /**
    * Returns true if response was received.
    *
    * @return
    */
   public final boolean isResponseReceived() {

      return responseReceived;
   }


   /**
    * Calculates timeout expiration time in milliseconds.
    *
    * @param timeoutMillis timeout
    * @return timeout expiration time in milliseconds
    * @see #waitForResult()
    */
   private static long calculateTimeoutExpirationTime(final long timeoutMillis) {

      return timeoutMillis == Long.MAX_VALUE || timeoutMillis == 0L
              ? Long.MAX_VALUE : (System.currentTimeMillis() + timeoutMillis);
   }


   /**
    * Calculates time in milliseconds afer that <code>wait()</code> should retry.
    *
    * @param timeoutMillis
    * @return time in milliseconds afer that <code>wait()</code> should retry.
    * @see #waitForResult()
    */
   private long calculateWakeupTimeoutMillis(final long timeoutMillis) {

      return timeoutMillis < RETRY_WAIT_TIME_MILLIS ? timeoutMillis : RETRY_WAIT_TIME_MILLIS;
   }


   /**
    * Creates a timeout message.
    *
    * @param timeoutMillis
    * @return timeout message
    */
   private String createTimeoutMessage(final long timeoutMillis) {
      // REVIEWME: slava@cacheonix.org - 2010-01-02:
      //           a) Need to replace with .append();
      //           b) Need to impement short versions of the setToSring() below
      final StringBuffer sb = new StringBuffer(300);
      sb.append("Request timed out for sender " + request.getSender() + " after " + timeoutMillis + " ms, waiting for address: " + request.getReceiver());
      return sb.toString();
   }


   /**
    * Default implementation.
    */
   public synchronized void notifyTimeout() {

      setResult(new TimeoutException());

      finished = true;
   }


   /**
    * Notifies this waiter that the processor has been shutdown. As there is no point in waiting, sets the result to
    * ShutdownException and calls notifyAll to wakeup a client thread.
    */
   public synchronized void notifyShutdown() {

      setResult(new ShutdownException("System was shutdown while waiting for a response"));

      finished = true;

      notifyAll();
   }


   /**
    * Notifies this waiter that system configuration has changed in way that is not compatible with continuing waiting
    * for a response from the request. As there is no point in waiting, sets the result to ReconfigurationException and
    * calls notifyAll to wakeup a client thread.
    */
   public synchronized void notifyReset() {

      setResult(new ReconfigurationException("Cluster node left the previous cluster and joined a new cluster while waiting for a response to '" + getRequest().getClass().getSimpleName() + "'"));

      finished = true;

      notifyAll();
   }


   /**
    * @noinspection ArithmeticOnVolatileField
    */
   public String toString() {

      return "Waiter{" +
              "requestUUID=" + getRequestUUID() +
              ", finished=" + finished +
              ", request.class=" + (request == null ? "null" : request.getClass().toString()) +
              ", result=" + result +
              '}';
   }
}
