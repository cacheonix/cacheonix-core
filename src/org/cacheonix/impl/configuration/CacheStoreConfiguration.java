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

import org.cacheonix.cache.loader.CacheLoader;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class CacheStoreConfiguration.
 */
public class CacheStoreConfiguration extends DocumentReader {


   /**
    * Field lru.
    */
   private LRUConfiguration lru;

   /**
    * Field fixed.
    */
   private FixedSizeConfiguration fixed;


   /**
    * Field fixed.
    */
   private OverflowToDiskConfiguration overflowToDiskConfiguration;


   /**
    * Field expiration.
    */
   private ExpirationConfiguration expiration;

   /**
    * Field dataSource.
    */
   private DataSourceConfiguration dataSource;

   /**
    * Field dataStore.
    */
   private DataStoreConfiguration dataStore;

   /**
    * Field invalidator.
    */
   private InvalidatorConfiguration invalidator;

   /**
    * An optional configuration for a loader of cache data used to populate Cacheonix at startup.
    */
   private LoaderConfiguration loader;


   /**
    * Returns the value of field 'fixed'.
    *
    * @return the value of field 'fixed'.
    */
   public FixedSizeConfiguration getFixed() {

      return this.fixed;
   }


   /**
    * Returns the value of field 'lru'.
    *
    * @return the value of field 'Lru'.
    */
   public LRUConfiguration getLru() {

      return this.lru;
   }


   /**
    * Sets the value of field 'fixed'.
    *
    * @param fixed the value of field 'fixed'.
    */
   public void setFixed(final FixedSizeConfiguration fixed) {

      this.fixed = fixed;
   }


   /**
    * Sets the value of field 'lru'.
    *
    * @param lru the value of field 'lru'.
    */
   public void setLru(final LRUConfiguration lru) {

      this.lru = lru;
   }


   public OverflowToDiskConfiguration getOverflowToDiskConfiguration() {

      return overflowToDiskConfiguration;
   }


   public void setOverflowToDiskConfiguration(final OverflowToDiskConfiguration overflowToDiskConfiguration) {

      this.overflowToDiskConfiguration = overflowToDiskConfiguration;
   }


   /**
    * Returns the value of field 'dataSource'.
    *
    * @return the value of field 'dataSource'.
    */
   public DataSourceConfiguration getDataSource() {

      return this.dataSource;
   }


   /**
    * Returns the value of field 'dataStore'.
    *
    * @return the value of field 'dataStore'.
    */
   public DataStoreConfiguration getDataStore() {

      return this.dataStore;
   }


   /**
    * Returns the value of field 'invalidator'.
    *
    * @return the value of field 'invalidator'.
    */
   public InvalidatorConfiguration getInvalidator() {

      return this.invalidator;
   }


   /**
    * Returns the value of field 'expiration'.
    *
    * @return the value of field 'expiration'.
    */
   public ExpirationConfiguration getExpiration() {

      return this.expiration;
   }


   /**
    * Returns the configuration for a loader of cache data used to populate Cacheonix at startup.
    *
    * @return the configuration for a loader of cache data used to populate Cacheonix at startup.
    * @see CacheLoader
    */
   public LoaderConfiguration getLoader() {

      return this.loader;
   }


   /**
    * Sets the value of field 'dataSource'.
    *
    * @param dataSource the value of field 'dataSource'.
    */
   public void setDataSource(final DataSourceConfiguration dataSource) {

      this.dataSource = dataSource;
   }


   /**
    * Sets the value of field 'dataStore'.
    *
    * @param dataStore the value of field 'dataStore'.
    */
   public void setDataStore(final DataStoreConfiguration dataStore) {

      this.dataStore = dataStore;
   }


   /**
    * Sets the value of field 'invalidator'.
    *
    * @param invalidator the value of field 'invalidator'.
    */
   public void setDataStore(final InvalidatorConfiguration invalidator) {

      this.invalidator = invalidator;
   }


   /**
    * Sets the value of field 'expiration'.
    *
    * @param expiration the value of field 'expiration'.
    */
   public void setExpiration(final ExpirationConfiguration expiration) {

      this.expiration = expiration;
   }


   /**
    * Sets the configuration for a loader of cache data used to populate Cacheonix at startup.
    *
    * @param loader the configuration for a loader of cache data used to populate Cacheonix at startup.
    */
   public void setLoader(final LoaderConfiguration loader) {

      this.loader = loader;
   }


   public boolean isOverflowToDisk() {

      return overflowToDiskConfiguration != null;
   }


   protected void readNode(final String nodeName, final Node node) {

      if ("lru".equals(nodeName)) {

         lru = new LRUConfiguration();
         lru.read(node);
      } else if ("fixed".equals(nodeName)) {

         fixed = new FixedSizeConfiguration();
         fixed.read(node);
      } else if ("overflowToDisk".equals(nodeName)) {

         overflowToDiskConfiguration = new OverflowToDiskConfiguration();
         overflowToDiskConfiguration.read(node);
      } else if ("expiration".equals(nodeName)) {

         expiration = new ExpirationConfiguration();
         expiration.read(node);
      } else if ("dataSource".equals(nodeName)) {

         dataSource = new DataSourceConfiguration();
         dataSource.read(node);
      } else if ("dataStore".equals(nodeName)) {

         dataStore = new DataStoreConfiguration();
         dataStore.read(node);
      } else if ("invalidator".equals(nodeName)) {

         invalidator = new InvalidatorConfiguration();
         invalidator.read(node);
      } else if ("loader".equals(nodeName)) {

         loader = new LoaderConfiguration();
         loader.read(node);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      // This element doesn't have attributes yet
   }


   protected void postProcessRead() {

      super.postProcessRead();

      if (expiration == null) {

         expiration = new ExpirationConfiguration();
         expiration.setUpDefaults();
      }
   }


   public String toString() {

      return "CacheStoreConfiguration{" +
              "lru=" + lru +
              ", fixed=" + fixed +
              ", overflowToDiskConfiguration=" + overflowToDiskConfiguration +
              ", expiration=" + expiration +
              ", dataSource=" + dataSource +
              ", dataStore=" + dataStore +
              ", invalidator=" + invalidator +
              ", loader=" + loader +
              "} " + super.toString();
   }
}
