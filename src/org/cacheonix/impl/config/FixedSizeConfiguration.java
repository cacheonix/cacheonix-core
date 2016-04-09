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
 * Configuration for the fixed-size policy for managing cache size. When the cache becomes full, the cache will throw a
 * runtime exception in response to requests to add more elements. The fixed size policy is used to configure Cacheonix
 * to run in the loss-less data grid mode.
 */
public final class FixedSizeConfiguration extends DocumentReader {

   /**
    * The mandatory size of the cache in bytes. In case of the fixed cache size, Cacheonix will return a error to a
    * request to add an element to the cache if the size of the elements of elements in cache exceeds maxBytes.
    * <p/>
    * Important: Cacheonix recommends setting 'maxBytes' so that total summa of sizes  of all caches doesn't exceed 40%
    * of JVM heap.
    */
   private long maxBytes;


   /**
    * Returns the mandatory size of the cache in bytes. In case of the fixed cache size, Cacheonix will return a error
    * to a request to add an element to the cache if the size of the elements of elements in cache exceeds maxBytes.
    *
    * @return the mandatory size of the cache in bytes. In case of the fixed cache size, Cacheonix will return a error
    *         to a request to add an element to the cache if the size of the elements of elements in cache exceeds
    *         maxBytes.
    */
   public long getMaxBytes() {

      return this.maxBytes;
   }


   /**
    * Sets the mandatory size of the cache in bytes. In case of the fixed cache size, Cacheonix will return a error to a
    * request to add an element to the cache if the size of the elements of elements in cache exceeds maxBytes.
    * <p/>
    * Important: Cacheonix recommends setting 'maxBytes' so that total summa of sizes  of all caches doesn't exceed 40%
    * of JVM heap.
    *
    * @param maxBytes the mandatory size of the cache in bytes. In case of the fixed cache size, Cacheonix will return a
    *                 error to a request to add an element to the cache if the size of the elements of elements in cache
    *                 exceeds maxBytes. Important: Cacheonix recommends setting 'maxBytes' so that total summa of sizes
    *                 of all caches doesn't exceed 40% of JVM heap.
    */
   public void setMaxBytes(final long maxBytes) {

      this.maxBytes = maxBytes;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("maxBytes".equals(attributeName)) {

         maxBytes = StringUtils.readBytes(attributeValue);
      }
   }


   public String toString() {

      return "FixedSizeConfiguration{" +
              "maxBytes='" + maxBytes + '\'' +
              '}';
   }
}
