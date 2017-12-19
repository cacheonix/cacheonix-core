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
package org.cacheonix.impl.cache.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.cache.util.DummyObjectSizeCalculator;
import org.cacheonix.impl.config.ElementEventNotification;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tests {@link LocalCacheReadPerformanceTest}
 *
 * @noinspection ControlFlowStatementWithoutBraces, JUnitTestMethodWithNoAssertions
 */
public final class LocalCacheReadPerformanceTest extends CacheonixTestCase {

   /**
    * Logger.
    */
   @SuppressWarnings("UnusedDeclaration")
   private static final Logger LOG = Logger.getLogger(LocalCacheReadPerformanceTest.class); // NOPMD

   private static final int MAX_SIZE = 100000;

   public final static int SIZE = 50000;

   private LocalCache<String, String> cache;


   public void testRead() {


      final List<String> list = new ArrayList<String>(SIZE);

      for (int i = 0; i < SIZE; i++) {

         list.add(String.valueOf(i));

      }


      final Map<String, String> map = new HashMap<String, String>(SIZE);

      for (final String s : list) {

         map.put(s, s);

      }


      long timeBefore = System.currentTimeMillis();

      cache.putAll(map);

      long time = System.currentTimeMillis() - timeBefore;

      LOG.info("Updated " + SIZE + " values in " + time + " milliseconds");


      final List<String> retList = new ArrayList<String>(SIZE);

      timeBefore = System.currentTimeMillis();

      for (final String s : list) {

         retList.add(cache.get(s));

      }

      time = System.currentTimeMillis() - timeBefore;

      LOG.info("Read " + SIZE + " values in " + time + " milliseconds");

   }


   protected void setUp() throws Exception {

      super.setUp();
      cache = new LocalCache<String, String>(TestConstants.LOCAL_TEST_CACHE, MAX_SIZE, 0, 0, 0, getClock(),
              getEventNotificationExecutor(), new DummyDiskStorage(TestConstants.LOCAL_TEST_CACHE),
              new DummyObjectSizeCalculator(), new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      cache.shutdown();

      super.tearDown();

      cache = null;
   }
}
