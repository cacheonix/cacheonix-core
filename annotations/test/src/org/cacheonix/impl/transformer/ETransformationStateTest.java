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

/**
 * @author calsif
 */
public class ETransformationStateTest extends TestCase {

   /**
    * Test method for {@link ETransformationState#toString()}.
    */
   public void testToString() {

      assertEquals("INITIAL_STATE", ETransformationState.INITIAL_STATE
              .toString());
      assertEquals("READING_CONFIG_ANNOTATION",
              ETransformationState.READING_CONFIG_ANNOTATION.toString());
      assertEquals("READING_METHOD_ANNOTATION",
              ETransformationState.READING_METHOD_ANNOTATION.toString());
   }

}
