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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.config.ExpirationConfiguration;
import org.cacheonix.impl.config.FrontCacheConfiguration;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.PrepareResult;
import org.cacheonix.impl.net.processor.Prepareable;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Provides an abstract implementation of scatter-gather request pattern.
 * <p/>
 * <code>AggregatingRequest</code> is a foundation for a category of cache requests that require processing set-like
 * data at nodes that own it. <code>AggregatingRequest</code> encapsulates common actions and delegates the
 * implementation of actions specific to a particular request. In this regard <code>AggregatingRequest</code>  is an
 * implementation of the template method pattern.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public abstract class AggregatingRequest extends CacheDataRequest implements Prepareable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AggregatingRequest.class); // NOPMD

   /**
    * True if the request was prepared.
    */
   private boolean prepared = false;


   /**
    * Storage number to that the request is addressed. The storage number can be null which means that this is a root
    * request. It can be zero, which means that this is a request to a primary bucket owner. It can be between one and
    * the number of replicas which means that this is a request to a replica owner.
    */
   private Integer storageNumber = null;


   /**
    * Required to support <code>Wireable</code>.
    */
   AggregatingRequest() {

   }


   /**
    * Creates an <code>AggregatingRequest</code> using given wireable type and cache name.
    *
    * @param wireableType unique wireable type. The wireable type should have {@link Wireable#DESTINATION_CACHE_PROCESSOR}.
    * @param cacheName    cache name
    * @param readRequest  a flag indicating if this is a read request. If the flag is set to <code>false</code>, this is
    *                     a write request. Read or write type determines if the request checks out a bucket for read or
    *                     for write. Read requests extend the read lease time if the willCacheUntil is set and if there
    *                     are no pending write requests for the bucket.
    */
   AggregatingRequest(final int wireableType, final String cacheName, final boolean readRequest) {

      super(wireableType, cacheName, readRequest);
   }


   /**
    * Sets a storage number.
    *
    * @param storageNumber the storage number to set. The storage number can be null which means that this is a root
    *                      request. It can be zero, which means that this is a requires to a primary bucket owner. It
    *                      can be between one and the number of replicas which means that this is a request to a replica
    *                      owner.
    * @see #getStorageNumber
    */
   final void setStorageNumber(final int storageNumber) {

      this.storageNumber = storageNumber;
   }


   /**
    * Returns the storage number. The storage number can be null which means that this is a root request. It can be
    * zero, which means that this is a requires to a primary bucket owner. It can be between one and the number of
    * replicas which means that this is a request to a replica owner.
    *
    * @return the storage number. The storage number can be null which means that this is a root request. It can be
    *         zero, which means that this is a requires to a primary bucket owner. It can be between one and the number
    *         of replicas which means that this is a request to a replica owner.
    */
   final Integer getStorageNumber() {

      return storageNumber;
   }


   /**
    * Returns <code>true</code> if this request is a root request.
    *
    * @return <code>true</code> if this request is a root request. A root request is the one that collects results on
    *         behalf of the client thread.
    */
   final boolean isRootRequest() {

      return storageNumber == null;
   }


   /**
    * Returns <code>true</code> if this request is a primary owner request.
    *
    * @return <code>true</code> if this request is a primary owner request. A primary owner request is the one that is
    *         is sent to a primary owner.
    */
   final boolean isPrimaryRequest() {

      return storageNumber != null && storageNumber == 0;
   }


   /**
    * Returns <code>true</code> if this request is a replica owner request.
    *
    * @return <code>true</code> if this request is a replica owner request. A replica owner request is the one that is
    *         is sent to a replica owner.
    */
   final boolean isReplicaRequest() {

      return storageNumber != null && storageNumber > 0;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation proceeds normally if this is a sub-request. It responds with retry if this is a client
    * request.
    */
   protected final void executeBlocked() {

      if (isRootRequest()) {

         // Client request
         getProcessor().post(createResponse(Response.RESULT_RETRY));
      } else {

         // Infrastructure request
         executeOperational();
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * If this is a root request, it posts sub-requests and return <code>false</code> indicating that this request should
    * not be added to the execution queue. Otherwise returns <code>false</code>.
    */
   public PrepareResult prepare() {

      if (isRootRequest()) {

         // This is a root, submit subrequests instead of executing
         final Collection<? extends AggregatingRequest> subrequests = split(0);

         // NOTE: simeshev@cacheonix.org - 2010-01-12 - If there are no subrequests,
         // there is nothing to wait for. This may happen if no data was provided.
         // Finish now. The aggregate() method must be prepared to deal with an
         // empty result. For the majority it seems that an empty success means
         // "no action was performed". See CACHEONIX-254 for more information.
         if (subrequests.isEmpty()) {

            getWaiter().finish();
         } else {

            final CacheProcessor processor = getCacheProcessor();
            if (processor.getFrontCache() != null) {

               // Set will cache flag if there is a front cache
               final FrontCacheConfiguration cacheConfiguration = processor.getFrontCache().getFrontCacheConfiguration();
               final ExpirationConfiguration expiration = cacheConfiguration.getStore().getExpiration();
               final Time willCacheUntil = processor.getClock().currentTime().add(expiration.getTimeToLiveMillis());
               for (final AggregatingRequest subrequest : subrequests) {

                  subrequest.setWillCacheUntil(willCacheUntil);
               }
            }

            postSubrequests(null, subrequests);
         }

         return PrepareResult.BREAK;
      } else {


         // Not a root request, execute in run()
         if (getReceiver().isAddressOf(getProcessor().getAddress())) {

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
    * Posts a collection of sub-requests.
    * <p/>
    * Before being posted each sub-request is assigned an owner waiter and the owner response. The owner waiter is set
    * to this request's waiter.
    * <p/>
    * Sub-request's waiter is registered in this request waiter's list of partial waiters. Sub-requests use it to detect
    * completion - the list is empty. Once the completion is detected, the owner
    *
    * @param ownerResponse the owner response to set. Can be null if no response should be sent upon receiving responses
    *                      from all sub-requests.
    * @param subRequests   the Collection of sub-requests.
    */
   private void postSubrequests(final CacheResponse ownerResponse,
           final Collection<? extends AggregatingRequest> subRequests) {

      // Minor optimization
      if (subRequests == null || subRequests.isEmpty()) {
         return;
      }

      // Register and pass owner response
      ((CacheDataRequest.Waiter) getWaiter()).attachSubrequests(ownerResponse, subRequests);

      // Post
      getProcessor().post(subRequests);
   }


   /**
    * Returns <code>true</code> if the list of partial waiters is empty. Otherwise returns <code>false</code>.
    *
    * @return <code>true</code> if the list of partial waiters is empty. Otherwise returns <code>false</code>.
    */
   final boolean isWaitingForSubrequests() {

      return !((CacheDataRequest.Waiter) getWaiter()).isPartialWaitersEmpty();
   }


   /**
    * Splits data carried by an implementation of <code>AggregatingRequest</code> into a collection of requests
    * according to the ownership of the data at the given storage number.
    *
    * @param storageNumber storage number for that to check for data ownership.
    * @return a Collection of requests, each carrying parts of data per owner.
    * @see #prepare()
    */
   protected abstract Collection<? extends AggregatingRequest> split(final int storageNumber);


   /**
    * Aggregates responses from subrequests that were sent by a <b>parent</b> request. This template is called when a
    * parent request finishes.
    * <p/>
    * <p/>
    * Root requests (requests from the root to primary buckets owners) must transform partial responses from primary
    * bucket owners to a results usable by the client thread.
    * <p/>
    * <p/>
    * Requests from primary bucket owners to replicas if such are required (all write requests do update replicas) at
    * least must collect errors if any.
    *
    * @param partialResponses list of responses from subrequests.
    * @return a resulting object that can be understood by the caller or an exception object. This is the object
    *         returned by <code>Waiter.waitForResult()</code>.
    * @see Waiter#notifyFinished()
    */
   protected abstract Object aggregate(final List<Response> partialResponses);


   /**
    * Clears unprocessed data that this request still holds.
    */
   abstract void clear();


   public final Response createResponse(final int resultCode) {

      final AggregatingResponse response = new AggregatingResponse(getCacheName());
      response.setResponseToClass(getClass());
      response.setResponseToUUID(getUuid());
      response.setResultCode(resultCode);
      response.setReceiver(getSender());
      return response;
   }


   /**
    * Creates a RetryException while setting the exception message to the response's result if the response carries a
    * String result. A utility method.
    *
    * @param response the response to use.
    * @return RetryException with the exception message set to the response's result if the response carries a String
    *         result.
    */
   static RetryException createRetryException(final Response response) {

      if (response.getResult() instanceof String) {

         final String message = (String) response.getResult();
         return new RetryException(message);
      } else {

         return new RetryException();
      }
   }


   // Posts a response with a debug statement
   final void respond(final AggregatingResponse response) {

      // This is an end of chain becuase this is storage zero with
      // no replicas or a replica storage
      if (LOG.isDebugEnabled()) {
         LOG.debug("ooooooooooooooo Responding: " + response);
      }

      getProcessor().post(response);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      prepared = in.readBoolean();
      storageNumber = SerializerUtils.readInteger(in);
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      out.writeBoolean(prepared);
      SerializerUtils.writeInteger(out, storageNumber);
   }

   // ==================================================================================================================
   //
   // Waiter
   //
   // ==================================================================================================================

   /**
    * {@inheritDoc}
    * <p/>
    * In addition to the usual waiting for a response, <code>AggregatingRequest</code>'s waiter holds objects and
    * provides methods that support scattering sub-requests and gathering responses.
    */
   @SuppressWarnings({"ClassNameSameAsAncestorName", "CanBeFinal", "ReturnOfCollectionOrArrayField"})
   static abstract class Waiter extends CacheDataRequest.Waiter {

      /**
       * Collector for partial results. Holds responses from sub-requests. Makes sense only for a parent request.
       */
      private List<Response> partialResponses = null;


      /**
       * Creates waiter.
       *
       * @param request request this owner belongs to.
       */
      Waiter(final Request request) {

         super(request);
      }


      /**
       * Returns a list of partial responses. If the list is not set, initializes it to an empty list before returning.
       *
       * @return the list of partial responses.
       */
      final List<Response> getPartialResponses() {

         if (partialResponses == null) {
            partialResponses = new LinkedList<Response>();
         }
         return partialResponses;
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
         if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooooooooooooooooo Response: " + message);

         // Remove processed buckets so that notifyFinished() re-posts only those left

         final AggregatingRequest request = (AggregatingRequest) getRequest();


         if (request.isRootRequest()) {

            // Root request still can receive responses such as retry

            final Waiter rootWaiter = (Waiter) request.getWaiter();
            if (message instanceof AggregatingResponse) {

               final AggregatingResponse response = (AggregatingResponse) message;

               switch (response.getResultCode()) {

                  // Root request *cannot* receive success
                  case Response.RESULT_SUCCESS:

                     // Clear the unprocessed entries because there is no point in
                     // retrying. The requester should receive an error in response.
                     request.clear();

                     // Add error to partial responses
                     final Response unexpectedSuccessResponse = createErrorResponse("Impossible success response", message);
                     rootWaiter.getPartialResponses().add(unexpectedSuccessResponse);

                     break;

                  // Root request *cannot* receive error
                  case Response.RESULT_ERROR:

                     // Clear the unprocessed entries because there is no point in
                     // retrying. The requester should receive an error in response.
                     request.clear();

                     // Add error to partial responses
                     final Response unexpectedErrorResponse = createErrorResponse("Impossible error response", message);
                     rootWaiter.getPartialResponses().add(unexpectedErrorResponse);

                     break;

                  case Response.RESULT_INACCESSIBLE:
                  case Response.RESULT_RETRY:

                     // Means that reconfiguration is in process. All entries
                     // belonging to this request must be re-submitted. Re-submission
                     // is done in notifyFinished(). All we need to do is do nothing,
                     // AggregatingRequest's notifyFinished() will re-submit
                     // all entries left in the request.

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


                     rootWaiter.getPartialResponses().add(message);
                     break;

                  default:

                     // Add errors to partial responses
                     final Response unexpectedResultCodeResponse = createErrorResponse("Unexpected response result code", response);
                     rootWaiter.getPartialResponses().add(unexpectedResultCodeResponse);
                     break;
               }

            } else {

               // response is not instanceof AggregatingResponse


               // This is a rare but possible situation when the error response
               // had to be created before the requests could be obtained. As it
               // cannot be obtained, it cannot be asked to create a proper type
               // response using its createResponse() method. So, this is never
               // a success response.

               // Clear the unprocessed entries because there is no point
               // in retrying. The requester should receive an error in response.
               request.clear();

               // Add errors to partial responses
               final Response unexpectedResponseError = createErrorResponse("Unexpected response type", message);
               rootWaiter.getPartialResponses().add(unexpectedResponseError);
            }
         } else {

            // Response to a partial request

            final Waiter ownerWaiter = (Waiter) getOwnerWaiter();

            if (message instanceof AggregatingResponse) {

               final AggregatingResponse response = (AggregatingResponse) message;

               Assert.assertNotNull(ownerWaiter, "Parent's notifyResponseReceived() should never be called: {0}", message);

               switch (response.getResultCode()) {

                  // Request was processed successfully,
                  // some buckets might have been rejected
                  case Response.RESULT_SUCCESS:

                     processSuccessResponse(request, ownerWaiter, response);

                     break;

                  // Unrecoverable error occurred at the destination
                  case Response.RESULT_ERROR:

                     // Clear the unprocessed entries because there is no point
                     // in trying. The requester should receive an error in response.
                     request.clear();

                     // Always add errors to partial response, i.e. regardless
                     // of primary or replica response.
                     ownerWaiter.getPartialResponses().add(response);

                     break;

                  case Response.RESULT_INACCESSIBLE:
                  case Response.RESULT_RETRY:

                     // Means that reconfiguration is in process. All entries
                     // belonging to this request must be re-submitted. Re-submission
                     // is done in notifyFinished(). All we need to do is do nothing,
                     // AggregatingRequest's notifyFinished() will re-submit
                     // all entries left in the request.
                     break;

                  default:

                     // Add errors to partial responses
                     final Response unexpectedResponseError = createErrorResponse("Unexpected response result code", response);
                     ownerWaiter.getPartialResponses().add(unexpectedResponseError);
                     break;
               }

            } else {

               // response is not instanceof AggregatingResponse


               // This is a rare but possible situation when the error response
               // had to be created before the requests could be obtained. As it
               // cannot be obtained, it cannot be asked to create a proper type
               // response using its createResponse() method. So, this is never
               // a success response.

               // Clear the unprocessed entries because there is no point
               // in retrying. The requester should receive an error in response.
               request.clear();

               // Add errors to partial responses
               final Response unexpectedResponseError = createErrorResponse("Unexpected response type", message);
               ownerWaiter.getPartialResponses().add(unexpectedResponseError);
            }
         }

         super.notifyResponseReceived(message);
      }


      protected abstract void processSuccessResponse(final AggregatingRequest request,
                                                     final Waiter ownerWaiter, final AggregatingResponse response);


      /**
       * Called when:
       * <p/>
       * 1. This is a root request and all subrequests has finished. The partial responses are aggregated and the result
       * is set. Call to super unblocks the client thread. The partial responses can only contain results. Rejected
       * buckets should be empty.
       * <p/>
       * 2. This is a subrequest
       */
      protected synchronized void notifyFinished() {

         final AggregatingRequest request = (AggregatingRequest) getRequest();

         // If owner request is null, this means that this is a
         // waiter for a root request.
         //
         // If owner request a root request, this means that *this*
         // is a waiter for a request to a primary owner.
         //
         // If owner request is a primary request, this means
         // that this is a waiter for a request to a replica owner.


         if (request.isRootRequest()) {

            // Root is done and should notify the client thread.

            Assert.assertTrue(getOwnerWaiter() == null, "Owner should be null", getOwnerWaiter());

            // Aggregate
            final List<Response> partialResponses = getPartialResponses();

            assertNoRejectedBuckets(partialResponses);

            // Aggregate partial responses
            final Object aggregatedResult = request.aggregate(partialResponses);

            // Set result
            setResult(aggregatedResult);

         } else {

            // Re-posting is done by an owner request.
            //
            // Note: a root request doesn't have an owner. It's just
            // a functor that submits requests to primary owners.

            // Re-post if there were buckets left. After executing this block
            // if the list of split request is empty, no subrequests will be
            // posted and getOwnerWaiter().isPartialWaitersEmpty() will be true.
            final Collection<? extends AggregatingRequest> requests = request.split(request.getStorageNumber());
            final CacheDataRequest.Waiter ownerWaiter = getOwnerWaiter();
            final AggregatingRequest ownerRequest = (AggregatingRequest) ownerWaiter.getRequest();
            final CacheResponse ownerResponse = getOwnerResponse();
            ownerRequest.postSubrequests(ownerResponse, requests);


            // This is a sub request - remove self from the wait list

            final boolean existed = getOwnerWaiter().getPartialWaiters().remove(this);
            Assert.assertTrue(existed, "Waiter should have been registered, but it wasn't: {0}", this);

            // Check if owner waiter is still waiting for responses from subrequests

            if (getOwnerWaiter().isPartialWaitersEmpty()) {

               // All sub-requests has finished.

               if (ownerRequest.isRootRequest()) {

                  // Owner request is a root. This means that this is a waiter 
                  // for a primary request. As root request doesn't receive
                  // an actual response, this last finished primary waiter must
                  // finish the root request explicitly. When finishing, the root
                  // request will aggregate the partial responses from primary owners
                  // and set the result that will be used by a client thread.
                  getOwnerWaiter().finish();
               } else {

                  // *Owner* request is a primary request. This means that this
                  // is a waiter is for a replica request. The above is
                  // correct because replica request are leaf requests.

                  // When owner response can be null?
                  if (ownerResponse != null) {

                     // Amend owner response to a error if any of the aggregated responses is an error
                     if (ownerResponse.getResultCode() != Response.RESULT_ERROR) {

                        final List<Response> partialResponses = ((Waiter) getOwnerWaiter()).getPartialResponses();
                        for (final Response partialResponse : partialResponses) {

                           if (partialResponse.getResultCode() == Response.RESULT_ERROR) {

                              ownerResponse.setResultCode(Response.RESULT_ERROR);
                              ownerResponse.setResult(partialResponse.getResult());
                              break;
                           }
                        }

                     }
                     request.getProcessor().post(ownerResponse);
                  }
               }
            }
         }

         super.notifyFinished();
      }


      /**
       * Creates a error response.
       *
       * @param description     the error description.
       * @param problemResponse the problem response.
       * @return the error response.
       */
      final AggregatingResponse createErrorResponse(final String description,
              final Response problemResponse) {

         final AggregatingResponse result = (AggregatingResponse) (getRequest()).createResponse(Response.RESULT_ERROR);
         final String errorMessage = description + ": " + problemResponse;
         result.setResult(errorMessage);
         return result;
      }


      /**
       * Asserts there are no rejected buckets in a list of partial responses.
       *
       * @param partialResponses the list of partial responses.
       */
      private static void assertNoRejectedBuckets(final List<Response> partialResponses) {

         for (final Message response : partialResponses) {
            if (response instanceof AggregatingResponse) {
               final AggregatingResponse aggregatingResponse = (AggregatingResponse) response;
               Assert.assertTrue(aggregatingResponse.isRejectedBucketsEmpty(),
                       "Root sub-response cannot have rejected buckets", aggregatingResponse.getRejectedBuckets());
            }
         }
      }


      public String toString() {

         return "Waiter{" +
                 "partialResponses=" + StringUtils.sizeToString(partialResponses) +
                 "} " + super.toString();
      }
   }


   public String toString() {

      return "AggregatingRequest{" +
              "storage=" + storageNumber +
              "} " + super.toString();
   }
}
