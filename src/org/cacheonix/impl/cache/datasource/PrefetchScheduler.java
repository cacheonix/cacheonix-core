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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.configuration.ConfigurationConstants;

/**
 * A prefetch scheduler.
 */
public final class PrefetchScheduler implements PrefetchStage {

   private final AtomicInteger cancelCounter = new AtomicInteger(0);

   private final Timer timer = new Timer("Skippy");

   private final PrefetchStage nextStage;


   public PrefetchScheduler(final PrefetchStage nextStage) {

      this.nextStage = nextStage;
   }


   public void schedule(final PrefetchCommand prefetchCommand) {

      // Create command adapter
      final TimerTask timerTask = new PrefetchCommandTimerTaskAdapter(prefetchCommand);

      // Remember it in the command
      prefetchCommand.setStageContext(timerTask);

      // Set self as context
      prefetchCommand.setCurrentStage(this);

      // Schedule
      final Time prefetchTime = prefetchCommand.getPrefetchTime();
      timer.schedule(timerTask, new Date(prefetchTime.getMillis()));
   }


   public PrefetchStage nextStage() {

      return nextStage;
   }


   public void cancel(final PrefetchCommand prefetchCommand) {

      // Mark timer task as cancelled
      final TimerTask timerTask = (TimerTask) prefetchCommand.getStageContext();
      timerTask.cancel();

      // Run purge
      if (cancelCounter.incrementAndGet() >= ConfigurationConstants.MAX_PREFETCH_CANCELS_BEFORE_PURGE) {

         cancelCounter.set(0);
         timer.purge();
      }
   }


   /**
    * Shutdowns the internal timer.
    */
   public void shutdown() {

      timer.cancel();
   }


   /**
    * Returns the value of the current cancel counter.
    *
    * @return the value of the current cancel counter.
    */
   int getCancelCounter() {

      return cancelCounter.get();
   }


   public String toString() {

      return "PrefetchScheduler{" +
              "cancelCounter=" + cancelCounter +
              '}';
   }
}
