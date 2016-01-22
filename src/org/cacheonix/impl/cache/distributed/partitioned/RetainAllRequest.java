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
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Retains only provided keys.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see PartitionedCache#clear()
 */
public final class RetainAllRequest extends BucketSetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RetainAllRequest.class); // NOPMD

   private HashSet<Binary> keySet = null; // NOPMD


   /**
    * Required by Wireable
    *
    * @see Wireable
    */
   public RetainAllRequest() {

   }


   public RetainAllRequest(final String cacheName) {

      super(TYPE_CACHE_RETAIN_ALL_REQUEST, cacheName, false);
   }


   public void setKeySet(final HashSet<Binary> keySet) { // NOPMD

      this.keySet = new HashSet<Binary>(keySet.size());
      this.keySet.addAll(keySet);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears buckets that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processBuckets(final List<Bucket> bucketsToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing RetainAllRequest: " + this);

      // Clear and remember bucket as cleared
      final List<Bucket> modifiedBuckets = new ArrayList<Bucket>(bucketsToProcess.size());
      for (final Bucket bucket : bucketsToProcess) {

         if (!bucket.isEmpty()) {

            if (bucket.retainAll(keySet)) {

               modifiedBuckets.add(bucket);
            }
         }
      }

      return new ProcessingResult(!modifiedBuckets.isEmpty(), modifiedBuckets);
   }


   /**
    * {@inheritDoc}
    */
   protected Object aggregate(final List<Response> partialResponses) {

      boolean modified = false;
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
                  // Any bucket owner that modified its content will rise
                  // the modified flag.
                  modified |= (Boolean) result;
                  break;
               default:
                  return WaiterUtils.unknownResultToThrowable(resultCode, result);
            }
         }
      }

      return modified;
   }


   /**
    * {@inheritDoc}
    */
   protected final BucketSetRequest createRequest() {

      final RetainAllRequest result = new RetainAllRequest(getCacheName());
      result.keySet = keySet;
      return result;
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      keySet = SerializerUtils.readBinaryHashSet(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeBinaryHashSet(out, keySet);
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new RetainAllRequest();
      }
   }
}