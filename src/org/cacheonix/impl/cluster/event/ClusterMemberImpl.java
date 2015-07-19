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
package org.cacheonix.impl.cluster.event;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.cacheonix.cluster.ClusterMember;
import org.cacheonix.cluster.ClusterMemberAddress;


/**
 * An implementation of user API ClusterMember.
 */
@SuppressWarnings("RedundantIfStatement")
public final class ClusterMemberImpl implements ClusterMember {

   /**
    * A name of the cluster this member belongs to.
    */
   private String clusterName;

   /**
    * A list of ClusterMemberAddress object that this cluster member may listen on.
    */
   private List<ClusterMemberAddress> clusterMemberAddresses;


   /**
    * Cacheonix port.
    */
   private int clusterMemberPort;


   /**
    * Required by Externalizable.
    */
   public ClusterMemberImpl() {

   }


   public ClusterMemberImpl(final String clusterName, final List<ClusterMemberAddress> clusterMemberAddresses,
                            final int clusterMemberPort) {

      this.clusterName = clusterName;
      this.clusterMemberAddresses = new ArrayList<ClusterMemberAddress>(clusterMemberAddresses);
      this.clusterMemberPort = clusterMemberPort;
   }


   /**
    * Returns a name of the cluster this cluster member belongs to.
    *
    * @return the name of the cluster this cluster member belongs to.
    */
   public String getClusterName() {

      return clusterName;
   }


   /**
    * Returns an list of <code>ClusterMemberAddress</code> objects that this cluster member may be accessed at.
    *
    * @return a list of <code>ClusterMemberAddress</code> objects that this cluster member may be accessed at.
    */
   public List<ClusterMemberAddress> getClusterMemberAddresses() {

      return new ArrayList<ClusterMemberAddress>(clusterMemberAddresses);
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

      out.writeUTF(clusterName);
      out.writeInt(clusterMemberPort);
      out.writeInt(clusterMemberAddresses.size());
      for (final ClusterMemberAddress clusterMemberAddress : clusterMemberAddresses) {

         clusterMemberAddress.writeExternal(out);
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

      clusterName = in.readUTF();
      clusterMemberPort = in.readInt();
      final int size = in.readInt();
      clusterMemberAddresses = new ArrayList<ClusterMemberAddress>(size);
      for (int i = 0; i < size; i++) {

         final ClusterMemberAddress clusterMemberAddress = new ClusterMemberAddressImpl();
         clusterMemberAddress.readExternal(in);
         clusterMemberAddresses.add(clusterMemberAddress);
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ClusterMemberImpl that = (ClusterMemberImpl) o;

      if (clusterMemberPort != that.clusterMemberPort) {
         return false;
      }
      if (clusterMemberAddresses != null ? !clusterMemberAddresses.equals(that.clusterMemberAddresses) : that.clusterMemberAddresses != null) {
         return false;
      }
      if (clusterName != null ? !clusterName.equals(that.clusterName) : that.clusterName != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = clusterName != null ? clusterName.hashCode() : 0;
      result = 31 * result + (clusterMemberAddresses != null ? clusterMemberAddresses.hashCode() : 0);
      result = 31 * result + clusterMemberPort;
      return result;
   }


   @Override
   public String toString() {

      return "ClusterMemberImpl{" +
              "clusterName='" + clusterName + '\'' +
              ", clusterMemberAddresses=" + clusterMemberAddresses +
              ", clusterMemberPort=" + clusterMemberPort +
              '}';
   }
}
