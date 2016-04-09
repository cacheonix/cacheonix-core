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

import org.cacheonix.impl.util.logging.Logger;

/**
 * HashCodeCalculatorType
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 13, 2008 4:05:26 PM
 */
public final class HashCodeCalculatorType {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(HashCodeCalculatorType.class); // NOPMD

   /**
    * FNV 1A 32 bit.
    */
   public static final HashCodeCalculatorType FNV1A32 = new HashCodeCalculatorType((byte) 1);

   /**
    * MD5
    */
   public static final HashCodeCalculatorType MD5 = new HashCodeCalculatorType((byte) 2);

   private final byte type;


   private HashCodeCalculatorType(final byte type) {

      this.type = type;
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

      final HashCodeCalculatorType that = (HashCodeCalculatorType) o;

      if (type != that.type) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return (int) type;
   }


   public String toString() {

      return "HashCodeCalculatorType{" +
              "type=" + type +
              '}';
   }
}
