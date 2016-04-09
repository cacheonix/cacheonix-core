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
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class LocalConfiguration.
 */
public final class LocalConfiguration extends DocumentReader {

   /**
    * A parent of this element.
    */
   private CacheonixConfiguration cacheonixConfiguration;


   /**
    * Field localCacheList.
    */
   private final List<LocalCacheConfiguration> localCacheList;


   @SuppressWarnings("WeakerAccess")
   public LocalConfiguration() {

      this.localCacheList = new ArrayList<LocalCacheConfiguration>(1);
   }


   /**
    * @param vLocalCache
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void addLocalCache(final LocalCacheConfiguration vLocalCache) throws IndexOutOfBoundsException {

      this.localCacheList.add(vLocalCache);
   }


   /**
    * @param index
    * @param vLocalCache
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void addLocalCache(final int index,
                             final LocalCacheConfiguration vLocalCache) throws IndexOutOfBoundsException {

      this.localCacheList.add(index, vLocalCache);
   }


   /**
    * Method enumerateLocalCache.
    *
    * @return an Enumeration over all org.cacheonix.impl.configuration.LocalCacheConfiguration elements
    */
   public List<? extends LocalCacheConfiguration> enumerateLocalCache() {

      return new ArrayList<LocalCacheConfiguration>(this.localCacheList);
   }


   /**
    * Method getLocalCache.
    *
    * @param index
    * @return the value of the org.cacheonix.impl.configuration.LocalCacheConfiguration at the given index
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public LocalCacheConfiguration getLocalCache(final int index) throws IndexOutOfBoundsException {

      // check bounds for index
      if (index < 0 || index >= this.localCacheList.size()) {
         throw new IndexOutOfBoundsException("getLocalCache: Index value '" + index + "' not in range [0.." + (this.localCacheList.size() - 1) + ']');
      }

      return localCacheList.get(index);
   }


   /**
    * Method getLocalCache.Returns the contents of the collection in an Array.  <p>Note:  Just in case the collection
    * contents are changing in another thread, we pass a 0-length Array of the correct type into the API call.  This way
    * we <i>know</i> that the Array returned is of exactly the correct length.
    *
    * @return this collection as an Array
    */
   public LocalCacheConfiguration[] getLocalCache() {

      final LocalCacheConfiguration[] array = new LocalCacheConfiguration[0];
      return this.localCacheList.toArray(array);
   }


   /**
    * Method getLocalCacheCount.
    *
    * @return the size of this collection
    */
   public int getLocalCacheCount() {

      return this.localCacheList.size();
   }


   /**
    */
   public void removeAllLocalCache() {

      this.localCacheList.clear();
   }


   /**
    * Method removeLocalCache.
    *
    * @param vLocalCache
    * @return true if the object was removed from the collection.
    */
   public boolean removeLocalCache(final LocalCacheConfiguration vLocalCache) {

      return localCacheList.remove(vLocalCache);
   }


   /**
    * Method removeLocalCacheAt.
    *
    * @param index
    * @return the element removed from the collection
    */
   public LocalCacheConfiguration removeLocalCacheAt(final int index) {

      return this.localCacheList.remove(index);
   }


   /**
    * @param index
    * @param vLocalCache
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void setLocalCache(final int index,
                             final LocalCacheConfiguration vLocalCache) throws IndexOutOfBoundsException {

      // check bounds for index
      if (index < 0 || index >= this.localCacheList.size()) {
         throw new IndexOutOfBoundsException("setLocalCache: Index value '" + index + "' not in range [0.." + (this.localCacheList.size() - 1) + ']');
      }

      this.localCacheList.set(index, vLocalCache);
   }


   /**
    * @param vLocalCacheArray
    */
   public void setLocalCache(final LocalCacheConfiguration[] vLocalCacheArray) {

      //-- copy array
      localCacheList.clear();

      this.localCacheList.addAll(Arrays.asList(vLocalCacheArray));
   }


   /**
    * Returns a copy of the local cache configurations.
    *
    * @return a copy of the local cache configurations.
    */
   public List<LocalCacheConfiguration> getLocalCacheConfigurationList() {

      return new ArrayList<LocalCacheConfiguration>(localCacheList);
   }


   /**
    * Returns <code>true</code> if create-all template is present.
    *
    * @return <code>true</code> if create-all template is present.
    */
   public boolean isCreateAllTemplatePresent() {

      for (final LocalCacheConfiguration localCacheConfig : localCacheList) {

         if (localCacheConfig.isTemplate() && "*".equals(localCacheConfig.getName())) {

            return true;
         }
      }
      return false;
   }


   public void setCacheonixConfiguration(final CacheonixConfiguration cacheonixConfiguration) {

      this.cacheonixConfiguration = cacheonixConfiguration;
   }


   public CacheonixConfiguration getCacheonixConfiguration() {

      return cacheonixConfiguration;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("localCache".equals(nodeName)) {

         final LocalCacheConfiguration localCacheConfiguration = new LocalCacheConfiguration();
         localCacheConfiguration.setLocal(this);
         localCacheConfiguration.read(childNode);
         localCacheList.add(localCacheConfiguration);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      // This element doesn't have attributes yet
   }


   public String toString() {

      return "LocalConfiguration{" +
              "localCacheList=" + localCacheList +
              '}';
   }
}
