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
package org.cacheonix.impl.cache.local;

import org.cacheonix.TestConstants;
import junit.framework.TestCase;

/**
 * LocalCacheMember Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>06/15/2008</pre>
 */
public final class LocalCacheNodeTest extends TestCase {

   private LocalCacheMember localCacheNode = null;


   public void testToString() {

      assertNotNull(localCacheNode.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      localCacheNode = new LocalCacheMember(TestConstants.CACHEONIX_XML);
   }


   public String toString() {

      return "LocalCacheNodeTest{" +
              "localCacheNode=" + localCacheNode +
              "} " + super.toString();
   }
}
