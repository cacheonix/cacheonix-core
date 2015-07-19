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
package org.cacheonix.impl.net.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * JavaSerializerTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 2, 2010 1:59:29 PM
 */
public final class JavaSerializerTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(JavaSerializerTest.class); // NOPMD


   public void testSerializeDeserializeInteger() throws Exception {

      final Integer integer = Integer.valueOf(10);
      final JavaSerializer serializer = JavaSerializer.getInstance();
      assertEquals(integer, serializer.deserialize(serializer.serialize(integer)));
   }


   public void testSerializeDeserializeDouble() throws Exception {

      final Double aDouble = Double.valueOf(10);
      final JavaSerializer serializer = JavaSerializer.getInstance();
      assertEquals(aDouble, serializer.deserialize(serializer.serialize(aDouble)));
   }


   public void testSerializeDeserializeByte() throws Exception {

      final Byte byteObject = Byte.valueOf((byte) 10);
      final JavaSerializer serializer = JavaSerializer.getInstance();
      assertEquals(byteObject, serializer.deserialize(serializer.serialize(byteObject)));
   }


   public void testSerializeDeserializeLong() throws Exception {

      final Long longObject = Long.valueOf(10L);
      final JavaSerializer serializer = JavaSerializer.getInstance();
      assertEquals(longObject, serializer.deserialize(serializer.serialize(longObject)));
   }


   public void testSerializeDeserializeString() throws Exception {

      final String object = "Test";
      final JavaSerializer serializer = JavaSerializer.getInstance();
      assertEquals(object, serializer.deserialize(serializer.serialize(object)));
   }


   public void testSerializeDeserializeObject() throws Exception {

      final Object object = new Date();
      final JavaSerializer serializer = JavaSerializer.getInstance();
      assertEquals(object, serializer.deserialize(serializer.serialize(object)));
   }


   public void testSerializeDeserializeArrayList() throws Exception {

      doTestCollection(10, new ArrayList<Integer>(10));
   }


   public void testSerializeDeserializeLinkedList() throws Exception {

      doTestCollection(10, new LinkedList<Integer>());
   }


   public void testSerializeDeserializeHashSet() throws Exception {

      doTestCollection(10, new HashSet<Integer>(10));
   }


   private static void doTestCollection(final int capacity, final Collection<Integer> collection)
           throws IOException {

      for (int i = 0; i < capacity; i++) {
         collection.add(i);
      }
      final JavaSerializer serializer = JavaSerializer.getInstance();
      assertEquals(collection, serializer.deserialize(serializer.serialize(collection)));
   }


   public void testSerializeDeserializeNull() throws Exception {

      final JavaSerializer serializer = JavaSerializer.getInstance();
      assertNull(serializer.deserialize(serializer.serialize(null)));
   }
}
