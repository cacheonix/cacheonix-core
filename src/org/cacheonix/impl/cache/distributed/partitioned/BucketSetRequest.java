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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.PrepareResult;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.IntArrayList;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.array.IntIterator;
import org.cacheonix.impl.util.array.IntProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BucketSetRequest is sent to an owner of a set of buckets.
 * <p/>
 * BucketSetRequest should be used every time all buckets must be processed. The root request is initialized with a
 * complete bucket set.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see ClearRequest
 * @see ContainsValueRequest
 * @see ExecuteRequest
 * @see GetEntrySetRequest
 * @see GetKeySetRequest
 * @see RetainAllRequest
 * @see SizeRequest
 * @see ValuesRequest * @since Apr 22, 2010 2:29:40 PM
 */
@SuppressWarnings("RedundantIfStatement")
public abstract class BucketSetRequest extends AggregatingRequest {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketSetRequest.class); // NOPMD

   /**
    * Set of buckets to process.
    */
   private IntHashSet bucketSet = null;


   /**
    * Default constructor to support Wireable.
    */
   protected BucketSetRequest() {

   }


   /**
    * @param wireableType the wireable type.
    * @param cacheName    the cache name.
    * @param readRequest  a flag indicating if this is a read request. If the flag is set to <code>false</code>, this is
    *                     a write request. Read or write type determines if the request checks out a bucket for read or
    *                     for write. Read requests extend the read lease time if the willCacheUntil is set and if there
    *                     are no pending write requests for the bucket.
    */
   protected BucketSetRequest(final int wireableType, final String cacheName, final boolean readRequest) {

      super(wireableType, cacheName, readRequest);
   }


   /**
    * Returns an existing bucket or initializes and returns a new one.
    *
    * @return bucket set
    */
   public final IntHashSet getBucketSet() {

      if (bucketSet == null) {
         bucketSet = new IntHashSet();
      }
      return bucketSet;
   }


   /**
    * Sets bucket set.
    *
    * @param bucketSet bucket set to set.
    */
   public final void setBucketSet(final IntHashSet bucketSet) {

      this.bucketSet = bucketSet;
   }


   /**
    * Clears bucket set by setting it to null.
    */
   final void clear() {

      bucketSet = null;
   }


   public PrepareResult prepare() {

      // All root bucket set requests require a full bucket set
      if (isRootRequest()) {

         bucketSet = getCacheProcessor().createBucketSet();
      }

      // Call super
      return super.prepare();
   }


   /**
    * {@inheritDoc}
    */
   public final void executeOperational() {

      Assert.assertFalse(isRootRequest(), "Request cannot be root");
      Assert.assertNotNull(bucketSet, "Subrequest bucket set cannot be null");
      Assert.assertFalse(bucketSet.isEmpty(), "Subrequest bucket set cannot be empty");

      //
      // Collect buckets to process and collect rejects
      //
      IntHashSet rejectedBuckets = null;
      final List<Bucket> bucketToProcess = new LinkedList<Bucket>();
      final CacheProcessor processor = getCacheProcessor();
      final Integer storageNumber = getStorageNumber();
      for (final IntIterator iterator = bucketSet.iterator(); iterator.hasNext(); ) {

         // Get bucket
         final int bucketNumber = iterator.next();
         final Bucket bucket = processor.getBucket(storageNumber, bucketNumber);
         if (!processor.isBucketOwner(storageNumber, bucketNumber) || bucket == null || bucket.isReconfiguring()) {

            if (rejectedBuckets == null) {

               rejectedBuckets = new IntHashSet(1);
            }
            rejectedBuckets.add(bucketNumber);
            continue;
         }

         bucketToProcess.add(bucket);
      }

      //
      // Process
      //
      final ProcessingResult processingResult = processBuckets(bucketToProcess);

      //
      // Create response and set result and rejected buckets
      //
      final AggregatingResponse response = (AggregatingResponse) createResponse(Response.RESULT_SUCCESS);
      response.setRejectedBuckets(rejectedBuckets);
      response.setResult(processingResult.getResult());

      //
      // Respond now if this is a replica because replicas don't need replication or cache invalidation.
      //
      if (isReplicaRequest() || !processingResult.hasModifiedBuckets()) {

         respond(response);

         return;
      }


      //
      // Create front cache invalidation requests
      //

      // Collect bucket numbers that have unexpired leases
      final List<Bucket> modifiedBuckets = processingResult.getModifiedBuckets();
      final Collection<Request> subrequests = new ArrayList<Request>(processor.getBucketOwnerCount() + 1);
      final IntArrayList bucketNumbersToInvalidate = new IntArrayList(modifiedBuckets.size());
      for (final Bucket bucket : modifiedBuckets) {

         if (hasUnexpiredLease(bucket)) {

            bucket.setLeaseExpirationTime(null);
            bucketNumbersToInvalidate.add(bucket.getBucketNumber());
         }
      }

      // Add bucket invalidation announcement
      if (!CollectionUtils.isEmpty(bucketNumbersToInvalidate)) {

         subrequests.add(new ClearFrontCacheBucketAnnouncement(getCacheName(), bucketNumbersToInvalidate));
      }

      //
      // Create replica update subrequests
      //

      // Create modified bucket set
      final IntHashSet modifiedBucketNumbers = new IntHashSet(modifiedBuckets.size());
      for (final Bucket bucket : modifiedBuckets) {

         modifiedBucketNumbers.add(bucket.getBucketNumber());
      }

      // Add replica subrequests
      final int storageEnd = getCacheProcessor().getReplicaCount();
      for (int i = 1; i <= storageEnd; i++) {

         subrequests.addAll(splitBucketSet(i, modifiedBucketNumbers));
      }

      //
      // Attach subrequests
      //
      ((CacheDataRequest.Waiter) getWaiter()).attachSubrequests(response, subrequests);


      //
      // Post subrequests
      //
      getProcessor().post(subrequests);

      //
      // Respond now if no waiters were added by posting subrequest
      //
      if (!isWaitingForSubrequests()) {

         // Post
         respond(response);
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation splits this request's <code>bucketSet</code> by an owner.
    */
   protected final Collection<? extends AggregatingRequest> split(final int storageNumber) {

      return splitBucketSet(storageNumber, bucketSet);
   }


   /**
    * {@inheritDoc}
    */
   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   /**
    * Splits a set of bucket numbers on a per-owner basis at the given storage number to a set of objects extending
    * <code>BucketSetRequest</code>. The creating of the request is done using a template method
    * <code>createRequest()</code>.
    *
    * @param storageNumber    owner's storage number.
    * @param bucketSetToSplit the set of bucket numbers to split.
    * @return a collection of <code>BucketSetRequests</code> with pre-set sender, storage number and receiver.
    * @see #createRequest()
    * @see BucketSetRequest
    */
   protected final Collection<BucketSetRequest> splitBucketSet(final int storageNumber,
                                                               final IntHashSet bucketSetToSplit) {

      // Check if there is anything to handle
      if (bucketSetToSplit == null || bucketSetToSplit.isEmpty()) {
         return Collections.emptyList();
      }

      // Prepare result
      final CacheProcessor cacheProcessor = getCacheProcessor();
      final Map<ClusterNodeAddress, BucketSetRequest> result = new HashMap<ClusterNodeAddress, BucketSetRequest>(1);

      // Iterate bucket set
      bucketSetToSplit.forEach(new IntProcedure() {

         @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
         public boolean execute(final int bucketNumber) {

            //
            ClusterNodeAddress bucketOwner = cacheProcessor.getBucketOwner(storageNumber, bucketNumber);
            if (bucketOwner == null) {
               if (storageNumber == 0) {
                  // REVIEWME: simeshev@cacheonix.org - 2010-03-05 - Right now we post
                  // unassigned to self. This may create a send-receive-retry cycle
                  // while the ownership is stabilized. Consider a timed approach,
                  // maybe through a deferred request queue.
                  bucketOwner = cacheProcessor.getAddress();
               } else {

                  // Continue
                  return true;
               }
            }

            //
            BucketSetRequest partialRequest = result.get(bucketOwner);
            if (partialRequest == null) {
               partialRequest = createRequest();
               partialRequest.setSender(cacheProcessor.getAddress());
               partialRequest.setStorageNumber(storageNumber);
               partialRequest.setReceiver(bucketOwner);
               result.put(bucketOwner, partialRequest);
            }
            partialRequest.getBucketSet().add(bucketNumber);

            // Continue
            return true;
         }
      });

      // Return generated requests
      return result.values();
   }


   /**
    * Processes buckets according to the request purpose. This method should post a response if it is required or post
    * subrequests.
    *
    * @param bucketsToProcess a list of locked buckets to process
    * @return an object holding the result of the call and the list of modified buckets.
    * @see #executeOperational()
    */
   protected abstract ProcessingResult processBuckets(final List<Bucket> bucketsToProcess);


   /**
    * A implementation of this factory method should return a new instance of a concrete implementation of
    * <code>BucketSetRequest</code>.
    *
    * @return a new instance of a concrete implementation of <code>BucketSetRequest</code>.
    */
   protected abstract BucketSetRequest createRequest();


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      bucketSet = SerializerUtils.readIntHashSet(in);
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeIntHashSet(out, bucketSet);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof BucketSetRequest)) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final BucketSetRequest that = (BucketSetRequest) o;

      if (bucketSet != null ? !bucketSet.equals(that.bucketSet) : that.bucketSet != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (bucketSet != null ? bucketSet.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "BucketSetRequest{" +
              "bucketSet.size()=" + (bucketSet == null ? "null" : Integer.toString(bucketSet.size())) +
              "} " + super.toString();
   }


   // ==================================================================================================================
   //
   // Result
   //
   // ==================================================================================================================


   /**
    * The object holding result of the call to {@link BucketSetRequest#processBuckets(List)}.
    */
   protected static final class ProcessingResult {

      /**
       * The result of execution.
       */
      private Object result = null;

      /**
       * Numbers of modified buckets or null or empty.
       */
      private List<Bucket> modifiedBuckets = null;


      /**
       * Creates new result.
       *
       * @param result          the result of execution.
       * @param modifiedBuckets a set of modified buckets.
       */
      public ProcessingResult(final Object result, final List<Bucket> modifiedBuckets) {

         this.result = result;
         this.modifiedBuckets = CollectionUtils.copy(modifiedBuckets);
      }


      /**
       * Returns true if has a non-null, non-empty set of modified bucket numbers.
       *
       * @return true if has a non-null, non-empty set of modified bucket numbers.
       */
      public boolean hasModifiedBuckets() {

         return modifiedBuckets != null && !modifiedBuckets.isEmpty();
      }


      /**
       * Returns the execution result.
       *
       * @return execution result.
       */
      public Object getResult() {

         return result;
      }


      /**
       * Returns the set of numbers of modified buckets or null or empty.
       *
       * @return the set of numbers of modified buckets or null or empty.
       */
      public List<Bucket> getModifiedBuckets() {

         return modifiedBuckets;
      }


      public String toString() {

         return "Result{" +
                 "result=" + result +
                 ", modifiedBuckets=" + StringUtils.sizeToString(modifiedBuckets) +
                 '}';
      }
   }


   // ==================================================================================================================
   //
   // Waiter
   //
   // ==================================================================================================================


   /**
    * Waiter for the <code>BucketSetResponse</code>.
    */
   static final class Waiter extends AggregatingRequest.Waiter {

      /**
       * Creates waiter.
       *
       * @param request request UUID
       */
      Waiter(final Request request) {

         super(request);
      }


      protected void processSuccessResponse(final AggregatingRequest request,
                                            final AggregatingRequest.Waiter ownerWaiter,
                                            final AggregatingResponse response) {

         final IntHashSet rejectedBuckets = response.handOffRejectedBuckets();
         if (rejectedBuckets != null && !rejectedBuckets.isEmpty()) {

            // This is the only place we cast to a particular request type
            final BucketSetRequest bucketSetRequest = (BucketSetRequest) request;

            // Remove rejected buckets from the request
            final boolean sizeIsSame = bucketSetRequest.getBucketSet().size() == rejectedBuckets.size();
            final boolean rbsModified = bucketSetRequest.getBucketSet().retainAll(rejectedBuckets.toArray());
            Assert.assertTrue(rbsModified || sizeIsSame, "Request bucket set should have had rejected buckets");
         } else {

            // All buckets were processed, no rejected buckets
            request.clear();
         }

         // REVIEWME: simeshev@viewtier.com - 2010-05-13 - Right now we add all responses w/o considering if they
         // are going to be aggregated (for instance, ClearRequest only cares about errors). This may lead for
         // responses hanging around for longer than they should instead of being GC-ed immediately.

         if (request.isPrimaryRequest()) {
            ownerWaiter.getPartialResponses().add(response);
         }
      }
   }
}
