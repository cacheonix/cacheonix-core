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

import org.cacheonix.impl.net.processor.SimpleProcessorKey;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 */
@SuppressWarnings("RedundantIfStatement")
public final class CacheProcessorKey extends SimpleProcessorKey {

   private final String cacheName;


   public CacheProcessorKey(final String cacheName) {

      super(Wireable.DESTINATION_CACHE_PROCESSOR);
      this.cacheName = cacheName;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final CacheProcessorKey that = (CacheProcessorKey) o;

      if (!cacheName.equals(that.cacheName)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + cacheName.hashCode();
      return result;
   }


   public String toString() {

      return cacheName + ':' + super.toString();
   }
}
