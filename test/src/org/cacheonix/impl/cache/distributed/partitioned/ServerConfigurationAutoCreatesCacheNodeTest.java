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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.time.Timeout;

/**
 * Tests clustered cache
 *
 * @noinspection ProhibitedExceptionDeclared, ProhibitedExceptionDeclared, ConstantNamingConvention,
 * ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention, ConstantNamingConvention
 */
public final class ServerConfigurationAutoCreatesCacheNodeTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ServerConfigurationAutoCreatesCacheNodeTest.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster.
    */
   private static final String[] NODE_CONFIGURATIONS = {
           "cacheonix-config-cluster-member-1.xml",
           "cacheonix-config-cluster-member-2.xml",
           "cacheonix-config-cluster-member-3.xml",
           "cacheonix-config-cluster-member-server.xml",
   };

   private static final int NODE_COUNT = NODE_CONFIGURATIONS.length;

   private static final String DISTRIBUTED_CACHE_NAME = "partitioned.distributed.cache";

   private final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);


   /**
    * Tests that server configuration auto-creates a cache member.
    *
    * @throws Exception if an unexpected error occurred.
    */
   public void testServerCreatesCache() throws Exception {

      // Startup caches
      final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);
      try {
         for (int i = 0; i < NODE_COUNT; i++) {

            final String configurationPath = TestUtils.getTestFile(NODE_CONFIGURATIONS[i]).toString();
            cacheManagerList.add(Cacheonix.getInstance(configurationPath));
         }

         // Wait for cluster to stabilize
         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("======= Begin waiting for a cache processor to create ======= "); // NOPMD

         final Timeout timeoutForOwnersToArrive = new Timeout(10000L).reset();
         while (!timeoutForOwnersToArrive.isExpired() && cacheManagerList.get(0).getCache(DISTRIBUTED_CACHE_NAME).getKeyOwners().size() != NODE_COUNT) {

            Thread.sleep(100L);
         }

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("======= End waiting for a cache processor to create ======= "); // NOPMD

         // Assert key owners size - should be number of configs though
         // the server configuration does not define the named node.
         assertEquals(NODE_COUNT, cacheManagerList.get(0).getCache(DISTRIBUTED_CACHE_NAME).getKeyOwners().size());
      } catch (final Exception e) {

         LOG.error(e, e);
         throw e;
      } finally {

         // Shutdown caches
         for (final Cacheonix cacheonix : cacheManagerList) {

            cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         }
         cacheManagerList.clear();
      }
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      savedSystemProperty.save();
      System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      savedSystemProperty.restore();
      super.tearDown();
   }


   public ServerConfigurationAutoCreatesCacheNodeTest(final String name) {

      super(name);
   }


   public String toString() {

      return "AutoCacheNodeCreatesCacheTest{" +
              "savedSystemProperty=" + savedSystemProperty +
              "} " + super.toString();
   }
}