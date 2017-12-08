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

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * A configuration for datasource's prefetch element indicating that Cacheonix should read cache elements that are about
 * to expire ahead of time, asynchronously.
 */
public final class PrefetchConfiguration extends DocumentReader {

   /**
    * A flag indicating that Cacheonix should read cache elements that are about to expire ahead of time,
    * asynchronously.
    */
   private boolean enabled = false;


   protected void readNode(final String nodeName, final Node childNode) {
      // Do nothing as it doesn't have child nodes.
   }


   public boolean isEnabled() {

      return enabled;
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("enabled".equals(attributeName)) {

         enabled = Boolean.parseBoolean(attributeValue);
      }
   }


   @Override
   void postProcessRead() {

   }
}
