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
package org.cacheonix.cache;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestConstants;
import org.cacheonix.cluster.Cluster;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.locks.ReadWriteLock;

import static org.cacheonix.impl.config.SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE;

/**
 * Tests CacheManager using local cahce configurations only.
 */
public final class LocalCacheonixTest extends CacheonixTestCase {

   private static final String TEST_LOCAL_CACHE = "local.test.cache";

   private static final String TEST_CACHE_DOES_NOT_EXIT = "test_cache_does_not_exist";

   private Cacheonix instance = null;


   public void testGetInstance() {

      assertNotNull(instance);
   }


   /**
    * Test getting a named local cache.
    */
   public void testGetLocalCache() {

      final Cache cache = instance.getCache(TEST_LOCAL_CACHE);
      assertNotNull("Cache should be not null", cache);
   }


   /**
    * Test getting a named local cache.
    */
   public void testGetNonExistingCache() {

      final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);
      savedSystemProperty.save();
      try {
         System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
         assertNull(instance.getCache(TEST_CACHE_DOES_NOT_EXIT));
      } finally {
         savedSystemProperty.restore();
      }
   }


   public void testToString() {

      assertNotNull(instance.toString());
   }


   public void testDeleteCache() {

      final SavedSystemProperty savedSystemProperty = new SavedSystemProperty(NAME_CACHEONIX_AUTO_CREATE_CACHE);
      savedSystemProperty.save();
      try {

         System.setProperty(NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");
         instance.deleteCache(TEST_LOCAL_CACHE);
         assertNull(instance.getCache(TEST_LOCAL_CACHE));
      } finally {

         savedSystemProperty.restore();
      }
   }


   public void testCreateCache() {

      instance.deleteCache(TEST_LOCAL_CACHE);
      assertNotNull(instance.createCache(TEST_LOCAL_CACHE));
      assertNotNull(instance.getCache(TEST_LOCAL_CACHE));
   }


   public void testGetCluster() {

      final Cluster cluster = instance.getCluster();
      assertNotNull(cluster);

      final ReadWriteLock readWriteLock = cluster.getReadWriteLock();
      assertNotNull(readWriteLock);
   }


   protected void setUp() throws Exception {

      super.setUp();
      instance = Cacheonix.getInstance(TestConstants.CACHEONIX_LOCAL_XML);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      instance.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      instance = null;

      super.tearDown();
   }


   public String toString() {

      return "LocalCacheManagerTest{" +
              "instance=" + instance +
              '}';
   }
}
