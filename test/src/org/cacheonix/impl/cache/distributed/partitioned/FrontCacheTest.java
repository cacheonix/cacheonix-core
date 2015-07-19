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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.configuration.CacheonixConfiguration;
import org.cacheonix.impl.configuration.ConfigurationReader;
import org.cacheonix.impl.configuration.FrontCacheConfiguration;

/**
 */
public final class FrontCacheTest extends CacheonixTestCase {

   private FrontCache frontCache;


   public void testPut() throws Exception {

      frontCache.put(toBinary("key"), toBinary("value"), getClock().currentTime().add(10));
   }


   public void setUp() throws Exception {

      super.setUp();

      final ConfigurationReader reader = new ConfigurationReader();
      final CacheonixConfiguration configuration = reader.readConfiguration(TestUtils.getTestFile("cacheonix-config-with-front-cache.xml").toString());
      final FrontCacheConfiguration frontCacheConfiguration = configuration.getServer().enumeratePartitionedCaches().get(0).getFrontCacheConfiguration();
      frontCache = new FrontCache(getClock(), frontCacheConfiguration);
   }


   public void tearDown() throws Exception {

      frontCache.clear();
      frontCache = null;

      super.tearDown();
   }
}
