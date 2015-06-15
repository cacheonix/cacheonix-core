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
package org.cacheonix.cache.subscriber;

import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import junit.framework.TestCase;

/**
 */
public class EntryModifiedEventTypeTest extends TestCase {

   public void testEquals() throws Exception {

      assertEquals(EntryModifiedEventType.ADD, EntryModifiedEventType.ADD);
   }


   public void testHashCode() throws Exception {

      assertEquals(1, EntryModifiedEventType.ADD.hashCode());
   }


   public void testSerializeDeserialize() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(EntryModifiedEventType.ADD, ser.deserialize(ser.serialize(EntryModifiedEventType.ADD)));
   }


   public void testToString() throws Exception {

      assertNotNull(EntryModifiedEventType.ADD.toString());
   }
}
