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

import org.cacheonix.impl.configuration.ConfigurationConstants;

/**
 * Integer utilities.
 */
public final class IntegerUtils {

   /**
    * An integer object cache.
    */
   private static final Integer[] INTEGER_VALUES = initIntegerValues();


   private IntegerUtils() {

   }


   public static Integer valueOf(final int i) {

      if (i >= 0 && i <= ConfigurationConstants.BUCKET_COUNT) {
         return INTEGER_VALUES[i];
      } else {
         return Integer.valueOf(i);
      }
   }


   private static Integer[] initIntegerValues() {

      final int bucketCount = ConfigurationConstants.BUCKET_COUNT;
      final Integer[] integers = new Integer[bucketCount + 1];
      for (int i = 0; i <= bucketCount; i++) {
         integers[i] = Integer.valueOf(i);
      }
      return integers;
   }
}
