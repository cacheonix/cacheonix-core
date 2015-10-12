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
package org.cacheonix.impl.cache.store;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.storage.disk.StorageException;

/**
 * Utility methods for binary store.
 */
public final class BinaryStoreUtils {

   private BinaryStoreUtils() {

   }


   /**
    * Gets a value encapsulated by the <code>element</code> taking an account the possibility that the
    * <code>element</code> can be null.
    *
    * @param element the element to get the value from.
    * @return the value encapsulated by the <code>element</code> or <code>null</code> if the <code>element</code> is
    *         null.
    * @throws StorageException if the storage error occured.
    */
   public static Binary getValue(final ReadableElement element) throws StorageException {

      return element == null ? null : element.getValue();
   }
}
