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
package org.cacheonix.impl.net.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.SecureRandom;

import org.cacheonix.impl.util.logging.Logger;

/**
 * A universally unique identifier (UUID).
 */
@SuppressWarnings("RedundantIfStatement")
public final class UUID implements Externalizable, Comparable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(UUID.class); // NOPMD

   /**
    * Random number generator.
    */
   private static final SecureRandom SECURE_RANDOM = new SecureRandom();

   private long mostSignificantBits;

   private long leastSignificantBits;

   private int hashCode;


   /**
    * Required by java.io.Externalizable.
    */
   public UUID() {

   }


   public static UUID randomUUID() {

      // Generate a random UUID
      final byte[] bytes = new byte[16];
      SECURE_RANDOM.nextBytes(bytes);

      // Return result
      return new UUID(bytes);
   }


   private UUID(final byte[] data) {

      this.mostSignificantBits = calculateMostSignificantBits(data);
      this.leastSignificantBits = calculateLeastSignificantBits(data);
      this.hashCode = calculateHashCode(mostSignificantBits, leastSignificantBits);
   }


   public UUID(final long mostSignificantBits, final long leastSignificantBits) {

      this.mostSignificantBits = mostSignificantBits;
      this.leastSignificantBits = leastSignificantBits;
      this.hashCode = calculateHashCode(mostSignificantBits, leastSignificantBits);
   }


   public long getLeastSignificantBits() {

      return leastSignificantBits;
   }


   public long getMostSignificantBits() {

      return mostSignificantBits;
   }


   private static int calculateHashCode(final long mostSigBits, final long leastSigBits) {

      int result = (int) (mostSigBits ^ (mostSigBits >>> 32));
      result = 31 * result + (int) (leastSigBits ^ (leastSigBits >>> 32));
      return result;
   }


   private static long calculateLeastSignificantBits(final byte[] data) {

      assert data.length == 16;
      long result = 0;
      for (int i = 8; i < 16; i++) {
         result = result << 8 | data[i] & 0xff;
      }
      return result;
   }


   private static long calculateMostSignificantBits(final byte[] data) {

      assert data.length == 16;
      long result = 0;
      for (int i = 0; i < 8; i++) {
         result = result << 8 | data[i] & 0xff;
      }
      return result;
   }


   public int compareTo(final UUID val) {

      return this.mostSignificantBits < val.mostSignificantBits ? -1 : this.mostSignificantBits > val.mostSignificantBits ? 1 : this.leastSignificantBits < val.leastSignificantBits ? -1 : this.leastSignificantBits > val.leastSignificantBits ? 1 : 0;
   }


   public int compareTo(final Object o) {

      if (o instanceof UUID) {

         final UUID other = (UUID) o;
         return this.mostSignificantBits < other.mostSignificantBits ? -1 : this.mostSignificantBits > other.mostSignificantBits ? 1 : this.leastSignificantBits < other.leastSignificantBits ? -1 : this.leastSignificantBits > other.leastSignificantBits ? 1 : 0;
      } else {

         return 1;
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final UUID uuid = (UUID) o;

      if (leastSignificantBits != uuid.leastSignificantBits) {
         return false;
      }

      if (mostSignificantBits != uuid.mostSignificantBits) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return hashCode;
   }


   /**
    * The object implements the writeExternal method to save its contents by calling the methods of DataOutput for its
    * primitive values or calling the writeObject method of ObjectOutput for objects, strings, and arrays.
    *
    * @param out the stream to write the object to
    * @throws IOException Includes any I/O exceptions that may occur
    * @serialData Overriding methods should use this tag to describe the data layout of this Externalizable object. List
    * the sequence of element types and, if possible, relate the element to a public/protected field and/or method of
    * this Externalizable class.
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.writeLong(mostSignificantBits);
      out.writeLong(leastSignificantBits);
   }


   /**
    * The object implements the readExternal method to restore its contents by calling the methods of DataInput for
    * primitive types and readObject for objects, strings and arrays.  The readExternal method must read the values in
    * the same sequence and with the same types as were written by writeExternal.
    *
    * @param in the stream to read data from in order to restore the object
    * @throws IOException            if I/O errors occur
    */
   public void readExternal(final ObjectInput in) throws IOException {

      mostSignificantBits = in.readLong();
      leastSignificantBits = in.readLong();
      hashCode = calculateHashCode(mostSignificantBits, leastSignificantBits);
   }


   public String toString() {

      return "UUID{" +
              "mostSigBits=" + mostSignificantBits +
              ", leastSigBits=" + leastSignificantBits +
              '}';
   }
}

