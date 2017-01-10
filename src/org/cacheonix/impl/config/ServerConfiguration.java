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

import static org.cacheonix.impl.config.ConfigurationConstants.DEFAULT_LEASE_TIME_MILLIS;
import static org.cacheonix.impl.config.ConfigurationConstants.DEFAULT_LOCK_TIMEOUT_MILLIS;
import static org.cacheonix.impl.config.ConfigurationConstants.DEFAULT_SELECTOR_TIMEOUT_MILLIS;
import static org.cacheonix.impl.config.ConfigurationConstants.DEFAULT_SO_TIMEOUT_MILLIS;
import static org.cacheonix.impl.config.SystemProperty.CACHEONIX_DEFAULT_LEASE_TIME_MILLIS;
import static org.cacheonix.impl.config.SystemProperty.CACHEONIX_DEFAULT_LOCK_TIMEOUT_MILLIS;
import static org.cacheonix.impl.config.SystemProperty.CACHEONIX_DEFAULT_UNLOCK_TIMEOUT_MILLIS;
import static org.cacheonix.impl.config.SystemProperty.CACHEONIX_GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS;
import static org.cacheonix.impl.config.SystemProperty.CACHEONIX_SELECTOR_TIMEOUT_MILLIS;
import static org.cacheonix.impl.config.SystemProperty.CACHEONIX_SOCKET_TIMEOUT_MILLIS;
import static org.cacheonix.impl.util.StringUtils.readTime;

/**
 * Class ServerConfiguration.
 */
public final class ServerConfiguration extends DocumentReader {


   /**
    * The parent.
    */
   private CacheonixConfiguration cacheonixConfiguration;

   /**
    * Field machineName.
    */
   private String machineName;

   /**
    * Field rackName.
    */
   private String rackName;

   /**
    * Field territoryName.
    */
   private String territoryName;

   /**
    * Field countryName.
    */
   private String countryName;

   /**
    * Field continentName.
    */
   private String continentName;

   /**
    * Default lock timeout in milliseconds. Cacheonix uses the default lock timeout for a lock that was acquired without
    * setting the timeout explicitly.  This value can be overridden by the system property
    * <code>cacheonix.default.lock.timeout</code>.
    */
   private long defaultLockTimeoutMillis;


   /**
    * The time between issuing a request to shutdown the server gracefully and time a forced shutdown is performed.
    * Partition contributors use this timeout to move their to other nodes to try to avoid loss of data or recovery
    * overhead that may result from the forced shutdown.
    */
   private long gracefulShutdownTimeoutMillis;

   /**
    * Default unlock timeout in milliseconds.
    */
   private long defaultUnlockTimeoutMillis;


   /**
    * Default lease time. Cacheonix uses the default lease time for lease configurations that don't define lease time.
    * This value can be overridden from the command line by setting the system property
    * <code>cacheonix.default.lease.time</code>.
    */
   private long defaultLeaseTimeMillis;

   /**
    * Time the NIO selector should block for while waiting for a channel to become ready, must be greater than zero.
    * Majority of Cacheonix configuration should leave 'selectorTimeout' default.
    */
   private long selectorTimeoutMillis;

   private boolean hasSelectorTimeoutMillis = false;


   /**
    * Field propertyList.
    */
   private final List<PropertyConfiguration> propertyList;

   /**
    * Field cluster.
    */
   private ClusterConfiguration cluster;

   /**
    * Field listener.
    */
   private ListenerConfiguration listener;

   /**
    * Field broadcast.
    */
   private BroadcastConfiguration broadcastConfiguration;

   /**
    * Field partitionedCacheList.
    */
   private final List<PartitionedCacheConfiguration> partitionedCacheList;

   /**
    * Field partitionedCacheList.
    */
   private final List<WebSessionReplicaConfiguration> webSessionReplicaList;

   /**
    * A flag indicating that defaultLeaseTimeMillis was read.
    */
   private boolean hasDefaultLeaseTimeMillis = false;

   private boolean hasDefaultLockTimeoutMillis = false;

   private boolean hasDefaultUnlockTimeoutMillis = false;

   /**
    * SO_TIMEOUT.
    */
   private long socketTimeoutMillis;

   private boolean hasSocketTimeoutMillis = false;


   @SuppressWarnings("WeakerAccess")
   public ServerConfiguration() {

      this.propertyList = new ArrayList<PropertyConfiguration>(1);
      this.partitionedCacheList = new ArrayList<PartitionedCacheConfiguration>(1);
      this.webSessionReplicaList = new ArrayList<WebSessionReplicaConfiguration>(1);
   }


   /**
    * Method enumeratePartitionedCache.
    *
    * @return an Enumeration over all org.cacheonix.impl.configuration.PartitionedCacheConfiguration elements
    */
   public List<? extends PartitionedCacheConfiguration> enumeratePartitionedCaches() {

      return new ArrayList<PartitionedCacheConfiguration>(this.partitionedCacheList);
   }


   /**
    * Returns the value of field 'broadcast'.
    *
    * @return the value of field 'Broadcast'.
    */
   public BroadcastConfiguration getBroadcastConfiguration() {

      return this.broadcastConfiguration;
   }


   /**
    * Returns the value of field 'cluster'.
    *
    * @return the value of field 'Cluster'.
    */
   public ClusterConfiguration getClusterConfiguration() {

      return this.cluster;
   }


   /**
    * Returns the value of field 'listener'.
    *
    * @return the value of field 'Listener'.
    */
   public ListenerConfiguration getListener() {

      return this.listener;
   }


   /**
    * Returns the default unlock timeout in milliseconds.
    *
    * @return the default unlock timeout in milliseconds.
    */
   public long getDefaultUnlockTimeoutMillis() {

      return defaultUnlockTimeoutMillis;
   }


   /**
    * Returns the default lock timeout in milliseconds. Cacheonix uses the default lock timeout for a lock that was
    * acquired without setting the timeout explicitly.  This value can be overridden by the system property
    * <code>cacheonix.default.lock.timeout</code>.
    *
    * @return the default lock timeout in milliseconds. Cacheonix uses the default lock timeout for a lock that was
    *         acquired without setting the timeout explicitly.  This value can be overridden by the system property
    *         <code>cacheonix.default.lock.timeout</code>.
    */
   public long getDefaultLockTimeoutMillis() {

      return defaultLockTimeoutMillis;
   }


   /**
    * Returns the default lease time.
    *
    * @return the default lease time. Cacheonix uses the default lease time for lease configurations that don't define
    *         lease time. This value can be overridden from the command line by setting the system property
    *         <code>cacheonix.default.lease.time</code>.
    */
   public long getDefaultLeaseTimeMillis() {

      return defaultLeaseTimeMillis;
   }


   /**
    * Returns the time between issuing a request to shutdown the server gracefully and time a forced shutdown is
    * performed. Partition contributors use this timeout to move their to other nodes to try to avoid loss of data or
    * recovery overhead that may result from the forced shutdown.
    *
    * @return The time between issuing a request to shutdown the server gracefully and time a forced shutdown is
    *         performed. Partition contributors use this timeout to move their to other nodes to try to avoid loss of
    *         data or recovery overhead that may result from the forced shutdown.
    */
   public long getGracefulShutdownTimeoutMillis() {

      return gracefulShutdownTimeoutMillis;
   }


   /**
    * Returns the socket timeout.
    *
    * @return the socket timeout.
    */
   public long getSocketTimeoutMillis() {

      return socketTimeoutMillis;
   }


   /**
    * Sets the socket timeout.
    *
    * @param socketTimeoutMillis the socket timeout to set.
    */
   public void setSocketTimeoutMillis(final long socketTimeoutMillis) {

      this.socketTimeoutMillis = socketTimeoutMillis;
      this.hasSocketTimeoutMillis = true;
   }


   /**
    * Returns the NIO selector should block for while waiting for a channel to become ready, must be greater than zero.
    *
    * @return NIO selector should block for while waiting for a channel to become ready, must be greater than zero.
    */
   public long getSelectorTimeoutMillis() {

      return selectorTimeoutMillis;
   }


   /**
    * Sets the NIO selector should block for while waiting for a channel to become ready, must be greater than zero.
    * Majority of Cacheonix configuration should leave 'selectorTimeout' default.
    *
    * @param selectorTimeoutMillis the NIO selector should block for while waiting for a channel to become ready, must
    *                              be greater than zero. Majority of Cacheonix configuration should leave
    *                              'selectorTimeout' default.
    */
   public void setSelectorTimeoutMillis(final long selectorTimeoutMillis) {

      this.selectorTimeoutMillis = selectorTimeoutMillis;
      this.hasSelectorTimeoutMillis = true;
   }


   /**
    * Returns the parent of this element.
    *
    * @return the parent of this element.
    */
   public CacheonixConfiguration getCacheonixConfiguration() {

      return cacheonixConfiguration;
   }


   /**
    * Sets the parent of this element.
    *
    * @param cacheonixConfiguration the parent of this element.
    */
   public void setCacheonixConfiguration(final CacheonixConfiguration cacheonixConfiguration) {

      this.cacheonixConfiguration = cacheonixConfiguration;
   }


   /**
    * Returns a copy of the list of partitioned cache configurations.
    *
    * @return the copy of the list of partitioned cache configurations.
    */
   public List<PartitionedCacheConfiguration> getPartitionedCacheList() {

      return new ArrayList<PartitionedCacheConfiguration>(partitionedCacheList);
   }


   public List<PartitionedCacheConfiguration> getNormalPartitionedCacheTypes() {

      final List<PartitionedCacheConfiguration> result = new ArrayList<PartitionedCacheConfiguration>(
              partitionedCacheList.size());
      for (final PartitionedCacheConfiguration partitionedCacheConfiguration : partitionedCacheList) {

         if (!partitionedCacheConfiguration.isTemplate() && !partitionedCacheConfiguration.getName().endsWith("*")) {

            result.add(partitionedCacheConfiguration);
         }
      }

      return result;
   }


   public PartitionedCacheConfiguration getCreateAllTemplate() {

      for (final PartitionedCacheConfiguration partitionedCacheConfiguration : partitionedCacheList) {

         if ("*".equals(partitionedCacheConfiguration.getName())) {

            return partitionedCacheConfiguration;
         }
      }

      return null;
   }


   public PartitionedCacheConfiguration getPartitionedCacheType(final String cacheConfigName) {

      for (final PartitionedCacheConfiguration partitionedCacheConfiguration : partitionedCacheList) {

         if (partitionedCacheConfiguration.getName().equals(cacheConfigName)) {

            return partitionedCacheConfiguration;
         }
      }

      return null;
   }


   /**
    * Returns a list of configuration of session replicas.
    *
    * @return the list of configuration of session replicas. The list can be empty if no configurations are provided.
    */
   public List<WebSessionReplicaConfiguration> getWebSessionReplicaList() {

      return webSessionReplicaList;
   }


   protected void readNode(final String nodeName, final Node node) {

      if ("property".equals(nodeName)) {

         final PropertyConfiguration property = new PropertyConfiguration();
         property.read(node);
         propertyList.add(property);
      } else if ("cluster".equals(nodeName)) {

         cluster = new ClusterConfiguration();
         cluster.read(node);
      } else if ("listener".equals(nodeName)) {

         listener = new ListenerConfiguration();
         listener.read(node);
      } else if ("broadcast".equals(nodeName)) {

         broadcastConfiguration = new BroadcastConfiguration();
         broadcastConfiguration.read(node);
      } else if ("partitionedCache".equals(nodeName)) {

         final PartitionedCacheConfiguration partitionedCacheConfiguration = new PartitionedCacheConfiguration();
         partitionedCacheConfiguration.setServerConfiguration(this);
         partitionedCacheConfiguration.read(node);
         partitionedCacheList.add(partitionedCacheConfiguration);
      } else if ("webSessionReplica".equals(nodeName)) {

         final WebSessionReplicaConfiguration webSessionReplicaConfiguration = new WebSessionReplicaConfiguration();
         webSessionReplicaConfiguration.read(node);
         webSessionReplicaList.add(webSessionReplicaConfiguration);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("machineName".equals(attributeName)) {

         machineName = systemOrAttribute(SystemProperty.CACHEONIX_MACHINE_NAME, attributeValue);
      } else if ("rackName".equals(attributeName)) {

         rackName = systemOrAttribute(SystemProperty.CACHEONIX_RACK_NAME, attributeValue);
      } else if ("territoryName".equals(attributeName)) {

         territoryName = attributeValue;
      } else if ("countryName".equals(attributeName)) {

         countryName = attributeValue;
      } else if ("continentName".equals(attributeName)) {

         continentName = attributeValue;
      } else if ("defaultLockTimeout".equals(attributeName)) {

         defaultLockTimeoutMillis = readTime(attributeValue);
         hasDefaultLockTimeoutMillis = true;
      } else if ("defaultUnlockTimeout".equals(attributeName)) {

         defaultUnlockTimeoutMillis = readTime(attributeValue);
         hasDefaultUnlockTimeoutMillis = true;
      } else if ("gracefulShutdownTimeout".equals(attributeName)) {

         gracefulShutdownTimeoutMillis = systemOrAttribute(CACHEONIX_GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS,
                 readTime(attributeValue));

      } else if ("defaultLeaseTime".equals(attributeName)) {

         defaultLeaseTimeMillis = readTime(attributeValue);
         hasDefaultLeaseTimeMillis = true;
      } else if ("socketTimeout".equals(attributeName)) {

         setSocketTimeoutMillis(systemOrAttribute(CACHEONIX_SOCKET_TIMEOUT_MILLIS, readTime(attributeValue)));
      } else if ("selectorTimeout".equals(attributeName)) {

         setSelectorTimeoutMillis(systemOrAttribute(CACHEONIX_SELECTOR_TIMEOUT_MILLIS, readTime(attributeValue)));
      }
   }


   private static long systemOrAttribute(final Long systemValue, final long attributeValue) {

      if (systemValue != null) {
         return systemValue;
      } else {
         return attributeValue;
      }
   }


   protected void postProcessRead() {

      super.postProcessRead();


      // Override the default lease time

      if (CACHEONIX_DEFAULT_LEASE_TIME_MILLIS != null) {

         defaultLeaseTimeMillis = CACHEONIX_DEFAULT_LEASE_TIME_MILLIS;
         hasDefaultLeaseTimeMillis = true;
      }

      if (!hasDefaultLeaseTimeMillis) {

         defaultLeaseTimeMillis = DEFAULT_LEASE_TIME_MILLIS;
         hasDefaultLeaseTimeMillis = true;
      }

      // Override the default lock timeout

      if (CACHEONIX_DEFAULT_LOCK_TIMEOUT_MILLIS != null) {

         defaultLockTimeoutMillis = CACHEONIX_DEFAULT_LOCK_TIMEOUT_MILLIS;
         hasDefaultLockTimeoutMillis = true;
      }

      if (!hasDefaultLockTimeoutMillis) {

         defaultLockTimeoutMillis = DEFAULT_LOCK_TIMEOUT_MILLIS;
         hasDefaultLockTimeoutMillis = true;
      }

      // Override the default unlock timeout

      if (CACHEONIX_DEFAULT_UNLOCK_TIMEOUT_MILLIS != null) {

         defaultUnlockTimeoutMillis = CACHEONIX_DEFAULT_UNLOCK_TIMEOUT_MILLIS;
         hasDefaultUnlockTimeoutMillis = true;
      }

      if (!hasDefaultUnlockTimeoutMillis) {

         defaultUnlockTimeoutMillis = DEFAULT_LOCK_TIMEOUT_MILLIS;
         hasDefaultUnlockTimeoutMillis = true;
      }

      if (!hasSocketTimeoutMillis) {

         setSocketTimeoutMillis(DEFAULT_SO_TIMEOUT_MILLIS);
      }

      if (!hasSelectorTimeoutMillis) {

         setSelectorTimeoutMillis(DEFAULT_SELECTOR_TIMEOUT_MILLIS);
      }

      if (cluster == null) {
         cluster = new ClusterConfiguration();
         cluster.setUpDefaults();
      }
   }


   public String toString() {

      return "ServerConfiguration{" +
              "machineName='" + machineName + '\'' +
              ", rackName='" + rackName + '\'' +
              ", territoryName='" + territoryName + '\'' +
              ", countryName='" + countryName + '\'' +
              ", continentName='" + continentName + '\'' +
              ", defaultLockTimeoutMillis=" + defaultLockTimeoutMillis +
              ", gracefulShutdownTimeoutMillis=" + gracefulShutdownTimeoutMillis +
              ", defaultUnlockTimeoutMillis=" + defaultUnlockTimeoutMillis +
              ", defaultLeaseTimeMillis=" + defaultLeaseTimeMillis +
              ", selectorTimeoutMillis=" + selectorTimeoutMillis +
              ", hasSelectorTimeoutMillis=" + hasSelectorTimeoutMillis +
              ", propertyList=" + propertyList +
              ", cluster=" + cluster +
              ", listener=" + listener +
              ", broadcast=" + broadcastConfiguration +
              ", partitionedCacheList=" + partitionedCacheList +
              ", webSessionReplicaList=" + webSessionReplicaList +
              ", hasDefaultLeaseTimeMillis=" + hasDefaultLeaseTimeMillis +
              ", hasDefaultLockTimeoutMillis=" + hasDefaultLockTimeoutMillis +
              ", hasDefaultUnlockTimeoutMillis=" + hasDefaultUnlockTimeoutMillis +
              ", socketTimeoutMillis=" + socketTimeoutMillis +
              ", hasSocketTimeoutMillis=" + hasSocketTimeoutMillis +
              "} ";
   }
}
