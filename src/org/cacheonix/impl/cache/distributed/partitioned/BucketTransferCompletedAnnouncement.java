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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cacheonix.impl.cluster.node.state.bucket.BucketOwnershipAssignment;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.cluster.node.state.group.GroupMember;
import org.cacheonix.impl.cluster.node.state.group.GroupMessage;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BucketTransferCompletedAnnouncement
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection RedundantIfStatement, ClassNameSameAsAncestorName
 * @since Aug 12, 2009 10:43:08 PM
 */
public final class BucketTransferCompletedAnnouncement extends GroupMessage {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketTransferCompletedAnnouncement.class); // NOPMD

   private byte sourceStorageNumber = -1;

   private byte destinationStorageNumber = -1;

   private List<Integer> transferredBucketNumbers = null;

   private ClusterNodeAddress newOwnerAddress = null;

   private ClusterNodeAddress previousOwnerAddress = null;


   /**
    * Required by Wireable.
    */
   @SuppressWarnings("WeakerAccess")
   public BucketTransferCompletedAnnouncement() { // NOPMD

   }


   public BucketTransferCompletedAnnouncement(final String cacheName) {

      super(TYPE_GROUP_BUCKET_TRANSFER_COMPLETED, Group.GROUP_TYPE_CACHE, cacheName);
   }


   public void addTransferredBucketNumbers(final List<Integer> bucketNumbers) {

      if (CollectionUtils.isEmpty(bucketNumbers)) {

         return;
      }

      if (transferredBucketNumbers == null) {

         transferredBucketNumbers = new ArrayList<Integer>(bucketNumbers.size());
      }
      transferredBucketNumbers.addAll(bucketNumbers);
   }


   public void setPreviousOwnerAddress(final ClusterNodeAddress previousOwner) {

      this.previousOwnerAddress = previousOwner;
   }


   public ClusterNodeAddress getNewOwnerAddress() {

      return newOwnerAddress;
   }


   public void setNewOwnerAddress(final ClusterNodeAddress newOwner) {

      this.newOwnerAddress = newOwner;
   }


   public byte getSourceStorageNumber() {

      return sourceStorageNumber;
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


   public void execute() {

      final String cacheName = getGroupName();
      final Group group = getReplicatedState().getGroup(getGroupType(), cacheName);

      // Update bucket ownership
      final BucketOwnershipAssignment bucketOwnershipAssignment = group.getBucketOwnershipAssignment();

      // Finish transfers
      if (!CollectionUtils.isEmpty(transferredBucketNumbers)) {

         //noinspection ControlFlowStatementWithoutBraces
//         if (LOG.isDebugEnabled()) {
//            LOG.debug("Finishing bucket transfer from '" + previousOwnerAddress + "' to '" + newOwnerAddress +
//                    "', sourceStorageNumber: " + sourceStorageNumber + ", destinationStorageNumber: " + destinationStorageNumber +
//                    ", bucket numbers: " + transferredBucketNumbers); // NOPMD
//         }

         bucketOwnershipAssignment.finishBucketTransfer(sourceStorageNumber, destinationStorageNumber, previousOwnerAddress, newOwnerAddress, transferredBucketNumbers);
      }

      // Request *forced* leave for the previous owner if the previous owner
      // is leaving and now does not have any bucket responsibilities

      final GroupMember previousOwnerMember = group.getGroupMember(previousOwnerAddress);
      if (previousOwnerMember.isPartitionContributor() && previousOwnerMember.isLeaving()
              && !bucketOwnershipAssignment.hasBucketResponsibilities(previousOwnerAddress)) {

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Force leave: " + cacheName + ':' + previousOwnerAddress); // NOPMD

         // Mark as inactive. This in turn will cause shutdown of the local processor if any
         group.removeMembers(Collections.singleton(previousOwnerAddress));
      }
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      sourceStorageNumber = in.readByte();
      destinationStorageNumber = in.readByte();
      previousOwnerAddress = SerializerUtils.readAddress(in);
      newOwnerAddress = SerializerUtils.readAddress(in);
      transferredBucketNumbers = SerializerUtils.readShortList(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeByte(sourceStorageNumber);
      out.writeByte(destinationStorageNumber);
      SerializerUtils.writeAddress(previousOwnerAddress, out);
      SerializerUtils.writeAddress(newOwnerAddress, out);
      SerializerUtils.writeShortList(out, transferredBucketNumbers);
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

      final BucketTransferCompletedAnnouncement that = (BucketTransferCompletedAnnouncement) o;

      if (sourceStorageNumber != that.sourceStorageNumber) {
         return false;
      }
      if (destinationStorageNumber != that.destinationStorageNumber) {
         return false;
      }
      if (newOwnerAddress != null ? !newOwnerAddress.equals(that.newOwnerAddress) : that.newOwnerAddress != null) {
         return false;
      }
      if (previousOwnerAddress != null ? !previousOwnerAddress.equals(that.previousOwnerAddress) : that.previousOwnerAddress != null) {
         return false;
      }
      if (transferredBucketNumbers != null ? !transferredBucketNumbers.equals(that.transferredBucketNumbers) : that.transferredBucketNumbers != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (int) sourceStorageNumber;
      result = 31 * result + (int) destinationStorageNumber;
      result = 31 * result + (transferredBucketNumbers != null ? transferredBucketNumbers.hashCode() : 0);
      result = 31 * result + (newOwnerAddress != null ? newOwnerAddress.hashCode() : 0);
      result = 31 * result + (previousOwnerAddress != null ? previousOwnerAddress.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "BucketTransferCompletedAnnouncement{" +
              "sourceStorageNumber=" + sourceStorageNumber +
              ", destinationStorageNumber=" + destinationStorageNumber +
              ", previousOwner=" + previousOwnerAddress +
              ", newOwner=" + newOwnerAddress +
              ", transferredBucketNumbers=" + StringUtils.sizeToString(transferredBucketNumbers) +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new BucketTransferCompletedAnnouncement();
      }
   }
}
