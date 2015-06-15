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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * A key that identifies a lock queue in a lock queue registry.
 */
public final class LockQueueKey implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * A name of the region where this lock is going to be placed. The region name is used to separate cluster-wide and
    * cache-specific locks.
    */
   private String lockRegionName;

   /**
    * A lock identifier.
    */
   private Binary lockKey;


   /**
    * Reuired by Wireable.
    */
   public LockQueueKey() {

   }


   /**
    * Creates a LockQueueKey.
    *
    * @param lockRegionName a name of the region where this lock is going to be placed. The region name is used to
    *                       separate cluster-wide and cache-specific locks.
    * @param lockKey        a lock identifier.
    */
   public LockQueueKey(final String lockRegionName, final Binary lockKey) {

      this.lockRegionName = lockRegionName;
      this.lockKey = lockKey;
   }


   /**
    * Returns a name of the region where this lock is going to be placed. The region name is used to separate
    * cluster-wide and cache-specific locks.
    *
    * @return the a name of the region where this lock is going to be placed. The region name is used to separate
    *         cluster-wide and cache-specific locks.
    */
   public String getLockRegionName() {

      return lockRegionName;
   }


   /**
    * Returns a  lock identifier.
    *
    * @return the lock identifier.
    */
   public Binary getLockKey() {

      return lockKey;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      SerializerUtils.writeString(lockRegionName, out);
      SerializerUtils.writeBinary(out, lockKey);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      lockRegionName = SerializerUtils.readString(in);
      lockKey = SerializerUtils.readBinary(in);

   }


   public int getWireableType() {

      return TYPE_LOCK_QUEUE_KEY;
   }


   @SuppressWarnings("RedundantIfStatement")
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final LockQueueKey that = (LockQueueKey) o;

      if (lockKey != null ? !lockKey.equals(that.lockKey) : that.lockKey != null) {
         return false;
      }
      if (lockRegionName != null ? !lockRegionName.equals(that.lockRegionName) : that.lockRegionName != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = lockRegionName != null ? lockRegionName.hashCode() : 0;
      result = 31 * result + (lockKey != null ? lockKey.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "LockQueueKey{" +
              "lockRegionName='" + lockRegionName + '\'' +
              ", lockKey=" + lockKey +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new LockQueueKey();
      }
   }
}
