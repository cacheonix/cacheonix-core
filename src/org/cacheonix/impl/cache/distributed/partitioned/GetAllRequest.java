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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cacheonix.CacheonixException;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.BinaryStoreUtils;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.processor.PrepareResult;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.IntObjectProcedure;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * GetAllRequest returns a list of values for a given set of keys.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see PartitionedCache#getAll(Set)
 */
public final class GetAllRequest extends KeySetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GetAllRequest.class); // NOPMD


   /**
    * Required by Wireable
    *
    * @see Wireable
    */
   public GetAllRequest() {

   }


   public GetAllRequest(final String cacheName) {

      super(TYPE_CACHE_GET_ALL_REQUEST, cacheName, true);
   }


   public PrepareResult prepare() {

      if (isRootRequest()) {

         final CacheProcessor processor = getCacheProcessor();
         final FrontCache frontCache = processor.getFrontCache();
         final IntObjectHashMap<HashSet<Binary>> keySet = getKeySet();
         final int replicaCount = processor.getReplicaCount();

         final List<CacheableEntry> results = new ArrayList<CacheableEntry>(1);
         keySet.retainEntries(new IntObjectProcedure<HashSet<Binary>>() {

            public boolean execute(final int number, final HashSet<Binary> keys) { // NOPMD

               for (final Iterator<Binary> iterator = keys.iterator(); iterator.hasNext(); ) {

                  try {

                     final Binary key = iterator.next();
                     final int bucketNumber = processor.getBucketNumber(key);

                     // Try to get from cache
                     CacheableEntry result = null;
                     if (frontCache != null) {

                        // There is front cache
                        final ReadableElement element = frontCache.get(key);
                        if (element != null) {

                           // Create result
                           final CacheableValue value = new CacheableValue(BinaryStoreUtils.getValue(element), null);
                           result = new CacheableEntry(key, value);

                           // No need to split this key into a subrequest
                           iterator.remove();
                        }
                     }

                     // Try to get from local bucket(s)
                     if (result == null) {

                        for (int storageNumber = 0; storageNumber <= replicaCount; storageNumber++) {

                           final Bucket bucket = processor.getBucket(storageNumber, bucketNumber);
                           if (processor.isBucketOwner(storageNumber, bucketNumber) && bucket != null && !bucket.isReconfiguring()) {

                              // Has bucket, proceed with returning a key from the bucket
                              final ReadableElement element = bucket.get(key);
                              if (element != null) {

                                 // Found element
                                 final CacheableValue value = new CacheableValue(BinaryStoreUtils.getValue(element), null);
                                 result = new CacheableEntry(key, value);

                                 // No need to split this key into a subrequest because it wasn't found at the owner
                                 iterator.remove();

                                 // Found bucket for the key
                                 break;
                              }
                           }
                        }
                     }

                     // Keep the result
                     if (result != null) {

                        results.add(result);
                     }
                  } catch (final RuntimeException e) {
                     throw e;
                  } catch (final Exception e) {
                     throw new CacheonixException(e);
                  }
               }

               if (keys.isEmpty()) {
                  return false;
               }


               // Continue
               return true;
            }
         });


         // Respond immediately if result was obtained from the cache or local bucket(s)
         if (!results.isEmpty()) {

            // Create response
            final Response response = createResponse(Response.RESULT_SUCCESS);
            response.setResult(results);

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled())
               LOG.debug("Responding with locally found elements: " + results.size() + ", keys left: " + getKeysSize()); // NOPMD

            // Add to 
            ((AggregatingRequest.Waiter) getWaiter()).getPartialResponses().add(response);
         }
      }


      return super.prepare();
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears keys that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processKeys(final List<BucketKeys> keysToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing GetAllRequest: " + this);

      // Prepare results - a collection of a key and value pars placed sequentially
      final Collection<CacheableEntry> results = new ArrayList<CacheableEntry>(getKeysSize());

      // Collect entries - we put keys and values side by side becuase
      // for transfer it does not matter that this is just a list
      for (final BucketKeys bucketKey : keysToProcess) {

         final Bucket bucket = bucketKey.getBucket();
         if (bucket.isEmpty()) {

            continue;
         }

         bucketKey.getKeys().forEach(new ObjectProcedure<Binary>() {

            public boolean execute(final Binary key) {

               try {
                  final ReadableElement element = bucket.get(key);
                  if (element != null) {

                     final Binary value = BinaryStoreUtils.getValue(element);

                     // Create result
                     final Time resultExpirationTime = isWillCache() ? renewLease(bucket, element.getExpirationTime()) : null;
                     results.add(new CacheableEntry(key, new CacheableValue(value, resultExpirationTime)));
                  }
               } catch (final RuntimeException e) {

                  throw e;
               } catch (final Exception e) {

                  throw new CacheonixException(e);
               }

               // Continue
               return true;
            }
         });
      }

      return new ProcessingResult(results, null);
   }


   @SuppressWarnings("unchecked")
   protected final void aggregate(final Object[] resultAccumulator, final CacheResponse cacheResponse) {

      // Collect entries - we put keys and values side by side becuase
      // for collection it does not matter that this is just a list -
      // it will be converted to map at return.
      ((Collection<CacheableEntry>) resultAccumulator[0]).addAll((Collection<CacheableEntry>) cacheResponse.getResult());
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation creates an empty <code>Binary</code> list.
    */
   protected final Object[] createResultAccumulator() {

      final Object[] resultAccumulator = new Object[1];
      resultAccumulator[0] = new ArrayList<CacheableEntry>(getKeysSize() * 2);

      return resultAccumulator;
   }


   /**
    * {@inheritDoc}
    */
   protected final KeySetRequest createRequest() {

      return new GetAllRequest(getCacheName());
   }


   /**
    * {@inheritDoc}
    */
   protected final Waiter createWaiter() {

      return new Waiter(this);
   }


   // ==================================================================================================================
   //
   // Waiter
   //
   // ==================================================================================================================


   public static final class Waiter extends KeySetRequest.Waiter {

      /**
       * Creates waiter.
       *
       * @param request request UUID
       */
      Waiter(final Request request) {

         super(request);
      }


      @SuppressWarnings("unchecked")
      protected synchronized void notifyFinished() {

         // Finish normally
         super.notifyFinished();


         // Try to cache the result
         final KeySetRequest request = (KeySetRequest) getRequest();
         if (!request.isRootRequest()) {
            return;
         }

         // Check if there is processor - a request could have finished with retry because there is no a processor yet.
         // See CACHEONIX-368 - "java.lang.NullPointerException at GetAllRequest$Waiter.notifyFinished()" for details.
         final CacheProcessor processor = (CacheProcessor) request.getProcessor();
         if (processor == null) {
            return;
         }

         // Check if there is a front cache
         final FrontCache frontCache = processor.getFrontCache();
         if (frontCache == null) {
            return;
         }

         // Check if the result is a cacheable
         if (!(getResult() instanceof Collection)) {
            return;
         }

         // Iterate the result and cache the entries
         final Time currentTime = processor.getClock().currentTime();
         final Collection<CacheableEntry> result = (Collection<CacheableEntry>) getResult();
         for (final CacheableEntry cacheableEntry : result) {

            final CacheableValue cacheableValue = cacheableEntry.getValue();
            final Time expirationTime = cacheableValue.getTimeToLeave();
            if (expirationTime != null && expirationTime.compareTo(currentTime) > 0) {

               // Cache
               frontCache.put(cacheableEntry.getKey(), cacheableValue.getBinaryValue(), expirationTime);
            }
         }
      }
   }

   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetAllRequest();
      }
   }
}
