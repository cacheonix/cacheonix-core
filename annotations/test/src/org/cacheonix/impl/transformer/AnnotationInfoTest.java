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

import junit.framework.TestCase;
import org.objectweb.asm.Type;

/**
 *
 */
public class AnnotationInfoTest extends TestCase {

   public AnnotationInfo infoMain;


   /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
   protected void setUp() throws Exception {

      super.setUp();

      final String annotationName = "StatAnnotation";
      final int methodAccess = 1;
      final String methodName = "methodNameStud";
      final String methodDesc = "methodDescStud";
      final String methodSignature = "meSig";

      infoMain = new AnnotationInfo(annotationName, methodAccess, methodName,
              methodDesc, methodSignature);
   }


   /**
    * Test method for {@link AnnotationInfo#AnnotationInfo(String, int, String, String, String)} .
    */
   public void testAnnotationInfo() {

      final String annotationName = "NewAnnotation";
      final int methodAccess = 1;
      final String methodName = "methodName";
      final String methodDesc = "methodDesc";
      final String methodSignature = null;

      final AnnotationInfo info = new AnnotationInfo(annotationName,
              methodAccess, methodName, methodDesc, methodSignature);

      assertNotNull(info);
      assertSame(annotationName, info.getAnnotationName());
      assertEquals(methodAccess, info.getMethodAccess());
      assertSame(methodDesc, info.getMethodDesc());
      assertSame(methodSignature, info.getMethodSignature());
   }


   /**
    * Test method for {@link AnnotationInfo#addAnnotationParameter(String, String, Type)} .
    */
   public void testAddAnnotationParameter() {

      final String name = "newParam";
      final String val = String.valueOf(34);
      final Type type = Type.INT_TYPE;

      infoMain.addAnnotationParameter(name, val, type);

      final Object oVal = infoMain.getAnnotationParameterValue(name);

      assertSame(val, oVal);
   }


   /**
    * Test method for {@link AnnotationInfo#getAnnotationParameterValue(String)} .
    */
   public void testGetAnnotationParameterValue() {

      final String name = "newParam";
      final String val = String.valueOf(34);
      final Type type = Type.INT_TYPE;

      infoMain.addAnnotationParameter(name, val, type);

      final Object oVal = infoMain.getAnnotationParameterValue(name);

      assertSame(val, oVal);
   }


   /**
    * Test method for {@link AnnotationInfo#isAnnotationParameterPresent(String)} .
    */
   public void testIsAnnotationParameterPresent() {

      final String name = "newParam";
      final String val = String.valueOf(34);
      final Type type = Type.INT_TYPE;

      infoMain.addAnnotationParameter(name, val, type);

      assertTrue(infoMain.isAnnotationParameterPresent(name));
      assertFalse(infoMain.isAnnotationParameterPresent("bogusName"));
   }


   /**
    * Test method for {@link AnnotationInfo#annotationParameterSize()} .
    */
   public void testAnnotationParameterSize() {

      final String name = "newParam";
      final String val = String.valueOf(34);
      final Type type = Type.INT_TYPE;

      infoMain.addAnnotationParameter(name, val, type);

      assertEquals(1, infoMain.annotationParameterSize());
   }
}
