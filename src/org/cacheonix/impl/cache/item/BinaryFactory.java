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
 * Factory for creating an item according to it's storage type.
 */
public interface BinaryFactory {

   Binary NULL_BINARY = new NullBinary();

   /**
    * Creates item from a raw object value.
    *
    * @param object to create an item from.
    * @return new item.
    * @throws InvalidObjectException if the given object cannot be stored in an item.
    */
   Binary createBinary(final Object object) throws InvalidObjectException;
}
