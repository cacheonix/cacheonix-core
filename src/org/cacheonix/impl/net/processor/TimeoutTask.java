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

import java.util.TimerTask;

import org.cacheonix.impl.util.exception.ExceptionUtils;

/**
 * A task submitted to a timer. It just enqueues a timeout command.
 */
final class TimeoutTask extends TimerTask {

   private final RequestProcessor processor;

   private final UUID requestUUID;


   /**
    * Creates a TimeoutTask.
    *
    * @param processor   the processor to enqueue a timeout command to.
    * @param requestUUID the UUID of a request that timed out.
    */
   public TimeoutTask(final RequestProcessor processor, final UUID requestUUID) {

      this.processor = processor;
      this.requestUUID = requestUUID;
   }


   /**
    * Enqueues a timeout command to the processor.
    */
   public void run() {

      try {
         if (!processor.isShutdown()) {
            processor.enqueue(new TimeoutCommand(processor, requestUUID));
         }
      } catch (final InterruptedException e) {
         ExceptionUtils.ignoreException(e, "Nothing we can do");
      }
   }


   public String toString() {

      return "TimeoutTask{" +
              "processor=" + processor.getAddress() +
              ", requestUUID=" + requestUUID +
              "} " + super.toString();
   }
}
