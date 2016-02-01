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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * @noinspection RedundantIfStatement
 */
public final class BeginBucketTransferMessage extends LocalCacheMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BeginBucketTransferMessage.class); // NOPMD

   private byte sourceStorageNumber = (byte) 0;

   private byte destinationStorageNumber = (byte) 0;

   private ClusterNodeAddress currentOwner = null;

   private ClusterNodeAddress newOwner = null;

   private Collection<Integer> bucketNumbers = null;


   public BeginBucketTransferMessage(final String cacheName) {

      super(TYPE_CACHE_BEGIN_BUCKET_TRANSFER_MESSAGE, cacheName);
   }


   /**
    * Required by Wireable.
    */
   public BeginBucketTransferMessage() {

   }


   /**
    * Sets a collection of bucket numbers.
    *
    * @param bucketNumbers unmodifiable collection of bucket numbers.
    * @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   public void setBucketNumbers(final Collection<Integer> bucketNumbers) {

      this.bucketNumbers = bucketNumbers;
   }


   /**
    * @return a collection of Integer bucket numbers.
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public Collection<Integer> getBucketNumbers() {

      return bucketNumbers;
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


   public ClusterNodeAddress getCurrentOwner() {

      return currentOwner;
   }


   public void setCurrentOwner(final ClusterNodeAddress currentOwner) {

      this.currentOwner = currentOwner;
   }


   public ClusterNodeAddress getNewOwner() {

      return newOwner;
   }


   public void setNewOwner(final ClusterNodeAddress newOwner) {

      this.newOwner = newOwner;
   }


   /**
    * {@inheritDoc}
    */
   protected void executeOperational() {

      final CacheProcessor cacheProcessor = getCacheProcessor();

      // Create reusable vars
      final ClusterNodeAddress localAddress = cacheProcessor.getAddress();

      // Check if we are current owner.
      Assert.assertTrue(localAddress.equals(currentOwner), "Begin transfer should be addresses to the current owner {0}", this);

      // A list of bucket numbers that cannot be transferred. For example,
      // bucket cannot be transferred if it cannot be locked for write.
      final List<Integer> rejectTransfers = new LinkedList<Integer>();

      // A list of empty buckets to send in a batch
      final List<Bucket> emptyBuckets = new LinkedList<Bucket>();

      // A list of non-empty buckets to send in a batch
      final List<Bucket> nonEmptyBuckets = new LinkedList<Bucket>();

      // Mark buckets as reconfiguring, collect rejects, empty and non-empty
      for (final Integer bucketNumber : bucketNumbers) {

         // Lock or register in the the rejection list if cannot be locked

         final Bucket bucket = cacheProcessor.getBucket(sourceStorageNumber, bucketNumber);
         if (bucket == null) {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) {
               LOG.debug("Rejecting bucket transfer from " + currentOwner + ", sourceStorage '" + sourceStorageNumber + "' to " + newOwner + ", destinationStorage '" + destinationStorageNumber + "' because bucket " + bucketNumber + " is null"); // NOPMD
            }
            rejectTransfers.add(bucketNumber);
         } else if (bucket.isReconfiguring()) {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) {
               LOG.debug("Rejecting bucket transfer from " + currentOwner + ", sourceStorage '" + sourceStorageNumber + "' to " + newOwner + ", destinationStorage '" + destinationStorageNumber + "' because bucket " + bucketNumber + " is reconfiguring"); // NOPMD
            }
            rejectTransfers.add(bucketNumber);
         } else {

            // Mark bucket as being reconfigured. This flag is used as a mutex to prevent
            // other write and configuration requests from accessing the bucket
            bucket.setReconfiguring(true);

            // Add to empty or non-empty collections for further processing
            if (bucket.isEmpty()) {

               emptyBuckets.add(bucket.copy());
            } else {

               nonEmptyBuckets.add(bucket.copy());
            }
         }
      }

      // Announce rejected transfers
      final String cacheName = getCacheName();
      if (!rejectTransfers.isEmpty()) {

         final BucketTransferRejectedAnnouncement ann = new BucketTransferRejectedAnnouncement(cacheName);
         ann.setDestinationStorageNumber(destinationStorageNumber);
         ann.setSourceStorageNumber(sourceStorageNumber);
         ann.setPreviousOwner(currentOwner);
         ann.setBucketNumbers(rejectTransfers);
         ann.setNewOwner(newOwner);
         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Rejecting transfer, ann: " + ann); // NOPMD
         cacheProcessor.post(ann);
      }

      // Begin transferring empty
      if (!emptyBuckets.isEmpty()) {

         final TransferBucketRequest transferRequest = new TransferBucketRequest(cacheName);
         transferRequest.setDestinationStorageNumber(destinationStorageNumber);
         transferRequest.setSourceStorageNumber(sourceStorageNumber);
         transferRequest.setCurrentOwner(currentOwner);
         transferRequest.setReceiver(newOwner);
         transferRequest.setNewOwner(newOwner);
         transferRequest.addBuckets(emptyBuckets);
         cacheProcessor.post(transferRequest);
      }

      // Begin transferring non-empty
      if (!nonEmptyBuckets.isEmpty()) {

         for (final Bucket bucket : nonEmptyBuckets) {

            final TransferBucketRequest transferRequest = new TransferBucketRequest(cacheName);
            transferRequest.setDestinationStorageNumber(destinationStorageNumber);
            transferRequest.setSourceStorageNumber(sourceStorageNumber);
            transferRequest.addBucket(bucket);
            transferRequest.setCurrentOwner(currentOwner);
            transferRequest.setReceiver(newOwner);
            transferRequest.setNewOwner(newOwner);
            cacheProcessor.post(transferRequest);
         }
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void executeBlocked() {
      // This is an operation that may progress even if the cluster
      // is blocked. It will progress if the replica owner is alive.
      // Otherwise the TransferBucketRequest that this message produces
      // must announce rollback.
      executeOperational();
   }


   public String toString() {

      return "BeginBucketTransferMessage{" +
              "bucketNumbers=" + StringUtils.sizeToString(bucketNumbers) +
              ", currentOwner=" + currentOwner +
              ", newOwner=" + newOwner +
              ", sourceStorageNumber=" + sourceStorageNumber +
              ", destinationStorageNumber=" + destinationStorageNumber +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new BeginBucketTransferMessage();
      }
   }
}
