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
package org.cacheonix.impl.util;

import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

/**
 *
 */
public final class IOUtilsTest extends TestCase {

   private static final String STRING_FIELD = "String field";

   private static final int INTEGER_FIELD = 99999;

   private static final TestSerializebleObject OBJECT = new TestSerializebleObject(STRING_FIELD, INTEGER_FIELD);


   /**
    */
   public void testCopy() throws IOException, ClassNotFoundException {

      assertEquals(OBJECT, IOUtils.copy(OBJECT));
   }


   public void testInetAddressHashCode() {

      assertEquals(0, IOUtils.inetAddressHashCode(null));
      assertEquals(25260, IOUtils.inetAddressHashCode(IOUtils.getInetAddress("1.1.1.1")));
      assertEquals(25261, IOUtils.inetAddressHashCode(IOUtils.getInetAddress("1.1.1.2")));
   }


   /**
    * Test serializable object
    */
   private static final class TestSerializebleObject implements Serializable {

      private static final long serialVersionUID = -9169871706570078961L;

      private final String stringFelds;

      private final int integerField;


      TestSerializebleObject(final String stringFelds, final int integerField) {

         this.stringFelds = stringFelds;
         this.integerField = integerField;
      }


      public String getStringFelds() {

         return stringFelds;
      }


      public int getIntegerField() {

         return integerField;
      }


      /**
       * @noinspection RedundantIfStatement
       */
      public boolean equals(final Object obj) {

         if (this == obj) {
            return true;
         }
         if (obj == null || getClass() != obj.getClass()) {
            return false;
         }

         final TestSerializebleObject that = (TestSerializebleObject) obj;

         if (integerField != that.integerField) {
            return false;
         }
         if (!stringFelds.equals(that.stringFelds)) {
            return false;
         }

         return true;
      }


      public int hashCode() {

         int result = stringFelds.hashCode();
         result = 29 * result + integerField;
         return result;
      }


      public String toString() {

         return "TestSerializebleObject{" +
                 "integerField=" + integerField +
                 ", stringFelds='" + stringFelds + '\'' +
                 '}';
      }
   }
}
