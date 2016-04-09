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
package org.cacheonix.plugin.hibernate.v32;

import java.util.Properties;

import org.cacheonix.TestConstants;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;
import com.gargoylesoftware.base.testing.OrderedTestSuite;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.hibernate.cache.Cache;
import org.hibernate.cfg.Environment;

/**
 * Tests {@link HibernateCacheonixCacheProvider}.
 */
public final class HibernateCacheonixCacheProviderTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(HibernateCacheonixCacheProviderTest.class); // NOPMD

   private static final String TEST_REGION_NAME = "test_hibernate_cache";

   private static final int DEFAULR_LOC_TIMEOUT_MILLIS = HibernateCacheonixCacheProvider.DEFAULT_LOCK_TIMEOUT_SECS * 1000;

   private static final String NOT_INTEGER = "not integer";

   private static final String NEVER_EXISTED_CACHEONIX_XML = "never_existed_cacheonix-config.xml";

   private static final long LONG_ZERO = 0L;

   private static final long LONG_MINUS_ONE = -1L;

   private HibernateCacheonixCacheProvider provider = null;


   /**
    * Tests {@link HibernateCacheonixCacheProvider#buildCache(String, Properties)}
    */
   public void testBuildCache() {

      final Properties properties = new Properties();
      final Cache cache = provider.buildCache(TEST_REGION_NAME, properties);
      assertNotNull(cache);
      assertEquals(TEST_REGION_NAME, cache.getRegionName());
      assertEquals(LONG_ZERO, cache.getElementCountInMemory());
      assertEquals(LONG_ZERO, cache.getElementCountOnDisk());
      // -1 stands for "not impmeneted"
      assertEquals(LONG_MINUS_ONE, cache.getSizeInMemory());
      assertEquals(DEFAULR_LOC_TIMEOUT_MILLIS, cache.getTimeout());
   }


   /**
    * Tests {@link HibernateCacheonixCacheProvider#buildCache(String, Properties)}
    */
   public void testBuildCacheSetsDefaultTimeout() {

      final Properties properties = new Properties();
      properties.setProperty(HibernateCacheonixCacheProvider.PROPERTY_CACHEONIX_LOCK_TIMEOUT, NOT_INTEGER);
      final Cache cache = provider.buildCache(TEST_REGION_NAME, properties);
      assertEquals(DEFAULR_LOC_TIMEOUT_MILLIS, cache.getTimeout());
   }


   /**
    * Tests {@link HibernateCacheonixCacheProvider#isMinimalPutsEnabledByDefault()}
    *
    * @noinspection InstanceMethodNamingConvention
    */
   public void testIsMinimalPutsEnabledByDefault() {

      assertFalse(provider.isMinimalPutsEnabledByDefault());
   }


   /**
    * Tests {@link HibernateCacheonixCacheProvider#nextTimestamp()}
    */
   public void testNextTimestamp() {

      assertTrue(provider.nextTimestamp() > LONG_ZERO);
   }


   /**
    * Tests {@link HibernateCacheonixCacheProvider#start(Properties)}
    *
    * @noinspection OverlyBroadCatchBlock
    */
   public void testStart() {

      try {
         final Properties properties = new Properties();
         provider.start(properties);
      } catch (final Exception e) {
         fail("Exception should not be trown but it was: " + e.toString());
      }
   }


   /**
    * Tests {@link HibernateCacheonixCacheProvider#start(Properties)}
    *
    * @noinspection NestedTryStatement, OverlyBroadCatchBlock, InstanceMethodNamingConvention, ImplicitNumericConversion
    */
   public void testStartThrowsExceptionOnMissingConfiguration() {

      try {
         final Properties properties = new Properties();
         properties.setProperty(Environment.CACHE_PROVIDER_CONFIG, NEVER_EXISTED_CACHEONIX_XML);
         final HibernateCacheonixCacheProvider anotherProvider = new HibernateCacheonixCacheProvider();
         try {
            anotherProvider.start(properties);
         } finally {
            anotherProvider.stop();
         }
         fail("Expected exception but it was not thrown");
      } catch (final Exception e) {
         assertTrue("Name of the missing Cacheonix configuration should be mentioned", StringUtils.toString(e).indexOf(NEVER_EXISTED_CACHEONIX_XML) > LONG_ZERO);
      }
   }


   /**
    * Tests {@link HibernateCacheonixCacheProvider#stop()}
    */
   public void testStop() {

      try {
         provider.stop();
      } catch (final Exception e) {
         fail("Exception should not be trown but it was: " + e.toString());
      }
   }


   public void testToString() {

      assertNotNull(provider.toString());
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();

      // Use configuration specific to hibernate cache provider test
      final Properties properties = new Properties();
      properties.setProperty(Environment.CACHE_PROVIDER_CONFIG, TestConstants.CACHEONIX_CONFIG_HIBERNATE_CACHE_PROVIDER_TEST_XML);

      // Create provider
      provider = new HibernateCacheonixCacheProvider();
      provider.start(properties);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      provider.stop();
      super.tearDown();
   }


   /**
    * Required by JUnit
    */
   public static TestSuite suite() {

      return new OrderedTestSuite(HibernateCacheonixCacheProviderTest.class, new String[]{
              "testStartThrowsExceptionOnMissingConfiguration",
      });
   }


   /**
    * Constructs a test case with the given name.
    */
   public HibernateCacheonixCacheProviderTest(final String name) {

      super(name);
   }


   public String toString() {

      return "HibernateCacheonixCacheProviderTest{" +
              "provider=" + provider +
              '}';
   }
}
