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
import java.util.Collections;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Translation of a <code>CancelBucketTransferCommand</code> to an async message suitable for processing by a cache
 * node.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
@SuppressWarnings("RedundantIfStatement")
public final class CancelBucketTransferMessage extends LocalCacheMessage {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CancelBucketTransferMessage.class); // NOPMD

   private byte sourceStorageNumber = -1;

   private byte destinationStorageNumber = -1;

   private ClusterNodeAddress previousOwner = null;

   private ClusterNodeAddress newOwner = null;

   private List<Integer> bucketNumbers = Collections.emptyList();


   public CancelBucketTransferMessage(final String cacheName) {

      super(TYPE_CACHE_CANCEL_BUCKET_TRANSFER_MESSAGE, cacheName);
   }


   public CancelBucketTransferMessage() {

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
    * Returns an unmodifiable collection of bucket numbers.
    *
    * @return unmodifiable collection of bucket numbers.
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public Collection<Integer> getBucketNumbers() {

      return bucketNumbers;
   }


   /**
    * @param bucketNumbers unmodifiable collection of bucket numbers.
    * @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   public void setBucketNumbers(final List<Integer> bucketNumbers) {

      this.bucketNumbers = bucketNumbers;
   }


   /**
    * {@inheritDoc}
    */
   protected void executeOperational() {

      final CacheProcessor cacheProcessor = getCacheProcessor();
      final ClusterNodeAddress address = cacheProcessor.getAddress();
      if (address.equals(previousOwner)) {

         for (final Integer bucketNumber : bucketNumbers) {

            final Bucket bucket = cacheProcessor.getBucket(sourceStorageNumber, bucketNumber);
            if (bucket == null) {

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Bucket " + bucketNumber + " is supposed to be at the previous owner, but it is not. It is possible that the previous owner called the cancel itself.");
               }
            } else {

               if (LOG.isDebugEnabled() && !bucket.isReconfiguring()) {
                  LOG.debug("Bucket " + bucketNumber + " is supposed to be in the reconfiguring state at the previous owner, but it is not. It is possible that the previous owner called the cancel itself.");
               }
               bucket.setReconfiguring(false);
            }
         }
      } else if (address.equals(newOwner)) {

         // Remove buckets
         if (LOG.isDebugEnabled()) {
            LOG.debug("Removing " + bucketNumbers.size() + " buckets from storage " + destinationStorageNumber);
         }

         for (final Integer bucketNumber : bucketNumbers) { // NOPMD
            cacheProcessor.removeBucket(destinationStorageNumber, bucketNumber);
         }
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

      return "CancelBucketTransferMessage{" +
              "sourceStorageNumber=" + sourceStorageNumber +
              ", destinationStorageNumber=" + destinationStorageNumber +
              ", previousOwner=" + previousOwner +
              ", newOwner=" + newOwner +
              ", bucketNumbers=" + bucketNumbers +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new CancelBucketTransferMessage();
      }
   }
}