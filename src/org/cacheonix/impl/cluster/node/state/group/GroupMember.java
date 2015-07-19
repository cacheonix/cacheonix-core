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
package org.cacheonix.impl.cluster.node.state.group;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Member of CacheGroup. Represents a single cache member.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferencedInHashCode, NonFinalFieldReferenceInEquals, RedundantIfStatement @since Jan 19,
 * 2009 3:11:25 AM
 */

public final class GroupMember implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GroupMember.class); // NOPMD

   // Set by deserialization of the Group
   private transient Group group = null;

   private ClusterNodeAddress address = null;

   private boolean active = false;


   private boolean partitionContributor = false;

   private long heapSizeBytes = 0L;

   private String cacheConfigName = null;

   /**
    * Flag indicating that the group member is leaving the group.
    */
   private boolean leaving = false;


   /**
    * Constructor.
    *
    * @param address              home address of the cache member.
    * @param partitionContributor <code>true</code> if partition contributor.
    * @param heapSizeBytes        cache member's heap size.
    */
   public GroupMember(final ClusterNodeAddress address, final boolean partitionContributor,
                      final long heapSizeBytes) {

      Assert.assertTrue(!partitionContributor || heapSizeBytes > 0L, "Heap size should be a positive integer");
      this.address = address;
      this.partitionContributor = partitionContributor;
      this.heapSizeBytes = heapSizeBytes;
   }


   /**
    * Default ccontructor required by Externalizable.
    */
   public GroupMember() {

   }


   /**
    * Wires group member to owner group.
    *
    * @param group owner group
    */
   public void setGroup(final Group group) {

      this.group = group;
   }


   public Group getGroup() {

      return group;
   }


   /**
    * Returns address of this cache member. The address can be set only once in the constructor.
    *
    * @return address of this cache member.
    */
   public ClusterNodeAddress getAddress() {

      return address;
   }


   /**
    * Returns <code>true</code> if this group member is an active member of the group. Returns <code>false</code> if
    * this member left the group.
    *
    * @return <code>true</code> if this group member is an active member of the group.
    */
   public boolean isActive() {

      return active;
   }


   public void setActive(final boolean active) {

      this.active = active;
   }


   /**
    * Returns <code>true</code> if this member of the group is leaving the group.
    *
    * @return <code>true</code> if this member of the group is leaving the group. Returns <code>false</code> if the
    *         member is not leaving or if it has already left.
    */
   public boolean isLeaving() {

      return leaving;
   }


   public void setLeaving(final boolean leaving) {

      this.leaving = leaving;
   }


   /**
    * Returns <code>true</code> if this node is a partition contributor.
    *
    * @return <code>true</code> if this node is a partition contributor.
    */
   public boolean isPartitionContributor() {

      return partitionContributor;
   }


   /**
    * Sets local cache configuration name used to create the given cache group member.
    *
    * @param cacheConfigName cache configuration name for the given cache group member.
    */
   public void setCacheConfigName(final String cacheConfigName) {

      this.cacheConfigName = cacheConfigName;
   }


   /**
    * Returns local cache configuration name  used to create the given cache group member.
    */
   public String getCacheConfigName() {

      return cacheConfigName;
   }


   /**
    * Returns heap size of this node. The heap size can be set only once in the constructor.
    *
    * @return heap size of this node.
    */
   public long getHeapSizeBytes() {

      return heapSizeBytes;
   }


   public void readWire(final DataInputStream in) throws IOException {

      active = in.readBoolean();
      leaving = in.readBoolean();
      address = SerializerUtils.readAddress(in);
      partitionContributor = in.readBoolean();
      heapSizeBytes = in.readLong();
      cacheConfigName = SerializerUtils.readString(in);
   }


   public int getWireableType() {

      return TYPE_GROUP_MEMBER;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.writeBoolean(active);
      out.writeBoolean(leaving);
      SerializerUtils.writeAddress(address, out);
      out.writeBoolean(partitionContributor);
      out.writeLong(heapSizeBytes);
      SerializerUtils.writeString(cacheConfigName, out);
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final GroupMember that = (GroupMember) obj;

      if (active != that.active) {
         return false;
      }
      if (heapSizeBytes != that.heapSizeBytes) {
         return false;
      }
      if (leaving != that.leaving) {
         return false;
      }
      if (partitionContributor != that.partitionContributor) {
         return false;
      }
      if (address != null ? !address.equals(that.address) : that.address != null) {
         return false;
      }
      if (cacheConfigName != null ? !cacheConfigName.equals(that.cacheConfigName) : that.cacheConfigName != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = address != null ? address.hashCode() : 0;
      result = 31 * result + (active ? 1 : 0);
      result = 31 * result + (partitionContributor ? 1 : 0);
      result = 31 * result + (int) (heapSizeBytes ^ (heapSizeBytes >>> 32));
      result = 31 * result + (cacheConfigName != null ? cacheConfigName.hashCode() : 0);
      result = 31 * result + (leaving ? 1 : 0);
      return result;
   }


   public String toString() {

      return "GroupMember{" +
              "group=" + (group == null ? "null" : group.getName()) +
              ", address=" + address +
              ", active=" + active +
              ", partitionContributor=" + partitionContributor +
              ", heapSizeBytes=" + heapSizeBytes +
              ", cacheConfigName='" + cacheConfigName + '\'' +
              ", leaving=" + leaving +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new GroupMember();
      }
   }
}
