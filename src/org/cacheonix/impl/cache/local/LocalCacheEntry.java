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
package org.cacheonix.impl.cache.local;


import java.io.Serializable;
import java.util.Set;

import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.cache.executor.Aggregator;
import org.cacheonix.cache.executor.Executable;
import org.cacheonix.impl.cache.item.Binary;

/**
 * An implementation of CacheEntry used by the local cache.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see LocalCache#execute(Executable, Aggregator)
 * @see LocalCache#execute(EntryFilter, Executable, Aggregator)
 * @see LocalCache#executeAll(Set, Executable, Aggregator)
 * @since May 17, 2010 10:42:31 PM
 */
final class LocalCacheEntry implements CacheEntry {

   private final Binary key;

   private final Binary value;


   public LocalCacheEntry(final Binary key, final Binary value) {

      this.key = key;
      this.value = value;
   }


   public Object getKey() {

      return toObject(key);
   }


   public Object getValue() {

      return toObject(value);
   }


   private static Serializable toObject(final Binary result) {

      if (result == null) {

         return null;
      }

      return (Serializable) result.getValue();
   }


   public String toString() {

      return "LocalCacheEntry{" +
              "key=" + key +
              ", value=" + value +
              '}';
   }
}
