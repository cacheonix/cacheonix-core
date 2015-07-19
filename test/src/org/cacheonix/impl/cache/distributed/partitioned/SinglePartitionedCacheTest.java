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

import java.util.concurrent.ExecutionException;

import org.cacheonix.impl.util.logging.Logger;

/**
 * A tester for a partitioned cache with a single-node configuration.
 */
public class SinglePartitionedCacheTest extends SinglePartitionedCacheTestDriver {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SinglePartitionedCacheTest.class); // NOPMD

   private static final String CONFIGURATION = "cacheonix-config-cluster-member-alone.xml";

   private static final int GET_SINGLE_KEY_PERFORMANCE_COUNT = 1000;


   public SinglePartitionedCacheTest() {

      super(CONFIGURATION);
   }


   public void testIsEmpty() { // NOPMD

      super.testIsEmpty();
   }


   public void testGetSingleKeyPerformance() throws ExecutionException, InterruptedException { // NOPMD

      super.testGetSingleKeyPerformance();
   }


   public void testGetSingleKeyParallelPerformance() throws ExecutionException, InterruptedException { // NOPMD

      super.testGetSingleKeyParallelPerformance();
   }


   public void setUp() throws Exception {

      super.setUp();

      // Overwrite the performance count.
      super.setSingleKeyPerformanceCount(GET_SINGLE_KEY_PERFORMANCE_COUNT);
   }
}
