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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.cluster.node.state.group.GroupMessage;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BucketTransferCompletedAnnouncement is sent when a current owner cannot transfer a bucket for some reason (usually it
 * doesn't have a bucket yet or the bucket is still being transferred.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection RedundantIfStatement, ParameterNameDiffersFromOverriddenParameter, ClassNameSameAsAncestorName
 * @see BeginBucketTransferMessage#executeOperational()
 * @since Oct 27, 2009 12:04:56 PM
 */
public final class BucketTransferRejectedAnnouncement extends GroupMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketTransferRejectedAnnouncement.class); // NOPMD

   private byte sourceStorageNumber = 0;

   private byte destinationStorageNumber = 0;

   private ClusterNodeAddress previousOwner = null;

   private ClusterNodeAddress newOwner = null;

   private List<Integer> bucketNumbers = Collections.emptyList();


   private BucketTransferRejectedAnnouncement() {

   }


   BucketTransferRejectedAnnouncement(final String cacheName) {

      super(TYPE_GROUP_BUCKET_TRANSFER_REJECTED, Group.GROUP_TYPE_CACHE, cacheName);
   }


   public byte getSourceStorageNumber() {

      return sourceStorageNumber;
   }


   /**
    * Sets bucket numbers.
    *
    * @param bucketNumbers an unmodifiable collection.
    * @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   public void setBucketNumbers(final List<Integer> bucketNumbers) {

      this.bucketNumbers = bucketNumbers;
   }


   public ClusterNodeAddress getPreviousOwner() {

      return previousOwner;
   }


   public ClusterNodeAddress getNewOwner() {

      return newOwner;
   }


   public void setSourceStorageNumber(final byte sourceStorageNumber) {

      this.sourceStorageNumber = sourceStorageNumber;
   }


   public byte getDestinationStorageNumber() {

      return destinationStorageNumber;
   }


   public void setDestinationStorageNumber(final byte destinationStorageNumber) {

      this.destinationStorageNumber = destinationStorageNumber;
   }


   public void setPreviousOwner(final ClusterNodeAddress previousOwner) {

      this.previousOwner = previousOwner;
   }


   public void setNewOwner(final ClusterNodeAddress newOwner) {

      this.newOwner = newOwner;
   }


   public void execute() {

      if (CollectionUtils.isEmpty(bucketNumbers)) {

         // Got nothing to do
         return;
      }

      // Get bucket ownership assignment
      final ReplicatedState replicatedState = getReplicatedState();
      final Group group = replicatedState.getGroup(getGroupType(), getGroupName());
      final BucketOwnershipAssignment bucketOwnershipAssignment = group.getBucketOwnershipAssignment();

      // Reject transfer
      bucketOwnershipAssignment.rejectBucketTransfer(sourceStorageNumber, destinationStorageNumber, previousOwner, newOwner, bucketNumbers);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      sourceStorageNumber = in.readByte();
      destinationStorageNumber = in.readByte();
      bucketNumbers = SerializerUtils.readShortList(in);
      previousOwner = SerializerUtils.readAddress(in);
      newOwner = SerializerUtils.readAddress(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeByte(sourceStorageNumber);
      out.writeByte(destinationStorageNumber);
      SerializerUtils.writeShortList(out, bucketNumbers);
      SerializerUtils.writeAddress(previousOwner, out);
      SerializerUtils.writeAddress(newOwner, out);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof BucketTransferRejectedAnnouncement)) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final BucketTransferRejectedAnnouncement that = (BucketTransferRejectedAnnouncement) o;

      if (sourceStorageNumber != that.sourceStorageNumber) {
         return false;
      }
      if (destinationStorageNumber != that.destinationStorageNumber) {
         return false;
      }
      if (bucketNumbers != null ? !bucketNumbers.equals(that.bucketNumbers) : that.bucketNumbers != null) {
         return false;
      }
      if (newOwner != null ? !newOwner.equals(that.newOwner) : that.newOwner != null) {
         return false;
      }
      if (previousOwner != null ? !previousOwner.equals(that.previousOwner) : that.previousOwner != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + sourceStorageNumber;
      result = 31 * result + destinationStorageNumber;
      result = 31 * result + (previousOwner != null ? previousOwner.hashCode() : 0);
      result = 31 * result + (newOwner != null ? newOwner.hashCode() : 0);
      result = 31 * result + (bucketNumbers != null ? bucketNumbers.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "BucketTransferRejectedAnnouncement{" +
              "sourceStorageNumber=" + sourceStorageNumber +
              ", destinationStorageNumber=" + destinationStorageNumber +
              ", bucketNumbers=" + bucketNumbers +
              ", newOwner=" + newOwner.getTcpPort() +
              ", previousOwner=" + previousOwner.getTcpPort() +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new BucketTransferRejectedAnnouncement();
      }
   }
}