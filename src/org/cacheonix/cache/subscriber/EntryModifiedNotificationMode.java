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
 * An enumeration of notifications modes for <code>EntryModifiedSubscriber</code>.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NumericCastThatLosesPrecision
 * @see EntryModifiedSubscriber#getNotificationMode()
 */
public final class EntryModifiedNotificationMode implements Externalizable {


   private static final long serialVersionUID = 3233085322940742702L;

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EntryModifiedNotificationMode.class); // NOPMD

   private static final int CODE_SINGLE_UPDATE = 1;

   private static final int CODE_BATCH_UPDATE = 2;

   /**
    * Notifications about entry updates are delivered as they occur.
    *
    * @see EntryModifiedSubscriber#getNotificationMode()
    */
   public static final EntryModifiedNotificationMode SINGLE = new EntryModifiedNotificationMode(CODE_SINGLE_UPDATE, "Single update");

   /**
    * Enables batching of notifications about entry updates.
    *
    * @see EntryModifiedSubscriber#getNotificationMode()
    */
   public static final EntryModifiedNotificationMode BATCH = new EntryModifiedNotificationMode(CODE_BATCH_UPDATE, "Batch update");

   private int code;

   private String description;


   /**
    * Creates an uninitialized <code>EntryModifiedNotificationMode</code>. This constructor is provided to satisfy
    * implementation requirements for <code>java.io.Externalizable<code>. It must not be called directly.
    */
   public EntryModifiedNotificationMode() {

   }


   /**
    * Enumeration constructor.
    *
    * @param code        enumeration code
    * @param description enumeration description
    */
   private EntryModifiedNotificationMode(final int code, final String description) {

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
    * Converts an <code>int</code> code to an instance of <code>EntryModifiedNotificationMode</code>.
    *
    * @param notificationModeCode an int code to convert to an instance of <code>EntryModifiedNotificationMode</code>.
    * @return an instance of <code>EntryModifiedNotificationMode</code> corresponding the code. If the code cannot be
    *         matched, returns  <code>EntryModifiedNotificationMode.SINGLE</code>
    * @see EntryModifiedNotificationMode#SINGLE
    */
   public static EntryModifiedNotificationMode toMode(final int notificationModeCode) {


      switch (notificationModeCode) {

         case CODE_BATCH_UPDATE:

            return BATCH;
         case CODE_SINGLE_UPDATE:

            return SINGLE;

         default:
            return SINGLE;
      }
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

      final EntryModifiedNotificationMode that = (EntryModifiedNotificationMode) obj;

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

      out.writeInt(code);
      SerializerUtils.writeString(description, out);
   }


   /**
    * {@inheritDoc}
    */
   public void readExternal(final ObjectInput in) throws IOException {

      code = in.readInt();
      description = SerializerUtils.readString(in);
   }


   public String toString() {

      return "EntryModifiedNotificationMode{" +
              description +
              '}';
   }
}
