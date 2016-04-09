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
 * A configuration for web session replication.
 */
public final class WebSessionReplicaConfiguration extends DocumentReader {

   private static final String CACHEONIX_PREFIX = "cacheonix-";

   /**
    * A unique cache name.
    */
   private String name;


   /**
    * A list of optional properties.
    */
   private final List<PropertyConfiguration> propertyList;


   /**
    * A configuration for a partitioned session store.
    */
   private PartitionedSessionReplicaStoreConfiguration partitionedSessionReplicaStoreConfiguration;


   /**
    * Creates a new instance of WebSessionReplicaConfiguration.
    */
   public WebSessionReplicaConfiguration() {

      this.propertyList = new ArrayList<PropertyConfiguration>(1);
   }


   public PartitionedSessionReplicaStoreConfiguration getPartitionedSessionReplicaStoreConfiguration() {

      return partitionedSessionReplicaStoreConfiguration;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("property".equals(nodeName)) {

         final PropertyConfiguration property = new PropertyConfiguration();
         property.read(childNode);
         propertyList.add(property);
      } else if ("partitionedStore".equals(nodeName)) {

         partitionedSessionReplicaStoreConfiguration = new PartitionedSessionReplicaStoreConfiguration();
         partitionedSessionReplicaStoreConfiguration.read(childNode);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("name".equals(attributeName)) {

         // Check validity
         if (attributeValue.startsWith(CACHEONIX_PREFIX)) {
            throw new IllegalArgumentException("Session replica name cannot start with '" + CACHEONIX_PREFIX + '\'');
         }

         //
         name = attributeValue;
      }
   }


   public String toString() {

      return "WebSessionReplicaConfiguration{" +
              "name='" + name + '\'' +
              ", propertyList=" + propertyList +
              ", partitionedSessionReplicaStoreConfiguration=" + partitionedSessionReplicaStoreConfiguration +
              "} ";
   }
}
