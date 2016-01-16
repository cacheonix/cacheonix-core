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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cacheonix.impl.RuntimeIOException;
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
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntArrayList;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.IntObjectProcedure;
import org.cacheonix.impl.util.array.IntProcedure;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * KeySetRequest is sent to owners of a set of keys.
 * <p/>
 * KeySetRequest completes when all owners respond.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 22, 2010 2:29:40 PM
 */
@SuppressWarnings("RedundantIfStatement")
public abstract class KeySetRequest extends AggregatingRequest {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(KeySetRequest.class); // NOPMD

   /**
    * Set of keys to process placed in buckets. At the root they are assigned to bucket 0 because all root needs is a
    * storage and becuase <code>splitKeySet()</code> operates on keys and ignores bucket numbers. Bucket numbers are
    * used at <code>notifyFinished()</code> when retaining processed keys.
    * <p/>
    * Int key is a bucket number and the hash set is keys in the bucket.
    */
   private IntObjectHashMap<HashSet<Binary>> keySet = null;


   /**
    * Default constructor to support Wireable.
    */
   KeySetRequest() {

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
   KeySetRequest(final int wireableType, final String cacheName, final boolean readRequest) {

      super(wireableType, cacheName, readRequest);
   }


   /**
    * Sets a <code>Binary</code> key set.
    *
    * @param keySet the key set to set.
    */
   public final void setKeySet(final IntObjectHashMap<HashSet<Binary>> keySet) { // NOPMD

      this.keySet = keySet;
   }


   /**
    * Returns a key set or initializes and returns a new one.
    *
    * @return the key set
    */
   @SuppressWarnings("ReturnOfCollectionOrArrayField")
   final IntObjectHashMap<HashSet<Binary>> getKeySet() {

      if (keySet == null) {
         keySet = new IntObjectHashMap<HashSet<Binary>>(1);
      }
      return keySet;
   }


   /**
    * Calculates a total number of keys stored in the key set.
    *
    * @return the total number of keys stored in the key set.
    */
   final int getKeysSize() {

      final int[] size = {0};

      final IntObjectHashMap<HashSet<Binary>> hashSetIntObjectHashMap = getKeySet();
      hashSetIntObjectHashMap.forEachValue(new ObjectProcedure<HashSet<Binary>>() {

         public boolean execute(final HashSet<Binary> keys) { // NOPMD
            size[0] += keys.size();
            return true;
         }
      });
      return size[0];
   }


   /**
    * {@inheritDoc}
    */
   public final void executeOperational() {

      Assert.assertFalse(isRootRequest(), "Request cannot be root");
      Assert.assertNotNull(keySet, "Subrequest key set cannot be null");
      Assert.assertFalse(keySet.isEmpty(), "Subrequest keys set cannot be empty");

      // Prepare buckets to process and collect rejects
      final CacheProcessor processor = getCacheProcessor();

      // Initialize rejected
      final IntHashSet rejectedBuckets = new IntHashSet(1);
      final List<BucketKeys> keysToProcess = new LinkedList<BucketKeys>();
      final Integer storageNumber = getStorageNumber();


      // Collect keys to process and collect rejects
      keySet.forEachEntry(new IntObjectProcedure<HashSet<Binary>>() {

         @SuppressWarnings("AutoUnboxing")
         public boolean execute(final int bucketNumber, final HashSet<Binary> keysToCheck) {  // NOPMD

            // Lock bucket for write
            final Bucket bucket = processor.getBucket(storageNumber, bucketNumber);
            if (!processor.isBucketOwner(storageNumber, bucketNumber) || bucket == null || bucket.isReconfiguring()) {

               rejectedBuckets.add(bucketNumber);
               return true;
            }

            // Add to locked list
            keysToProcess.add(new BucketKeys(bucket, keysToCheck));

            return true;
         }
      });

      // Process
      final ProcessingResult processingResult = processKeys(keysToProcess);

      //
      // Create response and set result and rejected buckets
      //
      final AggregatingResponse response = (AggregatingResponse) createResponse(Response.RESULT_SUCCESS);
      response.setRejectedBuckets(rejectedBuckets);
      response.setResult(processingResult.getResult());

      //
      // Respond now if this is a replica because replicas don't need replication or cache invalidation.
      //
      if (isReplicaRequest() || !processingResult.hasModifiedKeys()) {

         respond(response);

         return;
      }

      //
      // Create front cache invalidation requests
      //
      final Collection<Request> subrequests = new ArrayList<Request>(processor.getBucketOwnerCount() + 1);

      // Collect buckets that have unexpired leases
      final IntObjectHashMap<HashSet<Binary>> modifiedKeys = processingResult.getModifiedKeys();
      final IntArrayList bucketNumbersToInvalidate = new IntArrayList(modifiedKeys.size());
      modifiedKeys.forEachKey(new IntProcedure() {

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

         subrequests.addAll(splitKeySet(i, processingResult.getModifiedKeys()));
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

      return splitKeySet(storageNumber, keySet);
   }


   /**
    * Splits a set of keys on a per-owner basis at the given storage number to a set of objects extending
    * <code>KeySetRequest</code>. The creating of the request is done using a template method
    * <code>createRequest()</code>.
    *
    * @param storageNumber owner's storage number.
    * @param keySetToSplit the set of bucket numbers to split.
    * @return a collection of <code>KeySetRequests</code> with pre-set sender, storage number and receiver.
    * @see #createRequest()
    * @see KeySetRequest
    */
   private Collection<KeySetRequest> splitKeySet(final int storageNumber,
           final IntObjectHashMap<HashSet<Binary>> keySetToSplit) {

      // Return generated requests
      // Check if there is anything to handle
      if (keySetToSplit == null || keySetToSplit.isEmpty()) {
         return Collections.emptyList();
      }

      // Prepare result
      final Map<ClusterNodeAddress, KeySetRequest> result = new HashMap<ClusterNodeAddress, KeySetRequest>(1);
      final CacheProcessor cacheProcessor = getCacheProcessor();
      final ClusterNodeAddress nodeAddress = cacheProcessor.getAddress();

      // Iterate bucket set
      keySetToSplit.forEachValue(new ObjectProcedure<HashSet<Binary>>() {

         public boolean execute(final HashSet<Binary> keys) { // NOPMD

            // Iterate keys in the bucket
            keys.forEach(new ObjectProcedure<Binary>() {

               @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
               public boolean execute(final Binary key) {

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

                  // Get/create partial request
                  KeySetRequest partialRequest = result.get(bucketOwner);
                  if (partialRequest == null) {

                     partialRequest = createRequest();
                     partialRequest.setSender(nodeAddress);
                     partialRequest.setStorageNumber(storageNumber);
                     partialRequest.setReceiver(bucketOwner);
                     result.put(bucketOwner, partialRequest);
                  }

                  // Put the key to the bucket in the request
                  final IntObjectHashMap<HashSet<Binary>> partialKeySet = partialRequest.getKeySet();
                  HashSet<Binary> partialKeys = partialKeySet.get(bucketNumber);
                  if (partialKeys == null) {
                     partialKeys = new HashSet<Binary>(1);
                     partialKeySet.put(bucketNumber, partialKeys);
                  }
                  partialKeys.add(key);

                  // Continue
                  return true;
               }
            });


            // Continue
            return true;

         }
      });
      return result.values();
   }


   /**
    * Processes keys according to the request purpose. This method should post a response if it is required or post
    * subrequests.
    *
    * @param keysToProcess a list of locked buckets to process. <code>BucketKeys</code> contains a bucket and a set of
    *                      keys guaranteed to belong to the bucket.
    * @return the result of processing.
    * @see #executeOperational()
    */
   protected abstract ProcessingResult processKeys(final List<BucketKeys> keysToProcess);


   /**
    * A implementation of this factory method should return a new instance of a concrete implementation of
    * <code>KeySetRequest</code>.
    *
    * @return a new instance of a concrete implementation of <code>KeySetRequest</code>.
    */
   protected abstract KeySetRequest createRequest();


   /**
    * {@inheritDoc}
    */
   protected final Object aggregate(final List<Response> partialResponses) {

      final Object[] resultAccumulator = createResultAccumulator();
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


   /**
    * Creates a result accumulator that type is understood by the implementation of {@link #aggregate(Object[],
    * CacheResponse)}.
    *
    * @return a result accumulator that type is understood by the implementation of {@link #aggregate(Object[],
    *         CacheResponse)}.
    */
   protected abstract Object[] createResultAccumulator();


   /**
    * Aggregates successful responses into a result accumulator.
    *
    * @param resultAccumulator a result accumulator created by the implementation of {@link
    *                          #createResultAccumulator()}.
    * @param cacheResponse     a successful cache response.
    */
   protected abstract void aggregate(final Object[] resultAccumulator, final CacheResponse cacheResponse);


   final void clear() {

      keySet = null;
   }


   /**
    * {@inheritDoc}
    */
   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      final int keySetSize = in.readInt();
      keySet = new IntObjectHashMap<HashSet<Binary>>(keySetSize);
      for (int i = 0; i < keySetSize; i++) {
         final int bucketNumber = in.readShort();
         final int keysSize = in.readInt();
         final HashSet<Binary> keys = new HashSet<Binary>(keysSize);
         for (int j = 0; j < keysSize; j++) {
            keys.add(SerializerUtils.readBinary(in));
         }
         keySet.put(bucketNumber, keys);
      }
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeInt(getKeySet().size());
      keySet.forEachEntry(new IntObjectProcedure<HashSet<Binary>>() {

         public boolean execute(final int bucketNumber, final HashSet<Binary> keys) {  // NOPMD
            try {
               out.writeShort(bucketNumber);
               out.writeInt(keys.size());
            } catch (final IOException e) {
               throw new RuntimeIOException(e);
            }
            keys.forEach(new ObjectProcedure<Binary>() {

               public boolean execute(final Binary key) {

                  try {
                     SerializerUtils.writeBinary(out, key);
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
      if (!(o instanceof KeySetRequest)) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final KeySetRequest that = (KeySetRequest) o;

      if (keySet != null ? !keySet.equals(that.keySet) : that.keySet != null) {
         return false;
      }

      return true;
   }


   public final int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (keySet != null ? keySet.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "KeySetRequest{" +
              "keysSet.size()=" + (keySet == null ? "null" : Integer.toString(keySet.size())) +
              "} " + super.toString();
   }

   // ==================================================================================================================
   //
   // ProcessingResult
   //
   // ==================================================================================================================


   /**
    * The object holding result of the call to {@link KeySetRequest#processKeys(List)}.
    */
   protected static final class ProcessingResult {

      /**
       * The result of execution.
       */
      private Object result = null;

      /**
       * Numbers of modified buckets or null or empty.
       */
      private IntObjectHashMap<HashSet<Binary>> modifiedKeys = null;


      /**
       * Creates new result.
       *
       * @param result       the result of execution.
       * @param modifiedKeys a set of modified buckets.
       */
      public ProcessingResult(final Object result, final IntObjectHashMap<HashSet<Binary>> modifiedKeys) {

         this.result = result;
         this.modifiedKeys = modifiedKeys;
      }


      /**
       * Returns true if has a non-null, non-empty set of modified bucket numbers.
       *
       * @return true if has a non-null, non-empty set of modified bucket numbers.
       */
      public boolean hasModifiedKeys() {

         return modifiedKeys != null && !modifiedKeys.isEmpty();
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
      public IntObjectHashMap<HashSet<Binary>> getModifiedKeys() {

         return modifiedKeys;
      }


      public String toString() {

         return "Result{" +
                 "result=" + result +
                 ", modifiedBuckets=" + StringUtils.sizeToString(modifiedKeys) +
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
   static class Waiter extends AggregatingRequest.Waiter {

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

            // Some buckets were rejected, remove rejected buckets from the request
            final KeySetRequest keySetRequest = (KeySetRequest) request;
            final boolean sizeIsSame = keySetRequest.getKeySet().size() == rejectedBuckets.size();
            final boolean rbsModified = keySetRequest.getKeySet().retainEntries(new IntObjectProcedure<HashSet<Binary>>() {

               public boolean execute(final int bucketNumber, final HashSet<Binary> keys) {  // NOPMD
                  return rejectedBuckets.contains(bucketNumber);
               }
            });
            Assert.assertTrue(rbsModified || sizeIsSame, "Request bucket set should have had rejected buckets");
         } else {

            // All keys were processed, no rejected buckets
            request.clear();
         }


         // REVIEWME: simeshev@cacheonix.org - 2010-05-13 - Right now we add all responses w/o considering if they
         // are going to be aggregated (for instance, ClearRequest only cares about errors). This may lead for
         // responses hanging around for longer than they should instead of being GC-ed immediately.

         // Add response only if this is a request to a primary owner
         if (request.isPrimaryRequest()) {
            ownerWaiter.getPartialResponses().add(response);
         }
      }
   }
}