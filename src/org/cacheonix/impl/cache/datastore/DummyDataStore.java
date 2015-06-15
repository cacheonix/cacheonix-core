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

import java.util.Collection;

import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.datastore.DataStoreContext;
import org.cacheonix.cache.datastore.Storable;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Dummy cache data store.
 */
public final class DummyDataStore implements DataStore {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DummyDataStore.class); // NOPMD


   /**
    * {@inheritDoc}
    */
   public void setContext(final DataStoreContext context) {

   }


   /**
    * {@inheritDoc}
    * <p/>
    * This impementation of the method does nothing.
    */
   public void store(final Storable storable) {

   }


   /**
    * {@inheritDoc}
    * <p/>
    * This impementation of the method does nothing.
    */
   public void store(final Collection storables) {

   }


   public String toString() {

      return "DummyDataStore{" +
              '}';
   }
}
