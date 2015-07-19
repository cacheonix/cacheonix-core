/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
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
package org.cacheonix.impl.configuration;

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 */
public abstract class DocumentReader {

   /**
    * This element's node.
    *
    * @param node this element's node.
    */
   public final void read(final Node node) {

      // Read attributes
      final NamedNodeMap attributes = node.getAttributes();
      final int attributesLength = attributes.getLength();
      for (int i = 0; i < attributesLength; i++) {

         final Attr item = (Attr) attributes.item(i);
         readAttribute(item.getNodeName(), item, item.getValue());
      }

      // Read child elements
      final NodeList childNodes = node.getChildNodes();
      final int elementsLength = childNodes.getLength();
      for (int i = 0; i < elementsLength; i++) {

         final Node childNode = childNodes.item(i);
         final String nodeName = childNode.getNodeName();
         readNode(nodeName, childNode);
      }

      postProcessRead();
   }


   protected abstract void readNode(String nodeName, Node childNode);

   protected abstract void readAttribute(String attributeName, Attr attributeNode, final String attributeValue);


   /**
    *
    */
   protected void postProcessRead() {

   }


   protected static long systemOrAttribute(final Long systemSettingValue, final String attributeValue) {

      return systemSettingValue == null ? Long.parseLong(attributeValue) : systemSettingValue;
   }


   protected static int systemOrAttribute(final Integer systemSettingValue, final String attributeValue) {

      return systemSettingValue == null ? Integer.parseInt(attributeValue) : systemSettingValue;
   }


   protected static String systemOrAttribute(final String systemSettingValue, final String attributeValue) {

      return StringUtils.isBlank(systemSettingValue) ? attributeValue : systemSettingValue;
   }
}
