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

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * An entry returned by GetAllRequest.
 */
@SuppressWarnings("RedundantIfStatement")
public final class CacheableEntry implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * A key.
    */
   private Binary key;

   /**
    * A cacheable value.
    */
   private CacheableValue value;


   /**
    * Required by wireable.
    */
   public CacheableEntry() {

   }


   /**
    * Creates a new CacheableEntry.
    *
    * @param key   the key.
    * @param value the value.
    */
   public CacheableEntry(final Binary key, final CacheableValue value) {

      this.key = key;
      this.value = value;
   }


   /**
    * Returns the key.
    *
    * @return the key.
    */
   public Binary getKey() {

      return key;
   }


   /**
    * Returns the value.
    *
    * @return the value.
    */
   public CacheableValue getValue() {

      return value;
   }


   /**
    * {@inheritDoc}
    *
    * @return <code>Wireable.TYPE_CACHEABLE_ENTRY</code>
    */
   public int getWireableType() {

      return Wireable.TYPE_CACHEABLE_ENTRY;
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      SerializerUtils.writeBinary(out, key);
      SerializerUtils.writeCacheableValue(out, value);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      key = SerializerUtils.readBinary(in);
      value = SerializerUtils.readCacheableValue(in);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }

      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final CacheableEntry that = (CacheableEntry) o;

      if (key != null ? !key.equals(that.key) : that.key != null) {
         return false;
      }

      if (value != null ? !value.equals(that.value) : that.value != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = key != null ? key.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
   }


   /**
    * {@inheritDoc}
    */
   public String toString() {

      return "CacheableEntry{" +
              "key=" + key +
              ", value=" + value +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new CacheableEntry();
      }
   }
}
