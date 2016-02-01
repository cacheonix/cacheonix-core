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

import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.impl.cache.store.CacheStatisticsImpl;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Request to obtain cache statistics.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see PartitionedCache#getStatistics() ()
 */
public final class GetStatisticsRequest extends BucketSetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GetStatisticsRequest.class); // NOPMD


   /**
    * Required by Wireable
    *
    * @see Wireable
    */
   public GetStatisticsRequest() {

   }


   public GetStatisticsRequest(final String cacheName) {

      super(TYPE_CACHE_STATISTICS_REQUEST, cacheName, true);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears buckets that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processBuckets(final List<Bucket> bucketsToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing GetStatisticsRequest: " + this);

      // Calculate statistics
      long elementsOnDiskCount = 0L;
      long readHitCount = 0L;
      long readMissCount = 0L;
      long writeHitCount = 0L;
      long writeMissCount = 0L;

      for (final Bucket bucket : bucketsToProcess) {

         final CacheStatistics bucketStatistics = bucket.getStatistics();
         elementsOnDiskCount += bucketStatistics.getElementsOnDiskCount();
         readHitCount += bucketStatistics.getReadHitCount();
         readMissCount += bucketStatistics.getReadMissCount();
         writeHitCount += bucketStatistics.getWriteHitCount();
         writeMissCount += bucketStatistics.getWriteMissCount();
      }

      final CacheStatisticsImpl partialResult = new CacheStatisticsImpl(readHitCount, readMissCount,
              writeHitCount, writeMissCount, elementsOnDiskCount);

      // Return result
      return new ProcessingResult(partialResult, null);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation simply sums up partial sizes.
    */
   protected Object aggregate(final List<Response> partialResponses) {

      long elementsOnDiskCount = 0L;
      long readHitCount = 0L;
      long readMissCount = 0L;
      long writeHitCount = 0L;
      long writeMissCount = 0L;
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

                  final CacheStatistics partialResult = (CacheStatistics) result;
                  elementsOnDiskCount += partialResult.getElementsOnDiskCount();
                  readHitCount += partialResult.getReadHitCount();
                  readMissCount += partialResult.getReadMissCount();
                  writeHitCount += partialResult.getWriteHitCount();
                  writeMissCount += partialResult.getWriteMissCount();
                  break;

               default:

                  return WaiterUtils.unknownResultToThrowable(resultCode, result);
            }
         }
      }

      return new CacheStatisticsImpl(readHitCount, readMissCount, writeHitCount, writeMissCount, elementsOnDiskCount);
   }


   /**
    * {@inheritDoc}
    */
   protected final BucketSetRequest createRequest() {

      return new GetStatisticsRequest(getCacheName());
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetStatisticsRequest();
      }
   }
}