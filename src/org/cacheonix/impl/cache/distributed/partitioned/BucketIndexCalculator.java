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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.util.HashCode;
import org.cacheonix.util.HashCodeType;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A calculator of a bucket number for an object.
 * <p/>
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 22, 2009 4:14:20 PM
 */
public final class BucketIndexCalculator {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketIndexCalculator.class); // NOPMD

   /**
    * Number of buckets, should be the same for all nodes in the cluster.
    */
   private final int bucketCount;


   /**
    * Creates BucketIndexCalculator.
    *
    * @param bucketCount number of buckets, should be the same for all nodes in the cluster.
    */
   @SuppressWarnings("SameParameterValue")
   public BucketIndexCalculator(final int bucketCount) {

      this.bucketCount = bucketCount;
   }


   /**
    * Calculates bucket index based on
    *
    * @param object object for that to calculate bucket index.
    * @return bucket index for this object, from 0 to numberOfBuckets - 1
    */
   public int calculateBucketIndex(final Object object) {

      final HashCode hashCode = new HashCode(HashCodeType.NORMAL);
      hashCode.add(object.hashCode());
      return (hashCode.getValue() & 0x7FFFFFFF) % bucketCount;
   }


   public String toString() {

      return "BucketIndexCalculator{" +
              "bucketCount=" + bucketCount +
              '}';
   }
}
