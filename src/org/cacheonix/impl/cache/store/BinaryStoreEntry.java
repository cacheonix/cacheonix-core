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

import java.util.Map.Entry;

import org.cacheonix.impl.OperationNotSupportedException;
import org.cacheonix.impl.cache.item.Binary;

/**
 * Serializable map entry.
 */
@SuppressWarnings("RedundantIfStatement")
final class BinaryStoreEntry implements Entry<Binary, Binary> {

   private final Binary key;

   private final Binary value;


   BinaryStoreEntry(final Binary key, final Binary value) {

      this.key = key;
      this.value = value;
   }


   public Binary getKey() {

      return key;
   }


   public Binary getValue() {

      return value;
   }


   public Binary setValue(final Binary value) {

      throw new OperationNotSupportedException("Cacheonix doesn't support Entry.setValue() yet");
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final BinaryStoreEntry that = (BinaryStoreEntry) o;

      if (key != null ? !key.equals(that.key) : that.key != null) {
         return false;
      }
      if (value != null ? !value.equals(that.value) : that.value != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = key != null ? key.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
   }
}
