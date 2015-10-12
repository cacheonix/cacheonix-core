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
package org.cacheonix.impl;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.configuration.CacheonixConfiguration;
import org.cacheonix.impl.configuration.ConfigurationReader;
import org.cacheonix.impl.configuration.SystemProperty;
import org.cacheonix.impl.util.IOUtils;

/**
 * Tests behavior of the cluster node in condition when there is no a cache member in a cluster.
 *
 * @noinspection FieldCanBeLocal, JavaDoc, ProhibitedExceptionDeclared
 */
@SuppressWarnings("ConstantNamingConvention")
public final class ClusterNodeWithNoCacheNodesTest extends CacheonixTestCase {

   private static final String DISTRIBUTED_CACHE = "distributed.cache";

   private DistributedCacheonix distributedCacheonixWithCacheNodes;

   private DistributedCacheonix distributedCacheonixWithoutCacheNodes;


   /**
    */
   public void testGetCache() {

      final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);
      savedSystemProperty.save();
      boolean thrown = false;
      try {
         System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "true");
         distributedCacheonixWithoutCacheNodes.getCache(DISTRIBUTED_CACHE);
      } catch (final IllegalArgumentException iae) {
         thrown = true;
      } finally {
         savedSystemProperty.restore();
      }


      assertTrue(thrown);
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();

      // Startup node with caches.
      distributedCacheonixWithCacheNodes = new DistributedCacheonix(getClusterConfigWithCacheNode().getServer());
      distributedCacheonixWithCacheNodes.startup();

      // Startup node without caches.
      distributedCacheonixWithoutCacheNodes = new DistributedCacheonix(getClusterConfigWithoutCacheNode().getServer());
      distributedCacheonixWithoutCacheNodes.startup();
   }


   private static CacheonixConfiguration getClusterConfigWithCacheNode() throws IOException {

      final ConfigurationReader configurationReader = new ConfigurationReader();
      return configurationReader.readConfiguration(TestUtils.getTestFileInputStream("cacheonix-config-ClusterMemberImplTest.xml"));
   }


   private static CacheonixConfiguration getClusterConfigWithoutCacheNode() throws IOException {

      final ConfigurationReader configurationReader = new ConfigurationReader();
      return configurationReader.readConfiguration(TestUtils.getTestFileInputStream("cacheonix-config-no-cache-nodes.xml"));
   }


   protected void tearDown() throws Exception {

      IOUtils.shutdownHard(distributedCacheonixWithCacheNodes);
      IOUtils.shutdownHard(distributedCacheonixWithoutCacheNodes);
      super.tearDown();
   }


   public String toString() {

      return "ClusterNodeWithNoCacheNodesTest{" +
              "distributedCacheonixWithCacheNodes=" + distributedCacheonixWithCacheNodes +
              ", distributedCacheonixWithoutCacheNodes=" + distributedCacheonixWithoutCacheNodes +
              "} " + super.toString();
   }
}