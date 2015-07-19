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

/**
 * Item that holds a object passsed by copy.
 *
 * @noinspection NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals,
 * NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode, NonFinalFieldReferencedInHashCode
 */
public final class PassByReferenceBinary implements Binary {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private static final long serialVersionUID = 0L;

   private Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);

   private Object reference = null;


   public PassByReferenceBinary() {

   }


   /**
    * Constructor.
    *
    * @param object to wrap.
    * @noinspection PublicConstructorInNonPublicClass
    */
   public PassByReferenceBinary(final Object object) {

      this.reference = object;
   }


   /**
    * {@inheritDoc}
    */
   public Object getValue() {

      return reference;
   }


   public Binary create(final Serializable value) {

      return new PassByReferenceBinary(value);
   }


   public int getWireableType() {

      return TYPE_PASS_BY_REFERENCE_BINARY;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.write((int) serializer.getType());
      serializer.serialize(reference, out);
   }


   public void readWire(final DataInputStream in) throws IOException {

      final byte serializerType = in.readByte();
      serializer = SerializerFactory.getInstance().getSerializer(serializerType);
      reference = serializer.deserialize(in);
   }


   /**
    * {@inheritDoc}
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.writeByte(serializer.getType());
      SerializerUtils.writeObject(out, reference);
   }


   /**
    * {@inheritDoc}
    */
   public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

      final byte serializerType = in.readByte();
      serializer = SerializerFactory.getInstance().getSerializer(serializerType);
      reference = SerializerUtils.readObject(in);
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final PassByReferenceBinary that = (PassByReferenceBinary) obj;


      if (reference == null || that.reference == null) {
         return false;
      }

      if (reference.getClass() != that.reference.getClass()) {
         return false;
      }

      // Array
      if (reference.getClass().isArray()) {
         return Arrays.equals((Object[]) reference, (Object[]) that.reference);
      }

      return reference.equals(that.reference);
   }


   public int hashCode() {

      return reference == null ? 0 : reference.hashCode();
   }


   public String toString() {

      return "PassByReferenceItem{" +
              "reference=" + reference +
              '}';
   }


   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new PassByReferenceBinary();
      }
   }
}
