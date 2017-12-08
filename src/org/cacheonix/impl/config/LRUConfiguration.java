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

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Configuration for the Least Recently Used policy for managing cache size. When the cache becomes full, the least
 * recently used item is evicted from the cache.
 */
public final class LRUConfiguration extends DocumentReader {

   /**
    * The mandatory maximum number of bytes stored in the cache. If the number of elements in cache exceeds maxBytes,
    * Cacheonix will evict the least recently used elements.
    */
   private long maxBytes = 0L;

   /**
    * The size of the cache in number of elements. Cacheonix does not limit the number of elements in cache if the
    * maximum number of elements is not set or if it is set to zero.
    */
   private long maxElements = 0L;


   /**
    * Returns the mandatory maximum number of bytes stored in the cache. If the size of elements in cache exceeds
    * maxBytes, Cacheonix will evict the least recently used elements.
    *
    * @return the mandatory maximum number of bytes stored in the cache.
    */
   public long getMaxBytes() {

      return this.maxBytes;
   }


   /**
    * Returns the size of the cache in number of elements. Cacheonix does not limit the number of elements in cache if
    * the maximum number of elements is not set or if it is set to zero.
    *
    * @return the maximum number of elements in cache.
    */
   public long getMaxElements() {

      return this.maxElements;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("maxBytes".equals(attributeName)) {

         maxBytes = StringUtils.readBytes(attributeValue);
      } else if ("maxElements".equals(attributeName)) {

         maxElements = Long.parseLong(attributeValue);
      }
   }


   @Override
   void postProcessRead() {

   }


   public String toString() {

      return "LRUConfiguration{" +
              "maxBytes='" + maxBytes + '\'' +
              ", maxElements=" + maxElements +
              '}';
   }
}
