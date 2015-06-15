/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.configuration;

import java.io.Serializable;

import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * MemorySize
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Jun 16, 2008 11:19:59 PM
 */
public final class MemorySize implements Serializable {

   private static final long serialVersionUID = 8778828701347931476L;

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MemorySize.class); // NOPMD

   private static final int BYTES_IN_GIGABYTE = (1024 << 10) << 10;

   private static final int BYTES_IN_MEGABYTE = 1024 << 10;

   private static final int BYTES_IN_KILOBYTE = 1024;

   private static final int BYTES_IN_BYTE = 1;

   private final long sizeBytes;


   /**
    * @param memorySize
    * @throws IllegalArgumentException
    */
   public MemorySize(final String description, final String memorySize)
           throws IllegalArgumentException {

      final String memorySizeLowerCase = memorySize.toLowerCase();
      if (memorySizeLowerCase.endsWith("%")) {
         final int percentIndex = memorySizeLowerCase.length() - 1;
         // Relative size
         final String stringMaxSizeBytesPercent = memorySizeLowerCase.substring(0, percentIndex);
         if (StringUtils.isValidInteger(stringMaxSizeBytesPercent)) {
            final int percent = Integer.parseInt(stringMaxSizeBytesPercent);
            final int normalizedPercent = percent > 100 ? 100 : percent < 0 ? 0 : percent;
            sizeBytes = (long) ((double) Runtime.getRuntime().maxMemory() * ((double) normalizedPercent / (double) 100));
         } else {
            throw createInvalidMaxSizeBytesException(description);
         }
      } else if (memorySizeLowerCase.endsWith("g") || memorySizeLowerCase.endsWith("gb")) {
         sizeBytes = calculate(memorySizeLowerCase, description, 'g', BYTES_IN_GIGABYTE);
      } else if (memorySizeLowerCase.endsWith("m") || memorySizeLowerCase.endsWith("mb")) {
         sizeBytes = calculate(memorySizeLowerCase, description, 'm', BYTES_IN_MEGABYTE);
      } else if (memorySizeLowerCase.endsWith("k") || memorySizeLowerCase.endsWith("kb")) {
         sizeBytes = calculate(memorySizeLowerCase, description, 'k', BYTES_IN_KILOBYTE);
      } else {
         sizeBytes = calculate(memorySizeLowerCase + 'b', description, 'b', BYTES_IN_BYTE);
      }
   }


   /**
    * Returns size in bytes.
    *
    * @return size in bytes.
    */
   public long getSizeBytes() {

      return sizeBytes;
   }


   private static long calculate(final String formattedMaxSize, final String description,
                                 final char marker, final int measure) {

      final int markerIndex = formattedMaxSize.lastIndexOf((int) marker);
      final String stringMaxSize = formattedMaxSize.substring(0, markerIndex);
      if (StringUtils.isValidInteger(stringMaxSize)) {
         return Long.parseLong(stringMaxSize) * measure;
      } else {
         throw createInvalidMaxSizeBytesException(description);
      }
   }


   private static IllegalArgumentException createInvalidMaxSizeBytesException(final String name) {

      return new IllegalArgumentException(name + " should be a positive integer, " +
              "an integer with a suffix 'k', 'kb', 'm', 'mb', 'g', 'gb' or a percent");
   }


   public String toString() {

      return "MemorySize{" +
              "sizeBytes=" + sizeBytes +
              '}';
   }
}
