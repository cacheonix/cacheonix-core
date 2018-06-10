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

import junit.framework.TestCase;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.locks.Lock;
import org.cacheonix.locks.ReadWriteLock;

/**
 * Tester for LocalCluster.
 */
public final class LocalClusterTest extends TestCase {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LocalCacheWithDiskStorageTest.class); // NOPMD

   private LocalCluster localCluster;


   @SuppressWarnings("EmptyTryBlock")
   public void testGetNamedReadWriteReadLock() {

      final ReadWriteLock readWriteLock = localCluster.getReadWriteLock();
      final Lock readLock = readWriteLock.readLock();
      readLock.lock();
      try { // NOPMD

      } finally {
         readLock.unlock();
      }

      final Lock writeLock = readWriteLock.readLock();
      writeLock.lock();
      try {  // NOPMD

      } finally {
         writeLock.unlock();
      }
   }


   @SuppressWarnings("EmptyTryBlock")
   public void testGetNamedReadWriteWriteLock() {

      final ReadWriteLock readWriteLock = localCluster.getReadWriteLock();
      final Lock writeLock = readWriteLock.readLock();
      writeLock.lock();
      try {  // NOPMD

      } finally {
         writeLock.unlock();
      }
   }


   public void setUp() throws Exception {

      super.setUp();

      localCluster = new LocalCluster();
   }


   public void tearDown() throws Exception {

      localCluster = null;
      super.tearDown();
   }
}
