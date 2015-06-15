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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.tcp.server.TCPRequestDispatcher;

/**
 * A singleton unknown request dispatcher.
 */
public final class UnknownTypeTCPRequestDispatcher implements TCPRequestDispatcher {

   private static final UnknownTypeTCPRequestDispatcher instance = new UnknownTypeTCPRequestDispatcher();


   private UnknownTypeTCPRequestDispatcher() {

   }


   public void dispatch(final Message message) {

      throw new IllegalArgumentException("Unknown type: " + message.getWireableType());
   }


   public static UnknownTypeTCPRequestDispatcher getInstance() {

      return instance;
   }
}
