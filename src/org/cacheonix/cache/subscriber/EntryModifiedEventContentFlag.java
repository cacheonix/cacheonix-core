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
package org.cacheonix.cache.subscriber;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An enumeration of flags to use to determine what information should be provided by <code>EntryModifiedEvent</code>.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NumericCastThatLosesPrecision
 * @see EntryModifiedSubscriber#getEventContentFlags()
 */
public final class EntryModifiedEventContentFlag implements Externalizable {


   private static final long serialVersionUID = 2066116659243442125L;

   private static final int CODE_NEED_KEY = 1;

   private static final int CODE_NEED_NEW_VALUE = 2;

   private static final int CODE_NEED_PREVIOUS_VALUE = 3;

   private static final int CODE_NEED_ALL = 4;


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EntryModifiedEventContentFlag.class); // NOPMD

   /**
    * Event should supply the key that value was updated.
    */
   public static final EntryModifiedEventContentFlag NEED_KEY = new EntryModifiedEventContentFlag(CODE_NEED_KEY, "Need key");

   /**
    * Event should supply the new value.
    */
   public static final EntryModifiedEventContentFlag NEED_NEW_VALUE = new EntryModifiedEventContentFlag(CODE_NEED_NEW_VALUE, "Need new value");

   /**
    * Event should supply the value before the update.
    */
   public static final EntryModifiedEventContentFlag NEED_PREVIOUS_VALUE = new EntryModifiedEventContentFlag(CODE_NEED_PREVIOUS_VALUE, "Need previous value");

   /**
    * Event should supply the key, the new value and the previous value.
    */
   public static final EntryModifiedEventContentFlag NEED_ALL = new EntryModifiedEventContentFlag(CODE_NEED_ALL, "Need all");

   private int code;

   private String description;


   /**
    * Creates an uninitialized <code>EntryModifiedEventContentFlag</code>. This constructor is provided to satisfy
    * implementation requirements for <code>java.io.Externalizable<code>. It must not be called directly.
    */
   public EntryModifiedEventContentFlag() {

   }


   /**
    * Enumeration constructor.
    *
    * @param code        enumeration code
    * @param description enumeration description
    */
   private EntryModifiedEventContentFlag(final int code, final String description) {

      this.code = code;
      this.description = description;
   }


   /**
    * Returns a unique enumeration code.
    *
    * @return the unique enumeration code.
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

      final EntryModifiedEventContentFlag that = (EntryModifiedEventContentFlag) obj;

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
    * Converts an <code>int</code> code to an instance of <code>EntryModifiedEventContentFlag</code>.
    *
    * @param eventFlagCode an int code to convert to an instance of <code>EntryModifiedEventContentFlag</code>.
    * @return an instance of <code>EntryModifiedEventContentFlag</code> corresponding the code. If the code cannot be
    *         matched, returns  <code>EntryModifiedEventContentFlag.NEED_ALL</code>
    */
   public static EntryModifiedEventContentFlag toFlag(final int eventFlagCode) {

      switch (eventFlagCode) {

         case CODE_NEED_KEY:

            return NEED_KEY;
         case CODE_NEED_NEW_VALUE:

            return NEED_NEW_VALUE;
         case CODE_NEED_PREVIOUS_VALUE:

            return NEED_PREVIOUS_VALUE;
         case CODE_NEED_ALL:

            return NEED_ALL;
         default:
            return NEED_ALL;
      }
   }


   public String toString() {

      return "EntryModifiedEventContentFlag{" +
              description +
              '}';
   }
}
