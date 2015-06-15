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

import junit.framework.TestCase;
import org.objectweb.asm.Type;


public class MethodMetaDataTest extends TestCase {

   private static final String methodDesc = "(IDLjava/lang/String;[ICF)Ljava/lang/String;";

   private static final String annotationName = CacheonixAnnotation.CACHE_DATA_SOURCE_DESCRIPTOR;

   private static final String namePar = "newParam";

   private static final String valTest = String.valueOf(34);

   public List<AnnotationInfo> annotationInfo;

   public List<Integer> annotationMethodParameterInfo;

   public Type[] argTypes = Type.getArgumentTypes(methodDesc);

   public Type returnType = Type.getReturnType(methodDesc);

   public String name = "getMoreItems";

   public String desc = methodDesc;


   /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
   protected void setUp() throws Exception {

      super.setUp();

      final int methodAccess = 1;
      final String methodName = "methodName";
      final String methodDesc = "methodDesc";
      final String methodSignature = null;

      final AnnotationInfo info = new AnnotationInfo(annotationName,
              methodAccess, methodName, methodDesc, methodSignature);

      final Type type = Type.INT_TYPE;

      info.addAnnotationParameter(namePar, valTest, type);

      annotationInfo = new ArrayList<AnnotationInfo>();

      annotationInfo.add(info);

      annotationMethodParameterInfo = new ArrayList<Integer>();
      annotationMethodParameterInfo.add(new Integer(0));
   }


   /**
    * Test method for (@link org.cacheonix.impl.transformer.MethodMetaData#isAnnotationsPresent()}.
    */
   public void testIsAnnotationsPresent() {

      final MethodMetaData mtd = new MethodMetaData(name, desc,
              annotationInfo, annotationMethodParameterInfo);
      assertTrue(mtd.isAnnotationsPresent());
   }


   /**
    * Test method for {@link MethodMetaData#MethodMetaData(String, String, List)} .
    */
   public void testMethodMetaData() {

      final MethodMetaData mtd = new MethodMetaData(name, desc,
              annotationInfo, annotationMethodParameterInfo);

      assertNotNull(mtd);
      assertSame(name, mtd.getName());
      assertSame(desc, mtd.getDesc());
   }


   /**
    * Test method for {@link MethodMetaData#getAnnotationParameterValue(String, String)} .
    */
   public void testGetAnnotationParameterValue() {

      final MethodMetaData mtd = new MethodMetaData(name, desc,
              annotationInfo, annotationMethodParameterInfo);

      final Object obj = mtd.getAnnotationParameterValue(annotationName,
              namePar);

      assertEquals(valTest, obj.toString());
   }


   /**
    * Test method for {@link MethodMetaData#getMethodParamAnnotationInfo()} .
    */
   public void testGetMethodParamAnnotationInfo() {

      final MethodMetaData mtd = new MethodMetaData(name, desc,
              annotationInfo, annotationMethodParameterInfo);
      assertEquals(mtd.getMethodParamAnnotationInfo(),
              annotationMethodParameterInfo);
   }


   /**
    * Test method for {@link MethodMetaData#isAnnotationsPresent(String)} .
    */
   public void testIsAnnotationPresentWithArg() {

      final MethodMetaData mtd = new MethodMetaData(name, desc,
              annotationInfo, annotationMethodParameterInfo);
      assertTrue(mtd
              .isAnnotationPresent(CacheonixAnnotation.CACHE_DATA_SOURCE_DESCRIPTOR));
   }
}
