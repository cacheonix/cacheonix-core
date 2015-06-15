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

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Configuration for the overflow to disk.
 */
public final class OverflowToDiskConfiguration extends DocumentReader {

   /**
    * The mandatory size of the disk storage cache in bytes.
    */
   private long maxOverflowBytes;


   /**
    * Returns the mandatory size of the disk storage cache in bytes.
    *
    * @return the mandatory size of the disk storage cache in bytes.
    */
   public long getMaxOverflowBytes() {

      return this.maxOverflowBytes;
   }


   /**
    * Sets the mandatory size of the disk storage cache in bytes.
    *
    * @param maxOverflowBytes mandatory size of the disk storage cache in bytes.
    */
   public void setMaxOverflowBytes(final long maxOverflowBytes) {

      this.maxOverflowBytes = maxOverflowBytes;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("maxOverflowBytes".equals(attributeName)) {

         maxOverflowBytes = StringUtils.readBytes(attributeValue);
      }
   }


   public String toString() {

      return "OverflowToDiskConfiguration{" +
              "maxOverflowBytes='" + maxOverflowBytes + '\'' +
              '}';
   }
}
