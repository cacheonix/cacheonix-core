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
package org.cacheonix.impl.storage.disk;

import java.io.File;
import java.io.IOException;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import junit.framework.TestCase;

/**
 * Tests {@link StorageFactory}
 */
public final class StorageFactoryTest extends TestCase {

   private static final String diskStorageName = "DiskStorageTestName";

   private static final long fileSize = (long) (32 * 10000);

   private static final String diskStoragefile = "TestDiskStorage.dat";


   public void testCreateStorage1() throws IOException, StorageException {

      final File fl = TestUtils.getTestFile(diskStoragefile);

      final DiskStorage st = StorageFactory.createStorage(diskStorageName, fileSize, fl.getCanonicalPath());

      assertTrue(fl.exists() && fl.canRead() && fl.canWrite());

      assertEquals(diskStorageName, st.getName());

      st.shutdown(true);
   }


   public void testCreateStorage2() throws IOException, StorageException {

      final File fl = TestUtils.getTestFile(diskStoragefile);

      final DiskStorage st1 = StorageFactory.createStorage(diskStorageName + 1, fileSize, fl.getCanonicalPath() + 1);

      final DiskStorage st2 = StorageFactory.createStorage(diskStorageName + 2, fileSize, fl.getCanonicalPath() + 2);

      final DiskStorage st3 = StorageFactory.createStorage(diskStorageName + 3, fileSize, fl.getCanonicalPath() + 3);

      assertEquals(diskStorageName + 1, st1.getName());

      assertEquals(diskStorageName + 2, st2.getName());

      assertEquals(diskStorageName + 3, st3.getName());

      st1.shutdown(true);

      st2.shutdown(true);

      st3.shutdown(true);
   }


   public void testCreateStorageFailure() throws IOException {

      final File fl = TestUtils.getTestFile(diskStoragefile);

      try {

         // First test... File already exists
         fl.createNewFile();
         fl.setReadOnly();

         final DiskStorage st = StorageFactory.createStorage(diskStorageName, fileSize, fl.getCanonicalPath());

         st.shutdown(true);

      } catch (final StorageException es) {
         assertTrue(true);
      } finally {
         try {
            fl.delete();
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored");
         }
      }


      try {
         final DiskStorage st = StorageFactory.createStorage(null, fileSize, fl.getCanonicalPath());
         st.shutdown(true);
         fail();
      } catch (final StorageException ex) {
         assertTrue(true);
      }

      try {
         final DiskStorage st = StorageFactory.createStorage(diskStorageName, 0L, fl.getCanonicalPath());
         st.shutdown(true);
         fail();
      } catch (final StorageException ex) {
         assertTrue(true);
      } finally {
         try {
            fl.delete();
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored");
         }
      }

      try {
         final DiskStorage st = StorageFactory.createStorage(diskStorageName, fileSize, null);
         st.shutdown(true);
         fail();
      } catch (final StorageException ex) {
         assertTrue(true);
      } finally {
         try {
            fl.delete();
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored");
         }
      }
   }
}
