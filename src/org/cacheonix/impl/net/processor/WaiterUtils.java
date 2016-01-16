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

import java.net.InetAddress;
import java.util.Arrays;

import org.cacheonix.CacheonixException;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Contains a set of helper methods used by various implementations of the <code>Waiter</code>.
 */
public final class WaiterUtils {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(WaiterUtils.class); // NOPMD


   /**
    * Utility class constructor.
    */
   private WaiterUtils() {

   }


   public final static String resultToString(final Object result, final int maxLength) {

      if (result == null) {
         return "null";
      } else {
         final String resultString = result.toString();
         return resultString.substring(0, Math.min(maxLength, resultString.length() - 1));
      }
   }


   /**
    * Converts a result object to a an exception. Leaves the result untouched if it is already a
    * <code>Throwable</code>.
    *
    * @param result to convert to <code>Throwable</code>
    * @return a Throwable
    */
   public static Throwable resultToThrowable(final Object result) {

      return (result instanceof Throwable) ? (Throwable) result : new CacheonixException(String.valueOf(result));
   }


   /**
    * Converts a result object to a an exception. Leaves the result untouched if it is already a
    * <code>Throwable</code>.
    *
    * @param result to convert to <code>Throwable</code>
    * @return a Throwable
    */
   public static Throwable resultToThrowable(final Message response, final Object result) {

      if (result instanceof Throwable) {
         return (Throwable) result;
      }
      final ClusterNodeAddress senderAddress = response.getSender();
      final InetAddress[] addresses = senderAddress.getAddresses();
      final String addressesAsString = addresses == null? "null" : Arrays.toString(addresses);
      return new CacheonixException("Node " + addressesAsString + ':' + senderAddress.getTcpPort()
              + " returned error: " + (result == null ? "null" : result.toString()));
   }


   /**
    * Helper method.
    *
    * @param resultCode result code
    * @param result     result object
    * @return CacheonixException
    */
   public static CacheonixException unknownResultToThrowable(final int resultCode, final Object result) {

      return new CacheonixException(
              "Received unknown result: " + resultToString(result, 100) + ", code: " + resultCode);
   }
}
