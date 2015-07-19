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

import junit.framework.TestCase;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 */
public class ByteInstructionTest extends TestCase {

   /**
    * Test method for {@link ByteInstruction#ByteInstruction(int, int)} .
    */
   public void testByteInstruction() {

      final int code = Opcodes.NOP;
      final ByteInstruction bi = new ByteInstruction(code,
              ByteInstruction.START_STACK_INDEX);

      assertNotNull(bi);

      assertEquals(Opcodes.NOP, bi.code);
      assertEquals(ByteInstruction.START_STACK_INDEX, bi.stackIndex);
      assertEquals(ByteInstruction.START_STACK_INDEX, bi.nextStackIndex);
   }


   /**
    * Test method for {@link ByteInstruction#getInstruction(ByteInstruction, Type)} .
    */
   public void testGetInstruction() {

      final int code = Opcodes.NOP;
      final ByteInstruction bi = new ByteInstruction(code,
              ByteInstruction.START_STACK_INDEX);

      ByteInstruction.getInstruction(bi, Type.INT_TYPE);
      assertEquals(Opcodes.ILOAD, bi.code);
      assertEquals(ByteInstruction.START_STACK_INDEX, bi.stackIndex);
      assertEquals(ByteInstruction.START_STACK_INDEX + 1, bi.nextStackIndex);

      int stackIndex = bi.nextStackIndex;
      ByteInstruction.getInstruction(bi, Type.LONG_TYPE);
      assertEquals(Opcodes.LLOAD, bi.code);
      assertEquals(stackIndex, bi.stackIndex);
      assertEquals(stackIndex + 2, bi.nextStackIndex);

      stackIndex = bi.nextStackIndex;
      ByteInstruction.getInstruction(bi, Type.DOUBLE_TYPE);
      assertEquals(Opcodes.DLOAD, bi.code);
      assertEquals(stackIndex, bi.stackIndex);
      assertEquals(stackIndex + 2, bi.nextStackIndex);
   }


   /**
    * Test method for {@link ByteInstruction#getReturnCode(String desc)} .
    */
   public void testGetReturnCode() {

      String desc = "(IF)V";
      int ret = ByteInstruction.getReturnCode(desc);
      assertEquals(ret, Opcodes.RETURN);

      desc = "(IF)I";
      ret = ByteInstruction.getReturnCode(desc);
      assertEquals(ret, Opcodes.IRETURN);

      desc = "(IF)D";
      ret = ByteInstruction.getReturnCode(desc);
      assertEquals(ret, Opcodes.DRETURN);

      desc = "(IF)Ljava/lang/Object;";
      ret = ByteInstruction.getReturnCode(desc);
      assertEquals(ret, Opcodes.ARETURN);

   }

}
