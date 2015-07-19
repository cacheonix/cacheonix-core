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
package org.cacheonix.impl.cache.datasource;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.cacheonix.cache.datasource.DataSource;
import org.cacheonix.exceptions.CacheonixException;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A factory for cache data source.
 *
 * @see BinaryStoreDataSource
 */
public final class BinaryStoreDataSourceFactory {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BinaryStoreDataSourceFactory.class); // NOPMD

   private static final DummyBinaryStoreDataSource DUMMY_DATASOURCE = new DummyBinaryStoreDataSource();


   /**
    * Creates data store.
    *
    * @param clock                  a clock
    * @param cacheName              a cache name.
    * @param dataSourceClass        a data source class name.
    * @param dataSourceProperties   user-supplied data source properties.
    * @param prefetchEnabled        true if the data store should pre-fetch the data.
    * @param prefetchScheduler      a pre-fetch scheduler.
    * @param prefetchElementUpdater a updater of an element for that a newer version is found.
    * @return a new data source.
    */
   @SuppressWarnings("MethodMayBeStatic")
   public BinaryStoreDataSource createDataSource(final Clock clock, final String cacheName,
                                                 final String dataSourceClass, final Properties dataSourceProperties,
                                                 final boolean prefetchEnabled,
                                                 final PrefetchStage prefetchScheduler,
                                                 final PrefetchElementUpdater prefetchElementUpdater) {

      try {

         // Inform
         if (StringUtils.isBlank(dataSourceClass)) {

            return DUMMY_DATASOURCE;
         } else {

            LOG.info("Creating data source for " + cacheName + ": " + dataSourceClass);
         }

         // Create user data source
         final DataSource userDataSource = createUserDataSource(cacheName, dataSourceClass);

         // Set context
         final DataSourceContextImpl userDataSourceContext = new DataSourceContextImpl(cacheName, dataSourceProperties);
         userDataSource.setContext(userDataSourceContext);

         // Create Cacheonix binary store data source
         return new BinaryStoreDataSourceImpl(clock, prefetchElementUpdater, prefetchScheduler, userDataSource, prefetchEnabled);
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheonixException(e);
      }
   }


   /**
    * Creates a user-supplied data source.
    *
    * @param cacheName       a cache name.
    * @param dataSourceClass a user-supplied data source class name.
    * @return a new user-supplied data source.
    * @throws ClassNotFoundException if the class cannot be located.
    * @throws InstantiationException if this <code>dataSourceClass</code> represents an abstract class, an interface, an
    *                                array class, a primitive type, or void; or if the class has no nullary constructor;
    *                                or if the instantiation fails for some other reason.
    * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
    */
   private static DataSource createUserDataSource(final String cacheName,
                                                  final String dataSourceClass)
           throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

      // Load class
      final Class clazz = Class.forName(dataSourceClass);

      // Create new instance.
      if (DataSource.class.isAssignableFrom(clazz)) {

         return (DataSource) clazz.getConstructor().newInstance();
      }

      throw new CacheonixException("Class " + dataSourceClass
              + " configured as a data source for cache "
              + cacheName + " does not implement interface cacheonix.cache.datasource.DataSource");
   }
}
