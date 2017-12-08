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
 * Class CoherenceConfiguration.
 */
public final class CoherenceConfiguration extends DocumentReader {

   /**
    * A parent.
    */
   private PartitionedCacheStoreConfiguration partitionedCacheStoreConfiguration = null;

   /**
    * Field lease.
    */
   private LeaseConfiguration lease = null;


   /**
    * Sets the parent.
    *
    * @param configuration the parent.
    */
   public void setPartitionedCacheStoreConfiguration(final PartitionedCacheStoreConfiguration configuration) {

      this.partitionedCacheStoreConfiguration = configuration;
   }


   /**
    * Returns the parent.
    *
    * @return the parent.
    */
   public PartitionedCacheStoreConfiguration getPartitionedCacheStoreConfiguration() {

      return partitionedCacheStoreConfiguration;
   }


   /**
    * Returns the value of field 'lease'.
    *
    * @return the value of field 'Lease'.
    */
   public LeaseConfiguration getLease() {

      return this.lease;
   }


   /**
    * Sets the value of field 'lease'.
    *
    * @param lease the value of field 'lease'.
    */
   public void setLease(final LeaseConfiguration lease) {

      this.lease = lease;
   }


   public void setUpDefaults() {

      // Create lease configuration
      setUpDefaultLease();
   }


   private void setUpDefaultLease() {

      lease = new LeaseConfiguration();
      lease.setCoherenceConfiguration(this);
      lease.setUpDefaults();
   }


   protected void readNode(final String nodeName, final Node node) {

      if ("lease".equals(nodeName)) {

         lease = new LeaseConfiguration();
         lease.setCoherenceConfiguration(this);
         lease.read(node);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      // This element doesn't have attributes yet
   }


   protected void postProcessRead() {

      if (lease == null) {

         setUpDefaultLease();
      }
   }


   public String toString() {

      return "CoherenceConfiguration{" +
              "lease=" + lease +
              '}';
   }
}
