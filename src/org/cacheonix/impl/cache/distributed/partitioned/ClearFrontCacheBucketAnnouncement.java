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
import java.util.Arrays;

import org.cacheonix.impl.net.cluster.ClusterResponse;
import org.cacheonix.impl.net.cluster.DeliveryAware;
import org.cacheonix.impl.net.cluster.ReplicatedStateProcessorKey;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.RequestProcessor;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.array.IntArrayList;

/**
 * This reliable mcast message posts an invalidation message to the local cache.
 *
 * @see ClearFrontCacheBucketMessage
 */
@SuppressWarnings("RedundantIfStatement")
public final class ClearFrontCacheBucketAnnouncement extends Request {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private String cacheName;

   private int[] bucketNumbers;


   /**
    * Required by <code>Wireable</code>.
    */
   private ClearFrontCacheBucketAnnouncement() {

   }


   public ClearFrontCacheBucketAnnouncement(final String cacheName) {

      super(Wireable.TYPE_CACHE_INVALIDATE_FRONT_CACHE_ANNOUNCEMENT);

      this.cacheName = cacheName;
   }


   public ClearFrontCacheBucketAnnouncement(final String cacheName, final int bucketNumber) {

      this(cacheName);
      this.bucketNumbers = new int[]{bucketNumber};
   }


   public ClearFrontCacheBucketAnnouncement(final String cacheName, final IntArrayList bucketNumbers) {

      this(cacheName);
      this.bucketNumbers = bucketNumbers.toNativeArray();
   }


   protected ProcessorKey getProcessorKey() {

      return ReplicatedStateProcessorKey.getInstance();
   }


   int[] getBucketNumbers() {

      return ArrayUtils.copy(bucketNumbers);
   }


   public void execute() {

      final RequestProcessor processor = getProcessor();
      processor.post(new ClearFrontCacheBucketMessage(cacheName, bucketNumbers));
   }


   public Response createResponse(final int responseCode) {

      return new ClusterResponse();
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      SerializerUtils.writeString(cacheName, out);
      SerializerUtils.writeShortArray(out, bucketNumbers);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      cacheName = SerializerUtils.readString(in);
      bucketNumbers = SerializerUtils.readShortArray(in);
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

      final ClearFrontCacheBucketAnnouncement that = (ClearFrontCacheBucketAnnouncement) o;

      if (!Arrays.equals(bucketNumbers, that.bucketNumbers)) {
         return false;
      }
      if (cacheName != null ? !cacheName.equals(that.cacheName) : that.cacheName != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (cacheName != null ? cacheName.hashCode() : 0);
      result = 31 * result + (bucketNumbers != null ? Arrays.hashCode(bucketNumbers) : 0);
      return result;
   }


   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   public static final class Waiter extends CacheDataRequest.Waiter implements DeliveryAware {

      /**
       * Creates waiter.
       *
       * @param request request UUID
       */
      public Waiter(final Request request) {

         super(request);
      }


      public void notifyDelivered() {

         finish();
      }


      protected void notifyFinished() {

         getOwnerWaiter().getPartialWaiters().remove(this);

         if (getOwnerWaiter().isPartialWaitersEmpty() && (getOwnerResponse() != null)) {

            getRequest().getProcessor().post(getOwnerResponse());
         }

         super.notifyFinished();
      }
   }


   public String toString() {

      return "ClearFrontCacheBucketAnnouncement{" +
              "cacheName='" + cacheName + '\'' +
              ", bucketNumber=" + Arrays.toString(bucketNumbers) +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClearFrontCacheBucketAnnouncement();
      }
   }
}
