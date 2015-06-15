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

import org.cacheonix.impl.util.logging.Logger;

/**
 * CyclicCountDown
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Apr 3, 2008 10:31:48 PM
 */
public final class CyclicCountDown {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CyclicCountDown.class); // NOPMD

   /**
    * Max counter value.
    */
   private final int maxValue;

   /**
    * Current counter value.
    */
   private int value = 0;


   public CyclicCountDown(final int maxValue) {

      this.maxValue = maxValue <= 0 ? 1 : maxValue;
      this.value = maxValue;
   }


   /**
    * Decrements or resets counter.
    *
    * @return counter value.
    */
   public int decrement() {

      if (value <= 0) {
         value = maxValue;
      }
      value--;
      return value;
   }
}
