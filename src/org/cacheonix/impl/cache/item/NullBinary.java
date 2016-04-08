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
package org.cacheonix.impl.cache.item;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * Item that holds a null.
 *
 * @noinspection NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals,
 * NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode, NonFinalFieldReferencedInHashCode
 */
public final class NullBinary implements Binary {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private static final long serialVersionUID = 0L;

   /**
    * {@inheritDoc}
    */
   public Object getValue() {

      return null;
   }


   public int getWireableType() {

      return TYPE_NULL_BINARY;
   }


   public void writeWire(final DataOutputStream out) {

   }


   public void readWire(final DataInputStream in) {

   }


   /**
    * {@inheritDoc}
    */
   public void writeExternal(final ObjectOutput out) {

   }


   /**
    * {@inheritDoc}
    */
   public void readExternal(final ObjectInput in) {

   }


   @SuppressWarnings("RedundantIfStatement")
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return 0;
   }


   public String toString() {

      return "NullBinary{}";
   }


   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new NullBinary();
      }

   }
}
