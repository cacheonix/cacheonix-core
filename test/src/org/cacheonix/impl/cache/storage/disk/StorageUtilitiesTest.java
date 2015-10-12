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

import junit.framework.TestCase;

/**
 * Tests {@link StorageUtilities}
 */
public final class StorageUtilitiesTest extends TestCase {

   private static final String key = "testKey";

   private static final String data = "testData";

   private static final String diskStorageName = "DiskStorageTestName";

   private static final Object[] pairOrg = new Object[2];

   // TODO @SF - Decide what will be initialization object (File Name?
   // Pathname?...) and how to get it


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      pairOrg[0] = key;
      pairOrg[1] = data;
   }


   public String toString() {

      return "StorageUtilitiesTest{" + "diskStorage=" + diskStorageName + '}';
   }


   // /////////////////////////////////////////////////////////////


   public void testSerialize() throws IOException, ClassNotFoundException {

      final byte[] resA = StorageUtilities.serialize(pairOrg);
      final Object objRes = StorageUtilities.deserialize(resA);

      assertTrue(objRes instanceof Object[]);
      if (objRes instanceof Object[]) {
         final String keyRes = (String) ((Object[]) objRes)[0];
         final String dataRes = (String) ((Object[]) objRes)[1];
         assertEquals(key, keyRes);
         assertEquals(data, dataRes);
      }
   }


   public void testDeserialize() throws IOException, ClassNotFoundException {

      final byte[] resA = StorageUtilities.serialize(pairOrg);
      final Object objRes = StorageUtilities.deserialize(resA);
      assertTrue(objRes instanceof Object[]);
      if (objRes instanceof Object[]) {
         final String keyRes = (String) ((Object[]) objRes)[0];
         final String dataRes = (String) ((Object[]) objRes)[1];
         assertEquals(key, keyRes);
         assertEquals(data, dataRes);
      }
   }


   public void testCalculateNumberOfCells() {

      final int[] cellSize = {32, 345, 23};

      final int headerSize = StorageConstants.STORAGE_CELL_HEADER_SIZE;

      for (int i = 0; i < cellSize.length; ++i) {
         // Define Scope!!!
         final int[] nofCells = {15, 64, 1};
         final byte[] src = new byte[cellSize[i] * nofCells[i] - headerSize * nofCells[i]
                 - StorageConstants.STORAGE_DATA_LENGTH_SIZE];
         final int nofCellsCalculated1 = StorageUtilities.calculateNumberOfCells(src, cellSize[i]);
         assertEquals(nofCells[i], nofCellsCalculated1);

         // Define Scope!!!
         final byte[] src1 = new byte[cellSize[i] * nofCells[i]];
         final int nofCellsCalculated2 = StorageUtilities.calculateNumberOfCells(src1, cellSize[i]);
         final int temp1 = nofCells[i] * (cellSize[i] - headerSize) + nofCells[i] * headerSize
                 + StorageConstants.STORAGE_DATA_LENGTH_SIZE;
         final int expectedCells2 = temp1 / (cellSize[i] - headerSize) + (temp1 % (cellSize[i] - headerSize) == 0 ? 0 : 1);
         assertEquals(expectedCells2, nofCellsCalculated2);
      }

   }


   public void testObjectPacker() throws IOException {

      final byte[] packed = StorageUtilities.objectPacker(key, data);
      final Object[] unpacked = StorageUtilities.objectUnpacker(packed);

      assertEquals(key, unpacked[0]);
      assertEquals(data, unpacked[1]);
   }


   public void testLongToByteArray() {
      // 1
      final long tst1 = 0xAAAAAAAAAAAAAAAAL;
      final byte[] tstA1 = StorageUtilities.longToByteArray(tst1);
      assertEquals(8, tstA1.length);
      final long chk1 = StorageUtilities.byteArrayToLong(tstA1, 0);
      assertEquals(tst1, chk1);
      // 2
      final long tst2 = -1L;
      final byte[] tstA2 = StorageUtilities.longToByteArray(tst2);
      assertEquals(8, tstA2.length);
      final long chk2 = StorageUtilities.byteArrayToLong(tstA2, 0);
      assertEquals(tst2, chk2);
      // 3
      final long tst3 = 0L;
      final byte[] tstA3 = StorageUtilities.longToByteArray(tst3);
      assertEquals(8, tstA3.length);
      final long chk3 = StorageUtilities.byteArrayToLong(tstA3, 0);
      assertEquals(tst3, chk3);
   }


   public void testControlInfoToByteArray1() {

      final long position = 3456L;
      final byte controlByte = (byte) 255;
      final byte[] tstA = StorageUtilities.controlInfoToByteArray(controlByte, position);

      assertEquals(controlByte, tstA[0]);

      final byte[] chkA = new byte[8];
      System.arraycopy(tstA, 1, chkA, 0, 8);

      final long chkpos = StorageUtilities.byteArrayToLong(chkA, 0);

      assertEquals(position, chkpos);
   }


   public void testControlInfoToByteArray2() {

      // Initialize Array
      final byte[] tstA = new byte[32 * 8];
      for (int i = 0; i < 32 * 8; ++i) {
         tstA[i] = (byte) 0xAA;
      }

      int offset = 0;
      final byte controlByte = (byte) 255;
      final long position = 3456L;
      StorageUtilities.controlInfoToByteArray(tstA, offset, controlByte, position);
      boolean testPass = controlByte == tstA[offset];
      if (testPass) {
         final byte[] chkposB = new byte[8];
         System.arraycopy(tstA, offset + 1, chkposB, 0, 8);
         final long chkPosition = StorageUtilities.byteArrayToLong(chkposB, 0);
         testPass = position == chkPosition;
         if (testPass) {
            for (int i = offset + 9; i < offset + 32; ++i) {
               testPass = (tstA[i] & 0xFF) == 0xAA;
               if (!testPass) {
                  break;
               }
            }
         }
      }
      assertTrue(testPass);

      offset = 32 * 4;
      StorageUtilities.controlInfoToByteArray(tstA, offset, controlByte, position);
      testPass = controlByte == tstA[offset];
      if (testPass) {
         final byte[] chkposB = new byte[8];
         System.arraycopy(tstA, offset + 1, chkposB, 0, 8);
         final long chkPosition = StorageUtilities.byteArrayToLong(chkposB, 0);
         testPass = position == chkPosition;
         if (testPass) {
            for (int i = offset + 9; i < offset + 32; ++i) {
               testPass = (tstA[i] & 0xFF) == 0xAA;
               if (!testPass) {
                  break;
               }
            }
         }
      }
      assertTrue(testPass);

      offset = 32 * 7;
      StorageUtilities.controlInfoToByteArray(tstA, offset, controlByte, position);
      testPass = controlByte == tstA[offset];
      if (testPass) {
         final byte[] chkposB = new byte[8];
         System.arraycopy(tstA, offset + 1, chkposB, 0, 8);
         final long chkPosition = StorageUtilities.byteArrayToLong(chkposB, 0);
         testPass = position == chkPosition;
         if (testPass) {
            for (int i = offset + 9; i < offset + 32; ++i) {
               testPass = (tstA[i] & 0xFF) == 0xAA;
               if (!testPass) {
                  break;
               }
            }
         }
      }
      assertTrue(testPass);
   }
}
