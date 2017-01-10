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

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class ListenerConfiguration.
 */
public final class ListenerConfiguration extends DocumentReader {


   /**
    * Field tcp.
    */
   private TCPListenerConfiguration tcp;


   /**
    * Returns the value of field 'tcp'.
    *
    * @return the value of field 'Tcp'.
    */
   public TCPListenerConfiguration getTcp() {

      return this.tcp;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("tcp".equals(nodeName)) {

         tcp = new TCPListenerConfiguration();
         tcp.read(childNode);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      // This element doesn't have attributes yet
   }


   public String toString() {

      return "ListenerConfiguration{" +
              "tcp=" + tcp +
              '}';
   }
}
