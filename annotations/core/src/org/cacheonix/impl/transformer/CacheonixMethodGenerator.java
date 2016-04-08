/**
 *
 */
/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Injects the code in the method to look into the Cacheonix cache based on the annotation parameters first before
 * requesting the data from the source. User's method is renamed to the format: orig$Cacheonix$methodName refer to
 * <code>LocalStackUtil</code> for more information on the generated method body.
 */
public class CacheonixMethodGenerator implements Opcodes {

   /**
    * Generates a new method body for implementing DataSource with the old name to look into the Cacheonix cache first
    * before calling the original method
    *
    * @param cv                       ClassVisitor that this class delegates the calls to
    * @param className                Name of the class for which the method is being generated
    * @param access                   Method level access
    * @param desc                     Method descriptor
    * @param signature                Method signature
    * @param exceptions               Any exceptions that the method can throw
    * @param name                     original name of the method
    * @param newName                  the original method renamed to the format:orig$Cacheonix$methodName
    * @param metaData                 Annotation information for the method
    * @param cacheonixCacheFieldValue cacheName specified at the class level
    */
   public static void generateCacheAddBody(final ClassVisitor cv,
                                           final String className, final int access, final String desc,
                                           final String signature, final String[] exceptions,
                                           final String name, final String newName,
                                           final MethodMetaData metaData, final String cacheonixCacheFieldValue) {

      final Type[] args = Type.getArgumentTypes(desc);
      final LocalStackUtil stackFrame = new LocalStackUtil(args);
      int expirationTime = CacheonixAnnotation.CACHEDATASOURCE_EXPIRATION_TIME_MILLIS_DEFAULT_VALUE;

      if (metaData.isAnnotationsPresent()) {
         final Object expTime = metaData.getAnnotationParameterValue(
                 CacheonixAnnotation.CACHE_DATA_SOURCE_DESCRIPTOR,
                 CacheonixAnnotation.CACHEDATASOURCE_EXPIRATION_TIME_MILLIS);
         if (expTime != null) {
            expirationTime = Integer.parseInt(expTime.toString());
         }
      }

      // Start
      final MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
      mv.visitCode();

      final Label l0 = new Label();
      final Label l1 = new Label();
      final Label l2 = new Label();

      mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");

      final String classCan = 'L' + className + ';';

      mv.visitLdcInsn(Type.getType(classCan));
      mv.visitMethodInsn(INVOKESTATIC, "org/cacheonix/impl/util/logging/Logger",
              "getLogger",
              "(Ljava/lang/Class;)Lorg/cacheonix/impl/util/logging/Logger;");

      mv.visitVarInsn(ASTORE, stackFrame.getLogLocalStackPos());

      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, stackFrame.getValObjLocalStackPos()); // val

      generateKeyAggregationSequence(mv, args, stackFrame, cacheonixCacheFieldValue, metaData.getMethodParamAnnotationInfo());

      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, stackFrame.getCacheRefLocalStackPos()); // cache

//		printingToSysout(mv, "!!!!!  G E N E R A T E D   !!!!!! '" + name + "' is Called");

      mv.visitLabel(l0);
      // Config file from annotation on Class level
      mv.visitFieldInsn(GETSTATIC, className,
              CacheonixClassAdapter.CACHEONIX_CONFIG_FILE_FIELD,
              "Ljava/lang/String;");

      mv.visitMethodInsn(INVOKESTATIC, "cacheonix/cache/CacheManager",
              "getInstance",
              "(Ljava/lang/String;)Lcacheonix/cache/CacheManager;");
      mv.visitVarInsn(ASTORE, stackFrame.getCacheManagerLocalStackPos()); // inst

      // CATCH Block
      mv.visitLabel(l1);
      final Label l3 = new Label();
      mv.visitJumpInsn(GOTO, l3);
      mv.visitLabel(l2);
      mv.visitVarInsn(ASTORE, stackFrame.getExceptionLocalStackPos()); // exception
      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, stackFrame.getCacheManagerLocalStackPos()); // inst
      mv.visitVarInsn(ALOAD, stackFrame.getLogLocalStackPos()); // log
      mv.visitLdcInsn(">>>>> Exception getting CacheManager ");
      mv.visitVarInsn(ALOAD, stackFrame.getExceptionLocalStackPos()); // exception
      mv.visitMethodInsn(INVOKEVIRTUAL,
              "org/cacheonix/impl/util/logging/Logger",
              "e",
              "(Ljava/lang/Object;Ljava/lang/Throwable;)V");
      // END OF TRY CACTCH

      mv.visitLabel(l3);

//		printingToSysout(mv, "!!!!!  INST is NOT NULL   !!!!!! '" + name + "' is Called");

      mv.visitVarInsn(ALOAD, stackFrame.getCacheManagerLocalStackPos()); // inst
      final Label l4 = new Label();
      mv.visitJumpInsn(IFNULL, l4);
      mv.visitVarInsn(ALOAD, stackFrame.getCacheManagerLocalStackPos()); // inst

      mv.visitFieldInsn(GETSTATIC, className,
              CacheonixClassAdapter.CACHE_NAME_FIELD, "Ljava/lang/String;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "cacheonix/cache/CacheManager",
              "getCache", "(Ljava/lang/String;)Lcacheonix/cache/Cache;");

      mv.visitVarInsn(ASTORE, stackFrame.getCacheRefLocalStackPos()); // cache
      mv.visitVarInsn(ALOAD, stackFrame.getCacheRefLocalStackPos()); // cache
      mv.visitVarInsn(ALOAD, stackFrame.getKeyGenLocalStackPos()); // key
      mv.visitMethodInsn(INVOKEINTERFACE, "cacheonix/cache/Cache", "get",
              "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitVarInsn(ASTORE, stackFrame.getValObjLocalStackPos()); // val

      mv.visitVarInsn(ALOAD, stackFrame.getValObjLocalStackPos()); // val
      mv.visitJumpInsn(IFNONNULL, l4);

//		printingToSysout(mv, "!!!!!  VALUE IN CACHE IS NULL   !!!!!! '" + name + "' is Called");

      generateMethodParameterLoadingSequence(mv, args);

      mv.visitMethodInsn(INVOKESPECIAL, className, newName, desc);

      mv.visitVarInsn(ASTORE, stackFrame.getValObjLocalStackPos()); // val
      mv.visitVarInsn(ALOAD, stackFrame.getCacheRefLocalStackPos()); // cache
      mv.visitVarInsn(ALOAD, stackFrame.getKeyGenLocalStackPos()); // key
      mv.visitVarInsn(ALOAD, stackFrame.getValObjLocalStackPos()); // val

      if (expirationTime == -1) {
         mv.visitMethodInsn(INVOKEINTERFACE, "cacheonix/cache/Cache", "put",
                 "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
      } else {
         mv.visitLdcInsn(Long.valueOf(expirationTime));
         mv
                 .visitMethodInsn(INVOKEINTERFACE, "cacheonix/cache/Cache",
                         "put",
                         "(Ljava/lang/Object;Ljava/lang/Object;J)Ljava/lang/Object;");
      }

      mv.visitInsn(POP);

      mv.visitLabel(l4);

      mv.visitVarInsn(ALOAD, stackFrame.getValObjLocalStackPos()); // val

      // Return type
      final String retType = Type.getReturnType(desc).getInternalName();
      mv.visitTypeInsn(CHECKCAST, retType);
      mv.visitInsn(ARETURN);
      mv.visitMaxs(6, 10);
      mv.visitEnd();

   }


   /**
    * Generate Byte Instructions for loading the arguments for the method
    *
    * @param mv   Method visitor that writes the byte instructions
    * @param args array of Method arguments Types
    */
   private static void generateMethodParameterLoadingSequence(
           final MethodVisitor mv, final Type[] args) {

      mv.visitVarInsn(ALOAD, 0); // this
      final ByteInstruction bi = new ByteInstruction();
      for (int i = 0; i < args.length; ++i) {
         ByteInstruction.getInstruction(bi, args[i]);
         mv.visitVarInsn(bi.code, bi.stackIndex);
      }
   }


   /**
    * Generates the byte instructions for the Aggregate key generated to be used by the Cacheonix cache
    *
    * @param mv              MethodVisitor that writes byte instructions
    * @param types           array of Method arguments Types
    * @param stackFrame      Contains the offset for the local variables in the generated method
    * @param list
    * @param strSyntheticKey
    */
   private static void generateKeyAggregationSequence(final MethodVisitor mv,
                                                      final Type[] types, final LocalStackUtil stackFrame,
                                                      final String strSyntheticKey, final List<Integer> list) {

      final String strGeneratorKey = "org/cacheonix/impl/transformer/GeneratedKey";

      mv.visitTypeInsn(NEW, strGeneratorKey);

      mv.visitInsn(DUP);

      int keyElements = 1; //For Synthetic key
      keyElements += list.size();

      mv.visitIntInsn(BIPUSH, keyElements);
      mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

      //Add Synthetic key to the GeneratorKey
      mv.visitInsn(DUP);
      mv.visitIntInsn(BIPUSH, 0);

      final Type tpString = Type.getType(String.class);

      mv.visitLdcInsn(strSyntheticKey);

      // Call function
      String argDescriptor = getKeyGenArgumentDescrStr(tpString);
      mv.visitMethodInsn(INVOKESTATIC, strGeneratorKey, "addKeyElement", argDescriptor);
      // Push (store) result to the stack
      mv.visitInsn(AASTORE);


      for (int i = 0, j = 1; i < list.size(); ++i, ++j) {
         //Get the argument index which is marked with @CacheKey
         final int argTypeIndex = list.get(i).intValue();

         mv.visitInsn(DUP);
         mv.visitIntInsn(BIPUSH, j);

         final ByteInstruction bi = ByteInstruction.getByteInstructionAt(argTypeIndex, types);
         mv.visitVarInsn(bi.code, bi.stackIndex);

         // Call function
         argDescriptor = getKeyGenArgumentDescrStr(types[argTypeIndex]);
         mv.visitMethodInsn(INVOKESTATIC, strGeneratorKey, "addKeyElement", argDescriptor);
         // Push (store) result to the stack
         mv.visitInsn(AASTORE);
         // ---

      }

      // Call Constructor function with array of arguments
      mv.visitMethodInsn(INVOKESPECIAL, strGeneratorKey, "<init>", "([Ljava/lang/Object;)V");
      // Store new GeneratedKey to Local ()
      mv.visitVarInsn(ASTORE, stackFrame.getKeyGenLocalStackPos());

   }


   /**
    * Returns the descriptor name for the Argument type.
    *
    * @param t Type of the argument.
    * @return returns "(X)Ljava/lang/Object;" for all primitive types where X = I for int, B for byte etc., for Objects
    *         it returns "(Ljava/lang/Object;)Ljava/lang/Object;"
    */
   private static String getKeyGenArgumentDescrStr(final Type t) {

      if (t.getDescriptor().length() == 1) {
         return '(' + t.getDescriptor() + ")Ljava/lang/Object;";
      }
      return "(Ljava/lang/Object;)Ljava/lang/Object;";
   }


   /**
    * Generated the byte instructions to write the message to System out stream
    *
    * @param mv      Method Visitor that writes byte instructions
    * @param message message that needs to be written to the out stream
    */
   public static void printingToSysout(final MethodVisitor mv,
                                       final String message) {

      mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
              "Ljava/io/PrintStream;");
      mv.visitLdcInsn(message);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
              "println", "(Ljava/lang/String;)V");
   }


   /**
    * Generates a new method body for implementing CacheInvalidate with the old name to look into the Cacheonix cache
    * first before calling the original method
    *
    * @param cv                       ClassVisitor that this class delegates the calls to
    * @param className                Name of the class for which the method is being generated
    * @param access                   Method level access
    * @param desc                     Method descriptor
    * @param signature                Method signature
    * @param exceptions               Any exceptions that the method can throw
    * @param name                     original name of the method
    * @param newName                  the original method renamed to the format:orig$Cacheonix$methodName
    * @param metaData                 Annotation information for the method
    * @param cacheonixCacheFieldValue cacheName specified at the class level
    */
   public static void generateCacheRemoveBody(final ClassVisitor cv,
                                              final String className, final int access, final String desc,
                                              final String signature, final String[] exceptions,
                                              final String name, final String newName,
                                              final MethodMetaData metaData, final String cacheonixCacheFieldValue) {

      final Type[] args = Type.getArgumentTypes(desc);
      final LocalStackUtil stackFrame = new LocalStackUtil(args);

      // Start
      final MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
      mv.visitCode();

      final Label l0 = new Label();
      final Label l1 = new Label();
      final Label l2 = new Label();

      mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");

      final Label l3 = new Label();
      final Label l4 = new Label();
      final Label l5 = new Label();

      mv.visitTryCatchBlock(l3, l4, l5, "java/lang/Exception");

      final String classCan = 'L' + className + ';';

      mv.visitLdcInsn(Type.getType(classCan));
      mv.visitMethodInsn(INVOKESTATIC, "org/cacheonix/impl/util/logging/Logger",
              "getLogger",
              "(Ljava/lang/Class;)Lorg/cacheonix/impl/util/logging/Logger;");

      mv.visitVarInsn(ASTORE, stackFrame.getCILogLocalStackPos());     // Log
      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, stackFrame.getCICacheManagerLocalStackPos());     // inst

      // try
      mv.visitLabel(l0);

      mv.visitFieldInsn(GETSTATIC, className,
              CacheonixClassAdapter.CACHEONIX_CONFIG_FILE_FIELD,
              "Ljava/lang/String;");
      mv.visitMethodInsn(INVOKESTATIC, "cacheonix/cache/CacheManager",
              "getInstance",
              "(Ljava/lang/String;)Lcacheonix/cache/CacheManager;");

      mv.visitVarInsn(ASTORE, stackFrame.getCICacheManagerLocalStackPos());     // inst
      // catch in
      mv.visitLabel(l1);
      mv.visitJumpInsn(GOTO, l3);
      // catch out
      mv.visitLabel(l2);
      mv.visitVarInsn(ASTORE, stackFrame.getCIExceptionLocalStackPos());     // Exception1
      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, stackFrame.getCIExceptionLocalStackPos());     // inst <- null
      mv.visitVarInsn(ALOAD, stackFrame.getCILogLocalStackPos());      // Log
      mv.visitLdcInsn(">>>>> Exception getting CacheManager ");
      mv.visitVarInsn(ALOAD, stackFrame.getCIExceptionLocalStackPos());      // Exception
      mv.visitMethodInsn(INVOKEVIRTUAL,
              "org/cacheonix/impl/util/logging/Logger",
              "e",
              "(Ljava/lang/Object;Ljava/lang/Throwable;)V");
      // END OF TRY CACTCH

      // try
      mv.visitLabel(l3);

//        printingToSysout(mv, "!!!!!  INVALIDATE | INST is NOT NULL   !!!!!! '" + name + "' is Called");

      mv.visitVarInsn(ALOAD, stackFrame.getCICacheManagerLocalStackPos());      // inst
      final Label l6 = new Label();
      mv.visitJumpInsn(IFNULL, l6);

      // Key Loop
      generateKeyAggregationSequence(mv, args, stackFrame, cacheonixCacheFieldValue, metaData.getMethodParamAnnotationInfo());

      mv.visitVarInsn(ALOAD, stackFrame.getCICacheManagerLocalStackPos());      // inst

      mv.visitFieldInsn(GETSTATIC, className,
              CacheonixClassAdapter.CACHE_NAME_FIELD, "Ljava/lang/String;");

      mv.visitMethodInsn(INVOKEVIRTUAL, "cacheonix/cache/CacheManager",
              "getCache", "(Ljava/lang/String;)Lcacheonix/cache/Cache;");

      mv.visitVarInsn(ASTORE, stackFrame.getCICacheRefLocalStackPos());     // cache
      mv.visitVarInsn(ALOAD, stackFrame.getCICacheRefLocalStackPos());      // cache
      mv.visitJumpInsn(IFNULL, l6);
      mv.visitVarInsn(ALOAD, stackFrame.getCICacheRefLocalStackPos());      // cache
      mv.visitVarInsn(ALOAD, stackFrame.getCIKeyGenLocalStackPos());      // Key
      mv.visitMethodInsn(INVOKEINTERFACE, "cacheonix/cache/Cache", "remove",
              "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitInsn(POP);              // Ignore result

      mv.visitLabel(l4);
      mv.visitJumpInsn(GOTO, l6);
      mv.visitLabel(l5);

      mv.visitVarInsn(ASTORE, stackFrame.getCIEExceptionLocalStackPos());     // EException
      mv.visitVarInsn(ALOAD, stackFrame.getCILogLocalStackPos());      // Log
      mv.visitLdcInsn(">>>>> Exception while removing key ");
      mv.visitVarInsn(ALOAD, stackFrame.getCIEExceptionLocalStackPos());      // EException
      mv.visitMethodInsn(INVOKEVIRTUAL, "org/cacheonix/impl/util/logging/Logger",
              "e",
              "(Ljava/lang/Object;Ljava/lang/Throwable;)V");
      mv.visitLabel(l6);

      //
      generateMethodParameterLoadingSequence(mv, args);

      mv.visitMethodInsn(INVOKESPECIAL, className, newName, desc);

      //
      final int op = ByteInstruction.getReturnCode(desc);
      mv.visitInsn(op);
      mv.visitMaxs(6, 9);
      mv.visitEnd();

   }
}
