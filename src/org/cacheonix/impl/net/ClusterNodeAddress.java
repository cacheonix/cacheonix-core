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
package org.cacheonix.impl.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.hashcode.HashCode;
import org.cacheonix.impl.util.hashcode.HashCodeType;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Processes communicate by exchanging messages. Each process is unique. An example of a process is a cache.
 * <p/>
 * ClusterNodeAddressImpl is an immutable object.
 *
 * @noinspection FieldNotUsedInToString, CompareToUsesNonFinalVariable
 */
public final class ClusterNodeAddress implements Comparable, Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterNodeAddress.class); // NOPMD

   /**
    * Cache that keeps inlined cluster node addresses.
    */
   private static final Map<ClusterNodeAddress, ClusterNodeAddress> cache = new HashMap<ClusterNodeAddress, ClusterNodeAddress>(
           111); // NOPMD

   /**
    * A helper constant used to typify conversation from a list to an array.
    */
   private static final InetAddress[] INET_ADDRESS_ARRAY_TEMPLATE = new InetAddress[0];

   /**
    * TCP tcpPort number of the process.
    */
   private int tcpPort = 0;

   /**
    * List of InetAddresses.
    *
    * @see InetAddress
    */
   private InetAddress[] addresses = null;

   /**
    * Core count.
    */
   private int coreCount = 0;

   /**
    * Hashcode calculated at object creation.
    */
   private int precalculatedHashCode = 0;


   /**
    * @noinspection UnnecessaryThis, JavaDoc
    */
   public ClusterNodeAddress(final int port, final InetAddress[] addresses) {

      this.tcpPort = port;
      this.addresses = ArrayUtils.copy(addresses);
      this.precalculatedHashCode = calculateHashCode();
      this.coreCount = Runtime.getRuntime().availableProcessors();
   }


   public ClusterNodeAddress() {

   }


   /**
    * @return TCP tcpPort number of the process.
    */
   public int getTcpPort() {

      return tcpPort;
   }


   /**
    * Returns addresses that the cluster node advertises as listening on.
    *
    * @return the addresses that the cluster node advertises as listening on.
    */
   public InetAddress[] getAddresses() {

      return ArrayUtils.copy(addresses);
   }


   int getCoreCount() {

      return coreCount;
   }


   /**
    * Returns <code>true</code> if listen-on addresses are loopback-only.
    *
    * @return <code>true</code> if listen-on addresses are loopback-only.
    */
   public boolean isLoopbackOnly() {

      boolean loopBackOnly = addresses[0].isLoopbackAddress();
      for (int i = 1; i < addresses.length && loopBackOnly; i++) {

         loopBackOnly = addresses[i].isLoopbackAddress();
      }

      return loopBackOnly;
   }


   /**
    * @noinspection EqualsWhichDoesntCheckParameterClass
    */
   public boolean equals(final Object obj) {

      return calculateEquals(obj);
   }


   public int hashCode() {

      return precalculatedHashCode;
   }


   /**
    * @noinspection RedundantIfStatement, JavaDoc
    */
   private boolean calculateEquals(final Object obj) {
      //noinspection ObjectEquality
      if (this == obj) {
         return true;
      }
      if (obj == null || !getClass().equals(obj.getClass())) {
         return false;
      }
      final ClusterNodeAddress other = (ClusterNodeAddress) obj;
      if (precalculatedHashCode != other.precalculatedHashCode) {
         return false;
      }

      if (!Arrays.equals(addresses, other.addresses)) {
         return false;
      }
      if (tcpPort != other.tcpPort) {
         return false;
      }
      if (coreCount != other.coreCount) {
         return false;
      }
      return true;
   }


   private int calculateHashCode() {

      final HashCode hashCode = new HashCode(HashCodeType.STRONG);
      hashCode.add(tcpPort);
      hashCode.add(coreCount);
      hashCode.add(IOUtils.inetAddressesHashCode(addresses));
      return hashCode.getValue();
   }


   /**
    * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive integer
    * as this object is less than, equal to, or greater than the specified object.<p>
    * <p/>
    * In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
    * <i>signum</i> function, which is defined to return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to
    * whether the value of <i>expression</i> is negative, zero or positive.
    * <p/>
    * The implementer must ensure <tt>sgn(x.compareTo(y)) == -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and
    * <tt>y</tt>.  (This implies that <tt>x.compareTo(y)</tt> must throw an exception iff <tt>y.compareTo(x)</tt> throws
    * an exception.)<p>
    * <p/>
    * The implementor must also ensure that the relation is transitive: <tt>(x.compareTo(y)&gt;0 &amp;&amp;
    * y.compareTo(z)&gt;0)</tt> implies <tt>x.compareTo(z)&gt;0</tt>.<p>
    * <p/>
    * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt> implies that <tt>sgn(x.compareTo(z)) ==
    * sgn(y.compareTo(z))</tt>, for all <tt>z</tt>.<p>
    * <p/>
    * It is strongly recommended, but <i>not</i> strictly required that <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.
    * Generally speaking, any class that implements the <tt>Comparable</tt> interface and violates this condition should
    * clearly indicate this fact.  The recommended language is "Note: this class has a natural ordering that is
    * inconsistent with equals."
    *
    * @param o the Object to be compared.
    * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
    * specified object.
    * @throws ClassCastException if the specified object's type prevents it from being compared to this Object.
    */
   public int compareTo(final Object o) {

      final ClusterNodeAddress other = (ClusterNodeAddress) o;

      if (precalculatedHashCode > other.precalculatedHashCode) {
         return 1;
      }
      if (precalculatedHashCode < other.precalculatedHashCode) {
         return -1;
      }

      if (tcpPort > other.tcpPort) {
         return 1;
      }
      if (tcpPort < other.tcpPort) {
         return -1;
      }

      if (coreCount > other.coreCount) {
         return 1;
      }
      if (coreCount < other.coreCount) {
         return -1;
      }

      final int addressesCompare = IOUtils.inetAddressesCompare(addresses, other.addresses);
      if (addressesCompare > 0) {
         return 1;
      }
      if (addressesCompare < 0) {
         return -1;
      }

      return 0;
   }


   /**
    * Creates a cluster node address uniquely identifying a cluster node using given parameters.
    *
    * @param address the address of the node.
    * @param port    the port of the node.
    * @return a new instance
    * @throws IOException
    */
   public static ClusterNodeAddress createAddress(final String address, final int port) throws IOException {

      final List<InetAddress> inetAddressList = new ArrayList<InetAddress>(3);
      if (StringUtils.isBlank(address)) {

         inetAddressList.addAll(NetUtils.getLocalInetAddresses());
      } else {

         final InetAddress inetAddress = InetAddress.getByName(address);
         inetAddressList.add(inetAddress);
      }

      final InetAddress[] inetAddresses = inetAddressList.toArray(INET_ADDRESS_ARRAY_TEMPLATE);

      return new ClusterNodeAddress(port, inetAddresses);
   }


   public static ClusterNodeAddress[] copy(final ClusterNodeAddress[] array) {

      if (array == null) {
         return null;
      }
      final ClusterNodeAddress[] result = new ClusterNodeAddress[array.length];
      System.arraycopy(array, 0, result, 0, array.length);
      return result;
   }


   /**
    * Returns <code>true</code> if the list of addresses is the same.
    *
    * @param address the address to compare with.
    * @return <code>true</code> if the list of addresses is the same.
    */
   public boolean sameHost(final ClusterNodeAddress address) {

      return Arrays.equals(addresses, address.addresses);
   }


   public boolean hasAnyOf(final InetAddress[] addresses) {

      for (final InetAddress address : addresses) {

         for (final InetAddress clusterNodeInetAddress : this.addresses) {

            if (address.equals(clusterNodeInetAddress)) {

               return true;
            }
         }
      }

      return false;
   }


   public ClusterNodeAddress inline() {

      synchronized (cache) {

         final ClusterNodeAddress existing = cache.get(this);
         if (existing != null) {
            return existing;
         }

         cache.put(this, this);
      }

      return this;
   }


   public int getWireableType() {

      return TYPE_NODE_ADDRESS;
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
      out.writeShort(coreCount);
      out.writeInt(precalculatedHashCode);
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
      coreCount = in.readShort();
      precalculatedHashCode = in.readInt();
   }


   public String toString() {

      return Arrays.toString(addresses) + ':' + tcpPort;
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClusterNodeAddress();
      }
   }
}
