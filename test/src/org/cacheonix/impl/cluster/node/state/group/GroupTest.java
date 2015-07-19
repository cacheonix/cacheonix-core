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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.logging.Logger;

/**
 * CacheGroup Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>01/20/2009</pre>
 */
public final class GroupTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GroupTest.class); // NOPMD


   private Group group = null;

   private static final String NAME = "my.distributed.cache";

   private static final int BUCKET_COUNT = 10;

   private static final byte REPLICA_COUNT = 3;

   private static final long PARTITION_SIZE_BYTES = 10000L;

   private static final int MAX_SIZE = 100000;

   private ClusterNodeAddress address;


   public void testSetGetPartitionSize() throws Exception {

      assertEquals(PARTITION_SIZE_BYTES, group.getPartitionSizeBytes());
   }


   public void testSerialze() throws IOException, ClassNotFoundException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final Group actual = (Group) serializer.deserialize(serializer.serialize(group));
      assertEquals(group, actual);
      for (byte i = 0; i <= REPLICA_COUNT; i++) {
         for (int j = 0; j < BUCKET_COUNT; j++) {
            assertEquals(group.getBucketOwner(i, j), actual.getBucketOwner(i, j));
         }
      }
   }


   public void testGetName() throws Exception {

      assertEquals(NAME, group.getName());
   }


   public void testGetType() throws Exception {

      assertEquals(Group.GROUP_TYPE_CACHE, group.getGroupType());
   }


   public void testToString() {

      assertNotNull(group.toString());
   }


   public void testHashCode() {

      assertTrue(group.hashCode() != 0);
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(group, ser.deserialize(ser.serialize(group)));
   }


   public void testGetGroupMember() {

      assertNotNull(group.getGroupMember(address));
      assertNull(group.getGroupMember(TestUtils.createTestAddress(10000)));
   }


   protected void setUp() throws Exception {

      super.setUp();
      address = TestUtils.createTestAddress();
      final GroupMember member = new GroupMember(address, true, PARTITION_SIZE_BYTES);
      member.setCacheConfigName(NAME);
      group = new Group(NAME, Group.GROUP_TYPE_CACHE);
      group.configurePartition(REPLICA_COUNT, PARTITION_SIZE_BYTES, MAX_SIZE);
      group.reattachGroupEventSubscriberList(new GroupEventSubscriberList());
      group.addMember(member);
   }


   public String toString() {

      return "GroupTest{" +
              "group=" + group +
              "} " + super.toString();
   }
}
