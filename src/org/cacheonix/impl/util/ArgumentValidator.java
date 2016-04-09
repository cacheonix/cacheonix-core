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
package org.cacheonix.impl.util;

import java.util.List;

/**
 * Method attribute utils.
 */
public final class ArgumentValidator {

   private ArgumentValidator() {

   }


   public static String validateArgumentNotBlank(final String argValue, final String argDescr) {

      if (StringUtils.isBlank(argValue)) {
         throw new IllegalArgumentException("Argument \"" + argDescr + "\" can not be blank.");
      }
      return argValue;
   }


   public static Object validateArgumentNotNull(final Object argValue, final String argDescr) {

      if (argValue != null) {
         return argValue;
      }
      throw new IllegalArgumentException("Argument \"" + argDescr + "\" can not be null.");
   }


   public static int validateArgumentGTZero(final int argValue, final String argDescr) {

      if (argValue <= 0) {
         throw new IllegalArgumentException("Argument \"" + argDescr + "\" should be greater than zero but it was " + argValue);
      }
      return argValue;
   }


   public static byte validateArgumentGTZero(final byte argValue, final String argDescr) {

      if (argValue <= 0) {
         throw new IllegalArgumentException("Argument \"" + argDescr + "\" should be greater than zero but it was " + argValue);
      }
      return argValue;
   }


   public static List validateArgumentNotEmpty(final List variableValues, final String argDescr) {

      if (variableValues != null && !variableValues.isEmpty()) {
         return variableValues;
      }
      throw new IllegalArgumentException("Argument \"" + argDescr + "\" can not be empty.");
   }


   public static int validateArgumentGEZero(final int argValue, final String argDescr) {

      if (argValue < 0) {
         throw new IllegalArgumentException("Argument \"" + argDescr + "\" should be greater or equal than zero but it was " + argValue);
      }
      return argValue;
   }


   public static void validateArgumentNotZero(final int argValue, final String argDescr) {

      if (argValue == 0) {
         throw new IllegalArgumentException("Argument \"" + argDescr + "\" should be not equal zero but it was");
      }
   }


   /**
    * Validates if the given value is a valid positive integer. If not, IllegalArgumentException is thrown.
    *
    * @param value       to validate
    * @param description of the value.
    * @return valid positive integer equal value.
    * @throws IllegalArgumentException
    */
   public static int validatePositiveInteger(final int value, final String description) {

      if (value <= 0) {
         throw new IllegalArgumentException(description + " should be a positive integer");
      }
      return value;
   }


   /**
    * Validates if the given value is a valid positive integer. If not, IllegalArgumentException is thrown.
    *
    * @param value       to validate
    * @param description of the value.
    * @return valid positive integer equal value.
    * @throws IllegalArgumentException
    */
   public static int validateNonNegativeInteger(final int value, final String description) {

      if (value < 0) {
         throw new IllegalArgumentException(description + " should be a greater or equal zero");
      }
      return value;
   }
}
