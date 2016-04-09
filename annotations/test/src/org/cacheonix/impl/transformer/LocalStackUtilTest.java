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
public class LocalStackUtilTest extends TestCase {

   public LocalStackUtil lsMain;


   /**
    * @param name
    */
   public LocalStackUtilTest(final String name) {

      super(name);
   }


   /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
   protected void setUp() throws Exception {

      super.setUp();

      final String methodDesc = "(IDLjava/lang/String;[ICF)Ljava/lang/String;";

      final Type[] args = Type.getArgumentTypes(methodDesc);

      lsMain = new LocalStackUtil(args);
   }


   /**
    * Test method for {@link LocalStackUtil#LocalStackUtil(Type[])} .
    */
   public void testLocalStackUtil() {

      final String methodDesc = "(IDLjava/lang/String;[ICF)Ljava/lang/String;";

      final Type[] args = Type.getArgumentTypes(methodDesc);

      final LocalStackUtil ls = new LocalStackUtil(args);

      assertNotNull(ls);

      assertEquals(7, ls.getParametersStackSize(args));
   }


   /**
    * Test method for {@link LocalStackUtil#getParametersStackSize(Type[])} .
    */
   public void testGetParametersStackSize() {

      final String methodDesc = "(ILjava/lang/String;[ICF)Ljava/lang/String;";

      final Type[] args = Type.getArgumentTypes(methodDesc);

      assertEquals(5, lsMain.getParametersStackSize(args));
   }


   /**
    * Test method for {@link LocalStackUtil#getLogLocalStackPos()}.
    */
   public void testGetLogLocalStackPos() {

      assertEquals(8, lsMain.getLogLocalStackPos());
   }


   /**
    * Test method for {@link LocalStackUtil#getCacheManagerLocalStackPos()} .
    */
   public void testGetCacheManagerLocalStackPos() {

      assertEquals(9, lsMain.getCacheManagerLocalStackPos());
   }


   /**
    * Test method for {@link LocalStackUtil#getValObjLocalStackPos()} .
    */
   public void testGetValObjLocalStackPos() {

      assertEquals(10, lsMain.getValObjLocalStackPos());
   }


   /**
    * Test method for {@link LocalStackUtil#getKeyGenLocalStackPos()} .
    */
   public void testGetKeyGenLocalStackPos() {

      assertEquals(11, lsMain.getKeyGenLocalStackPos());
   }


   /**
    * Test method for {@link LocalStackUtil#getCahceRefLocalStackPos()} .
    */
   public void testGetCahceRefLocalStackPos() {

      assertEquals(12, lsMain.getCacheRefLocalStackPos());
   }


   /**
    * Test method for {@link LocalStackUtil#getExceptionLocalStackPos()} .
    */
   public void testGetExceptionLocalStackPos() {

      assertEquals(13, lsMain.getExceptionLocalStackPos());
   }

}
