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
package org.cacheonix.impl.cache.store;

import org.cacheonix.impl.cache.item.Binary;

/**
 * A holder of a previous value. The purpose of this class is to provide clients the information about if the previous
 * value was present.
 */
public final class PreviousValue {

   private final boolean previousValuePresent;

   private final Binary value;


   /**
    * Creates <code>PreviousValue</code>.
    *
    * @param value                a binary value.
    * @param previousValuePresent a flag indicating if a previous value was present even if it is null.
    */
   public PreviousValue(final Binary value, final boolean previousValuePresent) {

      this.previousValuePresent = previousValuePresent;
      this.value = value;
   }


   /**
    * Returns a binary previous value represented by this PreviousValue
    *
    * @return the binary previous value represented by this PreviousValue
    */
   public Binary getValue() {

      return value;
   }


   /**
    * Returns a flag indicating if a previous value was present even if it is null.
    *
    * @return the flag indicating if a previous value was present even if it is null.
    */
   public boolean isPreviousValuePresent() {

      return previousValuePresent;
   }


   public String toString() {

      return "PreviousValue{" +
              "previousValuePresent=" + previousValuePresent +
              ", value=" + value +
              '}';
   }
}
