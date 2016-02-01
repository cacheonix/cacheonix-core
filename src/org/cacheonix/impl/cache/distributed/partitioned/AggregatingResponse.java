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

import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.logging.Logger;

/**
 * AggregatingCacheResponse
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 4, 2010 11:50:33 PM
 */
@SuppressWarnings("RedundantIfStatement")
public final class AggregatingResponse extends CacheResponse {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder AGGREGATING_RESPONSE_BUILDER = new Builder();


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AggregatingResponse.class); // NOPMD

   private IntHashSet rejectedBuckets = null;


   /**
    * Required by <code>Wireable</code>.
    */
   @SuppressWarnings("WeakerAccess")
   public AggregatingResponse() {

   }


   public AggregatingResponse(final String cacheName) {

      super(TYPE_CACHE_AGGREGATING_CACHE_RESPONSE, cacheName);
   }


   public IntHashSet getRejectedBuckets() {

      return rejectedBuckets;
   }


   public IntHashSet handOffRejectedBuckets() {

      final IntHashSet result = rejectedBuckets;
      rejectedBuckets = null;
      return result;
   }


   public boolean isRejectedBucketsEmpty() {

      return rejectedBuckets == null || rejectedBuckets.isEmpty();
   }


   public void setRejectedBuckets(final IntHashSet rejectedBuckets) {

      this.rejectedBuckets = rejectedBuckets;
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      rejectedBuckets = SerializerUtils.readIntHashSet(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeIntHashSet(out, rejectedBuckets);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof AggregatingResponse)) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final AggregatingResponse that = (AggregatingResponse) o;

      if (rejectedBuckets != null ? !rejectedBuckets.equals(that.rejectedBuckets) : that.rejectedBuckets != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (rejectedBuckets != null ? rejectedBuckets.hashCode() : 0);
      return result;
   }


   @Override
   public String toString() {

      return "AggregatingResponse{" +
              "rejectedBuckets.size()=" + (rejectedBuckets == null ? "null" : Integer.toString(rejectedBuckets.size())) +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new AggregatingResponse();
      }
   }
}
