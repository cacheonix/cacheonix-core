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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.config.CacheonixConfiguration;
import org.cacheonix.impl.config.ConfigurationReader;
import org.cacheonix.impl.config.PartitionedCacheConfiguration;
import org.cacheonix.impl.config.ServerConfiguration;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * @noinspection FieldCanBeLocal, JavaDoc, ProhibitedExceptionDeclared
 */
public class DistributedCacheonixTCPMulticastTest extends CacheonixTestCase {

   @SuppressWarnings("UnusedDeclaration")
   private static final Logger LOG = Logger.getLogger(DistributedCacheonixTCPMulticastTest.class); // NOPMD

   private static final String CACHEONIX_CONFIG_CLUSTER_MEMBER_IMPL_TEST_XML = "cacheonix-config-DistributedCacheonixTCPMulticastTest.xml";

   private static final String TEST_KEY1 = "test_key1";

   private static final String TEST_KEY2 = "test_key2";

   private static final String TEST_VALUE1 = "test_value1";

   private static final String TEST_VALUE2 = "test_value2";

   private static final String DISTRIBUTED_CACHE = "distributed.cache";

   private DistributedCacheonix node;

   private PartitionedCacheConfiguration cacheConfig;


   /**
    */
   public void testToString() {

      assertNotNull(node.toString());
   }


   /**
    */
   public void testGetCache() throws InterruptedException, StorageException {

      Cache<String, String> cache = null;
      boolean timeoutReached = false;
      final long timeoutBarrier = System.currentTimeMillis() + 10000L;
      while (cache == null && !timeoutReached) {
         cache = node.getCache(DISTRIBUTED_CACHE);
         Thread.sleep(1L);
         timeoutReached = System.currentTimeMillis() >= timeoutBarrier;
      }
      assertTrue("Timeout", !timeoutReached);
      assertNotNull(cache);
      assertEquals(cacheConfig.getName(), cache.getName());

      assertNull(cache.put(TEST_KEY1, TEST_VALUE1));
      assertEquals(TEST_VALUE1, cache.put(TEST_KEY1, TEST_VALUE2));
      assertNull(cache.put(TEST_KEY2, TEST_VALUE2));
      assertEquals(TEST_VALUE2, cache.put(TEST_KEY2, TEST_VALUE1));
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      final ConfigurationReader configurationReader = new ConfigurationReader();
      final CacheonixConfiguration configuration = configurationReader.readConfiguration(TestUtils.getTestFile(CACHEONIX_CONFIG_CLUSTER_MEMBER_IMPL_TEST_XML).getCanonicalPath());
      final ServerConfiguration serverConfiguration = configuration.getServer();
      cacheConfig = serverConfiguration.getPartitionedCacheList().get(0);
      node = new DistributedCacheonix(serverConfiguration);
      node.startup();
   }


   protected void tearDown() throws Exception {

      IOUtils.shutdownHard(node);
      super.tearDown();
   }


   public String toString() {

      return "ClusterImplTest{" +
              "manager=" + node +
              '}';
   }
}
