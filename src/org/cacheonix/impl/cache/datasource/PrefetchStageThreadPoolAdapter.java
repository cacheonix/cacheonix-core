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
package org.cacheonix.impl.cache.datasource;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.cacheonix.impl.config.ConfigurationConstants;

/**
 * An adapter from a ThreadPoolExecutor to a prefetch stage.
 */
public final class PrefetchStageThreadPoolAdapter implements PrefetchStage {

   public final AtomicInteger cancelCounter = new AtomicInteger(0);

   /**
    * A delegate thread pool
    */
   private final ThreadPoolExecutor threadPool;


   /**
    * Creates a new instance of PrefetchStageThreadPoolAdapter.
    *
    * @param threadPool the tread pool to wrap.
    */
   public PrefetchStageThreadPoolAdapter(final ThreadPoolExecutor threadPool) {

      this.threadPool = threadPool;
   }


   /**
    * {@inheritDoc}
    */
   public void schedule(final PrefetchCommand prefetchCommand) {

      // Set stage
      prefetchCommand.setCurrentStage(this);

      // Schedule
      final Future<?> future = threadPool.submit(prefetchCommand);

      // Remember future to use for cancellation
      prefetchCommand.setStageContext(future);
   }


   /**
    * {@inheritDoc}
    */
   public void cancel(final PrefetchCommand prefetchCommand) {

      // Cancel command
      final Future<?> future = (Future<?>) prefetchCommand.getStageContext();
      future.cancel(true);

      // Purge
      if (cancelCounter.incrementAndGet() >= ConfigurationConstants.MAX_PREFETCH_CANCELS_BEFORE_PURGE) {

         threadPool.purge();
         cancelCounter.set(0);
      }
   }


   /**
    * Returns current cancel counter.
    *
    * @return current cancel counter.
    */
   int getCancelCounter() {

      return cancelCounter.get();
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation returns null becuase it is a last stage.
    */
   public PrefetchStage nextStage() {

      return null;
   }
}
