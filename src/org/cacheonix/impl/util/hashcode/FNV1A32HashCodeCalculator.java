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
package org.cacheonix.impl.util.hashcode;

import org.cacheonix.impl.util.logging.Logger;

/**
 * FNV1A32HashCodeCalculator
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 13, 2008 3:53:09 PM
 */
final class FNV1A32HashCodeCalculator implements HashCodeCalculator {

   private static final long FNV_32_PRIME = 16777619L;

   private static final long FNV_32_INIT = 2166136261L;

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(FNV1A32HashCodeCalculator.class); // NOPMD

   private static final long serialVersionUID = 0L;

   /**
    * Calculated hash code.
    */
   private long hash;


   /**
    * Initializes the internal data structures.
    */
   public void init() {

      hash = FNV_32_INIT;
   }


   /**
    * Adds value to the hash code.
    *
    * @param value
    */
   public void add(final byte value) {

      hash ^= (long) value;
      hash *= FNV_32_PRIME;
   }


   /**
    * Adds byte array value to the hash code.
    *
    * @param value
    */
   public void add(final byte[] value) {

      if (value == null || value.length == 0) {
         return;
      }
      for (int i = 0; i < value.length; i++) {
         add(value[i]);
      }
   }


   /**
    * Returns accumulated hash code.
    *
    * @return
    */
   public int calculate() {

      return (int) (hash ^ hash >>> 32);
   }


   public String toString() {

      return "FNV1A32HashCodeCalculator{" +
              "hash=" + hash +
              '}';
   }
}
