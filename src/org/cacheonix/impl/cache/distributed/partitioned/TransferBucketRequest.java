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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.RequestProcessor;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.logging.Logger;

/**
 * TransferRequest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection RedundantIfStatement
 * @since Aug 12, 2009 5:27:36 PM
 */
public final class TransferBucketRequest extends CacheRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TransferBucketRequest.class); // NOPMD

   private ClusterNodeAddress currentOwner = null;

   private ClusterNodeAddress newOwner = null;

   private byte sourceStorageNumber = (byte) 0;

   private byte destinationStorageNumber = (byte) 0;

   private List<Bucket> buckets = null;


   /**
    * @noinspection WeakerAccess
    */
   public TransferBucketRequest() {

   }


   TransferBucketRequest(final String cacheName) {

      super(TYPE_CACHE_TRANSFER_BUCKET_REQUEST, cacheName);
   }


   /**
    * Sets bucket for transfer. The bucket should be locked for updates until a response confirming that the bucket was
    * transferred or transfer is canceled is received.
    *
    * @param bucket to transfer.
    */
   void addBucket(final Bucket bucket) {

      getOrCreateBuckets(1).add(bucket);
   }


   /**
    * Adds buckets.
    *
    * @param bucketsToAdd a collection of buckets to add
    */
   void addBuckets(final Collection<Bucket> bucketsToAdd) {

      getOrCreateBuckets(bucketsToAdd.size()).addAll(bucketsToAdd);
   }


   /**
    * Returns a new list of bucket numbers.
    *
    * @return the new list of bucket numbers.
    */
   public List<Integer> getBucketNumbers() {

      if (buckets == null || buckets.isEmpty()) {
         return new ArrayList<Integer>(0);
      }
      final List<Integer> result = new ArrayList<Integer>(buckets.size());
      for (final Bucket bucket : buckets) {
         result.add(bucket.getBucketNumber());
      }
      return result;
   }


   /**
    * Returns a list of existing buckets or initializes it if it has not been initialized yet.
    *
    * @param createSize size of the new list
    * @return the list of existing buckets or initializes it if it has not been initialized yet.
    */
   List<Bucket> getOrCreateBuckets(final int createSize) {

      if (buckets == null) {
         buckets = new ArrayList<Bucket>(createSize);
      }
      return buckets;
   }


   public synchronized byte getSourceStorageNumber() {

      return sourceStorageNumber;
   }


   public synchronized void setSourceStorageNumber(final byte sourceStorageNumber) {

      this.sourceStorageNumber = sourceStorageNumber;
   }


   public byte getDestinationStorageNumber() {

      return destinationStorageNumber;
   }


   public void setDestinationStorageNumber(final byte destinationStorageNumber) {

      this.destinationStorageNumber = destinationStorageNumber;
   }


   public void setNewOwner(final ClusterNodeAddress newOwner) {

      this.newOwner = newOwner;
   }


   private synchronized ClusterNodeAddress getNewOwner() {

      return newOwner;
   }


   void setCurrentOwner(final ClusterNodeAddress currentOwner) {

      this.currentOwner = currentOwner;
   }


   private ClusterNodeAddress getCurrentOwner() {

      return currentOwner;
   }


   /**
    * Executes TransferBucketRequest. Puts the transferred bucket to the local bucket registry.
    */
   protected void executeOperational() {

      Assert.assertTrue(sourceStorageNumber == destinationStorageNumber
              || sourceStorageNumber == 0 && destinationStorageNumber > 0,
              "The transfer request should be either in-storage or primary-to-replica");

      if (buckets == null || buckets.isEmpty()) {

         return;
      }

      final CacheProcessor cacheProcessor = getCacheProcessor();

      final List<Integer> rejectedBucketNumbers = new ArrayList<Integer>(buckets.size() >> 4);
      final List<Integer> completedBucketNumbers = new ArrayList<Integer>(buckets.size());
      for (final Bucket bucket : buckets) {

         final Integer bucketNumber = bucket.getBucketNumber();
         if (cacheProcessor.hasBucket(destinationStorageNumber, bucketNumber)) {

            // Reject transfer becuase the bucket is still here
            rejectedBucketNumbers.add(bucketNumber);
         } else {

            // Load bucket into the cache processor
            cacheProcessor.setBucket(destinationStorageNumber, bucketNumber, bucket);

            // Mark as reconfiguring
            bucket.setReconfiguring(true);

            // Record in completed
            completedBucketNumbers.add(bucketNumber);
         }
      }

      // Create result
      final TransferBucketResult result = new TransferBucketResult();
      result.setRejectedBucketNumbers(rejectedBucketNumbers);
      result.setTransferredBucketNumbers(completedBucketNumbers);

      if (LOG.isDebugEnabled() && result.hasRejectedBuckets()) {

         //noinspection ControlFlowStatementWithoutBraces
         LOG.debug("Responding with some buckets rejected, rejected buckets: " + result.getRejectedBucketNumbers()); // NOPMD
      }

      // Respond with success
      cacheProcessor.post(createResponse(Response.RESULT_SUCCESS, result));
   }


   /**
    * {@inheritDoc}
    */
   protected void executeBlocked() {

      // TransferBucketRequest can successfully execute
      // the bucket transfer because it is not affected
      // by the state of the CacheProcessor.
      executeOperational();
   }


   @SuppressWarnings("RedundantMethodOverride")
   protected org.cacheonix.impl.net.processor.Waiter createWaiter() {

      return new Waiter(this);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      //
      sourceStorageNumber = in.readByte();
      destinationStorageNumber = in.readByte();

      // OPTIMIZEME: simeshev@cacheonix.org - 2009-11-17 - Consider writing a reference
      // if owners are present in receiver set.
      newOwner = SerializerUtils.readAddress(in);
      currentOwner = SerializerUtils.readAddress(in);

      //
      final int bucketCount = in.readInt();
      buckets = new ArrayList<Bucket>(bucketCount);
      for (int i = 0; i < bucketCount; i++) {
         buckets.add(SerializerUtils.readBucket(in));
      }
   }


   @SuppressWarnings("ForLoopReplaceableByForEach")
   public void writeWire(final DataOutputStream out) throws IOException {

      final int bucketCount = buckets.size();
      super.writeWire(out);
      //
      out.writeByte(sourceStorageNumber);
      out.writeByte(destinationStorageNumber);

      //
      SerializerUtils.writeAddress(newOwner, out);
      SerializerUtils.writeAddress(currentOwner, out);

      //
      out.writeInt(bucketCount);
      for (int i = 0; i < bucketCount; i++) {
         SerializerUtils.writeBucket(out, buckets.get(i));
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final TransferBucketRequest that = (TransferBucketRequest) o;

      if (sourceStorageNumber != that.sourceStorageNumber) {
         return false;
      }
      if (destinationStorageNumber != that.destinationStorageNumber) {
         return false;
      }
      if (buckets != null ? !buckets.equals(that.buckets) : that.buckets != null) {
         return false;
      }
      if (currentOwner != null ? !currentOwner.equals(that.currentOwner) : that.currentOwner != null) {
         return false;
      }
      if (newOwner != null ? !newOwner.equals(that.newOwner) : that.newOwner != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (currentOwner != null ? currentOwner.hashCode() : 0);
      result = 31 * result + (newOwner != null ? newOwner.hashCode() : 0);
      result = 31 * result + (int) sourceStorageNumber;
      result = 31 * result + (int) destinationStorageNumber;
      result = 31 * result + (buckets != null ? buckets.hashCode() : 0);
      return result;
   }


   /**
    * @noinspection UnnecessaryParentheses
    */
   public String toString() {

      return "TransferBucketRequest{" +
              "sourceStorageNumber=" + sourceStorageNumber +
              ", destinationStorageNumber=" + destinationStorageNumber +
              ", buckets.size()=" + ((buckets == null) ? null : Integer.toString(buckets.size())) +
              ", currentOwner=" + ((currentOwner == null) ? "null" : Integer.toString(currentOwner.getTcpPort())) +
              ", newOwner=" + ((newOwner == null) ? "null" : Integer.toString(newOwner.getTcpPort())) +
              "} " + super.toString();
   }

   // ================================================================================================================
   //
   // Waiter
   //
   // ================================================================================================================


   /**
    * This waiter is invoked when a response to TransferRequest is received. That response should be simulated as a
    * result of notification about change in ownership. In case of success, this waiter should release the bucket. Also,
    * the response can be received as a result of the transfer receiver that decided not to accept transfer.
    *
    * @noinspection ClassNameSameAsAncestorName
    */
   private static final class Waiter extends org.cacheonix.impl.net.processor.Waiter {

      Waiter(final Request request) {

         super(request);
      }


      /**
       * {@inheritDoc}
       * <p/>
       * This method enhances the default behaviour by announcing bucket ownership change
       */
      protected final synchronized void notifyFinished() {

         final TransferBucketRequest request = (TransferBucketRequest) getRequest();
         final RequestProcessor processor = request.getProcessor();

         // What does it mean that the processor it null?

         if (processor != null && getResult() instanceof TransferBucketResult) {

            final TransferBucketResult result = (TransferBucketResult) getResult();


            // Finish bucket transfer for successfully transferred buckets by posting an announcement
            if (result.hasTransferredBuckets()) {

               // Remove transferred bucket from the request
               for (final Integer transferredBucketNumber : result.getTransferredBucketNumbers()) {

                  for (final Iterator<Bucket> requestBucketIter = request.buckets.iterator(); requestBucketIter.hasNext(); ) {

                     final Bucket requestBucket = requestBucketIter.next();
                     if (requestBucket.getBucketNumber() == transferredBucketNumber) {

                        requestBucketIter.remove();
                        break;
                     }
                  }
               }


               // Post announcement
               final BucketTransferCompletedAnnouncement ann = new BucketTransferCompletedAnnouncement(request.getCacheName());
               ann.setDestinationStorageNumber(request.getDestinationStorageNumber());
               ann.addTransferredBucketNumbers(result.getTransferredBucketNumbers());
               ann.setSourceStorageNumber(request.getSourceStorageNumber());
               ann.setPreviousOwnerAddress(request.getCurrentOwner());
               ann.setNewOwnerAddress(request.getNewOwner());
               processor.post(ann);
            }


            // Retry bucket transfer if there are buckets to send
            if (result.hasRejectedBuckets()) {

               final TransferBucketRequest retryTransferRequest = new TransferBucketRequest(request.getCacheName());
               retryTransferRequest.setDestinationStorageNumber(request.getDestinationStorageNumber());
               retryTransferRequest.setSourceStorageNumber(request.getSourceStorageNumber());
               retryTransferRequest.setCurrentOwner(request.getCurrentOwner());
               retryTransferRequest.setReceiver(request.getNewOwner());
               retryTransferRequest.setNewOwner(request.getNewOwner());
               retryTransferRequest.addBuckets(request.buckets);
               processor.post(retryTransferRequest);
            }
         }

         super.notifyFinished();
      }


      /**
       * {@inheritDoc}
       * <p/>
       * This method enhances default behaviour by setting the result contained in the TransferResponse.
       *
       * @see TransferBucketResult
       */
      public final void notifyResponseReceived(final Response message) throws InterruptedException {

         if (message instanceof CacheResponse) {

            switch (message.getResultCode()) {

               // Result will contain TransferBucketResult
               case Response.RESULT_SUCCESS:

                  setResult(message.getResult());
                  break;
               case Response.RESULT_INACCESSIBLE:
               case Response.RESULT_RETRY:
               case Response.RESULT_ERROR:
               default:

                  // Reject all bucket numbers
                  final TransferBucketResult result = new TransferBucketResult();
                  final TransferBucketRequest request = (TransferBucketRequest) getRequest();
                  result.setRejectedBucketNumbers(request.getBucketNumbers());
                  setResult(result);
                  break;
            }
         }

         super.notifyResponseReceived(message);
      }
   }

   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new TransferBucketRequest();
      }
   }
}

