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

/**
 * @author sfichel@cacheonix.org
 */
public final class StorageFactory {

   /**
    * Utility class constructor.
    */
   private StorageFactory() {

   }


   /**
    * <b>CreateStorage</b> function creates new Storage Object and initialize it
    *
    * @param name - String with name to refer to the storage
    * @param size - size of the storage in bytes
    * @param path - File path for the storage
    * @return Storage interface if successfully created and initialized, null if fail
    * @throws StorageException
    */
   public static DiskStorage createStorage(final String name, final long size,
                                           final String path) throws StorageException {

      if (size <= 0) {
         return new DummyDiskStorage(name);
      }

      DiskStorage result = null;
      final DiskStorageImpl diskStorage = new DiskStorageImpl();
      String errMessage = null;

      try {
         if (name == null || name.length() <= 0 || size <= 0L || path == null || path.length() <= 0) {
            final boolean bEmptyName = name == null || name.length() <= 0;
            final boolean bEmptyPath = path == null || path.length() <= 0;

            errMessage = "Factory cannot instantiate DiskStorage for "
                    + (bEmptyName ? "empty DiskStorage Name, " : "name '" + name + "', ")
                    + (size <= 0L ? "negative size or size 0, " : "size=" + Long.toString(size) + ", ")
                    + (bEmptyPath ? "empty Path" : "Path '" + path + '\'');
         } else {
            final Object[] startupInfo = new Object[3];
            startupInfo[0] = name;
            startupInfo[1] = Long.valueOf(size);
            startupInfo[2] = path;

            if (diskStorage.initialize(startupInfo)) {
               result = diskStorage;
            }
         }
      } catch (final Exception e) {
         throw new StorageException("Factory cannot instantiate DiskStorage implementation with name '"
                 + name
                 + "', size = "
                 + size
                 + ", path '"
                 + path
                 + '\''
                 , e);
      }

      if (result == null) {
         if (errMessage != null) {
            throw new StorageException(errMessage);
         } else {
            throw new StorageException("Factory cannot instantiate DiskStorage implementation with name '"
                    + name
                    + "', size = "
                    + size
                    + ", path '"
                    + path
                    + '\'');
         }
      }

      return diskStorage;
   }
}
