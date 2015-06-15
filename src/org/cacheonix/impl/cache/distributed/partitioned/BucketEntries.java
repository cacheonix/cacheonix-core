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

import java.util.Map;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Holds a locked bucket and a set of entries for processing.
 */
public final class BucketEntries {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketEntries.class); // NOPMD

   private final Bucket bucket;

   private final HashMap<Binary, Binary> entries;  // NOPMD


   /**
    * Constructor.
    *
    * @param bucket
    * @param entries
    */
   public BucketEntries(final Bucket bucket, final HashMap<Binary, Binary> entries) {  // NOPMD
      this.bucket = bucket;
      this.entries = new HashMap<Binary, Binary>((Map<Binary, Binary>) entries);
   }


   /**
    * Returns a bucket.
    *
    * @return the bucket.
    */
   public Bucket getBucket() {

      return bucket;
   }


   /**
    * Returns a key set.
    *
    * @return the key set.
    */
   @SuppressWarnings("ReturnOfCollectionOrArrayField")
   public HashMap<Binary, Binary> getEntries() {  // NOPMD
      return entries;
   }
}