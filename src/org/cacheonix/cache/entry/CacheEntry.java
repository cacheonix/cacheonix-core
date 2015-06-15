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
package org.cacheonix.cache.entry;

import java.util.Collection;

import org.cacheonix.cache.executor.Executable;

/**
 * A cache entry that Cacheonix make available to <code>Executable</code> for processing.
 *
 * @see Executable#execute(Collection)
 * @see Executable
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
}
