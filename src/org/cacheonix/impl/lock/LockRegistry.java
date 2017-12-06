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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.ObjectObjectProcedure;

/**
 * A registry for named locks.
 */
@SuppressWarnings("RedundantIfStatement")
public final class LockRegistry implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * A set of queues mapped to their keys.
    */
   private final HashMap<LockQueueKey, LockQueue> lockQueues = new HashMap<LockQueueKey, LockQueue>(11); // NOPMD


   /**
    * Default constructor required by Wireable.
    */
   @SuppressWarnings("RedundantNoArgConstructor")
   public LockRegistry() { // NOPMD
   }


   /**
    * Returns a lock queue for a lock identified by the combination of <code>lockRegionName</code> and
    * <code>lockKey</code>. If the lock queue does not exist, creates and registers it.
    *
    * @param lockRegionName a name of the region where this lock is going to be placed. The region name is used to
    *                       separate cluster-wide and cache-specific locks.
    * @param lockKey@return a lock queue.
    * @return the lock queue.
    */
   public LockQueue getLockQueue(final String lockRegionName, final Binary lockKey) {

      final LockQueueKey lockQueueKey = new LockQueueKey(lockRegionName, lockKey);
      LockQueue lockQueue = lockQueues.get(lockQueueKey);
      if (lockQueue == null) {

         lockQueue = new LockQueue();
         lockQueues.put(lockQueueKey, lockQueue);
      }
      return lockQueue;
   }


   public HashMap<LockQueueKey, LockQueue> getLockQueues() { // NOPMD

      return lockQueues;
   }


   /**
    * Returns {Message#TYPE_LOCK_REGISTRY}.
    *
    * @return {Message#TYPE_LOCK_REGISTRY}.
    */
   public int getWireableType() {

      return TYPE_LOCK_REGISTRY;
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      // Write map size
      out.writeInt(lockQueues.size());

      // Write map entries
      final IOException[] exception = new IOException[1];
      lockQueues.forEachEntry(new ObjectObjectProcedure<LockQueueKey, LockQueue>() {

         public boolean execute(final LockQueueKey key, final LockQueue value) {

            try {

               key.writeWire(out);
               value.writeWire(out);
            } catch (final IOException e) {

               exception[0] = e;
               return false;
            }

            return true;
         }
      });

      // Throw exception if any
      if (exception[0] != null) {

         throw exception[0];
      }
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      final int size = in.readInt();
      for (int i = 0; i < size; i++) {

         final LockQueueKey lockQueueKey = new LockQueueKey();
         lockQueueKey.readWire(in);
         final LockQueue lockQueue = new LockQueue();
         lockQueue.readWire(in);
         lockQueues.put(lockQueueKey, lockQueue);
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final LockRegistry that = (LockRegistry) o;

      if (lockQueues != null ? !lockQueues.equals(that.lockQueues) : that.lockQueues != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return lockQueues != null ? lockQueues.hashCode() : 0;
   }


   public String toString() {

      return "LockRegistry{" +
              "lockQueues=" + lockQueues +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new LockRegistry();
      }
   }
}
