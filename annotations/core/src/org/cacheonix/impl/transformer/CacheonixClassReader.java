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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Reads Cacheonix annotation information from the byte code of the class
 */
public class CacheonixClassReader extends EmptyVisitor {

   public static final Map<String, CacheonixAnnotation> cacheonixAnnotations = CacheonixAnnotation
           .annotationMapCreator();

   // Fully qualifies name of the class which is being transformed
   // private String targetClassName;

   private ETransformationState currentState = ETransformationState.INITIAL_STATE;

   private final Map<String, AnnotationParameter> classLevelAnnotationInfo = new HashMap<String, AnnotationParameter>();

   private final Map<String, MethodMetaData> methodsInfo = new HashMap<String, MethodMetaData>();

   private boolean bCacheonixAnnotationPresent = false;

   private String className;


   /**
    * Class constructor
    */
   public CacheonixClassReader() {

      currentState = ETransformationState.INITIAL_STATE;
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String[])
     */
   public void visit(final int version, final int access, final String name,
                     final String signature, final String superName,
                     final String[] interfaces) {
      // if ((version & 0xFF) < Opcodes.V1_5)
      // {
      // // System.out.println("Version < 1.5 Class: " + name);
      // // REVIEWME -> Skip all further processing for this class
      // }
      // get the Name
      className = name;

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String,
     * boolean)
     */
   public AnnotationVisitor visitAnnotation(final String desc,
                                            final boolean visible) {

      if (visible) {
         final CacheonixAnnotation annRef = cacheonixAnnotations.get(desc);

         if (annRef != null) {
            currentState = annRef.stateForProcessing;
         }
      }
      return super.visitAnnotation(desc, visible);
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String,
     * java.lang.Object)
     */
   public void visit(final String name, final Object value) {

      switch (currentState) {
         // If our current state is annotation then get the
         // values for annotation
         case READING_CONFIG_ANNOTATION:
            if (name.compareTo(CacheonixAnnotation.CACHECONFIGURATION_CONFIGURATION_PATH) == 0) {
               bCacheonixAnnotationPresent = true;
               classLevelAnnotationInfo.put(CacheonixAnnotation.CACHECONFIGURATION_CONFIGURATION_PATH,
                       new AnnotationParameter(
                               CacheonixClassAdapter.CACHEONIX_CONFIG_FILE_FIELD,
                               value, Type.getType(String.class)));

            } else if (name.compareTo(CacheonixAnnotation.CACHECONFIGURATION_CACHE_NAME) == 0) {
               bCacheonixAnnotationPresent = true;
               classLevelAnnotationInfo.put(CacheonixAnnotation.CACHECONFIGURATION_CACHE_NAME,
                       new AnnotationParameter(
                               CacheonixClassAdapter.CACHE_NAME_FIELD, value,
                               Type.getType(String.class)));
            }
            break;

         case READING_METHOD_ANNOTATION:
            break;

         default:
            break;
      }

      currentState = ETransformationState.INITIAL_STATE;
      super.visit(name, value);
   }


   /**
    * Returns Class level annotation info
    *
    * @return Map of annotation name as the key and object containing parameter type and value for the annotation
    */
   public Map<String, AnnotationParameter> getClassLevelAnnotationInfo() {

      return classLevelAnnotationInfo;
   }


   /**
    * Returns Method level annotation info
    *
    * @return Map of method name + method desc as the key and MethodMetaData as value
    */
   public Map<String, MethodMetaData> getMethodsInfo() {

      return methodsInfo;
   }


   /**
    * Adds a method's annotation info to the Method annotation info collection. If a method has Cacheonix annotations
    * specified, but the class annotations are missing, then the class level annotations are added
    *
    * @param metaData object containing method name , description and annotation info
    */
   public void addMethodInfo(final MethodMetaData metaData) {

      if (metaData.isAnnotationsPresent()) {
         UpdateClassLevelAnnotationInfo();
      }
      methodsInfo.put(metaData.getName() + metaData.getDesc(), metaData);
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String[])
     */
   public MethodVisitor visitMethod(final int access, final String name,
                                    final String desc, final String signature, final String[] exceptions) {

      currentState = ETransformationState.INITIAL_STATE;
      return new CacheonixMethodVisitor(access, name, desc, signature, this);
   }


   /**
    * If there is atleast one Cacheonix annotation present then update the missing annotation values with the default
    * ones
    */
   public void visitEnd() {

      if (bCacheonixAnnotationPresent) {
         UpdateClassLevelAnnotationInfo();
      }
   }


   /**
    * Adds the missing class level annotations
    */
   public void UpdateClassLevelAnnotationInfo() {

      if (classLevelAnnotationInfo.get(CacheonixAnnotation.CACHECONFIGURATION_CACHE_NAME) == null) {
         classLevelAnnotationInfo.put(CacheonixAnnotation.CACHECONFIGURATION_CACHE_NAME,
                 new AnnotationParameter(CacheonixClassAdapter.CACHE_NAME_FIELD, className.replace('/', '.'),
                         Type.getType(String.class)));
      }

      if (classLevelAnnotationInfo.get(CacheonixAnnotation.CACHECONFIGURATION_CONFIGURATION_PATH) == null) {
         classLevelAnnotationInfo.put(CacheonixAnnotation.CACHECONFIGURATION_CONFIGURATION_PATH,
                 new AnnotationParameter(CacheonixAnnotation.CACHECONFIGURATION_CONFIGURATION_PATH,
                         CacheonixAnnotation.CACHECONFIGURATION_CONFIGURATION_PATH_DEFAULT,
                         Type.getType(String.class)));
      }
   }

}
