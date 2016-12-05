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
package org.cacheonix.impl.cache.item;

/**
 * Creates a factory according to the storage type.
 *
 * @see BinaryFactory
 * @see BinaryType
 */
public final class BinaryFactoryBuilder {

   /**
    * Creates the factory according to config.
    *
    * @param storageType for that to create a factory.
    * @return the item factory corresponding the storage type.
    */
   public BinaryFactory createFactory(final BinaryType storageType) {

      if (storageType.equals(BinaryType.BY_COMPRESSED_COPY)) {

         return decorateAsImmutable(new CompressedBinaryFactory());
      } else if (storageType.equals(BinaryType.BY_COPY)) {

         return decorateAsImmutable(new PassByCopyBinaryFactory());
      } else if (storageType.equals(BinaryType.BY_REFERENCE)) {

         return decorateAsImmutable(new ImmutableBinaryFactory());
      } else {

         throw new IllegalArgumentException("Unknown storage type: " + storageType);
      }
   }


   private static BinaryFactory decorateAsImmutable(final BinaryFactory factory) {

      return new ImmutableBinaryFactoryDecorator(factory);
   }
}
