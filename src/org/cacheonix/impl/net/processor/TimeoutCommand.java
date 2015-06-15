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
package org.cacheonix.impl.net.processor;

/**
 *
 */
final class TimeoutCommand extends Command {

   private final RequestProcessor processor;

   private final UUID requestUUID;


   public TimeoutCommand(final RequestProcessor processor, final UUID requestUUID) {

      this.processor = processor;
      this.requestUUID = requestUUID;
   }


   public void execute() {

      processor.getWaiterList().notifyTimeout(requestUUID);
   }


   public String toString() {

      return "TimeoutCommand{" +
              "processor=" + processor.getAddress() +
              ", requestUUID=" + requestUUID +
              "} " + super.toString();
   }
}
