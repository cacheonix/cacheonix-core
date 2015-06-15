/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.util;

/**
 * A Mutable boolean.
 */
public final class MutableBoolean {

   /**
    * A mutable boolean value.
    */
   private boolean value = false;


   /**
    * Creates MutableBoolean with value <code>false</code>
    */
   public MutableBoolean() {

   }


   /**
    * Creates MutableBoolean.
    *
    * @param value initial value.
    */
   public MutableBoolean(final boolean value) {

      this.value = value;
   }


   /**
    * Returns a value of this boolean.
    *
    * @return the value of this boolean.
    */
   public boolean get() { // NOPMD

      return value;
   }


   /**
    * Sets a value of this boolean.
    *
    * @param value the value to set.
    */
   public void set(final boolean value) {

      this.value = value;
   }


   @SuppressWarnings("RedundantIfStatement")
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final MutableBoolean that = (MutableBoolean) o;

      if (value != that.value) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return (value ? 1 : 0);
   }


   public String toString() {

      return Boolean.toString(value);
   }
}
