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
package org.cacheonix.impl.cache.item;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;

import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.exception.ExceptionUtils;

/**
 * Holds a compressed value passed by copy. Using this item as a key is not recommended because it has to un-compress
 * the value every time <code>equals()</code> or <code>hashCode()</code> is called.
 *
 * @noinspection RedundantIfStatement, NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals,
 * NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals, OverlyBroadCatchBlock
 */
public final class CompressedBinary implements Binary {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private static final long serialVersionUID = 0L;

   /**
    * Service compressor.
    */
   private final Compressor compressor = Compressor.getInstance();

   private String valueClassName;

   /**
    * REVIEWME: slava@cacheonix.org - this should be made a configuration parameter.
    */
   private Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);

   /**
    * Actual object copy.
    */
   private byte[] compressedCopy = null;


   /**
    * Constructor.
    *
    * @param value Raw value to wrap.
    * @throws InvalidObjectException it the raw value cannot be converted to the internal representation.
    * @noinspection PublicConstructorInNonPublicClass
    */
   public CompressedBinary(final Object value) throws InvalidObjectException {

      if (value == null) {

         compressedCopy = null;
      } else {

         valueClassName = value.getClass().getName();

         try {
            final byte[] uncompressedCopy = serializer.serialize(value);
            compressedCopy = compressor.compress(uncompressedCopy);
         } catch (final IOException e) {
            throw new InvalidObjectException(e);
         }
      }
   }


   public CompressedBinary() {

   }


   /**
    * {@inheritDoc}
    *
    * @noinspection ProhibitedExceptionThrown
    */
   public Object getValue() throws IllegalStateException {

      if (compressedCopy == null) {
         return null;
      }
      try {
         final byte[] decompressedCopy = compressor.decompress(compressedCopy);
         return serializer.deserialize(decompressedCopy);
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   public Binary create(final Serializable value) throws InvalidObjectException {

      return new CompressedBinary(value);
   }


   public int getWireableType() {

      return TYPE_COMPRESSED_BINARY;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.write((int) serializer.getType());
      SerializerUtils.writeString(valueClassName, out);
      SerializerUtils.writeByteArray(out, compressedCopy);
   }


   public void readWire(final DataInputStream in) throws IOException {

      final byte serializerType = in.readByte();
      serializer = SerializerFactory.getInstance().getSerializer(serializerType);
      valueClassName = SerializerUtils.readString(in);
      compressedCopy = SerializerUtils.readByteArray(in);
   }


   /**
    * {@inheritDoc}
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.write((int) serializer.getType());
      SerializerUtils.writeString(valueClassName, out);
      SerializerUtils.writeByteArray(out, compressedCopy);
   }


   /**
    * {@inheritDoc}
    */
   public void readExternal(final ObjectInput in) throws IOException {

      final byte serializerType = in.readByte();
      serializer = SerializerFactory.getInstance().getSerializer(serializerType);
      valueClassName = SerializerUtils.readString(in);
      compressedCopy = SerializerUtils.readByteArray(in);
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final CompressedBinary that = (CompressedBinary) obj;
      if (this.compressedCopy == null && that.compressedCopy == null) {
         return true;
      }

      if (this.compressedCopy == null || that.compressedCopy == null) {
         return false;
      }

      if (!this.valueClassName.equals(that.valueClassName)) {
         return false;
      }

      return Arrays.equals(this.compressedCopy, that.compressedCopy);
   }


   public int hashCode() {

      final Object value = getValue();
      return value == null ? 0 : value.hashCode();
   }


   /**
    * @noinspection ImplicitArrayToString
    */
   public String toString() {

      return "CompressedItem{" +
              "compressor=" + compressor +
              ", serializer=" + serializer +
              ", compressedCopy=" + Arrays.toString(compressedCopy) +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new CompressedBinary();
      }
   }
}
