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

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.configuration.ElementEventNotification;
import org.cacheonix.impl.util.cache.DummyObjectSizeCalculator;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.DaemonThreadFactory;

/**
 * Tests {@link LocalCachePerformanceTest}
 *
 * @noinspection ControlFlowStatementWithoutBraces, JUnitTestMethodWithNoAssertions
 */
public final class LocalCachePerformanceTest extends CacheonixTestCase {

   /**
    * Logger.
    */
   private static final Logger log = Logger.getLogger(LocalCachePerformanceTest.class); // NOPMD

   private static final int MAX_SIZE = 10000;

   private static final int NUMBER_OF_PUTS = 100000;

   private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();


   private LocalCache<Long, Boolean> cache;

   private DaemonThreadFactory threadFactory;


   /**
    * Tests performance of concurrent puts.
    * <p/>
    * This test uses random Long keys and a single-reference Boolean values.
    *
    * @throws InterruptedException if interruption occurs
    * @noinspection CallToThreadYield, UnsecureRandomNumberGeneration
    */
   public void testConcurrentPuts() throws InterruptedException {

      final Random random = new Random(System.currentTimeMillis());
      final long started = System.currentTimeMillis();
      final CountDownLatch startLatch = new CountDownLatch(1);
      final Thread[] threads = new Thread[NUMBER_OF_THREADS];
      for (int i = 0; i < NUMBER_OF_THREADS; i++) {
         final Thread th = makeWriterThread(startLatch, NUMBER_OF_PUTS, random);
         threads[i] = th;
         th.start();
      }
      startLatch.countDown(); // release
      for (final Thread thread : threads) {
         thread.join();
      }
      final long finished = System.currentTimeMillis();
      // print result
      final long totalTimeMillis = finished - started;
      if (log.isDebugEnabled()) log.debug("time, ms: " + totalTimeMillis);
      if (log.isDebugEnabled()) log.debug("puts per ms: " + NUMBER_OF_PUTS / totalTimeMillis);
      if (log.isDebugEnabled()) log.debug("cache.getStatistics(): " + cache.getStatistics());
   }


   private Thread makeWriterThread(final CountDownLatch startLatch, final int numberOfPuts,
           final Random random) {

      return threadFactory.newThread(new Runnable() {

         public void run() {

            try {
               startLatch.await();
               for (int j = 0; j < numberOfPuts; j++) {
                  cache.put(random.nextLong(), Boolean.TRUE);
               }
            } catch (final InterruptedException e) {
               ExceptionUtils.ignoreException(e, "should not be thrown");
            }
         }
      });
   }


   protected void setUp() throws Exception {

      super.setUp();
      threadFactory = new DaemonThreadFactory("LocalCachePerformanceTest");
      cache = new LocalCache<Long, Boolean>(TestConstants.LOCAL_TEST_CACHE, MAX_SIZE, 0, 0, 0, getClock(),
              getEventNotificationExecutor(), new DummyDiskStorage(TestConstants.LOCAL_TEST_CACHE),
              new DummyObjectSizeCalculator(), new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      cache.shutdown();

      cache = null;

      threadFactory = null;

      super.tearDown();
   }


   public String toString() {

      return "LocalCachePerformanceTest{" +
              "cache=" + cache +
              '}';
   }
}
