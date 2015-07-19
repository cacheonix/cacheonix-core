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

import java.io.IOException;

/**
 * @author sfichel@cacheonix.org
 */
public interface DiskStorage {

   /**
    * @param startupInfo - object (container) which could carry Storage specific initialization information
    * @return <code>true</code> if <b>Storage</b> successfully initialized and <code>false</code> in case of failure
    * @throws StorageException
    */
   boolean initialize(final Object startupInfo) throws StorageException;


   /**
    * <b>shutdown</b> - closing and cleaning (when indicated by deleteStorageContents) Storage resource.
    * <p/>
    * This method does not throw any exceptions. If any errors occur, they are logged as error-level messages.
    *
    * @param deleteStorageContents - boolean which indicates if storage should be erased after shutdown (true) or
    *                              persistent (false)
    */
   void shutdown(final boolean deleteStorageContents);


   /**
    * <b>put</b> puts cache object with key and value into Storage.
    *
    * @param key   - serializable object for key
    * @param value - serializable object for value
    * @return StoredObject wrapper in case of success
    * @throws StorageException
    */
   StoredObject put(Object key, Object value) throws StorageException;


   /**
    * <b>get</b> gets value object from specific storage. Entry in the storage is <u>not remove</u>.
    *
    * @param key - is specific for system key object. In case of disk storage for example it will be StorageObject which
    *            will carry long Position for the disk cell
    * @return value <b>object</b> which is associated with the key
    * @throws StorageException
    */
   Object get(final Object key) throws StorageException;


   /**
    * <b>restore</b> gets value object from specific storage. Entry in the storage is <u>removed</u>.
    *
    * @param key - is specific for system key object. In case of disk storage for example it will be StorageObject which
    *            will carry long </u>position</u> for the disk cell
    * @return value <b>object</b> which is associated with the key
    */
   Object restore(final Object key) throws StorageException;


   /**
    * <b>remove</b> removes object associated with specific key from the storage
    *
    * @param key - is specific for system key object. In case of disk storage for example it will be StorageObject which
    *            will carry long <u>position</u> for the disk cell
    * @return <code>true</code> if object successfully removed or <code>false</code> if fail
    * @throws IOException
    */
   boolean remove(final Object key) throws IOException;


   /**
    * <b>getName</b> get name for the Storage
    *
    * @return <u>String</u> with the Storage name
    * @throws StorageException
    */
   String getName() throws StorageException;


   /**
    * <b>size</b> gets size allocated for Storage
    *
    * @return <u>long</u> size value
    */
   long size() throws StorageException;


   /**
    * Clears the storage from all objects.
    */
   void clear() throws StorageException;
}
