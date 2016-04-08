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
 * Tester for {@link PartitionedSessionReplicaStoreConfiguration}.
 */
public final class PartitionedSessionReplicaStoreConfigurationTest extends TestCase {

   private PartitionedSessionReplicaStoreConfiguration configuration;


   public void testGetReplicaCount() throws Exception {

      assertEquals(1, configuration.getReplicaCount());
   }


   public void testMaxBytes() throws Exception {

      assertEquals(1048576, configuration.getMaxBytes());
   }


   public void setUp() throws Exception {

      super.setUp();

      final ConfigurationReader configurationReader = new ConfigurationReader();
      final String configurationPath = TestUtils.getTestFile("cacheonix-config-cluster-member-w-session-replica-1.xml").getCanonicalPath();
      final CacheonixConfiguration configuration = configurationReader.readConfiguration(configurationPath);
      final ServerConfiguration server = configuration.getServer();
      final WebSessionReplicaConfiguration sessionConfiguration = server.getWebSessionReplicaList().get(0);
      this.configuration = sessionConfiguration.getPartitionedSessionReplicaStoreConfiguration();
   }


   public void tearDown() throws Exception {

      configuration = null;
      super.tearDown();
   }
}
