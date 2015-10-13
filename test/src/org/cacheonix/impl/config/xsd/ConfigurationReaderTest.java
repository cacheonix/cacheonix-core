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
package org.cacheonix.impl.config.xsd;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.config.CacheonixConfiguration;
import org.cacheonix.impl.config.ConfigurationReader;
import org.cacheonix.impl.config.LocalConfiguration;
import org.cacheonix.impl.config.ServerConfiguration;

/**
 * Tester for configuration reader.
 */
public final class ConfigurationReaderTest extends CacheonixTestCase {

   private ConfigurationReader reader;


   public void testReadServerConfiguration() throws Exception {

      final CacheonixConfiguration configuration = reader.readConfiguration(TestUtils.getTestFile("new-config-example-server.xml").getCanonicalPath());
      final LocalConfiguration local = configuration.getLocal();
      final ServerConfiguration server = configuration.getServer();

      assertNull(local);
      assertNotNull(server);
   }


   public void testReadLocalConfiguration() throws Exception {

      final CacheonixConfiguration configuration = reader.readConfiguration(TestUtils.getTestFile("new-config-example-local.xml").getCanonicalPath());

      final LocalConfiguration local = configuration.getLocal();
      final ServerConfiguration server = configuration.getServer();

      assertNull(server);
      assertNotNull(local);
   }


   public void setUp() throws Exception {

      super.setUp();

      reader = new ConfigurationReader();
   }
}
