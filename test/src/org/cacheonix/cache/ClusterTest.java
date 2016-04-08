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
package org.cacheonix.cache;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tester for cacheonix.cluster.Cluster
 */
public final class ClusterTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterTest.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster.
    */
   private static final String[] CACHEONIX_CONFIGURATIONS = {
           "cacheonix-config-cluster-member-1.xml",
           "cacheonix-config-cluster-member-2.xml",
           "cacheonix-config-cluster-member-3.xml"
   };

   /**
    * List of Cacheonix instance.
    */
   private final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);


   public void testGetCluster() throws Exception {

      assertNotNull(cacheManagerList.get(0).getCluster());
   }


   public void testToString() throws Exception {

      assertNotNull(cacheManagerList.get(0).getCluster().toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      for (final String configuration : CACHEONIX_CONFIGURATIONS) {
         final Cacheonix manager = Cacheonix.getInstance(TestUtils.getTestFile(configuration).toString());
         cacheManagerList.add(manager);
      }

      // Wait for cluster to form
      waitForClusterToForm(cacheManagerList);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      for (int i = 0; i < CACHEONIX_CONFIGURATIONS.length; i++) {
         cacheManagerList.get(i).shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      }
      cacheManagerList.clear();
      super.tearDown();
   }
}
