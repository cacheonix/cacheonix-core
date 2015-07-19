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

/**
 * A message to the CacheProcessor to assign a bucket.
 */
public final class AssignBucketMessage extends LocalCacheMessage {

   /**
    * A storage number that the bucket to assign belongs to.
    */
   private byte storageNumber;


   /**
    * A bucket number to assign.
    */
   private Integer bucketNumber;


   /**
    * Required by Wireable.
    */
   public AssignBucketMessage() {

   }


   /**
    * Creates a new AssignBucketMessage.
    *
    * @param cacheName     a cache name.
    * @param storageNumber a storage number.
    * @param bucketNumber  a bucket number.
    */
   public AssignBucketMessage(final String cacheName, final byte storageNumber, final Integer bucketNumber) {

      super(TYPE_CACHE_ORPHAN_BUCKET, cacheName);
      this.bucketNumber = bucketNumber;
      this.storageNumber = storageNumber;
   }


   /**
    * Returns the storage number that the bucket to assign belongs to.
    *
    * @return the storage number that the bucket to assign belongs to.
    */
   byte getStorageNumber() {

      return storageNumber;
   }


   /**
    * Returns the bucket number to assign.
    *
    * @return the bucket number to assign.
    */
   Integer getBucketNumber() {

      return bucketNumber;
   }


   protected void executeOperational() {


      final CacheProcessor processor = getCacheProcessor();

      processor.createBucket(storageNumber, bucketNumber);

   }


   protected void executeBlocked() {

      executeOperational();
   }


   public String toString() {

      return "AssignBucketMessage{" +
              "storage=" + storageNumber +
              ", bucketNumber=" + bucketNumber +
              "} " + super.toString();
   }
}
