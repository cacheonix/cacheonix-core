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

import java.util.List;

import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Request to calculate cache size.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see PartitionedCache#clear()
 */
public final class SizeRequest extends BucketSetRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SizeRequest.class); // NOPMD


   /**
    * Required by Wireable
    *
    * @see Wireable
    */
   public SizeRequest() {

   }


   public SizeRequest(final String cacheName) {

      super(TYPE_CACHE_SIZE_REQUEST, cacheName, true);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears buckets that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processBuckets(final List<Bucket> bucketsToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing SizeRequest: " + this);

      // Clear and remember bucket as cleared

      // Calculate
      int partialSize = 0;
      for (final Bucket bucket : bucketsToProcess) {
         partialSize += bucket.size();
      }

      // Return result
      return new ProcessingResult(partialSize, null);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation simply sums up partial sizes.
    */
   protected Object aggregate(final List<Response> partialResponses) {

      int size = 0;
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
                  size += (Integer) result;
                  break;
               default:
                  return WaiterUtils.unknownResultToThrowable(resultCode, result);
            }
         }
      }

      return size;
   }


   /**
    * {@inheritDoc}
    */
   protected final BucketSetRequest createRequest() {

      return new SizeRequest(getCacheName());
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new SizeRequest();
      }
   }
}