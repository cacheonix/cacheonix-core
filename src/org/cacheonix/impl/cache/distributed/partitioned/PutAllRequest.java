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

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.ObjectObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * PutAllRequest is a request that, in order to send a list of entries, is capable of forming a chain from the source
 * though the primary owners to the back up owners.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection RedundantIfStatement, WeakerAccess, NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode,
 * ThrowableResultOfMethodCallIgnored, ThrowableInstanceNeverThrown
 * @since Dec 19, 2009 12:53:57 PM
 */
public final class PutAllRequest extends EntrySetRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PutAllRequest.class); // NOPMD

   private Time expirationTime = null;


   /**
    *
    */
   public PutAllRequest() {

   }


   public PutAllRequest(final String cacheName) {

      super(TYPE_CACHE_PUT_ALL_REQUEST, cacheName, false);
   }


   public void setExpirationTime(final Time expirationTime) {

      this.expirationTime = expirationTime;
   }


   public Time getExpirationTime() {

      return expirationTime;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears keys that it owns and submits sub-requests to clear replicas.
    */
   protected ProcessingResult processEntries(final List<BucketEntries> entriesToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("ooooooooooooooo Executing PutAllRequest: " + this);

      final IntObjectHashMap<HashMap<Binary, Binary>> updatedEntries = new IntObjectHashMap<HashMap<Binary, Binary>>(1);
      for (final BucketEntries bucketEntry : entriesToProcess) {

         final Bucket bucket = bucketEntry.getBucket();
         final HashMap<Binary, Binary> entries = bucketEntry.getEntries();
         entries.forEachEntry(new ObjectObjectProcedure<Binary, Binary>() {

            public boolean execute(final Binary key, final Binary value) {

               // Put
               final Binary previousValue = bucket.put(key, value, expirationTime);

               // Check if modified
               if (previousValue != null && previousValue.getValue() == null && value != null && value.getValue() == null) {

                  // Not modified, continue
                  return true;
               }

               // Register in modified entries
               HashMap<Binary, Binary> map = updatedEntries.get(bucket.getBucketNumber());
               if (map == null) {

                  map = new HashMap<Binary, Binary>(1);
                  updatedEntries.put(bucket.getBucketNumber(), map);
               }
               map.put(key, value);

               return true;
            }
         });
      }


      return new ProcessingResult(!updatedEntries.isEmpty(), updatedEntries);
   }


   @SuppressWarnings("unchecked")
   protected final void aggregate(final Object[] resultAccumulator, final CacheResponse cacheResponse) {

      Boolean result = (Boolean) resultAccumulator[0];
      if (result == null) {
         result = Boolean.FALSE;
         resultAccumulator[0] = result;
      }
      resultAccumulator[0] = result || (Boolean) cacheResponse.getResult();
   }


   protected EntrySetRequest createRequest() {

      final PutAllRequest result = new PutAllRequest(getCacheName());
      result.expirationTime = expirationTime;
      return result;
   }


   public String toString() {

      return "PutAllRequest{" +
              "expirationTime=" + expirationTime +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new PutAllRequest();
      }
   }
}