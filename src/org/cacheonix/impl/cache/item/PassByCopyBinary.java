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
 * Holds value passed by copy.
 *
 * @noinspection NonFinalFieldReferenceInEquals, OverlyBroadCatchBlock, RedundantIfStatement
 */
public final class PassByCopyBinary implements Binary {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private static final long serialVersionUID = 0L;

   /**
    * REVIEWME: slava@cacheonix.org - this should be made a configuration parameter.
    */
   private Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);

   private Object copy = null;


   /**
    * @param value raw Object value.
    * @throws InvalidObjectException if object cannot be serialized.
    * @noinspection PublicConstructorInNonPublicClass
    */
   public PassByCopyBinary(final Object value) throws InvalidObjectException {

      try {
         copy = serializer.deserialize(serializer.serialize(value));
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new InvalidObjectException(e);
      }
   }


   public PassByCopyBinary() {

   }


   /**
    * {@inheritDoc}
    *
    * @noinspection ProhibitedExceptionThrown
    */
   public Object getValue() throws IllegalStateException {

      try {
         return serializer.deserialize(serializer.serialize(copy));
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   public Binary create(final Serializable value) throws InvalidObjectException {

      return new PassByCopyBinary(value);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.write((int) serializer.getType());
      serializer.serialize(copy, out);
   }


   public void readWire(final DataInputStream in) throws IOException {

      final byte serializerType = in.readByte();
      serializer = SerializerFactory.getInstance().getSerializer(serializerType);
      copy = serializer.deserialize(in);
   }


   /**
    * {@inheritDoc}
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.writeByte(serializer.getType());
      SerializerUtils.writeObject(out, copy);
   }


   /**
    * {@inheritDoc}
    */
   public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

      final byte serializerType = in.readByte();
      serializer = SerializerFactory.getInstance().getSerializer(serializerType);
      copy = SerializerUtils.readObject(in);
   }


   public int getWireableType() {

      return TYPE_PASS_BY_COPY_BINARY;
   }


   @Override
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (!(obj instanceof PassByCopyBinary)) {
         return false;
      }

      final PassByCopyBinary that = (PassByCopyBinary) obj;

      if (copy == null && that.copy == null) {
         return true;
      }

      if (copy == null || that.copy == null) {
         return false;
      }

      if (!copy.getClass().equals(that.copy.getClass())) {
         return false;
      }

      // Array
      if (copy.getClass().isArray()) {
         return Arrays.equals((Object[]) copy, (Object[]) that.copy);
      }

      return copy.equals(that.copy);
   }


   @Override
   public int hashCode() {

      return copy == null ? 0 : copy.hashCode();
   }


   @Override
   public String toString() {

      return "PassByCopyBinary{" +
              "copy=" + copy +
              ", serializer=" + serializer +
              '}';
   }


   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new PassByCopyBinary();
      }
   }
}
