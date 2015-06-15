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
package org.cacheonix.plugin.ibatis.v230;

import java.util.Properties;

import org.cacheonix.TestConstants;
import org.cacheonix.impl.util.logging.Logger;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import junit.framework.TestCase;

/**
 * Tests iBatis plug in IBatisCacheController.
 *
 * @noinspection DuplicateStringLiteralInspection, ControlFlowStatementWithoutBraces
 */
public final class IBatisCacheControllerTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(IBatisCacheControllerTest.class); // NOPMD

   private static final String KEY = "test_key";

   private static final String OBJECT = "test_object";

   private static final String PROPERTY_CACHEONIX_CACHE_NAME = IBatisCacheController.PROPERTY_CACHEONIX_CACHE_NAME;

   private static final String PROPERTY_CACHEONIX_CONFIGURATION = IBatisCacheController.PROPERTY_CACHEONIX_CONFIGURATION;

   private CacheModel cacheModel = null;

   private IBatisCacheController controller = null;

   private TestIbatisCacheFactory ibatisCacheFactory = null;


   public void testConfigure() {

      assertTrue(ibatisCacheFactory.isGetCacheCalled());
   }


   /**
    * Tests {@link IBatisCacheController#flush(CacheModel)}.
    */
   public void testFlush() {

      controller.putObject(cacheModel, KEY, OBJECT);
      controller.flush(cacheModel);
      assertEquals(0, controller.size());
   }


   /**
    * Tests {@link IBatisCacheController#getObject(CacheModel, Object)}.
    */
   public void testGetObject() {

      controller.putObject(cacheModel, KEY, OBJECT);
      assertEquals(OBJECT, controller.getObject(cacheModel, KEY));
   }


   /**
    * Tests {@link IBatisCacheController#getObject(CacheModel, Object)}.
    */
   public void testPutObject() {

      final int initialSize = controller.size();
      controller.putObject(cacheModel, KEY, OBJECT);
      assertEquals(initialSize + 1, controller.size());
   }


   /**
    * Tests {@link IBatisCacheController#getObject(CacheModel, Object)}.
    */
   public void testRemoveObject() {

      assertEquals(0, controller.size());
      controller.putObject(cacheModel, KEY, OBJECT);
      assertEquals(1, controller.size());
      final Object actual = controller.removeObject(cacheModel, KEY);
      assertEquals(0, controller.size());
      assertEquals(OBJECT, actual);
   }


   /**
    * Tests that default constructor exists. IBatisCacheController should have a default constuctor.
    */
   public void testDefaultConstructor() throws ClassNotFoundException, IllegalAccessException,
           InstantiationException {

      assertNotNull(Class.forName(IBatisCacheController.class.getName()).newInstance());
   }


   public void testToString() {

      assertNotNull(controller.toString());
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      cacheModel = new CacheModel();
      ibatisCacheFactory = new TestIbatisCacheFactory();
      controller = new IBatisCacheController(ibatisCacheFactory);
      controller.configure(makeConfigurationProperties());
      controller.flush(cacheModel);
      assertEquals(0, controller.size());
   }


   private static Properties makeConfigurationProperties() {

      final Properties props = new Properties();
      props.setProperty(PROPERTY_CACHEONIX_CACHE_NAME, TestConstants.TEST_IBATIS_CACHE);
      props.setProperty(PROPERTY_CACHEONIX_CONFIGURATION, TestConstants.CACHE_CONTROLLER_CACHEONIX_CONFIG_XML);
      return props;
   }


   /**
    * @noinspection ObjectToString
    */
   public String toString() {

      return "IBatisCacheControllerTest{" +
              "cacheModel=" + cacheModel +
              ", controller=" + controller +
              ", ibatisCacheFactory=" + ibatisCacheFactory +
              '}';
   }
}
