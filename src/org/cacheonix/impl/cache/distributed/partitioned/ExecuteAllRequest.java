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
import java.util.Set;

import org.cacheonix.CacheonixException;
import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.executor.Aggregator;
import org.cacheonix.cache.executor.Executable;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.store.BinaryStoreUtils;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * ExecuteAllRequest invokes an executable against a set of keys.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see PartitionedCache#executeAll(Set, Executable, Aggregator)
 */
public final class ExecuteAllRequest extends KeySetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ExecuteAllRequest.class); // NOPMD


   /**
    * Executable.
    */
   private Executable executable = null;


   /**
    * Required by Wireable
    *
    * @see Wireable
    */
   public ExecuteAllRequest() {

   }


   public ExecuteAllRequest(final String cacheName) {

      super(TYPE_CACHE_EXECUTE_ALL_REQUEST, cacheName, false);
   }


   public void setExecutable(final Executable executable) {

      this.executable = executable;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears keys that it owns and submits sub-requests to clear replicas.
    */
   @SuppressWarnings("ThrowableInstanceNeverThrown")
   protected ProcessingResult processKeys(final List<BucketKeys> keysToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing ExecuteAllRequest: " + this);

      // Calculate size
      final int calcSize = getKeysSize();

      // Collect entries
      final List<CacheEntry> cacheEntries = new ArrayList<CacheEntry>(calcSize);
      for (final BucketKeys bucketKey : keysToProcess) {

         final Bucket bucket = bucketKey.getBucket();
         if (bucket.isEmpty()) {
            continue;
         }

         // Carries an exception storage for the inner class' procedure
         final Exception[] exception = new InvalidObjectException[1];

         final HashSet<Binary> keys = bucketKey.getKeys();
         keys.forEach(new ObjectProcedure<Binary>() {

            public boolean execute(final Binary key) {

               if (bucket.containsKey(key)) {
                  final Binary value;
                  try {

                     final ReadableElement element = bucket.get(key);
                     value = BinaryStoreUtils.getValue(element);
                     final Time expirationTime = element.getExpirationTime();
                     final Time createdTime = element.getCreatedTime();
                     final DistributedCacheEntry distributedCacheEntry = new DistributedCacheEntry(key, value,
                             createdTime, expirationTime);
                     cacheEntries.add(distributedCacheEntry);
                  } catch (final RuntimeException e) {

                     throw e;
                  } catch (final Exception e) {

                     exception[0] = e;
                     return false;
                  }
               }
               return true;
            }
         });

         // REVIEWME: simeshev@cacheonix.org - 2010-12-31 - Right now we just throw an exception that is going
         // to be caught in CacheRequest.execute() and converted to an error response that is going to be
         // posted to the sender. Is there an effect of this way of processing on the fact that it is
         // thrown in the middle of the cycle?
         if (exception[0] != null) {

            throw new CacheonixException(exception[0]);
         }
      }


      // Execute

      // Remember interrupted flag in case 3-rd party executable tries to rise it.
      final boolean interruptedBeforeExecute = Thread.currentThread().isInterrupted();

      final List<Object> results = new ArrayList<Object>(keysToProcess.size());
      try {

         // Call executable
         final Serializable result = executable.execute(cacheEntries);

         // Add to results
         results.add(result);
      } catch (final RuntimeException e) {

         // Do not trust 3-rd party executables
         results.add(e);
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

      // Return result
      return new ProcessingResult(results, null);
   }


   /**
    * {@inheritDoc}
    */
   protected final Object[] createResultAccumulator() {

      final Object[] resultAccumulator = new Object[1];
      resultAccumulator[0] = new LinkedList<Object>();

      return resultAccumulator;
   }


   @SuppressWarnings("unchecked")
   protected final void aggregate(final Object[] resultAccumulator, final CacheResponse cacheResponse) {

      final Collection<Object> list = (Collection<Object>) resultAccumulator[0];
      list.addAll((Collection) cacheResponse.getResult());
   }


   /**
    * {@inheritDoc}
    */
   protected final KeySetRequest createRequest() {

      final ExecuteAllRequest result = new ExecuteAllRequest(getCacheName());
      result.executable = executable;
      return result;
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      final ObjectInputStream ois = new ObjectInputStream(in);
      try {
         executable = (Executable) SerializerUtils.readObject(ois);
      } finally {
         IOUtils.closeHard(ois);
      }
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      final ObjectOutputStream oos = new ObjectOutputStream(out);
      try {
         oos.writeObject(executable);
         oos.flush();
      } finally {
         IOUtils.closeHard(oos);
      }
   }


   public String toString() {

      return "ExecuteAllRequest{" +
              "executable=" + executable +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new ExecuteAllRequest();
      }
   }
}
