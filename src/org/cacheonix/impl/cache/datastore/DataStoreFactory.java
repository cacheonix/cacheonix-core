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
package org.cacheonix.impl.cache.datastore;

import java.util.Properties;

import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.exceptions.CacheonixException;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A factory for cache data store.
 *
 * @see DataStore
 */
public final class DataStoreFactory {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DataStoreFactory.class); // NOPMD


   /**
    * The name of the dummy data source class.
    */
   private static final String DUMMY_DATASTORE_NAME = DummyDataStore.class.getName();


   /**
    * Creates data store.
    *
    * @param cacheName
    * @param dataStoreClass
    * @param dataStoreProperties
    * @return
    */
   public DataStore createDataStore(final String cacheName, final String dataStoreClass,
                                    final Properties dataStoreProperties) {

      final DataStore dataStore;
      try {
         // Inform
         final boolean dataStoreBlank = StringUtils.isBlank(dataStoreClass);
         if (!dataStoreBlank) {
            LOG.info("Creating data store for " + cacheName + ": " + dataStoreClass);
         }
         // Create
         final Class clazz = Class.forName(dataStoreBlank ? DUMMY_DATASTORE_NAME : dataStoreClass);
         if (!DataStore.class.isAssignableFrom(clazz)) {
            throw new CacheonixException("Class " + dataStoreClass
                    + " configured as a data store for cache "
                    + cacheName + " does not implement interface DataStore");
         }
         dataStore = (DataStore) clazz.getConstructor().newInstance();
         // Set context
         dataStore.setContext(new DataStoreContextImpl(cacheName, dataStoreProperties));
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheonixException(e);
      }
      return dataStore;
   }

}
