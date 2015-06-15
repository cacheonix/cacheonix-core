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
package org.cacheonix.cache.executor;

import java.io.Serializable;
import java.util.Collection;

import org.cacheonix.cache.Cache;
import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.entry.EntryFilter;

/**
 * Processes a set of cache entries.
 */
public interface Executable extends Serializable {

   /**
    * Processes a collection of <code>cacheEntries</code> and returns a result. This method runs in parallel on all
    * nodes of the cluster that carry cached data. As such, it utilizes data affinity by processing a subset of the
    * cached data that is local to a cluster node it runs on. The final aggregation of the results is performed by an
    * instance of <code>Aggregator</code> provided to <code>execute()</code>.
    * <p/>
    * <b>Important:</b> Implementations of this method must not use blocking operations such as I/O. The implementations
    * of this method must not use any synchronization or access threading APIs. The implementations of this method
    * should also avoid making any assumptions about the execution environment except the provided
    * <code>cacheEntries</code> because it can and will be executed on any node in the cluster. If initial set up is
    * required, it should be performed before calling <code>Cache.execute()</code>.
    *
    * @param cacheEntries a Set of CacheEntries to process. This is a subset of the complete set of cache entries. It
    *                     resides on the same cluster node that this method runs on.
    * @return the result of processing.
    * @see Aggregator
    * @see Cache#execute(Executable, Aggregator)
    * @see Cache#execute(EntryFilter, Executable, Aggregator)
    */
   Serializable execute(Collection<CacheEntry> cacheEntries);
}
