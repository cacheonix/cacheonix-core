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
package org.cacheonix.impl.cache.local;

import java.io.Serializable;

import org.cacheonix.cache.Cache;
import org.cacheonix.exceptions.OperationNotSupportedException;

/**
 * Serializable map entry.
 */
@SuppressWarnings("RedundantIfStatement")
final class SerializableMapEntry<K extends Serializable, V extends Serializable> implements Cache.Entry<K, V> {

   private final K key;

   private final V value;


   SerializableMapEntry(final K key, final V value) {

      this.key = key;
      this.value = value;
   }


   public K getKey() {

      return key;
   }


   public V getValue() {

      return value;
   }


   public V setValue(final V value) {

      throw new OperationNotSupportedException();
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final SerializableMapEntry that = (SerializableMapEntry) o;

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
