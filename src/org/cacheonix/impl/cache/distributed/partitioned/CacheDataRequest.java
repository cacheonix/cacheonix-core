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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashSet;

/**
 * A cache request that reads or writes cache data.
 */
@SuppressWarnings("RedundantIfStatement")
public abstract class CacheDataRequest extends CacheRequest {

   /**
    * A flag indicating if this is a read request. If the flag is set to <code>false</code>, this is a write request.
    * Read or write type determines if the request checks out a bucket for read or for write. Read requests extend the
    * read lease time if the willCacheUntil is set and if there are no pending write requests for the bucket.
    */
   private boolean readRequest = false;

   /**
    * A time until that the requester would like to cache the results if the result is cacheable.
    */
   private Time willCacheUntil = null;


   CacheDataRequest() {

   }


   CacheDataRequest(final int wireableType, final String cacheName, final boolean readRequest) {

      super(wireableType, cacheName);

      this.readRequest = readRequest;
   }


   /**
    * Returns a flag indicating if this is a read request.
    *
    * @return flag indicating if this is a read request. If the flag is set to <code>false</code>, this is a write
    *         request. Read or write type determines if the request checks out a bucket for read or for write. Read
    *         requests extend the read lease time if the willCacheUntil is set and if there are no pending write
    *         requests for the bucket.
    */
   public final boolean isReadRequest() {

      return readRequest;
   }


   /**
    * The flag indicating if the result is going to be cached if the result is cacheable.
    *
    * @return the flag indicating if the result is going to be cached if the result is cacheable.
    */
   final boolean isWillCache() {

      return willCacheUntil != null;
   }


   /**
    * Sets the time until that the requester would like to cache the results if the result is cacheable.
    *
    * @param willCacheUntil the time until that the requester would like to cache the results if the result is
    *                       cacheable.
    */
   final void setWillCacheUntil(final Time willCacheUntil) {

      this.willCacheUntil = willCacheUntil;
   }


   /**
    * Returns the time until that the requester would like to cache the results if the result is cacheable.
    *
    * @return the time until that the requester would like to cache the results if the result is cacheable.
    */
   public final Time getWillCacheUntil() {

      return willCacheUntil;
   }


   /**
    * Updates lease time based on the element's time to live.
    *
    * @param bucket                    the bucket the element belongs to.
    * @param desiredLeaseExtensionTime element's time to live.
    * @return new lease time as it was set in the bucket.
    */
   final Time renewLease(final Bucket bucket, final Time desiredLeaseExtensionTime) {

      final Time currentTime = getProcessor().getClock().currentTime();
      final long leaseDurationMillis = bucket.getLeaseDurationMillis();
      final Time newLeaseExpirationTime = currentTime.add(leaseDurationMillis);
      final Time leaseExpirationTime = bucket.getLeaseExpirationTime();

      if (desiredLeaseExtensionTime == null) {

         // Non-expirable element
         if (leaseExpirationTime == null || currentTime.compareTo(leaseExpirationTime) >= 0) {

            // Lease expired
            bucket.setLeaseExpirationTime(newLeaseExpirationTime);
            return newLeaseExpirationTime;
         } else {

            // Lease not expired
            return leaseExpirationTime;
         }
      } else {

         // Expirable element
         if (leaseExpirationTime == null || currentTime.compareTo(leaseExpirationTime) >= 0) {

            // Lease expired - figure out new lease time
            if (newLeaseExpirationTime.compareTo(desiredLeaseExtensionTime) > 0) {

               // New lease time is too long
               bucket.setLeaseExpirationTime(desiredLeaseExtensionTime);
               return desiredLeaseExtensionTime;

            } else {

               // New lease time is closer than expiration
               bucket.setLeaseExpirationTime(newLeaseExpirationTime);
               return newLeaseExpirationTime;
            }
         } else {

            // Lease not expired
            if (leaseExpirationTime.compareTo(desiredLeaseExtensionTime) > 0) {

               // New lease time is too long
               return desiredLeaseExtensionTime;
            } else {

               // New lease time is closer than expiration
               return leaseExpirationTime;
            }
         }
      }
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      willCacheUntil = SerializerUtils.readTime(in);
      readRequest = in.readBoolean();
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      SerializerUtils.writeTime(willCacheUntil, out);
      out.writeBoolean(readRequest);
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

      final CacheDataRequest that = (CacheDataRequest) o;

      if (readRequest != that.readRequest) {
         return false;
      }
      if (willCacheUntil != null ? !willCacheUntil.equals(that.willCacheUntil) : that.willCacheUntil != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (readRequest ? 1 : 0);
      result = 31 * result + (willCacheUntil != null ? willCacheUntil.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "CacheDataRequest{" +
              "readRequest=" + readRequest +
              ", willCacheUntil=" + willCacheUntil +
              "} " + super.toString();
   }


   boolean hasUnexpiredLease(final Bucket bucket) {

      final Time leaseExpirationTime = bucket.getLeaseExpirationTime();
      final Time currentTime = getProcessor().getClock().currentTime();
      return leaseExpirationTime != null && currentTime.compareTo(leaseExpirationTime) < 0;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This extension holds objects and provides methods that support scattering sub-requests.
    */
   @SuppressWarnings({"ClassNameSameAsAncestorName", "CanBeFinal", "ReturnOfCollectionOrArrayField"})
   abstract static class Waiter extends org.cacheonix.impl.net.processor.Waiter {

      /**
       * Waiter of the owner of this request. Makes sense only for a sub-request. <code>ownerWaiter</code> be null if
       * this is a root request. <code>ownerWaiter</code> is always null if the request is a stub.
       */
      private final AtomicReference<Waiter> ownerWaiter = new AtomicReference<Waiter>(null);

      /**
       * A response that this owner created before posting sub-requests. This response will be posted when all
       * subrequests finished. Makes sense only for a sub-request.
       */
      private final AtomicReference<CacheResponse> ownerResponse = new AtomicReference<CacheResponse>(null);

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
       * Returns a list of partial waiters. If the list is not set, initializes it to an empty list before returning.
       * <p/>
       * The list of partial waiters is used by subrequests to decide if the owner is done.
       *
       * @return the list of partial waiters.
       */
      final Set<Waiter> getPartialWaiters() {

         final boolean ownerNull = ownerWaiter.get() == null || ((AggregatingRequest) ownerWaiter.get().getRequest()).isRootRequest();
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
      final boolean isPartialWaitersEmpty() {

         return partialWaiters == null || partialWaiters.isEmpty();
      }


      /**
       * Returns a response that this owner created before posting sub-requests. Makes sense only for a sub-request.
       *
       * @return the response that this owner created before posting sub-requests.
       */
      public CacheResponse getOwnerResponse() {

         return ownerResponse.get();
      }


      /**
       * Sets the response that this owner created before posting sub-requests. Makes sense only for a sub-request.
       *
       * @param ownerResponse the response that this owner created before posting sub-requests. Makes sense only for a
       *                      sub-request.
       */
      public final void setOwnerResponse(final CacheResponse ownerResponse) {

         this.ownerResponse.set(ownerResponse);
      }


      /**
       * Attaches subrequest to the owner request.
       *
       * @param ownerResponse owner response to post on completion of all subrequests.
       * @param subRequest    the subrequest.
       */
      public final void attachSubrequest(final CacheResponse ownerResponse, final Request subRequest) {

         final Waiter subRequestWaiter = (Waiter) subRequest.getWaiter();
         subRequestWaiter.setOwnerWaiter(this);
         subRequestWaiter.setOwnerResponse(ownerResponse);
         getPartialWaiters().add(subRequestWaiter);
      }


      /**
       * Attaches subrequests to the owner request.
       *
       * @param ownerResponse owner response to post on completion of all subrequests.
       * @param subRequests   the subrequest.
       */
      public final void attachSubrequests(final CacheResponse ownerResponse,
                                          final Collection<? extends Request> subRequests) {

         for (final Request subRequest : subRequests) {

            attachSubrequest(ownerResponse, subRequest);
         }
      }


      public String toString() {

         return "Waiter{" +
                 "ownerWaiter=" + ownerWaiter +
                 ", ownerResponse=" + ownerResponse +
                 ", partialWaiters=" + StringUtils.sizeToString(partialWaiters) +
                 "} " + super.toString();
      }
   }
}
