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
package org.cacheonix.cache.entry;

import java.io.Serializable;
import java.util.Collection;

import org.cacheonix.cache.Cache;
import org.cacheonix.cache.executor.Executable;
import org.cacheonix.impl.clock.Time;

/**
 * A cache entry.
 *
 * @see Executable#execute(Collection)
 * @see Cache#entry(Serializable)
 */
public interface CacheEntry {

   /**
    * Returns a cache entry key.
    *
    * @return the cache entry key.
    */
   Object getKey();

   /**
    * Returns a cache entry value.
    *
    * @return the cache entry value.
    */
   Object getValue();

   /**
    * Returns time this element expires.
    *
    * @return time this element expires. Can be <code>null</code> if the time is not known.
    */
   Time getExpirationTime();


   /**
    * Returns the time this element was created.
    *
    * @return the time this element was created. Can be <code>null</code> if the time is not known.
    */
   Time getCreatedTime();
}
