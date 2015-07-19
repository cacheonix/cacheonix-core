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
import org.cacheonix.impl.configuration.ElementEventNotification;
import org.cacheonix.impl.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.cache.DummyObjectSizeCalculator;
import org.cacheonix.impl.util.logging.Logger;

/**
 */
public final class LocalCacheIntegerPutGetPerformanceTest extends CacheonixTestCase {

   /**
    * Logger.
    */
   @SuppressWarnings("UnusedDeclaration")
   private static final Logger LOG = Logger.getLogger(LocalCacheIntegerPutGetPerformanceTest.class); // NOPMD

   private final static int SIZE = 1000000;

   private LocalCache<Integer, Integer> cache;


   public void testRead() {


      final List<Integer> list = new ArrayList<Integer>(SIZE);

      for (int i = 0; i < SIZE; i++) {

         list.add(i);
      }


      final Map<Integer, Integer> map = new HashMap<Integer, Integer>(SIZE);
      for (final Integer integer : list) {
         map.put(integer, integer);
      }


      long timeBefore = System.nanoTime();

      for (final Integer i : list) {
         cache.put(i, i);
      }

      long time = System.nanoTime() - timeBefore;

      LOG.info("One-by-one update of " + SIZE + " keys took " + time / 1000000L + " milliseconds");


      timeBefore = System.nanoTime();

      for (final Integer i : list) {

         cache.get(i);

      }

      time = System.nanoTime() - timeBefore;

      LOG.info("One-by-one read of " + SIZE + " key took " + time / 1000000L + " milliseconds, or " + Math.round(
              SIZE / ((double) time / (double) 1000000000L)) + " per second");

   }


   protected void setUp() throws Exception {

      super.setUp();
      cache = new LocalCache<Integer, Integer>(TestConstants.LOCAL_TEST_CACHE, SIZE, 0, 0, 0, getClock(),
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


   public String toString() {

      return "LocalCacheIntegerPutGetPerformanceTest{" +
              "cache=" + cache +
              '}';
   }
}
