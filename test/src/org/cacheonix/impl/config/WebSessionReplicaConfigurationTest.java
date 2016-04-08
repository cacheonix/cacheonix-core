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
package org.cacheonix.impl.config;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;

/**
 * Tester for {@link WebSessionReplicaConfiguration}.
 */
public final class WebSessionReplicaConfigurationTest extends TestCase {

   private WebSessionReplicaConfiguration configuration;


   public void testGetPartitionedSessionReplicaStoreConfiguration() throws Exception {

      assertNotNull(configuration.getPartitionedSessionReplicaStoreConfiguration());
   }


   public void setUp() throws Exception {

      super.setUp();

      final ConfigurationReader configurationReader = new ConfigurationReader();
      final String configurationPath = TestUtils.getTestFile("cacheonix-config-cluster-member-w-session-replica-1.xml").getCanonicalPath();
      final CacheonixConfiguration configuration = configurationReader.readConfiguration(configurationPath);
      final ServerConfiguration server = configuration.getServer();
      this.configuration = server.getWebSessionReplicaList().get(0);
   }


   public void tearDown() throws Exception {

      configuration = null;
      super.tearDown();
   }
}
