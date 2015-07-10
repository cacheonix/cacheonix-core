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

import java.io.Serializable;

import org.cacheonix.cache.Cache;
import org.cacheonix.cache.executor.Aggregator;
import org.cacheonix.cache.executor.Executable;

/**
 * An object that defines filtered access to cached data.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface EntryFilter extends Serializable {

   /**
    * This method is used to filters out a cache entry before it is passed passed to  an <code>Executable</code>. It
    * returns <code>true</code> if a cache entry satisfies filter's criteria. It returns <code>false</code> if the entry
    * does not satisfy filter's criteria.
    * <p/>
    * <b>Important:</b> Implementations of this method must not use blocking operations such as I/O. The implementations
    * of this method must not use any synchronization or access threading APIs. The implementations of this method
    * should also avoid making any assumptions about the execution environment except the provided
    * <code>cacheEntry</code> because it can and will be executed on any node in the cluster. If initial set up is
    * required, it should be performed before calling <code>Cache.execute()</code>.
    *
    * @param cacheEntry the cache entry to evaluate.
    * @return <code>true</code> if an entry satisfies filter's criteria. Returns <code>false</code> if the entry does
    *         not satisfy filter's criteria.
    * @see Cache#execute(EntryFilter, Executable, Aggregator)
    */
   boolean matches(CacheEntry cacheEntry);
}