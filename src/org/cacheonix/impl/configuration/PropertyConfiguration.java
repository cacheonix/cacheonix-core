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

import java.util.List;
import java.util.Properties;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class PropertyConfiguration.
 */
public final class PropertyConfiguration extends DocumentReader {

   /**
    * Field name.
    */
   private String name;

   /**
    * Field value.
    */
   private String value;


   public static Properties toProperties(final List<? extends PropertyConfiguration> params) {

      final Properties result = new Properties();
      for (final PropertyConfiguration propertyConfiguration : params) {

         result.setProperty(propertyConfiguration.name, propertyConfiguration.value);
      }

      return result;
   }


   /**
    * Returns the value of field 'name'.
    *
    * @return the value of field 'Name'.
    */
   public String getName() {

      return this.name;
   }


   /**
    * Returns the value of field 'value'.
    *
    * @return the value of field 'Value'.
    */
   public String getValue() {

      return this.value;
   }


   /**
    * Sets the value of field 'name'.
    *
    * @param name the value of field 'name'.
    */
   public void setName(final String name) {

      this.name = name;
   }


   /**
    * Sets the value of field 'value'.
    *
    * @param value the value of field 'value'.
    */
   public void setValue(final String value) {

      this.value = value;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("name".equals(attributeName)) {

         name = attributeValue;
      } else if ("value".equals(attributeName)) {

         value = attributeValue;
      }
   }


   public String toString() {

      return "PropertyConfiguration{" +
              "name='" + name + '\'' +
              ", value='" + value + '\'' +
              '}';
   }
}
