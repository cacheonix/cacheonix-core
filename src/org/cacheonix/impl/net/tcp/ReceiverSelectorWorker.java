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

import java.nio.channels.Selector;

/**
 * A selector worker used by TCPServer.
 */
class ReceiverSelectorWorker extends SelectorWorker {

   /**
    * Creates SelectorWorker.
    *
    * @param selector              a selector to run select cycle for.
    * @param socketTimeoutMillis   a network timeout, milliseconds.
    * @param selectorTimeoutMillis a time the selector should block for while waiting for a channel to become ready,
    *                              must be greater than zero.
    */
   ReceiverSelectorWorker(final Selector selector, final long socketTimeoutMillis,
           final long selectorTimeoutMillis) {

      super(selector, socketTimeoutMillis, selectorTimeoutMillis);
   }
}
