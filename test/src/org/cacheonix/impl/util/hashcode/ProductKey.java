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
package org.cacheonix.impl.util.hashcode;

import java.io.Serializable;

/**
 * Example of an immutable key using HashCode.
 */
public final class ProductKey implements Serializable {

   private static final long serialVersionUID = -9169871706570078961L;

   private final HashCode hashCode = new HashCode(HashCodeType.NORMAL);

   private final int id;

   private final String name;


   public ProductKey(final int id, final String name) {

      this.id = id;
      this.name = name;
      this.hashCode.add(id);
      this.hashCode.add(name);
   }


   public int getID() {

      return id;
   }


   public String getName() {

      return name;
   }


   public int hashCode() {

      return hashCode.getValue();
   }


   /**
    * @noinspection RedundantIfStatement
    */
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ProductKey that = (ProductKey) o;

      if (id != that.id) {
         return false;
      }
      if (!name.equals(that.name)) {
         return false;
      }

      return true;
   }


   public String toString() {

      return "ProductKey{" +
              "hashCode=" + hashCode +
              ", id=" + id +
              ", name='" + name + '\'' +
              '}';
   }
}
