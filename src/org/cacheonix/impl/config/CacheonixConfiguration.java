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

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * CacheonixConfiguration defines the root element of the Cacheonix configuration file.
 */
@SuppressWarnings({"serial", "WeakerAccess"})
public final class CacheonixConfiguration extends DocumentReader {

   /**
    * Field tempDir.
    */
   private TemporaryDirectoryConfiguration tempDir;


   /**
    * Logging configuration.
    */
   private LoggingConfiguration loggingConfiguration;


   /**
    * Returns the value of field 'tempDir'.
    *
    * @return the value of field 'TempDir'.
    */
   public TemporaryDirectoryConfiguration getTempDir() {

      return this.tempDir;
   }


   /**
    * Sets the value of field 'tempDir'.
    *
    * @param tempDir the value of field 'tempDir'.
    */
   public void setTempDir(final TemporaryDirectoryConfiguration tempDir) {

      this.tempDir = tempDir;
   }


   /**
    * Field server.
    */
   private ServerConfiguration server;

   /**
    * Field local.
    */
   private LocalConfiguration local;


   /**
    * Returns the value of field 'local'.
    *
    * @return the value of field 'Local'.
    */
   public LocalConfiguration getLocal() {

      return this.local;
   }


   /**
    * Returns the value of field 'server'.
    *
    * @return the value of field 'Server'.
    */
   public ServerConfiguration getServer() {

      return this.server;
   }


   /**
    * Sets the value of field 'local'.
    *
    * @param local the value of field 'local'.
    */
   public void setLocal(final LocalConfiguration local) {

      this.local = local;
   }


   /**
    * Sets the value of field 'server'.
    *
    * @param server the value of field 'server'.
    */
   public void setServer(final ServerConfiguration server) {

      this.server = server;
   }


   public LoggingConfiguration getLoggingConfiguration() {

      return loggingConfiguration;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("logging".equals(nodeName)) {

         loggingConfiguration = new LoggingConfiguration();
         loggingConfiguration.read(childNode);
      } else if ("tempDir".equals(nodeName)) {

         tempDir = new TemporaryDirectoryConfiguration();
         tempDir.read(childNode);
      } else if ("server".equals(nodeName)) {

         server = new ServerConfiguration();
         server.setCacheonixConfiguration(this);
         server.read(childNode);
      } else if ("local".equals(nodeName)) {

         local = new LocalConfiguration();
         local.setCacheonixConfiguration(this);
         local.read(childNode);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      // This element doesn't have attributes yet
   }


   protected void postProcessRead() {

      super.postProcessRead();

      if (loggingConfiguration == null) {

         loggingConfiguration = new LoggingConfiguration();
         loggingConfiguration.setUpDefaults();
      }

      if (tempDir == null) {

         tempDir = new TemporaryDirectoryConfiguration();
         tempDir.setUpDefaults();
      }
   }


   public String toString() {

      return "CacheonixConfiguration{" +
              "tempDir=" + tempDir +
              ", loggingConfiguration=" + loggingConfiguration +
              ", server=" + server +
              ", local=" + local +
              "} ";
   }
}
