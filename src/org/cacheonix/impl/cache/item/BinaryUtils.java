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
package org.cacheonix.impl.cache.item;

import java.io.Serializable;
import java.util.Set;

import org.cacheonix.exceptions.InvalidParameterException;
import org.cacheonix.impl.util.array.HashSet;

/**
 * A collection of reusable utility methods to simplify work with <code>Binary</code> objects.
 */
public final class BinaryUtils {

   /**
    * A binary factory builder.
    */
   private static final BinaryFactoryBuilder BINARY_FACTORY_BUILDER = new BinaryFactoryBuilder();

   /**
    * Binary factory is used to convert
    */
   private static final BinaryFactory binaryFactory = BINARY_FACTORY_BUILDER.createFactory(BinaryType.BY_COPY);


   /**
    * Utility class constructor.
    */
   private BinaryUtils() {

   }


   /**
    * Converts a Serializable object to a Binary.
    *
    * @param object an object to convert to a binary.
    * @return the Binary result of conversion.
    * @throws InvalidParameterException if the object cannot be converted to a Binary.
    */
   public static Binary toBinary(final Serializable object) throws InvalidParameterException {

      final Binary binary;
      try {

         binary = binaryFactory.createBinary(object);
      } catch (final InvalidObjectException e) {

         throw new InvalidParameterException(e);
      }
      return binary;
   }


   public static Serializable toObject(final Binary binary) {

      if (binary == null) {

         return null;
      }

      //noinspection unchecked
      return (Serializable) binary.getValue();
   }


   public static HashSet<Binary> toBinarySet( // NOPMD void using implementation types like 'HashSet'
                                              final Set<? extends Serializable> set) {

      final HashSet<Binary> result = new HashSet<Binary>(set.size(), 1.0f);

      for (final Serializable serializable : set) {
         result.add(toBinary(serializable));
      }


      return result;
   }


   public static HashSet<Binary> toBinarySet(final Serializable key) { // NOPMD

      final HashSet<Binary> binaryKeySet = new HashSet<Binary>(1);
      binaryKeySet.add(toBinary(key));
      return binaryKeySet;
   }


   public static HashSet<Binary> copy(final HashSet<Binary> keys) { // NOPMD

      if (keys == null) {
         return null;
      }

      if (keys.isEmpty()) {
         return new HashSet<Binary>(0);
      }

      final HashSet<Binary> result = new HashSet<Binary>(keys.size());
      result.addAll(keys);

      return result;
   }
}
