package org.cacheonix.impl.cache.invalidator;

import java.util.Properties;

import junit.framework.TestCase;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.cache.invalidator.CacheInvalidatorContext;

/**
 * A tester for {@link CacheInvalidatorFactory}.
 */
public final class CacheInvalidatorFactoryTest extends TestCase {


   private static final String PROPERTY_VALUE = "test.property.value";

   private static final String PROPERTY_NAME = "test.property";

   private static final String CACHE_NAME = "test.cache";

   private CacheInvalidatorFactory cacheInvalidatorFactory;


   /**
    * Tests {@link CacheInvalidatorFactory#createInvalidator(String, String, Properties)}
    */
   public void testCreateInvalidator() {

      final Properties invalidatorProperties = new Properties();
      invalidatorProperties.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
      final CacheInvalidator invalidator = cacheInvalidatorFactory.createInvalidator(CACHE_NAME,
              TestCacheInvalidator.class.getName(), invalidatorProperties);

      assertTrue(invalidator instanceof TestCacheInvalidator);
      final TestCacheInvalidator testCacheInvalidator = (TestCacheInvalidator) invalidator;
      final CacheInvalidatorContext context = testCacheInvalidator.getContext();
      assertEquals(CACHE_NAME, context.getCacheName());
      assertEquals(invalidatorProperties, context.getProperties());
   }


   public void setUp() throws Exception {

      super.setUp();

      cacheInvalidatorFactory = new CacheInvalidatorFactory();
   }


   public void tearDown() throws Exception {

      cacheInvalidatorFactory = null;

      super.tearDown();
   }


   public String toString() {

      return "CacheInvalidatorFactoryTest{" +
              '}';
   }
}