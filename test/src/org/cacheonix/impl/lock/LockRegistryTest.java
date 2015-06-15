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

/**
 * Tester for LockRegistry.
 */
public class LockRegistryTest extends CacheonixTestCase {

   private static final String TEST_LOCK = "test.lock";

   private static final String TEST_LOCK_REGION = "test.lock.region";

   private LockRegistry registry;

   private AcquireLockRequest request;

   private static final boolean READ_LOCK = false;

   private static final long UNLOCK_TIMEOUT_MILLIS = 10000L;

   private static final ClusterNodeAddress OWNER_ADDRESS = TestUtils.createTestAddress(1);


   public void testToString() throws Exception {

      assertNotNull(registry.toString());
   }


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new LockRegistry().toString());
   }


   public void testHashCode() throws IOException, ClassNotFoundException {

      final LockQueue lockQueue = registry.getLockQueue(TEST_LOCK_REGION, toBinary(TEST_LOCK));

      lockQueue.getPendingRequests().add(request);

      lockQueue.getPendingRequests().peek();
      assertTrue(registry.hashCode() != 0);
   }


   public void testSerializeDeserializeEmpty() throws IOException, ClassNotFoundException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(registry, ser.deserialize(ser.serialize(registry)));
   }


   public void testSerializeDeserializeWithQueue() throws IOException, ClassNotFoundException {

      final LockQueue lockQueue = registry.getLockQueue(TEST_LOCK_REGION, toBinary(TEST_LOCK));

      lockQueue.getPendingRequests().add(request);

      lockQueue.getPendingRequests().peek();
      assertEquals(lockQueue, registry.getLockQueue(TEST_LOCK_REGION, toBinary(TEST_LOCK)));

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final LockRegistry actual = (LockRegistry) ser.deserialize(ser.serialize(registry));
      assertEquals(registry, actual);
      assertEquals(request, registry.getLockQueue(TEST_LOCK_REGION, toBinary(TEST_LOCK)).getPendingRequests().peek());
   }


   public void testIsReadLock() {

      assertEquals(READ_LOCK, request.isReadLock());
   }


   protected void setUp() throws Exception {

      super.setUp();

      // Request
      final Thread currentThread = Thread.currentThread();
      request = new AcquireLockRequest(TEST_LOCK_REGION, toBinary(TEST_LOCK), OWNER_ADDRESS,
              System.identityHashCode(currentThread), currentThread.getName(), READ_LOCK,
              getClock().currentTime().add(UNLOCK_TIMEOUT_MILLIS));
      request.setSender(OWNER_ADDRESS);

      // Registry
      registry = new LockRegistry();
   }
}
