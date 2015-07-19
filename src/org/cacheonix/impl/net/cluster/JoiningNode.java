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
package org.cacheonix.impl.net.cluster;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * A server that would like to join the cluster.
 */
@SuppressWarnings("RedundantIfStatement")
public final class JoiningNode implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private ClusterNodeAddress address;


   public JoiningNode() {

   }


   public JoiningNode(final ClusterNodeAddress address) {

      this.address = address;
   }


   public ClusterNodeAddress getAddress() {

      return address;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      SerializerUtils.writeAddress(address, out);
   }


   public void readWire(final DataInputStream in) throws IOException {

      address = SerializerUtils.readAddress(in);
   }


   public int getWireableType() {

      return TYPE_JOINING_NODE;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final JoiningNode that = (JoiningNode) o;

      if (address != null ? !address.equals(that.address) : that.address != null) {
         return false;
      }
      return true;
   }


   public int hashCode() {

      return address != null ? address.hashCode() : 0;
   }


   public String toString() {

      return "JoiningNode{" +
              "address=" + address +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new JoiningNode();
      }
   }
}
