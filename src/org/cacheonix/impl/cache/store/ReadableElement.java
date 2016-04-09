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
package org.cacheonix.impl.cache.store;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.clock.Time;

/**
 * A read-only binary store element. This element is attached to an element stored in the binary store.
 */
public interface ReadableElement {

   /**
    * Returns this store element value.
    *
    * @return the value of the element.
    * @throws StorageException if a storage error occured.
    */
   Binary getValue() throws StorageException;

   /**
    * Returns time this element expires.
    *
    * @return time this element expires.
    */
   Time getExpirationTime();
}
