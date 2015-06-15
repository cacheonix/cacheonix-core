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
package org.cacheonix.impl.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.cacheonix.util.HashCode;
import org.cacheonix.util.HashCodeType;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.exception.ExceptionUtils;
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
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private static final String CACHEONIX_CLUSTER_NUMBER = "cacheonix.cluster.number";

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterNodeAddress.class); // NOPMD

   /**
    * Cache that keeps inlined cluster node addresses.
    */
   private static final HashMap<ClusterNodeAddress, ClusterNodeAddress> cache = new HashMap<ClusterNodeAddress, ClusterNodeAddress>(111); // NOPMD

   /**
    * TCP tcpPort number of the process.
    */
   private int tcpPort = 0;

   /**
    * Resolved host name or string representation of the
    */
   private String hostName = null;

   /**
    * Random number that should be assigned at creation time. This ID is used to spread out processes identity when
    * forming total order for messages.
    */
   private int number = 0;

   /**
    * List of InetAddresses.
    *
    * @see InetAddress
    */
   private InetAddress[] addresses = null;

   /**
    * Core count.
    */
   private int coreCount;

   /**
    * Hashcode calculated at object creation.
    */
   private int precalculatedHashCode = 0;


   /**
    * @noinspection UnnecessaryThis, JavaDoc
    */
   public ClusterNodeAddress(final int port, final String hostName, final int number, final InetAddress[] addresses) {

      this.tcpPort = port;
      this.hostName = hostName;
      this.number = number;
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


   /**
    * @return resolved host name or string representation of the IP address.
    */
   public String getHostName() {

      return hostName;
   }


   public int getCoreCount() {

      return coreCount;
   }


   /**
    * @return random number assigned at creation time. This ID is used to spread out processes identity when forming
    *         total order for messages.
    */
   public int getNumber() {

      return number;
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
      if (number != other.number) {
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
      if (!hostName.equals(other.hostName)) {
         return false;
      }
      return true;
   }


   private int calculateHashCode() {

      final HashCode hashCode = new HashCode(HashCodeType.STRONG);
      hashCode.add(tcpPort);
      hashCode.add(coreCount);
      hashCode.add(hostName);
      hashCode.add(IOUtils.inetAddressesHashCode(addresses));
      hashCode.add(number);
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
    *         specified object.
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

      if (number > other.number) {
         return 1;
      }

      if (number < other.number) {
         return -1;
      }

      final int addressesCompare = IOUtils.inetAddressesCompare(addresses, other.addresses);
      if (addressesCompare > 0) {
         return 1;
      }
      if (addressesCompare < 0) {
         return -1;
      }

      final int hostNameCompare = hostName.compareTo(other.hostName);
      if (hostNameCompare > 0) {
         return 1;
      }
      if (hostNameCompare < 0) {
         return -1;
      }

      return 0;
   }


   /**
    * Creates a process ID using given parameters.
    *
    * @param address
    * @param port
    * @return
    * @throws IOException
    */
   public static ClusterNodeAddress createAddress(final String address, final int port) throws IOException {

      try {
         final List<InetAddress> inetAddressList = new ArrayList<InetAddress>(3);
         final String hostName;
         if (StringUtils.isBlank(address)) {

            inetAddressList.addAll(NetUtils.getLocalInetAddresses());
            hostName = InetAddress.getLocalHost().getHostName();
         } else {

            final InetAddress inetAddress = InetAddress.getByName(address);
            hostName = inetAddress.getHostName();
            inetAddressList.add(inetAddress);
         }

         final InetAddress[] inetAddresses = inetAddressList.toArray(new InetAddress[inetAddressList.size()]);

         final int number = readNumber();

         return new ClusterNodeAddress(port, hostName, number, inetAddresses
         );
      } catch (final NoSuchAlgorithmException e) {
         throw IOUtils.createIOException(e);
      }
   }


   /**
    * Best-effort method to read or create cluster number.
    *
    * @return Random number of this address.
    * @throws NoSuchAlgorithmException if the algorithm used to generate the random number is not available.
    */
   private static synchronized int readNumber() throws NoSuchAlgorithmException {
      // Get user directory
      final String userDir = System.getProperty("user.dir");
      if (StringUtils.isBlank(userDir)) {
         return 0;
      }

      // Read number
      final File cacheonixProperties = new File(userDir, ".cacheonix.properties");
      if (cacheonixProperties.exists()) {
         return readClusterNumber(cacheonixProperties);
      } else {
         // Try to create the file
         final boolean newFile;
         try {
            newFile = cacheonixProperties.createNewFile();
         } catch (final IOException e) {
            LOG.warn(e, e);
            return 0;
         }
         if (newFile) {
            final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(System.currentTimeMillis());
            final Properties properties = new Properties();
            final int number = random.nextInt();
            properties.setProperty(CACHEONIX_CLUSTER_NUMBER, Integer.toString(number));
            try {
               FileOutputStream out = null;
               try {
                  out = new FileOutputStream(cacheonixProperties);
                  properties.store(out, "Cacheonix persistent properties");
               } finally {
                  IOUtils.closeHard(out);
               }
               return number;
            } catch (final Exception e) {
               LOG.warn(e, e);
               return 0;
            }
         } else {
            return readClusterNumber(cacheonixProperties);
         }
      }
   }


   private static int readClusterNumber(final File cacheonixProperties) {

      int result = 0;
      final Properties properties = new Properties();
      FileInputStream inStream = null;
      try {
         inStream = new FileInputStream(cacheonixProperties);
         properties.load(inStream);
         final String property = properties.getProperty(CACHEONIX_CLUSTER_NUMBER);
         if (StringUtils.isValidInteger(property)) {
            result = Integer.parseInt(property);
         }
      } catch (final Exception e) {
         ExceptionUtils.ignoreException(e, "Cannot read the file, ignoring");
      } finally {
         IOUtils.closeHard(inStream);
      }
      return result;
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


   public synchronized ClusterNodeAddress inline() {

      final ClusterNodeAddress existing = cache.get(this);
      if (existing != null) {
         return existing;
      }

      cache.put(this, this);

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
      out.writeInt(number);
      out.writeInt(tcpPort);
      out.writeShort(coreCount);
      out.writeInt(precalculatedHashCode);
      SerializerUtils.writeString(hostName, out);
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
      number = in.readInt();
      tcpPort = in.readInt();
      coreCount = in.readShort();
      precalculatedHashCode = in.readInt();
      hostName = SerializerUtils.readString(in);
   }


   public String toString() {

      return hostName + ':' + tcpPort;
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClusterNodeAddress();
      }
   }
}
