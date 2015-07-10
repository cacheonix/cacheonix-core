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

import java.util.List;

import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An implementation of CacheEntry used by distributed cached.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see KeySetRequest#processKeys(List)
 * @see BucketSetRequest#processBuckets(List)
 * @since May 17, 2010 10:42:31 PM
 */
final class DistributedCacheEntry implements CacheEntry {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DistributedCacheEntry.class); // NOPMD

   private final Binary key;

   private final Binary value;


   DistributedCacheEntry(final Binary key, final Binary value) {

      this.key = key;
      this.value = value;
   }


   public Object getKey() {

      return key.getValue();
   }


   public Object getValue() {

      return value.getValue();
   }


   public String toString() {

      return "DistributedCacheEntry{" +
              "key=" + key +
              ", value=" + value +
              '}';
   }
}