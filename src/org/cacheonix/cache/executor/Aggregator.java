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
package org.cacheonix.cache.executor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.cacheonix.cache.Cache;

/**
 * An aggregator of partial results produced by <code>Executable#execute()</code>. An implementation of
 * <code>Aggregator</code> must be supplied to all variations of <code>Cache.execute()</code>. Cacheonix calls the
 * <code>Aggregator.aggregate()</code> after it receives partial results from all cluster nodes participating in the
 * distributed processing of cache entries. The <code>Aggregator</code> must process the partial results and return the
 * final result.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see Cache#executeAll(Set, Executable, Aggregator)
 * @since May 14, 2010 9:35:04 PM
 */
public interface Aggregator {

   /**
    * Produces a final result of distributed data processing by aggregating a collection of partial results that are a
    * result of work of <code>Executable</code>. Cacheonix calls this method after it receives all partial results from
    * all cluster nodes participating in the distributed processing of cache entries.
    *
    * @param partialExecutionResults a collection of objects representing partial execution results. Each object in the
    *                                <code>partialExecutionResults</code> is a result of a call to
    *                                <code>Executable.execute()</code>. The <code>Executable.execute()</code> must
    *                                produce objects of a type or types that are understood by the Aggregator. The
    *                                actual type of the members of the collection is application-specific.
    * @return the final result of distributed data processing. The result may be null if the logic allows for no
    *         result.
    * @see Executable#execute(Collection)
    * @see Cache#executeAll(Set, Executable, Aggregator)
    */
   Serializable aggregate(Collection<Serializable> partialExecutionResults);
}
