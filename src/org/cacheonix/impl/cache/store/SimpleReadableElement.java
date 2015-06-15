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
package org.cacheonix.impl.cache.store;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;

/**
 * A value object implementing interface ReadableElement.
 */
public final class SimpleReadableElement implements ReadableElement {

   private final Binary value;

   private final Time expirationTime;


   public SimpleReadableElement(final Binary value, final Time expirationTime) {

      this.value = value;
      this.expirationTime = expirationTime;
   }


   public Binary getValue() {

      return value;
   }


   public Time getExpirationTime() {

      return expirationTime;
   }
}
