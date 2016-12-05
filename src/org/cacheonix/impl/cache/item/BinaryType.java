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

/**
 * Enumeration to define item types.
 *
 * @see BinaryFactory
 */
public final class BinaryType {

   private static final byte CODE_BY_REFERENCE = (byte) 1;

   private static final byte CODE_BY_COPY = (byte) 2;

   private static final byte CODE_BY_COMPRESSED_COPY = (byte) 3;

   public static final BinaryType BY_REFERENCE = new BinaryType(CODE_BY_REFERENCE);

   public static final BinaryType BY_COPY = new BinaryType(CODE_BY_COPY);

   public static final BinaryType BY_COMPRESSED_COPY = new BinaryType(CODE_BY_COMPRESSED_COPY);

   private final byte type;


   private BinaryType(final byte type) {

      this.type = type;
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final BinaryType that = (BinaryType) obj;

      return type == that.type;
   }


   public int hashCode() {

      return (int) type;
   }


   public String toString() {

      return "ItemType{" +
              "type=" + type +
              '}';
   }
}
