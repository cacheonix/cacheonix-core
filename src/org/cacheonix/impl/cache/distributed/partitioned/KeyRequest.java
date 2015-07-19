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
import java.util.List;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.configuration.ExpirationConfiguration;
import org.cacheonix.impl.configuration.FrontCacheConfiguration;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.PrepareResult;
import org.cacheonix.impl.net.processor.Prepareable;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.RequestProcessor;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Provides an abstract implementation of request-sub-request pattern.
 * <p/>
 * <code>KeyRequest</code> is a foundation for a category of cache requests that require processing of
 * <b>key-addressed</b> data at nodes that own the keys. <code>KeyRequest</code> encapsulates common actions and
 * delegates the implementation of actions specific to a particular request. In this regard <code>KeyRequest</code>  is
 * an implementation of the template method pattern.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public abstract class KeyRequest extends CacheDataRequest implements Prepareable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(KeyRequest.class); // NOPMD

   /**
    * True if the request was prepared.
    */
   private boolean prepared = false;


   /**
    * Storage number to that the request is addressed. The storage number can be zero, which means that this is a
    * request to a primary bucket owner. It can be between one and the number of replicas which means that this is a
    * request to a replica owner.
    */
   private int storageNumber = 0;


   /**
    * Request key.
    */
   private Binary key = null;


   /**
    * Bucket number for the key.
    */
   private Integer bucketNumber = null;

   /**
    * If true, a bucket that is reconfiguring will be locked. Otherwise, that bucket that is reconfiguring will be
    * rejected. This flag is useful for read operations.
    */
   private boolean lockReconfiguringBucket = false;


   /**
    * Required to support <code>Wireable</code>.
    */
   protected KeyRequest() {

   }


   /**
    * Creates an AggregatingCacheRequest using given wireable type and cache name.
    *
    * @param wireableType            unique wireable type. The wireable type should have {@link
    *                                Wireable#DESTINATION_CACHE_PROCESSOR}.
    * @param cacheName               cache name
    * @param lockReconfiguringBucket flag to lock reconfiguring buckets. If true, a bucket that is reconfiguring will be
    *                                locked. Otherwise, that bucket that is reconfiguring will be rejected. This flag is
    *                                useful for read operations.
    * @param readRequest             a flag indicating if this is a read request. If the flag is set to
    *                                <code>false</code>, this is a write request. Read or write type determines if the
    *                                request checks out a bucket for read or for write. Read requests extend the read
    *                                lease time if the willCacheUntil is set and if there are no pending write requests
    *                                for the bucket.
    */
   protected KeyRequest(final int wireableType, final String cacheName, final boolean lockReconfiguringBucket,
                        final boolean readRequest) {

      super(wireableType, cacheName, readRequest);
      this.lockReconfiguringBucket = lockReconfiguringBucket;
   }


   /**
    * Storage number to that the request is addressed.
    *
    * @return Storage number to that the request is addressed. The storage number can be zero, which means that this is
    *         a request to a primary bucket owner. It can be between one and the number of replicas which means that
    *         this is a request to a replica owner.
    */
   public int getStorageNumber() {

      return storageNumber;
   }


   public final void setKey(final Binary key) {

      this.key = key;
   }


   public final Binary getKey() {

      return key;
   }


   /**
    * Returns a bucket number for the request key.
    *
    * @return the bucket number for the request key. The bucket number is initialized when the request is prepared which
    *         occurs at <code>prepare()</code> which is called right after the requests is posted.
    * @see #prepare()
    */
   public final Integer getBucketNumber() {

      return bucketNumber;
   }


   /**
    * Returns <code>true</code> if this request is a primary owner request.
    *
    * @return <code>true</code> if this request is a primary owner request. A primary owner request is the one that is
    *         is sent to a primary owner.
    */
   protected final boolean isPrimaryRequest() {

      return storageNumber == 0;
   }


   /**
    * Returns <code>true</code> if this request is a replica owner request.
    *
    * @return <code>true</code> if this request is a replica owner request. A replica owner request is the one that is
    *         is sent to a replica owner.
    */
   protected final boolean isReplicaRequest() {

      return storageNumber > 0;
   }


   /**
    * {@inheritDoc}
    */
   public PrepareResult prepare() {

      final CacheProcessor processor = getCacheProcessor();

      // Set will cache flag if there is a front cache
      if (storageNumber == 0 && processor.getFrontCache() != null) {

         final FrontCacheConfiguration cacheConfiguration = processor.getFrontCache().getFrontCacheConfiguration();
         final ExpirationConfiguration expiration = cacheConfiguration.getStore().getExpiration();
         final Time currentTime = processor.getClock().currentTime();
         setWillCacheUntil(currentTime.add(expiration.getTimeToLiveMillis()));
      }

      // Calculate bucket number
      bucketNumber = processor.getBucketNumber(key);

      // All key requests require owner address
      final ClusterNodeAddress keyOwner = processor.getBucketOwner(storageNumber, bucketNumber);
      if (keyOwner == null) {

         // There is no owner
         processor.post(createResponse(Response.RESULT_RETRY));

         // Nothing to process
         return PrepareResult.BREAK;

      } else {

         setReceiver(keyOwner);

         if (getReceiver().isAddressOf(processor.getAddress())) {

            return PrepareResult.EXECUTE;
         } else {

            return PrepareResult.ROUTE;
         }
      }
   }


   /**
    * {@inheritDoc}
    */
   public final boolean isPrepared() {

      return prepared;
   }


   /**
    * {@inheritDoc}
    */
   public final void markPrepared() {

      prepared = true;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation enhances default behaviour by validating that the bucket number has been resolved.
    */
   public void validate() throws InvalidMessageException {

      super.validate();

      // Validate that the bucket number has been resolved
      if (bucketNumber == null) {
         throw new InvalidMessageException("Bucket number is null");
      }
   }


   /**
    * {@inheritDoc}
    */
   protected final void executeOperational() {

      // Get bucket
      final CacheProcessor cacheProcessor = getCacheProcessor();
      if (!cacheProcessor.isBucketOwner(storageNumber, bucketNumber)) {

         // Not our bucket
         if (LOG.isDebugEnabled()) {
            LOG.debug(">>>>>>>>>>> " + this.getClass().getSimpleName() + ": Not our bucket " + bucketNumber);
         }
         cacheProcessor.post(createResponse(Response.RESULT_RETRY));
         return;
      }


      // Get a bucket to process

      final Bucket bucket = cacheProcessor.getBucket(storageNumber, bucketNumber);
      if (bucket == null || (!lockReconfiguringBucket && bucket.isReconfiguring())) {

         if (LOG.isDebugEnabled()) {
            LOG.debug("Bucket " + bucketNumber + " is reconfiguring, asking to retry: " + this);
         }

         cacheProcessor.post(createResponse(Response.RESULT_RETRY));
         return;
      }

      // Process
      final ProcessingResult processingResult = processKey(bucket, key);

      // Create response and set result
      final CacheResponse response = (CacheResponse) createResponse(Response.RESULT_SUCCESS);
      response.setResult(processingResult.getResult());

      // Calculate invalidation
      final boolean invalidate = processingResult.hasModifiedKey() && hasUnexpiredLease(bucket);

      // Post response if this is a no-replica configuration or if this is a leaf request
      if (cacheProcessor.getReplicaCount() == 0) {

         respond(invalidate, response);

         return;
      }


      // Replica does not need invalidation
      if (isReplicaRequest()) {

         // Just post response
         respond(false, response);

         return;
      }


      // This is primary
      Assert.assertTrue(isPrimaryRequest(), "Should be a primary request: {0}", this);

      // Update replicas.
      final List<? extends KeyRequest> subrequests = createSubrequests(response);
      if (subrequests == null || subrequests.isEmpty()) {

         // No replica subrequests, post response
         respond(invalidate, response);
      } else {

         // There are subrequests

         // Post invalidate
         if (invalidate) {

            // Most important is that first all subrequests are created and attached and only then posted.
            cacheProcessor.post(createInvalidateAnnouncement(response));
         }

         // Post replica update requests
         for (final KeyRequest subrequest : subrequests) {

            cacheProcessor.post(subrequest);
         }
      }
   }


   private void respond(final boolean invalidate, final CacheResponse response) {

      if (invalidate) {

         // Delay response until all caches are invalidated
         getProcessor().post(createInvalidateAnnouncement(response));
      } else {

         // Just post response
         getProcessor().post(response);
      }
   }


   private ClearFrontCacheBucketAnnouncement createInvalidateAnnouncement(final CacheResponse response) {

      final ClearFrontCacheBucketAnnouncement announcement = new ClearFrontCacheBucketAnnouncement(getCacheName(), bucketNumber);
      ((CacheDataRequest.Waiter) getWaiter()).attachSubrequest(response, announcement);
      return announcement;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation proceeds normally if this is a replica request. It responds with retry if this is a primary
    * owner request.
    */
   protected final void executeBlocked() {

      if (storageNumber > 0) {

         // Infrastructure request
         executeOperational();

      } else {

         // Client request
         getProcessor().post(createResponse(Response.RESULT_RETRY));
      }
   }


   /**
    * Processes a key in a bucket. It is guaranteed that this bucket is an owner of the key and that it is locked.
    *
    * @param bucket the bucket that owns the key.
    * @param key    the key to process.
    * @return the result of processing the key.
    */
   protected abstract ProcessingResult processKey(final Bucket bucket, final Binary key);


   /**
    * Splits data carried by an implementation of <code>KeyRequest</code> into a collection of requests to replicas.
    *
    * @param response the pre-created response.
    * @return a Collection of requests, each carrying parts of data per owner.
    * @see #prepare()
    */
   protected List<KeyRequest> createSubrequests(final CacheResponse response) {

      final CacheProcessor cacheProcessor = getCacheProcessor();
      final int replicaCount = cacheProcessor.getReplicaCount();
      final int bucketNumber = cacheProcessor.getBucketNumber(key);
      Assert.assertTrue(replicaCount > 0, "Replica count should be greater than zero");
      final List<KeyRequest> result = new ArrayList<KeyRequest>(replicaCount);
      final Waiter ownerWaiter = (Waiter) getWaiter();
      for (int i = 1; i <= replicaCount; i++) {

         final ClusterNodeAddress owner = cacheProcessor.getBucketOwner(i, bucketNumber);
         if (owner != null) {

            final KeyRequest subrequest = createRequest();
            subrequest.setReceiver(owner);
            subrequest.storageNumber = i;
            ownerWaiter.attachSubrequest(response, subrequest);
            result.add(subrequest);
         }
      }
      return result;
   }


   public abstract KeyRequest createRequest();


   @SuppressWarnings("RedundantMethodOverride")
   protected org.cacheonix.impl.net.processor.Waiter createWaiter() {

      return new Waiter(this);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      storageNumber = SerializerUtils.readInteger(in);
      bucketNumber = SerializerUtils.readInteger(in);
      lockReconfiguringBucket = in.readBoolean();
      key = SerializerUtils.readBinary(in);
      prepared = in.readBoolean();
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      SerializerUtils.writeInteger(out, storageNumber);
      SerializerUtils.writeInteger(out, bucketNumber);
      out.writeBoolean(lockReconfiguringBucket);
      SerializerUtils.writeBinary(out, key);
      out.writeBoolean(prepared);
   }


   // ==================================================================================================================
   //
   // Result
   //
   // ==================================================================================================================


   /**
    * The object holding result of the call to {@link KeyRequest#processKey(Bucket, Binary)}.
    */
   protected static final class ProcessingResult {

      /**
       * The result of execution.
       */
      private Object result = null;

      /**
       * Numbers of modified buckets or null or empty.
       */
      private Binary modifiedKey = null;


      /**
       * Creates new result.
       *
       * @param result      the result of execution.
       * @param modifiedKey a modified key, can be null if no modifications made.
       */
      public ProcessingResult(final Object result, final Binary modifiedKey) {

         this.result = result;
         this.modifiedKey = modifiedKey;
      }


      /**
       * Returns true if has a non-null, non-empty set of modified bucket numbers.
       *
       * @return true if has a non-null, non-empty set of modified bucket numbers.
       */
      public boolean hasModifiedKey() {

         return modifiedKey != null;
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
      public Binary getModifiedKey() {

         return modifiedKey;
      }


      public String toString() {

         return "Result{" +
                 "result=" + result +
                 ", modifiedKey=" + modifiedKey +
                 '}';
      }
   }


   // ==================================================================================================================
   //
   // Waiter
   //
   // ==================================================================================================================

   /**
    * {@inheritDoc}
    * <p/>
    * In addition to the usual waiting for a response, <code>KeyRequest</code>'s waiter holds objects and provides
    * methods that support waiting for responses from sub-requests.
    */
   @SuppressWarnings({"ClassNameSameAsAncestorName", "CanBeFinal", "ReturnOfCollectionOrArrayField"})
   static class Waiter extends CacheDataRequest.Waiter {


      /**
       * Creates waiter.
       *
       * @param request request this owner belongs to.
       */
      Waiter(final Request request) {

         super(request);
      }


      /**
       * {@inheritDoc}
       */
      @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "ThrowableInstanceNeverThrown"})
      public void notifyResponseReceived(final Response response) throws InterruptedException {

         // Convert response to the result
         if (response instanceof CacheResponse) {

            final CacheResponse cacheResponse = (CacheResponse) response;
            final int resultCode = cacheResponse.getResultCode();
            final Object result = cacheResponse.getResult();
            switch (resultCode) {

               case CacheResponse.RESULT_SUCCESS:

                  setResult(result);
                  break;

               case CacheResponse.RESULT_INACCESSIBLE:
               case CacheResponse.RESULT_RETRY:

                  setResult(new RetryException());
                  break;

               case CacheResponse.RESULT_ERROR:

                  setResult(WaiterUtils.resultToThrowable(result));
                  break;

               default:

                  setResult(WaiterUtils.unknownResultToThrowable(resultCode, result));
            }
         } else {

            setResult("Unknown result type: " + response.getClass().getName());
         }
         super.notifyResponseReceived(response);
      }


      /**
       * Called when:
       * <p/>
       * 1. This is a root request and all subrequests has finished.
       * <p/>
       * 2. This is a subrequest
       */
      protected synchronized void notifyFinished() {

         // Check if there is processor - a request could have finished with retry because there is no a processor yet.
         // See CACHEONIX-368 - "java.lang.NullPointerException at GetAllRequest$Waiter.notifyFinished()" for details.
         if (getRequest().getProcessor() != null) {

            if (getOwnerWaiter() == null) {

               // This is a root request - try to cache result
               if (getResult() instanceof CacheableValue) {

                  final KeyRequest request = (KeyRequest) getRequest();
                  final CacheProcessor processor = (CacheProcessor) request.getProcessor();
                  final FrontCache frontCache = processor.getFrontCache();
                  if (frontCache != null) {

                     final CacheableValue cacheableValue = (CacheableValue) getResult();
                     final Time currentTime = processor.getClock().currentTime();
                     final Time expirationTime = cacheableValue.getTimeToLeave();
                     if (expirationTime != null && expirationTime.compareTo(currentTime) > 0) {

                        // Cache
                        frontCache.put(request.getKey(), cacheableValue.getBinaryValue(), expirationTime);
                     }
                  }
               }
            } else {

               // This is a sub request - remove self from the wait list
               final boolean existed = getOwnerWaiter().getPartialWaiters().remove(this);
               Assert.assertTrue(existed, "Waiter should have been registered, but it wasn't: {0}", this);

               final KeyRequest request = (KeyRequest) getRequest();
               final RequestProcessor processor = request.getProcessor();

               // Re-submit if required
               if (getResult() instanceof RetryException) {

                  final KeyRequest resubmit = request.createRequest();
                  processor.post(resubmit);
               }

               // Post remembered owner response if all subrequests have finished
               if (getOwnerWaiter().isPartialWaitersEmpty() && (getOwnerResponse() != null)) {

                  processor.post(getOwnerResponse());
               }
            }
         }

         super.notifyFinished();
      }
   }


   public String toString() {

      return "KeyRequest{" +
              "prepared=" + prepared +
              ", storageNumber=" + storageNumber +
              ", bucketNumber=" + bucketNumber +
              ", key=" + key +
              ", lockReconfiguringBucket=" + lockReconfiguringBucket +
              "} " + super.toString();
   }
}
