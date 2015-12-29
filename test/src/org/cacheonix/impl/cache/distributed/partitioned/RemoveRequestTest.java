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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * CachePutRequestImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/29/2008</pre>
 */
public final class RemoveRequestTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "cache.name";

   private static final Binary KEY = toBinary("key");

   private RemoveRequest request = null;

   private ClusterNodeAddress clusterNodeAddress;


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new RemoveRequest().toString());
   }


   public void testGetPartitionName() throws Exception {

      assertEquals(CACHE_NAME, request.getCacheName());
   }


   public void testGetKey() throws Exception {

      assertEquals(KEY, request.getKey());
   }


   public void testToString() {

      assertNotNull(request.toString());
   }


   public void testGetProcessID() throws Exception {

      assertEquals(clusterNodeAddress, request.getSender());
   }


   public void testSerializeDeserialize() throws IOException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
   }


   public void testHashCode() {

      assertTrue(request.hashCode() != 0);
   }


   public void testGetType() {

      assertEquals(Wireable.TYPE_CACHE_REMOVE_REQUEST, request.getWireableType());
   }


   protected void setUp() throws Exception {

      super.setUp();
      clusterNodeAddress = TestUtils.createTestAddress();
      request = new RemoveRequest(clusterNodeAddress, CACHE_NAME, KEY);
   }


   public String toString() {

      return "PartitionRemoveRequestTest{" +
              "request=" + request +
              ", clusterNodeAddress=" + clusterNodeAddress +
              '}';
   }
}
