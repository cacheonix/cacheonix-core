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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.cacheonix.CacheonixTestCase;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tester for PrefetchStageThreadPoolAdapter.
 */
public final class PrefetchStageThreadPoolAdapterTest extends CacheonixTestCase {


   private PrefetchStageThreadPoolAdapter prefetchScheduler;

   private ThreadPoolExecutor threadPool;


   public void testSchedule() {

      // Mock prefetch command
      final PrefetchCommand prefetchCommand = mock(PrefetchCommand.class);
      when(prefetchCommand.getPrefetchTime()).thenReturn(getClock().currentTime().add(1000L));

      // Schedule
      prefetchScheduler.schedule(prefetchCommand);

      // Check the command was scheduled
      verify(prefetchCommand).setCurrentStage(any(PrefetchStage.class));
      verify(prefetchCommand).setStageContext(any());
   }


   public void testNextStage() {

      assertNull(prefetchScheduler.nextStage());
   }


   public void testCancel() {

      final PrefetchCommandImpl prefetchCommand = new PrefetchCommandImpl(mock(PrefetchElementUpdater.class), mock(BinaryStoreDataSource.class), toBinary("test.key"), getClock().currentTime().add(1000L), 1);
      prefetchScheduler.schedule(prefetchCommand);
      prefetchScheduler.cancel(prefetchCommand);
      assertEquals(1, prefetchScheduler.getCancelCounter());
   }


   public void setUp() throws Exception {

      super.setUp();

      threadPool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1));
      prefetchScheduler = new PrefetchStageThreadPoolAdapter(threadPool);
   }


   public void tearDown() throws Exception {


      threadPool.shutdownNow();
      threadPool = null;
      prefetchScheduler = null;

      super.tearDown();
   }
}
