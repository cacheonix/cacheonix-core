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
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * ReplaceIfMappedRequestTest Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 */
public final class ReplaceIfMappedRequestTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "cache.name";

   private static final Binary KEY = toBinary("key");

   private static final Binary NEW_VALUE = toBinary("new_value");

   private ReplaceIfMappedRequest request = null;

   private ClusterNodeAddress clusterNodeAddress;


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new ReplaceIfMappedRequest().toString());
   }


   public void testGetPartitionName() {

      assertEquals(CACHE_NAME, request.getCacheName());
   }


   public void testGetKey() {

      assertEquals(KEY, request.getKey());
   }


   public void testGetValue() {

      assertEquals(NEW_VALUE, request.getValue());
   }


   public void testToString() {

      assertNotNull(request.toString());
   }


   public void testGetProcessID() {

      assertEquals(clusterNodeAddress, request.getSender());
   }


   public void testSerializeDeserialize() throws IOException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final ReplaceIfMappedRequest deserializedRequest = (ReplaceIfMappedRequest) ser.deserialize(ser.serialize(request));
      assertEquals(request, deserializedRequest);
      assertEquals(request.getValue(), deserializedRequest.getValue());
   }


   public void testHashCode() {

      assertTrue(request.hashCode() != 0);
   }


   public void testGetType() {

      assertEquals(Wireable.TYPE_CACHE_REPLACE_IF_MAPPED_REQUEST, request.getWireableType());
   }


   protected void setUp() throws Exception {

      super.setUp();
      clusterNodeAddress = TestUtils.createTestAddress();
      request = new ReplaceIfMappedRequest(clusterNodeAddress, CACHE_NAME, KEY, NEW_VALUE);
   }


   public String toString() {

      return "ReplaceIfMappedRequestTest{" +
              "request=" + request +
              ", clusterNodeAddress=" + clusterNodeAddress +
              "} " + super.toString();
   }
}
