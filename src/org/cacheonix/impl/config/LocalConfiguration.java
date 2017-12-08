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
 * Class LocalConfiguration.
 */
public final class LocalConfiguration extends DocumentReader {

   /**
    * A parent of this element.
    */
   private CacheonixConfiguration cacheonixConfiguration = null;


   /**
    * Field localCacheList.
    */
   private final List<LocalCacheConfiguration> localCacheList;


   @SuppressWarnings("WeakerAccess")
   public LocalConfiguration() {

      this.localCacheList = new ArrayList<LocalCacheConfiguration>(1);
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
    * Returns a copy of the local cache configurations.
    *
    * @return a copy of the local cache configurations.
    */
   public List<LocalCacheConfiguration> getLocalCacheConfigurationList() {

      return new ArrayList<LocalCacheConfiguration>(localCacheList);
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


   @Override
   void postProcessRead() {

   }


   public String toString() {

      return "LocalConfiguration{" +
              "localCacheList=" + localCacheList +
              '}';
   }
}
