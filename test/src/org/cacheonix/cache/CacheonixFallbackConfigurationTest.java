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
package org.cacheonix.cache;

import java.net.URL;

import com.gargoylesoftware.base.testing.OrderedTestSuite;
import junit.framework.TestSuite;
import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.ShutdownMode;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tests Cacheonix.
 */
public final class CacheonixFallbackConfigurationTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheonixFallbackConfigurationTest.class); // NOPMD

   private Cacheonix instance = null;


   public CacheonixFallbackConfigurationTest(final String name) {

      super(name);
   }


   public void testGetInstance() {

      assertNotNull(instance);
   }


   public void testGetCache() {

      final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);
      savedSystemProperty.save();
      try {
         System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "true");
         assertNotNull(instance.getCache("cache-name"));
      } finally {
         savedSystemProperty.restore();
      }
   }


   protected void setUp() throws Exception {

      super.setUp();
      final URL resource = Cacheonix.class.getResource(ConfigurationConstants.FALLBACK_CACHEONIX_XML_RESOURCE);
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("resource: " + resource);
      instance = Cacheonix.getInstance(resource.toExternalForm());
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      instance.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      instance = null;

      super.tearDown();
   }


   /**
    * Required by JUnit
    */
   public static TestSuite suite() {

      return new OrderedTestSuite(CacheonixFallbackConfigurationTest.class, new String[]{
              "testGetCache",
      });
   }


   public String toString() {

      return "CacheonixFallbackConfigurationTest{" +
              "instance=" + instance +
              '}';
   }
}
