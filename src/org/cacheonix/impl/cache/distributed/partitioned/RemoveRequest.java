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

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.PreviousValue;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * RemoveRequest removes a key from a distributed cache.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferencedInHashCode, NonFinalFieldReferenceInEquals, RedundantIfStatement,
 * UnnecessaryParentheses
 */
public final class RemoveRequest extends KeyRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RemoveRequest.class); // NOPMD


   /**
    * Required by <code>Wireable<code>.
    *
    * @see Wireable
    */
   public RemoveRequest() {

   }


   public RemoveRequest(final ClusterNodeAddress sender, final String cacheName, final Binary key) {

      super(TYPE_CACHE_REMOVE_REQUEST, cacheName, false, false);
      this.setSender(sender);
      this.setKey(key);
   }


   public RemoveRequest(final String cacheName, final Binary key) {

      super(TYPE_CACHE_REMOVE_REQUEST, cacheName, false, false);
      this.setKey(key);
   }


   protected ProcessingResult processKey(final Bucket bucket, final Binary key) {

      // Execute
      final PreviousValue previousValue = bucket.remove(key);

      // Create result
      final Binary modifiedKey = previousValue.isPreviousValuePresent() ? key : null;

      // We need to send the previous value back of only for primary updates
      if (isPrimaryRequest()) {

         return new ProcessingResult(previousValue.getValue(), modifiedKey);
      } else {

         // Replica update request
         return new ProcessingResult(null, null);
      }
   }


   /**
    * {@inheritDoc}
    */
   public KeyRequest createRequest() {

      return new RemoveRequest(getCacheName(), getKey());
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new RemoveRequest();
      }
   }
}
