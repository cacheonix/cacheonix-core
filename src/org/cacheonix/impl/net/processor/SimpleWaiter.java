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


import org.cacheonix.impl.util.logging.Logger;

/**
 * Waits for a response and converts a response to a result. Response.RESULT_INACCESSIBLE and Response.RESULT_RETRY
 * produce a RetryException result. Response.RESULT_ERROR produces a RuntimeException result.
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
public class SimpleWaiter extends Waiter {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SimpleWaiter.class); // NOPMD


   /**
    * Creates waiter.
    *
    * @param request request UUID
    */
   public SimpleWaiter(final Request request) {

      super(request);
   }


   /**
    * {@inheritDoc}
    */
   public void notifyResponseReceived(final Response response) throws InterruptedException {

      final int resultCode = response.getResultCode();
      final Object result = response.getResult();
      switch (resultCode) {

         case Response.RESULT_SUCCESS:

            setResult(result);
            break;
         case Response.RESULT_INACCESSIBLE:
         case Response.RESULT_RETRY:

            if (result instanceof String) {

               // Set the reason to the exception message to make debugging easier
               setResult(new RetryException(result.toString()));
            } else {

               setResult(new RetryException());
            }
            break;
         case Response.RESULT_ERROR:

            setResult(WaiterUtils.resultToThrowable(result));
            break;
         default:

            setResult(WaiterUtils.unknownResultToThrowable(resultCode, result));
            break;
      }

      super.notifyResponseReceived(response);
   }
}
