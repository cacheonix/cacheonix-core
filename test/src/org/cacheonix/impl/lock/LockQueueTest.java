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
package org.cacheonix.impl.lock;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.distributed.partitioned.GetRequest;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * Tester for LockQueue.
 */
public class LockQueueTest extends CacheonixTestCase {

   private static final String TEST_LOCK = "test.lock";

   private static final String TEST_REGION = "test.region";

   private LockQueue lockQueue;

   private AcquireLockRequest request;

   private static final boolean READ_LOCK = true;

   private int ownerThreadID;

   private String threadName;

   private ClusterNodeAddress ownerAddress;


   public void testToString() throws Exception {

      assertNotNull(lockQueue.toString());
   }


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new GetRequest().toString());
   }


   public void testHashCode() throws IOException, ClassNotFoundException {

      assertTrue(lockQueue.hashCode() != 0);
   }


   public void testSerializeDeserializeEmpty() throws IOException, ClassNotFoundException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(lockQueue, ser.deserialize(ser.serialize(lockQueue)));
   }


   public void testAddLockRequest() throws IOException, ClassNotFoundException {

      lockQueue.getPendingRequests().add(request);
      assertEquals(request, lockQueue.getPendingRequests().peek());
   }


   public void testSerializeDeserializeWithQueue() throws IOException, ClassNotFoundException {

      lockQueue.getPendingRequests().add(request);
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final LockQueue actual = (LockQueue) ser.deserialize(ser.serialize(lockQueue));
      assertEquals(lockQueue, actual);
      assertEquals(request, lockQueue.getPendingRequests().peek());
   }


   public void testIsReadLock() {

      assertEquals(READ_LOCK, request.isReadLock());
   }


   public void testRegisterForcedRelease() throws Exception {

      // Grant read
      lockQueue.grantLockRequest(request);

      // Get read owner
      final LockOwner owner = lockQueue.getReadLockOwners().get(0);

      // Register
      lockQueue.registerForcedRelease(owner);

      //
      assertTrue(lockQueue.isRegisteredInForcedReleases(owner));

      final ReleaseLockRequest releaseLockRequest = new ReleaseLockRequest(
              TEST_REGION, toBinary(TEST_LOCK), ownerAddress, ownerThreadID, threadName, READ_LOCK);
      releaseLockRequest.setSender(ownerAddress);
      lockQueue.releaseLock(releaseLockRequest);

      assertFalse(lockQueue.isRegisteredInForcedReleases(owner));
   }


   protected void setUp() throws Exception {

      super.setUp();

      // Request
      final Thread currentThread = Thread.currentThread();
      ownerThreadID = System.identityHashCode(currentThread);
      threadName = currentThread.getName();
      ownerAddress = TestUtils.createTestAddress(1);
      request = new AcquireLockRequest(TEST_REGION, toBinary(TEST_LOCK), ownerAddress, ownerThreadID, threadName, READ_LOCK, getClock().currentTime().add(10000L));
      request.setSender(ownerAddress);


      // Registry
      lockQueue = new LockQueue();
   }
}
