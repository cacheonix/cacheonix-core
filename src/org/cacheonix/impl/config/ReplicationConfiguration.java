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
 * Class ReplicationConfiguration.
 */
public final class ReplicationConfiguration extends DocumentReader {

   /**
    * A number of backup copies to be made for each element of this cache.
    */
   private int replicaCount;


   /**
    * Returns the number of backup copies to be made for each element of this cache.
    *
    * @return the number of backup copies to be made for each element of this cache.
    */
   public int getReplicaCount() {

      return this.replicaCount;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("replicaCount".equals(attributeName)) {

         replicaCount = Integer.parseInt(attributeValue);
      }
   }


   /**
    * Sets up default values to attributes and to children.
    */
   public void setUpDefaults() {

      replicaCount = 0;
   }


   public String toString() {

      return "ReplicationConfiguration{" +
              "replicaCount=" + replicaCount +
              '}';
   }
}
