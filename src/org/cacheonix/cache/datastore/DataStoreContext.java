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
package org.cacheonix.cache.datastore;

import java.util.Properties;

/**
 * Context for the DataStore. DataStore uses DataStoreContext to access its context data. Cacheonix provides an instance
 * of DataStoreContext next after creating a DataStore by calling {@link DataStore#setContext(DataStoreContext)}.
 *
 * @see Storable
 */
public interface DataStoreContext {

   /**
    * Returns a cache name for the cache data store.
    *
    * @return cache name for the cache data store.
    */
   String getCacheName();


   /**
    * Returns a copy the configuration properties as defined in <code>cacheonix-config.xml</code>.
    * <p/>
    * <b>Example:</b>
    * <pre>
    *   &lt;cache name="my.cache" maxSize="1000"
    *          invalidator="my.project.DataStoreImpl"
    *          <b>dataStoreProperties</b>="connectionURL=my/database/url;user=my_user;password=my_password"/&gt;
    * </pre>
    *
    * @return a copy of the configuration properties for a data store as defined in <code>cacheonix-config.xml</code>.
    */
   Properties getProperties();
}
