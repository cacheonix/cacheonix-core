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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * CacheGetRequestImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/29/2008</pre>
 */
public final class GetRequestTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "cache.name";

   private static final Binary KEY = toBinary("key");

   private GetRequest request = null;


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new GetRequest().toString());
   }


   public void testGetPartitionName() throws Exception {

      assertEquals(CACHE_NAME, request.getCacheName());
   }


   public void testSetGetKey() throws Exception {

      request.setKey(KEY);
      assertEquals(KEY, request.getKey());
   }


   public void testToString() {

      assertNotNull(request.toString());
   }


   public void testSetGetProcessID() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      request.setSender(clusterNodeAddress);
      assertEquals(clusterNodeAddress, request.getSender());
   }


   public void testSerializeDeserialize() throws IOException {

      request.setKey(KEY);
      request.setSender(TestUtils.createTestAddress());
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
   }


   public void testHashCode() {

      request.setKey(KEY);
      request.setSender(TestUtils.createTestAddress());
      assertTrue(request.hashCode() != 0);
   }


   protected void setUp() throws Exception {

      super.setUp();
      request = new GetRequest(CACHE_NAME, KEY);
   }


   public String toString() {

      return "PartitionGetRequestTest{" +
              "request=" + request +
              '}';
   }
}
