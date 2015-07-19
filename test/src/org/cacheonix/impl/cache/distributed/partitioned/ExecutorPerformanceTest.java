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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Test performance of the Executor framework
 */
public final class ExecutorPerformanceTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PartitionedCacheTestDriver.class); // NOPMD

   private static final int singleKeyPerformanceCount = 1000000;


   public void testExecutor() throws Exception {


      // Prepare
      final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

      final Callable<Object> callable = new Callable<Object>() {

         public Object call() throws Exception {

            return null;
         }
      };

      final AtomicReference<Exception> error = new AtomicReference<Exception>();
      final int halfAvailableProcessors = Runtime.getRuntime().availableProcessors() / 2;
      final int threadCount = halfAvailableProcessors == 0 ? 1 : halfAvailableProcessors;
      final List<Thread> readerTreads = new ArrayList<Thread>(threadCount);

      for (int i = 0; i < threadCount; i++) {

         readerTreads.add(new Thread(new Runnable() {

            public void run() {

               for (int j = 0; j < singleKeyPerformanceCount; j++) {

                  try {

//                     executorService.submit(callable);
                     executorService.submit(callable).get();
                  } catch (final Exception e) {

                     //noinspection ControlFlowStatementWithoutBraces
                     if (LOG.isDebugEnabled()) LOG.debug("Error while running the test" + e, e); // NOPMD

                     error.set(e);
                  }
               }
            }
         }));
      }

      // Mark the beginning of the test
      final long start = System.currentTimeMillis();

      // Start
      for (final Thread readerTread : readerTreads) {

         readerTread.start();
      }

      // Finish
      for (final Thread readerTread : readerTreads) {

         readerTread.join();
      }

      // Let the tasks finish (if any)
      executorService.shutdown();

      // Mark the end of the test
      final long end = System.currentTimeMillis();

      //noinspection ThrowableResultOfMethodCallIgnored
      if (error.get() != null) {

         throw error.get();
      }


      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("end - start: " + (end - start)); // NOPMD
   }
}
