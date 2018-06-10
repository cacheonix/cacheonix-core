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
package org.cacheonix.plugin.primefaces;

import junit.framework.TestCase;

/**
 * A tester for {@link PrimefacesCacheProvider}.
 */
public final class PrimefacesCacheProviderTest extends TestCase {


   private static final String REGION = "primefaces.test.cache";

   private static final String OBJECT = "Test object";

   private static final String KEY = "Test key";

   private PrimefacesCacheProvider provider;


   public void testGet() {

      provider.put(REGION, KEY, OBJECT);
      final Object obj = provider.get(REGION, KEY);
      assertEquals(OBJECT, obj);
   }


   public void testPut() {

      provider.put(REGION, KEY, OBJECT);
      final Object obj = provider.get(REGION, KEY);
      assertEquals(OBJECT, obj);
   }


   public void testRemove() {

      provider.put(REGION, KEY, OBJECT);
      provider.remove(REGION, KEY);
      final Object obj = provider.get(REGION, KEY);
      assertNull(obj);
   }


   public void testClear() {

      provider.put(REGION, KEY, OBJECT);
      provider.clear();
      final Object obj = provider.get(REGION, KEY);
      assertNull(obj);
   }


   public void setUp() throws Exception {

      super.setUp();

      provider = new PrimefacesCacheProvider();
   }


   public void tearDown() throws Exception {

      provider.clear();
      provider.tearDown();

      super.tearDown();
   }


   public String toString() {

      return "PrimefacesCacheProviderTest{" +
              "provider=" + provider +
              "} " + super.toString();
   }
}