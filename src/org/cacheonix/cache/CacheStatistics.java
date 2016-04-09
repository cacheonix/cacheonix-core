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
package org.cacheonix.cache;

/**
 * A cache statistics.
 */
public interface CacheStatistics {

   /**
    * Returns a hit count for this cache.
    *
    * @return the hit count for this cache.
    */
   long getReadHitCount();


   /**
    * Returns a hit ratio for this cache.
    *
    * @return the hit ratio for this cache.
    */
   float getReadHitRatio();


   /**
    * Returns a miss count for this cache.
    *
    * @return the miss count for this cache.
    */
   long getReadMissCount();


   /**
    * Returns a miss ratio for this cache.
    *
    * @return the miss ratio for this cache.
    */
   float getReadMissRatio();


   /**
    * Return a number of elements on disk.
    *
    * @return the number of elements on disk.
    */
   long getElementsOnDiskCount();

   /**
    * Returns a total number of reads.
    *
    * @return the total number of reads.
    */
   long getReadTotalCount();

   /**
    * Returns the number of write hits. A write hit is a put operation that is performed against an existing key.
    *
    * @return the number of write hits. A write hit is a put operation that is performed against an existing key.
    */
   long getWriteHitCount();

   /**
    * Returns a ratio of write hits to a total number of writes.
    *
    * @return the ratio of write hits to a total number of writes.
    */
   float getWriteHitRatio();

   /**
    * Returns a total number of writes.
    *
    * @return the total number of writes.
    */
   long getWriteTotalCount();

   /**
    * Returns a number of write misses. A write miss is a put operation that is performed against an key that is not
    * present in the cache.
    *
    * @return the number of write misses. A write miss is a put operation that is performed against an key that is not
    *         present in the cache.
    */
   long getWriteMissCount();

   /**
    * Returns a ratio of write misses to a total number of writes.
    *
    * @return the ratio of write misses to a total number of writes.
    */
   float getWriteMissRatio();
}
