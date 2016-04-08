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
package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;

/**
 * Tester for CacheProcessorKey.
 */
public final class CacheProcessorKeyTest extends TestCase {

   private static final String TEST_CACHE = "test.cache";

   private CacheProcessorKey key;


   public void testEquals() throws Exception {

      assertEquals(key, new CacheProcessorKey(TEST_CACHE));
      assertTrue(!new CacheProcessorKey("some.other.name").equals(key));
   }


   public void testHashCode() throws Exception {

      assertTrue(key.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(key.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      key = new CacheProcessorKey(TEST_CACHE);
   }


   public void tearDown() throws Exception {

      key = null;

      super.tearDown();
   }
}
