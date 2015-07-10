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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * CachePutRequestImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/29/2008</pre>
 */
public final class PutRequestTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "cache.name";

   private static final Binary KEY = toBinary("key");

   private static final Binary VALUE = toBinary("value");

   private PutRequest request = null;

   private static final Time EXPIRATION_TIME = new Time(2000, 0);


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new PutRequest().toString());
   }


   public void testSetGetKey() throws Exception {

      request.setKey(KEY);
      assertEquals(KEY, request.getKey());
   }


   public void testToString() {

      assertNotNull(request.toString());
   }


   public void testSetGetValue() throws Exception {

      request.setValue(VALUE);
      assertEquals(VALUE, request.getValue());
   }


   public void testSetGetProcessID() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      request.setSender(clusterNodeAddress);
      assertEquals(clusterNodeAddress, request.getSender());
   }


   public void testGetExpirationTime() {

      assertEquals(EXPIRATION_TIME, request.getExpirationTime());
   }


   public void testGetPutIfAbsent() {

      assertTrue(request.isPutIfAbsent());
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      request.setKey(KEY);
      request.setValue(VALUE);
      request.setSender(TestUtils.createTestAddress());
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
      assertTrue(request.isPutIfAbsent());
   }


   public void testHashCode() {

      request.setKey(KEY);
      request.setValue(VALUE);
      request.setSender(TestUtils.createTestAddress());
      assertTrue(request.hashCode() != 0);
   }


   protected void setUp() throws Exception {

      super.setUp();
      request = new PutRequest(TestUtils.createTestAddress(), CACHE_NAME, KEY, VALUE,
              EXPIRATION_TIME, true);
   }


   public String toString() {

      return "PartitionPutRequestImplTest{" +
              "request=" + request +
              '}';
   }
}
