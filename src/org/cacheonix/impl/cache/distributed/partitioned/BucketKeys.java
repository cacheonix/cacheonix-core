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

import java.util.Collection;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Holds a bucket and a set of keys belonging to that bucket for processing.
 */
public final class BucketKeys {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketKeys.class); // NOPMD

   private final Bucket bucket;

   private final HashSet<Binary> keys;  // NOPMD


   /**
    * Constructor.
    *
    * @param bucket
    * @param keys
    */
   public BucketKeys(final Bucket bucket, final HashSet<Binary> keys) {  // NOPMD
      this.bucket = bucket;
      this.keys = new HashSet<Binary>((Collection<? extends Binary>) keys);
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
   public HashSet<Binary> getKeys() {  // NOPMD
      return keys;
   }
}
