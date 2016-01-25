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
 * Factory for creating sore-by-reference element value.
 */
final class PassByReferenceBinaryFactory implements BinaryFactory {

   /**
    * {@inheritDoc}
    */
   public Binary createItem() {

      return new PassObjectByReferenceBinary();
   }


   /**
    * {@inheritDoc}
    */
   public Binary createBinary(final Object object) {

      return new PassObjectByReferenceBinary(object);
   }


   /**
    * {@inheritDoc}
    */
   public String toString() {

      return "PassByReferenceItemFactory{}";
   }
}


