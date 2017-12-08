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
package org.cacheonix.impl.config;

import java.net.InetAddress;

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class MulticastBroadcastConfiguration.
 */
public final class MulticastBroadcastConfiguration extends DocumentReader {

   /**
    * A address of the multicast group used for broadcast-style communication in the cluster. Cacheonix uses multicast
    * address send and receive messages to maintains cluster membership and inter-cache communications. All Cacheonix
    * instances that belong to the same cluster should use the same multicast address. IP v.4 defines multicast
    * addresses as addresses from 224.0.0.0 to 239.255.255.255. Addresses in the range 224.0.0.0 to 224.0.0.255 are
    * reserved for low-level multicast support operations. We recommend using the range 239.000.000.000-239.255.255.255
    * for setting multicastAddress.
    * <p/>
    * Example: multicastAddress="239.0.10.1".
    */
   private InetAddress multicastAddress = StringUtils.readInetAddress("225.0.1.2");

   /**
    * A multicast port. A mandatory attribute multicastPort defines a UDP port that Cacheonix should use to receive and
    * to send multicast messages. All Cacheonix instances that belong to the same cluster should use the same multicast
    * port. We recommend setting multicast port to 9999.
    */
   private int multicastPort = 9999;

   /**
    * Multicast TTL defines multicast TTL that is used to control how far the multicast messages can propagate.
    * <p/>
    * A value of zero indicates that a message should not be forwarded out of the current host. This value should be
    * used in development mode to avoid unintentional communications with cluster nodes running on other developer's
    * machines.
    * <p/>
    * A value of one means that the message is not forwarded out of the current subnet. This value is suitable for QA
    * and for most production environments.
    * <p/>
    * Values of two or greater mean larger, scopes as shown in the table below:
    * <p/>
    * <p/>
    * <p/>
    * Multicast TTL Scope
    * <p/>
    * <p/>
    * 0 Node-local
    * <p/>
    * <p/>
    * 1 Link-local
    * <p/>
    * <p/>
    * Lesser than 32 Site-local
    * <p/>
    * <p/>
    * Lesser than 64 Region-local
    * <p/>
    * <p/>
    * Lesser than 128 Continent-local
    * <p/>
    * <p/>
    * Lesser than 255 Global
    */
   private int multicastTTL = 1;


   /**
    * Returns the address of the multicast group used for broadcast-style communication in the cluster. Cacheonix uses
    * multicast address send and receive messages to maintains cluster membership and inter-cache communications. IP v.4
    * defines multicast addresses as addresses from 224.0.0.0 to 239.255.255.255. Addresses in the range 224.0.0.0 to
    * 224.0.0.255 are reserved for low-level multicast support operations. We recommend using the range
    * 239.000.000.000-239.255.255.255 for setting multicastAddress. All Cacheonix instances that belong to the same
    * cluster should use the same multicast address.
    * <p/>
    * Example: multicastAddress="239.0.10.1".
    *
    * @return the address of the multicast group used for broadcast-style communication in the cluster.
    */
   public InetAddress getMulticastAddress() {

      return this.multicastAddress;
   }


   /**
    * Returns the multicast port.
    *
    * @return the multicast port.
    */
   public int getMulticastPort() {

      return this.multicastPort;
   }


   /**
    * Returns the value of field 'multicastTTL'. The field 'multicastTTL' has the following description: Multicast TTL
    * defines multicast TTL that is used to control how far the multicast messages can propagate.
    * <p/>
    * A value of zero indicates that a message should not be forwarded out of the current host. This value should be
    * used in development mode to avoid unintentional communications with cluster nodes running on other developer's
    * machines.
    * <p/>
    * A value of one means that the message is not forwarded out of the current subnet. This value is suitable for QA
    * and for most production environments.
    * <p/>
    * Values of two or greater mean larger, scopes as shown in the table below:
    * <p/>
    * <p/>
    * <p/>
    * Multicast TTL Scope
    * <p/>
    * <p/>
    * 0 Node-local
    * <p/>
    * <p/>
    * 1 Link-local
    * <p/>
    * <p/>
    * Lesser than 32 Site-local
    * <p/>
    * <p/>
    * Lesser than 64 Region-local
    * <p/>
    * <p/>
    * Lesser than 128 Continent-local
    * <p/>
    * <p/>
    * Lesser than 255 Global
    *
    * @return the value of field 'MulticastTTL'.
    */
   public int getMulticastTTL() {

      return this.multicastTTL;
   }


   /**
    * Sets the value of field 'multicastTTL'. The field 'multicastTTL' has the following description: Multicast TTL
    * defines multicast TTL that is used to control how far the multicast messages can propagate.
    * <p/>
    * A value of zero indicates that a message should not be forwarded out of the current host. This value should be
    * used in development mode to avoid unintentional communications with cluster nodes running on other developer's
    * machines.
    * <p/>
    * A value of one means that the message is not forwarded out of the current subnet. This value is suitable for QA
    * and for most production environments.
    * <p/>
    * Values of two or greater mean larger, scopes as shown in the table below:
    * <p/>
    * <p/>
    * <p/>
    * Multicast TTL Scope
    * <p/>
    * <p/>
    * 0 Node-local
    * <p/>
    * <p/>
    * 1 Link-local
    * <p/>
    * <p/>
    * Lesser than 32 Site-local
    * <p/>
    * <p/>
    * Lesser than 64 Region-local
    * <p/>
    * <p/>
    * Lesser than 128 Continent-local
    * <p/>
    * <p/>
    * Lesser than 255 Global
    *
    * @param multicastTTL the value of field 'multicastTTL'.
    */
   public void setMulticastTTL(final int multicastTTL) {

      this.multicastTTL = multicastTTL;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("multicastAddress".equals(attributeName)) {

         if (SystemProperty.CACHEONIX_MULTICAST_ADDRESS == null) {

            multicastAddress = StringUtils.readInetAddress(attributeValue);
         } else {

            multicastAddress = SystemProperty.CACHEONIX_MULTICAST_ADDRESS;
         }
      } else if ("multicastPort".equals(attributeName)) {


         multicastPort = systemOrAttribute(SystemProperty.CACHEONIX_MULTICAST_PORT, attributeValue);

      } else if ("multicastTTL".equals(attributeName)) {

         multicastTTL = systemOrAttribute(SystemProperty.CACHEONIX_MULTICAST_TTL, attributeValue);
      }
   }


   @Override
   void postProcessRead() {

   }


   public String toString() {

      return "MulticastBroadcastConfiguration{" +
              "multicastAddress='" + multicastAddress + '\'' +
              ", multicastPort=" + multicastPort +
              ", multicastTTL=" + multicastTTL +
              '}';
   }
}
