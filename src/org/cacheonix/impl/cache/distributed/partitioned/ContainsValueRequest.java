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
import java.util.List;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * ContainsValueRequest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Dec 10, 2009 10:25:46 PM
 */
@SuppressWarnings("RedundantIfStatement")
public final class ContainsValueRequest extends BucketSetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ContainsValueRequest.class); // NOPMD

   /**
    * Value to look up.
    */
   private Binary value = null;


   /**
    * Required by <code>Wireable</code>
    */
   @SuppressWarnings("UnusedDeclaration")
   public ContainsValueRequest() {

   }


   public ContainsValueRequest(final String cacheName, final Binary value) {

      super(TYPE_CACHE_CONTAINS_VALUE_REQUEST, cacheName, true);
      this.value = value;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears buckets that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processBuckets(final List<Bucket> bucketsToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing ContainsValueRequest: " + this);

      // Find
      boolean found = false;
      for (final Bucket bucket : bucketsToProcess) {
         if (bucket.containsValue(value)) {
            found = true;
            break;
         }
      }

      // Return result
      return new ProcessingResult(found, null);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation simply sums up partial sizes.
    */
   protected Object aggregate(final List<Response> partialResponses) {

      boolean found = false;
      for (final Message partialResponse : partialResponses) {

         if (partialResponse instanceof CacheResponse) {
            final CacheResponse cacheResponse = (CacheResponse) partialResponse;
            final int resultCode = cacheResponse.getResultCode();
            final Object result = cacheResponse.getResult();
            switch (resultCode) {
               case Response.RESULT_ERROR:
                  return WaiterUtils.resultToThrowable(result);
               case Response.RESULT_INACCESSIBLE:
               case Response.RESULT_RETRY:
                  return createRetryException(cacheResponse);
               case Response.RESULT_SUCCESS:
                  if (!found && Boolean.TRUE.equals(result)) {
                     found = true;
                  }
                  break;
               default:
                  return WaiterUtils.unknownResultToThrowable(resultCode, result);
            }
         }
      }

      return found;
   }


   /**
    * {@inheritDoc}
    */
   protected final BucketSetRequest createRequest() {

      return new ContainsValueRequest(getCacheName(), value);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      value = SerializerUtils.readBinary(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeBinary(out, value);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof ContainsValueRequest)) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final ContainsValueRequest that = (ContainsValueRequest) o;

      if (value != null ? !value.equals(that.value) : that.value != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "ContainsValueRequest{" +
              "value=" + value +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ContainsValueRequest();
      }
   }
}