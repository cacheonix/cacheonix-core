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
package org.cacheonix.cache.datasource;

import java.util.Collection;

/**
 * An optional supplier of data to the cache for the case when a key is not in the cache (a cache miss).
 * <p/>
 * <code>DataSource</code> API hides the specifics of reading the data and enables read-through and read-ahead caching
 * in Cacheonix. <code>DataSource</code> API allows developers to program retrieving the data from an external data
 * source such as a database when application objects are not present in the cache. <code>DataSource</code> API
 * represents a reading part of data grid functionality in Cacheonix.
 * <p/>
 * Classes implementing <code>DataSource</code> must provide a public un-protected no-argument constructor. The cache
 * data source is configured by adding <code>dataSource</code> element to the cache configuration.
 * <p/>
 * <b>Example of cacheonix-config.xml that defines a cache backed by a data source:</b>
 * <pre>
 * &lt;?xml version ="1.0"?&gt;
 * &lt;cacheonix xmlns="http://www.cacheonix.org/schema/configuration"
 *            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *            xsi:schemaLocation="http://www.cacheonix.org/schema/configuration http://www.cacheonix.org/schema/cacheonix-config-2.0.xsd"&gt;
 *
 *    &lt;local&gt;
 *
 *       &lt;localCache name="property.cache"&gt;
 *          &lt;store&gt;
 *             &lt;lru maxElements="10" maxBytes="10mb"/&gt;
 *             &lt;expiration timeToLive="1s"/&gt;
 *
 *             &lt;dataSource className="example.MyCacheDataSource"/&gt;
 *
 *          &lt;/store&gt;
 *       &lt;/localCache&gt;
 *    &lt;/local&gt;
 * &lt;/cacheonix&gt;
 * </pre>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface DataSource {

   /**
    * Sets a cache datasource context.
    * <p/>
    * Cacheonix calls <code>setContext()</code> method once in <tt>DataSource</tt> lifetime, immediately after creating
    * an instance of <code>DataSource</code>.
    *
    * @param context an instance of {@link DataSourceContext}
    * @throws DataSourceException if an error occurred while setting the context. Cacheonix recommends implementing
    *                             methods to wrapping checked exceptions that may be thrown in <tt>setContext()</tt>
    *                             into <tt>DataSourceException</tt>.
    */
   void setContext(final DataSourceContext context) throws DataSourceException;


   /**
    * Returns an <code>DataSourceObject</code> corresponding the given key.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the key in the cache (cache miss) thus giving
    * the data source a chance to supply the missing key. This method should return <code>null</code> if the data source
    * cannot supply the key.
    *
    * @param key the key this data source should use to look up an object.
    * @return the object corresponding the given key or <code>null</code> if the data source cannot supply the key.
    * @throws DataSourceException if an error occurred while obtaining an <tt>DataSourceObject</tt> for the key.
    *                             Cacheonix recommends implementing methods to wrapping checked exceptions that may be
    *                             thrown in <tt>get()</tt> into <tt>DataSourceException</tt>.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   DataSourceObject get(final Object key) throws DataSourceException;


   /**
    * Returns a collection of <code>DataSourceObject</code> corresponding the given collection of keys.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the keys in the cache (cache miss) thus giving
    * the data source a chance to supply missing keys.  The returned collection should contain <code>null</code> at the
    * position of the key that the data source cannot supply.
    *
    * @param keys the collection of keys this data source should use to look up objects.
    * @return the collection of <code>DataSourceObjects</code> corresponding the given collection of keys. The returned
    *         collection should contain <code>null</code> at the position of the key that the data source cannot
    *         supply.
    * @throws DataSourceException if an error occurred while obtaining <tt>DataSourceObjects</tt> for the key. Cacheonix
    *                             recommends implementing methods to wrapping checked exceptions that may be thrown in
    *                             <tt>get()</tt> into <tt>DataSourceException</tt>.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   Collection<DataSourceObject> get(final Collection keys) throws DataSourceException;
}
