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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cacheonix.impl.util.logging.Logger;

/**
 * <b>StorageCellsManager</b> class is singleton which manipulates with file in the file system
 *
 * @author sfichel@cacheonix.org
 */
public final class StorageCellsManager {

   private static final Logger LOGGER = Logger.getLogger(StorageCellsManager.class);

   // Indicates that file should be left on disk after exiting from the
   // application
   private final boolean persistenceRequired = false; // NOPMD

   // cluster size in bytes
   private final long storageCellSize = StorageConstants.DEFAULT_STORAGE_CLUSTER_SIZE; // Default

   // number of clusters
   private final long numberOfStorageCells = StorageConstants.DEFAULT_NUMBER_OF_CLUSTERS; // Default

   // ///////////////////////////////////////////////////////////////////////////////////////////

   private String storageName = null;

   // Increment storage size
   private long storageSizeIncrement = storageCellSize * numberOfStorageCells;

   // Calculate default DiskStorage file size
   private long storageSize = storageCellSize * numberOfStorageCells;

   // Internal List of Free Cells
   private final List freeStorageCellsList = new ArrayList(11);

   // Random Access File for storing DATA
   private RandomAccessFile fileSystemStorage = null;

   public String filePath = "tempdata.cxd";

   private File randomFile = null;

   private boolean initialized = false;

   // Locking simultaneous access to the same file
   // Remember, each cache has own file and own lock for file access. They do
   // not intersect.
   private final Lock lock = new ReentrantLock();

   // ////////////////////////////////////////////////////////////////////////////////////////////


   public StorageCellsManager() {

   }


   public StorageCellsManager(final Object startupObject) throws StorageException {

      if (!initialize(startupObject)) {
         throw new StorageException("StorageCellManager Initialization failed.");
      }
   }

   // ///////////////////////////////////////////////////////////////////////////////////////////


   /**
    * While initialize Storage File header will be read and tested if StorageCell size is not the same as set in the
    * File Header Read the file, then EMPTY Contents.
    *
    * @return <code>true</code> if successful or <code>false</code> if fail quite
    * @throws StorageException
    * @noinspection RedundantIfStatement
    */
   public/* synchronized */boolean initialize(final Object startupObject) throws StorageException {

      lock.lock();
      try {
         if (!initialized) {
            final Object[] startItems = (Object[]) startupObject;
            storageName = (String) startItems[0];
            storageSize = (Long) startItems[1];
            filePath = (String) startItems[2];

            randomFile = new File(filePath);

            final long numberOfCellsIncrement = (storageSize + storageCellSize - 1L) / storageCellSize;
            storageSizeIncrement = numberOfCellsIncrement * storageCellSize;

            final long numberOfCells;
            final boolean bRescanFile;
            if (randomFile.exists()) {

               // Will throw Exception if verification fail
               verifyForCorrectFileType(randomFile);

               storageSize = randomFile.length(); // Get existing DiskStorage size
               numberOfCells = storageSize / storageCellSize;

               if (persistenceRequired) {
                  bRescanFile = true;
               } else {
                  bRescanFile = false;
               }
            } else {
               numberOfCells = numberOfCellsIncrement;
               storageSize = storageSizeIncrement;
               bRescanFile = false;
            }

            fileSystemStorage = new RandomAccessFile(randomFile, "rw");

            if (bRescanFile) {
               scanFileForFreeCells(numberOfCells);
            } else {
               fileSystemStorage.setLength(storageSize);
               resetFreeCellsList(numberOfCells);
            }

            // Record could be:
            // Control - Byte | long - Next Cluster in sequence or 0 | [Key
            // Bytes
            // part 1-n] | Contents Bytes - [part 1-n]

            initialized = true;

            LOGGER.debug("Disk DiskStorage '" + storageName + "' with size '" + storageSize + "' and location '" + filePath
                    + "' was instantiated successfully.");
         }
      } catch (final Exception fnf) {
         throw new StorageException("Cannot Initialize StorageCellsManager", fnf);
      } finally {
         lock.unlock();
      }

      return initialized;
   }


   private void verifyForCorrectFileType(final File file) throws StorageException, IOException {

      boolean bVerificationFail = false;
      final StringBuilder strError = new StringBuilder(100).append("Desired storage file with name '").append(file.getCanonicalPath()).append('\'');

      if (!file.canRead()) {
         bVerificationFail = true;
         strError.append(" is not readable.");
      } else if (!file.canWrite()) {
         bVerificationFail = true;
         strError.append(" is not writable.");
      } else if (file.isDirectory()) {
         bVerificationFail = true;
         strError.append(" is a directory not a file.");
      }

      if (bVerificationFail) {
         throw new StorageException(strError.toString());
      }
   }


   /**
    * <b>resetFreeCellsList</b> resets internal list of free cells
    *
    * @param numberOfCells - number of free cells to put to freeStorageCellsList List
    */
   private void resetFreeCellsList(final long numberOfCells) {

      freeStorageCellsList.clear();
      for (long i = 0L; i < numberOfCells; ++i) {
         final Long FreeItem = Long.valueOf(i);
         freeStorageCellsList.add(FreeItem);
      }
   }


   public String getName() throws StorageException {

      if (!initialized) {
         throw new StorageException("StorageCellManager is not initialized yet");
      }

      return storageName;
   }


   public long getSize() throws StorageException {

      if (!initialized) {
         throw new StorageException("StorageCellManager is not initialized yet");
      }

      return storageSize;
   }


   /**
    * <b>shutdown</b> - closing and cleaning (when indicated by deleteStorageContents) Storage resource
    *
    * @param deleteStorageContents - boolean which indicates if storage should be erased after shutdown (true) or
    *                              persistent (false)
    */
   public/* synchronized */void shutdown(final boolean deleteStorageContents)
           throws IOException, StorageException {

      if (!initialized) {
         throw new StorageException("StorageCellManager is not initialized yet");
      }

      lock.lock();
      try {
         fileSystemStorage.close();
         if (deleteStorageContents) {
            randomFile.delete();
         }
      } finally {

         lock.unlock();
         initialized = false;
         storageName = null;
         storageSize = 0L;
         filePath = null;
         randomFile = null;
      }
   }

   // TODO @SF - Can make this more complicated but faster using observer thread
   // Observer thread should be switched on when initial big FreeMem regions are
   // gone
   // Observer thread should keep ordered by region size list of free regions.
   // (map by sizes)


   /**
    * <b>checkFreeCellsAvalability</b> checks size of freeStorageCellsList list
    */
   public int checkFreeCellsAvalability() {

      return freeStorageCellsList.size();
   }


   /**
    * <b>allocateFreeCells</b> allocates required number of free cells
    *
    * @param nofCells - number of required free cells
    * @return List with allocated cells
    */
   private List allocateFreeCells(final int nofCells) throws IOException, StorageFullException {

      int nFree = checkFreeCellsAvalability();

      while (nFree < nofCells) {
         incrementFileSize();
         nFree = checkFreeCellsAvalability();
      }

      List aRes = null;
      if (nFree >= nofCells) {
         aRes = new ArrayList(nofCells);
         // Get From the top and put into TEMP return List
         for (int i = 0; i < nofCells; ++i) {
            final Long cellNo = (Long) freeStorageCellsList.get(0);
            aRes.add(cellNo);
            freeStorageCellsList.remove(0);
         }
      } else {
         // In case something fail... Throw!
         throw new StorageFullException("Number of free cells less than number of required cells");
      }

      return aRes;
   }


   public void incrementFileSize() throws IOException {
      // Get Last block (At present storageSize)
      final long nLastCell = storageSize / storageCellSize;
      final long numberOfBlocks = storageSizeIncrement / storageCellSize;
      storageSize += storageSizeIncrement;
      // SetFileSize...
      fileSystemStorage.setLength(storageSize);
      // Populate free cells to free Cells Array
      final long finalSize = nLastCell + numberOfBlocks;
      for (long l = nLastCell; l < finalSize; ++l) {
         final Long FreeItem = Long.valueOf(l);
         freeStorageCellsList.add(FreeItem);
      }
   }


   /**
    * <b>recordCells</b> stores key and value to the disk storage
    *
    * @param key   - key of the object. Should be serializable
    * @param value - value of the object. Should be serializable
    * @return - <u>long</u> position
    * @throws IOException
    * @throws StorageException
    */
   public/* synchronized */long recordCells(final Object key, final Object value)
           throws IOException, StorageException {

      if (!initialized) {
         throw new StorageException("StorageCellManager is not initialized yet");
      }

      long positionRes = -1L;
      lock.lock();
      try {
         if (key != null && value != null) {
            final byte[] tempBytes = StorageUtilities.objectPacker(key, value);

            if (tempBytes != null) {
               final int nCells = StorageUtilities.calculateNumberOfCells(tempBytes, (int) storageCellSize);
               final List selectedCells = allocateFreeCells(nCells);
               positionRes = writeToStorage(selectedCells, tempBytes);
            } else {
               throw new StorageException("Error serializing key and value to bytes");
            }
         } else {
            throw new StorageException("Key and Value cannot be null");
         }
      } finally {
         lock.unlock();
      }

      return positionRes;
   }


   /**
    * <b>retrieveValue</b> retrieves value from Storage. Value in storage will not be touched.
    *
    * @param position
    * @return
    * @throws IOException, StorageException
    */
   public/* synchronized */Object retrieveValue(final long position)
           throws IOException, StorageException {

      if (!initialized) {
         throw new StorageException("StorageCellManager is not initialized yet");
      }

      lock.lock();
      try {

         final byte[] buffer = new byte[(int) storageCellSize]; // Long size

         long offset = position;
         int lenOfData = StorageConstants.STORAGE_CELL_HEADER_SIZE;

         final ByteArrayOutputStream readData = new ByteArrayOutputStream();

         int i = 0;

         while (offset != -1L && lenOfData > 0) {
            fileSystemStorage.seek(offset);
            fileSystemStorage.read(buffer); // read next position

            // Get Control byte
            final byte controlByte = buffer[0];
            if ((controlByte & StorageConstants.STORAGE_CELL_OCCUPIED) != (int) StorageConstants.STORAGE_CELL_OCCUPIED) {
               throw new StorageException("Control byte indicates not used STORAGE CELL.");
            }

            offset = StorageUtilities.byteArrayToLong(buffer, 1); // get next
            // storage
            // cell
            // position

            int usefulLen = (int) (storageCellSize - (long) StorageConstants.STORAGE_CELL_HEADER_SIZE);
            int dataOffset = StorageConstants.STORAGE_CELL_HEADER_SIZE;

            if (i == 0) {
               lenOfData = StorageUtilities.byteArrayToInt(buffer, dataOffset);
               usefulLen -= StorageConstants.STORAGE_DATA_LENGTH_SIZE;
               dataOffset += StorageConstants.STORAGE_DATA_LENGTH_SIZE;
            }
            ++i;

            if (usefulLen > lenOfData) {
               usefulLen = lenOfData;
            }

            readData.write(buffer, dataOffset, usefulLen);

            lenOfData -= usefulLen;
         }

         final byte[] tempData = readData.toByteArray();

         final Object oRes = StorageUtilities.objectUnpacker(tempData);

         if (oRes == null) {
            throw new StorageException("Retrieved Object is null");
         }

         return oRes;
      } finally {
         lock.unlock();
      }
   }


   /**
    * @param position
    * @throws IOException
    */
   public synchronized void removeCells(final long position) throws IOException {

      final byte[] buffer = new byte[8]; // Long size
      final byte controlByte = StorageConstants.STORAGE_CELL_FREE;
      long offset = position;

      while (offset != -1L) {
         fileSystemStorage.seek(offset);
         fileSystemStorage.write((int) controlByte); // write control byte and
         // move marker to next
         // position

         // Convert offset to StorageCell item.
         final long currentStorageCell = offset / storageCellSize;
         final Long returnCell = Long.valueOf(currentStorageCell);

         // Return StorageCell to FreeStorageCellList
         freeStorageCellsList.add(returnCell);

         // Get and see next position if not end
         fileSystemStorage.read(buffer); // read next position
         offset = StorageUtilities.byteArrayToLong(buffer, 0);
      }
   }


   /**
    * Clearing Storage (Formatting)
    *
    * @throws StorageException
    */
   public/* synchronized */void clearStorage() throws StorageException {

      if (!initialized) {
         throw new StorageException("StorageCellManager is not initialized yet");
      }

      lock.lock();
      try {
         clearAll();
      } catch (final IOException ioe) {
         throw new StorageException(ioe);
      } finally {
         lock.unlock();
      }
   }


   /**
    * Clearing Storage (Formatting) internal function
    *
    * @throws IOException
    */
   private void clearAll() throws IOException {

      final long numberOfCells = (storageSize + storageCellSize - 1L) / storageCellSize;

      try {
         long offset = 0L;
         final byte controlByte = StorageConstants.STORAGE_CELL_FREE;
         for (int i = 0; i < (int) numberOfCells; ++i) {
            offset = (long) i * storageCellSize;
            fileSystemStorage.seek(offset);
            fileSystemStorage.write((int) controlByte); // write control byte
            // and move marker to
            // next
         }
      } finally {
         resetFreeCellsList(numberOfCells);
      }
   }


   /**
    * <b>restoreValue</b> reads the value from storage and remove item from storage safely
    *
    * @param position - <u> long </u> position in the disk
    * @return value object
    * @throws IOException, StorageException
    */
   public/* synchronized */Object restoreValueSafely(final long position)
           throws IOException, StorageException {

      if (!initialized) {
         throw new StorageException("StorageCellManager is not initialized yet");
      }

      lock.lock();
      try {
         Object oRes = null;

         try {
            oRes = retrieveValue(position);

            if (oRes == null) {
               throw new StorageException("Retrieved Object is null");
            }
         } finally {
            try {
               removeCells(position);
            } catch (final IOException e) {
               LOGGER.error("Exception removing storage cells", e);
            }
         }

         return oRes;
      } finally {
         lock.unlock();
      }
   }


   /**
    * <b>restoreValue</b> reads the value from storage and remove item from storage
    *
    * @param position - <u> long </u> position in the disk
    * @return value object
    * @throws IOException
    */
   public/* synchronized */Object restoreValue(final long position)
           throws IOException, StorageException {

      if (!initialized) {
         throw new StorageException("StorageCellManager is not initialized yet");
      }

      lock.lock();
      try {

         final byte[] buffer = new byte[(int) storageCellSize - 1]; // Long
         // size -
         // CONTROL
         // BYTE (1)
         long offset = position;
         int lenOfData = StorageConstants.STORAGE_CELL_HEADER_SIZE;

         final ByteArrayOutputStream readData = new ByteArrayOutputStream();

         final byte controlByte = StorageConstants.STORAGE_CELL_FREE;

         int i = 0;

         while (offset != -1L && lenOfData > 0) {
            fileSystemStorage.seek(offset);

            // Assume ControlByte is in correct state.
            // In case of any doubt use restoreValueSafely
            // RECORD Control Byte first and Move Marker 1 byte
            fileSystemStorage.write((int) controlByte); // write control byte
            // and move marker to
            // next

            // Convert offset to StorageCell item.
            final long currentStorageCell = offset / storageCellSize;

            final Long returnCell = Long.valueOf(currentStorageCell);
            // Return StorageCell to FreeStorageCellList
            freeStorageCellsList.add(returnCell);

            // CONTINUE READING from the Marker position (Offset + 1)
            // Get whole block without ControlByte and see next position if not
            // end
            fileSystemStorage.read(buffer); // read next position

            offset = StorageUtilities.byteArrayToLong(buffer, 0); // get next
            // storage
            // cell
            // position

            int usefulLen = (int) (storageCellSize - (long) StorageConstants.STORAGE_CELL_HEADER_SIZE);
            int dataOffset = StorageConstants.STORAGE_CELL_HEADER_SIZE - 1; // 1 is
            // ControlByte
            // (which is not in
            // buffer)

            if (i == 0) {
               lenOfData = StorageUtilities.byteArrayToInt(buffer, dataOffset);
               usefulLen -= StorageConstants.STORAGE_DATA_LENGTH_SIZE;
               dataOffset += StorageConstants.STORAGE_DATA_LENGTH_SIZE;
            }
            ++i;

            if (usefulLen > lenOfData) {
               usefulLen = lenOfData;
            }

            readData.write(buffer, dataOffset, usefulLen);

            lenOfData -= usefulLen;
         }

         final byte[] tempData = readData.toByteArray();

         final Object oRes = StorageUtilities.objectUnpacker(tempData);

         if (oRes == null) {
            throw new StorageException("Retrieved Object is null");
         }

         return oRes;
      } finally {
         lock.unlock();
      }
   }


   /**
    * <b>writeToStorage</b> writes Storage Cells to the File
    *
    * @param selectedCells - List of Cells to be written to file
    * @param data          - byte array to be recorded to disk
    * @return - <u>long</u> postion of the first cell on disk
    * @throws StorageException
    */
   private long writeToStorage(final List selectedCells, final byte[] data)
           throws StorageException {

      final long position;

      try {
         // REVIEWME: @SF -> Preferred to put into SortedArray (Queue or like that)
         // and then flush it to disk
         final int lastItem = selectedCells.size() - 1;
         long currentCell = (Long) selectedCells.get(0);
         position = storageCellSize * currentCell;
         int lengthToRecord = data.length;

         final byte[] buffer = new byte[(int) storageCellSize];

         int useful_length = (int) (storageCellSize - (long) StorageConstants.STORAGE_CELL_HEADER_SIZE);
         for (int i = 0; i < lastItem + 1; ++i) {
            Arrays.fill(buffer, (byte) 0);
            currentCell = (Long) selectedCells.get(i);
            long nextCell = StorageConstants.FINAL_STORAGECELL_IN_SEQUENCE;
            long nextPos = nextCell;
            if (i < lastItem) {
               nextCell = (Long) selectedCells.get(i + 1);
               nextPos = storageCellSize * nextCell;
            }

            // Calculate position on the device using Block size
            final long currentPos = storageCellSize * currentCell;

            final byte controlByte = StorageConstants.STORAGE_CELL_OCCUPIED;
            // Transfer control info
            // Record data object length if first block
            int dataOffset = StorageConstants.STORAGE_CELL_HEADER_SIZE;
            int offset = 0;

            if (i <= 0) {
               StorageUtilities.controlInfoToByteArray(buffer, 0, controlByte, nextPos, data.length);
               dataOffset = StorageConstants.STORAGE_CELL_HEADER_SIZE + StorageConstants.STORAGE_DATA_LENGTH_SIZE;
               useful_length = (int) (storageCellSize - (long) dataOffset);
            } else {
               StorageUtilities.controlInfoToByteArray(buffer, 0, controlByte, nextPos);
               useful_length = (int) (storageCellSize - (long) dataOffset);
               offset = useful_length - 4 + (i - 1) * useful_length;
            }

            if (lengthToRecord < useful_length) {
               useful_length = lengthToRecord;
            }
            // Transfer data bytes into buffer;
            StorageUtilities.byteArrayCopy(buffer, dataOffset, data, offset, useful_length);

            lengthToRecord -= useful_length;

            // position Marker
            fileSystemStorage.seek(currentPos);
            fileSystemStorage.write(buffer);
         }
      } catch (final Exception e) {
         clearCells(selectedCells);
         throw new StorageException(e);
      }

      return position;
   }


   /**
    * ClearCells record {@link StorageConstants.STORAGE_CELL_FREE} control byte for cells previously allocated for
    * writing. <p/> It has presumption, that if writing got IOError this IOError will appear while recording control
    * byte. System will do best effort recording (Ignore error and continue writing)
    *
    * @param selectedCells - <u>List</u> of cells allocated for writing
    */
   private void clearCells(final List selectedCells) {

      try {
         if (selectedCells != null && !selectedCells.isEmpty()) {

            final byte[] buffer = new byte[StorageConstants.STORAGE_CELL_HEADER_SIZE]; // Long
            // size
            final byte controlByte = StorageConstants.STORAGE_CELL_FREE;
            final long nextPos = StorageConstants.FINAL_STORAGECELL_IN_SEQUENCE;

            for (final Object currentCell : selectedCells) {
               if (currentCell instanceof Long) {
                  final long offset = (Long) currentCell * storageCellSize;
                  if (offset >= 0L) {
                     try {
                        StorageUtilities.controlInfoToByteArray(buffer, 0, controlByte, nextPos);
                        fileSystemStorage.seek(offset);
                        fileSystemStorage.write(buffer); // write control byte
                        // and next position
                     } catch (final IOException ex) {
                        LOGGER.debug("Problem recording control information to the disk", ex);
                     } catch (final Exception e) {
                        LOGGER.debug("Problem recording control information to the disk", e);
                     } finally {
                        freeStorageCellsList.add(currentCell); // Return back to
                        // the list
                     }
                  } else {
                     LOGGER
                             .debug("Retrived current cell contains negative index value. Cell is ignored and not returned to the FreeCellsList.");
                  }
               } else {
                  LOGGER.debug("Current cell either NULL or is not of Long type");
               }
            }
         } else {
            LOGGER.debug("No free cells to clear");
         }
      } catch (final Exception el) {
         LOGGER.error("Exception Clearing Cells", el);
      }
   }


   /**
    * @param numberOfCells
    */
   // REVIEWME @SF -> Implement initial file scanning for free blocks
   private void scanFileForFreeCells(final long numberOfCells) {

      resetFreeCellsList(numberOfCells);
   }


   @SuppressWarnings("ObjectToString")
   public String toString() {

      return "StorageCellsManager{" + "persistenceRequired=" + persistenceRequired + ", storageCellSize=" + storageCellSize
              + ", numberOfStorageCells=" + numberOfStorageCells + ", storageName='" + storageName + '\'' + ", storageSize="
              + storageSize + ", freeStorageCellsList=" + freeStorageCellsList.size() + ", fileSystemStorage=" + fileSystemStorage
              + ", filePath='" + filePath + '\'' + ", randomFile=" + randomFile + ", initialized=" + initialized + '}';
   }
}
