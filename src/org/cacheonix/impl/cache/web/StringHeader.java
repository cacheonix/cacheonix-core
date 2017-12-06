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
import static org.cacheonix.impl.util.StringUtils.isBlank;

/**
 * A String header.
 */
@SuppressWarnings("NegatedConditionalExpression")
public final class StringHeader implements Header {

   public static final WireableBuilder BUILDER = new WireableBuilder() {

      public Wireable create() {

         return new StringHeader();
      }
   };

   /**
    * The name of the header.
    */
   private String name;

   /**
    * The string value.
    */
   private String value;


   public StringHeader() {

   }


   /**
    * Creates a new instance of <code>StringHeader</code>
    *
    * @param value the String value.
    */
   StringHeader(final String name, final String value) {

      this.value = value;
      this.name = name;
   }


   /**
    * {@inheritDoc}
    */
   public void addToResponse(final HttpServletResponse httpServletResponse) {

      httpServletResponse.addHeader(name, value);
   }


   /**
    * {@inheritDoc}
    */
   public boolean containsString(final String s) {

      if (isBlank(value) || isBlank(s)) {
         return false;
      }

      return value.toLowerCase().contains(s.toLowerCase());
   }


   /**
    * {@inheritDoc}
    */
   public boolean startsWith(final String s) {

      if (isBlank(value) || isBlank(s)) {
         return false;
      }

      return value.toLowerCase().startsWith(s.toLowerCase());
   }


   /**
    * Returns the name of the header.
    *
    * @return the name of the header.
    */
   String getName() {

      return name;
   }


   String getValue() {

      return value;
   }


   /**
    * Returns index of this object in the object registry.
    *
    * @return index of this object in the object registry.
    */
   public int getWireableType() {

      return TYPE_STRING_HEADER;
   }


   /**
    * Writes this wireable object to the wire.
    *
    * @param out data output stream
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      writeString(name, out);
      writeString(value, out);
   }


   /**
    * Reads this wireable object from the stream.
    *
    * @param in a binary data input
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   public void readWire(final DataInputStream in) throws IOException {

      name = readString(in);
      value = readString(in);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final StringHeader that = (StringHeader) o;

      if (name != null ? !name.equals(that.name) : that.name != null) {
         return false;
      }
      return !(value != null ? !value.equals(that.value) : that.value != null);

   }


   public int hashCode() {

      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "StringHeader{" +
              "name='" + name + '\'' +
              ", value='" + value + '\'' +
              '}';
   }
}
