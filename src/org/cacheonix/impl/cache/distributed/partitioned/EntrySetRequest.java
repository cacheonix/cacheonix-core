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

import org.cacheonix.exceptions.RuntimeIOException;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.IntArrayList;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.IntObjectProcedure;
import org.cacheonix.impl.util.array.IntProcedure;
import org.cacheonix.impl.util.array.ObjectObjectProcedure;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * EntrySetRequest is sent to owners of a set of entries.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
@SuppressWarnings("RedundantIfStatement")
public abstract class EntrySetRequest extends AggregatingRequest {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EntrySetRequest.class); // NOPMD

   /**
    * Set of entries to process placed in buckets. At the root they are assigned to bucket 0 because all root needs is a
    * storage and because <code>splitKeySet()</code> operates on keys and ignores bucket numbers. Bucket numbers are
    * used at <code>notifyFinished()</code> when retaining processed keys.
    * <p/>
    * Int key is a bucket number and the hash set is keys in the bucket.
    */
   private IntObjectHashMap<HashMap<Binary, Binary>> entrySet = null;


   /**
    * Default constructor to support Wireable.
    */
   protected EntrySetRequest() {

   }


   /**
    * Abstract class constructor.
    *
    * @param wireableType wireable type.
    * @param cacheName    cache name.
    * @param readRequest  a flag indicating if this is a read request. If the flag is set to <code>false</code>, this is
    *                     a write request. Read or write type determines if the request checks out a bucket for read or
    *                     for write. Read requests extend the read lease time if the willCacheUntil is set and if there
    *                     are no pending write requests for the bucket.
    */
   @SuppressWarnings("SameParameterValue")
   protected EntrySetRequest(final int wireableType, final String cacheName, final boolean readRequest) {

      super(wireableType, cacheName, readRequest);
   }


   /**
    * Returns a key set or initializes and returns a new one.
    *
    * @return the key set
    */
   @SuppressWarnings("ReturnOfCollectionOrArrayField")
   final IntObjectHashMap<HashMap<Binary, Binary>> getEntrySet() {

      if (entrySet == null) {
         entrySet = new IntObjectHashMap<HashMap<Binary, Binary>>(1);
      }
      return entrySet;
   }


   /**
    * Sets a <code>Binary</code> key set.
    *
    * @param entrySet the key set to set.
    */
   public final void setEntrySet(final HashMap<Binary, Binary> entrySet) { // NOPMD
      this.entrySet = new IntObjectHashMap<HashMap<Binary, Binary>>(1);
      this.entrySet.put(0, entrySet);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * <p/>
    * This implementation clears key set by setting it to null.
    */
   final void clear() {

      entrySet = null;
   }


   /**
    * {@inheritDoc}
    */
   public final void executeOperational() {

      Assert.assertFalse(isRootRequest(), "Request cannot be root");
      Assert.assertNotNull(entrySet, "Subrequest key set cannot be null");
      Assert.assertFalse(entrySet.isEmpty(), "Subrequest keys set cannot be empty");

      // Collect bucket for processing and collect rejects
      final CacheProcessor processor = getCacheProcessor();

      // Initialize rejected
      final IntHashSet rejectedBuckets = new IntHashSet(1);
      final LinkedList<BucketEntries> entriesToProcess = new LinkedList<BucketEntries>();
      final Integer storageNumber = getStorageNumber();

      entrySet.forEachEntry(new IntObjectProcedure<HashMap<Binary, Binary>>() {

         public boolean execute(final int bucketNumber, final HashMap<Binary, Binary> entriesToCheck) {  // NOPMD

            // Lock bucket for write
            final Bucket bucket = processor.getBucket(storageNumber, bucketNumber);
            if (!processor.isBucketOwner(storageNumber, bucketNumber) || bucket == null || bucket.isReconfiguring()) {

               rejectedBuckets.add(bucketNumber);
               return true;
            }


            // Add to locked list
            entriesToProcess.add(new BucketEntries(bucket, entriesToCheck));

            return true;
         }
      });

      // Process
      final ProcessingResult processingResult = processEntries(entriesToProcess);

      //
      // Create response and set result and rejected buckets
      //
      final AggregatingResponse response = (AggregatingResponse) createResponse(Response.RESULT_SUCCESS);
      response.setRejectedBuckets(rejectedBuckets);
      response.setResult(processingResult.getResult());

      //
      // Respond now if this is a replica because replicas don't need replication or cache invalidation.
      //
      if (isReplicaRequest() || !processingResult.hasModifiedEntries()) {

         respond(response);

         return;
      }

      //
      // Create front cache invalidation requests
      //
      final Collection<Request> subrequests = new ArrayList<Request>(processor.getBucketOwnerCount() + 1);

      // Collect bucket numbers that have unexpired leases
      final IntObjectHashMap<HashMap<Binary, Binary>> modifiedEntries = processingResult.getModifiedEntries();
      final IntArrayList bucketNumbersToInvalidate = new IntArrayList(modifiedEntries.size());
      modifiedEntries.forEachKey(new IntProcedure() {

         public boolean execute(final int bucketNumber) {

            final Bucket bucket = processor.getBucket(storageNumber, bucketNumber);
            if (bucket != null && hasUnexpiredLease(bucket)) {

               bucket.setLeaseExpirationTime(null);
               bucketNumbersToInvalidate.add(bucketNumber);
            }

            return true;
         }
      });

      // Add bucket invalidation announcement
      if (!CollectionUtils.isEmpty(bucketNumbersToInvalidate)) {

         subrequests.add(new ClearFrontCacheBucketAnnouncement(getCacheName(), bucketNumbersToInvalidate));
      }

      //
      // Create replica update subrequests
      //
      final int storageEnd = getCacheProcessor().getReplicaCount();
      for (int i = 1; i <= storageEnd; i++) {

         subrequests.addAll(splitEntrySet(i, processingResult.getModifiedEntries()));
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

         respond(response);
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation splits this request's <code>bucketSet</code> by an owner.
    */
   protected final Collection<? extends AggregatingRequest> split(final int storageNumber) {

      return splitEntrySet(storageNumber, entrySet);
   }


   /**
    * {@inheritDoc}
    */
   protected final Waiter createWaiter() {

      return new Waiter(this);
   }


   /**
    * Splits a set of keys on a per-owner basis at the given storage number to a set of objects extending
    * <code>KeySetRequest</code>. The creating of the request is done using a template method
    * <code>createRequest()</code>.
    *
    * @param storageNumber   owner's storage number.
    * @param entrySetToSplit the set of bucket numbers to split.
    * @return a collection of <code>KeySetRequests</code> with pre-set sender, storage number and receiver.
    * @see #createRequest()
    * @see EntrySetRequest
    */
   protected final Collection<EntrySetRequest> splitEntrySet(final int storageNumber,
                                                             final IntObjectHashMap<HashMap<Binary, Binary>> entrySetToSplit) {

      // Check if there is anything to handle
      if (entrySet == null || entrySet.isEmpty()) {
         return Collections.emptyList();
      }

      // Prepare result
      final HashMap<ClusterNodeAddress, EntrySetRequest> result = new HashMap<ClusterNodeAddress, EntrySetRequest>(1);
      final CacheProcessor cacheProcessor = getCacheProcessor();
      final ClusterNodeAddress nodeAddress = cacheProcessor.getAddress();

      // Iterate bucket set
      entrySetToSplit.forEachValue(new ObjectProcedure<HashMap<Binary, Binary>>() {

         public boolean execute(final HashMap<Binary, Binary> entries) { // NOPMD


            entries.forEachEntry(new ObjectObjectProcedure<Binary, Binary>() {

               public boolean execute(final Binary key, final Binary value) {

                  final int bucketNumber = cacheProcessor.getBucketNumber(key);
                  ClusterNodeAddress bucketOwner = cacheProcessor.getBucketOwner(storageNumber, bucketNumber);
                  if (bucketOwner == null) {

                     if (storageNumber == 0) {
                        // REVIEWME: simeshev@cacheonix.org - 2010-03-05 - Right now we post
                        // unassigned to self. This may create a send-receive-retry cycle
                        // while the ownership is stabilized. Consider a timed approach,
                        // maybe through a deferred request queue.
                        bucketOwner = nodeAddress;
                     } else {

                        // We don't care about unassigned replicas - continue
                        return true;
                     }
                  }

                  // Get/create request
                  EntrySetRequest partialRequest = result.get(bucketOwner);
                  if (partialRequest == null) {
                     partialRequest = createRequest();
                     partialRequest.setSender(nodeAddress);
                     partialRequest.setStorageNumber(storageNumber);
                     partialRequest.setReceiver(bucketOwner);
                     result.put(bucketOwner, partialRequest);
                  }

                  // Put the key to the bucket
                  // NOPMD
                  final IntObjectHashMap<HashMap<Binary, Binary>> partialEntrySet = partialRequest.getEntrySet();
                  HashMap<Binary, Binary> partialEntries = partialEntrySet.get(bucketNumber);
                  if (partialEntries == null) {
                     partialEntries = new HashMap<Binary, Binary>(1);
                     partialEntrySet.put(bucketNumber, partialEntries);
                  }
                  partialEntries.put(key, value);
                  return true;
               }
            });

            return true;
         }
      });

      return result.values();
   }


   /**
    * Processes entries according to the request purpose. This method should post a response if it is required or post
    * subrequests.
    *
    * @param entriesToProcess a list of locked buckets to process. <code>LockedBucketEntries</code> contains a bucket
    *                         and a set of keys guaranteed to belong to the bucket.
    * @return the result of processing.
    * @see #executeOperational()
    */
   protected abstract ProcessingResult processEntries(final List<BucketEntries> entriesToProcess);


   /**
    * A implementation of this factory method should return a new instance of a concrete implementation of
    * <code>KeySetRequest</code>.
    *
    * @return a new instance of a concrete implementation of <code>KeySetRequest</code>.
    */
   protected abstract EntrySetRequest createRequest();


   /**
    * {@inheritDoc}
    */
   protected final Object aggregate(final List<Response> partialResponses) {

      final Object[] resultAccumulator = {null};
      for (final Message partialResponse : partialResponses) {

         if (partialResponse instanceof CacheResponse) {
            final CacheResponse cacheResponse = (CacheResponse) partialResponse;
            final int resultCode = cacheResponse.getResultCode();
            final Object result = cacheResponse.getResult();
            switch (resultCode) {
               case CacheResponse.RESULT_ERROR:
                  return WaiterUtils.resultToThrowable(result);
               case CacheResponse.RESULT_INACCESSIBLE:
               case CacheResponse.RESULT_RETRY:
                  return createRetryException(cacheResponse);
               case CacheResponse.RESULT_SUCCESS:
                  aggregate(resultAccumulator, cacheResponse);
                  break;
               default:
                  return WaiterUtils.unknownResultToThrowable(resultCode, result);
            }
         }
      }

      return resultAccumulator[0];
   }


   protected abstract void aggregate(final Object[] resultAccumulator, final CacheResponse cacheResponse);


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      final int entrySetSize = in.readInt();
      entrySet = new IntObjectHashMap<HashMap<Binary, Binary>>(entrySetSize);
      for (int i = 0; i < entrySetSize; i++) {
         final int bucketNumber = in.readShort();
         final int entriesSize = in.readInt();
         final HashMap<Binary, Binary> entries = new HashMap<Binary, Binary>(entriesSize);
         for (int j = 0; j < entriesSize; j++) {
            entries.put(SerializerUtils.readBinary(in), SerializerUtils.readBinary(in));
         }
         entrySet.put(bucketNumber, entries);
      }
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeInt(getEntrySet().size());
      entrySet.forEachEntry(new IntObjectProcedure<HashMap<Binary, Binary>>() {

         public boolean execute(final int bucketNumber, final HashMap<Binary, Binary> entries) {  // NOPMD
            try {
               out.writeShort(bucketNumber);
               out.writeInt(entries.size());
            } catch (final IOException e) {
               throw new RuntimeIOException(e);
            }
            entries.forEachEntry(new ObjectObjectProcedure<Binary, Binary>() {

               public boolean execute(final Binary key, final Binary value) {

                  try {
                     SerializerUtils.writeBinary(out, key);
                     SerializerUtils.writeBinary(out, value);
                  } catch (final IOException e) {
                     throw new RuntimeIOException(e);
                  }
                  return true;
               }
            });
            return true;
         }
      });
   }


   public final boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof EntrySetRequest)) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final EntrySetRequest that = (EntrySetRequest) o;

      if (entrySet != null ? !entrySet.equals(that.entrySet) : that.entrySet != null) {
         return false;
      }

      return true;
   }


   public final int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (entrySet != null ? entrySet.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "EntrySetRequest{" +
              "entrySet.size()=" + (entrySet == null ? "null" : Integer.toString(entrySet.size())) +
              "} " + super.toString();
   }

   // ==================================================================================================================
   //
   // ProcessingResult
   //
   // ==================================================================================================================


   /**
    * The object holding result of the call to {@link EntrySetRequest#processEntries(List)}.
    */
   protected static final class ProcessingResult {

      /**
       * The result of execution.
       */
      private Object result = null;

      /**
       * Numbers of modified buckets or null or empty.
       */
      private IntObjectHashMap<HashMap<Binary, Binary>> modifiedEntries = null;


      /**
       * Creates new result.
       *
       * @param result          the result of execution.
       * @param modifiedEntries a set of modified buckets.
       */
      public ProcessingResult(final Object result, final IntObjectHashMap<HashMap<Binary, Binary>> modifiedEntries) {

         this.result = result;
         this.modifiedEntries = modifiedEntries;
      }


      /**
       * Returns true if has a non-null, non-empty set of modified bucket numbers.
       *
       * @return true if has a non-null, non-empty set of modified bucket numbers.
       */
      public boolean hasModifiedEntries() {

         return modifiedEntries != null && !modifiedEntries.isEmpty();
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
      public IntObjectHashMap<HashMap<Binary, Binary>> getModifiedEntries() {

         return modifiedEntries;
      }


      public String toString() {

         return "Result{" +
                 "result=" + result +
                 ", modifiedBuckets=" + StringUtils.sizeToString(modifiedEntries) +
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
   @SuppressWarnings("ClassNameSameAsAncestorName")
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

            // Remove rejected buckets from the request
            final EntrySetRequest entrySetRequest = (EntrySetRequest) request;
            final boolean sizeIsSame = entrySetRequest.getEntrySet().size() == rejectedBuckets.size();
            final boolean rbsModified = entrySetRequest.getEntrySet().retainEntries(new IntObjectProcedure<HashMap<Binary, Binary>>() {

               public boolean execute(final int bucketNumber,
                                      final HashMap<Binary, Binary> entries) {  // NOPMD
                  return rejectedBuckets.contains(bucketNumber);
               }
            });
            Assert.assertTrue(rbsModified || sizeIsSame, "Request bucket set should have had rejected buckets");
         } else {

            // All entries were processed, no rejected buckets
            request.clear();
         }


         // REVIEWME: simeshev@viewtier.com - 2010-05-13 - Right now we add all responses w/o considering if they
         // are going to be aggregated (for instance, ClearRequest only cares about errors). This may lead for
         // responses hanging around for longer than they should instead of being GC-ed immediately.

         // Add response only if this is a request to a primary owner
         if (request.isPrimaryRequest()) {

            ownerWaiter.getPartialResponses().add(response);
         }
      }
   }
}