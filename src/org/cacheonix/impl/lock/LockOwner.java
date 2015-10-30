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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.MulticastMarker;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * An owner of a distributed lock.
 */
@SuppressWarnings("RedundantIfStatement")
public final class LockOwner implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private int threadID = 0;

   private ClusterNodeAddress address = null;

   private String threadName = null;

   private int entryCount = 0;

   private boolean readLock = false;

   /**
    * The cluster time at that a cluster representative will begin a forced lock release. If null, means that it is not
    * set yet.
    * <p/>
    * NOTE: simeshev@cacheonix.org - 2010-08-12 - This is a transient field and makes sense only on the cluster
    * representative.. If the cluster representative changes, the forced unlock process will start from scratches.
    *
    * @see MulticastMarker#processUnlockTimeouts()
    */
   private Time unlockTimeout = null;


   /**
    * Creates a LockOwner.
    *
    * @param threadID      lock owner's thread ID.
    * @param address       lock owner's address.
    * @param threadName    lock owner's thread name.
    * @param unlockTimeout cluster time when the lock must be released.
    * @param readLock      <code>true</code> if this is an owner for a read lock
    */
   public LockOwner(final Integer threadID, final ClusterNodeAddress address, final String threadName,
                    final Time unlockTimeout, final boolean readLock) {

      this.threadID = threadID;
      this.address = address;
      this.threadName = threadName;
      this.unlockTimeout = unlockTimeout;
      this.readLock = readLock;
   }


   /**
    * Required by Wireable.
    */
   public LockOwner() {

   }


   /**
    * Returns lock owner's thread ID.
    *
    * @return lock owner's thread ID.
    */
   public int getThreadID() {

      return threadID;
   }


   /**
    * Returns lock owner's address.
    *
    * @return lock owner's address.
    */
   public ClusterNodeAddress getAddress() {

      return address;
   }


   /**
    * Returns lock owner's thread name.
    *
    * @return lock owner's thread name.
    */
   public String getThreadName() {

      return threadName;
   }


   /**
    * Returns a number of times the lock owner's entered the lock.
    *
    * @return the number of times the lock owner's entered the lock.
    */
   public int getEntryCount() {

      return entryCount;
   }


   public boolean isReadLock() {

      return readLock;
   }


   /**
    * Returns cluster time at that a cluster representative will begin a forced lock release. If null, means that it is
    * not set yet.
    *
    * @return the cluster time at that a cluster representative will begin a forced lock release. If null, means that it
    *         is not set yet.
    * @see MulticastMarker#processUnlockTimeouts()
    */
   public Time getUnlockTimeout() {

      return unlockTimeout;
   }


   public void incrementEntryCount() {

      entryCount++;
   }


   public void decrementEntryCount() {

      entryCount--;
   }


   public boolean cameFromRequester(final LockRequest request) {

      return threadID == request.getOwnerThreadID() && address.equals(request.getOwnerAddress());
   }


   public int getWireableType() {

      return TYPE_LOCK_OWNER;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      SerializerUtils.writeTime(unlockTimeout, out);
      SerializerUtils.writeAddress(address, out);
      SerializerUtils.writeString(threadName, out);
      out.writeInt(threadID);
      out.writeInt(entryCount);
      out.writeBoolean(readLock);
   }


   public void readWire(final DataInputStream in) throws IOException {

      unlockTimeout = SerializerUtils.readTime(in);
      address = SerializerUtils.readAddress(in);
      threadName = SerializerUtils.readString(in);
      threadID = in.readInt();
      entryCount = in.readInt();
      readLock = in.readBoolean();
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final LockOwner lockOwner = (LockOwner) o;

      if (entryCount != lockOwner.entryCount) {
         return false;
      }
      if (readLock != lockOwner.readLock) {
         return false;
      }
      if (threadID != lockOwner.threadID) {
         return false;
      }
      if (address != null ? !address.equals(lockOwner.address) : lockOwner.address != null) {
         return false;
      }
      if (threadName != null ? !threadName.equals(lockOwner.threadName) : lockOwner.threadName != null) {
         return false;
      }
      if (unlockTimeout != null ? !unlockTimeout.equals(lockOwner.unlockTimeout) : lockOwner.unlockTimeout != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = threadID;
      result = 31 * result + (address != null ? address.hashCode() : 0);
      result = 31 * result + (threadName != null ? threadName.hashCode() : 0);
      result = 31 * result + entryCount;
      result = 31 * result + (readLock ? 1 : 0);
      result = 31 * result + (unlockTimeout != null ? unlockTimeout.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "LockOwner{" +
              "threadID=" + threadID +
              ", address=" + address +
              ", threadName='" + threadName + '\'' +
              ", entryCount=" + entryCount +
              ", readLock=" + readLock +
              ", unlockTimeMillis=" + unlockTimeout +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new LockOwner();
      }
   }
}
