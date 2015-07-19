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
package org.cacheonix.impl.cluster.node.state.group;

import java.io.IOException;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import junit.framework.TestCase;

/**
 * CacheGroupMember Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>01/21/2009</pre>
 */
public final class GroupMemberTest extends TestCase {

   private GroupMember member = null;

   private final ClusterNodeAddress testAddress = TestUtils.createTestAddress();

   private static final boolean PARTITION_CONTIBUTOR = true;

   private static final String TEST_CACHE = "test.cache";

   private static final long HEAP_SIZE_BYTES = 1000L;


   public void testGetAddress() throws Exception {

      assertEquals(testAddress, member.getAddress());
   }


   public void testSetPartitionContibutor() throws Exception {

      assertTrue(member.isPartitionContributor());
   }


   public void testGetHeapSizeBytes() throws Exception {

      assertEquals(HEAP_SIZE_BYTES, member.getHeapSizeBytes());
   }


   public void testToString() {

      assertNotNull(member.toString());
   }


   public void testHashCode() {

      assertTrue(member.getHeapSizeBytes() != 0);
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(member, ser.deserialize(ser.serialize(member)));
   }


   public void testGetCacheConfigName() {

      assertEquals(TEST_CACHE, member.getCacheConfigName());
   }


   protected void setUp() throws Exception {

      super.setUp();
      member = new GroupMember(testAddress, PARTITION_CONTIBUTOR, HEAP_SIZE_BYTES);
      member.setCacheConfigName(TEST_CACHE);
   }


   public String toString() {

      return "GroupMemberTest{" +
              "member=" + member +
              ", testAddress=" + testAddress +
              "} " + super.toString();
   }
}
