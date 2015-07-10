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

import java.util.Collections;
import java.util.List;

import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Local cache member.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
final class LocalCacheMember implements CacheMember {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalCacheMember.class); // NOPMD

   private final String cacheName;


   LocalCacheMember(final String cacheName) {

      this.cacheName = cacheName;
   }


   /**
    * Returns a returns an empty list.
    *
    * @return a returns an empty list.
    */
   public List getInetAddresses() {

      return Collections.emptyList();
   }


   public String getCacheName() {

      return cacheName;
   }


   public String toString() {

      return "LocalCacheMember{}";
   }
}
