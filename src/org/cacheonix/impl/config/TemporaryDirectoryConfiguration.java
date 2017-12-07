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

import java.io.File;
import java.io.IOException;

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Defines a directory used by Cacheonix store various temporary files.
 */
public final class TemporaryDirectoryConfiguration extends DocumentReader {

   /**
    * Field path.
    */
   private String path = null;


   /**
    * Returns the value of field 'path'.
    *
    * @return the value of field 'Path'.
    */
   public String getPath() {

      return path;
   }


   /**
    * Sets the value of field 'path'.
    *
    * @param path the value of field 'path'.
    * @throws IllegalArgumentException if the path cannot be converted to a canonical path.
    */
   public void setPath(final String path) throws IllegalArgumentException {

      try {

         this.path = new File(path).getCanonicalPath();
      } catch (final IOException e) {

         throw new IllegalArgumentException("Invalid path: " + path, e);
      }
   }


   protected void readNode(final String nodeName, final Node childNode) {

   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("path".equals(attributeName)) {

         if ("${java.io.tmpdir}".equals(attributeValue)) {

            setPath(ConfigurationConstants.JAVA_IO_TEMP);
         } else {

            setPath(attributeValue);
         }
      }
   }


   public void setUpDefaults() {

      if (StringUtils.isBlank(path)) {

         setPath(ConfigurationConstants.JAVA_IO_TEMP);
      }
   }


   public String toString() {

      return "TemporaryDirectoryConfiguration{" +
              "path='" + path + '\'' +
              '}';
   }
}
