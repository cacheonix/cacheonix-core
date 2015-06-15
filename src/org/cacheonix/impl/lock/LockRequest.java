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
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterResponse;
import org.cacheonix.impl.net.cluster.ReplicatedStateProcessorKey;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.SimpleWaiter;
import org.cacheonix.impl.net.processor.Waiter;
import org.cacheonix.impl.net.serializer.SerializerUtils;

/**
 * A request to acquire a lock.
 */
@SuppressWarnings("RedundantIfStatement")
abstract class LockRequest extends Request {

   /**
    * A key identifying the lock.
    */
   private Binary lockKey;

   /**
    * An ID of a sender-local thread sending this lock request.
    */
   private int ownerThreadID;

   /**
    * A name of a sender-local thread sending this lock request.
    */
   private String ownerThreadName;

   /**
    * A name of the lock region. The region name is used to separate cluster-wide and cache-specific locks.
    */
   private String lockRegionName;

   /**
    * Lock owner's address.
    */
   private ClusterNodeAddress ownerAddress;


   /**
    * The read lock flag. If false, it is a write lock.
    */
   private boolean readLock = false;


   /**
    * Required by Wireable.
    */
   @SuppressWarnings("UnusedDeclaration")
   LockRequest() {

   }


   /**
    * Create a lock request.
    *
    * @param wireableType    a wireable type.
    * @param lockRegionName  a name of the region where this lock is going to be placed. The region name is used to
    *                        separate cluster-wide and cache-specific locks.
    * @param lockKey         a lock key.
    * @param ownerAddress    an owner's address
    * @param ownerThreadID   a ID of the owner thread.
    * @param ownerThreadName a name of the owner thread.
    * @param readLock        a read lock flag. If false, it is a write lock.
    */
   LockRequest(final int wireableType, final String lockRegionName, final Binary lockKey,
               final ClusterNodeAddress ownerAddress, final int ownerThreadID, final String ownerThreadName,
               final boolean readLock) {

      super(wireableType);
      this.lockRegionName = lockRegionName;
      this.ownerThreadName = ownerThreadName;
      this.ownerThreadID = ownerThreadID;
      this.ownerAddress = ownerAddress;
      this.lockKey = lockKey;
      this.readLock = readLock;
   }


   /**
    * {@inheritDoc}
    */
   protected ProcessorKey getProcessorKey() {

      return ReplicatedStateProcessorKey.getInstance();
   }


   /**
    * Returns the name of the lock region.
    *
    * @return the name of the lock region. The region name is used to separate cluster-wide and cache-specific locks.
    */
   public String getLockRegionName() {

      return lockRegionName;
   }


   /**
    * Returns the key identifying the lock.
    *
    * @return the key identifying the lock.
    */
   public Binary getLockKey() {

      return lockKey;
   }


   /**
    * Returns the lock owner's address.
    *
    * @return the lock owner's address.
    */
   public ClusterNodeAddress getOwnerAddress() {

      return ownerAddress;
   }


   /**
    * Returns an ID of the sender-local owner thread.
    *
    * @return the ID of the sender-local owner thread.
    */
   public int getOwnerThreadID() {

      return ownerThreadID;
   }


   /**
    * Returns a name of the sender-local owner thread.
    *
    * @return the name of the sender-local owner thread.
    */
   public String getOwnerThreadName() {

      return ownerThreadName;
   }


   /**
    * Returns a read lock flag.
    *
    * @return the read lock flag. If false, it is a write lock.
    */
   public boolean isReadLock() {

      return readLock;
   }


   /**
    * Returns a context cluster service.
    *
    * @return the context cluster service.
    */
   final ClusterProcessor getClusterProcessor() {

      return (ClusterProcessor) getProcessor();
   }


   /**
    * Creates a response with receiver set to the sender of this message and a populated responseToUUID.
    *
    * @param resultCode the result code.
    * @return a response with receiver set to the sender of this message and a populated responseToUUID.
    */
   public final Response createResponse(final int resultCode) {

      final ClusterResponse response = new ClusterResponse();
      response.setResponseToClass(getClass());
      response.setResultCode(resultCode);
      response.setReceiver(getSender());
      response.setResponseToUUID(getUuid());
      return response;
   }


   public Waiter createWaiter() {

      return new SimpleWaiter(this);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeInt(ownerThreadID);
      out.writeBoolean(readLock);
      SerializerUtils.writeString(lockRegionName, out);
      SerializerUtils.writeBinary(out, lockKey);
      SerializerUtils.writeString(ownerThreadName, out);
      SerializerUtils.writeAddress(ownerAddress, out);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      ownerThreadID = in.readInt();
      readLock = in.readBoolean();
      lockRegionName = SerializerUtils.readString(in);
      lockKey = SerializerUtils.readBinary(in);
      ownerThreadName = SerializerUtils.readString(in);
      ownerAddress = SerializerUtils.readAddress(in);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final LockRequest that = (LockRequest) o;

      if (ownerThreadID != that.ownerThreadID) {
         return false;
      }
      if (readLock != that.readLock) {
         return false;
      }
      if (lockKey != null ? !lockKey.equals(that.lockKey) : that.lockKey != null) {
         return false;
      }
      if (lockRegionName != null ? !lockRegionName.equals(that.lockRegionName) : that.lockRegionName != null) {
         return false;
      }
      if (ownerAddress != null ? !ownerAddress.equals(that.ownerAddress) : that.ownerAddress != null) {
         return false;
      }
      if (ownerThreadName != null ? !ownerThreadName.equals(that.ownerThreadName) : that.ownerThreadName != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (lockKey != null ? lockKey.hashCode() : 0);
      result = 31 * result + ownerThreadID;
      result = 31 * result + (lockRegionName != null ? lockRegionName.hashCode() : 0);
      result = 31 * result + (ownerThreadName != null ? ownerThreadName.hashCode() : 0);
      result = 31 * result + (ownerAddress != null ? ownerAddress.hashCode() : 0);
      result = 31 * result + (readLock ? 1 : 0);
      return result;
   }


   public String toString() {

      return "LockRequest{" +
              "lockKey='" + lockKey + '\'' +
              ", ownerThreadID=" + ownerThreadID +
              ", ownerThreadName='" + ownerThreadName + '\'' +
              "} " + super.toString();
   }
}
