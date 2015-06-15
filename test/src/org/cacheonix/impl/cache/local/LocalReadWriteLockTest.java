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
package org.cacheonix.impl.cache.local;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.locks.Lock;
import org.cacheonix.locks.ReadWriteLock;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.configuration.ElementEventNotification;
import org.cacheonix.impl.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.util.cache.DummyObjectSizeCalculator;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tester for LocalReadWriteLock.
 *
 * @see LocalReadWriteLock
 */
public final class LocalReadWriteLockTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalReadWriteLockTest.class); // NOPMD

   private LocalCache<String, String> cache;

   private static final int MAX_SIZE = 10;


   public void testGetLock() {

      final ReadWriteLock writeLock = cache.getReadWriteLock();
      final Lock lock = writeLock.writeLock();
      lock.lock();
      try {

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("lock: " + lock); // NOPMD
      } finally {
         lock.unlock();
      }
   }


   /**
    * Test that multiple threads can call lock() on the same lock object.
    * <p/>
    * The idea is the following:
    * <p/>
    * 1. Start two threads.
    * <p/>
    * 2. Let them wait until they have started.
    * <p/>
    * 3. Command them to lock.
    * <p/>
    * 4. Wait after the lock.
    * <p/>
    * 5. Command them to unlock.
    * <p/>
    * 6. Finish.
    * <p/>
    * The correct test should demonstrate that no exceptions are thrown and that one lock waits until other one
    * unlocks.
    *
    * @throws Exception if an error occurs.
    */
   public void testLockObjectSharing() throws Exception {


      final Collection<Exception> errors = new ConcurrentLinkedQueue<Exception>();

      final Lock lock = cache.getReadWriteLock().writeLock();

      final CountDownLatch startupLatch = new CountDownLatch(2);

      final Thread thread1 = new Thread(new MyRunnable(startupLatch, lock, errors));
      final Thread thread2 = new Thread(new MyRunnable(startupLatch, lock, errors));

      thread1.start();
      thread2.start();

      thread1.join();
      thread2.join();

      if (!errors.isEmpty()) {
         throw errors.iterator().next();
      }
   }


   public void testNestedLocksDetectMismatchedUnlock() {

      // Place a write lock

      final Lock lock = cache.getReadWriteLock().writeLock();
      boolean brokenLockExceptionThrown = false;
      lock.lock();
      try {
         lock.lock();
         //noinspection EmptyTryBlock
         try { // NOPMD
         } finally {
            lock.unlock();
         }
      } finally {
         lock.unlock();
         try {
            lock.unlock();
         } catch (final IllegalMonitorStateException ignored) {
            brokenLockExceptionThrown = true;
         }
      }

      assertTrue(brokenLockExceptionThrown);
   }


   public void testNestedLocksDetectMismatchedUnlockWithDifferentLockObjects() {

      // Place a write lock
      cache.getReadWriteLock().writeLock().lock();
      boolean brokenLockExceptionThrown = false;
      try {
         cache.getReadWriteLock().writeLock().lock();
         //noinspection EmptyTryBlock
         try { // NOPMD
         } finally {
            cache.getReadWriteLock().writeLock().unlock();
         }
      } finally {
         cache.getReadWriteLock().writeLock().unlock();
         try {
            cache.getReadWriteLock().writeLock().unlock();
         } catch (final IllegalMonitorStateException ignored) {
            brokenLockExceptionThrown = true;
         }
      }

      assertTrue(brokenLockExceptionThrown);
   }


   protected void setUp() throws Exception {

      super.setUp();

      cache = new LocalCache<String, String>(TestConstants.LOCAL_TEST_CACHE, MAX_SIZE, 0, 0, 0,
              getClock(), getEventNotificationExecutor(), new DummyDiskStorage(TestConstants.LOCAL_TEST_CACHE),
              new DummyObjectSizeCalculator(), new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);
   }


   /**
    * The goal of this runnable is to confirm that that the same local lock object can be accessed from multiple
    * threads.
    */
   private static class MyRunnable implements Runnable {

      private final CountDownLatch startupLatch;

      private final Lock lock;

      private final Collection<Exception> errors;


      @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
      public MyRunnable(final CountDownLatch startupLatch, final Lock lock,
              final Collection<Exception> errors) {

         this.startupLatch = startupLatch;
         this.lock = lock;
         this.errors = errors;
      }


      public void run() {

         try {

            // Wait until both are ready
            startupLatch.countDown();
            startupLatch.await();
            lock.lock();
            try {

               // Wait
               Thread.sleep(10L);

            } finally {
               lock.unlock();
            }
         } catch (final Exception e) {
            errors.add(e);
         }
      }
   }
}
