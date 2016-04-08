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
package org.cacheonix.plugin.hibernate.v32;

import java.util.Properties;

import org.cacheonix.Cacheonix;
import org.cacheonix.ShutdownMode;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;
import org.hibernate.cfg.Environment;

/**
 * Cache provider plugin for Cacheonix.
 * <p/>
 * To configure multiple Hibernate configurations and multiple SessionFactories you need to configure multiple Cacheonix
 * instances. Specify Cacheonix configuration in each Hibernate configuration using the property
 * <code>hibernate.cache.provider_configuration_file_resource_path</code>.
 * <p/>
 * An example to set a Cacheonix configuration called <code>cacheonix-2.xml</code> would be
 * <code>hibernate.cache.provider_configuration_file_resource_path=cacheonix-2.xml</code>. Cacheonix will look for the
 * configuration file in the root of the classpath.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public final class HibernateCacheonixCacheProvider implements CacheProvider {

   /**
    * Number of milliseconds in a second.
    */
   private static final int MILLIS_IN_SECOND = 1000;

   /**
    * Logger.
    */
   private static final Logger LOG = Logger.getLogger(HibernateCacheonixCacheProvider.class);

   /**
    * Default lock timeout if not set
    */
   public static final int DEFAULT_LOCK_TIMEOUT_SECS = 60;

   /**
    * Hibernate property defining lock timeout.
    */
   public static final String PROPERTY_CACHEONIX_LOCK_TIMEOUT = "cacheonix.lock.timeout.secs";


   /**
    * Cacheonix instance to be used to build caches.
    */
   private Cacheonix cacheonix = null;


   /**
    * Default constructor required by Hibernate.
    */
   public HibernateCacheonixCacheProvider() {

      LOG.info("Creating Hibernate cache provider " + getClass().getName());
   }


   /**
    * Configure the cache
    *
    * @param regionName the name of the cache region
    * @param properties configuration settings
    * @throws CacheException
    * @noinspection UnusedCatchParameter
    */
   public Cache buildCache(final String regionName,
                           final Properties properties) throws CacheException {

      int lockTimeoutMillis;
      try {
         lockTimeoutMillis = Integer.parseInt(properties.getProperty(PROPERTY_CACHEONIX_LOCK_TIMEOUT, Integer.toString(DEFAULT_LOCK_TIMEOUT_SECS))) * MILLIS_IN_SECOND;
      } catch (final NumberFormatException e) {
         lockTimeoutMillis = DEFAULT_LOCK_TIMEOUT_SECS * MILLIS_IN_SECOND;
      }
      return new HibernateCacheonixCache(cacheonix, cacheonix.getCache(regionName), lockTimeoutMillis);
   }


   /**
    * Generate a timestamp
    */
   public long nextTimestamp() {
      // REVIEWME: simeshev - 2008-02-17 - Currently it uses
      // local time stamper. Consider effect of it in a
      // clustered environment.
      return Timestamper.next();
   }


   /**
    * Callback to perform any necessary initialization of the underlying cache implementation during SessionFactory
    * construction.
    *
    * @param properties current configuration settings.
    * @noinspection OverlyBroadCatchBlock, ProhibitedExceptionThrown
    */
   public void start(final Properties properties) throws CacheException {

      LOG.info("Starting Cacheonix cache provider");
      try {
         if (cacheonix == null) {
            final String cacheonixConfigPath = properties.getProperty(Environment.CACHE_PROVIDER_CONFIG, Cacheonix.CACHEONIX_XML);
            LOG.info("Using path to cacheonix configuration file or resource: " + cacheonixConfigPath);
            cacheonix = Cacheonix.getInstance(cacheonixConfigPath);
            LOG.info("Started Cacheonix cache provider using path to confiuration file: " + cacheonixConfigPath);
         } else {
            LOG.warn("Cacheonix cache provider has been started already");
         }
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheException(StringUtils.toString(e), e);
      }
   }


   /**
    * Callback to perform any necessary cleanup of the underlying cache implementation during SessionFactory.close().
    */
   public void stop() {

      if (cacheonix != null) {
         cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         cacheonix = null;
      }
   }


   public boolean isMinimalPutsEnabledByDefault() {

      return false; // By default minimal puts are disabled.
   }


   public String toString() {

      return "HibernateCacheonixCacheProvider{" +
              "cacheonix=" + cacheonix +
              '}';
   }
}
