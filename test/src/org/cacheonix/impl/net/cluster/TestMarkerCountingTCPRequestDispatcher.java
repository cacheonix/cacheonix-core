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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.ShutdownException;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.tcp.io.TCPRequestDispatcher;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * This message handler just delegates processing to the actual handler. It also counts recevied tokens.
 */
final class TestMarkerCountingTCPRequestDispatcher implements TCPRequestDispatcher {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TestMarkerCountingTCPRequestDispatcher.class); // NOPMD

   private final ClusterProcessor delegate;

   private final int connectionIndex;

   private int tokenCount = 0;


   public TestMarkerCountingTCPRequestDispatcher(final int connectionIndex, final ClusterProcessor processor) {

      this.connectionIndex = connectionIndex;
      this.delegate = processor;
   }


   public void dispatch(final Message message) throws InterruptedException {

      if (message.getWireableType() == Wireable.TYPE_CLUSTER_MULTICAST_MARKER) {
         tokenCount++;
      }

      try {
         delegate.enqueue(message);
      } catch (final ShutdownException ignored) {
         ExceptionUtils.ignoreException(ignored, "Shutting down");
      }
   }


   public int getMarkerCount() {

      return tokenCount;
   }


   public int getConnectionIndex() {

      return connectionIndex;
   }


   public String toString() {

      return "TestMarkerCountingTCPRequestDispatcher{" +
              "connectionIndex=" + connectionIndex +
              ", delegate=" + delegate +
              ", tokenCount=" + tokenCount +
              '}';
   }
}

