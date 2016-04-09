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
package org.cacheonix;

import org.cacheonix.impl.util.logging.Logger;

/**
 * A stand-alone Cacheonix server.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public final class Server {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Server.class); // NOPMD

   /**
    * Path to the built-in configuration for a standalone server.
    */
   private static final String META_INF_CACHEONIX_SERVER_CONFIG_XML = "/META-INF/cacheonix-server-config.xml";

   /**
    * Lock object.
    */
   private final Object lock = new Object();


   /**
    * Server constructor.
    */
   private Server() {

   }


   /**
    * Executes the server and waits until the process is killed.
    *
    * @param args arguments
    * @throws InterruptedException if the server was interrupted.
    */
   public static void main(final String[] args) throws InterruptedException {

      final Server server = new Server();
      server.run();
   }


   /**
    * Starts up the node and enters an infinite loop waiting for external termination.
    *
    * @throws InterruptedException if the server was interrupted.
    */
   @SuppressWarnings({"InfiniteLoopStatement", "WaitWithoutCorrespondingNotify"})
   private void run() throws InterruptedException {

      // This call to default getInstance() will startup the cluster node
      Cacheonix.getInstance(META_INF_CACHEONIX_SERVER_CONFIG_XML);

      // Wait infinitely server is expected to terminate by a kill signal
      synchronized (lock) {

         while (true) {

            lock.wait(1000L);
         }
      }
   }
}
