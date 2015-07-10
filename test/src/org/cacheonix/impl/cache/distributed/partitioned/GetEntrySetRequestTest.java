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
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.array.IntHashSet;

/**
 * GetEntrySetRequest Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 */
public final class GetEntrySetRequestTest extends CacheonixTestCase {

   private static final String CACHE_NAME = "cache.name";

   private GetEntrySetRequest request = null;


   public void testToString() {

      assertNotNull(request.toString());
   }


   public void testSetGetSender() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      request.setSender(clusterNodeAddress);
      assertEquals(clusterNodeAddress, request.getSender());
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      request.setSender(TestUtils.createTestAddress());
      request.setBucketSet(createBucketSet());
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
   }


   private static IntHashSet createBucketSet() {

      final IntHashSet intHashSet = new IntHashSet();
      for (int i = 0; i < 3000; i++) {
         intHashSet.add(i);
      }
      return intHashSet;
   }


   public void testHashCode() {

      assertTrue(request.hashCode() != 0);
   }


   public void testMandatoryDefaultConstructor() {

      assertNotNull(new GetEntrySetRequest().toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      request = new GetEntrySetRequest(CACHE_NAME);
      request.setSender(TestUtils.createTestAddress());
   }


   public String toString() {

      return "GetEntrySetRequestTest{" +
              "request=" + request +
              '}';
   }
}