/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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
import java.util.Map.Entry;
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
    * Builder used by WireableFactory.
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
      final Collection<Entry<Binary, Binary>> partialEntries = new ArrayList<Entry<Binary, Binary>>(size);
      for (final Bucket bucket : bucketsToProcess) {
         final Set<Entry<Binary, Binary>> entries = bucket.entrySet();
         for (final Entry<Binary, Binary> entry : entries) {
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

      final LinkedList<Entry<Binary, Binary>> result = new LinkedList<Entry<Binary, Binary>>();
      for (final Message partialResponse : partialResponses) {

         if (partialResponse instanceof CacheResponse) {
            final CacheResponse cacheResponse = (CacheResponse) partialResponse;
            final int resultCode = cacheResponse.getResultCode();
            final Object partialResult = cacheResponse.getResult();
            switch (resultCode) {
               case Response.RESULT_ERROR:
                  return WaiterUtils.resultToThrowable(partialResult);
               case Response.RESULT_INACCESSIBLE:
               case Response.RESULT_RETRY:

                  return cacheResponse.createRetryException();
               case Response.RESULT_SUCCESS:
                  result.addAll((Collection<Entry<Binary, Binary>>) partialResult);
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
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetEntrySetRequest();
      }
   }
}