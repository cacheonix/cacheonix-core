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
package org.cacheonix.impl.configuration;

import java.io.Serializable;

/**
 * Enumeration to define available cache eviction policies.
 */
public final class EvictionPolicy implements Serializable {

   private static final long serialVersionUID = -3436252398538221670L;

   private static final String NAME_LRU = "lru";

   /**
    * @noinspection NumericCastThatLosesPrecision
    */
   private static final byte CODE_LRU = (byte) 0;

   /**
    * <b>L</b>east <b>R</b>ecently <b>U</b>sed eviction policy. When the cache is full, the least recently used item is
    * evicted.
    */
   public static final EvictionPolicy LRU = new EvictionPolicy(NAME_LRU, CODE_LRU);

   /**
    * Unique name of an eviction policy.
    */
   private final String name;

   /**
    * Unique code of an eviction policy.
    */
   private final byte code;


   /**
    * Enumeration constructor.
    *
    * @param name name of the eviction policy.
    * @param code code of the eviction policy.
    * @noinspection SameParameterValue
    */
   private EvictionPolicy(final String name, final byte code) {

      this.name = name;
      this.code = code;
   }


   /**
    * Returns name of the eviction policy.
    *
    * @return name of the eviction policy.
    */
   public String getName() {

      return name;
   }


   /**
    * Returns code of the eviction policy.
    *
    * @return code of the eviction policy.
    */
   public byte getCode() {

      return code;
   }


   /**
    * @noinspection SimplifiableIfStatement
    */
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }
      return code == ((EvictionPolicy) obj).code;
   }


   public int hashCode() {

      return (int) code;
   }


   public String toString() {

      return "EvictionPolicy{" +
              "name='" + name + '\'' +
              ", code=" + code +
              '}';
   }
}
