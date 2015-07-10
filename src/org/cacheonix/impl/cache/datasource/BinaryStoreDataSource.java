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
package org.cacheonix.impl.cache.datasource;

import java.util.Collection;

import org.cacheonix.cache.datasource.DataSource;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.BinaryStoreElement;
import org.cacheonix.impl.clock.Time;


/**
 * An adapter for {@link DataSource} that is an optional supplier of data to the cache for the case when a key is not in
 * the cache (a cache miss).
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface BinaryStoreDataSource {

   /**
    * Returns an <code>BinaryStoreDataSourceObject</code> corresponding the given key.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the key in the cache (cache miss) thus giving
    * the data source a chance to supply the missing key. This method should <code>null</code> if the data source cannot
    * supply the key.
    *
    * @param key the key this data source should use to look up an object.
    * @return the object corresponding the given key or <code>null</code> if the data source cannot supply the key.
    * @see BinaryStoreDataSourceObject
    */
   BinaryStoreDataSourceObject get(final Binary key);


   /**
    * Returns a collection of <code>BinaryStoreDataSourceObject</code> corresponding the given collection of keys.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the keys in the cache (cache miss) thus giving
    * the data source a chance to supply missing keys.  The returned collection should contain <code>null</code> at the
    * position of the key that the data source cannot supply.
    *
    * @param keys the collection of keys this data source should use to look up objects.
    * @return the collection of <code>BinaryStoreDataSourceObject</code> corresponding the given collection of keys. The
    *         returned collection should contain <code>null</code> at the position of the key that the data source
    *         cannot supply.
    * @see BinaryStoreDataSourceObject
    */
   Collection<BinaryStoreDataSourceObject> get(final Collection keys);

   /**
    * Schedules prefetch for the element.
    *
    * @param newElement                   an element for that to schedule a prefetch.
    * @param timeTookToReadFromDataSource time took to read from the datasource.
    */
   void schedulePrefetch(BinaryStoreElement newElement, Time timeTookToReadFromDataSource);
}
