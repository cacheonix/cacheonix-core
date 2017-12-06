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
package org.cacheonix.impl.cluster.node.state.group;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ReplicatedStateProcessorKey;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A request to add a group member. This request is executed by all cluster views synchronuously.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals,
 * NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals, NonFinalFieldReferenceInEquals,
 * NonFinalFieldReferencedInHashCode, NonFinalFieldReferencedInHashCode, NonFinalFieldReferencedInHashCode
 * @since Jan 19, 2009 2:37:52 AM
 */
public abstract class GroupMessage extends Message {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GroupMessage.class); // NOPMD


   /**
    * Group name.
    */
   private String groupName = null;

   /**
    * Group type.
    */
   private int groupType = Group.GROUP_TYPE_CACHE;


   /**
    * Required to support implementor's no-args constructor required by Externalizable.
    */
   protected GroupMessage() {

   }


   /**
    * Constructor.
    *
    * @param requestType
    * @param groupType   group type.
    * @param groupName   group name.
    */
   GroupMessage(final int requestType, final int groupType, final String groupName,
                final ClusterNodeAddress sender) {

      super(requestType);
      setSender(sender);
      this.groupType = groupType;
      this.groupName = groupName;
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return ReplicatedStateProcessorKey.getInstance();
   }


   /**
    * Constructor.
    *
    * @param requestType
    * @param groupType   group type.
    * @param groupName   group name.
    */
   protected GroupMessage(final int requestType, final int groupType, final String groupName) {

      super(requestType);
      this.groupType = groupType;
      this.groupName = groupName;
   }


   /**
    * Returns group type.
    *
    * @return group type.
    */
   public final int getGroupType() {

      return groupType;
   }


   /**
    * Returns group name.
    *
    * @return group name.
    */
   public final String getGroupName() {

      return groupName;
   }


   protected final ReplicatedState getReplicatedState() {

      final ClusterProcessor clusterProcessor = (ClusterProcessor) getProcessor();

      return clusterProcessor.getProcessorState().getReplicatedState();
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      groupType = in.readInt();
      groupName = SerializerUtils.readString(in);
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeInt(groupType);
      SerializerUtils.writeString(groupName, out);
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || !obj.getClass().equals(getClass())) {
         return false;
      }
      if (!super.equals(obj)) {
         return false;
      }

      final GroupMessage that = (GroupMessage) obj;

      if (groupType != that.groupType) {
         return false;
      }
      return groupName != null ? groupName.equals(that.groupName) : that.groupName == null;

   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
      result = 31 * result + groupType;
      return result;
   }


   public String toString() {

      return "GroupMessage{" +
              "groupName='" + groupName + '\'' +
              ", groupType=" + groupType +
              "} " + super.toString();
   }
}
