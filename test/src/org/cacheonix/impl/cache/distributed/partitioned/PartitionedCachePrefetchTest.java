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
package org.cacheonix.impl.cache.distributed.partitioned;

/**
 * A test for prefetch functionality for local cache.
 */
public final class PartitionedCachePrefetchTest extends PartitionedCachePrefetchTestDriver {

   /**
    * Cacheonix configurations, one per cluster.
    */
   private static final String[] CONFIGURATIONS = {
           "cacheonix-config-cluster-member-with-prefetch-1.xml",
           "cacheonix-config-cluster-member-with-prefetch-2.xml",
           "cacheonix-config-cluster-member-with-prefetch-3.xml"
   };


   public PartitionedCachePrefetchTest() {

      super(CONFIGURATIONS);
   }


   public PartitionedCachePrefetchTest(final String testName) {

      super(testName, CONFIGURATIONS);
   }
}
