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
package org.cacheonix.impl.cache.item;

import junit.framework.TestCase;

/**
 * CompressedCopyElementValueFactory Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>12/16/2008</pre>
 */
public final class CompressedBinaryFactoryTest extends TestCase {

   private CompressedBinaryFactory factory = null;

   private static final String OBJECT = "object";


   public void testToString() {

      assertNotNull(factory.toString());
   }


   public void testCreate() throws InvalidObjectException {

      final Binary binary = factory.createBinary(OBJECT);
      assertTrue(binary instanceof CompressedBinary);
   }


   protected void setUp() throws Exception {

      super.setUp();
      factory = new CompressedBinaryFactory();
   }


   public String toString() {

      return "CompressedBinaryFactoryTest{" +
              "factory=" + factory +
              "} " + super.toString();
   }
}
