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
package org.cacheonix.impl.cluster;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;

import org.cacheonix.cluster.ClusterMemberAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;

/**
 * An implementation of ClusterMemberAddress.
 */
@SuppressWarnings("RedundantIfStatement")
final class ClusterMemberAddressImpl implements ClusterMemberAddress {

   private static final long serialVersionUID = 3937437755889768537L;

   /**
    * An inet address of the cluster member.
    */
   private InetAddress inetAddress;


   /**
    * A public constructor required by {@link java.io.Externalizable}.
    */
   public ClusterMemberAddressImpl() {

   }


   /**
    * Creates ClusterMemberAddressImpl.
    *
    * @param inetAddress the inet address of the cluster member.
    */
   ClusterMemberAddressImpl(final InetAddress inetAddress) {

      this.inetAddress = inetAddress;
   }


   /**
    * Returns an address of a Cacheonix cluster member.
    *
    * @return the address of a Cacheonix cluster member.
    */
   public InetAddress getInetAddress() {

      return inetAddress;
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

      SerializerUtils.writeInetAddress(inetAddress, out, false);
   }


   /**
    * The object implements the readExternal method to restore its contents by calling the methods of DataInput for
    * primitive types and readObject for objects, strings and arrays.  The readExternal method must read the values in
    * the same sequence and with the same types as were written by writeExternal.
    *
    * @param in the stream to read data from in order to restore the object
    * @throws IOException if I/O errors occur
    */
   public void readExternal(final ObjectInput in) throws IOException {

      inetAddress = SerializerUtils.readInetAddress(in, false);
   }


   @Override
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ClusterMemberAddressImpl that = (ClusterMemberAddressImpl) o;

      if (!inetAddress.equals(that.inetAddress)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return inetAddress.hashCode();
   }


   @Override
   public String toString() {

      return "ClusterMemberAddressImpl{" +
              "inetAddress=" + inetAddress +
              '}';
   }
}
