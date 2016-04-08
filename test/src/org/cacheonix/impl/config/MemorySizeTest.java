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

/**
 * MemorySize Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>06/16/2008</pre>
 */
public final class MemorySizeTest extends TestCase {

   private static final String TEST_DESCRIPTION = "test";

   private static final int ONE_GB = 1024 * 1024 * 1024;

   private static final int ONE_MB = 1024 * 1024;

   private static final int ONE_KB = 1024;

   private static final int ONE = 1;


   public void testGetSizeBytes() throws Exception {

      assertEquals(Runtime.getRuntime().maxMemory() / 2, new MemorySize(TEST_DESCRIPTION, "50%").getSizeBytes());

      assertEquals(ONE_GB, new MemorySize(TEST_DESCRIPTION, "1g").getSizeBytes());
      assertEquals(ONE_GB, new MemorySize(TEST_DESCRIPTION, "1gb").getSizeBytes());
      assertEquals(ONE_GB, new MemorySize(TEST_DESCRIPTION, "1G").getSizeBytes());
      assertEquals(ONE_GB, new MemorySize(TEST_DESCRIPTION, "1Gb").getSizeBytes());

      assertEquals(ONE_MB, new MemorySize(TEST_DESCRIPTION, "1m").getSizeBytes());
      assertEquals(ONE_MB, new MemorySize(TEST_DESCRIPTION, "1mb").getSizeBytes());
      assertEquals(ONE_MB, new MemorySize(TEST_DESCRIPTION, "1M").getSizeBytes());
      assertEquals(ONE_MB, new MemorySize(TEST_DESCRIPTION, "1Mb").getSizeBytes());

      assertEquals(ONE_KB, new MemorySize(TEST_DESCRIPTION, "1k").getSizeBytes());
      assertEquals(ONE_KB, new MemorySize(TEST_DESCRIPTION, "1kb").getSizeBytes());
      assertEquals(ONE_KB, new MemorySize(TEST_DESCRIPTION, "1K").getSizeBytes());
      assertEquals(ONE_KB, new MemorySize(TEST_DESCRIPTION, "1Kb").getSizeBytes());

      assertEquals(ONE, new MemorySize(TEST_DESCRIPTION, "1").getSizeBytes());
   }


   public void testToString() {

      assertNotNull(new MemorySize(TEST_DESCRIPTION, "50%").toString());
   }
}
