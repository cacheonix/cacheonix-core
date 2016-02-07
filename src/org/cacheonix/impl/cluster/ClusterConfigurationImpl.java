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
package org.cacheonix.impl.cluster;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;

import org.cacheonix.cluster.ClusterConfiguration;
import org.cacheonix.cluster.ClusterMember;
import org.cacheonix.cluster.ClusterState;

/**
 * An implementation of user API ClusterConfiguration.
 */
@SuppressWarnings("RedundantIfStatement")
final class ClusterConfigurationImpl implements ClusterConfiguration {

   private static final long serialVersionUID = 4356310544242603491L;

   private Collection<ClusterMember> clusterMembers;

   private ClusterState clusterState;

   private String uuid;


   /**
    * Required by Externalizable.
    */
   @SuppressWarnings("UnusedDeclaration")
   public ClusterConfigurationImpl() {

   }


   /**
    * Creates ClusterConfigurationImpl.
    *
    * @param uuid           a cluster UUID.
    * @param clusterState   a cluster state.
    * @param clusterMembers a list of cluster members that this constructor uses to create a copy.
    */
   ClusterConfigurationImpl(final String uuid, final ClusterState clusterState,
           final Collection<ClusterMember> clusterMembers) {


      this.clusterMembers = new ArrayList<ClusterMember>(clusterMembers);
      this.clusterState = clusterState;
      this.uuid = uuid;
   }


   /**
    * Returns an un-modifiable collection of servers constituting the cluster.
    *
    * @return the un-modifiable collection of servers of the cluster.
    */
   public Collection<ClusterMember> getClusterMembers() {

      return new ArrayList<ClusterMember>(clusterMembers);
   }


   /**
    * {@inheritDoc}
    */
   public ClusterState getClusterState() {

      return clusterState;
   }


   /**
    * {@inheritDoc}
    */
   public String getClusterUUID() {

      return uuid;
   }


   /**
    * The object implements the writeExternal method to save its contents by calling the methods of DataOutput for its
    * primitive values or calling the writeObject method of ObjectOutput for objects, strings, and arrays.
    *
    * @param out the stream to write the object to
    * @throws IOException Includes any I/O exceptions that may occur
    * @serialData Overriding methods should use this tag to describe the data layout of this Externalizable object. List
    * the sequence of element types and, if possible, relate the element to a public/protected field and/or method of
    * this Externalizable class.
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.writeUTF(uuid);

      //
      ClusterState.writeDataOutput(clusterState, out);

      //
      out.writeInt(clusterMembers.size());
      for (final ClusterMember clusterMember : clusterMembers) {
         clusterMember.writeExternal(out);
      }
   }


   /**
    * The object implements the readExternal method to restore its contents by calling the methods of DataInput for
    * primitive types and readObject for objects, strings and arrays.  The readExternal method must read the values in
    * the same sequence and with the same types as were written by writeExternal.
    *
    * @param in the stream to read data from in order to restore the object
    * @throws IOException            if I/O errors occur
    * @throws ClassNotFoundException If the class for an object being restored cannot be found.
    */
   public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

      uuid = in.readUTF();

      //
      clusterState = ClusterState.readDataInput(in);

      //
      final int size = in.readInt();
      clusterMembers = new ArrayList<ClusterMember>(size);
      for (int i = 0; i < size; i++) {

         final ClusterMember clusterMember = new ClusterMemberImpl();
         clusterMember.readExternal(in);
         clusterMembers.add(clusterMember);
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ClusterConfigurationImpl that = (ClusterConfigurationImpl) o;

      if (clusterMembers != null ? !clusterMembers.equals(that.clusterMembers) : that.clusterMembers != null) {
         return false;
      }
      if (clusterState != null ? !clusterState.equals(that.clusterState) : that.clusterState != null) {
         return false;
      }
      if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = clusterMembers != null ? clusterMembers.hashCode() : 0;
      result = 31 * result + (clusterState != null ? clusterState.hashCode() : 0);
      result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "ClusterConfigurationImpl{" +
              "uuid='" + uuid + '\'' +
              ", clusterState=" + clusterState +
              ", clusterMembers=" + clusterMembers +
              '}';
   }
}
