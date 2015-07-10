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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.LogManager;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.PropertyConfigurator;

/**
 * An enumeration defining logging levels.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection FieldNotUsedInToString, NumericCastThatLosesPrecision
 */
public final class LoggingLevel implements Serializable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LoggingLevel.class); // NOPMD

   private static final long serialVersionUID = 8521795995189693459L;

   private static final byte CODE_DEBUG = (byte) 1;

   private static final byte CODE_ERROR = (byte) 2;

   private static final byte CODE_INFO = (byte) 3;

   private static final byte CODE_WARN = (byte) 4;

   private static final String STRING_DEBUG = "debug".toLowerCase();

   private static final String STRING_ERROR = "error".toLowerCase();

   private static final String STRING_INFO = "info".toLowerCase();

   private static final String STRING_WARN = "warn".toLowerCase();

   /**
    * Debug logging level provides maximum of the logging output. This includes informational messages, errors, warnings
    * and debug output.
    */
   public static final LoggingLevel DEBUG = new LoggingLevel(CODE_DEBUG, STRING_DEBUG, LogManager.RESOURCE_DEBUG);

   /**
    * Error logging level provides minimum of the logging output. This includes errors only.
    */
   public static final LoggingLevel ERROR = new LoggingLevel(CODE_ERROR, STRING_ERROR, LogManager.RESOURCE_ERROR);

   /**
    * Info logging level provides moderate level of the logging output. This includes informational messages and
    * errors.
    */
   public static final LoggingLevel INFO = new LoggingLevel(CODE_INFO, STRING_INFO, LogManager.RESOURCE_INFO);

   /**
    * Warning logging level provides moderate level of the logging output. This includes warning messages and errors.
    */
   public static final LoggingLevel WARN = new LoggingLevel(CODE_WARN, STRING_WARN, LogManager.RESOURCE_WARN);

   /**
    * Unique code.
    */
   private final byte code;

   /**
    * Unique description.
    */
   private final String description;

   /**
    * Name of a file in the classpath that contains Log4J configuration that corresponds this logging level
    */
   private final String resourceName;


   /**
    * Enumeration constructor.
    *
    * @param code        numeric code for logging level.
    * @param description logging level description.
    */
   private LoggingLevel(final byte code, final String description, final String resourceName) {

      this.code = code;
      this.description = description;
      this.resourceName = resourceName;
   }


   /**
    * Convert a string containing logging level to LoggingLevel object.
    *
    * @param stringLoggingLevel a string containing logging level. The valid values are <code>"debug"</code>,
    *                           <code>"error"</code> and <code>"info"</code>.
    * @return LoggingLevel corresponding the given <code>stringLoggingLevel</code>
    */
   public static LoggingLevel convert(final String stringLoggingLevel) {

      final String stringLevelLowerCase = toLowerCase(stringLoggingLevel);
      if (STRING_ERROR.equals(stringLevelLowerCase)) {
         return ERROR;
      }
      if (STRING_DEBUG.equals(stringLevelLowerCase)) {
         return DEBUG;
      }
      if (STRING_INFO.equals(stringLevelLowerCase)) {
         return INFO;
      }
      if (STRING_WARN.equals(stringLevelLowerCase)) {
         return WARN;
      }
      LOG.warn("Unknown logging level, will use " + STRING_WARN + ": "
              + stringLoggingLevel + ", Valid logging errors are "
              + STRING_INFO + ", " + STRING_ERROR + " and " + STRING_WARN);
      return WARN;
   }


   /**
    * Activates this logging level. If other logging level is active, replaces it with level.
    */
   public void activate() {

      try {
         LogManager.resetConfiguration();
         final Properties properties = readResource();
         final PropertyConfigurator propertyConfigurator = new PropertyConfigurator();
         propertyConfigurator.doConfigure(properties, LogManager.getLoggerRepository());
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new LoggingConfigurationException(e);
      }
   }


   /**
    * Reads resource.
    *
    * @return Properties defined in the resource
    * @throws IOException
    */
   Properties readResource() throws IOException {

      InputStream is = null;
      try {
         is = getClass().getResourceAsStream(resourceName);
         final Properties properties = new Properties();
         properties.load(is);
         return properties;
      } finally {
         IOUtils.closeHard(is);
      }
   }


   /**
    * Helper method.
    *
    * @param stringLevel converts a string level, usually coming from a configuration file, to a low-level string
    *                    logging level.
    * @return string converted to lower case or empty string if the string is blank.
    */
   private static String toLowerCase(final String stringLevel) {

      return StringUtils.isBlank(stringLevel) ? "" : stringLevel.toLowerCase();
   }


   /**
    * @noinspection RedundantIfStatement
    */
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final LoggingLevel that = (LoggingLevel) obj;

      if (code != that.code) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return (int) code;
   }


   public String toString() {

      return "LoggingLevel{" +
              "description='" + description + '\'' +
              '}';
   }
}
