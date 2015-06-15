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
package org.cacheonix.cache.subscriber;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An enumeration of types of updates that <code>EntryModifiedEvent</code> is sent for.
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @noinspection NumericCastThatLosesPrecision
 * @see EntryModifiedEvent#getUpdateType()
 */
public final class EntryModifiedEventType implements Externalizable {

   private static final int ADD_CODE = (int) 1;

   private static final int UPDATE_CODE = (int) 2;

   private static final int REMOVE_CODE = (int) 3;

   private static final int EVICT_CODE = (int) 4;

   private static final int EXPIRE_CODE = (int) 5;

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EntryModifiedEventType.class); // NOPMD

   /**
    * This event type indicates that a cache entry was added.
    *
    * @see EntryModifiedEvent#getUpdateType()
    */
   public static final EntryModifiedEventType ADD = new EntryModifiedEventType(ADD_CODE, "Add");

   /**
    * This event type indicates that a cache entry was updated.
    *
    * @see EntryModifiedEvent#getUpdateType()
    */
   public static final EntryModifiedEventType UPDATE = new EntryModifiedEventType(UPDATE_CODE, "Update");

   /**
    * This event type indicates that a cache entry was removed.
    *
    * @see EntryModifiedEvent#getUpdateType()
    */
   public static final EntryModifiedEventType REMOVE = new EntryModifiedEventType(REMOVE_CODE, "Remove");

   /**
    * This event type indicates that a cache entry was removed to maintain the required cache size (evicted). While
    * receiving notifications about the entry eviction may make sense in some environments, generally we recommend
    * avoiding it as it is often a high-frequency event.
    *
    * @see EntryModifiedEvent#getUpdateType()
    */
   public static final EntryModifiedEventType EVICT = new EntryModifiedEventType(EVICT_CODE, "Evict");

   /**
    * This event type indicates that a cache entry was evicted to maintain the required freshness of the cache
    * (expired). While receiving notifications about the entry expiration may make sense in some environments, generally
    * we recommend avoiding it as it is often a high-frequency event.
    *
    * @see EntryModifiedEvent#getUpdateType()
    */
   public static final EntryModifiedEventType EXPIRE = new EntryModifiedEventType(EXPIRE_CODE, "Expire");

   private int code;

   private String description;


   /**
    * Creates an uninitialized <code>EntryModifiedEventType</code>. This constructor is provided to satisfy
    * implementation requirements for <code>java.io.Externalizable<code>. It must not be called directly.
    */
   public EntryModifiedEventType() {

   }


   /**
    * Creates the enumeration.
    *
    * @param code        type code
    * @param description type description
    */
   private EntryModifiedEventType(final int code, final String description) {

      this.code = code;
      this.description = description;
   }


   /**
    * Returns the int code for this entry update type.
    *
    * @return int code for this entry update type.
    */
   public int getCode() {

      return code;
   }


   /**
    * Compares two enumeration objects.
    *
    * @param obj enumeration object to compare to.
    * @return <code>true</code> if the enumerations are equal.
    */
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final EntryModifiedEventType that = (EntryModifiedEventType) obj;

      return code == that.code;

   }


   /**
    * Returns a hashCode for this enumeration.
    *
    * @return hashCode for this enumeration.
    */
   public int hashCode() {

      return code;
   }


   /**
    * Converts the given code to the entry update type.
    *
    * @param code code
    * @return entry update type.
    * @throws IllegalArgumentException if there is no an entry update type that matches the code.
    */
   public static EntryModifiedEventType toEntryUpdateType(final int code) throws IllegalArgumentException {

      switch (code) {
         case ADD_CODE:
            return ADD;
         case UPDATE_CODE:
            return UPDATE;
         case REMOVE_CODE:
            return REMOVE;
         case EVICT_CODE:
            return EVICT;
         case EXPIRE_CODE:
            return EXPIRE;
         default:
            throw new IllegalArgumentException("Unknown entry update type code: " + code);
      }
   }


   /**
    * {@inheritDoc}
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.writeByte(code);
      SerializerUtils.writeString(description, out);
   }


   /**
    * {@inheritDoc}
    */
   public void readExternal(final ObjectInput in) throws IOException {

      code = in.readByte();
      description = SerializerUtils.readString(in);
   }


   /**
    * {@inheritDoc}
    */
   public String toString() {

      return "EntryModifiedEventType{" +
              description +
              '}';
   }
}
