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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Instrumentation agent to process <code>cacheonix</code> annotations
 */
public class CacheonixAgent implements ClassFileTransformer {

   /**
    *
    */

   public static void premain(final String arglist, final Instrumentation inst) {

      inst.addTransformer(new CacheonixAgent());
   }


   /*
     * (non-Javadoc)
     *
     * @see
     * java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader
     * , java.lang.String, java.lang.Class, java.security.ProtectionDomain,
     * byte[])
     */
   public byte[] transform(final ClassLoader loader, final String className,
                           final Class<?> classBeingRedefined,
                           final ProtectionDomain protectionDomain,
                           final byte[] classfileBuffer) throws IllegalClassFormatException {

      String strClassName = className;

      try {
         // if (className.startsWith("java")
         // || className.startsWith("cacheonix/"))
         // {
         // return null;
         // }
         // else if (className.startsWith("org/cacheonix/impl")
         // && !className.startsWith("org/cacheonix/impl/examples")
         // && !className.startsWith("org/cacheonix/impl/annotations/test") )
         // {
         // return null;
         // }
         // else if (className.startsWith("sun/nio")
         // || className.startsWith("sun/reflect")
         // || className.startsWith("sun/misc"))
         // {
         // return null;
         // }
         //
         // System.out.println("Processing -- " + className.replace('/',
         // '.'));

         final boolean b1 = className.endsWith("CacheAnnotatedTest");
         final boolean b2 = className.endsWith("CacheAnnotationsTest");

         if (!b1 && !b2) {
            return null;
         }

         strClassName = className.replace('/', '.');

         final ClassReader creader = new ClassReader(classfileBuffer);

         final CacheonixClassReader classCollector = new CacheonixClassReader();

         creader.accept(classCollector, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);

         final Map<String, AnnotationParameter> classLevelInfo = classCollector
                 .getClassLevelAnnotationInfo();

         final Map<String, MethodMetaData> methodsInfo = classCollector
                 .getMethodsInfo();

         if (!classLevelInfo.isEmpty() || !methodsInfo.isEmpty()) {
            // annotated fields present, generate the toString() method
            // Temporary System.out.println("Modifying " + strClassName);
            final ClassWriter writer = new ClassWriter(
                    ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            final CacheonixClassAdapter gen = new CacheonixClassAdapter(
                    writer, strClassName, classLevelInfo, methodsInfo);
            creader.accept(gen, 0); // ClassReader.SKIP_DEBUG
            return writer.toByteArray();

         }
      } catch (final IllegalStateException e) {
         throw new IllegalClassFormatException("Error: " + e.getMessage() + // NOPMD
                 " on class " + strClassName); // NOPMD
      }
      return null;
   }

}
