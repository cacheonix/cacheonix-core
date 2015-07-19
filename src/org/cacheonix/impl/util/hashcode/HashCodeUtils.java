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
package org.cacheonix.impl.util.hashcode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Hasher
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 2, 2008 6:20:53 PM
 */
public final class HashCodeUtils {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(HashCodeUtils.class); // NOPMD

   private static final long FNV_32_PRIME = 16777619L;

   private static final long FNV_32_INIT = 2166136261L;

   private static final long FNV_64_PRIME = 0x100000001b3L;

   private static final long FNV_64_INIT = 0xcbf29ce484222325L;


   /**
    * Utility class constructor.
    */
   private HashCodeUtils() {

   }


   public static long getCRC32(final String k) {

      final CRC32 crc32 = new CRC32();
      crc32.update(k.getBytes());
      return crc32.getValue() >> 16 & 0x7fff;
   }


   public static long getFNV32(final String s) {

      long hash = FNV_32_INIT;
      final int len = s.length();
      for (int i = 0; i < len; i++) {
         hash *= FNV_32_PRIME;
         hash ^= (long) s.charAt(i);
      }
      return hash;
   }


   public static long getFNV1a32(final String s) {

      long hash = FNV_32_INIT;
      final int len = s.length();
      for (int i = 0; i < len; i++) {
         hash ^= (long) s.charAt(i);
         hash *= FNV_32_PRIME;
      }
      return hash;
   }


   public static long getFNV64(final String s) {

      long hash = FNV_64_INIT;
      final int len = s.length();
      for (int i = 0; i < len; i++) {
         hash *= FNV_64_PRIME;
         hash ^= (long) s.charAt(i);
      }
      return hash;
   }


   public static long getFNV1a64(final String s) {

      long hash = FNV_64_INIT;
      final int len = s.length();
      for (int i = 0; i < len; i++) {
         hash ^= (long) s.charAt(i);
         hash *= FNV_64_PRIME;
      }
      return hash;
   }


   /**
    * Get the md5 of the given key.
    */
   public static byte[] getMD5(final String s) {

      final MessageDigest md5;
      try {
         md5 = MessageDigest.getInstance("MD5");
      } catch (final NoSuchAlgorithmException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
      md5.update(s.getBytes());
      return md5.digest();
   }
}