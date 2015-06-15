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
package org.cacheonix.impl.cache.datasource;

import org.cacheonix.CacheonixTestCase;

import static org.mockito.Mockito.*;

/**
 * Tester for PrefetchScheduler.
 */
public final class PrefetchSchedulerTest extends CacheonixTestCase {


   private PrefetchStage nextStage;

   private PrefetchScheduler prefetchScheduler;


   public void testSchedule() throws Exception {

      // Mock prefetch command
      final PrefetchCommand prefetchCommand = mock(PrefetchCommand.class);
      when(prefetchCommand.getPrefetchTime()).thenReturn(getClock().currentTime().add(1000L));

      // Schedule
      prefetchScheduler.schedule(prefetchCommand);

      // Check the command was scheduled
      verify(prefetchCommand).setCurrentStage(any(PrefetchStage.class));
      verify(prefetchCommand).setStageContext(any());
   }


   public void testNextStage() throws Exception {

      assertEquals(nextStage, prefetchScheduler.nextStage());
   }


   public void testCancel() throws Exception {

      final PrefetchCommandImpl prefetchCommand = new PrefetchCommandImpl(mock(PrefetchElementUpdater.class), mock(BinaryStoreDataSource.class), toBinary("test.key"), getClock().currentTime().add(1000L), 1);
      prefetchScheduler.schedule(prefetchCommand);
      prefetchScheduler.cancel(prefetchCommand);
      assertEquals(1, prefetchScheduler.getCancelCounter());
   }


   public void setUp() throws Exception {

      super.setUp();

      nextStage = mock(PrefetchStage.class);
      prefetchScheduler = new PrefetchScheduler(nextStage);
   }


   public void tearDown() throws Exception {


      prefetchScheduler.shutdown();
      prefetchScheduler = null;
      nextStage = null;

      super.tearDown();
   }
}
