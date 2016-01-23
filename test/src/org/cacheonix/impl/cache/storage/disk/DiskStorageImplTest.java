/**
 *
 */
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
package org.cacheonix.impl.cache.storage.disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.util.IOUtils;

/**
 * Tests DiskStorageImpl.
 */
public final class DiskStorageImplTest extends TestCase {

   private DiskStorageImpl diskStorage;

   private static final String key = "testKey";

   private static final String data = "testData";

   private static final String diskStorageName = "DiskStorageTestName";

   private static final String diskStorageFile = "TestDiskStorageFile.dat";


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      final File fl = TestUtils.getTestFile(diskStorageFile);
      diskStorage = (DiskStorageImpl) StorageFactory.createStorage(diskStorageName, 1024L * 1024L,
              fl.getCanonicalPath());
   }


   public void testPutGetRemove() throws StorageException, IOException {

      final StoredObject str = diskStorage.put(key, data);
      final Object savedValue = diskStorage.get(str);
      assertEquals(data, savedValue);
      diskStorage.remove(str);
   }


   public void testGetName() throws StorageException {

      assertEquals(diskStorageName, diskStorage.getName());
   }


   public void testRemove() throws StorageException, IOException {

      final StoredObject str = diskStorage.put(key, data);
      diskStorage.remove(str);
      Object value = null;
      try {
         value = diskStorage.get(str);
      } catch (final StorageException es) {
         assertTrue(true);
      }
      assertNotSame(value, data);
   }


   public void testClear() throws StorageException {

      final StoredObject str = diskStorage.put(key, data);

      diskStorage.clear();

      Object value = null;

      try {
         value = diskStorage.get(str);
      } catch (final StorageException e) {
         assertTrue(true);
      }
      assertNotSame(value, data);
   }


   public void testGetFailure() {

      Object value = null;

      try {
         final String failedKey = "JUNK Key";
         value = diskStorage.get(failedKey);
      } catch (final StorageException sex) {
         assertNull(value);
         assertTrue(sex.getMessage().contains("key object is not of StoredObject type"));
      }

      assertNull(value);
   }


   public void testGetIOFailure() throws StorageException, IOException {

      final StoredObject str = diskStorage.put(key, data);

      Object value = null;

      RandomAccessFile fileRand = null;
      try {
         final File fl = TestUtils.getTestFile(diskStorageFile);

         fileRand = new RandomAccessFile(fl, "rw");
         fileRand.setLength(10L);
         value = diskStorage.get(str);
      } catch (final StorageException sex) {
         assertNull(value);
         final Throwable iox = sex.getCause();

         assertTrue(iox instanceof IOException);
      } finally {

         IOUtils.closeHard(fileRand);
      }

      assertNull(value);
   }


   public void testRestore() throws StorageException {

      final StoredObject str = diskStorage.put(key, data);
      final Object savedValue = diskStorage.get(str);
      assertEquals(data, savedValue);

      final Object restoredValue = diskStorage.restore(str);
      assertEquals(data, restoredValue);
      assertEquals(savedValue, restoredValue);

      Object value = null;

      try {
         value = diskStorage.get(str);
      } catch (final StorageException e) {
         assertTrue(true);
      }
      assertNotSame(value, data);
   }


   public String toString() {

      return "DiskStorageImplTest{" + "diskStorage=" + diskStorage + '}';
   }
}
