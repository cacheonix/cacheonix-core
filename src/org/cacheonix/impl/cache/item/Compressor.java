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
package org.cacheonix.impl.cache.item;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Compressor of byte arrays.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Dec 16, 2008 10:08:28 PM
 */
final class Compressor {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Compressor.class); // NOPMD

   private static final String OBJECT_ZIP_ENTRY = "object";

   private static final Compressor instance = new Compressor();


   /**
    * Singleton constructor.
    */
   private Compressor() {

   }


   /**
    * @return Singleton instance.
    */
   public static Compressor getInstance() {

      return instance;
   }


   /**
    * Compresses given byte array by creating a zip output stream with entry name {@link #OBJECT_ZIP_ENTRY}.
    *
    * @param bytes bytes to compress
    * @return byte array with compressed source.
    * @throws IOException if there is an unrecoverable problem while compressing bytes.
    * @see #decompress(byte[])
    */
   public byte[] compress(final byte[] bytes) throws IOException {

      ByteArrayOutputStream baos = null;
      ZipOutputStream zos = null;
      ByteArrayInputStream bais = null;
      try {
         baos = new ByteArrayOutputStream();
         bais = new ByteArrayInputStream(bytes);
         zos = new ZipOutputStream(baos);
         // Create entry as required by ZipOutputStream
         final ZipEntry entry = new ZipEntry(OBJECT_ZIP_ENTRY);
         zos.putNextEntry(entry);
         IOUtils.copyInputToOutputStream(bais, zos);
         zos.flush();
         baos.flush();
         zos.closeEntry();
         return baos.toByteArray();
      } finally {
         IOUtils.closeHard(bais);
         IOUtils.closeHard(baos);
         IOUtils.closeHard(zos);
      }
   }


   /**
    * De-compresses given byte array with a zip input stream with entry name {@link #OBJECT_ZIP_ENTRY}.
    *
    * @param compressedBytes bytes to un-compress
    * @return byte array with de-compressed source.
    * @throws IOException if there is an unrecoverable problem while de-compressing bytes.
    */
   public byte[] decompress(final byte[] compressedBytes) throws IOException {

      ZipInputStream zis = null;
      ByteArrayOutputStream baos = null;
      ByteArrayInputStream bais = null;
      try {
         bais = new ByteArrayInputStream(compressedBytes);
         zis = new ZipInputStream(bais);
         // Get entry so that we can start reading.
         zis.getNextEntry();
         baos = new ByteArrayOutputStream(compressedBytes.length);
         IOUtils.copyInputToOutputStream(zis, baos);
         baos.flush();
         return baos.toByteArray();
      } finally {
         IOUtils.closeHard(bais);
         IOUtils.closeHard(zis);
         IOUtils.closeHard(baos);
      }
   }


   public String toString() {

      return "Compressor{}";
   }
}
