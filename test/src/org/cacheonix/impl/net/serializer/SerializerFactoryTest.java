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
package org.cacheonix.impl.net.serializer;

import junit.framework.TestCase;

/**
 * SerializerFactory Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/04/2008</pre>
 */
public final class SerializerFactoryTest extends TestCase {

   private SerializerFactory factory;

   private static final byte UNKNOWN_CODE = (byte) 127;


   public SerializerFactoryTest(final String name) {

      super(name);
   }


   public void testGetJavaSerializer() {

      final Serializer serializer = factory.getSerializer(Serializer.TYPE_JAVA);
      assertTrue(serializer instanceof JavaSerializer);
      assertEquals(Serializer.TYPE_JAVA, serializer.getType());
   }


   public void testGetUnknownSerializer() {

      final Serializer serializer = factory.getSerializer(UNKNOWN_CODE);
      assertTrue(serializer instanceof UnknownTypeSerializer);
      assertEquals(Serializer.TYPE_UNKNOWN, serializer.getType());
   }


   public void testGetInstance() {

      assertNotNull(factory);
   }


   public void testToString() {

      assertNotNull(factory.toString());
   }


   public void testHashCode() {

      assertTrue(factory.hashCode() != 0);
   }


   protected void setUp() throws Exception {

      super.setUp();
      factory = SerializerFactory.getInstance();
   }


   public String toString() {

      return "SerializerFactoryTest{" +
              "factory=" + factory +
              "} " + super.toString();
   }
}
