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
package org.cacheonix.impl.cache.storage.disk;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;

/**
 * Tests {@link StorageCellsManager}
 */
public final class StorageCellsManagerTest extends TestCase {

   private static final long NUMBER_OF_TEST_CELLS_INCR = 100L;

   private static final String key = "testKey";

   private static final String data = "testData";

   private static final String diskStorageName = "DiskStorageTestName";

   private static final long fileSize = 141L * NUMBER_OF_TEST_CELLS_INCR;

   private static final String diskStoragefile = "TestDiskStorage.dat";

   private static File fl = null;

   private StorageCellsManager storageCellsManager = null;
   // TODO @SF - Decide what will be initialization object (File Name? Pathname?...) and how to get it


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected final void setUp() throws Exception {

      super.setUp();

      fl = TestUtils.getTestFile(diskStoragefile);

      final Object[] startupInfo = new Object[3];
      startupInfo[0] = diskStorageName;
      startupInfo[1] = Long.valueOf(fileSize);
      startupInfo[2] = fl.getCanonicalPath();

      storageCellsManager = new StorageCellsManager(startupInfo);
   }


   public final String toString() {

      return "StorageCellManagerTest{" + "diskStorage=" + diskStorageName + '}';
   }

   ///////////////////////////////////////////////////////////////////////////////////


   public void testRecordRetriveCells() throws StorageException, IOException {

      try {
         final long pos = storageCellsManager.recordCells(key, data);
         final Object[] obj = (Object[]) storageCellsManager.retrieveValue(pos);
         storageCellsManager.removeCells(pos);
         assertEquals(key, obj[0]);
         assertEquals(data, obj[1]);
      } finally {
         storageCellsManager.shutdown(true);
      }
   }


   public void testRecordRestoreCells() throws StorageException, IOException {

      try {
         final long pos = storageCellsManager.recordCells(key, data);
         final Object[] obj = (Object[]) storageCellsManager.restoreValue(pos);
         assertEquals(key, obj[0]);
         assertEquals(data, obj[1]);
      } finally {
         storageCellsManager.shutdown(true);
      }
   }


   public void testRemoveCells() throws StorageException, IOException {

      try {

         final long pos = storageCellsManager.recordCells(key, data);
         storageCellsManager.removeCells(pos);
         Object[] obj = null;
         try {
            obj = (Object[]) storageCellsManager.retrieveValue(pos);
         } catch (final StorageException e) {
            assertTrue(true);
         }
         assertNull(obj);
      } finally {
         storageCellsManager.shutdown(true);
      }
   }


   public void testStorageEnlargement() throws StorageException, IOException {

      try {
         final long nBefore = fl.length();

         final byte[] data = new byte[128];
         final byte[] key = new byte[10];
         for (int i = 0; i < (int) (NUMBER_OF_TEST_CELLS_INCR * 3); ++i) {
            storageCellsManager.recordCells(key, data);
         }

         final long nAfter = fl.length();

         assertTrue(nAfter > nBefore * 3);
      } finally {
         storageCellsManager.shutdown(true);
      }
   }


   public void testRecordRestoreSafelyCells() throws StorageException, IOException {

      try {
         final long pos = storageCellsManager.recordCells(key, data);
         final Object[] obj = (Object[]) storageCellsManager.restoreValueSafely(pos);
         assertEquals(key, obj[0]);
         assertEquals(data, obj[1]);

         Object[] obj1 = null;
         try {
            obj1 = (Object[]) storageCellsManager.retrieveValue(pos);
         } catch (final StorageException e) {
            assertTrue(true);
         }
         assertNull(obj1);

      } finally {
         storageCellsManager.shutdown(true);
      }
   }


   public void testClearStorage() throws StorageException, IOException {

      try {
         final long pos = storageCellsManager.recordCells(key, data);
         storageCellsManager.clearStorage();

         Object[] obj1 = null;
         try {
            obj1 = (Object[]) storageCellsManager.retrieveValue(pos);
         } catch (final StorageException e) {
            assertTrue(true);
         }
         assertNull(obj1);

      } finally {
         try {
            storageCellsManager.shutdown(true);
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored");
         }
      }
   }

// ////////////////////////////// NEGATIVE TESTS /////////////////////////////////////////////////////


   public void testNotInitialized() throws IOException, StorageException {

      storageCellsManager.shutdown(true);
      final StorageCellsManager ssm = new StorageCellsManager();
      try {
         assertNull(ssm.getName());
      } catch (final StorageException sex) {
         assertTrue(true);
      }
      try {
         ssm.shutdown(true);
      } catch (final StorageException sex) {
         assertTrue(true);
      }
   }
}
