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
package org.cacheonix.impl.net.cluster;


import org.cacheonix.impl.util.thread.TimeoutAction;

/**
 * This action begins executing recovery from current process failure.
 */
final class MarkerTimeoutAction extends TimeoutAction {

   private ClusterProcessor processor;

   /**
    * Holds the number of markers received by the cluster node at the time the timeout was created.
    */
   private final long markerCounterAtCreate;


   MarkerTimeoutAction(final ClusterProcessor processor) {

      super(processor.getProcessorState().calculateMarkerTimeout());

      this.markerCounterAtCreate = processor.getProcessorState().getMarkerCounter();
      this.processor = processor;
   }


   public final void run() {

      final MarkerTimeoutMessage markerTimeoutMessage = new MarkerTimeoutMessage();
      markerTimeoutMessage.setMarkerCounterAtTimeoutCreate(markerCounterAtCreate);
      markerTimeoutMessage.setTimeoutMillis(getTimeoutMillis());
      markerTimeoutMessage.setReceiver(processor.getAddress());
      processor.post(markerTimeoutMessage);
      processor = null;
   }


   public String toString() {

      return "MarkerTimeoutAction{" +
              "markerCounterAtCreate=" + markerCounterAtCreate +
              ", context=" + ((processor == null) ? "null" : processor.getAddress().toString()) +
              '}';
   }
}