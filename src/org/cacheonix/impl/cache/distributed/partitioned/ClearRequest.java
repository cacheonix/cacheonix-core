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

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * ClearRequest clears all buckets.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see PartitionedCache#clear()
 */
public final class ClearRequest extends BucketSetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClearRequest.class); // NOPMD


   /**
    * Required by Wireable
    *
    * @see Wireable
    */
   public ClearRequest() {

   }


   public ClearRequest(final String cacheName) {

      super(TYPE_CACHE_CLEAR_REQUEST, cacheName, false);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears buckets that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processBuckets(final List<Bucket> bucketsToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing ClearRequest: " + this);

      // Clear and remember bucket as cleared
      final List<Bucket> modifiedBuckets = new ArrayList<Bucket>(bucketsToProcess.size());
      for (final Bucket bucket : bucketsToProcess) {

         if (!bucket.isEmpty()) {

            bucket.clear();
            modifiedBuckets.add(bucket);
         }
      }

      return new ProcessingResult(null, modifiedBuckets);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * Because <code>Map.clear()<code> returns <code>void</code>, this implementation simply searches the list of partial
    * responses for errors.
    */
   protected Object aggregate(final List<Response> partialResponses) {

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
                  // Do nothing because clear() does not require a response
                  break;
               default:
                  return WaiterUtils.unknownResultToThrowable(resultCode, result);
            }
         }
      }

      // We return null because clear() call returns void
      return null;
   }


   /**
    * {@inheritDoc}
    */
   protected final BucketSetRequest createRequest() {

      return new ClearRequest(getCacheName());
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClearRequest();
      }
   }
}