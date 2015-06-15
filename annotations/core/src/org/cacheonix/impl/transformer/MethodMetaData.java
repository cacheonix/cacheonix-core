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

import java.util.ArrayList;
import java.util.List;

/**
 * Contains method name, description and all the annotation information for a method that uses Cacheonix annotation at
 * the method level
 */
public class MethodMetaData {

   private final List<AnnotationInfo> annotationInfo;

   private final List<Integer> annotationParameterInfo;

   private final String name;

   private final String desc;


   /**
    * Class constructor
    *
    * @param name method's name
    * @param desc method's desc
    * @param info List containing all the annotation information for the method
    */
   public MethodMetaData(final String name, final String desc, final List<AnnotationInfo> info,
                         final List<Integer> paramInfo) {

      this.name = name;
      this.desc = desc;
      annotationInfo = new ArrayList<AnnotationInfo>(info);
      annotationParameterInfo = new ArrayList<Integer>(paramInfo);
   }


   /**
    * Returns a list containing the Index of the parameters that have @CacheKey parameter annotation
    *
    * @return list containing the Index of the parameters that have @CacheKey parameter annotation
    */
   public List<Integer> getMethodParamAnnotationInfo() {

      return annotationParameterInfo;
   }


   /**
    * Returns the value for an annotation parameter for a given annotation
    *
    * @param annotationName          name of the annotation
    * @param annotationParameterName parameter name of the annotation
    * @return annotation parameter value if it exists else null
    */
   public Object getAnnotationParameterValue(final String annotationName, final String annotationParameterName) {

      for (final AnnotationInfo info : annotationInfo) {
         if (info.getAnnotationName().equals(annotationName)) {
            return info.getAnnotationParameterValue(annotationParameterName);
         }
      }
      return null;

   }


   /**
    * Returns true if there are any Cacheonix Annotations specified for the method
    *
    * @return true if Cacheonix annotation is specified for the method else false
    */
   public boolean isAnnotationsPresent() {

      return !annotationInfo.isEmpty();
   }


   /**
    * Checks whether the Cacheonix annotation is declared for the method
    *
    * @param annotationName name of the annotation
    * @return true if the annotation is declared for the method else false
    */
   public boolean isAnnotationPresent(final String annotationName) {

      boolean annotationPresent = false;
      for (final AnnotationInfo info : annotationInfo) {
         if (info.getAnnotationName().equals(annotationName)) {
            annotationPresent = true;
            break;
         }
      }

      return annotationPresent;
   }


   /**
    * Returns name of the method
    *
    * @return method name
    */
   public String getName() {

      return name;
   }


   /**
    * Returns name of the method descriptor
    *
    * @return method's descriptor
    */
   public String getDesc() {

      return desc;
   }

}
