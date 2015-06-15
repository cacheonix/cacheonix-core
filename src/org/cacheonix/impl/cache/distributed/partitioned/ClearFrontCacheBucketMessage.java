/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.cache.distributed.partitioned;

import java.util.Arrays;

import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.ArrayUtils;

/**
 * This message clears a bucket cache in the local cache.
 *
 * @see ClearFrontCacheBucketAnnouncement
 */
@SuppressWarnings("RedundantIfStatement")
public final class ClearFrontCacheBucketMessage extends LocalCacheMessage {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private int[] bucketNumbers;


   public ClearFrontCacheBucketMessage() {

   }


   public ClearFrontCacheBucketMessage(final String cacheName, final int[] bucketNumbers) {

      super(Wireable.TYPE_CACHE_INVALIDATE_FRONT_CACHE_MESSAGE, cacheName);

      this.bucketNumbers = ArrayUtils.copy(bucketNumbers);
   }


   int[] getBucketNumbers() {

      return ArrayUtils.copy(bucketNumbers);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation clears the front cache if it is present
    */
   protected void executeOperational() {

      final CacheProcessor cacheProcessor = getCacheProcessor();

      final FrontCache frontCache = cacheProcessor.getFrontCache();
      if (frontCache != null) {

         for (final int bucketNumber : bucketNumbers) {

            frontCache.clearBucket(bucketNumber);
         }
      }
   }


   protected void executeBlocked() {

      executeOperational();
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final ClearFrontCacheBucketMessage that = (ClearFrontCacheBucketMessage) o;

      if (!Arrays.equals(bucketNumbers, that.bucketNumbers)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (bucketNumbers != null ? Arrays.hashCode(bucketNumbers) : 0);
      return result;
   }


   public String toString() {

      return "ClearFrontCacheBucketMessage{" +
              "bucketNumbers=" + Arrays.toString(bucketNumbers) +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClearFrontCacheBucketMessage();
      }
   }
}
