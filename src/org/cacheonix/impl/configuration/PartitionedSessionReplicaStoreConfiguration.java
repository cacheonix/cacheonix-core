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
package org.cacheonix.impl.configuration;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * A configuration for a partitioned session store.
 */
public final class PartitionedSessionReplicaStoreConfiguration extends DocumentReader {

   /**
    * A list of optional properties.
    */
   private final List<PropertyConfiguration> propertyList;

   /**
    * The mandatory size of the store in bytes. If the size of the elements of elements in cache exceeds maxBytes,
    * Cacheonix will throw an out-of-memory exception. Example: maxBytes="5mb". Important: Cacheonix recommends setting
    * 'maxBytes' so that total summa of sizes of all session stores and caches doesn't exceed 40% of JVM heap.
    */
   private long maxBytes;

   /**
    * A number of backup copies to be made for each session attribute.
    */
   private int replicaCount;


   /**
    * Creates a new instance of PartitionedSessionReplicaStoreConfiguration.
    */
   public PartitionedSessionReplicaStoreConfiguration() {

      propertyList = new ArrayList<PropertyConfiguration>(1);
   }


   /**
    * Returns the mandatory size of the store in bytes.
    *
    * @return a mandatory size of the store in bytes. If the size of the elements of elements in cache exceeds maxBytes,
    *         Cacheonix will throw an out-of-memory exception. Example: maxBytes="5mb". Important: Cacheonix recommends
    *         setting 'maxBytes' so that total summa of sizes of all session stores and caches doesn't exceed 40% of JVM
    *         heap.
    */
   public long getMaxBytes() {

      return maxBytes;
   }


   /**
    * Returns the  number of backup copies to be made for each session attribute.
    *
    * @return the  number of backup copies to be made for each session attribute.
    */
   public int getReplicaCount() {

      return replicaCount;
   }


   /**
    * {@inheritDoc}
    */
   protected void readNode(final String nodeName, final Node childNode) {

      if ("property".equals(nodeName)) {

         final PropertyConfiguration property = new PropertyConfiguration();
         property.read(childNode);
         propertyList.add(property);
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("replicaCount".equals(attributeName)) {

         replicaCount = Integer.parseInt(attributeValue);
      } else if ("maxBytes".equals(attributeName)) {

         maxBytes = StringUtils.readBytes(attributeValue);
      }
   }


   public String toString() {

      return "PartitionedSessionReplicaStoreConfiguration{" +
              "propertyList=" + propertyList +
              ", replicaCount=" + replicaCount +
              "} " + super.toString();
   }
}
