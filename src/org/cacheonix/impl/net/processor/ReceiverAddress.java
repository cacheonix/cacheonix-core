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
package org.cacheonix.impl.net.processor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import org.cacheonix.cache.Immutable;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.ArrayUtils;

/**
 * An immutable address of a message receiver.
 */
@SuppressWarnings("RedundantIfStatement")
public final class ReceiverAddress implements Wireable, Immutable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private int tcpPort;

   private InetAddress[] addresses;


   public ReceiverAddress(final InetAddress tcpAddress, final int tcpPort) {

      this.addresses = new InetAddress[]{tcpAddress};
      this.tcpPort = tcpPort;
   }


   /**
    * Required by wireable.
    */
   public ReceiverAddress() {

   }


   public ReceiverAddress(final InetAddress[] addresses, final int tcpPort) {

      this.addresses = ArrayUtils.copy(addresses);
      this.tcpPort = tcpPort;
   }


   public int getTcpPort() {

      return tcpPort;
   }


   public InetAddress[] getAddresses() {

      return ArrayUtils.copy(addresses);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      if (addresses == null || addresses.length == 0) {

         out.writeInt(0);
      } else {

         out.writeInt(addresses.length);
         for (final InetAddress address : addresses) {

            SerializerUtils.writeInetAddress(address, out, false);
         }
      }
      out.writeInt(tcpPort);
   }


   public void readWire(final DataInputStream in) throws IOException {

      final int addressesLength = in.readInt();
      if (addressesLength == 0) {

         addresses = null;
      } else {

         addresses = new InetAddress[addressesLength];
         for (int i = 0; i < addressesLength; i++) {

            addresses[i] = SerializerUtils.readInetAddress(in, false);
         }
      }
      tcpPort = in.readInt();
   }


   public int getWireableType() {

      return TYPE_RECEIVER_ADDRESS;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ReceiverAddress that = (ReceiverAddress) o;

      if (tcpPort != that.tcpPort) {
         return false;
      }
      if (!Arrays.equals(addresses, that.addresses)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = tcpPort;
      result = 31 * result + (addresses != null ? Arrays.hashCode(addresses) : 0);
      return result;
   }


   public boolean isAddressOf(final ClusterNodeAddress clusterNodeAddress) {

      if (clusterNodeAddress == null) {

         return false;
      }

      return tcpPort == clusterNodeAddress.getTcpPort() && clusterNodeAddress.hasAnyOf(addresses);

   }


   public String toString() {

      return "ReceiverAddress{" +
              "tcpPort=" + tcpPort +
              ", addresses=" + (addresses == null ? null : Arrays.asList(addresses)) +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new ReceiverAddress();
      }
   }
}
