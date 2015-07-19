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

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;

/**
 * Used to collect Cacheonix annotation information while reading the byte code of the class file
 */
public class CacheonixAnnotationCollector implements AnnotationVisitor {

   private final CacheonixMethodVisitor methodVisitor;

   private final CacheonixAnnotation annotationMetaInfo;

   private final AnnotationInfo annotationInfo;


   /**
    * Class constructor
    *
    * @param annotationMetaInfo contains the information containing the Cacheonix annotation names, their parameter names
    *                           and the default values for the parameters
    * @param annotationDesc     Descriptor for the annotation. For ex: Descriptor for a String is "Ljava/lang/String"
    * @param methodAccess       method's access flags
    * @param methodName         method's name
    * @param methodDesc         method's descriptor
    * @param methodSignature    method's signature
    * @param methodVisitor      methodVisitor that is processing the method's annotation
    */
   public CacheonixAnnotationCollector(
           final CacheonixAnnotation annotationMetaInfo,
           final String annotationDesc, final int methodAccess,
           final String methodName, final String methodDesc,
           final String methodSignature,
           final CacheonixMethodVisitor methodVisitor) {

      annotationInfo = new AnnotationInfo(annotationDesc, methodAccess,
              methodName, methodDesc, methodSignature);
      this.annotationMetaInfo = annotationMetaInfo;
      this.methodVisitor = methodVisitor;
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, Object)
     */
   public void visit(final String annotationParameterName, final Object value) {

      final List<AnnotationParameter> paramList = annotationMetaInfo.parameters;
      if (paramList != null) {
         for (final AnnotationParameter param : paramList) {
            if (param.name.equals(annotationParameterName)) {
               annotationInfo.addAnnotationParameter(
                       annotationParameterName, value.toString(),
                       param.type);
               break;
            }
         }
      }
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String
     * name, String desc)
     */
   public AnnotationVisitor visitAnnotation(final String name,
                                            final String desc) {

      return null;
   }

   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String
     * name)
     */


   public AnnotationVisitor visitArray(final String name) {

      return null;
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.AnnotationVisitor#visitEnum(java.lang.String name,
     * java.lang.String desc, java.lang.String value)
     */
   public void visitEnum(final String name, final String desc,
                         final String value) {

   }


   /**
    * Visits the end of the annotation. Adds the annotation parameters for this annotation to the methodVisitor
    */
   public void visitEnd() {

      final List<AnnotationParameter> paramList = annotationMetaInfo.parameters;
      if (paramList != null) {
         if (paramList.size() != annotationInfo.annotationParameterSize()) {
            // if the annotations are not explicitly specified then use the
            // default values
            // and add it to the list
            for (final AnnotationParameter param : paramList) {
               if (!annotationInfo
                       .isAnnotationParameterPresent(param.name)) {
                  annotationInfo.addAnnotationParameter(param.name,
                          param.oVal.toString(), param.type);
               }
            }
         }
      }
      methodVisitor.addMethodAnnotation(annotationInfo);
   }

}
