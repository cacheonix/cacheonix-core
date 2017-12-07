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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.cluster.node.state.group.GroupMember;
import org.cacheonix.impl.cluster.node.state.group.GroupMessage;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A reliable totally ordered multicast message that removes a cache group member.
 * <p/>
 * This message is sent when a CacheProcessor leaves the group as a result of a distributed cache being removed or when
 * an owning cluster node shuts down. LeaveCacheGroupAnnouncement executes in one of two modes: a graceful leave or an
 * immediate shutdown.
 * <p/>
 * The graceful leave just marks a bucket owner as leaving and posts a re-partition announcements that will take care of
 * moving buckets from the leaving node to operational nodes.
 * <p/>
 * The immediate shutdown marks the group owner as left and immediately shut downs the cache processor.
 * <p/>
 * This message should be prepared for the being sent and executed multiple times.
 */
@SuppressWarnings("RedundantIfStatement")
public final class LeaveCacheGroupAnnouncement extends GroupMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LeaveCacheGroupAnnouncement.class); // NOPMD

   /**
    * Address of the cache processor leaving the cache group.
    */
   private ClusterNodeAddress leavingAddress = null;

   /**
    * Flag indicating if this is a request for a graceful leave. If <code>false</code>, this is a forced leave.
    */
   private boolean gracefulLeave = true;


   /**
    * Required by Wireable.
    */
   public LeaveCacheGroupAnnouncement() {

   }


   /**
    * Creates LeaveCacheGroupAnnouncement.
    *
    * @param cacheName      a cache name.
    * @param leavingAddress an address of the leaving the group.
    * @param gracefulLeave  a flag indicating if this is a request for a graceful leave. If <code>false</code>, this is
    *                       a forced leave.
    */
   public LeaveCacheGroupAnnouncement(final String cacheName, final ClusterNodeAddress leavingAddress,
                                      final boolean gracefulLeave) {

      super(TYPE_GROUP_LEAVE_ANNOUNCEMENT, Group.GROUP_TYPE_CACHE, cacheName);
      this.leavingAddress = leavingAddress;
      this.gracefulLeave = gracefulLeave;
   }


   public ClusterNodeAddress getLeavingAddress() {

      return leavingAddress;
   }


   public boolean isGracefulLeave() {

      return gracefulLeave;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * The method is executed synchronously by all cluster nodes.
    */
   public void execute() {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Executing: " + this); // NOPMD

      // Get group

      final ReplicatedState replicatedState = ((ClusterProcessor) getProcessor()).getProcessorState().getReplicatedState();
      final Group group = replicatedState.getGroup(getGroupType(), getGroupName());
      final GroupMember groupMember = group.getGroupMember(leavingAddress);

      // Check if already left
      if (groupMember == null || !groupMember.isActive()) {
         return;
      }

      // Dispatch or graceful or forced.
      if (gracefulLeave && groupMember.isPartitionContributor()) {

         if (groupMember.isLeaving()) {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled())
               LOG.debug("Ignoring request for graceful leave, the member is already leaving: " + getGroupName() + ':' + leavingAddress); // NOPMD
         } else {

            //noinspection ControlFlowStatementWithoutBraces
            if (LOG.isDebugEnabled())
               LOG.debug("Beginning graceful leave: " + getGroupName() + ':' + leavingAddress); // NOPMD

            // Mark as leaving
            groupMember.setLeaving(true);

            // Begin graceful leave
            executeGracefulLeave(group);
         }
      } else {

         executeForcedLeave(group);
      }
   }


   /**
    * Executes graceful leave request by marking bucket owner as leaving.
    *
    * @param group the cache group.
    */
   private void executeGracefulLeave(final Group group) {


      final BucketOwnershipAssignment bucketOwnershipAssignment = group.getBucketOwnershipAssignment();

      // Check if owns any buckets
      if (bucketOwnershipAssignment.hasBucketResponsibilities(leavingAddress)) {

         // Begin graceful leave

         // Mark bucket owner as leaving
         bucketOwnershipAssignment.markBucketOwnerLeaving(leavingAddress);


         // Exit forcibly if all owners are leaving
         if (bucketOwnershipAssignment.isAllBucketOwnersLeaving()) {

            // Everyone is leaving, no one can accept our buckets, no point in waiting
            executeForcedLeave(group);
         } else {

            // Begin repartitioning
            bucketOwnershipAssignment.repartition();

            // Repartition may lead to orphaning buckets so there might be no need to wait
            // for transfer completion that would trigger forced leave.
            if (!bucketOwnershipAssignment.hasBucketResponsibilities(leavingAddress)) {

               executeForcedLeave(group);
            }
         }
      } else {

         // Force shutdown
         executeForcedLeave(group);
      }
   }


   /**
    * Executes forced leave request.
    *
    * @param group group from that to remove a cache processor.
    */
   private void executeForcedLeave(final Group group) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("Force leave: " + getGroupName() + ':' + leavingAddress); // NOPMD

      // Mark as inactive. This in turn will cause shutdown of the local processor if any.
      group.removeMembers(Collections.singleton(leavingAddress));
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      gracefulLeave = in.readBoolean();
      leavingAddress = SerializerUtils.readAddress(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      out.writeBoolean(gracefulLeave);
      SerializerUtils.writeAddress(leavingAddress, out);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final LeaveCacheGroupAnnouncement that = (LeaveCacheGroupAnnouncement) o;

      if (gracefulLeave != that.gracefulLeave) {
         return false;
      }
      if (leavingAddress != null ? !leavingAddress.equals(that.leavingAddress) : that.leavingAddress != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (leavingAddress != null ? leavingAddress.hashCode() : 0);
      result = 31 * result + (gracefulLeave ? 1 : 0);
      return result;
   }


   public String toString() {

      return "LeaveCacheGroupAnnouncement{" +
              "leavingAddress=" + leavingAddress +
              ", gracefulLeave=" + gracefulLeave +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new LeaveCacheGroupAnnouncement();
      }
   }
}
