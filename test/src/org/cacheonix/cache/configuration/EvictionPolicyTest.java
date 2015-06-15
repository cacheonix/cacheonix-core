/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.cache.configuration;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.configuration.EvictionPolicy;

/**
 * Tests EvictionPolicy.
 */
public final class EvictionPolicyTest extends CacheonixTestCase {

   private static final EvictionPolicy LRU = EvictionPolicy.LRU;

   private static final String EXPECTED_LRU_NAME = "lru";


   public void testLRU() {

      assertTrue(LRU.getName().equalsIgnoreCase(EXPECTED_LRU_NAME));
   }


   /**
    * Tests equals() method.
    *
    * @noinspection ObjectEqualsNull
    */
   public void testEquals() {

      assertEquals(LRU, LRU);
      assertFalse(LRU.equals(new Object()));
      assertFalse(LRU.equals(null)); // NOPMD EqualsNull
   }


   /**
    * Tests hashCode() method.
    */
   public void testHashCode() {

      assertEquals((int) LRU.getCode(), LRU.hashCode());
   }


   /**
    * Tests toString() method.
    */
   public void testToString() {

      assertNotNull(LRU.toString());
   }
}
