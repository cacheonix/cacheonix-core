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

import java.util.Collections;

import org.cacheonix.CacheonixException;
import org.cacheonix.impl.net.processor.Command;

/**
 * Command to shutdown the cluster node.
 */
public final class ShutdownClusterProcessorCommand extends Command {

   private final ClusterProcessor processor;

   /**
    * An optional shutdown reason. If set, attempts to enqueue messages to the shutdown cluster processor will throw an
    * exception.
    */
   private CacheonixException shutdownCause = null;


   public ShutdownClusterProcessorCommand(final ClusterProcessor processor) {

      this.processor = processor;
   }


   /**
    * Calls <code>ClusterService.shutdownHard()</code>
    *
    * @see ClusterProcessor#forceShutdown(CacheonixException)
    */
   public void execute() {

      // REVIEWME: simeshev@cacheonix.org - 2011-02-25 - Not sure if
      // we need it. Maybe we should have a shutdown notification?
      processor.getMulticastMessageListeners().notifyNodesLeft(Collections.singletonList(processor.getAddress()));

      // Shutdown
      processor.forceShutdown(shutdownCause);
   }


   /**
    * Sets the shutdown reason. If set, attempts to enqueue messages to the shutdown cluster processor will throw an
    * exception.
    *
    * @param shutdownReason a shutdown reason.
    */
   public void setShutdownCause(final CacheonixException shutdownReason) {

      this.shutdownCause = shutdownReason;
   }
}
