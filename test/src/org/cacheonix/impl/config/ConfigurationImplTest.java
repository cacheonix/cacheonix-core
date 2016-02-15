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
package org.cacheonix.impl.config;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.TestUtils.getTestFileInputStream;
import static org.cacheonix.impl.config.ConfigurationConstants.CACHE_TEMPLATE_NAME_DEFAULT;
import static org.cacheonix.impl.config.ConfigurationConstants.DEFAULT_LOGGING_LEVEL;
import static org.cacheonix.impl.config.SystemProperty.CACHEONIX_LOGGING_LEVEL;
import static org.cacheonix.impl.config.SystemProperty.NAME_CACHEONIX_LOGGING_LEVEL;

/**
 * Tests {@link CacheonixConfiguration}
 */
@SuppressWarnings("JavaDoc")
public final class ConfigurationImplTest extends TestCase {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ConfigurationImplTest.class); // NOPMD

   private static final int MULTICAST_PORT = 9998;

   private static final String CACHEONIX_CONFIG_CACHEONIX_15_XML = "cacheonix-config-CACHEONIX-15.xml";

   private static final String CACHEONIX_CONFIG_CACHEONIX_45_XML = "cacheonix-config-CACHEONIX-45.xml";

   private static final String CACHEONIX_CONFIG_CACHEONIX_71_XML = "cacheonix-config-CACHEONIX-71.xml";

   private static final String CACHEONIX_CONFIG_CACHEONIX_81_XML = "cacheonix-config-CACHEONIX-81.xml";

   private static final String CACHEONIX_CONFIG_CACHEONIX_99_XML = "cacheonix-config-CACHEONIX-99.xml";

   private static final String CACHEONIX_CONFIG_CACHEONIX_101_XML = "cacheonix-config-CACHEONIX-101.xml";

   private static final String CACHEONIX_CONFIG_CACHEONIX_112_XML = "cacheonix-config-CACHEONIX-112.xml";

   private static final String CACHEONIX_CONFIG_CACHEONIX_115_XML = "cacheonix-config-CACHEONIX-115.xml";

   private static final String CACHEONIX_CONFIG_CACHEONIX_119_XML = "cacheonix-config-CACHEONIX-119.xml";

   private static final String MULTICAST_ADDRESS = "225.0.1.2";

   private static final String TEST_CLUSTER_NAME = "test_cluster_name";


   public void testRead() throws IOException {

      assertNotNull(read(TestConstants.CACHEONIX_CLUSTER_XML));
   }


   public void testGetClusterConfigurations() throws IOException {

      final ServerConfiguration serverConfiguration = read(TestConstants.CACHEONIX_CLUSTER_XML).getServer();
      final MulticastBroadcastConfiguration multicastBroadcast = serverConfiguration.getBroadcastConfiguration().getMulticast();

      assertEquals(100, serverConfiguration.getSelectorTimeoutMillis());
      assertEquals(5000, serverConfiguration.getSocketTimeoutMillis());
      assertEquals(MULTICAST_ADDRESS, StringUtils.toString(multicastBroadcast.getMulticastAddress()));
      assertEquals(MULTICAST_PORT, multicastBroadcast.getMulticastPort());
      assertEquals(8877, serverConfiguration.getListener().getTcp().getPort());
      assertEquals("Cacheonix", serverConfiguration.getClusterConfiguration().getName());
      assertEquals(TimeUnit.SECONDS.toMillis(10L),
              serverConfiguration.getClusterConfiguration().getWorstCaseLatencyMillis());
      assertEquals(100L, serverConfiguration.getClusterConfiguration().getClusterAnnouncementTimeoutMillis());
   }


   public void testGetHomeAloneTimeout() throws IOException {

      final Long systemHomeAloneTimeout = SystemProperty.CACHEONIX_HOME_ALONE_TIMEOUT_VALUE_MILLIS;
      final long expected = systemHomeAloneTimeout != null ? systemHomeAloneTimeout : 55000L;

      assertEquals(expected,
              read(CACHEONIX_CONFIG_CACHEONIX_81_XML).getServer().getClusterConfiguration().getHomeAloneTimeoutMillis());
   }


   public void testGetDefaultHomeAloneTimeout() throws IOException {

      final Long systemHomeAloneTimeout = SystemProperty.CACHEONIX_HOME_ALONE_TIMEOUT_VALUE_MILLIS;
      final long expected = systemHomeAloneTimeout != null ? systemHomeAloneTimeout : 1000;

      final ClusterConfiguration clusterConfiguration = read(
              TestConstants.CACHEONIX_CLUSTER_XML).getServer().getClusterConfiguration();
      assertEquals(expected, clusterConfiguration.getHomeAloneTimeoutMillis());
   }


   public void testGetDefaultClusterSurveyTimeout() throws IOException {

      final Long systemClusterSurveyTimeout = SystemProperty.CACHEONIX_CLUSTER_SURVEY_TIMEOUT_VALUE_MILLIS;
      final long expected = systemClusterSurveyTimeout != null ? systemClusterSurveyTimeout : 500;

      final ClusterConfiguration clusterConfiguration = read(
              TestConstants.CACHEONIX_CLUSTER_XML).getServer().getClusterConfiguration();
      assertEquals(expected, clusterConfiguration.getClusterSurveyTimeoutMillis());
   }


   public void testGetUnlockTimeout() throws IOException {

      assertEquals(ConfigurationConstants.DEFAULT_LOCK_TIMEOUT_MILLIS,
              read(TestConstants.CACHEONIX_CLUSTER_XML).getServer().getDefaultUnlockTimeoutMillis());
      assertEquals(70000L, read(CACHEONIX_CONFIG_CACHEONIX_112_XML).getServer().getDefaultUnlockTimeoutMillis());
   }


   public void testGetDefaultLEase() throws IOException {

      final ServerConfiguration server = read(TestConstants.CACHEONIX_CLUSTER_XML).getServer();
      final PartitionedCacheConfiguration partitionedCache = server.enumeratePartitionedCaches().get(0);
      final PartitionedCacheStoreConfiguration store = partitionedCache.getStore();
      final CoherenceConfiguration coherence = store.getCoherence();
      final LeaseConfiguration lease = coherence.getLease();
      final long leaseTimeMillis = lease.getLeaseTimeMillis();
      assertEquals(5, leaseTimeMillis);
   }


   public void testGetLockTimeout() throws IOException {

      assertEquals(ConfigurationConstants.DEFAULT_LOCK_TIMEOUT_MILLIS,
              read(TestConstants.CACHEONIX_CLUSTER_XML).getServer().getDefaultLockTimeoutMillis());
      assertEquals(70000L, read(CACHEONIX_CONFIG_CACHEONIX_115_XML).getServer().getDefaultLockTimeoutMillis());
   }


   /**
    * Tests CACHEONIX-71: Add partition configuration parameters partitionSizeBytes and partitionContributor.
    */
   public void testGetCacheConfigurationCacheonix71() throws IOException {

      final ConfigurationReader configurationReader = new ConfigurationReader();
      final CacheonixConfiguration configuration71 = configurationReader.readConfiguration(
              getTestFileInputStream(CACHEONIX_CONFIG_CACHEONIX_71_XML));
      final ServerConfiguration serverConf = configuration71.getServer();
      final PartitionedCacheConfiguration cacheConf = serverConf.enumeratePartitionedCaches().get(0);
      assertEquals(99 * 1024, cacheConf.getStore().getLru().getMaxBytes());
      assertTrue(cacheConf.isPartitionContributor());
   }


   /**
    */
   public void testGetCacheConfigurationCacheonix101() throws IOException {

      final ConfigurationReader configurationReader = new ConfigurationReader();
      final CacheonixConfiguration configuration101 = configurationReader.readConfiguration(
              getTestFileInputStream(CACHEONIX_CONFIG_CACHEONIX_101_XML));
      final ServerConfiguration serverConf = configuration101.getServer();
      final PartitionedCacheConfiguration cacheConf = serverConf.enumeratePartitionedCaches().get(0);
      assertEquals(Runtime.getRuntime().maxMemory() / 2, cacheConf.getStore().getLru().getMaxBytes());
   }


   public void testGetLocalConfigurations() throws IOException {

      final LocalConfiguration localConfiguration = read(TestConstants.CACHEONIX_LOCAL_XML).getLocal();
      assertNotNull(localConfiguration.getCacheonixConfiguration());
      final List<LocalCacheConfiguration> cacheConfigurations = localConfiguration.getLocalCacheConfigurationList();
      assertEquals(7, cacheConfigurations.size());

      final LocalCacheConfiguration cacheConfiguration = cacheConfigurations.get(0);
      assertNotNull(cacheConfiguration.getLocalConfiguration());
      assertEquals(1000, cacheConfiguration.getStore().getExpiration().getTimeToLiveMillis());
      assertTrue(cacheConfiguration.getStore().isOverflowToDisk());
      assertEquals(1048576, cacheConfiguration.getStore().getOverflowToDiskConfiguration().getMaxOverflowBytes());
      assertEquals(10485760, cacheConfiguration.getStore().getLru().getMaxBytes());
   }


   /**
    * Tests {@link CacheonixConfiguration#getTempDir()}
    */
   public void testTempDir() throws IOException {

      assertNotNull(read(TestConstants.CACHEONIX_CLUSTER_XML).getTempDir());
   }


   /**
    * Tests enhancement CACHEONIX-15 "
    */
   public void testGetClusterNameCACHEONIX15() throws IOException {

      final ConfigurationReader configurationReader = new ConfigurationReader();
      final CacheonixConfiguration configuration15 = configurationReader.readConfiguration(
              getTestFileInputStream(CACHEONIX_CONFIG_CACHEONIX_15_XML));
      final ServerConfiguration serverConfiguration = configuration15.getServer();
      assertEquals(TEST_CLUSTER_NAME, serverConfiguration.getClusterConfiguration().getName());
   }


   /**
    * Tests enhancement CACHEONIX-119 "Add idle time seconds to the configuration"
    */
   public void testGetIdleTimeSecsCACHEONIX119() throws IOException {

      final ConfigurationReader configurationReader = new ConfigurationReader();
      final CacheonixConfiguration conf = configurationReader.readConfiguration(
              getTestFileInputStream(CACHEONIX_CONFIG_CACHEONIX_119_XML));
      final ServerConfiguration serverConfiguration = conf.getServer();
      final PartitionedCacheConfiguration cacheConfiguration = serverConfiguration.getPartitionedCacheList().get(0);
      assertEquals(120000, cacheConfiguration.getStore().getExpiration().getIdleTimeMillis());
   }


   /**
    * Tests enhancement CACHEONIX-15 "
    */
   public void testIsTemplate() throws IOException {

      final CacheonixConfiguration configuration45 = read(CACHEONIX_CONFIG_CACHEONIX_45_XML);
      final LocalConfiguration localConfigurations = configuration45.getLocal();
      final List<LocalCacheConfiguration> cacheConfigurations = localConfigurations.getLocalCacheConfigurationList();

      final LocalCacheConfiguration cacheConfiguration0 = cacheConfigurations.get(0);
      assertEquals(CACHE_TEMPLATE_NAME_DEFAULT, cacheConfiguration0.getName());
      assertTrue(cacheConfiguration0.isTemplate());

      final LocalCacheConfiguration cacheConfiguration1 = cacheConfigurations.get(1);
      assertEquals("default-local", cacheConfiguration1.getName());
      assertTrue(cacheConfiguration1.isTemplate());
   }


   public void testGetLoggingLevel() throws IOException {


      assertEquals(CACHEONIX_LOGGING_LEVEL != null ? CACHEONIX_LOGGING_LEVEL : DEFAULT_LOGGING_LEVEL,
              read(TestConstants.CACHEONIX_CLUSTER_XML).getLoggingConfiguration().getLoggingLevel());

      // Test only if the logging level is not overridden
      final String systemProperty = System.getProperty(NAME_CACHEONIX_LOGGING_LEVEL);
      if (systemProperty == null || "debug".equalsIgnoreCase(systemProperty)) {

         final CacheonixConfiguration cacheonixConfiguration = read(CACHEONIX_CONFIG_CACHEONIX_99_XML);
         final LoggingConfiguration loggingConfiguration = cacheonixConfiguration.getLoggingConfiguration();
         assertEquals(LoggingLevel.DEBUG, loggingConfiguration.getLoggingLevel());

      }
   }


   private static CacheonixConfiguration read(final String xml) throws IOException {

      final ConfigurationReader configurationReader = new ConfigurationReader();
      return configurationReader.readConfiguration(getTestFileInputStream(xml)); // NOPMD
   }


   public String toString() {

      return "ConfigurationImplTest{" +
              '}';
   }
}
