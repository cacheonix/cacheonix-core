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

/**
 * This is a dummy disk storage that does not do anything. It can be see as a storage of maxim size 0 bytes.
 */
public final class DummyDiskStorage implements DiskStorage {

   /**
    * Storage name
    */
   private final String name;


   /**
    * Constructor.
    *
    * @param name this storage name.
    */
   public DummyDiskStorage(final String name) {

      this.name = name;
   }


   /**
    * @param startupInfo - object (container) which could carry Storage specific initialization information
    * @return <code>true</code> if <b>Storage</b> successfully initialized and <code>false</code> in case of failure
    * @throws org.cacheonix.storage.StorageException
    *
    */
   public boolean initialize(final Object startupInfo) {

      return true;
   }


   /**
    * <b>shutdown</b> - closing and cleaning (when indicated by deleteStorageContents) Storage resource
    *
    * @param deleteStorageContents - boolean which indicates if storage should be erased after shutdown (true) or
    *                              persistent (false)
    */
   public void shutdown(final boolean deleteStorageContents) {

   }


   /**
    * <b>put</b> puts cache object with key and value into Storage.
    *
    * @param key   - serializable object for key
    * @param value - serializable object for value
    * @return always null
    */
   public StoredObject put(final Object key, final Object value) {

      return null;
   }


   /**
    * <b>get</b> gets value object from specific storage. Entry in the storage is <u>not remove</u>.
    *
    * @param key - is specific for system key object. In case of disk storage for example it will be StorageObject which
    *            will carry long Position for the disk cell
    * @return always null
    */
   public Object get(final Object key) {

      return null;
   }


   /**
    * <b>restore</b> gets value object from specific storage. Entry in the storage is <u>removed</u>.
    *
    * @param key - is specific for system key object. In case of disk storage for example it will be StorageObject which
    *            will carry long </u>position</u> for the disk cell
    * @return always null
    */
   public Object restore(final Object key) {

      return null;
   }


   /**
    * <b>remove</b> removes object associated with specific key from the storage
    *
    * @param key - is specific for system key object. In case of disk storage for example it will be StorageObject which
    *            will carry long <u>position</u> for the disk cell
    * @return always false
    */
   public boolean remove(final Object key) {

      return false;
   }


   /**
    * <b>getName</b> get name for the Storage
    *
    * @return <u>String</u> with the Storage name
    */
   public String getName() {

      return name;
   }


   /**
    * <b>size</b> gets size allocated for Storage
    *
    * @return <u>long</u> size value
    */
   public long size() {

      return 0L;  //To change body of implemented methods use File | Settings | File Templates.
   }


   /**
    * Clears the storage from all objects.
    */
   public void clear() {

   }


   public String toString() {

      return "DummyDiskStorage{" +
              "name='" + name + '\'' +
              '}';
   }
}
