/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.cache.Cache;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * AtomicRemoveRequest removes a key from a distributed cache according to specification defined by {@link
 * Cache#remove(Object)}.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferencedInHashCode, NonFinalFieldReferenceInEquals, RedundantIfStatement,
 * UnnecessaryParentheses
 */
public final class AtomicRemoveRequest extends KeyRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AtomicRemoveRequest.class); // NOPMD

   /**
    * A value required to be associated with the specified key for the remove to proceed.
    */
   private Binary value = null;


   /**
    * Required by <code>Wireable<code>.
    *
    * @see Wireable
    */
   public AtomicRemoveRequest() {

   }


   public AtomicRemoveRequest(final ClusterNodeAddress sender, final String cacheName, final Binary key,
                              final Binary value) {

      super(TYPE_CACHE_ATOMIC_REMOVE_REQUEST, cacheName, false, false);
      this.value = value;
      this.setSender(sender);
      this.setKey(key);
   }


   private AtomicRemoveRequest(final String cacheName, final Binary key, final Binary value) {

      super(TYPE_CACHE_ATOMIC_REMOVE_REQUEST, cacheName, false, false);
      this.value = value;
      this.setKey(key);
   }


   protected ProcessingResult processKey(final Bucket bucket, final Binary key) {

      final boolean removed = bucket.remove(key, value);

      // Create result
      final Binary modifiedKey = removed ? key : null;

      // We need to send the previous value back of only for primary updates
      if (isPrimaryRequest()) {

         return new ProcessingResult(Boolean.valueOf(removed), modifiedKey);
      } else {

         // Replica update request
         return new ProcessingResult(null, null);
      }
   }


   /**
    * {@inheritDoc}
    */
   public KeyRequest createRequest() {

      return new AtomicRemoveRequest(getCacheName(), getKey(), value);
   }


   /**
    * Returns the value required to be associated with the specified key for the remove to proceed.
    *
    * @return the value required to be associated with the specified key for the remove to proceed.
    */
   Binary getValue() {

      return value;
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeBinary(out, value);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      value = SerializerUtils.readBinary(in);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final AtomicRemoveRequest that = (AtomicRemoveRequest) o;

      if (value != null ? !value.equals(that.value) : that.value != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "AtomicRemoveRequest{" +
              "value=" + value +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new AtomicRemoveRequest();
      }
   }
}
