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

import org.cacheonix.impl.util.logging.Logger;

/**
 * Hash code calculator factory
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 13, 2008 4:03:16 PM
 */
public final class HashCodeCalculatorFactory {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(HashCodeCalculatorFactory.class); // NOPMD


   /**
    * Factory class constructor.
    */
   private HashCodeCalculatorFactory() {

   }


   /**
    * Creates a concrete impeementation of a HashCodeCalculator.
    *
    * @param type
    * @return a concrete impeementation of a HashCodeCalculator.
    */
   public static HashCodeCalculator createCalculator(final HashCodeCalculatorType type) {

      if (type.equals(HashCodeCalculatorType.FNV1A32)) {
         return new FNV1A32HashCodeCalculator();
      } else if (type.equals(HashCodeCalculatorType.MD5)) {
         return new MD5HashCodeCalculator();
      } else {
         throw new IllegalArgumentException("Unknown type");
      }
   }
}
