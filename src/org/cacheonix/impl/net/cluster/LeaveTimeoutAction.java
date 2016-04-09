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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.TimeoutAction;

/**
 * This action requests the node to shutdown.
 */
final class LeaveTimeoutAction extends TimeoutAction {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LeaveTimeoutAction.class); // NOPMD

   private ClusterProcessor processor;


   LeaveTimeoutAction(final ClusterProcessor processor) {

      super(processor.getProcessorState().calculateLeaveTimeout());

      this.processor = processor;
   }


   public final void run() {

      // Request context to begin forced shutdown
      processor.beginForcedShutdown();

      // Clear context
      processor = null;
   }


   public final String toString() {

      return "LeaveTimeoutAction{" +
              "context=" + (processor == null ? "null" : processor.getAddress().toString()) +
              '}';
   }
}
