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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.cacheonix.cache.datasource.DataSource;
import org.cacheonix.cache.datasource.DataSourceObject;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.BinaryStoreElement;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An adapter for {@link DataSource} that is an optional supplier of data to the cache for the case when a key is not in
 * the cache (a cache miss).
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public final class BinaryStoreDataSourceImpl implements BinaryStoreDataSource {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BinaryStoreDataSourceImpl.class); // NOPMD

   private final DataSource userDataSource;

   private final Clock clock;

   private final boolean prefetchEnabled;

   private final PrefetchStage prefetchStage;

   private final PrefetchElementUpdater prefetchElementUpdater;


   public BinaryStoreDataSourceImpl(final Clock clock, final PrefetchElementUpdater prefetchElementUpdater,
                                    final PrefetchStage prefetchStage, final DataSource userDataSource,
                                    final boolean prefetchEnabled) {

      this.prefetchElementUpdater = prefetchElementUpdater;
      this.prefetchEnabled = prefetchEnabled;
      this.prefetchStage = prefetchStage;
      this.userDataSource = userDataSource;
      this.clock = clock;
   }


   /**
    * Returns an <code>BinaryStoreDataSourceObject</code> corresponding the given key.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the key in the cache (cache miss) thus giving
    * the data source a chance to supply the missing key. This method should return <code>null</code> if the data source
    * cannot supply the key.
    *
    * @param key the key this data source should use to look up an object.
    * @return the object corresponding the given key or <code>null</code> if the data source cannot supply the key.
    * @see BinaryStoreDataSourceObject
    */
   public BinaryStoreDataSourceObject get(final Binary key) {

      // Check if the key is null
      if (key.getValue() == null) {

         return null;
      }

      // Setup timer used to calculate time to read
      final Time beginReadingTime = clock.currentTime();

      // Call user data source
      final DataSourceObject userDataSourceObject = userDataSource.get(key.getValue());
      if (userDataSourceObject == null) {
         return null;
      }

      // Calculate time to read
      final Time timeToRead = clock.currentTime().subtract(beginReadingTime);

      // Get user object
      final Serializable userObject = userDataSourceObject.getObject();

      // Return result
      return new BinaryStoreDataSourceObjectImpl(userObject, timeToRead);
   }


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
   public Collection<BinaryStoreDataSourceObject> get(final Collection keys) {

      // Call user data source
      final Time beginReadingTime = clock.currentTime();
      final Collection<DataSourceObject> dataSourceObjects = userDataSource.get(keys);
      if (dataSourceObjects.isEmpty()) {

         return Collections.emptyList();
      }

      // Setup timer used to calculate time to read
      final Time totalTimeToRead = clock.currentTime().subtract(beginReadingTime);

      // Call user data source
      final Time timeToRead = totalTimeToRead.divide(dataSourceObjects.size());

      // Return result
      final ArrayList<BinaryStoreDataSourceObject> result = new ArrayList<BinaryStoreDataSourceObject>(dataSourceObjects.size());
      for (final DataSourceObject userDataSourceObject : dataSourceObjects) {

         final Serializable object = userDataSourceObject.getObject();
         if (object == null) {

            result.add(null);
         } else {

            result.add(new BinaryStoreDataSourceObjectImpl(object, timeToRead));
         }
      }
      return result;
   }


   /**
    * {@inheritDoc}
    */
   public void schedulePrefetch(final BinaryStoreElement element, final Time timeTookToReadFromDataSource) {

      if (!prefetchEnabled) {

         return;
      }

      // Don't schedule if time is null.
      if (timeTookToReadFromDataSource == null) {

         return;
      }

      // Element needs to hold a reference to the prefetch order to be able
      // to cancel it. Create prefetch order and set it to the element.
      final Time expirationTime = element.getExpirationTime();
      final Time lookAheadTime = timeTookToReadFromDataSource.add(timeTookToReadFromDataSource.divide(10));
      final Time prefetchTime = expirationTime.subtract(lookAheadTime);
      final long updateCounter = element.getUpdateCounter();
      final Binary key = element.getKey();
      final PrefetchCommand prefetchCommand = new PrefetchCommandImpl(prefetchElementUpdater, this, key, prefetchTime, updateCounter);
      element.setPrefetchCommand(prefetchCommand);

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled())
         LOG.debug("Scheduling prefetch, prefetchTime: " + prefetchTime + ", expirationTime: " + expirationTime); // NOPMD

      // Schedule prefetch order
      prefetchStage.schedule(prefetchCommand);
   }


   public String toString() {

      return "BinaryStoreDataSourceImpl{" +
              "userDataSource=" + userDataSource +
              ", clock=" + clock +
              ", prefetchEnabled=" + prefetchEnabled +
              ", prefetchStage=" + prefetchStage +
              ", prefetchElementUpdater=" + prefetchElementUpdater +
              '}';
   }
}
