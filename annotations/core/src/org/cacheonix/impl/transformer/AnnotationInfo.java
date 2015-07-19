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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

/**
 * Stores the CacheonixAnnotation name for a given method and the values for all the annotation parameters
 */
public class AnnotationInfo {

   private final String annotationName;

   private final int methodAccess;

   private final String methodName;

   private final String methodDesc;

   private final String methodSignature;

   private final List<AnnotationParameter> annotationParameters = new ArrayList<AnnotationParameter>();


   /**
    * Class constructor
    *
    * @param annotationName  method annotation name
    * @param methodAccess    the method's access flags
    * @param methodName      the method's name
    * @param methodDesc      the method's descriptor
    * @param methodSignature the method's signature
    */
   public AnnotationInfo(final String annotationName, final int methodAccess,
                         final String methodName, final String methodDesc,
                         final String methodSignature) {

      this.annotationName = annotationName;
      this.methodAccess = methodAccess;
      this.methodName = methodName;
      this.methodDesc = methodDesc;
      this.methodSignature = methodSignature;
   }


   /**
    * Adds a method's annotation parameter to the parameter collection
    *
    * @param parameterName annotation parameter name
    * @param value         annotation parameter value
    * @param type          annotation Parameter of type <code>org.objectweb.asm.Type</code>
    */
   public void addAnnotationParameter(final String parameterName,
                                      final String value, final Type type) {

      annotationParameters.add(new AnnotationParameter(parameterName, value,
              type));
   }


   /**
    * Returns the annotation parameter value if the parameter name exists in the parameter collection
    *
    * @param parameterName name of the annotation parameter
    * @return the value for the annotation parameter if it exists othewise null
    */

   public Object getAnnotationParameterValue(final String parameterName) {

      for (final AnnotationParameter param : annotationParameters) {
         if (param.name.equals(parameterName)) {
            return param.oVal;
         }
      }
      return null;
   }


   /**
    * Returns true if the the Annotation name exists
    *
    * @param parameterName parameter name to search for
    * @return true if the annotation parameter exists in the parameter collection othewise false
    */
   public boolean isAnnotationParameterPresent(final String parameterName) {

      for (final AnnotationParameter param : annotationParameters) {
         if (param.name.equals(parameterName)) {
            return true;
         }
      }
      return false;
   }


   /**
    * Returns number of annotation parameters in the collection
    *
    * @return the number of annotation parameters in the collection
    */
   public int annotationParameterSize() {

      return annotationParameters.size();
   }


   /**
    * Returns name of the annotation
    *
    * @return name of the annotation
    */
   public String getAnnotationName() {

      return annotationName;
   }


   /**
    * Returns the method's access flags
    *
    * @return the method's access flags
    */
   public int getMethodAccess() {

      return methodAccess;
   }


   /**
    * Returns the method name
    *
    * @return the method name
    */
   public String getMethodName() {

      return methodName;
   }


   /**
    * Returns the method description
    *
    * @return the method description
    */
   public String getMethodDesc() {

      return methodDesc;
   }


   /**
    * Returns the method Signature
    *
    * @return the method Signature
    */
   public String getMethodSignature() {

      return methodSignature;
   }

}
