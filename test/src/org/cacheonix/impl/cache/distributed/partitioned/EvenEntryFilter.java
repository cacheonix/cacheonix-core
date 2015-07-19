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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.impl.util.logging.Logger;

/**
 * EvenFilter
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since May 20, 2010 4:38:33 PM
 */
final class EvenEntryFilter implements EntryFilter {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EvenEntryFilter.class); // NOPMD


   public boolean matches(final CacheEntry cacheEntry) {

      final String key = (String) cacheEntry.getKey();

      return Integer.parseInt(key.substring(CacheonixTestCase.TEST_KEY_PREFIX.length())) % 2 == 0;
   }
}
