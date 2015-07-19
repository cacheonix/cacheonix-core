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
package org.cacheonix.cluster;

import java.util.List;

/**
 * A server that runs Cacheonix instance for the given cache name. A clustered cache consists of a set of
 * <code>CacheMembers</code>.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface CacheMember {

   /**
    * Returns unmodifiable list of <code>InetAddress</code> objects that this cache member may be accessed at.
    * <p/>
    * A local cache returns an empty list.
    *
    * @return a list of <code>InetAddress</code> objects that this cache member may be accessed at.
    */
   List getInetAddresses();


   /**
    * Returns the name of the cache.
    *
    * @return name of the cache.
    */
   String getCacheName();
}
