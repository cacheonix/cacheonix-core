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
import java.io.Serializable;

import org.cacheonix.cache.Cache;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * AtomicReplaceRequest replaces a key from a distributed cache according to specification defined by {@link
 * Cache#replace(Serializable, Serializable, Serializable)}
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferencedInHashCode, NonFinalFieldReferenceInEquals, RedundantIfStatement,
 * UnnecessaryParentheses
 */
public final class AtomicReplaceRequest extends KeyRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AtomicReplaceRequest.class); // NOPMD

   /**
    * A value required to be associated with the specified key for the remove to proceed.
    */
   private Binary oldValue;

   /**
    * A value to set.
    */
   private Binary newValue;


   /**
    * Required by <code>Wireable<code>.
    *
    * @see Wireable
    */
   public AtomicReplaceRequest() {

   }


   public AtomicReplaceRequest(final ClusterNodeAddress sender, final String cacheName, final Binary key,
                               final Binary oldValue, final Binary newValue) {

      super(TYPE_CACHE_ATOMIC_REPLACE_REQUEST, cacheName, false, false);
      this.oldValue = oldValue;
      this.newValue = newValue;
      this.setSender(sender);
      this.setKey(key);
   }


   private AtomicReplaceRequest(final String cacheName, final Binary key, final Binary oldValue, final Binary newValue) {

      super(TYPE_CACHE_ATOMIC_REPLACE_REQUEST, cacheName, false, false);
      this.oldValue = oldValue;
      this.newValue = newValue;
      this.setKey(key);
   }


   protected ProcessingResult processKey(final Bucket bucket, final Binary key) {

      final boolean replaced = bucket.replace(key, oldValue, newValue);

      // Create result
      final Binary modifiedKey = replaced ? key : null;

      // We need to send the previous value back of only for primary updates
      if (isPrimaryRequest()) {

         return new ProcessingResult(Boolean.valueOf(replaced), modifiedKey);
      } else {

         // Replica update request
         return new ProcessingResult(null, null);
      }
   }


   /**
    * {@inheritDoc}
    */
   public KeyRequest createRequest() {

      return new AtomicReplaceRequest(getCacheName(), getKey(), oldValue, newValue);
   }


   /**
    * Returns the value required to be associated with the specified key for the remove to proceed.
    *
    * @return the value required to be associated with the specified key for the remove to proceed.
    */
   Binary getOldValue() {

      return oldValue;
   }


   /**
    * Returns value to be associated with the specified key.
    *
    * @return value to be associated with the specified key.
    */
   Binary getNewValue() {

      return newValue;
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeBinary(out, oldValue);
      SerializerUtils.writeBinary(out, newValue);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      oldValue = SerializerUtils.readBinary(in);
      newValue = SerializerUtils.readBinary(in);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final AtomicReplaceRequest that = (AtomicReplaceRequest) o;

      if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) {
         return false;
      }
      if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
      result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "AtomicReplaceRequest{" +
              "oldValue=" + oldValue +
              ", newValue=" + newValue +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new AtomicReplaceRequest();
      }
   }
}
