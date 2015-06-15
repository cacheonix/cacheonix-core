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
package org.cacheonix.impl.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

/**
 *
 */
public final class StringUtilsTest extends TestCase {

   private static final String TEST_MESSAGE = "Test message";

   private static final String HOST = "123.123.123.123";


   /**
    * Tests {@link  StringUtils#daysToMillis(int)}
    */
   public void testDaysToMillis() {

      assertEquals(432000000L, StringUtils.daysToMillis(5));
   }


   /**
    * Tests {@link  StringUtils#isNull(String)}
    */
   public void testIsNull() {

      assertTrue(StringUtils.isNull(null));
   }


   public void testInetAddressToString() throws UnknownHostException {

      assertEquals(HOST, StringUtils.toString(InetAddress.getByName(HOST)));
   }


   /**
    * Tests {@link  StringUtils#toString(Throwable)}
    */
   public void testToString() {

      assertTrue(StringUtils.toString(new Exception(TEST_MESSAGE)).contains(TEST_MESSAGE));
   }


   /**
    * Tests {@link  StringUtils#isValidInteger(String)}
    */
   public void testIsValidInteger() {

      assertTrue(StringUtils.isValidInteger("12345"));
      assertFalse(StringUtils.isValidInteger("Not integer"));
   }


   public void testArrayToString() {

      assertEquals("null", StringUtils.toString((Object[]) null));
      assertEquals("[object # 0]", StringUtils.toString(new Object[]{"object # 0"}));
      assertEquals("[object # 0, object # 1]", StringUtils.toString(new Object[]{"object # 0", "object # 1"}));
   }


   public void testSizeToString() throws Exception {

      assertEquals("null", StringUtils.sizeToString((Collection) null));
      assertEquals("0", StringUtils.sizeToString(new ArrayList(0)));
      final ArrayList<Integer> list = new ArrayList<Integer>(1);
      list.add(1);
      assertEquals("1", StringUtils.sizeToString(list));
   }


   public void testReadBytes() {

      assertEquals(1, StringUtils.readBytes("1bytes"));
      assertEquals(2097152, StringUtils.readBytes("2mb"));

      boolean thrown = false;
      try {

         assertEquals(2000000, StringUtils.readBytes("2blah"));
      } catch (final IllegalArgumentException e) {

         thrown = true;
      }
      assertTrue(thrown);
   }


   public void testReadPercentBytes() {

      assertTrue(StringUtils.readBytes("50%") > 0L);
   }
}
