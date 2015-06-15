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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Utility class to help get the Byte code instruction and the Stack Index for the local variables for a method
 */
public class ByteInstruction implements Opcodes {

   public int code;

   public int stackIndex;

   public int nextStackIndex;

   public static final int START_STACK_INDEX = 1;


   /**
    * Class constructor
    *
    * @param code       Byte code instruction to transfer value from local variables to the operand stack
    * @param stackIndex index of the local variable that must be read
    */
   public ByteInstruction(final int code, final int stackIndex) {

      this.code = code;
      this.stackIndex = stackIndex;
      this.nextStackIndex = stackIndex;
   }


   /**
    * Class Constructor
    */
   public ByteInstruction() {

      this.code = Opcodes.NOP;
      this.stackIndex = START_STACK_INDEX;
      this.nextStackIndex = START_STACK_INDEX;
   }


   /**
    * Computes the Opcode instruction for a given Type <code><code>org.objectweb.asm.Type</code>
    *
    * @param bi ByteInstruction class that contains the ByteCode and StackIndex
    * @param t  instruction for Type <code><code>org.objectweb.asm.Type</code>
    */
   public static void getInstruction(final ByteInstruction bi, final Type t) {

      bi.code = t.getOpcode(Opcodes.ILOAD);
      bi.stackIndex = bi.nextStackIndex;
      bi.nextStackIndex += t.getSize();
   }


   /**
    * Returns ByteInstruction object that is specific to a particular element type in the array
    *
    * @param argIndex Index of the element in the array
    * @param types    array of Types
    * @return ByteInstruction specific to the element in the array
    */
   public static ByteInstruction getByteInstructionAt(final int argIndex, final Type[] types) {

      final ByteInstruction bi = new ByteInstruction();
      for (int i = 0; i <= argIndex; ++i) {
         ByteInstruction.getInstruction(bi, types[i]);
      }
      return bi;
   }


   /**
    * Returns the Opcode for the specific return type
    *
    * @param desc method description
    * @return Opcode for the specific return type
    */
   public static int getReturnCode(final String desc) {

      final Type t = Type.getReturnType(desc);
      return t.getOpcode(Opcodes.IRETURN);
   }

}
