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
package org.cacheonix.impl.annotations.tests;

import java.lang.reflect.Method;

import org.cacheonix.Cacheonix;

/**
 *
 */
public class CacheAnnotationsTest {

   public static final Cacheonix cacheman;

   private static final String EOL = System.getProperty("line.separator");


   static {

      cacheman = Cacheonix.getInstance();
   }


   private static Method findMethod(final Class<?> orgClass,
                                    final String string) {

      Method ret = null;
      final Method[] methods = orgClass.getMethods();

      for (final Method method : methods) {
         if (string.equals(method.getName())) {
            ret = method;
            break;
         }
      }
      return ret;
   }


   /**
    * @param args arguments.
    */
   public static void main(final String[] args) {

      final CacheAnnotatedTest tst = new CacheAnnotatedTest();

      System.out.println(tst.toString()); // NOPMD

      // ///////////////////////////////////////////////
      System.out // NOPMD
              .println("////////////////////////////////////////////////////////////////////////////////////");
      final String valOne = tst.getItem("three");
      System.out.println("Keys: '" + "three" + '\''); // NOPMD
      System.out.println("Got: '" + valOne + '\''); // NOPMD

      final String valTwo = tst.getItem("three");
      System.out.println("CACHED? Keys: '" + "three" + '\''); // NOPMD
      System.out.println("CACHED? Got: '" + valTwo + '\''); // NOPMD

      assert valOne.equals(valTwo);
      // //////////////////////////////////////////////////
      System.out // NOPMD
              .println("/////////////////////////////////////////////////////////////////////////////////////////");

      final Method originalMethodToCall = findMethod(tst.getClass(),
              "orig$Cacheonix$get3Items");
      if (originalMethodToCall != null) {
         try {
            final Object obj = originalMethodToCall.invoke(tst, "one",
                    "four", "fif");
            System.out.println("Invoking renamed original Method " + EOL // NOPMD
                    + "Keys: '" + "one." + "four." + "fif" + '\'');
            System.out.println("Got: '" + obj + '\''); // NOPMD
         } catch (final Exception e) {
            throw new RuntimeException(e);
         }
      }

      // //////////////////////////////////////////////////
      System.out // NOPMD
              .println("//////////////////////////////////////////////////////////////////////////////////");
      final String val3 = tst.get3Items("one", "four", "fif");
      System.out.println("Keys: '" + "one." + "four." + "fif" + '\''); // NOPMD
      System.out.println("Got: '" + val3 + '\''); // NOPMD

      final String val3x = tst.get3Items("one", "four", "fif");
      System.out.println("CACHED? Keys: '" + "one." + "four." + "fif" + '\''); // NOPMD
      System.out.println("CACHED? Got: '" + val3x + '\''); // NOPMD

      // //////////////////////////////////////////////////
      System.out // NOPMD
              .println("////////////////////////////////////////////////////////////////////////////////////");
      final String valMany = tst.getItems("teen", "eight", "six", "one",
              "ven"); // NOPMD
      System.out.println("Keys: '" + "teen." + "eight." + "six." + "one." + "ven" + '\''); // NOPMD
      System.out.println("Got: '" + valMany + '\''); // NOPMD

      final String valm = tst.getItems("teen", "eight", "six", "one", "ven");
      System.out.println("CACHED? Keys: '" + "teen." + "eight." + "six." + "one." + "ven" + '\''); // NOPMD
      System.out.println("CACHED? Got: '" + valm + '\''); // NOPMD

      // //////////////////////////////////////////////////

      // trying to access Cacheonix Cache
      System.out // NOPMD
              .println("/////////////////////////////////////////////////////////////////////////////////////");
      final String valCachedOne = tst.getItem("three");
      System.out.println("Keys: '" + "three" + '\''); // NOPMD
      System.out.println("Got: '" + valCachedOne + '\''); // NOPMD

      // /////////////////////////////////////////////////////

      System.out // NOPMD
              .println("/////////////////////////////////////////////////////////////////////////////////////");
      final String valwauwau = tst.getMoreThanFiveArgs(27, 44.0, "one",
              "ten", 345.67, 3, new Double(9.0D));
      System.out.println("Keys: '" + "one." + '\''); // NOPMD
      System.out.println("Got: '" + valwauwau + '\''); // NOPMD

      final String valwauwau2 = tst.getMoreThanFiveArgs(27, 44.45, "one",
              "ten", 345.67, 3, new Double(9.0D)); // NOPMD
      System.out.println("CAHCED? Keys: '" + "one." + '\''); // NOPMD
      System.out.println("CAHCED? Got: '" + valwauwau2 + '\''); // NOPMD

      // //////////////////////////////////////////////////
      System.out // NOPMD
              .println("//////////////////////////////////////////////////////////////////////////////////////");
      final String valvary = tst.getMixedItems(23, 45.0, "one", "ven");
      System.out.println("Keys: '" + "one." + "ven" + '\''); // NOPMD
      System.out.println("Got: '" + valvary + '\''); // NOPMD

      final String valvary2 = tst.getMixedItems(23, 45.0, "one", "ven");
      System.out.println("CAHCED? Keys: '" + "one." + "ven" + '\''); // NOPMD
      System.out.println("CAHCED? Got: '" + valvary2 + '\''); // NOPMD

   }
}
