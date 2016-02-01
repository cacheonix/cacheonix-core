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
package org.cacheonix.util;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import org.cacheonix.Version;
import org.cacheonix.impl.util.StringUtils;

import static org.cacheonix.impl.util.StringUtils.toYesNo;


/**
 * This utility class outputs list of network interfaces to System.out.
 *
 * @noinspection MethodMayBeStatic, UnusedCatchParameter
 */
public final class NetworkInterfaceEnumerator {

   private static final char LINE_SEPARATOR = '\n';

   private static final int DEFAULT_STRING_BUFFER_LENGTH = 200;

   private static final Class[] EMPTY_CLASS_ARRAY = {};

   private static final Object[] EMPTY_OBJECT_ARRAY = {};

   private static final String INET_ADDRESS_ES = "         Inet address: ";

   private static final String INTERFACE_ADDRESS_ES = "    Interface address: ";

   private static final String LOOPBACK = "             Loopback: ";

   private static final String MTU = "                  MTU: ";

   private static final String POINT_TO_POINT = "       Point-to-point: ";

   private static final String SEPARATOR = StringUtils.appendChars(new StringBuffer(80), 80, '=').toString();

   private static final String SUPPORTS_MULTICAST = "   Supports multicast: ";

   private static final String UP = "                   Up: ";

   private static final String VIRTUAL = "              Virtual: ";

   static final String DISPLAY_NAME = "         Display name: ";

   static final String NAME = "       Interface name: ";


   /**
    * @noinspection UseOfSystemOutOrSystemErr
    */
   public static void main(final String[] args) throws SocketException,
           IllegalAccessException, NoSuchMethodException, InvocationTargetException {

      new NetworkInterfaceEnumerator().printInterfaces(System.out);
   }


   /**
    * Prints network interfaces.
    *
    * @param stream to print to
    * @throws SocketException
    */
   void printInterfaces(final PrintStream stream) throws SocketException, NoSuchMethodException,
           IllegalAccessException, InvocationTargetException {

      stream.println(Version.getVersion() + ". Available network interfaces: ");
      stream.println();
      final Enumeration netIfs = NetworkInterface.getNetworkInterfaces();
      while (netIfs.hasMoreElements()) {
         stream.println(SEPARATOR);
         final NetworkInterface netIf = (NetworkInterface) netIfs.nextElement();
         stream.println(NAME + netIf.getName());
         stream.println(DISPLAY_NAME + netIf.getDisplayName());
         stream.println(INET_ADDRESS_ES + inetAddressesToString(netIf));

         try {

            final List interfaceAddresses = (List) getInterfaceAddressesMethod(netIf).invoke(netIf, EMPTY_OBJECT_ARRAY);
            final StringBuilder buf = new StringBuilder(DEFAULT_STRING_BUFFER_LENGTH);
            for (int i = 0, n = interfaceAddresses.size(); i < n; i++) { // NOPMD
               final Object interfaceAddress = interfaceAddresses.get(i);
               final Method getAddressMethod = interfaceAddress.getClass().getMethod("getAddress", EMPTY_CLASS_ARRAY);
               final Method getNetworkPrefixLengthMethod = interfaceAddress.getClass().getMethod(
                       "getNetworkPrefixLength",
                       EMPTY_CLASS_ARRAY);
               buf.append(StringUtils.toString(
                       (InetAddress) getAddressMethod.invoke(interfaceAddress, EMPTY_OBJECT_ARRAY))).append('/')
                       .append(getNetworkPrefixLengthMethod.invoke(interfaceAddress, EMPTY_OBJECT_ARRAY));
               if (i < n - 1) {
                  buf.append(", ");
               }
            }
            stream.println(INTERFACE_ADDRESS_ES + buf);
         } catch (final NoSuchMethodException ignored) {
            // Multicast not supported
         }

         try {

            final Method mtuMethod = getMTUMethod(netIf);
            stream.println(MTU + ((Number) mtuMethod.invoke(netIf, EMPTY_OBJECT_ARRAY)).intValue());
         } catch (final NoSuchMethodException ignored) {
            // Multicast not supported
         }

         try {

            final Method supportsMulticastMethod = getSupportsMulticastMethod(netIf);
            final Boolean supportsMulticast = (Boolean) supportsMulticastMethod.invoke(netIf, EMPTY_OBJECT_ARRAY);
            stream.println(SUPPORTS_MULTICAST + toYesNo(supportsMulticast));
         } catch (final NoSuchMethodException ignored) {
            // Multicast not supported
         }

         try {

            final Method isUpMethod = getIsUpMethod(netIf);
            final Boolean isUp = (Boolean) isUpMethod.invoke(netIf, EMPTY_OBJECT_ARRAY);
            stream.println(UP + toYesNo(isUp));
         } catch (final NoSuchMethodException ignore) {
            // Is up is not supported
         }

         try {
            final Method isLoopbackMethod = getIsLoopbackMethod(netIf);
            final Boolean isLoopback = (Boolean) isLoopbackMethod.invoke(netIf, EMPTY_OBJECT_ARRAY);
            stream.println(LOOPBACK + toYesNo(isLoopback));
         } catch (final NoSuchMethodException ignore) {
            // isLoopback is not supported
         }

         try {

            final Method isPointToPointMethod = getIsPointToPointMethod(netIf);
            final Boolean isPointToPoint = (Boolean) isPointToPointMethod.invoke(netIf, EMPTY_OBJECT_ARRAY);
            stream.println(POINT_TO_POINT + toYesNo(isPointToPoint));
         } catch (final NoSuchMethodException ignore) {
            // isPointToPoint is not supported
         }

         try {

            final Method isVirtualMethod = getIsVirtualMethod(netIf);
            final Object isVirtual = isVirtualMethod.invoke(netIf, EMPTY_OBJECT_ARRAY);
            stream.println(VIRTUAL + toYesNo((Boolean) isVirtual));
         } catch (final NoSuchMethodException ignore) {
            // isPointToPoint is not supported
         }

         stream.println();
      }
   }


   private Method getIsVirtualMethod(final NetworkInterface netIf) throws NoSuchMethodException {

      return netIf.getClass().getMethod("isVirtual", EMPTY_CLASS_ARRAY);
   }


   private Method getIsPointToPointMethod(final NetworkInterface netIf) throws NoSuchMethodException {

      return netIf.getClass().getMethod("isPointToPoint", EMPTY_CLASS_ARRAY);
   }


   private Method getIsLoopbackMethod(final NetworkInterface netIf) throws NoSuchMethodException {

      return netIf.getClass().getMethod("isLoopback", EMPTY_CLASS_ARRAY);
   }


   private Method getIsUpMethod(final NetworkInterface netIf) throws NoSuchMethodException {

      return netIf.getClass().getMethod("isUp", EMPTY_CLASS_ARRAY);
   }


   private Method getSupportsMulticastMethod(final NetworkInterface netIf) throws NoSuchMethodException {

      return netIf.getClass().getMethod("supportsMulticast", EMPTY_CLASS_ARRAY);
   }


   private Method getMTUMethod(final NetworkInterface netIf) throws NoSuchMethodException {

      return netIf.getClass().getMethod("MTU", EMPTY_CLASS_ARRAY);
   }


   private Method getInterfaceAddressesMethod(final NetworkInterface netIf)
           throws NoSuchMethodException {

      return netIf.getClass().getMethod("getInterfaceAddresses", EMPTY_CLASS_ARRAY);
   }


   /**
    * Transform an enumeration of InetAddresses provided by NetworkInterface to a string.
    *
    * @param netIf NetworkInterface
    * @return String
    */
   private String inetAddressesToString(final NetworkInterface netIf) {

      final Enumeration inetAddresses = netIf.getInetAddresses();
      final StringBuilder buf = new StringBuilder(DEFAULT_STRING_BUFFER_LENGTH);
      boolean firstLine = true;
      while (inetAddresses.hasMoreElements()) {
         final InetAddress address = (InetAddress) inetAddresses.nextElement();
         if (firstLine) {
            buf.append(StringUtils.toString(address));
            buf.append(LINE_SEPARATOR);
            firstLine = false;
         }
         buf.append("  Canonical host name: ").append(address.getCanonicalHostName()).append(LINE_SEPARATOR);
         buf.append("   Site-local address: ").append(toYesNo(address.isSiteLocalAddress())).append(LINE_SEPARATOR);
         buf.append("   Link local address: ").append(toYesNo(address.isLinkLocalAddress())).append(LINE_SEPARATOR);
         buf.append("    Any local address: ").append(toYesNo(address.isAnyLocalAddress())).append(LINE_SEPARATOR);
         buf.append("     Loopback address: ").append(toYesNo(address.isLoopbackAddress())).append(LINE_SEPARATOR);
         buf.append("            Host name: ").append(address.getHostName()).append(LINE_SEPARATOR);
      }
      return buf.toString();
   }


   public String toString() {

      return "NetworkInterfaceEnumerator{}";
   }
}
