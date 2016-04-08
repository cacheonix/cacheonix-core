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

import org.cacheonix.impl.net.ClusterNodeAddress;

/**
 * @noinspection RedundantIfStatement
 */
final class PrimaryBucketOwner {

   PrimaryBucketOwner(final ClusterNodeAddress primaryOwner, final Integer bucketNumber) {

      this.primaryOwner = primaryOwner;
      this.bucketNumber = bucketNumber;
   }


   private final ClusterNodeAddress primaryOwner;

   private final Integer bucketNumber;


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (!(obj instanceof PrimaryBucketOwner)) {
         return false;
      }

      final PrimaryBucketOwner that = (PrimaryBucketOwner) obj;

      if (bucketNumber != null ? !bucketNumber.equals(that.bucketNumber) : that.bucketNumber != null) {
         return false;
      }
      if (primaryOwner != null ? !primaryOwner.equals(that.primaryOwner) : that.primaryOwner != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = primaryOwner != null ? primaryOwner.hashCode() : 0;
      result = 31 * result + (bucketNumber != null ? bucketNumber.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "PrimaryBucketOwner{" +
              "bucketNumber=" + bucketNumber +
              ", primaryOwner=" + primaryOwner +
              '}';
   }
}
