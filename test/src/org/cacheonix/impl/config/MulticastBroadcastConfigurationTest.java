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

/**
 * Test for MulticastBroadcastConfiguration.
 */
public class MulticastBroadcastConfigurationTest extends TestCase {


   private MulticastBroadcastConfiguration multicastBroadcastConfiguration;


   public void testDefaultMulticastTTLValue() throws Exception {

      final int defaultValue = 1;
      assertEquals("Default value should be " + defaultValue, defaultValue, multicastBroadcastConfiguration.getMulticastTTL());
   }


   public void testSetGetMulticastTTL() throws Exception {

      multicastBroadcastConfiguration.setMulticastTTL(2);
      assertEquals(2, multicastBroadcastConfiguration.getMulticastTTL());
   }


   public void setUp() throws Exception {

      super.setUp();

      multicastBroadcastConfiguration = new MulticastBroadcastConfiguration();
   }
}
