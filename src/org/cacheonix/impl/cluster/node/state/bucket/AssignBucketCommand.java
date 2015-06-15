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
package org.cacheonix.impl.cluster.node.state.bucket;

import org.cacheonix.impl.net.ClusterNodeAddress;

/**
 */
public final class AssignBucketCommand extends BucketCommand {

   private final byte storageNumber;

   private final Integer bucketNumber;

   private final ClusterNodeAddress ownerAddress;


   public AssignBucketCommand(final String cacheName, final byte storageNumber, final Integer bucketNumber,
                              final ClusterNodeAddress ownerAddress) {

      //
      super(cacheName);

      //
      this.storageNumber = storageNumber;
      this.bucketNumber = bucketNumber;
      this.ownerAddress = ownerAddress;
   }


   public byte getStorageNumber() {

      return storageNumber;
   }


   public Integer getBucketNumber() {

      return bucketNumber;
   }


   public ClusterNodeAddress getOwnerAddress() {

      return ownerAddress;
   }


   public String toString() {

      return "AssignBucketCommand{" +
              "storage=" + storageNumber +
              ", bucketNumber=" + bucketNumber +
              ", ownerAddress=" + ownerAddress +
              "} " + super.toString();
   }
}
