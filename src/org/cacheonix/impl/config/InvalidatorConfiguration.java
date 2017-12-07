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
 * Class InvalidatorConfiguration.
 */
public final class InvalidatorConfiguration extends DocumentReader {

   /**
    * A name of the cache invalidator class.
    */
   private String className = null;

   /**
    * Configuration properties for the cache invalidator.
    */
   private final List<PropertyConfiguration> paramList;


   @SuppressWarnings("WeakerAccess")
   public InvalidatorConfiguration() {

      this.paramList = new ArrayList<PropertyConfiguration>(1);
   }


   /**
    * Returns a copy of the configuration properties for the cache invalidator.
    *
    * @return a copy of the configuration properties for the cache invalidator.
    */
   public List<? extends PropertyConfiguration> getParams() {

      return new ArrayList<PropertyConfiguration>(this.paramList);
   }


   /**
    * Returns the name of the cache invalidator class.
    *
    * @return the name of the cache invalidator class.
    */
   public String getClassName() {

      return this.className;
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


   public String toString() {

      return "InvalidatorConfiguration{" +
              "className='" + className + '\'' +
              ", paramList=" + paramList +
              '}';
   }
}
