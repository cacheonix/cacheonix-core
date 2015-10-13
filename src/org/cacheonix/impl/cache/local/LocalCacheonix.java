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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cacheonix.ShutdownMode;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.CacheExistsException;
import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.cache.loader.CacheLoader;
import org.cacheonix.cluster.Cluster;
import org.cacheonix.exceptions.OperationNotSupportedException;
import org.cacheonix.impl.AbstractCacheonix;
import org.cacheonix.impl.cache.CacheonixCache;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSource;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSourceFactory;
import org.cacheonix.impl.cache.datastore.DataStoreFactory;
import org.cacheonix.impl.cache.invalidator.CacheInvalidatorFactory;
import org.cacheonix.impl.cache.loader.CacheLoaderFactory;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.cache.storage.disk.StorageFactory;
import org.cacheonix.impl.config.CacheonixConfiguration;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.config.DataSourceConfiguration;
import org.cacheonix.impl.config.DataStoreConfiguration;
import org.cacheonix.impl.config.ElementEventNotification;
import org.cacheonix.impl.config.FixedSizeConfiguration;
import org.cacheonix.impl.config.InvalidatorConfiguration;
import org.cacheonix.impl.config.LRUConfiguration;
import org.cacheonix.impl.config.LoaderConfiguration;
import org.cacheonix.impl.config.LocalCacheConfiguration;
import org.cacheonix.impl.config.LocalCacheStoreConfiguration;
import org.cacheonix.impl.config.LocalConfiguration;
import org.cacheonix.impl.config.OverflowToDiskConfiguration;
import org.cacheonix.impl.config.PropertyConfiguration;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.Shutdownable;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.cache.ObjectSizeCalculator;
import org.cacheonix.impl.util.cache.ObjectSizeCalculatorFactory;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Cache manager is a singleton that creates and provides access to Cacheonix caches.
 * <p/>
 * Each cache is uniquely identified by its name within Cacheonix configuration.
 * <p/>
 * <b>Example:</b>
 * <pre>
 * Cache<String, String> cache = Cacheonix.getInstance().getCacheNode("my.cache");
 * </pre>
 * <b>Configuring Cacheonix</b>
 * <p/>
 * Visit <a href="http://wiki.cacheonix.org/display/CCHNX20/Configuring+Cacheonix">online Cacheonix documentation</a>
 * for information on configuring Cacheonix.
 *
 * @noinspection JavaDoc @see cacheonix.cache.Cache
 */
public final class LocalCacheonix extends AbstractCacheonix {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalCacheonix.class); // NOPMD

   /**
    * An implementation of the cluster object specific to the local, non-distrbuted Cacheonix.
    */
   private final LocalCluster localCluster = new LocalCluster();


   /**
    * Constructor.
    *
    * @param configuration Cacheonix configuration.
    * @throws IOException if I/O error occued.
    */
   public LocalCacheonix(final CacheonixConfiguration configuration) {

      super(configuration);
   }


   /**
    * This method is called once during Cacheonix construction.
    *
    * @param configuration
    * @return
    */
   protected final Map createCacheConfigMap(final CacheonixConfiguration configuration) {

      final Map<String, LocalCacheConfiguration> result = new HashMap<String, LocalCacheConfiguration>(11);
      final LocalConfiguration localConfiguration = configuration.getLocal();
      final List<LocalCacheConfiguration> cacheConfigs = localConfiguration.getLocalCacheConfigurationList();
      for (final LocalCacheConfiguration cacheConfig : cacheConfigs) {

         if (result.containsKey(cacheConfig.getName())) {

            throw new CacheExistsException(cacheConfig.getName());
         }
         result.put(cacheConfig.getName(), cacheConfig);
      }
      return result;
   }


   protected Cache createAndRegisterCache(final String cacheName, final LocalCacheConfiguration cacheConfig,
           final boolean useConfigurationAsTemplate) {

      if (cacheConfig.isTemplate() && !useConfigurationAsTemplate) {

         throw new IllegalArgumentException("A template cannot be created: " + cacheConfig.getName());
      }

      final CacheonixCache result = createLocalCache(cacheName, cacheConfig);
      cacheMap.put(cacheName, result);
      return result;
   }


   private CacheonixCache createLocalCache(final String cacheName, final LocalCacheConfiguration cacheConfig) {


      try {

         // Normal local cache
         final LocalCacheStoreConfiguration cacheStoreConfiguration = cacheConfig.getStore();

         if (LOG.isInfoEnabled()) {
            LOG.info("Creating: " + cacheName);
         }

         // Create diskStorage
         final OverflowToDiskConfiguration overflowToDiskConfiguration = cacheStoreConfiguration.getOverflowToDiskConfiguration();
         final long adjustedOverflowSizeMBytes = overflowToDiskConfiguration == null ? 0L : overflowToDiskConfiguration.getMaxOverflowBytes();
         final String tempDir = cacheConfig.getLocalConfiguration().getCacheonixConfiguration().getTempDir().getPath();
         final String storageFile = tempDir + File.separatorChar + ConfigurationConstants.STORAGE_FILE_PREFIX + cacheName + ConfigurationConstants.STORAGE_FILE_EXTENSION;
         final DiskStorage diskStorage = StorageFactory.createStorage(cacheName, adjustedOverflowSizeMBytes,
                 storageFile);

         // Create object size calculator
         final FixedSizeConfiguration fixedSize = cacheStoreConfiguration.getFixed();
         final LRUConfiguration lruSize = cacheStoreConfiguration.getLru();
         final long maxBytes = fixedSize != null ? fixedSize.getMaxBytes() : lruSize.getMaxBytes();
         final ObjectSizeCalculatorFactory calculatorFactory = new ObjectSizeCalculatorFactory();
         final ObjectSizeCalculator objectSizeCalculator = calculatorFactory.createSizeCalculator(maxBytes);

         // Calculate max size elements
         final long maxElements = lruSize != null ? lruSize.getMaxElements() : 0;

         // Calculate times
         final long expirationTimeMillis = cacheStoreConfiguration.getExpiration().getTimeToLiveMillis();
         final long idleTimeMillis = cacheStoreConfiguration.getExpiration().getIdleTimeMillis();


         // Create dataSource
         final DataSourceConfiguration dataSourceConfiguration = cacheStoreConfiguration.getDataSource();
         final String dataSourceClass = dataSourceConfiguration == null ? null : dataSourceConfiguration.getClassName();
         final Properties dataSourceProperties = dataSourceConfiguration == null ? new Properties() : PropertyConfiguration.toProperties(
                 dataSourceConfiguration.getParams());
         final BinaryStoreDataSourceFactory binaryStoreDataSourceFactory = new BinaryStoreDataSourceFactory();
         final boolean prefetchEnabled = dataSourceConfiguration != null && dataSourceConfiguration.isPrefetchConfigurationSet() && dataSourceConfiguration.getPrefetchConfiguration().isEnabled();
         final LocalPrefetchElementUpdater prefetchElementUpdater = new LocalPrefetchElementUpdater();
         final BinaryStoreDataSource dataSource = binaryStoreDataSourceFactory.createDataSource(clock, cacheName,
                 dataSourceClass, dataSourceProperties, prefetchEnabled, getPrefetchScheduler(),
                 prefetchElementUpdater);

         // Create dataStore
         final DataStoreConfiguration dataStoreConfiguration = cacheStoreConfiguration.getDataStore();
         final String dataStoreClass = dataStoreConfiguration == null ? null : dataStoreConfiguration.getClassName();
         final Properties dataStoreProperties = dataStoreConfiguration == null ? new Properties() : PropertyConfiguration.toProperties(
                 dataStoreConfiguration.getParams());
         final DataStoreFactory dataStoreFactory = new DataStoreFactory();
         final DataStore dataStore = dataStoreFactory.createDataStore(cacheName, dataStoreClass, dataStoreProperties);

         // Create invalidator
         final InvalidatorConfiguration invalidatorConfiguration = cacheStoreConfiguration.getInvalidator();
         final String invalidatorClass = invalidatorConfiguration == null ? null : invalidatorConfiguration.getClassName();
         final Properties invalidatorProperties = invalidatorConfiguration == null ? new Properties() : PropertyConfiguration.toProperties(
                 invalidatorConfiguration.getParams());
         final CacheInvalidatorFactory invalidatorFactory = new CacheInvalidatorFactory();
         final CacheInvalidator invalidator = invalidatorFactory.createInvalidator(cacheName, invalidatorClass,
                 invalidatorProperties);

         // Create loader.
         final LoaderConfiguration loaderConfiguration = cacheStoreConfiguration.getLoader();
         final String loaderClass = loaderConfiguration == null ? null : loaderConfiguration.getClassName();
         final Properties loaderProperties = loaderConfiguration == null ? new Properties() : PropertyConfiguration.toProperties(
                 loaderConfiguration.getParams());
         final CacheLoaderFactory loaderFactory = new CacheLoaderFactory();
         final CacheLoader loader = loaderFactory.createLoader(cacheName, loaderClass, loaderProperties);

         // Get event configuration
         final ElementEventNotification elementEventNotification = cacheStoreConfiguration.getElementEvents().getNotification();

         // Create cache
         final LocalCache result = new LocalCache(cacheName, maxElements, maxBytes, expirationTimeMillis,
                 idleTimeMillis, clock, getEventNotificationExecutor(), diskStorage, objectSizeCalculator,
                 dataSource, dataStore, invalidator, loader, elementEventNotification);

         prefetchElementUpdater.setLocalCache(result);

         if (LOG.isDebugEnabled()) {
            LOG.debug("Created: " + result);
         }
         return result;
      } catch (final StorageException e) {

         throw new IllegalStateException("Error while creating a local cache: " + e, e);
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation returns string <code>"Cluster node"</code>  suffixed with the summary of the cluster
    * configuration.
    */
   protected String getDescription() {

      return "Local cache manager";
   }


   protected Cache createWaitCache(final String cacheName) {

      throw new OperationNotSupportedException("Local Cacheonix does not support " +
              "creating caches with a delayed arrival of a configuration");
   }


   protected final void doStartup() {

      // Create and startup local caches
      for (final Object o : cacheConfigMap.values()) {

         final LocalCacheConfiguration cacheConfig = (LocalCacheConfiguration) o;

         if (!cacheConfig.isTemplate()) {

            createAndRegisterCache(cacheConfig.getName(), cacheConfig, false);
         }
      }
   }


   public Cluster getCluster() {

      return localCluster;
   }


   @SuppressWarnings("ThrowableInstanceNeverThrown")
   public void shutdown(final ShutdownMode shutdownMode, final boolean unregisterSingleton) {

      LOG.info("Shutting down " + getDescription());
      if (SystemProperty.isPrintStacktraceAtCacheonixShutdown()) {
         LOG.debug("Stack trace at Cacheonix shutdown", new Throwable());
      }
      writeLock.lock();
      try {
         if (!shutdown) {

            // Unregister shutdown hook
            unregisterShutdownHook();

            // Shutdown items that we are responsible for
            for (final Object o : cacheMap.values()) {
               try {
                  ((Shutdownable) o).shutdown();
               } catch (final Exception e) {
                  ExceptionUtils.ignoreException(e, "Shutdown process");
               }
            }

            // Shutdown prefetch scheduler
            getPrefetchScheduler().shutdown();

            // Shutdown thread pool
            getThreadPoolExecutor().shutdownNow();

            // Destroy timer
            getTimer().cancel();

            // Terminate event notification executor
            getEventNotificationExecutor().shutdownNow();

            // Remove itself from the cache manager map
            shutdown = true;
         }

         // Remove it from the static cache manager registry
         if (unregisterSingleton) {
            unregister(this);
         }

         LOG.info(getDescription() + " has been shutdown");
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * @param cacheName    name of the cache to create.
    * @param templateName name of the template to use when creating the cache.
    * @return
    * @throws IllegalArgumentException
    * @throws StorageException
    */
   public final Cache createCache(final String cacheName, final String templateName)
           throws IllegalArgumentException {

      writeLock.lock();
      try {

         if (cacheExists(cacheName)) {

            throw new IllegalArgumentException("Cache already exists: " + cacheName);
         }

         // Find existing configuration
         final LocalCacheConfiguration localCacheConfiguration = (LocalCacheConfiguration) cacheConfigMap.get(
                 cacheName);
         if (localCacheConfiguration != null) {

            return createAndRegisterCache(cacheName, localCacheConfiguration, false);
         }

         // Use template configuration
         final LocalCacheConfiguration defaultConfiguration = (LocalCacheConfiguration) cacheConfigMap.get(
                 templateName);
         if (defaultConfiguration == null) {
            throw new IllegalArgumentException(
                    "Cache configuration not found and default configuration template is not set: " + cacheName);
         }
         return createAndRegisterCache(cacheName, defaultConfiguration, true);
      } finally {
         writeLock.unlock();
      }
   }
}