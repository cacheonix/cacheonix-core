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

import org.cacheonix.CacheonixTestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tester for PrefetchCommandTimerTaskAdapter.
 */
public final class PrefetchCommandTimerTaskAdapterTest extends CacheonixTestCase {


   private PrefetchCommandTimerTaskAdapter prefetchCommandTimerTaskAdapter;

   private PrefetchCommand prefetchCommand;


   public void testRun() throws Exception {

      prefetchCommandTimerTaskAdapter.run();
      verify(prefetchCommand).run();
   }


   public void setUp() throws Exception {

      super.setUp();

      prefetchCommand = mock(PrefetchCommand.class);
      prefetchCommandTimerTaskAdapter = new PrefetchCommandTimerTaskAdapter(prefetchCommand);
   }


   public void tearDown() throws Exception {

      prefetchCommandTimerTaskAdapter = null;

      super.tearDown();
   }
}
