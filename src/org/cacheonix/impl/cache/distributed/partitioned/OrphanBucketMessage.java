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

import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A message to the CacheProcessor to orphan a bucket.
 */
public final class OrphanBucketMessage extends LocalCacheMessage {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(OrphanBucketMessage.class); // NOPMD

   /**
    * A storage number that the bucket to orphan belongs to.
    */
   private byte storageNumber;


   /**
    * A bucket number to orphan.
    */
   private Integer bucketNumber;


   /**
    * Required by Wireable.
    */
   public OrphanBucketMessage() {

   }


   /**
    * Creates a new OrphanBucketMessage.
    *
    * @param cacheName     a cache name.
    * @param storageNumber a storage number.
    * @param bucketNumber  a bucket number.
    */
   public OrphanBucketMessage(final String cacheName, final byte storageNumber, final Integer bucketNumber) {

      super(TYPE_CACHE_ORPHAN_BUCKET, cacheName);
      this.bucketNumber = bucketNumber;
      this.storageNumber = storageNumber;
   }


   /**
    * Returns the storage number that the bucket to orphan belongs to.
    *
    * @return the storage number that the bucket to orphan belongs to.
    */
   byte getStorageNumber() {

      return storageNumber;
   }


   /**
    * Returns the bucket number to orphan.
    *
    * @return the bucket number to orphan.
    */
   Integer getBucketNumber() {

      return bucketNumber;
   }


   protected void executeOperational() {


      final CacheProcessor processor = getCacheProcessor();

      final Bucket bucket = processor.getBucket((int) storageNumber, bucketNumber);
      if (bucket == null || bucket.isReconfiguring()) {

         // REVIEWME: simeshev@cacheonix.com - 2011-04-07 -> Not clear if it is OK to bail out
         // and leave the bucket undeleted. What are the other options? Wait longer until
         // readers finish? Don't try to lock just delete? What are the implications?

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled())
            LOG.debug("Cannot orphan bucket '" + bucket + "', bucket is  " + ((bucket == null) ? "null" : "reconfiguring")); // NOPMD
         return;
      }

      // Delete bucket
      processor.removeBucket(storageNumber, bucketNumber);
   }


   protected void executeBlocked() {

      executeOperational();
   }


   public String toString() {

      return "OrphanBucketMessage{" +
              "storage=" + storageNumber +
              ", bucketNumber=" + bucketNumber +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new OrphanBucketMessage();
      }
   }
}
