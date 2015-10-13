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
package org.cacheonix.impl.config;

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class ExpirationConfiguration.
 */
public final class ExpirationConfiguration extends DocumentReader {

   /**
    * A time since the element was put into the cache before an element is discarded from the cache. An element won't
    * expire if the time to live is set to 0. The default value is 0 ('don't expire').
    */
   private long timeToLiveMillis = StringUtils.readTime("0s");

   /**
    * A number of seconds since the last access before an element is discarded from the cache. The default value is 0
    * ('don't discard').
    */
   private long idleTimeMillis = StringUtils.readTime("0s");


   /**
    * Returns the number of seconds since the last access before an element is discarded from the cache.
    *
    * @return the number of seconds since the last access before an element is discarded from the cache.
    */
   public long getIdleTimeMillis() {

      return this.idleTimeMillis;
   }


   /**
    * Returns the time since the element was put into the cache before an element is discarded from the cache. An
    * element won't  expire if the time to live is set to 0. The default value is 0 ('don't expire').
    *
    * @return the time since the element was put into the cache before an element is discarded from the cache. An won't
    *         expire if the time to live is set to 0. The default value is 0 ('don't expire').
    */
   public long getTimeToLiveMillis() {

      return this.timeToLiveMillis;
   }


   /**
    * Sets the number of seconds since the last access before an element is discarded from the cache. The default value
    * is 0 ('don't discard').
    *
    * @param idleTimeMillis the number of seconds since the last access before an element is discarded from the cache.
    */
   public void setIdleTimeMillis(final long idleTimeMillis) {

      this.idleTimeMillis = idleTimeMillis;
   }


   /**
    * Sets the time since the element was put into the cache before an element is discarded from the cache. An element
    * won't  expire if the time to live is set to 0. The default value is 0 ('don't expire').
    *
    * @param timeToLiveMillis the time since the element was put into the cache before an element is discarded from the
    *                         cache. An element won't  expire if the time to live is set to 0. The default value is 0
    *                         ('don't expire').
    */
   public void setTimeToLiveMillis(final long timeToLiveMillis) {

      this.timeToLiveMillis = timeToLiveMillis;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // This element doesn't have child elements yet
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("timeToLive".equals(attributeName)) {

         timeToLiveMillis = StringUtils.readTime(attributeValue);
      } else if ("idleTime".equals(attributeName)) {

         idleTimeMillis = StringUtils.readTime(attributeValue);
      }
   }


   public void setUpDefaults() {

      timeToLiveMillis = 0L;
      idleTimeMillis = 0;
   }


   public String toString() {

      return "ExpirationConfiguration{" +
              "timeToLive='" + timeToLiveMillis + '\'' +
              ", idleTime='" + idleTimeMillis + '\'' +
              '}';
   }
}
