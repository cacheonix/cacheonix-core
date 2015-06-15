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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.impl.net.processor.SimpleProcessorKey;
import org.cacheonix.impl.net.serializer.Wireable;
import junit.framework.TestCase;

/**
 * Tester for SimpleProcessorKey.
 */
public final class SimpleProcessorKeyTest extends TestCase {

   private SimpleProcessorKey key;


   public void testEquals() throws Exception {

      assertEquals(key, ClusterProcessorKey.getInstance());
      assertTrue(!key.equals(new SimpleProcessorKey(Wireable.DESTINATION_CACHE_PROCESSOR)));
   }


   public void testHashCode() throws Exception {

      assertTrue(key.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(key.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      key = ClusterProcessorKey.getInstance();
   }


   public void tearDown() throws Exception {

      key = null;

      super.tearDown();
   }
}
