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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.Type;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Collects Method level Cacheonix annotation information
 */
public class CacheonixMethodVisitor implements MethodVisitor {

   private final List<AnnotationInfo> methodAnnotationInfo = new ArrayList<AnnotationInfo>();

   private final List<Integer> parameterAnnotatonIndices = new ArrayList<Integer>();

   public static final Map<String, CacheonixAnnotation> cacheonixAnnotations = CacheonixAnnotation
           .annotationMapCreator();

   private String annotationDesc;

   private final int access;

   private final String name;

   private final String desc;

   private final String signature;

   private final CacheonixClassReader classReader;

   private boolean bAnnotationPresent = false;

   private boolean bAnnotationParameterPresent = false;


   /**
    * Class constructor
    *
    * @param access      method access
    * @param name        method name
    * @param desc        method description
    * @param signature   method signature
    * @param classReader reader that collects the method annotation information
    */
   public CacheonixMethodVisitor(final int access, final String name,
                                 final String desc, final String signature,
                                 final CacheonixClassReader classReader) {

      this.access = access;
      this.name = name;
      this.desc = desc;
      this.signature = signature;
      this.classReader = classReader;
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitAnnotation(java.lang.String,
     * boolean)
     */
   public AnnotationVisitor visitAnnotation(final String annotationDesc,
                                            final boolean visible) {

      if (visible) {
         final CacheonixAnnotation annotationMetaInfo = cacheonixAnnotations.get(annotationDesc);

         if (annotationMetaInfo != null) {
            this.annotationDesc = annotationDesc;
            bAnnotationPresent = true;
            return new CacheonixAnnotationCollector(annotationMetaInfo,
                    annotationDesc, access, name, desc, signature, this);
         }
      }
      return null;
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitAnnotationDefault()
     */
   public AnnotationVisitor visitAnnotationDefault() {

      return null;
   }


   /*
     * (non-Javadoc)
     *
     * @see
     * org.objectweb.asm.MethodVisitor#visitAttribute(org.objectweb.asm.Attribute
     * )
     */
   public void visitAttribute(final Attribute arg0) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitCode()
     */
   public void visitCode() {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitEnd()
     */
   public void visitEnd() {

      updateAnnotationParameters();
      if (bAnnotationPresent) {

         final MethodMetaData info = new MethodMetaData(name, desc, methodAnnotationInfo, parameterAnnotatonIndices);
         classReader.addMethodInfo(info);
      }
   }


   /**
    * If there is a method annotation, but parameter annotation not present make sure that parameterAnnotatonIndices is
    * populated
    */
   private void updateAnnotationParameters() {

      if (bAnnotationPresent && !bAnnotationParameterPresent) {
         if (annotationDesc.equals(CacheonixAnnotation.CACHE_DATA_SOURCE_DESCRIPTOR)) {
            updateParameterIndicesForCacheDataSource();
         } else if (annotationDesc.equals(CacheonixAnnotation.CACHE_INVALIDATE_DESCRIPTOR)) {
            updateParameterIndicesForCacheInvalidate();
         }
      }
   }


   /**
    * Adds all method parameters to the parameterAnnotatonIndices
    */
   private void updateParameterIndicesForCacheDataSource() {

      parameterAnnotatonIndices.clear();
      final Type[] argTypeArray = Type.getArgumentTypes(desc);
      for (int i = 0; i < argTypeArray.length; ++i) {
         parameterAnnotatonIndices.add(Integer.valueOf(i));
      }
   }


   /**
    * Adds all method parameters  to parameterAnnotationsIndices
    */
   private void updateParameterIndicesForCacheInvalidate() {

      parameterAnnotatonIndices.clear();
      final Type[] argTypeArray = Type.getArgumentTypes(desc);
      for (int i = 0; i < argTypeArray.length; ++i) {
         parameterAnnotatonIndices.add(Integer.valueOf(i));
      }
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int,
     * java.lang.String, java.lang.String, java.lang.String)
     */
   public void visitFieldInsn(final int arg0, final String arg1,
                              final String arg2, final String arg3) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitFrame(int, int,
     * java.lang.Object[], int, java.lang.Object[])
     */
   public void visitFrame(final int arg0, final int arg1, final Object[] arg2,
                          final int arg3, final Object[] arg4) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitIincInsn(int, int)
     */
   public void visitIincInsn(final int arg0, final int arg1) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
     */
   public void visitInsn(final int arg0) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitIntInsn(int, int)
     */
   public void visitIntInsn(final int arg0, final int arg1) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitJumpInsn(int,
     * org.objectweb.asm.Label)
     */
   public void visitJumpInsn(final int arg0, final Label arg1) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitLabel(org.objectweb.asm.Label)
     */
   public void visitLabel(final Label arg0) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitLdcInsn(java.lang.Object)
     */
   public void visitLdcInsn(final Object arg0) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitLineNumber(int,
     * org.objectweb.asm.Label)
     */
   public void visitLineNumber(final int arg0, final Label arg1) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitLocalVariable(java.lang.String,
     * java.lang.String, java.lang.String, org.objectweb.asm.Label,
     * org.objectweb.asm.Label, int)
     */
   public void visitLocalVariable(final String arg0, final String arg1,
                                  final String arg2, final Label arg3, final Label arg4,
                                  final int arg5) {

   }


   /*
     * (non-Javadoc)
     *
     * @see
     * org.objectweb.asm.MethodVisitor#visitLookupSwitchInsn(org.objectweb.asm
     * .Label, int[], org.objectweb.asm.Label[])
     */
   public void visitLookupSwitchInsn(final Label arg0, final int[] arg1,
                                     final Label[] arg2) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitMaxs(int, int)
     */
   public void visitMaxs(final int arg0, final int arg1) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int,
     * java.lang.String, java.lang.String, java.lang.String)
     */
   public void visitMethodInsn(final int arg0, final String arg1,
                               final String arg2, final String arg3) {

   }


   /*
     * (non-Javadoc)
     *
     * @see
     * org.objectweb.asm.MethodVisitor#visitMultiANewArrayInsn(java.lang.String,
     * int)
     */
   public void visitMultiANewArrayInsn(final String arg0, final int arg1) {

   }


   /*
     * Stores the parameter index if the parameter is annotated with @CacheKey
     *
     * @see org.objectweb.asm.MethodVisitor#visitParameterAnnotation(int,
     * java.lang.String, boolean)
     */
   public AnnotationVisitor visitParameterAnnotation(final int parameter,
                                                     final String desc, final boolean visible) {

      if (desc.equals(CacheonixAnnotation.CACHE_KEY_DESCRIPTOR)) {
         bAnnotationParameterPresent = true;
         parameterAnnotatonIndices.add(Integer.valueOf(parameter));
      }
      return null;
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitTableSwitchInsn(int, int,
     * org.objectweb.asm.Label, org.objectweb.asm.Label[])
     */
   public void visitTableSwitchInsn(final int arg0, final int arg1,
                                    final Label arg2, final Label[] arg3) {

   }


   /*
     * (non-Javadoc)
     *
     * @see
     * org.objectweb.asm.MethodVisitor#visitTryCatchBlock(org.objectweb.asm.
     * Label, org.objectweb.asm.Label, org.objectweb.asm.Label,
     * java.lang.String)
     */
   public void visitTryCatchBlock(final Label arg0, final Label arg1,
                                  final Label arg2, final String arg3) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitTypeInsn(int, java.lang.String)
     */
   public void visitTypeInsn(final int arg0, final String arg1) {

   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodVisitor#visitVarInsn(int, int)
     */
   public void visitVarInsn(final int arg0, final int arg1) {

   }


   /**
    * Adds Annotation info for the method
    *
    * @param value Annotation info containing parameters and their values
    */
   public void addMethodAnnotation(final AnnotationInfo value) {

      methodAnnotationInfo.add(value);
   }


   /**
    * Verified whether the annotation is declared for the method
    *
    * @param annotationName
    * @return true if the annotation is declared for the method else false
    */
   public boolean isAnnotationPresent(final String annotationName) {

      for (final AnnotationInfo info : methodAnnotationInfo) {
         if (info.getAnnotationName().equals(annotationName)) {
            return true;
         }
      }
      return false;
   }
}
