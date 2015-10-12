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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * <b>StorageUtility</b> class - Converts Object to and From byte[] <p/> <b>StorageUtility</b> class contains only
 * static methods to convert from byte[] to Object and from Object to byte[]. Also contains calculation functions
 */
public final class StorageUtilities {

   private static final Logger LOGGER = Logger.getLogger(StorageUtilities.class);


   // Lock Constructor (Cannot be instantiated
   private StorageUtilities() {

   }


   public static byte[] serialize(final Object obj) throws IOException {

      final byte[] resA;
      final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      final ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
      try {
         objStream.writeObject(obj);
         resA = byteStream.toByteArray();
      } finally {
         IOUtils.closeHard(objStream);
      }

      if (resA == null) {
         throw new IOException("Cannot Serialize Object");
      }

      return resA;
   }


   public static Object deserialize(final byte[] block) throws IOException, ClassNotFoundException {

      final Object resSc;

      final ByteArrayInputStream ibyteStream = new ByteArrayInputStream(block);
      final BufferedInputStream inputStream = new BufferedInputStream(ibyteStream);
      final ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
      try {
         resSc = objInputStream.readObject();
      } finally {
         IOUtils.closeHard(inputStream);
      }

      if (resSc == null) {
         throw new IOException("Cannot deserialize object from byte[]");
      }

      return resSc;
   }


   /**
    * CalculateNumberOfCells - calculates number of StorageCells required to store byte Array
    *
    * @param Source
    * @param blockSize
    * @return number of cells or 0 if Source byte array is null or empty
    */
   public static int calculateNumberOfCells(final byte[] Source, final int blockSize) {

      final int nRes;

      if (Source != null && Source.length >= 0) {
         final int adjustedBlockSize = blockSize - StorageConstants.STORAGE_CELL_HEADER_SIZE;
         nRes = (Source.length + StorageConstants.STORAGE_DATA_LENGTH_SIZE) / adjustedBlockSize
                 + ((Source.length + StorageConstants.STORAGE_DATA_LENGTH_SIZE) % adjustedBlockSize == 0 ? 0 : 1);
      } else {
         nRes = 0;
         LOGGER.info("Source is empty, so number of cells required is 0");
      }

      return nRes;
   }


   public static byte[] objectPacker(final Object key, final Object value) throws IOException {

      final Object[] objAres = new Object[2];

      objAres[0] = key;
      objAres[1] = value;

      return serialize(objAres);
   }


   public static Object[] objectUnpacker(final byte[] bytes) throws IOException {

      Object[] resA = null;

      try {
         resA = (Object[]) deserialize(bytes);
      } catch (final ClassNotFoundException ex) {
         LOGGER.debug("Object Unpacker failed ", ex);
      }

      return resA;
   }


   // /////////////////////////////////////////////////////////////////////////////////////
   // TODO @SF - ERROR See Exception Handling here
   public static byte[] longToByteArray(final long nVal) {

      final byte[] bArray = new byte[8];
      final ByteBuffer bBuffer = ByteBuffer.wrap(bArray);
      final LongBuffer lBuffer = bBuffer.asLongBuffer();
      lBuffer.put(0, nVal);
      return bArray;
   }


   public static byte[] controlInfoToByteArray(final byte controlByte, final long position) {

      final byte[] bArray = new byte[9];
      final ByteBuffer bBuffer = ByteBuffer.wrap(bArray, 1, 8);
      final LongBuffer lBuffer = bBuffer.asLongBuffer();
      lBuffer.put(0, position);
      bArray[0] = controlByte;

      return bArray;
   }


   public static void controlInfoToByteArray(final byte[] data, final int offset,
                                             final byte controlByte, final long position) {

      data[offset] = controlByte;
      final ByteBuffer bBuffer = ByteBuffer.wrap(data, offset + 1, 8);
      final LongBuffer lBuffer = bBuffer.asLongBuffer();
      lBuffer.put(0, position);
   }


   public static void controlInfoToByteArray(final byte[] data, final int offset,
                                             final byte controlByte, final long position,
                                             final int length) {

      data[offset] = controlByte;
      final ByteBuffer bBuffer = ByteBuffer.wrap(data, offset + 1, StorageConstants.STORAGE_CELL_MARKER_SIZE);
      final LongBuffer lBuffer = bBuffer.asLongBuffer();
      lBuffer.put(0, position);
      final ByteBuffer wBuffer = ByteBuffer.wrap(data, offset + StorageConstants.STORAGE_CELL_HEADER_SIZE,
              StorageConstants.STORAGE_DATA_LENGTH_SIZE);
      wBuffer.putInt(length);
   }


   public static long byteArrayToLong(final byte[] buffer, final int offset) {

      final ByteBuffer bBuffer = ByteBuffer.wrap(buffer, offset, 8);
      final LongBuffer lBuffer = bBuffer.asLongBuffer();
      return lBuffer.get(0);
   }


   public static int byteArrayToInt(final byte[] buffer, final int offset) {

      final ByteBuffer bBuffer = ByteBuffer.wrap(buffer, offset, 4);
      final IntBuffer iBuffer = bBuffer.asIntBuffer();
      return iBuffer.get(0);
   }


   public static void byteArrayCopy(final byte[] dst, final int dstOffset, final byte[] src,
                                    final int srcOffset, final int length) {

      final ByteBuffer dstBuffer = ByteBuffer.wrap(dst, dstOffset, length);
      dstBuffer.put(src, srcOffset, length);
   }
}
