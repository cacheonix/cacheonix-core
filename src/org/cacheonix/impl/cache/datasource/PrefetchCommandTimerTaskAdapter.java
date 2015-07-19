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
package org.cacheonix.impl.cache.datasource;

import java.util.TimerTask;

/**
 * A adapter from a PrefetchCommand to a java.util.TimerTask.
 */
public final class PrefetchCommandTimerTaskAdapter extends TimerTask {

   /**
    * A delegate prefetch command.
    */
   private final PrefetchCommand prefetchCommand;


   /**
    * Creates a new instance of PrefetchCommandTimerTaskAdapter.
    *
    * @param prefetchCommand a PrefetchCommand to adapt to java.util.TimerTask.
    */
   public PrefetchCommandTimerTaskAdapter(final PrefetchCommand prefetchCommand) {

      this.prefetchCommand = prefetchCommand;
   }


   /**
    * The action to be performed by this timer task.
    */
   public void run() {

      prefetchCommand.run();
   }
}
