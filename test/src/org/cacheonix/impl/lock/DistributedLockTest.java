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
package org.cacheonix.impl.lock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import junit.framework.AssertionFailedError;
import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownException;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.RuntimeInterruptedException;
import org.cacheonix.impl.util.MutableBoolean;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.locks.BrokenLockException;
import org.cacheonix.locks.DeadlockException;
import org.cacheonix.locks.Lock;
import org.cacheonix.locks.ReadWriteLock;

/**
 * Tester for cacheonix.cluster.Cluster
 */
public final class DistributedLockTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DistributedLockTest.class); // NOPMD

   /**
    * Cacheonix configurations, one per cluster.
    */
   private static final String[] CACHEONIX_CONFIGURATIONS = {
           "cacheonix-config-cluster-member-1.xml",
           "cacheonix-config-cluster-member-2.xml",
           "cacheonix-config-cluster-member-3.xml"
   };

   /**
    * List of cache managers.
    */
   private final List<Cacheonix> cacheManagerList = new ArrayList<Cacheonix>(5);


   public void testGetLock() {

      final ReadWriteLock writeLock = cacheManagerList.get(0).getCluster().getReadWriteLock();
      final Lock lock = writeLock.writeLock();
      lock.lock();
      try {

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("lock: " + lock); // NOPMD
      } finally {
         lock.unlock();
      }
   }


   public void testTryLockWithNoWait() {

      // Place a hard lock
      final Lock lockAtNode0 = cacheManagerList.get(0).getCluster().getReadWriteLock().writeLock();
      lockAtNode0.lock();
      try {

         final Lock lockAtNode1 = cacheManagerList.get(1).getCluster().getReadWriteLock().writeLock();
         assertFalse("Must not succeed because write lock is already held by other node", lockAtNode1.tryLock());
      } finally {
         lockAtNode0.unlock();
      }
   }


   public void testGrantsMultipleReadLocks() {

      // Place a hard lock
      final Lock lockAtNode0 = cacheManagerList.get(0).getCluster().getReadWriteLock().readLock();
      lockAtNode0.lock();
      try {

         final Lock lockAtNode1 = cacheManagerList.get(1).getCluster().getReadWriteLock().readLock();
         assertTrue(lockAtNode1.tryLock());
         lockAtNode1.unlock();
      } finally {
         lockAtNode0.unlock();
      }
   }


   public void testDoesNotUpgradeReadLockWhileOtherThreadHoldsRead() {

      final ReadWriteLock rwLockAtNode0 = cacheManagerList.get(0).getCluster().getReadWriteLock();
      final Lock readLockAtNode0 = rwLockAtNode0.readLock();
      readLockAtNode0.lock();
      try {
         final ReadWriteLock rwLockAtNode1 = cacheManagerList.get(1).getCluster().getReadWriteLock();
         final Lock readLockAtNode1 = rwLockAtNode1.readLock();
         assertTrue(readLockAtNode1.tryLock());
         final Lock writeLockAtNode0 = rwLockAtNode0.writeLock();
         assertFalse(writeLockAtNode0.tryLock());
         readLockAtNode1.unlock();
      } finally {
         readLockAtNode0.unlock();
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
      final Lock lock = cacheManagerList.get(0).getCluster().getReadWriteLock().writeLock();

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


   public void testNestedWriteLocks() {

      // Place a write lock
      final Lock writeLock = cacheManagerList.get(0).getCluster().getReadWriteLock().writeLock();
      writeLock.lock();
      try {
         writeLock.lock();
         try {
            assertEquals(2, ((DistributedLock) writeLock).getEntryCount());
         } finally {
            writeLock.unlock();
         }
      } finally {
         writeLock.unlock();
      }
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   public void testNestedLocksWithDifferentLockObjects() {

      // Place a write lock
      final Cacheonix cacheonix = cacheManagerList.get(0);
      cacheonix.getCluster().getReadWriteLock().writeLock().lock();
      try {
         cacheonix.getCluster().getReadWriteLock().writeLock().lock();
         //noinspection EmptyTryBlock
         try { // NOPMD
         } finally {
            cacheonix.getCluster().getReadWriteLock().writeLock().unlock();
         }
      } finally {
         cacheonix.getCluster().getReadWriteLock().writeLock().unlock();
      }
   }


   public void testNestedLocksDetectMismatchedUnlock() {

      // Place a write lock
      final Lock lock = cacheManagerList.get(0).getCluster().getReadWriteLock().writeLock();
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
         } catch (final BrokenLockException e) {
            brokenLockExceptionThrown = true;
         }
      }

      assertTrue(brokenLockExceptionThrown);
   }


   public void testNestedLocksDetectMismatchedUnlockWithDifferentLockObjects() {

      // Place a write lock
      final Cacheonix cacheonix = cacheManagerList.get(0);
      cacheonix.getCluster().getReadWriteLock().writeLock().lock();
      boolean brokenLockExceptionThrown = false;
      try {
         cacheonix.getCluster().getReadWriteLock().writeLock().lock();
         //noinspection EmptyTryBlock
         try { // NOPMD
         } finally {
            cacheonix.getCluster().getReadWriteLock().writeLock().unlock();
         }
      } finally {
         cacheonix.getCluster().getReadWriteLock().writeLock().unlock();
         try {
            cacheonix.getCluster().getReadWriteLock().writeLock().unlock();
         } catch (final BrokenLockException e) {
            brokenLockExceptionThrown = true;
         }
      }

      assertTrue(brokenLockExceptionThrown);
   }


   public void testNestedReadLocks() {

      final Cacheonix cacheonix = cacheManagerList.get(0);
      final Lock readLock = cacheonix.getCluster().getReadWriteLock().readLock();
      readLock.lock();
      try {
         readLock.lock();
         try {
            assertEquals(2, ((DistributedLock) readLock).getEntryCount());
         } finally {
            readLock.unlock();
         }
      } finally {
         readLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
   }


   public void testReadLockUpgradesToWrite() {

      final Cacheonix cacheonix = cacheManagerList.get(0);
      final ReadWriteLock readWriteLock = cacheonix.getCluster().getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      final Lock writeLock = readWriteLock.writeLock();
      readLock.lock();
      try {
         writeLock.lock();
         try {
            assertEquals(1, ((DistributedLock) writeLock).getEntryCount());
            assertEquals(1, ((DistributedLock) readLock).getEntryCount());
         } finally {
            writeLock.unlock();
         }
      } finally {
         readLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   public void testWriteLockUpgradesToRead() {

      final Cacheonix cacheonix = cacheManagerList.get(0);
      final ReadWriteLock readWriteLock = cacheonix.getCluster().getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      final Lock writeLock = readWriteLock.writeLock();
      writeLock.lock();
      try {
         readLock.lock();
         try {
            assertEquals(1, ((DistributedLock) writeLock).getEntryCount());
            assertEquals(1, ((DistributedLock) readLock).getEntryCount());
         } finally {
            readLock.unlock();
         }
      } finally {
         writeLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   public void testWriteToReadToRead() {

      final Cacheonix cacheonix = cacheManagerList.get(0);
      final ReadWriteLock readWriteLock = cacheonix.getCluster().getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      final Lock writeLock = readWriteLock.writeLock();
      writeLock.lock();
      try {
         readLock.lock();
         try {
            readLock.lock();
            try {
               assertEquals(1, ((DistributedLock) writeLock).getEntryCount());
               assertEquals(2, ((DistributedLock) readLock).getEntryCount());
            } finally {
               readLock.unlock();
            }
         } finally {
            readLock.unlock();
         }
      } finally {
         writeLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   public void testReadToWriteToWrite() {

      final Cacheonix cacheonix = cacheManagerList.get(0);
      final ReadWriteLock readWriteLock = cacheonix.getCluster().getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      final Lock writeLock = readWriteLock.writeLock();
      readLock.lock();
      try {
         writeLock.lock();
         try {
            writeLock.lock();
            try {
               assertEquals(2, ((DistributedLock) writeLock).getEntryCount());
               assertEquals(1, ((DistributedLock) readLock).getEntryCount());
            } finally {
               writeLock.unlock();
            }
         } finally {
            writeLock.unlock();
         }
      } finally {
         readLock.unlock();
      }
      assertEquals(0, ((DistributedLock) readLock).getEntryCount());
      assertEquals(0, ((DistributedLock) writeLock).getEntryCount());
   }


   /**
    * Tests that a deadlock is detected
    *
    * @throws InterruptedException if interrupt occurs.
    */
   public void testDetectsDeadlock() throws InterruptedException {

      final Lock writeLock0 = cacheManagerList.get(0).getCluster().getReadWriteLock("lock0").writeLock();
      final Lock writeLock1 = cacheManagerList.get(0).getCluster().getReadWriteLock("lock1").writeLock();
      final MutableBoolean detected = new MutableBoolean();

      final CountDownLatch step1Latch = new CountDownLatch(1);
      final CountDownLatch step2Latch = new CountDownLatch(1);

      final int[] entryCount0 = new int[1];
      final Thread thread0 = new Thread(new Runnable() {

         public void run() {

            // Step #1
            try {
               step1Latch.await();
            } catch (final InterruptedException e) {
               throw new RuntimeInterruptedException(e);
            }
            writeLock0.lock();
            try {

               // Step #2
               try {
                  step2Latch.await();
                  Thread.sleep(10);
               } catch (final InterruptedException e) {
                  throw new RuntimeInterruptedException(e);
               }
               writeLock1.lock();
               try {
                  entryCount0[0] = ((DistributedLock) writeLock1).getEntryCount();
               } finally {
                  writeLock1.unlock();
               }
            } finally {
               writeLock0.unlock();
            }
         }
      });

      final int[] entryCount1 = new int[1];
      final Thread thread1 = new Thread(new Runnable() {

         public void run() {

            // Step #1
            try {
               step1Latch.await();
            } catch (final InterruptedException e) {
               throw new RuntimeInterruptedException(e);
            }
            writeLock1.lock();
            try {

               // Step #2
               try {
                  step2Latch.await();
                  Thread.sleep(20);
               } catch (final InterruptedException e) {
                  throw new RuntimeInterruptedException(e);
               }
               try {
                  writeLock0.lock();
               } catch (final DeadlockException e) {
                  detected.set(true);
               }
               entryCount1[0] = ((DistributedLock) writeLock0).getEntryCount();
            } finally {
               writeLock1.unlock();
            }
         }
      });

      //
      thread0.start();
      thread1.start();

      step1Latch.countDown();
      step2Latch.countDown();

      //
      thread0.join();
      thread1.join();

      //
      assertEquals(1, entryCount0[0]);
      assertEquals(0, entryCount1[0]);
      assertTrue(detected);
   }


   /**
    * Tests timed tryLock().
    *
    * @throws InterruptedException if interrupt occurred.
    */
   public void testTimedTryLock() throws InterruptedException {

      final Cacheonix cacheonix0 = cacheManagerList.get(0);
      final Lock writeLock = cacheonix0.getCluster().getReadWriteLock().writeLock();
      writeLock.lock();
      try {

         final Cacheonix cacheonix1 = cacheManagerList.get(1);
         assertFalse(cacheonix1.getCluster().getReadWriteLock().writeLock().tryLock(100L));

      } finally {
         writeLock.unlock();
      }
   }


   /**
    * Tests timed tryLock().
    *
    */
   @SuppressWarnings("TooBroadScope")
   public void testUnlockTimeout() {

      final long waitTime = 1000L;
      final long timeout = System.currentTimeMillis() + waitTime;
      final MutableBoolean lockGone = new MutableBoolean();
      final Lock writeLock0 = cacheManagerList.get(0).getCluster().getReadWriteLock().writeLock();
      final Lock writeLock1 = cacheManagerList.get(1).getCluster().getReadWriteLock().writeLock();
      writeLock0.lock(200L);
      try {

         // Assert we can lock now.
         while (!writeLock1.tryLock()) {

            if (System.currentTimeMillis() >= timeout) {

               throw new AssertionFailedError("Could not get a lock after " + waitTime + " ms");
            }
         }

      } finally {
         try {
            writeLock0.unlock();
         } catch (final BrokenLockException e) {
            lockGone.set(true);
         }
      }

      assertTrue(lockGone);
   }


   public void testWaitingForLockThrowsExceptionOnShutdown() throws Exception {

      final Lock writeLock = cacheManagerList.get(0).getCluster().getReadWriteLock().writeLock();
      final Cacheonix cacheonix1 = cacheManagerList.get(1);
      writeLock.lock();
      final MutableBoolean thrown = new MutableBoolean();
      try {


         final Thread thread = new Thread(new Runnable() {

            public void run() {

               try {
                  cacheonix1.getCluster().getReadWriteLock().writeLock().lock();
               } catch (final ShutdownException e) {
                  thrown.set(true);
               }
            }
         });
         thread.start();

         Thread.sleep(100L);
         cacheonix1.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         thread.join();

      } finally {
         writeLock.unlock();
      }

      assertTrue(thrown);
   }


   /**
    * Tests that it a cluster node leaves, this releases locks it held.
    *
    * @throws Exception if error occurs.
    */
   @SuppressWarnings("TooBroadScope")
   public void testWaitingForLockAcquiresLockOnShutdown() throws Exception {

      final Cacheonix cacheonix0 = cacheManagerList.get(0);
      final Cacheonix cacheonix1 = cacheManagerList.get(1);
      final MutableBoolean thrown = new MutableBoolean();
      final MutableBoolean acquired = new MutableBoolean();
      final Lock writeLock = cacheonix0.getCluster().getReadWriteLock().writeLock();
      writeLock.lock();
      try {


         final Thread thread = new Thread(new Runnable() {

            public void run() {

               try {
                  // Will acquire because shutdown of the node cacheonix0 will release a write lock held by it.
                  final Lock lock = cacheonix1.getCluster().getReadWriteLock().writeLock();
                  acquired.set(lock.tryLock(10000L));
                  lock.unlock();
               } catch (final InterruptedException e) {
                  throw new RuntimeInterruptedException(e);
               }
            }
         });
         thread.start();

         // Wait
         Thread.sleep(100L);

         // Shutdown owner of the write lock (cacheonix1) is waiting for a lock
         cacheonix0.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);

         // The fact that thread allowed to
         thread.join();

      } finally {
         try {
            writeLock.unlock();
         } catch (final ShutdownException e) {
            // Because by this time the owner of the first lock is shutdown
            thrown.set(true);
         }
      }

      assertTrue(acquired);
      assertTrue(thrown);
   }


   protected void setUp() throws Exception {
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("-------------------------- setUp -------------------------"); // NOPMD
      super.setUp();

      assertTrue(cacheManagerList.isEmpty());
      for (final String configuration : CACHEONIX_CONFIGURATIONS) {
         final Cacheonix manager = Cacheonix.getInstance(TestUtils.getTestFile(configuration).toString());
         cacheManagerList.add(manager);
      }

      // Wait for cluster to form
      waitForClusterToForm(cacheManagerList);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {
      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("-------------------------- tearDown ----------------------"); // NOPMD
      for (int i = 0; i < CACHEONIX_CONFIGURATIONS.length; i++) {
         final Cacheonix cacheonix = cacheManagerList.get(i);
         if (!cacheonix.isShutdown()) {
            cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
         }
      }
      cacheManagerList.clear();

      super.tearDown();
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
      MyRunnable(final CountDownLatch startupLatch, final Lock lock,
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
