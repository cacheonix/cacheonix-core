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
package org.cacheonix.impl.util.cache;

import java.util.Map.Entry;

import org.cacheonix.impl.cache.local.LocalCache;
import org.cacheonix.impl.util.logging.Logger;

/**
 * EntryImpl s used to return entry set from {@link LocalCache#entrySet()}
 * <p/>
 * Created on Mar 11, 2008 12:17:43 AM
 *
 * @author vimeshev
 */
@SuppressWarnings("RedundantIfStatement")
public final class EntryImpl implements Entry {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EntryImpl.class); // NOPMD


   private final Object key;

   private Object value;


   public EntryImpl(final Object key, final Object value) {

      this.value = value;
      this.key = key;
   }


   public Object getKey() {

      return key;
   }


   public Object getValue() {

      return value;
   }


   /**
    * @noinspection ParameterHidesMemberVariable
    */
   public Object setValue(final Object value) {

      this.value = value;
      return this.value;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof EntryImpl)) {
         return false;
      }

      final EntryImpl entry = (EntryImpl) o;

      if (key != null ? !key.equals(entry.key) : entry.key != null) {
         return false;
      }
      if (value != null ? !value.equals(entry.value) : entry.value != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = value != null ? value.hashCode() : 0;
      result = 31 * result + (key != null ? key.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "EntryImpl{" +
              "key=" + key +
              ", value=" + value +
              '}';
   }
}