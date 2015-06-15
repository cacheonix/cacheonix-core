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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Cache contains key request is sent by the distributed cache when the key is stored remotely.
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferencedInHashCode, NonFinalFieldReferenceInEquals, RedundantIfStatement,
 * UnnecessaryParentheses
 * @see PartitionedCache#containsKey(Object) (Object)
 */
public final class ContainsKeyRequest extends KeyRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ContainsKeyRequest.class); // NOPMD


   /**
    * Required by <code>Wireable<code>.
    *
    * @see Wireable
    */
   public ContainsKeyRequest() {

   }


   public ContainsKeyRequest(final String cacheName, final Binary key) {

      super(TYPE_CACHE_CONTAINS_REQUEST, cacheName, true, true);
      this.setKey(key);
   }


   protected ProcessingResult processKey(final Bucket bucket, final Binary key) {

      // Execute and set result
      final boolean containsKey = bucket.containsKey(key);
      return new ProcessingResult(containsKey, null);
   }


   /**
    * {@inheritDoc}
    */
   public KeyRequest createRequest() {

      return new ContainsKeyRequest(getCacheName(), getKey());
   }


   public String toString() {

      return "ContainsKeyRequest{" +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ContainsKeyRequest();
      }
   }
}
