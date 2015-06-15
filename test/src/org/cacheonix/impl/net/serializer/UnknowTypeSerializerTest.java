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
package org.cacheonix.impl.net.serializer;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * UnknowTypeSerializer Tester.
 */
public final class UnknowTypeSerializerTest extends TestCase {

   private static final byte UNKNOWN_TYPE = (byte) 127;

   private static final String OBJECT = "object";

   private UnknownTypeSerializer serializer;


   public void testGetType() throws Exception {

      assertEquals(0, (int) serializer.getType());
   }


   public void testToString() {

      assertNotNull(serializer.toString());
   }


   public void testHashCode() {

      assertTrue(serializer.hashCode() != 0);
   }


   public void testSerializeAlwaysThrowsException() {

      try {
         serializer.serialize(OBJECT);
         fail("Expected exception but it was not thrown");
      } catch (final IOException ignored) { // NOPMD
      }
   }


   public void testDeserialiseAlwaysThrowsException() throws IOException {

      final Serializer javaSerializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = javaSerializer.serialize(OBJECT);
      try {
         serializer.deserialize(bytes);
         fail("Expected exception but it was not thrown");
      } catch (final IOException ignored) { // NOPMD
      }
   }


   protected void setUp() throws Exception {

      super.setUp();
      serializer = new UnknownTypeSerializer(UNKNOWN_TYPE);
   }


   public String toString() {

      return "UnknowTypeSerializerTest{" +
              "serializer=" + serializer +
              "} " + super.toString();
   }
}
