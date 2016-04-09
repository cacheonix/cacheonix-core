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

import java.util.Map;

import junit.framework.TestCase;
import org.cacheonix.Cacheonix;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.TestConstants;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tests {@link HibernateCacheonixCache}.
 *
 * @noinspection OverlyBroadCatchBlock
 */
public final class HibernateCacheonixCacheTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(HibernateCacheonixCacheTest.class); // NOPMD

   private static final int LOCK_TIMEOUT_MILLIS = 60 * 1000;

   private static final String TEST_REGION_NAME = "test_hibernate_cache";

   private static final String KEY = "key";

   private static final String VALUE = "value";

   private final SavedSystemProperty savedAutocreate = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);

   private HibernateCacheonixCache hibernateCache = null;

   @SuppressWarnings("FieldCanBeLocal")
   private Cacheonix cacheonix = null;


   /**
    * Tests {@link HibernateCacheonixCache#clear()}
    */
   public void testClear() {

      hibernateCache.put(KEY, VALUE);
      hibernateCache.clear();
      assertEquals(0L, hibernateCache.getElementCountInMemory() + hibernateCache.getElementCountOnDisk());
   }


   /**
    * Tests {@link HibernateCacheonixCache#destroy()}
    */
   public void testDestroy() {

      try {
         hibernateCache.destroy();
      } catch (final Exception e) {
         fail("Exception should not be trown but it was: " + e.toString());
      }
   }


   /**
    * Tests {@link HibernateCacheonixCache#get(Object)}
    */
   public void testGet() {

      hibernateCache.put(KEY, VALUE);
      assertEquals(VALUE, hibernateCache.get(KEY));
   }


   /**
    * Tests {@link HibernateCacheonixCache#getElementCountInMemory()}
    */
   public void testGetElementCountInMemory() {

      hibernateCache.put(KEY, VALUE);
      assertEquals(1L, hibernateCache.getElementCountInMemory());
   }


   /**
    * Tests {@link HibernateCacheonixCache#getElementCountOnDisk()}
    */
   public void testGetElementCountOnDisk() {

      assertEquals(0L, hibernateCache.getElementCountOnDisk());
   }


   /**
    * Tests {@link HibernateCacheonixCache#getRegionName()}
    */
   public void testGetRegionName() {

      assertEquals(TEST_REGION_NAME, hibernateCache.getRegionName());
   }


   /**
    * Tests {@link HibernateCacheonixCache#getSizeInMemory()}
    */
   public void testGetSizeInMemory() {

      hibernateCache.put(KEY, VALUE);
      // -1 means that this feature is not implemented yet.
      assertEquals(-1L, hibernateCache.getSizeInMemory());
   }


   /**
    * Tests {@link HibernateCacheonixCache#getTimeout()}
    */
   public void testGetTimeout() {

      assertEquals(LOCK_TIMEOUT_MILLIS, hibernateCache.getTimeout());
   }


   /**
    * Tests {@link HibernateCacheonixCache#lock(Object)}
    */
   public void testLock() {

      hibernateCache.put(KEY, VALUE);
      hibernateCache.lock(KEY);
      hibernateCache.unlock(KEY);
   }


   /**
    * Tests {@link HibernateCacheonixCache#nextTimestamp()}
    */
   public void testNextTimestamp() {

      assertTrue(hibernateCache.nextTimestamp() > 0L);
   }


   /**
    * Tests {@link HibernateCacheonixCache#put(Object, Object)}
    */
   public void testPut() {

      hibernateCache.put(KEY, VALUE);
      assertEquals(VALUE, hibernateCache.get(KEY));
   }


   /**
    * Tests {@link HibernateCacheonixCache#read(Object)}
    */
   public void testRead() {

      hibernateCache.put(KEY, VALUE);
      assertEquals(VALUE, hibernateCache.read(KEY));
   }


   /**
    * Tests {@link HibernateCacheonixCache#toMap()}
    */
   public void testToMap() {

      hibernateCache.put(KEY, VALUE);
      final Map map = hibernateCache.toMap();
      assertEquals(1, map.size());
      assertEquals(VALUE, map.get(KEY));
   }


   /**
    * Tests {@link HibernateCacheonixCache#remove(Object)}
    */
   public void testRemove() {

      hibernateCache.put(KEY, VALUE);
      hibernateCache.remove(KEY);
      assertEquals(0L, hibernateCache.getElementCountInMemory());
   }


   /**
    * Tests {@link HibernateCacheonixCache#}
    */
   public void testUnlock() {

      hibernateCache.put(KEY, VALUE);
      hibernateCache.lock(KEY);
      hibernateCache.unlock(KEY);
   }


   /**
    * Tests {@link HibernateCacheonixCache#}
    */
   public void testUpdate() {

      hibernateCache.update(KEY, VALUE);
      assertEquals(1L, hibernateCache.getElementCountInMemory());
   }


   public void testToString() {

      assertNotNull(hibernateCache.toString());
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      // NOTE: simeshev@cacheonix.org - 2009-06-20 - Enabled autocreate becuase without
      // it now other test can proceed after testDestroy() executes.
      savedAutocreate.save();
      SystemProperty.enableAutocreate();
      //
      cacheonix = Cacheonix.getInstance(TestConstants.CACHEONIX_CONFIG_HIBERNATE_CACHE_PROVIDER_TEST_XML);
      hibernateCache = new HibernateCacheonixCache(cacheonix, cacheonix.getCache(TEST_REGION_NAME), LOCK_TIMEOUT_MILLIS);
      hibernateCache.clear();
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      super.tearDown();
      // Restore saved autocreate
      savedAutocreate.restore();
      //
      if (!hibernateCache.isDestroyed()) {
         hibernateCache.clear();
      }
   }


   public String toString() {

      return "HibernateCacheonixCacheTest{" +
              "hibernateCache=" + hibernateCache +
              '}';
   }
}
