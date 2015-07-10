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
package org.cacheonix.impl.cluster.node.state.group;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Request to add a cache member.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection UnnecessaryParentheses, UnnecessaryParentheses, NonFinalFieldReferenceInEquals,
 * NonFinalFieldReferencedInHashCode, RedundantIfStatement, ParameterNameDiffersFromOverriddenParameter
 * @since Jan 19, 2009 2:54:01 AM
 */
public final class JoinGroupMessage extends GroupMessage {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(JoinGroupMessage.class); // NOPMD

   private long partitionSize = -1L;

   private long heapSizeBytes = -1L;

   private boolean partitionContributor = false;

   private String cacheConfigName = null;

   private int replicaCount = -1;

   /**
    * A max number of elements in memory.
    */
   private long maxElements = Integer.MAX_VALUE;


   /**
    * Required by Wireable.
    */
   public JoinGroupMessage() {

   }


   /**
    * Constructor.
    *
    * @param sender               sender
    * @param cacheName            cache name
    * @param partitionContributor <code>true</code> if the given node it partition contributor.
    * @param partitionSize        partition size of this node.
    * @param heapSize             heap size of this node.
    * @param maxElements          a max number of elements in memory.
    */
   public JoinGroupMessage(final ClusterNodeAddress sender, final String cacheName,
                           final boolean partitionContributor, final long partitionSize,
                           final long heapSize, final long maxElements) {

      super(TYPE_GROUP_JOIN_GROUP, Group.GROUP_TYPE_CACHE, cacheName, sender);

      // Check parameters
      if (heapSize < partitionSize) {
         throw new IllegalArgumentException("Node heap size " + heapSize
                 + " is too small to accommodate partition size " + partitionSize);
      }

      // Set fields
      this.partitionSize = partitionSize;
      this.heapSizeBytes = heapSize;
      this.partitionContributor = partitionContributor;
      this.maxElements = maxElements;
   }


   /**
    * Sets a cache config name that a requester will later use to create actual cache configuration.
    *
    * @param cacheConfigName the cache config name that a requester will later use to create actual cache
    *                        configuration.
    */
   public void setCacheConfigName(final String cacheConfigName) {

      this.cacheConfigName = cacheConfigName;
   }


   /**
    * Sets desired replica count.
    *
    * @param replicaCount desired replica count to set.
    */
   public void setReplicaCount(final int replicaCount) {

      this.replicaCount = replicaCount;
   }


   public long getHeapSizeBytes() {

      return heapSizeBytes;
   }


   public long getPartitionSize() {

      return partitionSize;
   }//


   public boolean isPartitionContributor() {

      return partitionContributor;
   }


   public String getCacheConfigName() {

      return cacheConfigName;
   }


   public int getReplicaCount() {

      return replicaCount;
   }


   public void execute() {

      final ReplicatedState replicatedState = getReplicatedState();

      // Look up
      final String groupName = getGroupName();
      final int groupType = getGroupType();
      Group group = replicatedState.getGroup(groupType, groupName);

      if (group == null) {
         // New group, we are the first

         // Create group
         group = new Group(groupName, groupType);
         if (partitionContributor) {

            group.configurePartition(replicaCount, partitionSize, maxElements);
         }

         // Register group
         replicatedState.registerGroup(groupType, groupName, group);

         // Re-attach subscribers created while group wasn't there
         replicatedState.reattachSubscribers(group);
      } else {

         if (partitionContributor) {

            // Set partition size if necessary
            if (!group.isPartitionConfigured()) {

               group.configurePartition(replicaCount, partitionSize, maxElements);
            }

            // Check if our heap is big enough.
            if (heapSizeBytes < group.getPartitionSizeBytes()) {

               // Notify subscribers that member failed to join
               final String error = "Node heap size " + heapSizeBytes + " is too small to accommodate cluster-wide partition size " + group.getPartitionSizeBytes();
               final GroupMemberFailedToJoinEvent event = new GroupMemberFailedToJoinEvent(error, this);
               final GroupEventSubscriberList subscribers = replicatedState.getGroupEventSubscriberList(groupType);
               subscribers.notifyMemberFailedToJoin(event);
               return;
            }
         }
      }

      // Add group member
      final GroupMember member = new GroupMember(getSender(), partitionContributor, heapSizeBytes);
      member.setCacheConfigName(cacheConfigName);
      group.addMember(member);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      partitionContributor = in.readBoolean();
      partitionSize = in.readLong();
      heapSizeBytes = in.readLong();
      maxElements = in.readLong();
      replicaCount = in.readInt();
      cacheConfigName = SerializerUtils.readString(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeBoolean(partitionContributor);
      out.writeLong(partitionSize);
      out.writeLong(heapSizeBytes);
      out.writeLong(maxElements);
      out.writeInt(replicaCount);
      SerializerUtils.writeString(cacheConfigName, out);
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

      final JoinGroupMessage that = (JoinGroupMessage) o;

      if (heapSizeBytes != that.heapSizeBytes) {
         return false;
      }
      if (maxElements != that.maxElements) {
         return false;
      }
      if (partitionContributor != that.partitionContributor) {
         return false;
      }
      if (partitionSize != that.partitionSize) {
         return false;
      }
      if (replicaCount != that.replicaCount) {
         return false;
      }
      if (cacheConfigName != null ? !cacheConfigName.equals(that.cacheConfigName) : that.cacheConfigName != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (int) (partitionSize ^ (partitionSize >>> 32));
      result = 31 * result + (int) (heapSizeBytes ^ (heapSizeBytes >>> 32));
      result = 31 * result + (partitionContributor ? 1 : 0);
      result = 31 * result + (cacheConfigName != null ? cacheConfigName.hashCode() : 0);
      result = 31 * result + replicaCount;
      result = 31 * result + (int) maxElements;
      return result;
   }


   public String toString() {

      return "JoinGroupMessage{" +
              "groupName=" + getGroupName() +
              ", partitionSize=" + partitionSize +
              ", heapSizeBytes=" + heapSizeBytes +
              ", partitionContributor=" + partitionContributor +
              ", cacheConfigName='" + cacheConfigName + '\'' +
              ", replicaCount=" + replicaCount +
              ", maxSize=" + maxElements +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new JoinGroupMessage();
      }
   }
}
