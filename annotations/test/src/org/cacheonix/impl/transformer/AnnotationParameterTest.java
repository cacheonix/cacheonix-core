/**
 *
 */
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
package org.cacheonix.impl.transformer;

import junit.framework.TestCase;
import org.objectweb.asm.Type;

/**
 *
 */
@SuppressWarnings("CachedNumberConstructorCall")
public class AnnotationParameterTest extends TestCase {

   /**
    * Test method for {@link AnnotationParameter#AnnotationParameter(String, Object, Type)} .
    */
   public void testAnnotationParameter() {

      final String name = "newParam";
      final Object oVal = new Integer(34);
      final Type type = Type.INT_TYPE;

      final AnnotationParameter apN = new AnnotationParameter(name, oVal,
              type);

      assertNotNull(apN);

      assertSame(name, apN.name);
      assertSame(oVal, apN.oVal);
      assertSame(type, apN.type);
   }

}
