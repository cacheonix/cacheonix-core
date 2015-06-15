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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A class visitor that provides the implementation for Cacheonix annotations.
 * <p/>
 * <p/>
 * For generating new methods based on the annotation information this class uses <code>CacheonixMethodsGenerator</code>
 */
public class CacheonixClassAdapter extends ClassAdapter {

   public static final String CACHEONIX_CONFIG_FILE_FIELD = "strCacheonixConfigFile";

   public static final String CACHE_NAME_FIELD = "strCacheonixCacheName";

   private static final String defaultCacheonixConfigFile = "cacheonix-config.xml";

   private String cacheonixConfigFileFieldValue;

   private String cacheonixCacheFieldValue;

   private boolean bCacheonixConfigPresent = false;

   private boolean bCacheonixCacheName = false;

   private final Map<String, MethodMetaData> methodLevelAnnotationInfo;

   private String className;


   /**
    * Class constructor
    *
    * @param cv                    Class visitor that this class delegates the call in the chain for further processing
    * @param className             inernal name of the class
    * @param classLevelAnnotation  Map containing the Annotation Name and the parameter for the class
    * @param methodLevelAnnotation Map containg the Annotation information for each method in the class
    */
   public CacheonixClassAdapter(final ClassVisitor cv, final String className,
                                final Map<String, AnnotationParameter> classLevelAnnotation,
                                final Map<String, MethodMetaData> methodLevelAnnotation) {

      super(cv);

      methodLevelAnnotationInfo = new HashMap<String, MethodMetaData>(methodLevelAnnotation);

      // if there are any method level annotations but some or all of the
      // class level annotations are missing then populate the
      // class level annotation values with default values

      if (!methodLevelAnnotationInfo.isEmpty()) {
         AnnotationParameter param = classLevelAnnotation
                 .get(CacheonixAnnotation.CACHECONFIGURATION_CONFIGURATION_PATH);
         if (param == null) {
            cacheonixConfigFileFieldValue = defaultCacheonixConfigFile;
         } else {
            cacheonixConfigFileFieldValue = param.oVal.toString();
         }

         param = classLevelAnnotation
                 .get(CacheonixAnnotation.CACHECONFIGURATION_CACHE_NAME);
         if (param == null) {
            cacheonixCacheFieldValue = className.replace('/', '.');
         } else {
            cacheonixCacheFieldValue = param.oVal.toString();
         }

      }
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.ClassVisitor#visitField(int, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.Object)
     */
   public FieldVisitor visitField(final int access, final String name,
                                  final String desc, final String signature, final Object value) {

      if (name.equals(CACHEONIX_CONFIG_FILE_FIELD)) {
         bCacheonixConfigPresent = true;
      }
      if (name.equals(CACHE_NAME_FIELD)) {
         bCacheonixCacheName = true;
      }

      return cv.visitField(access, name, desc, signature, value);
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.ClassVisitor#visitEnd()
     */
   public void visitEnd() {

      final String signature = null;

      // Add CacheonixConfig Field Now
      if (!bCacheonixConfigPresent) {
         final FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE
                 | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                 CACHEONIX_CONFIG_FILE_FIELD, Type.getType(String.class)
                 .getDescriptor(), signature,
                 cacheonixConfigFileFieldValue);
         if (fv != null) {
            fv.visitEnd();
         }
      }

      // Add CacheonixCacheName Field Now
      if (!bCacheonixCacheName) {
         final FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE
                 | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, CACHE_NAME_FIELD,
                 Type.getType(String.class).getDescriptor(), signature,
                 cacheonixCacheFieldValue);
         if (fv != null) {
            fv.visitEnd();
         }
      }

      cv.visitEnd();
   }


   /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String[])
     */
   public MethodVisitor visitMethod(final int access, final String name,
                                    final String desc, final String signature, final String[] exceptions) {

      final String key = name + desc;

      // System.out.println(" ~~~~~~~~~~~~~~~~ Visit name and desc: " + name +
      // "  " + desc);
      // Type[] args = Type.getArgumentTypes(desc);
      // System.out.println(" ~~~~~~~~~~~~~~~~ Arguments are: " +
      // Arrays.toString(args));
      //
      // Object to = args;
      //
      String newName = name;
      final MethodMetaData metadata = methodLevelAnnotationInfo.get(key);
      if (metadata != null) {
         newName = "orig$Cacheonix$" + name;
         // System.out.println(" ~~~~~~~~~~~~~~~~ Processing name and desc: "
         // + name + "  " + desc);
         // System.out.println(" ~~~~~~~~~~~~~~~~ Replacing with name and desc: "
         // + newName + "  " + desc);

         if (metadata.isAnnotationPresent(CacheonixAnnotation.CACHE_DATA_SOURCE_DESCRIPTOR)) {
            CacheonixMethodGenerator.generateCacheAddBody(cv, className, access,
                    desc, signature, exceptions, name, newName, metadata, cacheonixCacheFieldValue);
         } else if (metadata.isAnnotationPresent(CacheonixAnnotation.CACHE_INVALIDATE_DESCRIPTOR)) {
            CacheonixMethodGenerator.generateCacheRemoveBody(cv, className, access,
                    desc, signature, exceptions, name, newName, metadata, cacheonixCacheFieldValue);
         }
      }
      return super.visitMethod(access, newName, desc, signature, exceptions);
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

      className = name;
      cv.visit(version, access, className, signature, superName, interfaces);
   }
}
