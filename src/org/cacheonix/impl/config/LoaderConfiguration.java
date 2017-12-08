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

import org.cacheonix.cache.loader.CacheLoader;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * A configuration for a loader of cache data used to populate Cacheonix at startup.
 */
public final class LoaderConfiguration extends DocumentReader {

   /**
    * Field className.
    */
   private String className = null;

   /**
    * Field paramList.
    */
   private final List<PropertyConfiguration> paramList;


   @SuppressWarnings("WeakerAccess")
   public LoaderConfiguration() {

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
    * Returns a copy of the configuration properties for the cache loader.
    *
    * @return copy of the configuration properties for the cache loader.
    * @see CacheLoader
    */
   public List<? extends PropertyConfiguration> getParams() {

      return new ArrayList<PropertyConfiguration>(this.paramList);
   }


   /**
    * Returns a name of the class implementing interface {@link CacheLoader}.
    *
    * @return a name of the class implementing interface {@link CacheLoader}.
    */
   public String getClassName() {

      return this.className;
   }


   /**
    * Method getParam.
    *
    * @param index
    * @return the value of the org.cacheonix.impl.configuration.PropertyConfiguration at the given index
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public PropertyConfiguration getParam(final int index) throws IndexOutOfBoundsException {

      // check bounds for index
      if (index < 0 || index >= this.paramList.size()) {
         throw new IndexOutOfBoundsException("getParam: Index value '" + index + "' not in range [0.." + (this.paramList.size() - 1) + ']');
      }

      return paramList.get(index);
   }


   /**
    * Returns a copy of the configuration properties for the cache loader.
    *
    * @return copy of the configuration properties for the cache loader.
    * @see CacheLoader
    */
   public PropertyConfiguration[] getParam() {

      final PropertyConfiguration[] array = new PropertyConfiguration[0];
      return this.paramList.toArray(array);
   }


   /**
    * Method getParamCount.
    *
    * @return the size of this collection
    */
   public int getParamCount() {

      return this.paramList.size();
   }


   /**
    */
   public void removeAllParam() {

      this.paramList.clear();
   }


   /**
    * Method removeParam.
    *
    * @param vParam
    * @return true if the object was removed from the collection.
    */
   public boolean removeParam(final PropertyConfiguration vParam) {

      return paramList.remove(vParam);
   }


   /**
    * Method removeParamAt.
    *
    * @param index
    * @return the element removed from the collection
    */
   public PropertyConfiguration removeParamAt(final int index) {

      return this.paramList.remove(index);
   }


   /**
    * Sets the name of the class implementing interface {@link CacheLoader}.
    *
    * @param className the name of the class implementing interface {@link CacheLoader}.
    */
   public void setClassName(final String className) {

      this.className = className;
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
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("className".equals(attributeName)) {

         className = attributeValue;
      }
   }


   @Override
   void postProcessRead() {

   }


   public String toString() {

      return "LoaderConfiguration{" +
              "className='" + className + '\'' +
              ", paramList=" + paramList +
              '}';
   }
}
