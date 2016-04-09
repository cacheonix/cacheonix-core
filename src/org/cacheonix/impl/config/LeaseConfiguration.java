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

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class LeaseConfiguration.
 */
public final class LeaseConfiguration extends DocumentReader {

   /**
    * A parent node.
    */
   private CoherenceConfiguration coherenceConfiguration;

   /**
    * Field leaseTime.
    */
   private long leaseTimeMillis = StringUtils.readTime("5ms");

   /**
    * True if the lease time was read.
    */
   private boolean hasLeaseTime;


   /**
    * Sets the parent.
    *
    * @param coherenceConfiguration the parent.
    */
   public void setCoherenceConfiguration(final CoherenceConfiguration coherenceConfiguration) {

      this.coherenceConfiguration = coherenceConfiguration;
   }


   /**
    * Returns the value of field 'leaseTime'.
    *
    * @return the value of field 'LeaseTime'.
    */
   public long getLeaseTimeMillis() {

      return this.leaseTimeMillis;
   }


   /**
    * Sets the value of field 'leaseTime'.
    *
    * @param leaseTimeMillis the value of field 'leaseTime'.
    */
   public void setLeaseTimeMillis(final long leaseTimeMillis) {

      this.leaseTimeMillis = leaseTimeMillis;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("leaseTime".equals(attributeName)) {

         leaseTimeMillis = StringUtils.readTime(attributeValue);
         hasLeaseTime = true;
      }
   }


   protected void postProcessRead() {

      super.postProcessRead();

      // Set lease time
      if (!hasLeaseTime) {

         loadDefaultLeaseTime();
      }
   }


   private void loadDefaultLeaseTime() {

      final PartitionedCacheStoreConfiguration partitionedCacheStoreConfiguration = coherenceConfiguration.getPartitionedCacheStoreConfiguration();
      final PartitionedCacheConfiguration partitionedCacheConfiguration = partitionedCacheStoreConfiguration.getPartitionedCacheConfiguration();
      final ServerConfiguration serverConfiguration = partitionedCacheConfiguration.getServerConfiguration();
      leaseTimeMillis = serverConfiguration.getDefaultLeaseTimeMillis();
      hasLeaseTime = true;
   }


   public void setUpDefaults() {

      loadDefaultLeaseTime();
   }


   public String toString() {

      return "LeaseConfiguration{" +
              "leaseTime='" + leaseTimeMillis + '\'' +
              '}';
   }
}
