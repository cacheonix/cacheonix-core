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

import org.w3c.dom.Node;

/**
 * Class PartitionedCacheStore.
 */
@SuppressWarnings("WeakerAccess")
public final class PartitionedCacheStoreConfiguration extends CacheStoreConfiguration {

   /**
    * Field replication.
    */
   private ReplicationConfiguration replication;

   /**
    * Field coherence.
    */
   private CoherenceConfiguration coherence;

   /**
    * The parent node.
    */
   private PartitionedCacheConfiguration partitionedCacheConfiguration;


   /**
    * Sets the parent node.
    *
    * @param partitionedCacheConfiguration the parent node.
    */
   public void setPartitionedCacheConfiguration(final PartitionedCacheConfiguration partitionedCacheConfiguration) {

      this.partitionedCacheConfiguration = partitionedCacheConfiguration;
   }


   /**
    * Returns the parent node.
    *
    * @return the parent node.
    */
   public PartitionedCacheConfiguration getPartitionedCacheConfiguration() {

      return partitionedCacheConfiguration;
   }


   /**
    * Returns the value of field 'coherence'.
    *
    * @return the value of field 'Coherence'.
    */
   public CoherenceConfiguration getCoherence() {

      return this.coherence;
   }


   /**
    * Returns the value of field 'replication'.
    *
    * @return the value of field 'ReplicationConfiguration'.
    */
   public ReplicationConfiguration getReplication() {

      return this.replication;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("replication".equals(nodeName)) {

         replication = new ReplicationConfiguration();
         replication.read(childNode);
      } else if ("coherence".equals(nodeName)) {

         coherence = new CoherenceConfiguration();
         coherence.setPartitionedCacheStoreConfiguration(this);
         coherence.read(childNode);
      } else {

         super.readNode(nodeName, childNode);
      }
   }


   protected void postProcessRead() {

      super.postProcessRead();

      // Set replication to no replication
      if (replication == null) {

         setUpDefaultReplication();
      }

      // Set coherence to leases if coherence is not configured
      if (coherence == null) {

         // Create coherence configuration and set it to lease
         setUpDefaultCoherence();
      }
   }


   private void setUpDefaultReplication() {

      replication = new ReplicationConfiguration();
      replication.setUpDefaults();
   }


   private void setUpDefaultCoherence() {

      coherence = new CoherenceConfiguration();
      coherence.setPartitionedCacheStoreConfiguration(this);
      coherence.setUpDefaults();
   }


   public String toString() {

      return "PartitionedCacheStoreConfiguration{" +
              "replication=" + replication +
              ", coherence=" + coherence +
              "} " + super.toString();
   }
}
