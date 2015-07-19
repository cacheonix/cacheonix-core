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
package org.cacheonix.impl.configuration;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class KnownAddressBroadcastConfiguration.
 */
public final class KnownAddressBroadcastConfiguration extends DocumentReader {

   /**
    * If true, the server may only talk to local addresses.
    */
   private boolean limitedToLocalAddresses = false;

   private TCPListenerConfiguration addressConfiguration;


   /**
    * Sets the flag indicating that the server may only talk to local addresses.
    */
   public void limitToLocalAddresses() {

      this.limitedToLocalAddresses = true;
   }


   /**
    * Returns true if the server may only to talk to local addresses.
    *
    * @return true if the server may only to talk to local addresses.
    */
   public boolean isLimitedToLocalAddresses() {

      return limitedToLocalAddresses;
   }


   public TCPListenerConfiguration getAddressConfiguration() {

      return addressConfiguration;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("tcp".equals(nodeName)) {

         addressConfiguration = new TCPListenerConfiguration();
         addressConfiguration.read(childNode);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      // This element doesn't have attributes yet
   }


   public String toString() {

      return "KnownAddressBroadcastConfiguration{" +
              "limitedToLocalAddresses=" + limitedToLocalAddresses +
              ", addressConfiguration=" + addressConfiguration +
              "} " + super.toString();
   }
}
