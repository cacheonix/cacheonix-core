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
package org.cacheonix.impl.util.hashcode;

import java.io.Serializable;

import org.cacheonix.impl.util.logging.Logger;

/**
 * HashCodeType is an enumeration of available types of {@link HashCode}.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NumericCastThatLosesPrecision
 * @since Apr 13, 2008 3:11:03 PM
 */
public final class HashCodeType implements Serializable {

   /**
    * This field should be changed whenever the internal structure of the class changes.
    */
   private static final long serialVersionUID = 0L;

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(HashCodeType.class); // NOPMD

   /**
    * Normal cache code provides slower but less conflicting hash codes.
    */
   public static final HashCodeType NORMAL = new HashCodeType((byte) 1, "Normal");

   /**
    *
    */
   public static final HashCodeType STRONG = new HashCodeType((byte) 2, "Strong");

   /**
    * A unique enumeration type.
    */
   private final byte type;

   /**
    * Am enumeration description.
    */
   private final String description;


   /**
    * Constructs enumeration.
    *
    * @param type        a unique enumeration code.
    * @param description an enumeration description.
    */
   private HashCodeType(final byte type, final String description) {

      this.type = type;
      this.description = description;
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || !obj.getClass().equals(getClass())) {
         return false;
      }

      final HashCodeType that = (HashCodeType) obj;

      return type == that.type && description.equals(that.description);

   }


   public int hashCode() {

      int result = (int) type;
      result = 29 * result + description.hashCode();
      return result;
   }


   public String toString() {

      return "HashCodeType{" +
              "type=" + type +
              ", description='" + description + '\'' +
              '}';
   }
}
