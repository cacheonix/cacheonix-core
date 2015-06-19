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
package org.cacheonix.impl.net.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.CompressedBinary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.item.PassByCopyBinary;
import org.cacheonix.impl.cache.item.PassByReferenceBinary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.cache.EntryImpl;

/**
 */
public class ClusterResponseTest extends TestCase {

   private Binary value = null;

   private ClusterResponse response = null;


   public void testSetGetValue() throws Exception {

      response.setResult(value);
      assertEquals(value, response.getResult());
   }


   public void testToString() {

      assertNotNull(response.toString());
   }


   public void testHashCode() {

      response.setResult(value);
      assertTrue(response.hashCode() != 0);
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      response.setResult(value);
      assertSerializedEquals(response);
   }


   public void testSerializeDeserializeWithTimestamp() throws IOException, ClassNotFoundException {

      response.setResult(value);
      response.setTimestamp(new Time(1000L, 2000L));
      assertSerializedEquals(response);
   }


   public void testSetTimestamp() throws Exception {

      final Time timestamp = new Time(1000L, 2000L);
      response.setTimestamp(timestamp);
      assertEquals(new Time(1000L, 2000L), response.getTimestamp());
      assertTrue(!new Time(2000L, 4000L).equals(response.getTimestamp()));
   }


   public void testSerializeDeserializeBinaryArrayList() throws IOException, ClassNotFoundException, InvalidObjectException {

      final ArrayList<Binary> result = new ArrayList<Binary>(1);
      result.add(new PassByCopyBinary("value1"));
      result.add(new PassByCopyBinary("value3"));
      response.setResult(result);
      assertSerializedEquals(response);
   }


   public void testSerializeDeserializeBinaryEntry() throws IOException, ClassNotFoundException, InvalidObjectException {

      final EntryImpl entry = new EntryImpl(new PassByCopyBinary("key"), new PassByCopyBinary("value"));
      response.setResult(entry);
      assertSerializedEquals(response);
   }


   public void testSerializeDeserializeBinarySetAsValue() throws IOException, ClassNotFoundException, InvalidObjectException {

      setBinary(new PassByReferenceBinary("value"));
      setBinary(new PassByCopyBinary("value"));
      setBinary(new PassByCopyBinary("value"));
      setBinary(new CompressedBinary("value"));
      assertSerializedEquals(response);
   }


   private static void assertSerializedEquals(final Message response) throws IOException, ClassNotFoundException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(response, ser.deserialize(ser.serialize(response)));
   }


   private void setBinary(final Binary binary) {

      final Set<Binary> binarySet = new HashSet<Binary>(11);
      binarySet.add(binary);
      response.setResult(binarySet);
   }


   protected void setUp() throws Exception {

      super.setUp();
      value = TestUtils.toBinary("value");
      response = new ClusterResponse();
      response.setResult(value);
   }
}
