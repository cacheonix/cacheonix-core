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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.util.Map;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.SavedSystemProperty;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.cache.Cache;
import org.cacheonix.exceptions.RuntimeTimeoutException;
import org.cacheonix.impl.configuration.SystemProperty;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;
import com.gargoylesoftware.base.testing.OrderedTestSuite;
import junit.framework.TestSuite;

/**
 * Tests clustered cache
 */
public final class PutAllRequestBug217Test extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PutAllRequestBug217Test.class); // NOPMD

   private static final String CACHEONIX_CONFIG_NO_CACHE_NODES_XML = "cacheonix-config-no-cache-nodes.xml";

   private static final String DISTRIBUTED_CACHE_NAME = "partitioned.distributed.cache";

   private final SavedSystemProperty autocreate = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE);

   private final SavedSystemProperty waitForCache = new SavedSystemProperty(SystemProperty.NAME_CACHEONIX_WAIT_FOR_CACHE);

   private final long savedClientRequestTimeoutMillis = SystemProperty.getClientRequestTimeoutMillis();

   private Cacheonix cacheonix;

   private Cache<String, String> cache;


   /**
    * List of clustered caches.
    *
    * @param name test name
    */
   public PutAllRequestBug217Test(final String name) {

      super(name);
   }


   /**
    * Tests EntrySetRequest.
    */
   public void testPutAll() {

      // Set up
      final int keyCount = 2;
      final Map<String, String> map = new HashMap<String, String>(3);
      for (int i = 0; i < keyCount; i++) {

         map.put(TEST_KEY_PREFIX + i, TEST_OBJECT_PREFIX + i);
      }

      RuntimeTimeoutException thrown = null;
      try {

         cache.putAll(map); // putAll() implements EntrySetRequest

      } catch (final RuntimeTimeoutException e) {

         thrown = e;
      }

      assertNotNull("Exception " + RuntimeTimeoutException.class.getName() + " should be thrown", thrown);
      assertTrue("Exception message should contain cache name", thrown.getMessage().contains(DISTRIBUTED_CACHE_NAME));
   }


   /**
    * Tests KeySetRequest.
    */
   public void testGetAll() {

      final Map all = cache.getAll(new HashSet<String>(1));
      assertTrue(all.isEmpty());
   }


   /**
    * Tests BucketSetRequest.
    */
   public void testSize() {

      RuntimeTimeoutException thrown = null;
      try {

         cache.size(); // size() implements BucketSetRequest

      } catch (final RuntimeTimeoutException e) {

         thrown = e;
      }

      assertNotNull("Exception " + RuntimeTimeoutException.class.getName() + " should be thrown", thrown);
      assertTrue("Exception message should contain cache name", thrown.getMessage().contains(DISTRIBUTED_CACHE_NAME));
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();

      LOG.debug("================================================================================================");
      LOG.debug("========== Starting up =========================================================================");
      LOG.debug("================================================================================================");

      // Disable autocreate
      autocreate.save();
      System.setProperty(SystemProperty.NAME_CACHEONIX_AUTO_CREATE_CACHE, "false");

      // Enable creating proxy w/o configuration
      waitForCache.save();
      System.setProperty(SystemProperty.NAME_CACHEONIX_WAIT_FOR_CACHE, "true");

      // Reduce client timeout
      SystemProperty.setClientRequestTimeoutMillis(3000L);

      final String configurationPath = TestUtils.getTestFile(CACHEONIX_CONFIG_NO_CACHE_NODES_XML).toString();
      cacheonix = Cacheonix.getInstance(configurationPath);
      cache = cacheonix.getCache(DISTRIBUTED_CACHE_NAME);

      // Let the cluster form
      Thread.sleep(1000L);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      cacheonix = null;
      cache = null;

      autocreate.restore();
      waitForCache.restore();
      SystemProperty.setClientRequestTimeoutMillis(savedClientRequestTimeoutMillis);

      super.tearDown();
      LOG.debug("================================================================================================");
      LOG.debug("========== Teared down =========================================================================");
      LOG.debug("================================================================================================");
   }


   /**
    * Required by JUnit.
    *
    * @return this test suite.
    */
   public static TestSuite suite() {

      return new OrderedTestSuite(PutAllRequestBug217Test.class, new String[]{
              "testPutAll",
      });
   }


   public String toString() {

      return "PutAllRequestBug217Test{" +
              "savedSystemProperty=" + autocreate +
              ", cacheonix=" + cacheonix +
              ", cache=" + cache +
              "} " + super.toString();
   }
}
