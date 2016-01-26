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
package org.cacheonix.impl.cache.item;

/**
 * Creates compressed item.
 *
 * @see CompressedBinary
 */
final class CompressedBinaryFactory implements BinaryFactory {

   /**
    * {@inheritDoc}
    */
   public Binary createItem() {

      return new CompressedBinary();
   }


   /**
    * {@inheritDoc}
    */
   public Binary createBinary(final Object object) throws InvalidObjectException {

      return object == null ? NULL_BINARY : new CompressedBinary(object);
   }


   /**
    * {@inheritDoc}
    */
   public String toString() {

      return "CompressedItemFactory{}";
   }
}
