/**
 *
 */
/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
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
package org.cacheonix.impl.transformer;

import org.objectweb.asm.Type;

/**
 * Class that holds the name, value and type for an Annotation parameter
 */

public class AnnotationParameter {

   public final String name;

   public final Object oVal;

   public final Type type;


   /**
    * Class constructor
    *
    * @param name parameter name
    * @param val  parameter value
    * @param type parameter of <code>org.objectweb.asm.Type</code> type
    */
   public AnnotationParameter(final String name, final Object val,
                              final Type type) {

      this.name = name;
      this.oVal = val;
      this.type = type;
   }

}
