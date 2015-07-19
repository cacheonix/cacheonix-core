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

import java.io.Serializable;

import org.cacheonix.impl.cache.datasource.PrefetchElementUpdater;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.BinaryUtils;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.processor.Router;

/**
 * An implementation of </code>PrefetchElementUpdater<code> used by distrbuted Cacheonix.
 */
public final class DistributedPrefetchElementUpdater implements PrefetchElementUpdater {

   /**
    * Cache name.
    */
   private final String cacheName;

   /**
    * Router.
    */
   private final Router router;


   /**
    * Creates a new instance of <code>DistributedPrefetchElementUpdater</code>.
    *
    * @param router    a route to route {@link UpdateKeyRequest}
    * @param cacheName a cache name.
    */
   public DistributedPrefetchElementUpdater(final Router router, final String cacheName) {

      this.cacheName = cacheName;
      this.router = router;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation posts a {@link UpdateKeyRequest}.
    */
   public void updateElement(final Binary key, final Serializable value, final Time timeToRead,
           final long expectedElementUpdateCounter) {

      //
      final UpdateKeyRequest updateKeyRequest = new UpdateKeyRequest(cacheName, key, BinaryUtils.toBinary(value),
              timeToRead, expectedElementUpdateCounter);
      //
      router.route(updateKeyRequest);
   }


   public void removeElement(final Binary key) {

      //
      final RemoveRequest removeRequest = new RemoveRequest(cacheName, key);

      //
      router.route(removeRequest);
   }


   public String toString() {

      return "DistributedPrefetchElementUpdater{" +
              "cacheName='" + cacheName + '\'' +
              '}';
   }
}
