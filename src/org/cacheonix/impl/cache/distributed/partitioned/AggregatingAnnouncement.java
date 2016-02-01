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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ReplicatedStateProcessorKey;
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.PrepareResult;
import org.cacheonix.impl.net.processor.Prepareable;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An abstract implementation of scatter-gather reliable multicast pattern.
 * <p/>
 * <code>AggregatingAnnouncement</code> is a foundation for a category of announcements that require processing
 * replicated data while delaying response to the sender of the announcement until a CacheProcessor that owns the data
 * responds.
 * <p/>
 * <code>AggregatingAnnouncement</code> encapsulates common actions and delegates the implementation of actions specific
 * to a particular request. In this regard <code>AggregatingAnnouncement</code>  is an implementation of the template
 * method pattern.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
@SuppressWarnings({"RedundantIfStatement", "WeakerAccess"})
public abstract class AggregatingAnnouncement extends Request implements Prepareable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AggregatingAnnouncement.class); // NOPMD

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

   private String cacheName = null;


   /**
    * Required to support <code>Wireable</code>.
    */
   protected AggregatingAnnouncement() {

   }


   /**
    * Creates an <code>AggregatingAnnouncement</code> using given wireable type and cache name.
    *
    * @param wireableType unique wireable type. The wireable type should have {@link Wireable#DESTINATION_REPLICATED_STATE}.
    * @param cacheName    cache name
    */
   protected AggregatingAnnouncement(final int wireableType, final String cacheName) {

      super(wireableType);

      setResponseRequired(true);

      this.cacheName = cacheName;
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return ReplicatedStateProcessorKey.getInstance();
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
    */
   public void validate() throws InvalidMessageException {

      super.validate();

      // Assert that receivers are empty
      if (isReceiverSet()) {

         throw new InvalidMessageException("Announcement cannot have a destination");
      }
   }


   protected final String getCacheName() {

      return cacheName;
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
   public final void setStorageNumber(final int storageNumber) {

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
   public final Integer getStorageNumber() {

      return storageNumber;
   }


   /**
    * Returns <code>true</code> if this request is a root request.
    *
    * @return <code>true</code> if this request is a root request. A root request is the one that collects results on
    *         behalf of the client thread.
    */
   protected final boolean isRootRequest() {

      return storageNumber == null;
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
         final Collection<? extends AggregatingAnnouncement> subAnnouncements = split(0);

         // NOTE: simeshev@cacheonix.org - 2010-01-12 - If there are no subrequests,
         // there is nothing to wait for. This may happen if no data was provided.
         // Finish now. The aggregate() method must be prepared to deal with an
         // empty result. For the majority it seems that an empty success means
         // "no action was performed". See CACHEONIX-254 for more information.
         if (subAnnouncements.isEmpty()) {

            getWaiter().finish();
         }

         postSubrequests(subAnnouncements);

         return PrepareResult.BREAK;
      } else {

         // Not a root request, execute in run()
         return PrepareResult.ROUTE;
      }
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
    * @param subRequests the Collection of sub-requests.
    */
   protected void postSubrequests(final Collection<? extends AggregatingAnnouncement> subRequests) {

      // Minor optimization
      if (subRequests == null || subRequests.isEmpty()) {
         return;
      }

      for (final AggregatingAnnouncement subRequest : subRequests) {

         // Register and pass owner response
         final Waiter ownerWaiter = (Waiter) this.getWaiter();
         final Waiter subRequestWaiter = (Waiter) subRequest.getWaiter();
         subRequestWaiter.setOwnerWaiter(ownerWaiter);
         ownerWaiter.getPartialWaiters().add(subRequestWaiter);

         // Post
         getProcessor().post(subRequest);
      }
   }


   /**
    * Returns <code>true</code> if the list of partial waiters is empty. Otherwise returns <code>false</code>.
    *
    * @return <code>true</code> if the list of partial waiters is empty. Otherwise returns <code>false</code>.
    */
   protected final boolean isWaitingForSubrequests() {

      return !((Waiter) getWaiter()).isPartialWaitersEmpty();
   }


   /**
    * Splits data carried by an implementation of <code>AggregatingRequest</code> into a collection of requests
    * according to the ownership of the data at the given storage number.
    *
    * @param storageNumber storage number for that to check for data ownership.
    * @return a Collection of requests, each carrying parts of data per owner.
    * @see #prepare()
    */
   protected abstract Collection<? extends AggregatingAnnouncement> split(final int storageNumber);


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

      final AggregatingAnnouncementResponse response = new AggregatingAnnouncementResponse();
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
   protected static RetryException createRetryException(final Response response) {

      if (response.getResult() instanceof String) {

         final String message = (String) response.getResult();
         return new RetryException(message);
      } else {

         return new RetryException();
      }
   }


   protected final ClusterProcessor getClusterProcessor() {

      return (ClusterProcessor) getProcessor();
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      storageNumber = SerializerUtils.readInteger(in);
      cacheName = SerializerUtils.readString(in);
      prepared = in.readBoolean();
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeInteger(out, storageNumber);
      SerializerUtils.writeString(cacheName, out);
      out.writeBoolean(prepared);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final AggregatingAnnouncement that = (AggregatingAnnouncement) o;

      if (cacheName != null ? !cacheName.equals(that.cacheName) : that.cacheName != null) {
         return false;
      }
      if (storageNumber != null ? !storageNumber.equals(that.storageNumber) : that.storageNumber != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (storageNumber != null ? storageNumber.hashCode() : 0);
      result = 31 * result + (cacheName != null ? cacheName.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "AggregatingAnnouncement{" +
              "cacheName='" + cacheName + '\'' +
              ", storageNumber=" + storageNumber +
              "} " + super.toString();
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
   abstract static class Waiter extends org.cacheonix.impl.net.processor.Waiter {

      /**
       * Waiter of the owner of this request. Makes sense only for a sub-request. <code>ownerWaiter</code> be null if
       * this is a root request. <code>ownerWaiter</code> is always null if the request is a stub.
       */
      private final AtomicReference<Waiter> ownerWaiter = new AtomicReference<Waiter>(null);

      /**
       * Collector for partial results. Holds responses from sub-requests. Makes sense only for a parent request.
       */
      private List<Response> partialResponses = null;

      /**
       * Partial wait list. Tracks completion of initial subrequests and their self-resends. Makes sense only for a
       * parent request.
       */
      private Set<Waiter> partialWaiters = null;


      /**
       * Creates waiter.
       *
       * @param request request this owner belongs to.
       */
      Waiter(final Request request) {

         super(request);
      }


      /**
       * Sets owner of this waiter.
       *
       * @param ownerWaiter the owner to set. Makes sense only for a sub-request.
       */
      public final void setOwnerWaiter(final Waiter ownerWaiter) {

         this.ownerWaiter.set(ownerWaiter);
      }


      /**
       * Returns a waiter of the owner of this request. <code>ownerWaiter</code> be null if this is a root request.
       * <code>ownerWaiter</code> is always null if the request is a stub.
       *
       * @return the waiter of the owner of this request. <code>ownerWaiter</code> be null if this is a root request.
       *         <code>ownerWaiter</code> is always null if the request is a stub.
       */
      public final Waiter getOwnerWaiter() {

         return ownerWaiter.get();
      }


      /**
       * Returns a list of partial responses. If the list is not set, initializes it to an empty list before returning.
       *
       * @return the list of partial responses.
       */
      protected final List<Response> getPartialResponses() {

         if (partialResponses == null) {
            partialResponses = new LinkedList<Response>();
         }
         return partialResponses;
      }


      /**
       * Returns a list of partial waiters. If the list is not set, initializes it to an empty list before returning.
       * <p/>
       * The list of partial waiters is used by subrequests to decide if the owner is done.
       *
       * @return the list of partial waiters.
       */
      protected final Set<Waiter> getPartialWaiters() {

         final boolean ownerNull = ownerWaiter.get() == null || ((AggregatingAnnouncement) ownerWaiter.get().getRequest()).isRootRequest();
         Assert.assertTrue(ownerNull, "This method can be called only if owner is null: {0}", ownerWaiter.get());
         if (partialWaiters == null) {

            partialWaiters = new HashSet<Waiter>(1);
         }
         return partialWaiters;
      }


      /**
       * Returns <code>true</code> if partial waiter list is empty.
       *
       * @return <code>true</code> if partial waiter list is empty.
       */
      protected boolean isPartialWaitersEmpty() {

         return partialWaiters == null || partialWaiters.isEmpty();
      }


      /**
       * Called when:
       * <p/>
       * 1. This is a root request and all subrequests has finished. The partial responses are aggregated and the result
       * is set. Call to super unblocks the client thread. The partial responses can only contain results. Rejected
       * buckets should be empty.
       * <p/>
       * 2. This is a subrequest. Re-splits and re-sends incomplete requests or finishes if nothing to re-send
       */
      protected synchronized void notifyFinished() {

         final AggregatingAnnouncement request = (AggregatingAnnouncement) getRequest();

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

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled()) LOG.debug("Root is done and should notify the client thread"); // NOPMD

            Assert.assertTrue(getOwnerWaiter() == null, "Owner should be null", getOwnerWaiter());

            // Aggregate
            final List<Response> partialResponses = getPartialResponses();

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
            final Collection<? extends AggregatingAnnouncement> requests = request.split(request.getStorageNumber());
            final AggregatingAnnouncement ownerRequest = (AggregatingAnnouncement) getOwnerWaiter().getRequest();
            ownerRequest.postSubrequests(requests);


            // This is a sub request - remove self from the wait list

            final boolean existed = getOwnerWaiter().getPartialWaiters().remove(this);
            Assert.assertTrue(existed, "Waiter should have been registered, but it wasn't: {0}", this);

            // Check if owner waiter is still waiting for responses from subrequests

            if (getOwnerWaiter().isPartialWaitersEmpty()) {

               // All sub-requests has finished.

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled()) LOG.debug("All sub-requests has finished"); // NOPMD

               if (ownerRequest.isRootRequest()) {

                  // Owner request is a root. This means that this is a waiter 
                  // for a primary request. As root request doesn't receive
                  // an actual response, this last finished primary waiter must
                  // finish the root request explicitly. When finishing, the root
                  // request will aggregate the partial responses from primary owners
                  // and set the result that will be used by a client thread.
                  getOwnerWaiter().finish();
               }
            }
         }

         super.notifyFinished();
      }


      public String toString() {

         return "Waiter{" +
                 "ownerWaiter=" + ownerWaiter +
                 ", partialResults.size()=" + (partialResponses == null ? "null" : partialResponses.size()) +
                 ", partialWaiters.size()=" + (partialWaiters == null ? "null" : partialWaiters.size()) +
                 "} " + super.toString();
      }
   }
}
