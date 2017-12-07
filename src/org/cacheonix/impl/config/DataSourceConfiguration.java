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
 * Class DataSourceConfiguration.
 */
public final class DataSourceConfiguration extends DocumentReader {

   /**
    * A name of the cache data source class.
    */
   private String className = null;


   /**
    * Configuration properties for the cache data source.
    */
   private final List<PropertyConfiguration> paramList;


   /**
    * Prefetch configuration.
    */
   private PrefetchConfiguration prefetchConfiguration = null;


   @SuppressWarnings("WeakerAccess")
   public DataSourceConfiguration() {

      this.paramList = new ArrayList<PropertyConfiguration>(1);
   }


   /**
    * @param vParam
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void addParam(final PropertyConfiguration vParam) throws IndexOutOfBoundsException {

      this.paramList.add(vParam);
   }


   /**
    * @param index
    * @param vParam
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void addParam(final int index, final PropertyConfiguration vParam) throws IndexOutOfBoundsException {

      this.paramList.add(index, vParam);
   }


   /**
    * Returns a copy of the configuration properties for the cache data source.
    *
    * @return a copy of the configuration properties for the cache data source.
    */
   public List<? extends PropertyConfiguration> getParams() {

      return new ArrayList<PropertyConfiguration>(this.paramList);
   }


   /**
    * Returns the name of the cache data source class.
    *
    * @return the name of the cache data source class.
    */
   public String getClassName() {

      return this.className;
   }


   /**
    * Returns a copy of the configuration properties for the cache data source.
    *
    * @return the copy of the configuration properties for the cache data source.
    */
   public PropertyConfiguration[] getParam() {

      final PropertyConfiguration[] array = new PropertyConfiguration[0];
      return this.paramList.toArray(array);
   }


   /**
    * Sets the name of the cache data source class.
    *
    * @param className the name of the cache data source class.
    */
   public void setClassName(final String className) {

      this.className = className;
   }


   public PrefetchConfiguration getPrefetchConfiguration() {

      return prefetchConfiguration;
   }


   public boolean isPrefetchConfigurationSet() {

      return prefetchConfiguration != null;
   }


   /**
    * @param index
    * @param vParam
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void setParam(final int index, final PropertyConfiguration vParam) throws IndexOutOfBoundsException {
      // check bounds for index
      if (index < 0 || index >= this.paramList.size()) {
         throw new IndexOutOfBoundsException("setParam: Index value '" + index + "' not in range [0.." + (this.paramList.size() - 1) + ']');
      }

      this.paramList.set(index, vParam);
   }


   /**
    * @param vParamArray
    */
   public void setParam(final PropertyConfiguration[] vParamArray) {
      //-- copy array
      paramList.clear();

      this.paramList.addAll(Arrays.asList(vParamArray));
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("param".equals(nodeName)) {

         final PropertyConfiguration property = new PropertyConfiguration();
         property.read(childNode);
         paramList.add(property);
      } else if ("prefetch".equals(nodeName)) {

         prefetchConfiguration = new PrefetchConfiguration();
         prefetchConfiguration.read(childNode);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("className".equals(attributeName)) {

         className = attributeValue;
      }
   }


   public String toString() {

      return "DataSourceConfiguration{" +
              "className='" + className + '\'' +
              ", paramList=" + paramList +
              '}';
   }
}
