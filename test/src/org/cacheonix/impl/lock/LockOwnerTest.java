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
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 */
public class LockOwnerTest extends CacheonixTestCase {

   private LockOwner lockOwner;

   private int threadID;

   private String threadName;

   private ClusterNodeAddress address;

   private Time unlockTime;


   public void testGetThreadID() {

      assertEquals(threadID, lockOwner.getThreadID());
   }


   public void testGetAddress() {

      assertEquals(address, lockOwner.getAddress());
   }


   public void testGetThreadName() {

      assertEquals(threadName, lockOwner.getThreadName());
   }


   public void testGetEntryCount() {

      assertEquals(0, lockOwner.getEntryCount());
   }


   public void testIncrementEntryCount() {

      lockOwner.incrementEntryCount();
      assertEquals(1, lockOwner.getEntryCount());
   }


   public void testDecrementEntryCount() {

      lockOwner.incrementEntryCount();
      lockOwner.incrementEntryCount();
      lockOwner.decrementEntryCount();
      assertEquals(1, lockOwner.getEntryCount());
   }


   public void testGetWireableType() {

      assertEquals(Wireable.TYPE_LOCK_OWNER, lockOwner.getWireableType());
   }


   public void testGetUnlockTimeoutMillis() {

      assertEquals(unlockTime, lockOwner.getUnlockTimeout());
   }


   public void testIsReadLock() {

      assertTrue(lockOwner.isReadLock());
   }


   public void testWriteReadWire() throws IOException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(lockOwner, ser.deserialize(ser.serialize(lockOwner)));
   }


   public void testHashCode() {

      assertTrue(lockOwner.hashCode() != 0);
   }


   public void testToString() {

      assertNotNull(lockOwner.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      final Thread currentThread = Thread.currentThread();
      threadID = System.identityHashCode(currentThread);
      threadName = currentThread.getName();
      address = TestUtils.createTestAddress(1);
      unlockTime = getClock().currentTime().add(10000L);
      lockOwner = new LockOwner(threadID, address, threadName, unlockTime, true);
   }
}
