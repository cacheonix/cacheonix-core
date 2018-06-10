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
package org.cacheonix.impl.cache.util;

import junit.framework.TestCase;

/**
 * EntryImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>05/03/2008</pre>
 */
public final class EntryImplTest extends TestCase {

   private static final Object VALUE = "value";

   private static final Object KEY = "key";

   private EntryImpl entry = null;


   public void testGetKey() {

      assertEquals(KEY, entry.getKey());
   }


   public void testSetGetValue() {

      assertEquals(VALUE, entry.getValue());
   }


   public void testToString() {

      assertNotNull(entry.toString());
   }


   public void testHashCode() {

      assertTrue(entry.hashCode() != 0);
   }


   protected void setUp() throws Exception {

      super.setUp();
      entry = new EntryImpl(KEY, VALUE);
   }


   public String toString() {

      return "EntryImplTest{" +
              "entry=" + entry +
              "} " + super.toString();
   }
}
