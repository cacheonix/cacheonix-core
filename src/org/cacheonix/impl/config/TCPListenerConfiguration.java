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
package org.cacheonix.impl.config;

import java.net.InetAddress;

import org.cacheonix.cache.ConfigurationException;
import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class TCPListenerConfiguration.
 */
@SuppressWarnings({"serial", "WeakerAccess"})
public final class TCPListenerConfiguration extends DocumentReader {

   /**
    * Optional IP address used to limit the address Cacheonix node is listening. The address must be set to one of the
    * local interface addresses. If the address is not specified, the node will accept connections on any of the node's
    * IP addresses. This attribute may be useful for cluster nodes with multiple interfaces where the node should accept
    * connections only on a single interface. Do not set this attribute unless there is a reason for limiting addresses
    * that Cacheonix should listen on.
    */
   private InetAddress address;

   /**
    * A mandatory TCP port. Cacheonix uses the TCP port to listen on for network requests. Default is 8777.
    */
   private int port = 9998;

   /**
    * Field buffer.
    */
   private long buffer = StringUtils.readBytes("128k");


   /**
    * Returns the optional IP address used to limit the address Cacheonix node is listening. The address must be set to
    * one of the local interface addresses. If the address is not specified, the node will accept connections on any of
    * the node's IP addresses. This attribute may be useful for cluster nodes with multiple interfaces where the node
    * should accept connections only on a single interface. Do not set this attribute unless there is a reason for
    * limiting addresses that Cacheonix should listen on.
    *
    * @return the optional IP address used to limit the address Cacheonix node is listening.
    */
   public InetAddress getAddress() {

      return this.address;
   }


   /**
    * Returns the value of field 'buffer'.
    *
    * @return the value of field 'Buffer'.
    */
   public long getBuffer() {

      return this.buffer;
   }


   /**
    * Returns the TCP port. Cacheonix uses the TCP port to listen on for network requests. Default is 8777.
    *
    * @return the TCP port. Cacheonix uses the TCP port to listen on for network requests. Default is 8777.
    */
   public int getPort() {

      return this.port;
   }


   /**
    * Sets the optional IP address used to limit the address Cacheonix node is listening.
    *
    * @param address the optional IP address used to limit the address Cacheonix node is listening. The address must be
    *                set to one of the local interface addresses. If the address is not specified, the node will accept
    *                connections on any of the node's IP addresses. This attribute may be useful for cluster nodes with
    *                multiple interfaces where the node should accept connections only on a single interface. Do not set
    *                this attribute unless there is a reason for limiting addresses that Cacheonix should listen on.
    */
   public void setAddress(final InetAddress address) {

      this.address = address;
   }


   /**
    * Sets the value of field 'buffer'.
    *
    * @param buffer the value of field 'buffer'.
    */
   public void setBuffer(final int buffer) {

      this.buffer = buffer;
   }


   /**
    * Sets the TCP port. Cacheonix uses the TCP port to listen on for network requests. Default is 8777.
    *
    * @param port the TCP port. Cacheonix uses the TCP port to listen on for network requests. Default is 8777.
    */
   public void setPort(final int port) {

      this.port = port;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("address".equals(attributeName)) {

         final InetAddress inetAddress = StringUtils.readInetAddress(attributeValue);
         if (inetAddress.isMulticastAddress()) {

            throw new ConfigurationException("Error reading TCP Listener, not a unicast address: " + attributeValue);
         }

         address = inetAddress;

      } else if ("port".equals(attributeName)) {

         port = systemOrAttribute(SystemProperty.CACHEONIX_TCP_LISTENER_PORT, attributeValue);
      } else if ("buffer".equals(attributeName)) {

         buffer = StringUtils.readBytes(attributeValue);
      }
   }


   public String toString() {

      return "TCPListenerConfiguration{" +
              "port=" + port +
              ", address='" + address + '\'' +
              ", buffer='" + buffer + '\'' +
              '}';
   }
}
