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
import java.util.Set;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.PreviousValue;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * ClearAllRequest clears all keys.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see PartitionedCache#removeAll(Set)
 */
public final class RemoveAllRequest extends KeySetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RemoveAllRequest.class); // NOPMD


   /**
    * Required by Wireable
    *
    * @see Wireable
    */
   public RemoveAllRequest() {

   }


   public RemoveAllRequest(final String cacheName) {

      super(TYPE_CACHE_REMOVE_ALL_REQUEST, cacheName, false);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears keys that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processKeys(final List<BucketKeys> keysToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing RemoveAllRequest: " + this);


      final IntObjectHashMap<HashSet<Binary>> removedKeys = new IntObjectHashMap<HashSet<Binary>>(1);
      for (final BucketKeys bucketKey : keysToProcess) {

         final Bucket bucket = bucketKey.getBucket();
         final HashSet<Binary> keys = bucketKey.getKeys();
         keys.forEach(new ObjectProcedure<Binary>() {

            public boolean execute(final Binary key) {

               final PreviousValue previousValue = bucket.remove(key);
               if (previousValue.isPreviousValuePresent()) {

                  // Register in cleared
                  final int bucketNumber = bucket.getBucketNumber();
                  HashSet<Binary> k = removedKeys.get(bucketNumber);
                  if (k == null) {

                     k = new HashSet<Binary>(1);
                     removedKeys.put(bucketNumber, k);
                  }
                  k.add(key);

               }
               return true;
            }
         });
      }

      return new ProcessingResult(!removedKeys.isEmpty(), removedKeys);
   }


   @SuppressWarnings("unchecked")
   protected final void aggregate(final Object[] resultAccumulator, final CacheResponse cacheResponse) {

      final Boolean result = (Boolean) resultAccumulator[0];
      resultAccumulator[0] = result || (Boolean) cacheResponse.getResult();
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation creates a FALSE <code>Boolean</code>.
    */
   protected final Object[] createResultAccumulator() {

      final Object[] resultAccumulator = new Object[1];
      resultAccumulator[0] = Boolean.FALSE;

      return resultAccumulator;
   }


   /**
    * {@inheritDoc}
    */
   protected final KeySetRequest createRequest() {

      return new RemoveAllRequest(getCacheName());
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new RemoveAllRequest();
      }
   }
}