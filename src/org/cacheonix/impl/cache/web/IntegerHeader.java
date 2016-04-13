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
package org.cacheonix.impl.cache.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

import static org.cacheonix.impl.net.serializer.SerializerUtils.readString;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeString;

/**
 * An integer header.
 */
@SuppressWarnings("NegatedConditionalExpression")
public final class IntegerHeader implements Header {

   public static final WireableBuilder BUILDER = new WireableBuilder() {

      public Wireable create() {

         return new IntegerHeader();
      }
   };

   /**
    * The name of the header.
    */
   private String name;

   /**
    * The integer value.
    */
   private int value;


   public IntegerHeader() {

   }


   /**
    * Creates IntegerHeader.
    *
    * @param value the integer value.
    */
   IntegerHeader(final String name, final int value) {

      this.value = value;
      this.name = name;
   }


   /**
    * {@inheritDoc}
    */
   public void addToResponse(final HttpServletResponse httpServletResponse) {

      httpServletResponse.addIntHeader(name, value);
   }


   /**
    * Returns the name of the header.
    *
    * @return the name of the header.
    */
   String getName() {

      return name;
   }


   /**
    * Returns the integer value.
    *
    * @return the integer value.
    */
   int getValue() {

      return value;
   }


   /**
    * Returns index of this object in the object registry.
    *
    * @return index of this object in the object registry.
    */
   public int getWireableType() {

      return TYPE_INTEGER_HEADER;
   }


   /**
    * Writes this wireable object to the wire.
    *
    * @param out data output stream
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      writeString(name, out);
      out.writeInt(value);
   }


   /**
    * Reads this wireable object from the stream.
    *
    * @param in a binary data input
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   public void readWire(final DataInputStream in) throws IOException {

      name = readString(in);
      value = in.readInt();
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final IntegerHeader that = (IntegerHeader) o;

      if (value != that.value) {
         return false;
      }
      return !(name != null ? !name.equals(that.name) : that.name != null);

   }


   public int hashCode() {

      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + value;
      return result;
   }


   public String toString() {

      return "IntegerHeader{" +
              "name='" + name + '\'' +
              ", value=" + value +
              '}';
   }
}
