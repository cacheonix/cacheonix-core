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
package org.cacheonix.impl.cache.datastore;

import org.cacheonix.cache.datastore.Storable;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.util.logging.Logger;

/**
 * {@inheritDoc}
 */
public final class StorableImpl implements Storable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(StorableImpl.class); // NOPMD


   private final Binary key;

   private final Binary value;


   /**
    * Creates StorableImpl.
    *
    * @param key   a key to store.
    * @param value value to store.
    */
   public StorableImpl(final Binary key, final Binary value) {

      this.key = key;
      this.value = value;
   }


   /**
    * {@inheritDoc}
    */
   public Object getKey() {

      return key.getValue();
   }


   /**
    * {@inheritDoc}
    */
   public Object getValue() {

      return value.getValue();
   }


   public String toString() {

      return "StorableImpl{" +
              "key=" + key +
              ", value=" + value +
              '}';
   }
}
