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
package org.cacheonix.impl.cluster.node;

import org.cacheonix.ShutdownException;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.net.tcp.server.TCPRequestDispatcher;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Temporarily request dispatcher to handle TCP requests until we added channels.
 *
 * @noinspection NonStaticInnerClassInSecureContext
 */
final class DistributedCacheonixTCPRequestDispatcher implements TCPRequestDispatcher {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DistributedCacheonixTCPRequestDispatcher.class); // NOPMD

   private final Router router;


   public DistributedCacheonixTCPRequestDispatcher(final Router router) {

      this.router = router;
   }


   public void dispatch(final Message message) {

      try {

         router.route(message);
      } catch (final ShutdownException ignored) {

         // Cluster processor has already been shutdown. Ignore the request
         // to dispatch the message because our shutdown will follow shortly.
         // The message is usually a response to our last marker.
         ExceptionUtils.ignoreException(ignored, "Shutdown in progress");
      }
   }


   public String toString() {

      return "DistributedCacheonixTCPRequestDispatcher{" +
              '}';
   }
}

