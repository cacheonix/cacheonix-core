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

import java.io.IOException;

import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * <b>DiskStorageImpl</b> class implements storage interface for use of standard file system file as storage
 *
 * @author sfichel@cacheonix.org
 */
public final class DiskStorageImpl implements DiskStorage {

   private static final Logger LOG = Logger.getLogger(DiskStorageImpl.class);

   private final StorageCellsManager storageCellsManager;


   public DiskStorageImpl() {

      storageCellsManager = new StorageCellsManager();
   }


   /*
    * (non-Javadoc)
    * 
    * @see org.cacheonix.impl.storage.DiskStorage#initialize(java.lang.Object)
    */
   public final boolean initialize(final Object startupInfo) throws StorageException {

      return storageCellsManager != null && storageCellsManager.initialize(startupInfo);
   }


   /*
    * (non-Javadoc)
    * 
    * @see org.cacheonix.impl.storage.DiskStorage#get(java.lang.Object)
    */
   public Object get(final Object key) throws StorageException {

      Object oRes = null;
      String failError = null;

      try {
         if (key instanceof StoredObject) {
            final long position = ((StoredObject) key).getValueOffset();
            final Object obj = storageCellsManager.retrieveValue(position);
            if (obj != null) {
               oRes = ((Object[]) obj)[1];
            }

            if (oRes == null) {
               failError = "Retrieved from DiskStorage object is null";
            }
         } else {
            failError = "key object is not of StoredObject type";
         }
      } catch (final IOException ioe) {
         throw new StorageException(ioe);
      }

      if (oRes == null) {
         throw new StorageException(failError);
      }

      return oRes;
   }


   /*
    * (non-Javadoc)
    * 
    * @see org.cacheonix.impl.storage.DiskStorage#getName()
    */
   public String getName() throws StorageException {

      return storageCellsManager.getName();
   }


   /**
    * (non-Javadoc)
    *
    * @throws StorageException if there was a error storing item on disk.
    * @see DiskStorage#put(Object, Object)
    */
   public StoredObject put(final Object key, final Object value) throws StorageException {

      StoredObject obj = null;

      try {
         final long position = storageCellsManager.recordCells(key, value);
         if (position != -1L) {
            obj = new StoredObjectImpl(position, -1L);
         }
      } catch (final IOException e) {
         throw new StorageException(e);
      }

      if (obj == null) {
         throw new StorageException("Recording object to storage fails and StoredObject is null");
      }

      return obj;
   }


   /**
    * (non-Javadoc)
    *
    * @throws IOException
    * @see DiskStorage#remove(Object)
    */
   public boolean remove(final Object key) throws IOException {

      boolean bRes = false;

      if (key instanceof StoredObject) {
         final long position = ((StoredObject) key).getValueOffset();
         storageCellsManager.removeCells(position);
         bRes = true;
      } else {
         LOG.debug("Key object is not of StoredObject type. Nothing removed.");
      }

      return bRes;
   }


   /*
    * (non-Javadoc)
    * 
    * @see org.cacheonix.impl.storage.DiskStorage#shutdown(boolean)
    */
   public void shutdown(final boolean deleteStorageContents) {

      try {

         storageCellsManager.shutdown(deleteStorageContents);
      } catch (final Exception ex) {

         LOG.error("Got Exception during shutdown: " + StringUtils.toString(ex), ex);
      }
   }


   public long size() throws StorageException {

      return storageCellsManager.getSize();
   }


   public final String toString() {

      return "DiskStorageImpl{" + "storageCellsManager=" + storageCellsManager + '}';
   }


   /*
    * (non-Javadoc)
    * 
    * @see org.cacheonix.impl.storage.DiskStorage#restore(java.lang.Object)
    */
   public Object restore(final Object key) throws StorageException {

      Object oRes = null;
      String failError = null;

      try {
         if (key instanceof StoredObject) {
            final long position = ((StoredObject) key).getValueOffset();
            final Object obj = storageCellsManager.restoreValue(position);
            if (obj != null) {
               oRes = ((Object[]) obj)[1];
            }

            if (oRes == null) {
               failError = "Retrieved from DiskStorage object is null";
            }
         } else {
            failError = "key object is not of StoredObject type";
         }
      } catch (final IOException ioe) {
         throw new StorageException(ioe);
      }

      if (oRes == null) {
         throw new StorageException(failError);
      }

      return oRes;
   }


   /**
    * Clears the storage from all objects.
    *
    * @throws StorageException if storage cannot be cleaned.
    */
   public void clear() throws StorageException {

      storageCellsManager.clearStorage();
   }
}
