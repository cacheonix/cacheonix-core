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
 * A node of a distributed partitioned cache.
 */
public final class PartitionedCacheConfiguration extends DocumentReader {

   /**
    * Parent server configuration.
    */
   private ServerConfiguration serverConfiguration = null;

   /**
    * A unique cache name.
    */
   private String name = null;

   /**
    * Field template.
    */
   private boolean template = false;

   /**
    * Field propertyList.
    */
   private final List<PropertyConfiguration> propertyList;

   /**
    * Field store.
    */
   private PartitionedCacheStoreConfiguration store = null;

   /**
    * Field frontCache.
    */
   private FrontCacheConfiguration frontCacheConfiguration = null;


   @SuppressWarnings("WeakerAccess")
   public PartitionedCacheConfiguration() {

      this.propertyList = new ArrayList<PropertyConfiguration>(1);
   }


   /**
    * Returns the value of field 'frontCache'.
    *
    * @return the value of field 'FrontCache'.
    */
   public FrontCacheConfiguration getFrontCacheConfiguration() {

      return this.frontCacheConfiguration;
   }


   /**
    * Returns the unique cache name.
    *
    * @return the unique cache name.
    */
   public String getName() {

      return this.name;
   }


   /**
    * Returns the value of field 'store'.
    *
    * @return the value of field 'CacheStoreConfiguration'.
    */
   public PartitionedCacheStoreConfiguration getStore() {

      return this.store;
   }


   /**
    * Returns a flag indicating if this cache configuration is a template configuration. The template cache
    * configuration is used to create a cache using method <code>Cacheonix.createCache(templateName, cacheName)</code>.
    * <p/>
    * To mark a cache configuration as a template set attribute "template" to "true".
    * <p/>
    * Note: The template cache configuration cannot be started.
    * <p/>
    * Example of cacheonix-config.xml:
    * <p/>
    * <pre>
    * &lt;partitionedCache name="my.cache" <b>template="true"</b>&gt;
    *    &lt;store&gt;
    *       &lt;size&gt;
    *          &lt;lru maxBytes="1mb" maxElements="1000"/&gt;
    *       &lt;/size&gt;
    *    &lt;/store&gt;
    * &lt;/partitionedCache&gt;
    * </pre>
    * <p/>
    * Cacheonix reserves cache name "default" for a template cache configuration that is used to create a new cache if
    * automatic cache creation is enabled.
    *
    * @return the flag indicating if this cache configuration is a template.
    */
   public boolean isTemplate() {

      return this.template;
   }


   /**
    * Returns true if this partitioned cache is configured to be a partition contributor. This is done by defining the
    * cache store. The partitioned cache does is not a partition contributor if the store configuration is not provided.
    * In this case the cache member remains a member of the cluster but operates in a client-only mode.
    * <p/>
    * The total size of the partitioned cache is determined as a multiple of the partition size and the number of
    * cluster members that are partition contributors.
    *
    * @return <code>true</code> is this partitioned cache configuration has a store configuration.
    */
   public boolean isPartitionContributor() {

      return store != null;
   }


   public void setServerConfiguration(final ServerConfiguration serverConfiguration) {

      this.serverConfiguration = serverConfiguration;
   }


   public ServerConfiguration getServerConfiguration() {

      return serverConfiguration;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("property".equals(nodeName)) {

         final PropertyConfiguration property = new PropertyConfiguration();
         property.read(childNode);
         propertyList.add(property);
      } else if ("store".equals(nodeName)) {

         store = new PartitionedCacheStoreConfiguration();
         store.setPartitionedCacheConfiguration(this);
         store.read(childNode);
      } else if ("frontCache".equals(nodeName)) {

         frontCacheConfiguration = new FrontCacheConfiguration();
         frontCacheConfiguration.read(childNode);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("name".equals(attributeName)) {

         name = attributeValue;
      } else if ("template".equals(attributeName)) {

         template = Boolean.parseBoolean(attributeValue);
      }
   }


   @Override
   void postProcessRead() {

   }


   public String toString() {

      return "PartitionedCacheConfiguration{" +
              "name='" + name + '\'' +
              ", template=" + template +
              ", propertyList=" + propertyList +
              ", store=" + store +
              ", frontCache=" + frontCacheConfiguration +
              '}';
   }
}
