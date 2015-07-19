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
import java.util.ArrayList;
import java.util.Collection;

import org.cacheonix.cache.datasource.DataSource;
import org.cacheonix.cache.datasource.DataSourceContext;
import org.cacheonix.cache.datasource.DataSourceObject;
import org.cacheonix.cache.datasource.SimpleDataSourceObject;
import org.cacheonix.exceptions.CacheonixException;

/**
 * A test data source to support {@link LocalCachePrefetchTest}.
 * <p/>
 * Simply returns the key with a 50 milliseconds delay.
 */
public final class LocalCachePrefetchTestDataSource implements DataSource {


   private static final long DELAY_MILLIS = 50L;


   /**
    * Sets a cache datasource context.
    * <p/>
    * Cacheonix calls <code>setContext()</code> method once in DataSource lifetime, immediately after creating an
    * instance of <code>DataSource</code>.
    *
    * @param context an instance of {@link DataSourceContext}
    */
   public void setContext(final DataSourceContext context) {
      // Do nothing
   }


   /**
    * Returns an <code>DataSourceObject</code> corresponding the given key.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the key in the cache (cache miss) thus giving
    * the data source a chance to supply the missing key. This method should <code>null</code> if the data source cannot
    * supply the key.
    *
    * @param key the key this data source should use to look up an object.
    * @return the object corresponding the given key or <code>null</code> if the data source cannot supply the key.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   public DataSourceObject get(final Object key) {

      // Sleep for the delay ms
      try {

         Thread.sleep(DELAY_MILLIS);
      } catch (final InterruptedException e) {
         throw new CacheonixException(e);
      }

      return new SimpleDataSourceObject((Serializable) key);
   }


   /**
    * Returns a collection of <code>DataSourceObject</code> corresponding the given collection of keys.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the keys in the cache (cache miss) thus giving
    * the data source a chance to supply missing keys.  The returned collection should contain <code>null</code> at the
    * position of the key that the data source cannot supply.
    *
    * @param keys the collection of keys this data source should use to look up objects.
    * @return the collection of <code>DataSourceObject</code> corresponding the given collection of keys. The returned
    *         collection should contain <code>null</code> at the position of the key that the data source cannot
    *         supply.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   public Collection<DataSourceObject> get(final Collection keys) {

      // Prepare result collection
      final Collection<DataSourceObject> result = new ArrayList<DataSourceObject>(keys.size());

      // Call get for each collection
      for (final Object key : keys) {

         result.add(get(key));
      }

      return result;
   }
}
