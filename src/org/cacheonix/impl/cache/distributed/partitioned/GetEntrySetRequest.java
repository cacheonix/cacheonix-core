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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Obtains and collects entry sets from all bucket owners.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 18, 2010 12:31:51 PM
 */
public final class GetEntrySetRequest extends BucketSetRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GetEntrySetRequest.class); // NOPMD


   /**
    * Required by <code>Wireable<code>.
    */
   public GetEntrySetRequest() {

   }


   public GetEntrySetRequest(final String cacheName) {

      super(TYPE_CACHE_ENTRY_SET_REQUEST, cacheName, true);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears buckets that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processBuckets(final List<Bucket> bucketsToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing GetEntrySetRequest: " + this);

      // Calculate bucket size
      int size = 0;
      for (final Bucket bucket : bucketsToProcess) {
         size += bucket.size();
      }

      // Collect
      final Collection<Map.Entry<Binary, Binary>> partialEntries = new ArrayList<Map.Entry<Binary, Binary>>(size);
      for (final Bucket bucket : bucketsToProcess) {
         final Set<Map.Entry<Binary, Binary>> entries = bucket.entrySet();
         for (final Map.Entry<Binary, Binary> entry : entries) {
            partialEntries.add(entry);
         }
      }

      // Return result
      return new ProcessingResult(partialEntries, null);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation simply sums up partial sizes.
    */
   @SuppressWarnings("unchecked")
   protected Object aggregate(final List<Response> partialResponses) {

      final LinkedList<Map.Entry<Binary, Binary>> result = new LinkedList<Map.Entry<Binary, Binary>>();
      for (final Message partialResponse : partialResponses) {

         if (partialResponse instanceof CacheResponse) {
            final CacheResponse cacheResponse = (CacheResponse) partialResponse;
            final int resultCode = cacheResponse.getResultCode();
            final Object partialResult = cacheResponse.getResult();
            switch (resultCode) {
               case CacheResponse.RESULT_ERROR:
                  return WaiterUtils.resultToThrowable(partialResult);
               case CacheResponse.RESULT_INACCESSIBLE:
               case CacheResponse.RESULT_RETRY:
                  return createRetryException(cacheResponse);
               case CacheResponse.RESULT_SUCCESS:
                  result.addAll((Collection<Map.Entry<Binary, Binary>>) partialResult);
                  break;
               default:
                  return WaiterUtils.unknownResultToThrowable(resultCode, partialResult);
            }
         }
      }

      return result;
   }


   /**
    * {@inheritDoc}
    */
   protected final BucketSetRequest createRequest() {

      return new GetEntrySetRequest(getCacheName());
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetEntrySetRequest();
      }
   }
}