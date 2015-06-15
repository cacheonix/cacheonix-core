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
package org.cacheonix.util;

import java.util.Arrays;

import org.cacheonix.impl.util.ArrayUtils;

/**
 * Multicast connectivity tester.
 *
 * @noinspection FieldCanBeLocal
 */
public final class MulticastTester {

   private final String[] args;


   public MulticastTester(final String[] args) {

      this.args = ArrayUtils.copy(args);
   }


   /**
    * @noinspection LocalVariableOfConcreteClass
    */
   public static void main(final String[] args) {

      new MulticastTester(args).test();
   }


   /**
    * Tests multicast connectivity.
    */
   public void test() {

   }


   public String toString() {

      return "MulticastTester{" +
              "args=" + (args == null ? null : Arrays.asList(args)) +
              '}';
   }
}
