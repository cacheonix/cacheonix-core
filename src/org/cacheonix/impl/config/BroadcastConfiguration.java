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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class BroadcastConfiguration.
 */
public final class BroadcastConfiguration extends DocumentReader {


   /**
    * Field multicast.
    */
   private MulticastBroadcastConfiguration multicast = null;

   /**
    * Field knownAddresses.
    */
   private List<KnownAddressBroadcastConfiguration> knownAddresses = null;


   /**
    * Returns the value of field 'knownAddress'.
    *
    * @return the value of field 'KnownAddress'.
    */
   public List<KnownAddressBroadcastConfiguration> getKnownAddresses() {

      return new ArrayList<KnownAddressBroadcastConfiguration>(knownAddresses);
   }


   /**
    * Returns the value of field 'multicast'.
    *
    * @return the value of field 'MulticastBroadcastConfiguration'.
    */
   public MulticastBroadcastConfiguration getMulticast() {

      return this.multicast;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("multicast".equals(nodeName)) {

         multicast = new MulticastBroadcastConfiguration();
         multicast.read(childNode);
      } else if ("knownAddress".equals(nodeName)) {

         // Initialize the list of known addresses
         if (knownAddresses == null) {
            knownAddresses = new ArrayList<KnownAddressBroadcastConfiguration>(3);
         }

         // Read the known address
         final KnownAddressBroadcastConfiguration knownAddress = new KnownAddressBroadcastConfiguration();
         knownAddress.read(childNode);
         knownAddresses.add(knownAddress);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      // This element doesn't have attributes yet
   }


   @Override
   void postProcessRead() {

   }


   public String toString() {

      return "BroadcastConfiguration{" +
              "multicast=" + multicast +
              ", knownAddresses=" + knownAddresses +
              "} ";
   }
}
