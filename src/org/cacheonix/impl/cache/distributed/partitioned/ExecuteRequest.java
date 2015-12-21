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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.cache.executor.Aggregator;
import org.cacheonix.cache.executor.Executable;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Request to invoke an executable on a set of buckets.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see PartitionedCache#execute(Executable, Aggregator)
 */
public final class ExecuteRequest extends BucketSetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ExecuteRequest.class); // NOPMD


   /**
    * Mandatory executable.
    */
   private Executable executable = null;

   /**
    * Optional entry filter.
    */
   private EntryFilter entryFilter = null;


   /**
    * Required by Wireable
    *
    * @see Wireable
    */
   public ExecuteRequest() {

   }


   public ExecuteRequest(final String cacheName) {

      super(TYPE_CACHE_EXECUTE_REQUEST, cacheName, false);
   }


   public void setExecutable(final Executable executable) {

      this.executable = executable;
   }


   /**
    * Sets an entry filter.
    *
    * @param entryFilter entry filter to set.
    */
   public void setEntryFilter(final EntryFilter entryFilter) {

      this.entryFilter = entryFilter;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears buckets that it owns and submits sub-requests to clear replicas.
    */
   @SuppressWarnings("ThrowableInstanceNeverThrown")
   protected ProcessingResult processBuckets(final List<Bucket> bucketsToProcess) {

      final List<Object> results = new ArrayList<Object>(1);
      try {
         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing ExecuteRequest: " + this);

         // Calculate size
         int size = 0;
         for (final Bucket bucket : bucketsToProcess) {
            size += bucket.size();
         }

         // Convert bucket entries tp cache entries
         final ArrayList<CacheEntry> cacheEntries = new ArrayList<CacheEntry>(size);
         for (final Bucket bucket : bucketsToProcess) {

            if (bucket.isEmpty()) {
               continue;
            }

            // Convert bucket entries tp cache entries
            final Set<Map.Entry<Binary, Binary>> entries = bucket.entrySet();
            for (final Map.Entry<Binary, Binary> entry : entries) {

               // REVIEWME: simeshev@cacheonix.org - 2010-05-20 - Consider passing
               // whole entry to DistributedCacheEntry - this will save saving
               // a reference in the list.

               // REVIEWME: simeshev@cacheonix.org - 2010-05-20 - Consider using
               // a limited size batch of entries instead of collecting them all
               // together.
               final DistributedCacheEntry executableCacheEntry = new DistributedCacheEntry(entry.getKey(), entry.getValue());
               if (entryFilter == null || entryFilter.matches(executableCacheEntry)) {
                  cacheEntries.add(executableCacheEntry);
               }
            }
         }

         // Execute

         // Remember interrupted flag in case 3-rd party executable tries to rise it.
         final boolean interruptedBeforeExecute = Thread.currentThread().isInterrupted();

         final Object result;
         try {
            result = executable.execute(cacheEntries);

         } finally {

            // Restore interrupted status if it changed by the Executable
            if (interruptedBeforeExecute) {
               if (!Thread.currentThread().isInterrupted()) {
                  Thread.currentThread().interrupt();
               }
            } else {
               if (Thread.currentThread().isInterrupted()) {
                  Thread.interrupted();
               }
            }
         }

         if (result != null) {

            if (!(result instanceof Serializable) && !(result instanceof Wireable)) {
               results.add(new IOException("Result is not serializable: " + result.getClass().getName()));
            }
         }
         results.add(result);
      } catch (final RuntimeException e) {
         // Do not trust 3-rd party executables
         results.add(e);
      }

      // Return result
      return new ProcessingResult(results, null);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation simply sums up partial sizes.
    */
   @SuppressWarnings("unchecked")
   protected Object aggregate(final List<Response> partialResponses) {

      final Collection<Object> list = new LinkedList<Object>();
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
                  list.addAll((Collection<Object>) result);
                  break;
               default:
                  return WaiterUtils.unknownResultToThrowable(resultCode, result);
            }
         }
      }

      return list;
   }


   /**
    * {@inheritDoc}
    */
   protected final BucketSetRequest createRequest() {

      final ExecuteRequest executeRequest = new ExecuteRequest(getCacheName());
      executeRequest.entryFilter = entryFilter;
      executeRequest.executable = executable;
      return executeRequest;
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      final ObjectInputStream ois = new ObjectInputStream(in);
      try {
         executable = (Executable) SerializerUtils.readObject(ois);
         entryFilter = (EntryFilter) SerializerUtils.readObject(ois);
      } finally {
         IOUtils.closeHard(ois);
      }
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      final ObjectOutputStream oos = new ObjectOutputStream(out);
      try {
         oos.writeObject(executable);
         oos.writeObject(entryFilter);
         oos.flush();
      } finally {
         IOUtils.closeHard(oos);
      }
   }


   public String toString() {

      return "ExecuteRequest{" +
              "entryFilter=" + entryFilter +
              ", executable=" + executable +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ExecuteRequest();
      }
   }
}