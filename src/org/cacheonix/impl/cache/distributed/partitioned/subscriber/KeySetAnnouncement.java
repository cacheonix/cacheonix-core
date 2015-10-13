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
package org.cacheonix.impl.cache.distributed.partitioned.subscriber;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cacheonix.exceptions.RuntimeIOException;
import org.cacheonix.impl.cache.distributed.partitioned.BucketIndexCalculator;
import org.cacheonix.impl.cache.distributed.partitioned.BucketOwner;
import org.cacheonix.impl.cache.distributed.partitioned.BucketOwnershipAssignment;
import org.cacheonix.impl.cache.distributed.partitioned.CacheResponse;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.IntObjectProcedure;
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
@SuppressWarnings({"RedundantIfStatement", "WeakerAccess", "WeakerAccess"})
public abstract class KeySetAnnouncement extends AggregatingAnnouncement {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(KeySetAnnouncement.class); // NOPMD

   private final BucketIndexCalculator bucketCalculator = new BucketIndexCalculator(ConfigurationConstants.BUCKET_COUNT);

   /**
    * Set of keys to process placed in buckets. At the root they are assigned to bucket 0 because all root needs is a
    * storage and becuase <code>splitKeySet()</code> operates on keys and ignores bucket numbers. Bucket numbers are
    * used at <code>notifyFinished()</code> when retaining processed keys.
    * <p/>
    * Int key is a bucket number and the hash set is keys in the bucket.
    */
   private IntObjectHashMap<HashSet<Binary>> keySet = null;

   /**
    * A bucket set owner.
    */
   private ClusterNodeAddress bucketOwnerAddress = null;


   /**
    * Default constructor to support Wireable.
    */
   protected KeySetAnnouncement() {

   }


   /**
    * Abstract class constructor.
    *
    * @param wireableType wireable type.
    * @param cacheName    cache name.
    */
   protected KeySetAnnouncement(final int wireableType, final String cacheName) {

      super(wireableType, cacheName);
   }


   /**
    * Sets a <code>Binary</code> key set.
    *
    * @param keySet the key set to set.
    */
   public final void setKeySet(final HashSet<Binary> keySet) { // NOPMD

      this.keySet = new IntObjectHashMap<HashSet<Binary>>(1);
      this.keySet.put(0, keySet);
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


   public void setBucketOwnerAddress(final ClusterNodeAddress bucketOwnerAddress) {

      this.bucketOwnerAddress = bucketOwnerAddress;
   }


   protected ClusterNodeAddress getBucketOwnerAddress() {

      return bucketOwnerAddress;
   }


   /**
    * Calculates a total number of keys stored in the key set.
    *
    * @return the total number of keys stored in the key set.
    */
   protected final int getKeysSize() {

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
   public void execute() throws InterruptedException {

      Assert.assertFalse(isRootRequest(), "Request cannot be root");
      Assert.assertNotNull(keySet, "Subrequest key set cannot be null");
      Assert.assertFalse(keySet.isEmpty(), "Subrequest keys set cannot be empty");


      // We are at (every) cluster processor now

      // We have only keys for a single cache processor [address] because keys were split

      // We can be at any node

      // The current node may or may not own the keys we got

      // Buckets can be in reconfiguring state (or in generally unmodifiable state)

      // For keys in buckets in reconfiguring state send response rejected/retry

      // If a key does not belong to the bucket, send reject/retry

      // For keys in OK buckets add subscribers to the replicated subscriber list
      //       and post a subscription message to the local cache processor


      // Prepare
      final ClusterProcessor processor = getClusterProcessor();
      final Integer storageNumber = getStorageNumber();

      final ReplicatedState replicatedState = processor.getProcessorState().getReplicatedState();
      final Group group = replicatedState.getGroup(Group.GROUP_TYPE_CACHE, getCacheName());
      final BucketOwnershipAssignment bucketOwnershipAssignment = group.getBucketOwnershipAssignment();

      // Init keys ready to go
      final IntObjectHashMap<HashSet<Binary>> keysToProcess = new IntObjectHashMap<HashSet<Binary>>(keySet.size());

      // Init rejected
      final IntHashSet rejectedBuckets = new IntHashSet(1);

      // Collected rejected buckets and keys to process
      keySet.forEachEntry(new IntObjectProcedure<HashSet<Binary>>() {

         public boolean execute(final int bucketNumber, final HashSet<Binary> keysToCheck) {  // NOPMD

            // Check if bucket has an owner
            final BucketOwner bucketOwner = bucketOwnershipAssignment.getBucketOwner(storageNumber, bucketNumber);
            if (bucketOwner == null) {

               rejectedBuckets.add(bucketNumber);

               // Continue
               return true;
            }


            // Check if the bucket still belongs to this node - add to rejected otherwise
            final ClusterNodeAddress actualBucketOwnerAddress = bucketOwner.getAddress();
            if (!bucketOwnerAddress.equals(actualBucketOwnerAddress)) {

               // Ownership changed, reject
               if (storageNumber == 0) {

                  rejectedBuckets.add(bucketNumber);
               }

               // Continue
               return true;
            }

            // Check if bucket reconfiguring
            if (bucketOwner.getOutboundTransfer(bucketNumber) != null) {

               rejectedBuckets.add(bucketNumber);

               // Continue
               return true;
            }

            // Add to keys to process
            HashSet<Binary> keys = keysToProcess.get(bucketNumber);
            if (keys == null) {
               keys = new HashSet<Binary>(1);
               keysToProcess.put(bucketNumber, keys);
            }
            keys.addAll(keysToCheck);

            // Continue
            return true;
         }
      });

      // Process
      processKeys(storageNumber, rejectedBuckets, keysToProcess);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation splits this request's <code>bucketSet</code> by an owner.
    */
   protected final Collection<? extends AggregatingAnnouncement> split(final int storageNumber) {

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
    * @see #createAnnouncement()
    * @see KeySetAnnouncement
    */
   protected final Collection<KeySetAnnouncement> splitKeySet(final int storageNumber,
                                                              final IntObjectHashMap<HashSet<Binary>> keySetToSplit) {

      // Return generated requests
      // Check if there is anything to handle
      if (keySetToSplit == null || keySetToSplit.isEmpty()) {
         return Collections.emptyList();
      }

      // Prepare
      final Map<ClusterNodeAddress, KeySetAnnouncement> result = new HashMap<ClusterNodeAddress, KeySetAnnouncement>(1);
      final ClusterProcessor processor = getClusterProcessor();
      final ClusterNodeAddress nodeAddress = processor.getAddress();

      final ReplicatedState replicatedState = processor.getProcessorState().getReplicatedState();
      final Group group = replicatedState.getGroup(Group.GROUP_TYPE_CACHE, getCacheName());
      final BucketOwnershipAssignment bucketOwnershipAssignment = group.getBucketOwnershipAssignment();

      // Iterate bucket set
      keySetToSplit.forEachValue(new ObjectProcedure<HashSet<Binary>>() {

         public boolean execute(final HashSet<Binary> keys) { // NOPMD

            // Iterate keys in the bucket
            keys.forEach(new ObjectProcedure<Binary>() {

               @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
               public boolean execute(final Binary key) {

                  final int bucketNumber = bucketCalculator.calculateBucketIndex(key);
                  ClusterNodeAddress bucketOwnerAddress = bucketOwnershipAssignment.getBucketOwnerAddress(storageNumber, bucketNumber);

                  if (bucketOwnerAddress == null) {

                     if (storageNumber == 0) {

                        // REVIEWME: simeshev@cacheonix.org - 2010-03-05 - Right now we post
                        // unassigned to self. This may create a send-receive-retry cycle
                        // while the ownership is stabilized. Consider a timed approach,
                        // maybe through a deferred request queue.
                        bucketOwnerAddress = nodeAddress;
                     } else {

                        // We don't care about unassigned replicas - continue
                        return true;
                     }
                  }

                  // Get/create partial request
                  KeySetAnnouncement partialRequest = result.get(bucketOwnerAddress);
                  if (partialRequest == null) {

                     partialRequest = createAnnouncement();
                     partialRequest.setSender(nodeAddress);
                     partialRequest.setStorageNumber(storageNumber);
                     partialRequest.setBucketOwnerAddress(bucketOwnerAddress);
                     result.put(bucketOwnerAddress, partialRequest);
                  }

                  // Put the key to the bucket in the request
                  HashSet<Binary> partialKeys = partialRequest.getKeySet().get(bucketNumber);
                  if (partialKeys == null) {

                     partialKeys = new HashSet<Binary>(1);
                     partialRequest.getKeySet().put(bucketNumber, partialKeys);
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
    * @param storageNumber   a storage number.
    * @param rejectedBuckets buckets that cannot be processed by the bucketOwner
    * @param keysToProcess   a list of locked buckets to process. <code>LockedBucket</code> contains a bucket and a set
    */
   protected abstract void processKeys(final Integer storageNumber, final IntHashSet rejectedBuckets,
                                       final IntObjectHashMap<HashSet<Binary>> keysToProcess);


   /**
    * A implementation of this factory method should return a new instance of a concrete implementation of
    * <code>KeySetRequest</code>.
    *
    * @return a new instance of a concrete implementation of <code>KeySetRequest</code>.
    */
   protected abstract KeySetAnnouncement createAnnouncement();


   /**
    * {@inheritDoc}
    */
   protected final Object aggregate(final List<Response> partialResponses) {

      final Object[] resultAccumulator = createResultAccumulator();
      for (final Message partialResponse : partialResponses) {

         if (partialResponse instanceof AggregatingAnnouncementResponse) {

            final AggregatingAnnouncementResponse response = (AggregatingAnnouncementResponse) partialResponse;
            final int resultCode = response.getResultCode();
            final Object result = response.getResult();
            switch (resultCode) {

               case CacheResponse.RESULT_ERROR:

                  return WaiterUtils.resultToThrowable(result);

               case CacheResponse.RESULT_INACCESSIBLE:

               case CacheResponse.RESULT_RETRY:

                  return createRetryException(response);

               case CacheResponse.RESULT_SUCCESS:

                  aggregate(resultAccumulator, response);

                  break;
               default:
                  return WaiterUtils.unknownResultToThrowable(resultCode, result);
            }
         }
      }

      return resultAccumulator[0];
   }


   /**
    * Creates a result accumulator as mutable object that type is understood by the implementation of {@link
    * #aggregate(Object[], AggregatingAnnouncementResponse)}. The array is used to support mutability of a parameter
    * supplied to <code>aggregate()</code>
    *
    * @return a result accumulator that type is understood by the implementation of {@link #aggregate(Object[],
    *         AggregatingAnnouncementResponse)}.
    * @see #aggregate(Object[], AggregatingAnnouncementResponse)
    */
   protected abstract Object[] createResultAccumulator();


   /**
    * Aggregates successful responses into a result accumulator.
    *
    * @param resultAccumulator a result accumulator created by the implementation of {@link
    *                          #createResultAccumulator()}.
    * @param response          a successful response to aggregate.
    */
   protected abstract void aggregate(final Object[] resultAccumulator, final AggregatingAnnouncementResponse response);


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

      bucketOwnerAddress = SerializerUtils.readAddress(in);

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

      SerializerUtils.writeAddress(bucketOwnerAddress, out);

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


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof KeySetAnnouncement)) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final KeySetAnnouncement that = (KeySetAnnouncement) o;

      if (keySet != null ? !keySet.equals(that.keySet) : that.keySet != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

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
   // Waiter
   //
   // ==================================================================================================================


   /**
    * Waiter for the <code>BucketSetResponse</code>.
    */
   @SuppressWarnings("ClassNameSameAsAncestorName")
   static class Waiter extends AggregatingAnnouncement.Waiter {


      private int responseCount = 0;


      /**
       * Creates waiter.
       *
       * @param request request UUID
       */
      Waiter(final Request request) {

         super(request);
      }


      /**
       * Processes response. The response is going to come in the following distinct cases:
       * <p/>
       * a) Response from primary owner server to primary owner client (at root)
       * <p/>
       * b) Response from (backup) owner server to back up owner client (at primary owner)
       * <p/>
       * c) RESULT_INACCESSIBLE - if the destination node could not be reached.
       * <p/>
       * d) RESULT_ERROR - if unrecoverable error occurred at the destination
       * <p/>
       * e) RESULT_RETRY - if the server was undergoing re-configuration
       *
       * @param message the response
       * @throws InterruptedException
       */
      public final void notifyResponseReceived(final Response message) throws InterruptedException {

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Response: " + message);

         // Remove processed buckets so that notifyFinished() re-posts only those left
         final KeySetAnnouncement request = (KeySetAnnouncement) getRequest();
         if (message instanceof AggregatingAnnouncementResponse) {

            final AggregatingAnnouncementResponse response = (AggregatingAnnouncementResponse) message;

            Assert.assertTrue(!request.isRootRequest() || response.getResultCode() == Response.RESULT_RETRY, "Root's notifyResponseReceived() should never be called except when it is a Retry response: {0}", message);
            Assert.assertTrue(getOwnerWaiter() != null || (request.isRootRequest() && response.getResultCode() == Response.RESULT_RETRY), "Parent's notifyResponseReceived() should never be called: {0}", message);

            switch (response.getResultCode()) {

               // Partial response received, some or all buckets might have been rejected, some may have been processed
               case Response.RESULT_SUCCESS:

                  // Some or all buckets were processed, retain only unprocessed buckets
                  final int[] processedBuckets = response.handOffProcessedBuckets();
                  if (processedBuckets != null && processedBuckets.length > 0) {

                     for (final int processedBucketNumber : processedBuckets) {

                        request.getKeySet().remove(processedBucketNumber);
                     }
                  }

                  // Increment response count, must receive two, one with processed buckets, and one from bucketOwner
                  responseCount++;

                  if (responseCount >= 2) {

                     //noinspection ControlFlowStatementWithoutBraces
                     if (LOG.isDebugEnabled()) LOG.debug("Response count reached : " + responseCount); // NOPMD

                     // NOTE: simeshev@cacheonix.org - 2011-02-19 - Call to default implementation initiates finish.
                     super.notifyResponseReceived(message);
                  }

                  break;

               // Unrecoverable error occurred at the destination
               case Response.RESULT_ERROR:

                  // Clear the unprocessed entries because there is no point
                  // in trying. The requester should receive an error in response.
                  request.clear();

                  // Always add errors to partial response, i.e. regardless
                  // of primary or replica response.
                  getOwnerWaiter().getPartialResponses().add(response);

                  // NOTE: simeshev@cacheonix.org - 2011-02-19 - Call to default implementation initiates finish.
                  // We call it from a condition block because some paths must wait for more responses.
                  super.notifyResponseReceived(message);

                  break;

               case Response.RESULT_INACCESSIBLE:
               case Response.RESULT_RETRY:

                  // Means that reconfiguration is in process. All entries
                  // belonging to this request must be re-submitted. Re-submission
                  // is done in notifyFinished(). All we need to do is do nothing,
                  // AggregatingRequest's notifyFinished() will re-submit
                  // all entries left in the request.

                  if (request.isRootRequest()) {

                     // NOTE: simeshev@cacheonix.org - 2010-12-26 - It is possible for a root request
                     // to receive a Response.RESULT_RETRY if case the root request is submitted when
                     // local cache processor is not there yet or it is already not there as a result of
                     // reset or shutdown (see ClusterProcessor's dispatch() and enqueue()).
                     //
                     // We need to handle this situation by adding the retry response to the list of
                     // partial responses. That list is normally populated by requests to primary owners,
                     // but in this case we simulate a response.
                     //
                     // The root request's notifyFinished() should be prepared to deal with the situation
                     // when Response.RESULT_RETRY is in the partial response list. As of this writing,
                     // nothing special needs to be done to notifyFinished() that now sits in
                     // AggregatingRequest.
                     //
                     // See CACHEONIX-217 for more information.


                     final AggregatingAnnouncement.Waiter rootWaiter = (AggregatingAnnouncement.Waiter) request.getWaiter();
                     final List<Response> rootPartialResponses = rootWaiter.getPartialResponses();
                     rootPartialResponses.add(message);
                  }

                  // NOTE: simeshev@cacheonix.org - 2011-02-19 - Default implementation initiates finish.
                  // We call it from a condition block because some paths must wait for more responses.
                  super.notifyResponseReceived(message);

                  break;

               default:

                  // Response code is unknown
                  final Response unexpectedResponseError = createResponseCodeError(response);
                  getOwnerWaiter().getPartialResponses().add(unexpectedResponseError);

                  // NOTE: simeshev@cacheonix.org - 2011-02-19 - Call to default implementation initiates finish.
                  // We call it from a condition block because some paths must wait for more responses.
                  super.notifyResponseReceived(message);

                  break;
            }
         } else {

            // This is a rare but possible situation when the error response
            // had to be created before the requests could be obtained. As it
            // cannot be obtained, it cannot be asked to create a proper type
            // response using its createResponse() method. So, this is never
            // a success response.

            // Clear the unprocessed entries because there is no point
            // in retrying. The requester should receive an error in response.
            request.clear();

            // Always add errors to partial response, i.e. regardless
            // of primary or replica response.
            final Response unexpectedResponseError = createUnexpectedResponseError(message);
            getOwnerWaiter().getPartialResponses().add(unexpectedResponseError);

            // NOTE: simeshev@cacheonix.org - 2011-02-19 - Call to default implementation initiates finish.
            // We call it from a condition block because some paths must wait for more responses.
            super.notifyResponseReceived(message);
         }
      }


      /**
       * {@inheritDoc}
       */
      public void notifyNodeLeft(final ClusterNodeAddress leftAddress) {

         // Guard
         if (isFinished()) {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("Finished, returning");

            return;
         }


         final KeySetAnnouncement announcement = (KeySetAnnouncement) getRequest();
         if (leftAddress.equals(announcement.bucketOwnerAddress)) {

            // NOTE: simeshev@cacheonix.org - 2011-02-21 - When the bucket owner leaves,
            // it means that there is no point in waiting for the response from bucket
            // owner. Reminder: we use a response from bucket owner to delay returning
            // to the client. It does not affect handling rejects. We just increment
            // the response count and, optionally, finish if it reached 2.

            if (responseCount < 2) {

               responseCount++;
            }

            if (responseCount >= 2) {

               // All expected responses received
               finish();
            }
         }
      }


      /**
       * Creates a error response.
       *
       * @param response the response.
       * @return the error response.
       */
      protected final AggregatingAnnouncementResponse createResponseCodeError(final Response response) {

         final AggregatingAnnouncementResponse result = (AggregatingAnnouncementResponse) getRequest().createResponse(Response.RESULT_ERROR);
         final String errorMessage = "Unexpected response result code " + response.getResultCode() + ", response: " + response;
         result.setResult(errorMessage);
         return result;
      }


      /**
       * Creates a error response.
       *
       * @param response the response.
       * @return the error response.
       */
      protected final AggregatingAnnouncementResponse createUnexpectedResponseError(final Response response) {

         final AggregatingAnnouncementResponse result = (AggregatingAnnouncementResponse) getRequest().createResponse(Response.RESULT_ERROR);
         final String errorMessage = "Unexpected response: " + response;
         result.setResult(errorMessage);
         return result;
      }
   }
}