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

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Defines a logging configuration.
 */
public final class LoggingConfiguration extends DocumentReader {

   /**
    * The logging level. The logging level is set by the <code>loggingLevel</code> attribute in the
    * <code>cacheonix</code> element in <code>cacheonix-config.xml</code>. The logging level can be overridden by
    * setting JVM system parameter <code>cacheonix.logging.level</code>.
    */
   private LoggingLevel loggingLevel;


   /**
    * Returns the the logging level.
    *
    * @return the the logging level. The logging level is set by the <code>loggingLevel</code> attribute in the
    *         <code>cacheonix</code> element in <code>cacheonix-config.xml</code>. The logging level can be overridden
    *         by setting JVM system parameter <code>cacheonix.logging.level</code>.
    */
   public LoggingLevel getLoggingLevel() {

      return this.loggingLevel;
   }


   /**
    * Sets the the logging level. The logging level is set by the <code>loggingLevel</code> attribute in the
    * <code>cacheonix</code> element in <code>cacheonix-config.xml</code>. The logging level can be overridden by
    * setting JVM system parameter <code>cacheonix.logging.level</code>.
    *
    * @param loggingLevel the the logging level.
    */
   public void setLoggingLevel(final LoggingLevel loggingLevel) {

      this.loggingLevel = loggingLevel;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      // Doesn't have child elements yet.
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("level".equals(attributeName)) {

         final String lowerCaseAttributeValue = attributeValue.toLowerCase();
         if ("error".equals(lowerCaseAttributeValue)) {

            loggingLevel = LoggingLevel.ERROR;
         } else if ("warning".equals(lowerCaseAttributeValue)) {

            loggingLevel = LoggingLevel.WARN;
         } else if ("info".equals(lowerCaseAttributeValue)) {

            loggingLevel = LoggingLevel.INFO;
         } else if ("debug".equals(lowerCaseAttributeValue)) {

            loggingLevel = LoggingLevel.DEBUG;
         }


         // Overwrite
         overwriteLoggingLevelWithSystemProperty();
      }
   }


   public void setUpDefaults() {

      loggingLevel = ConfigurationConstants.DEFAULT_LOGGING_LEVEL;

      overwriteLoggingLevelWithSystemProperty();
   }


   private void overwriteLoggingLevelWithSystemProperty() {

      final LoggingLevel systemLoggingLevel = SystemProperty.CACHEONIX_LOGGING_LEVEL;
      if (systemLoggingLevel != null) {

         loggingLevel = systemLoggingLevel;
      }
   }


   public String toString() {

      return "LoggingConfiguration{" +
              "loggingLevel='" + loggingLevel + '\'' +
              '}';
   }
}
