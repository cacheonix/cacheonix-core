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

import junit.framework.TestCase;
import org.cacheonix.TestUtils;

/**
 * Tester for {@link DataSourceConfiguration}.
 */
public final class PrefetchConfigurationTest extends TestCase {

   private LocalConfiguration local;


   public void testDefaultPrefetch() throws Exception {

      final DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration();
      assertFalse("Default should be 'disabled'",
              dataSourceConfiguration.isPrefetchConfigurationSet() && dataSourceConfiguration.getPrefetchConfiguration().isEnabled());
   }


   public void testReadPrefetch() throws Exception {

      final LocalCacheConfiguration localCacheConfiguration = local.getLocalCache(0);
      final LocalCacheStoreConfiguration localCacheConfigurationStore = localCacheConfiguration.getStore();
      final DataSourceConfiguration dataSourceConfiguration = localCacheConfigurationStore.getDataSource();
      assertTrue(
              dataSourceConfiguration.isPrefetchConfigurationSet() && dataSourceConfiguration.getPrefetchConfiguration().isEnabled());
   }


   public void setUp() throws Exception {

      super.setUp();

      final ConfigurationReader configurationReader = new ConfigurationReader();
      final String configurationPath = TestUtils.getTestFile(
              "cacheonix-config-DataSourceConfigurationTest.xml").getCanonicalPath();
      final CacheonixConfiguration configuration = configurationReader.readConfiguration(configurationPath);
      local = configuration.getLocal();
   }


   public void tearDown() throws Exception {

      local = null;
      super.tearDown();
   }
}
