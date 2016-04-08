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

import java.util.Collection;

/**
 * An optional write-through data store that isolates the application from the details of how the data is stored.
 * <p/>
 * <code>DataStore</code> API allows developers to save the data that was put to the cache in a database or in other
 * external data storage. <code>DataStore</code> API represents a writing part of data grid functionality in Cacheonix.
 * <p/>
 * The cache data store is configured by supplying a name of the class implementing <code>DataStore</code> in the
 * <code>dataStore</code> attribute of the <code>store</code> element in <a href="http://wiki.cacheonix.org/display/CCHNX20/Configuring+Cacheonix">cacheonix-config.xml</a>.
 * <p/>
 * Classes implementing <code>DataStore</code> must provide a public or an un-protected no-argument constructor.
 * <p/>
 * <b>Example:</b>
 * <pre>
 *  &lt;partitionedCache name="property.cache"&gt;
 *      &lt;store&gt;
 *          &lt;lru maxElements="1000" maxBytes="10mb"/&gt;
 *          &lt;expiration timeToLive="5s"/&gt;
 *          &lt;<b>dataStore className="org.cacheonix.examples.ConfigurationManagerDataStore"</b>/&gt;
 *      &lt;/store&gt;
 *  &lt;/localCache&gt;
 * </pre>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface DataStore {

   /**
    * Sets cache data store context. Cacheonix will call this method next after creating an instance of the class
    * implementing <code>DataStore</code>.
    *
    * @param context an instance of {@link DataStoreContext}
    */
   void setContext(final DataStoreContext context);


   /**
    * Stores the given <code>Storable</code> object. Cacheonix calls this method after any write operation.
    * <p/>
    * Depending on the configuration, this method maybe called synchronously or asynchronously, with or without respect
    * to causality or time of the cache write operation.
    *
    * @param storable an object to store.
    * @noinspection OverloadedMethodsWithSameNumberOfParameters
    */
   void store(final Storable storable);


   /**
    * Stores the given collection of <code>Storable</code> objects.
    * <p/>
    * Depending on the configuration, this method maybe called synchronously or asynchronously, with or without respect
    * to causality or time of the cache write operation. The content of the collection maybe reordered depending on the
    * configuration.
    *
    * @param storables a collection of <code>Storable</code> objects.
    * @noinspection OverloadedMethodsWithSameNumberOfParameters
    * @see Storable
    */
   void store(final Collection storables);
}
