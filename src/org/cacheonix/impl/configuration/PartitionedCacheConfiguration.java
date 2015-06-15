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
import java.util.Arrays;
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
   private ServerConfiguration serverConfiguration;

   /**
    * A unique cache name.
    */
   private String name;

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
   private PartitionedCacheStoreConfiguration store;

   /**
    * Field frontCache.
    */
   private FrontCacheConfiguration frontCacheConfiguration;


   @SuppressWarnings("WeakerAccess")
   public PartitionedCacheConfiguration() {

      this.propertyList = new ArrayList<PropertyConfiguration>(1);
   }


   /**
    * @param vProperty
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void addProperty(final PropertyConfiguration vProperty) throws IndexOutOfBoundsException {

      this.propertyList.add(vProperty);
   }


   /**
    * @param index
    * @param vProperty
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void addProperty(final int index, final PropertyConfiguration vProperty) throws IndexOutOfBoundsException {

      this.propertyList.add(index, vProperty);
   }


   /**
    * Method enumerateProperty.
    *
    * @return an Enumeration over all org.cacheonix.impl.configuration.PropertyConfiguration elements
    */
   public List<? extends PropertyConfiguration> enumerateProperty() {

      return new ArrayList<PropertyConfiguration>(this.propertyList);
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
    * Method getProperty.
    *
    * @param index
    * @return the value of the org.cacheonix.impl.configuration.PropertyConfiguration at the given index
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public PropertyConfiguration getProperty(final int index) throws IndexOutOfBoundsException {

      // check bounds for index
      if (index < 0 || index >= this.propertyList.size()) {
         throw new IndexOutOfBoundsException("getProperty: Index value '" + index + "' not in range [0.." + (this.propertyList.size() - 1) + ']');
      }

      return propertyList.get(index);
   }


   /**
    * Method getProperty.Returns the contents of the collection in an Array.  <p>Note:  Just in case the collection
    * contents are changing in another thread, we pass a 0-length Array of the correct type into the API call.  This way
    * we <i>know</i> that the Array returned is of exactly the correct length.
    *
    * @return this collection as an Array
    */
   public PropertyConfiguration[] getProperty() {

      final PropertyConfiguration[] array = new PropertyConfiguration[0];
      return this.propertyList.toArray(array);
   }


   /**
    * Method getPropertyCount.
    *
    * @return the size of this collection
    */
   public int getPropertyCount() {

      return this.propertyList.size();
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
    */
   public void removeAllProperty() {

      this.propertyList.clear();
   }


   /**
    * Method removeProperty.
    *
    * @param vProperty
    * @return true if the object was removed from the collection.
    */
   public boolean removeProperty(final PropertyConfiguration vProperty) {

      return propertyList.remove(vProperty);
   }


   /**
    * Method removePropertyAt.
    *
    * @param index
    * @return the element removed from the collection
    */
   public PropertyConfiguration removePropertyAt(final int index) {

      return this.propertyList.remove(index);
   }


   /**
    * Sets the value of field 'frontCache'.
    *
    * @param frontCacheConfiguration the value of field 'frontCache'.
    */
   public void setFrontCacheConfiguration(final FrontCacheConfiguration frontCacheConfiguration) {

      this.frontCacheConfiguration = frontCacheConfiguration;
   }


   /**
    * Sets the unique cache name.
    *
    * @param name the unique cache name.
    */
   public void setName(final String name) {

      this.name = name;
   }


   /**
    * @param index
    * @param vProperty
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void setProperty(final int index, final PropertyConfiguration vProperty) throws IndexOutOfBoundsException {

      // check bounds for index
      if (index < 0 || index >= this.propertyList.size()) {
         throw new IndexOutOfBoundsException("setProperty: Index value '" + index + "' not in range [0.." + (this.propertyList.size() - 1) + ']');
      }

      this.propertyList.set(index, vProperty);
   }


   /**
    * @param vPropertyArray
    */
   public void setProperty(final PropertyConfiguration[] vPropertyArray) {

      //-- copy array
      propertyList.clear();

      this.propertyList.addAll(Arrays.asList(vPropertyArray));
   }


   /**
    * Sets the value of field 'store'.
    *
    * @param store the value of field 'store'.
    */
   public void setStore(final PartitionedCacheStoreConfiguration store) {

      this.store = store;
   }


   /**
    * Sets the value of field 'template'.
    *
    * @param template the value of field 'template'.
    */
   public void setTemplate(final boolean template) {

      this.template = template;
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
