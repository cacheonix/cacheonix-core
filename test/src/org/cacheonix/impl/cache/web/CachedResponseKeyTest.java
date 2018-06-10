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
package org.cacheonix.impl.cache.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;

import junit.framework.TestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.array.HashMap;

import static org.cacheonix.impl.net.serializer.Serializer.TYPE_JAVA;
import static org.cacheonix.impl.net.serializer.Wireable.TYPE_CACHED_RESPONSE_KEY;

/**
 * A tester for {@link CachedResponseKey}.
 */
public final class CachedResponseKeyTest extends TestCase {

   private static final String TEST_URI = "test/uri";

   private static final Map<String, List<String>> PARAMETER_MAP = createParameterMap(10, 10);

   private static final List<Cookie> COOKIES = createCookies(10);


   private CachedResponseKey responseKey;


   public void testDefaultConstructor() {

      final CachedResponseKey defaultConstructor = new CachedResponseKey();
      assertNotNull(defaultConstructor.toString());
   }


   public void testEquals() {

      assertEquals(responseKey, new CachedResponseKey(TEST_URI, createParameterMap(10, 10), createCookies(10)));
   }


   public void testEqualsEmptyParams() {

      assertEquals(new CachedResponseKey(TEST_URI, createParameterMap(0, 0), createCookies(0)),
              new CachedResponseKey(TEST_URI, createParameterMap(0, 0), createCookies(0)));
   }


   public void testEqualsEmptyParamValues() {

      assertEquals(new CachedResponseKey(TEST_URI, createParameterMap(10, 0), createCookies(0)),
              new CachedResponseKey(TEST_URI, createParameterMap(10, 0), createCookies(0)));
   }


   public void testEqualsNullParam() {

      assertEquals(new CachedResponseKey(TEST_URI, null, null), new CachedResponseKey(TEST_URI, null, null));
   }


   public void testHashCode() {

      assertEquals(1130273223, responseKey.hashCode());
   }


   public void testGetWireableType() {

      assertEquals(TYPE_CACHED_RESPONSE_KEY, responseKey.getWireableType());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(TYPE_JAVA);
      assertEquals(responseKey, ser.deserialize(ser.serialize(responseKey)));
   }


   public void testWriteReadWireNulParameterMap() throws Exception {

      final CachedResponseKey responseKeyWithNullParameterMap = new CachedResponseKey(TEST_URI, null, null);

      final Serializer ser = SerializerFactory.getInstance().getSerializer(TYPE_JAVA);
      assertEquals(responseKeyWithNullParameterMap, ser.deserialize(ser.serialize(responseKeyWithNullParameterMap)));
   }


   public void testToString() {

      assertNotNull(responseKey.toString());
   }


   private static Map<String, List<String>> createParameterMap(final int paramCount, final int paramValueCount) {

      final Map<String, List<String>> result = new HashMap<String, List<String>>(3);
      for (int i = 0; i < paramCount; i++) {

         final List<String> paramArray = new ArrayList<String>(paramValueCount);
         for (int j = 0; j < paramValueCount; j++) {
            paramArray.add("value_" + j);
         }

         final String param = "param_" + i;
         result.put(param, paramArray);
      }
      return result;
   }


   private static List<Cookie> createCookies(final int cookieCount) {

      final List<Cookie> result = new ArrayList<Cookie>(cookieCount);
      for (int i = 0; i < cookieCount; i++) {

         final String name = "test_cookie_name_" + i;
         final String value = "test_cookie_value_" + i;
         final Cookie cookie = new Cookie(name, value);
         result.add(cookie);
      }

      return result;
   }


   public void setUp() throws Exception {

      super.setUp();

      responseKey = new CachedResponseKey(TEST_URI, PARAMETER_MAP, COOKIES);
   }


   public void tearDown() throws Exception {

      responseKey = null;

      super.tearDown();
   }


   public String toString() {

      return "CachedResponseKeyTest{" +
              "cachedResponseKey=" + responseKey +
              "} " + super.toString();
   }
}