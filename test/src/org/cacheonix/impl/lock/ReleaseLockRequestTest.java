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
package org.cacheonix.impl.lock;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * Tester for ReleaseLockRequest.
 */
public class ReleaseLockRequestTest extends CacheonixTestCase {

   private static final String TEST_LOCK = "test.lock";

   private static final String TEST_LOCK_REGION = "test.lock.region";

   private ReleaseLockRequest request;

   private int threadID;

   private String threadName;

   private static final boolean READ_LOCK = false;

   private static final ClusterNodeAddress OWNER_ADDRESS = TestUtils.createTestAddress(1);


   public void testToString() throws Exception {

      assertNotNull(request.toString());
   }


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new ReleaseLockRequest().toString());
   }


   public void testHashCode() throws IOException, ClassNotFoundException {

      assertTrue(request.hashCode() != 0);
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
   }


   public void testGetReceiver() {

      assertFalse(request.isReceiverSet());
   }


   public void testGetThreadName() {

      assertEquals(threadName, request.getOwnerThreadName());
   }


   public void testGetThreadID() {

      assertEquals(threadID, request.getOwnerThreadID());
   }


   public void testGetLockKey() {

      assertEquals(TEST_LOCK_REGION, request.getLockRegionName());
   }


   public void testGetLockRegion() {

      assertEquals(toBinary(TEST_LOCK), request.getLockKey());
   }


   public void testGetWireableType() {

      assertEquals(Wireable.TYPE_RELEASE_LOCK_REQUEST, request.getWireableType());
   }


   public void testIsReadLock() {

      assertEquals(READ_LOCK, request.isReadLock());
   }


   protected void setUp() throws Exception {

      super.setUp();

      final Thread currentThread = Thread.currentThread();
      threadID = System.identityHashCode(currentThread);
      threadName = currentThread.getName();
      request = new ReleaseLockRequest(TEST_LOCK_REGION, toBinary(TEST_LOCK), OWNER_ADDRESS, threadID, threadName, READ_LOCK);
      request.setSender(OWNER_ADDRESS);
   }
}
