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

import org.cacheonix.CacheonixException;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.cache.store.BinaryStoreUtils;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.processor.PrepareResult;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Cache get request is sent by the distributed cache when the key is stored remotely.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferencedInHashCode, NonFinalFieldReferenceInEquals, RedundantIfStatement,
 * UnnecessaryParentheses
 * @see PartitionedCache#get(Object)
 */
public final class GetRequest extends KeyRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GetRequest.class); // NOPMD


   /**
    * Required by <code>Wireable<code>.
    *
    * @see Wireable
    */
   public GetRequest() {

   }


   public GetRequest(final String cacheName, final Binary key) {

      super(TYPE_CACHE_GET_REQUEST, cacheName, true, true);
      setKey(key);
   }


   public final PrepareResult prepare() {

      // Receiver is not set

      try {

         // Try to get from cache
         CacheableValue result = null;
         final CacheProcessor processor = getCacheProcessor();
         final FrontCache frontCache = processor.getFrontCache();
         if (frontCache != null) {

            // There is front cache
            final ReadableElement element = frontCache.get(getKey());
            if (element != null) {

               // Respond with found cached value
               final Time expirationTime = element.getExpirationTime();
               final Time createdTime = element.getCreatedTime();
               result = new CacheableValue(BinaryStoreUtils.getValue(element), null, createdTime, expirationTime);
            }
         }

         // Try to get from local bucket(s)
         if (result == null) {

            final int replicaCount = processor.getReplicaCount();
            final int bucketNumber = processor.getBucketNumber(getKey());
            for (int storageNumber = 0; storageNumber <= replicaCount; storageNumber++) {

               final Bucket bucket = processor.getBucket(storageNumber, bucketNumber);
               if (processor.isBucketOwner(storageNumber,
                       bucketNumber) && bucket != null && !bucket.isReconfiguring()) {

                  // Has bucket, proceed with returning a key from the bucket
                  final ReadableElement element = bucket.get(getKey());
                  final Time expirationTime = element.getExpirationTime();
                  final Time createdTime = element.getCreatedTime();
                  result = new CacheableValue(BinaryStoreUtils.getValue(element), null, createdTime, expirationTime);

//                  //noinspection ControlFlowStatementWithoutBraces
//                  if (LOG.isDebugEnabled()) LOG.debug("Found element in storage " + storageNumber); // NOPMD
                  break;
               }
            }
         }

         // Respond immediately if result was obtained from the cache or local bucket(s)
         if (result != null) {

            final Response response = createResponse(Response.RESULT_SUCCESS);
            response.setResult(result);
            processor.post(response);

            // Stop there
            return PrepareResult.BREAK;
         }

         // Element wasn't in cache or local bucket(s), prepare normally
         return super.prepare();

      } catch (final InvalidObjectException e) {

         throw new CacheonixException(e);
      } catch (final StorageException e) {

         throw new CacheonixException(e);
      }
   }


   protected ProcessingResult processKey(final Bucket bucket, final Binary key) {

      try {

         // Execute and set result
         final ReadableElement element = bucket.get(key);


         if (element == null) {

            // The key not found
            return new ProcessingResult(null, null);
         } else {

            // The key found
            final Binary value = BinaryStoreUtils.getValue(element);

            // Calculate expiration time
            final Time resultExpirationTime = isWillCache() ? renewLease(bucket, element.getExpirationTime()) : null;

            // Set result
            final Time expirationTime = element.getExpirationTime();
            final Time createdTime = element.getCreatedTime();
            final CacheableValue cacheableValue = new CacheableValue(value, resultExpirationTime, createdTime,
                    expirationTime);
            return new ProcessingResult(cacheableValue, null);
         }
      } catch (final Exception e) {

         throw new CacheonixException(e);
      }
   }


   /**
    * {@inheritDoc}
    */
   public KeyRequest createRequest() {

      return new GetRequest(getCacheName(), getKey());
   }


   public String toString() {

      return "GetRequest{" +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetRequest();
      }
   }
}
