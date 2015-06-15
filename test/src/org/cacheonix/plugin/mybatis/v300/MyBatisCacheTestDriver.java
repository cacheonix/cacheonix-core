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
package org.cacheonix.plugin.mybatis.v300;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.apache.ibatis.cache.CacheKey;

public abstract class MyBatisCacheTestDriver extends CacheonixTestCase {


   private static final String CACHEONIX_CONFIG_MY_BATIS_CACHE_TEST_XML = "cacheonix-config-MyBatisCacheTest.xml";

   private static final String ID = "org.mybatis.example.AccountMapper";

   private static final String TEST_VALUE = "Test Value";

   protected MyBatisCache myBatisCache;

   private Cacheonix cacheonix;


   public void testClear() throws Exception {

      final CacheKey cacheKey = createKey();
      myBatisCache.putObject(cacheKey, TEST_VALUE);
      myBatisCache.clear();
      assertEquals(0, myBatisCache.getSize());
   }


   public void testGetId() throws Exception {

      assertEquals(ID, myBatisCache.getId());
   }


   public void testGetObject() throws Exception {

      final CacheKey cacheKey = createKey();
      myBatisCache.putObject(cacheKey, TEST_VALUE);
      assertEquals(TEST_VALUE, myBatisCache.getObject(cacheKey));
   }


   public void testGetReadWriteLock() throws Exception {

      assertNotNull(myBatisCache.getReadWriteLock());
   }


   public void testGetSize() throws Exception {

      final CacheKey cacheKey = createKey();
      myBatisCache.putObject(cacheKey, TEST_VALUE);
      assertEquals(1, myBatisCache.getSize());
   }


   public void testPutObject() throws Exception {

      final CacheKey cacheKey = createKey();
      myBatisCache.putObject(cacheKey, TEST_VALUE);
      assertEquals(TEST_VALUE, myBatisCache.getObject(cacheKey));
   }


   public void testRemoveObject() throws Exception {

      final CacheKey cacheKey = createKey();
      myBatisCache.putObject(cacheKey, TEST_VALUE);
      myBatisCache.removeObject(cacheKey);
      assertEquals(0, myBatisCache.getSize());
   }


   public void testEquals() throws Exception {

      assertEquals(myBatisCache, createMyBatisCache(cacheonix));
   }


   public void testHashCode() throws Exception {

      assertTrue(myBatisCache.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(myBatisCache.toString());
   }


   public void setUp() throws Exception {

      super.setUp();
      cacheonix = Cacheonix.getInstance(TestUtils.getTestFile(CACHEONIX_CONFIG_MY_BATIS_CACHE_TEST_XML).toString());
      myBatisCache = createMyBatisCache(cacheonix);
   }


   public void tearDown() throws Exception {

      myBatisCache = null;
      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      super.tearDown();
   }


   private static MyBatisCache createMyBatisCache(final Cacheonix cacheonix) {

      return new MyBatisCache(ID, cacheonix);
   }


   private static CacheKey createKey() {

      final CacheKey cacheKey = new CacheKey();
      cacheKey.update("org.mybatis.example.AccountMapper.getAccountByUsername");
      cacheKey.update("Key Object 1");
      cacheKey.update("Key Object 2");
      return cacheKey;
   }


   public String toString() {

      return "MyBatisCacheTest{" +
              "myBatisCache=" + myBatisCache +
              "} " + super.toString();
   }
}
