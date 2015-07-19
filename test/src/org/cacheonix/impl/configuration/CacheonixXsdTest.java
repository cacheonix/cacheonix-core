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

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A tester for Cacheonix XSD.
 */
public final class CacheonixXsdTest extends CacheonixTestCase {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ConfigurationImplTest.class); // NOPMD

   private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

   private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

   private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";


   public void testParseCacheonixXSD() throws ParserConfigurationException, IOException, SAXException {

      final InputStream xsdStream = getClass().getClassLoader().getResourceAsStream("META-INF/cacheonix-config-2.0.xsd");
      try {

         // Create parser factory
         final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware(true);
         documentBuilderFactory.setValidating(true);
         documentBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
         documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, "http://www.w3.org/2001/XMLSchema.xsd");

         // Create parser
         final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         documentBuilder.setErrorHandler(new ErrorHandler());
         final Document document = documentBuilder.parse(xsdStream);

         // Parse
         assertNotNull(document);
      } finally {
         IOUtils.closeHard(xsdStream);
      }
   }


   public void testParseCacheonixServer() throws ParserConfigurationException, IOException, SAXException {


      // Parse
      final Document document;
      final InputStream testFileInputStream = TestUtils.getTestFileInputStream("new-config-example-server.xml");
      try {

         document = parse(testFileInputStream);
      } finally {
         IOUtils.closeHard(testFileInputStream);
      }

      assertNotNull(document);
   }


   public void testParseCacheonixLocal() throws ParserConfigurationException, IOException, SAXException {


      // Parse
      final Document document;
      final InputStream testFileInputStream = TestUtils.getTestFileInputStream("new-config-example-local.xml");
      try {

         document = parse(testFileInputStream);
      } finally {
         IOUtils.closeHard(testFileInputStream);
      }

      assertNotNull(document);
   }


   private Document parse(final InputStream testFileInputStream) throws ParserConfigurationException, SAXException,
           IOException {

      final Document document;
      final InputStream xsdStream = getClass().getClassLoader().getResourceAsStream("META-INF/cacheonix-config-2.0.xsd");
      try {

         // Create parser factory
         final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware(true);
         documentBuilderFactory.setValidating(true);
         documentBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
         documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, xsdStream);

         // Create parser
         final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         documentBuilder.setErrorHandler(new ErrorHandler());
         document = documentBuilder.parse(testFileInputStream);
      } finally {
         IOUtils.closeHard(xsdStream);
      }
      return document;
   }


   public static class ErrorHandler implements org.xml.sax.ErrorHandler {

      public void warning(final SAXParseException exception) throws SAXException {

         LOG.warn(exception.toString());

      }


      public void error(final SAXParseException exception) throws SAXException {

         throw toSAXException(exception);
      }


      public void fatalError(final SAXParseException exception) throws SAXException {

         throw toSAXException(exception);
      }


      private static SAXException toSAXException(final SAXParseException exception) {

         final SAXException saxException = new SAXException("Line " + exception.getLineNumber() + ':' + exception.getColumnNumber() + ": " + exception.getMessage());
         saxException.setStackTrace(exception.getStackTrace());
         return saxException;
      }
   }
}
