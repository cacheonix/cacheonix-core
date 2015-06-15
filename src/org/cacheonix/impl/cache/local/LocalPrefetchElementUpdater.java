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

import org.cacheonix.ShutdownException;
import org.cacheonix.impl.cache.datasource.PrefetchElementUpdater;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.util.exception.ExceptionUtils;

/**
 *
 */
public final class LocalPrefetchElementUpdater implements PrefetchElementUpdater {

   private LocalCache localCache;


   public void setLocalCache(final LocalCache localCache) {

      //noinspection AssignmentToCollectionOrArrayFieldFromParameter
      this.localCache = localCache;
   }


   public void updateElement(final Binary key, final Serializable object, final Time timeToRead,
           final long expectedElementUpdateCounter) {

      try {

         localCache.update((Serializable) key.getValue(), object, timeToRead, expectedElementUpdateCounter);
      } catch (final ShutdownException e) {
         ExceptionUtils.ignoreException(e, "Nothing we can do because the local cache was shutdown");
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation removes the key from the local cache.
    */
   public void removeElement(final Binary key) {

      try {

         localCache.remove(key);
      } catch (final ShutdownException e) {
         ExceptionUtils.ignoreException(e, "Nothing we can do because the local cache was shutdown");
      }
   }
}
