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

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

import static org.cacheonix.impl.net.serializer.Serializer.TYPE_JAVA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * A tester for {@link DateHeader}.
 */
public final class DateHeaderTest extends TestCase {


   private static final long VALUE = 123456789L;

   private static final String NAME = "name";

   /**
    * Object under test.
    */
   private DateHeader header;


   public void testGetValue() throws Exception {

      assertEquals(VALUE, header.getValue());
   }


   public void testToString() throws Exception {

      assertNotNull(header.toString());
   }


   public void testAddToResponse() throws Exception {

      final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
      header.addToResponse(httpServletResponse);
      verify(httpServletResponse).addDateHeader(NAME, VALUE);
   }


   public void testGetName() throws Exception {

      assertEquals(NAME, header.getName());
   }


   public void testGetWireableType() throws Exception {

      assertEquals(Wireable.TYPE_DATE_HEADER, header.getWireableType());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(TYPE_JAVA);
      assertEquals(header, ser.deserialize(ser.serialize(header)));
   }


   public void testEquals() throws Exception {

      assertEquals(header, new DateHeader(NAME, VALUE));
   }


   public void testHashCode() throws Exception {

      assertEquals(228041706, header.hashCode());
   }


   public void setUp() throws Exception {

      super.setUp();

      header = new DateHeader(NAME, VALUE);
   }


   public void tearDown() throws Exception {

      header = null;

      super.tearDown();
   }


   public String toString() {

      return "DateHeaderTest{" +
              "dateHeader=" + header +
              "} " + super.toString();
   }
}