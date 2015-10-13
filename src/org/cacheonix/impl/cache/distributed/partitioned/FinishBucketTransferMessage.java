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

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Translation of a {@link FinishBucketTransferCommand } to an async message suitable for processing by a cache
 * processor.
 * <p/>
 * This message is sent to the local cache processors to unlock or delete the buckets for that the transfer was
 * completed.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 13, 2009 3:51:09 PM
 */
@SuppressWarnings("RedundantIfStatement")
public final class FinishBucketTransferMessage extends LocalCacheMessage {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(FinishBucketTransferMessage.class); // NOPMD

   private byte sourceStorageNumber = -1;

   private byte destinationStorageNumber = -1;

   private List<Integer> bucketNumbers = null;

   private ClusterNodeAddress previousOwner = null;

   private ClusterNodeAddress newOwner = null;


   public FinishBucketTransferMessage(final String cacheName) {

      super(TYPE_CACHE_FINISH_BUCKET_TRANSFER_MESSAGE, cacheName);
   }


   @SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
   public FinishBucketTransferMessage() {

   }


   public ClusterNodeAddress getNewOwner() {

      return newOwner;
   }


   public void setNewOwner(final ClusterNodeAddress newOwner) {

      this.newOwner = newOwner;
   }


   public ClusterNodeAddress getPreviousOwner() {

      return previousOwner;
   }


   public void setPreviousOwner(final ClusterNodeAddress previousOwner) {

      this.previousOwner = previousOwner;
   }


   public byte getSourceStorageNumber() {

      return sourceStorageNumber;
   }


   public void setSourceStorageNumber(final byte sourceStorageNumber) {

      this.sourceStorageNumber = sourceStorageNumber;
   }


   public byte getDestinationStorageNumber() {

      return destinationStorageNumber;
   }


   public void setDestinationStorageNumber(final byte destinationStorageNumber) {

      this.destinationStorageNumber = destinationStorageNumber;
   }


   /**
    * @param bucketNumbers a list of bucket numbers.
    * @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   public void setBucketNumbers(final List<Integer> bucketNumbers) {

      this.bucketNumbers = CollectionUtils.copy(bucketNumbers);
   }


   public List<Integer> getBucketNumbers() {

      if (bucketNumbers == null) {

         bucketNumbers = new ArrayList<Integer>(0);
      }
      return bucketNumbers;
   }


   protected void executeOperational() {

      Assert.assertTrue(sourceStorageNumber == destinationStorageNumber
              || sourceStorageNumber == 0 && destinationStorageNumber > 0,
              "The transfer request should be either in-storage or primary-to-replica");

      final CacheProcessor cacheProcessor = getCacheProcessor();
      final ClusterNodeAddress localAddress = cacheProcessor.getAddress();
      if (localAddress.equals(previousOwner)) {

         finishOnSourceOwner(cacheProcessor);

      } else if (localAddress.equals(newOwner)) {

         finishOnDestinationOwner(cacheProcessor);
      }
   }


   private void finishOnSourceOwner(final CacheProcessor cacheProcessor) {

      // Process completion of the transfer of a set of
      // buckets on the previous owner by removing them.

      // This will also take care of the case when the
      // primary owner is the previous owner.

      final List<Integer> bucketNumbers = getBucketNumbers();
//      if (LOG.isDebugEnabled()) {
//         LOG.debug("Completing transfer by removing " + bucketNumbers.size() + " buckets, message: " + this);
//      }
      for (final Integer bucketNumber : bucketNumbers) {

         // By this time new requests should be already aware that the ownership
         // changed.

         if (sourceStorageNumber == destinationStorageNumber) {

            // Finish in-storage transfer (move) by deleting source bucket.
            final Bucket bucket = cacheProcessor.removeBucket(sourceStorageNumber, bucketNumber);
            Assert.assertTrue(bucket != null && bucket.isReconfiguring(), "Bucket should be in reconfiguring state {0}", bucket);
         } else if (sourceStorageNumber == 0 && destinationStorageNumber > 0) {

            // Finish replica restore by marking source bucket as non-reconfiguring
            final Bucket bucket = cacheProcessor.getBucket(sourceStorageNumber, bucketNumber);
            Assert.assertNotNull(bucket, "Bucket should be in reconfiguring state {0}", bucket);
            Assert.assertTrue(bucket.isReconfiguring(), "Bucket should be in reconfiguring state {0}", bucket);
            bucket.setReconfiguring(false);
         }
      }
   }


   private void finishOnDestinationOwner(final CacheProcessor cacheProcessor) {

      final List<Integer> bucketNumbers = getBucketNumbers();
//      if (LOG.isDebugEnabled()) {
//         LOG.debug("Completing transfer by removing " + bucketNumbers.size() + " buckets, message: " + this);
//      }
      for (final Integer bucketNumber : bucketNumbers) {

         // By this time new requests should be already aware that the ownership
         // changed.

         final Bucket bucket = cacheProcessor.getBucket(destinationStorageNumber, bucketNumber);
         Assert.assertNotNull(bucket, "Bucket should be in reconfiguring state {0}", bucket);
         Assert.assertTrue(bucket.isReconfiguring(), "Bucket should be in reconfiguring state {0}", bucket);
         bucket.setReconfiguring(false);
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void executeBlocked() {

      // Blocked mode is processed the same way as operational
      // because it is a completion notification.
      executeOperational();
   }


   public String toString() {

      return "FinishBucketTransferMessage{" +
              "sourceStorageNumber=" + sourceStorageNumber +
              ", destinationStorageNumber=" + destinationStorageNumber +
              ", bucketNumbersCount=" + (bucketNumbers == null ? "null" : Integer.toString(bucketNumbers.size())) +
              ", newOwner=" + ((newOwner == null) ? "null" : Integer.toString(newOwner.getTcpPort())) +
              ", previousOwner=" + ((previousOwner == null) ? "null" : Integer.toString(previousOwner.getTcpPort())) +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new FinishBucketTransferMessage();
      }
   }
}
