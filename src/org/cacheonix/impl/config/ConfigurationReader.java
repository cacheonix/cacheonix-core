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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Configuration reader.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public final class ConfigurationReader {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ConfigurationReader.class); // NOPMD


   private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

   private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

   private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";


   /**
    * Reads configuration defined by the <code>configurationPath</code>. The configuration is first considered as a
    * fully-qualified URL, a fully-qualified file path, a relative file path or a resource in the class path.
    *
    * @param configurationPath path to Cacheonix configuration. Can be a URL,  file path or a resource in the
    *                          classpath.
    * @return Cacheonix configuration.
    * @throws IOException if an I/O error occurs while reading the configuration.
    * @noinspection MethodReturnOfConcreteClass, IOResourceOpenedButNotSafelyClosed
    */
   public CacheonixConfiguration readConfiguration(final String configurationPath) throws IOException {

      InputStream is = null;
      try {

         // Try to get it from the URL
         is = openAsURL(configurationPath);

         // Try to get it from a file
         if (is == null) {

            is = openAsFile(configurationPath);
         }

         // Try to get it from the classpath
         if (is == null) {

            is = openAsResource(configurationPath);
         }

         if (is == null) {

            throw new IOException("Configuration could not be found: " + configurationPath);
         }

         // Read
         return readConfiguration(is);
      } finally {
         IOUtils.closeHard(is);
      }
   }


   public CacheonixConfiguration readConfiguration(final InputStream is) throws IOException {

      final CacheonixConfiguration cacheonixConfiguration = new CacheonixConfiguration();
      final Document document = parseConfiguration(is);
      final Node cacheonix = document.getElementsByTagName("cacheonix").item(0);
      cacheonixConfiguration.read(cacheonix);
      return cacheonixConfiguration;
   }


   private InputStream openAsResource(final String configurationPath) {

      return getClass().getClassLoader().getResourceAsStream(configurationPath);
   }


   private static InputStream openAsFile(final String configurationName) throws FileNotFoundException {

      final File file = new File(configurationName);
      if (file.exists()) {
         return new FileInputStream(configurationName);
      }
      return null;
   }


   private static InputStream openAsURL(final String configurationName) {

      try {

         final URL url = new URL(configurationName);
         return url.openStream();
      } catch (final IOException ignored) { // NOPMD
         // Expected
      }
      return null;
   }


   private Document parseConfiguration(final InputStream is) throws IOException {

      final String xsd = getClass().getClassLoader().getResource("META-INF/cacheonix-config-2.0.xsd").toString();
      try {

         // Create parser factory
         final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
         documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, xsd);
         documentBuilderFactory.setIgnoringComments(true);
         documentBuilderFactory.setNamespaceAware(true);
         documentBuilderFactory.setValidating(true);

         // Create parser
         final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         documentBuilder.setErrorHandler(new ErrorHandler());

         // Parse
         return documentBuilder.parse(is);

      } catch (final RuntimeException e) {

         throw e;
      } catch (final Exception e) {
         throw ExceptionUtils.createIOException(e);
      }
   }


   private static class ErrorHandler implements org.xml.sax.ErrorHandler {

      public void warning(final SAXParseException exception) throws SAXException {

         LOG.warn(exception.toString());

      }


      public void error(final SAXParseException exception) throws SAXException {

         throw exception;
      }


      public void fatalError(final SAXParseException exception) throws SAXException {

         throw exception;
      }
   }
}
