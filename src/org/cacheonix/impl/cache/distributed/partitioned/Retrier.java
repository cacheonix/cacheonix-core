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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.RuntimeTimeoutException;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.ThreadUtils;
import org.cacheonix.impl.util.time.Timeout;

/**
 * Retries until done.
 */
public final class Retrier {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Retrier.class); // NOPMD

   private static final long INITIAL_RETRY_DELAY = 10L;

   private static final long MAX_RETRY_DELAY = 1000L;

   private static final long RETRY_MULTIPLY_FACTOR = 2;


   /**
    * Retries a Retryable until done.
    *
    * @param retryable the Retryable.
    * @return a result.
    */
   @SuppressWarnings("MethodMayBeStatic")
   public Object retryUntilDone(final Retryable retryable) {

      final long clientRequestTimeoutMillis = SystemProperty.getClientRequestTimeoutMillis();
      final Timeout timeout = new Timeout(clientRequestTimeoutMillis).reset();
      RetryException lastRetryException = null;
      long repeatDelay = INITIAL_RETRY_DELAY;
      int retryCount = 0;

      while (!timeout.isExpired()) {

         try {

            return retryable.execute();
         } catch (final RetryException e) {

            if (LOG.isDebugEnabled() && retryCount % 100 == 0) {
               LOG.debug("Retrying " + (retryable.hasDescription() ? retryable.description() : "") + ", retry count: " + retryCount);
            }
            retryCount++;
            lastRetryException = e;
            ThreadUtils.sleep(repeatDelay);
            repeatDelay *= RETRY_MULTIPLY_FACTOR;
            if (repeatDelay >= MAX_RETRY_DELAY) {

               repeatDelay = INITIAL_RETRY_DELAY;
            }
         }
      }

      // Could not finish operation
      final StringBuilder message = createTimeoutMessage(retryable, timeout, lastRetryException, retryCount);
      throw new RuntimeTimeoutException(message);
   }


   private static StringBuilder createTimeoutMessage(final Retryable retryable, final Timeout timeout,
                                                     final RetryException lastRetryException, final int retryCount) {

      final StringBuilder message = new StringBuilder(100).append("Could not complete operation '")
              .append(retryable.description()).append("' after ").append(timeout.getDuration())
              .append(" ms and ").append(retryCount).append(" retries");

      if (lastRetryException != null && !lastRetryException.isMessageBlank()) {

         message.append(". ").append("Additional information: ").append(lastRetryException.getMessage());
      }
      return message;
   }
}
