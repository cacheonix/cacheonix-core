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
package org.cacheonix.impl.cache.local;


import java.util.Set;

import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.cache.executor.Aggregator;
import org.cacheonix.cache.executor.Executable;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;

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

   /**
    * Time the element was created.
    */
   private Time createdTime;

   /**
    * Time to expire.
    */
   private Time expirationTime;


   /**
    * Creates a new instance of <tt>CacheEntryImpl</tt>
    *
    * @param key            the key.
    * @param value          the value.
    * @param createdTime    the time this entry was created.
    * @param expirationTime the time this entry expires.
    */
   LocalCacheEntry(final Binary key, final Binary value, final Time createdTime, final Time expirationTime) {

      this.key = key;
      this.value = value;
      this.createdTime = createdTime;
      this.expirationTime = expirationTime;
   }


   public Object getKey() {

      return toObject(key);
   }


   public Object getValue() {

      return toObject(value);
   }


   /**
    * Returns time this element expires.
    *
    * @return time this element expires or null of the expiration time is not available.
    */
   public Time getExpirationTime() {

      return expirationTime;
   }


   /**
    * Returns the time this element was created.
    *
    * @return the time this element was created  or null of the created time is not available.
    */
   public Time getCreatedTime() {

      return createdTime;
   }


   private static Object toObject(final Binary result) {

      if (result == null) {

         return null;
      }

      return result.getValue();
   }


   public String toString() {

      return "LocalCacheEntry{" +
              "key=" + key +
              ", value=" + value +
              ", createdTime=" + createdTime +
              ", expirationTime=" + expirationTime +
              '}';
   }
}
