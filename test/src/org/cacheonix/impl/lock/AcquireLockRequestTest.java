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
package org.cacheonix.impl.lock;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterProcessorState;
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

import static org.cacheonix.impl.lock.AcquireLockRequest.RESULT_LOCK_GRANTED;
import static org.cacheonix.impl.lock.AcquireLockRequest.RESULT_LOCK_WAIT_EXPIRED;
import static org.cacheonix.impl.net.ClusterNodeAddress.createAddress;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tester for AcquireLockRequest.
 */
public class AcquireLockRequestTest extends CacheonixTestCase {

   private static final String TEST_LOCK = "test.lock";

   private static final Binary LOCK_KEY = toBinary(TEST_LOCK);

   private static final long UNLOCK_TIMEOUT_MILLIS = 10000L;

   private static final boolean READ_LOCK = true;

   private static final String TEST_LOCK_REGION = "test.lock.region";

   private AcquireLockRequest request;

   private int threadID;

   private String threadName;

   private static final ClusterNodeAddress OWNER_ADDRESS = TestUtils.createTestAddress(1);

   private Time forcedUnlockTime;


   public void testToString() {

      assertNotNull(request.toString());
   }


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new AcquireLockRequest().toString());
   }


   public void testHashCode() {

      assertTrue(request.hashCode() != 0);
   }


   public void testSerializeDeserialize() throws IOException {

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

      assertEquals(LOCK_KEY, request.getLockKey());
   }


   public void testGetWireableType() {

      assertEquals(Wireable.TYPE_ACQUIRE_LOCK_REQUEST, request.getWireableType());
   }


   public void testIsReadLock() {

      assertEquals(READ_LOCK, request.isReadLock());
   }


   public void testGetUnlockTimeoutMillis() {

      assertEquals(forcedUnlockTime, request.getForcedUnlockTime());
   }


   public void testGetForcedUnlockTime() {

      assertNotNull(request.getForcedUnlockTime());
   }


   public void testValidateFailsIfReceiverSet() throws IOException {

      try {
         request.setReceiver(createAddress("127.0.0.1", 9999));
         request.validate();
      } catch (final InvalidMessageException expected) {
         return;
      }

      fail("Expected " + InvalidMessageException.class + ", but it wasn't thrown");
   }


   public void testCreateLockGrantedResponse() {

      final Response lockGrantedResponse = request.createLockGrantedResponse();
      assertEquals(RESULT_LOCK_GRANTED, lockGrantedResponse.getResult());
   }


   public void testCreateLockWaitExpiredResponse() {

      final Response lockWaitExpiredResponse = request.createLockWaitExpiredResponse();
      assertEquals(RESULT_LOCK_WAIT_EXPIRED, lockWaitExpiredResponse.getResult());
   }


   public void testExecute() {

      // Prepare
      final ClusterProcessor clusterProcessor = mock(ClusterProcessor.class);
      final ClusterProcessorState clusterProcessorState = mock(ClusterProcessorState.class);
      final ReplicatedState replicatedState = mock(ReplicatedState.class);
      final LockRegistry lockRegistry = mock(LockRegistry.class);
      final LockQueue lockQueue = mock(LockQueue.class);

      when(lockRegistry.getLockQueue(TEST_LOCK_REGION, LOCK_KEY)).thenReturn(lockQueue);
      when(replicatedState.getLockRegistry()).thenReturn(lockRegistry);
      when(clusterProcessorState.getReplicatedState()).thenReturn(replicatedState);
      when(clusterProcessor.getProcessorState()).thenReturn(clusterProcessorState);
      when(clusterProcessor.getAddress()).thenReturn(OWNER_ADDRESS);
      request.setProcessor(clusterProcessor);


      // Execute
      request.execute();

      // Verify
      verify(lockQueue).grantLockRequest(request);
      verify(clusterProcessor).post(any(Response.class));
   }


   protected void setUp() throws Exception {

      super.setUp();

      final Thread currentThread = Thread.currentThread();
      final UUID clusterUUID = UUID.randomUUID();
      threadID = System.identityHashCode(currentThread);
      threadName = currentThread.getName();
      forcedUnlockTime = getClock().currentTime().add(UNLOCK_TIMEOUT_MILLIS);
      request = new AcquireLockRequest(TEST_LOCK_REGION, LOCK_KEY, OWNER_ADDRESS, threadID, threadName,
              READ_LOCK, forcedUnlockTime);
      request.setClusterUUID(clusterUUID);
      request.setSender(OWNER_ADDRESS);
   }
}
