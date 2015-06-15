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
package org.cacheonix.impl.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;

/**
 * Utility methods for manipulating arrays.
 */
public final class ArrayUtils {

   /**
    * Shit to use when calculating has hcode.
    */
   private static final int HASHCODE_SHIFT = 29;

   /**
    * Reuseable zero length byte array.
    */
   public static final byte[] EMPTY_BYTE_ARRAY = {};

   /**
    * Resuseable string array.
    */
   public static final String[] EMPTY_STRING_ARRAY = {};


   /**
    * Utilicy class constructor.
    */
   private ArrayUtils() {

   }


   public static byte[] copy(final byte[] src) {

      if (src == null) {
         return null;
      }

      final byte[] dest = new byte[src.length];
      System.arraycopy(src, 0, dest, 0, src.length);
      return dest;
   }


   public static String[] copy(final String[] src) {

      final String[] dest = new String[src.length];
      System.arraycopy(src, 0, dest, 0, src.length);
      return dest;
   }


   /**
    * Calculates a hash code for an array.
    *
    * @param array the array for that to calculate hash code.
    * @return the hash code for an array.
    */
   public static int getHashCode(final byte[] array) {

      if (array == null) {
         return 0;
      }
      int result = 0;
      for (final byte anArray : array) {
         result = HASHCODE_SHIFT * result + anArray;
      }
      return result;
   }


   /**
    * Splits an array into a list of arrays.
    *
    * @param array     the array to split.
    * @param chunkSize the size of a chunk.
    * @return a list of arrays.
    */
   public static List<byte[]> split(final byte[] array, final int chunkSize) {

      if (array == null || array.length == 0) {
         return new ArrayList<byte[]>(0);
      } else {
         // Get counters
         final int fullChunkCount = array.length / chunkSize;
         final int lastChunkSize = array.length % chunkSize;
         final List<byte[]> result = new ArrayList<byte[]>(fullChunkCount + (lastChunkSize > 0 ? 1 : 0));

         // Get full chunks
         for (int i = 0; i < fullChunkCount; i++) {
            final int from = i * chunkSize;
            final int to = from + chunkSize;
            result.add(copyOfRange(array, from, to));
         }

         // ... Get last chunk

         return result;
      }
   }


   /**
    * Creates a copy of an array of {@link InetAddress} objects.
    *
    * @param array an array of {@link InetAddress} objects.
    * @return a copy of an array of {@link InetAddress} objects.
    */
   public static InetAddress[] copy(final InetAddress[] array) {

      final InetAddress[] result = new InetAddress[array.length];
      System.arraycopy(array, 0, result, 0, array.length);
      return result;
   }


   public static ClusterNodeAddress[] copy(final ClusterNodeAddress[] array) {

      final ClusterNodeAddress[] result = new ClusterNodeAddress[array.length];
      System.arraycopy(array, 0, result, 0, array.length);
      return result;
   }


   public static boolean[] copy(final boolean[] array) {

      final boolean[] result = new boolean[array.length];
      System.arraycopy(array, 0, result, 0, array.length);
      return result;
   }


   public static List<Integer> asList(final int[] ints) {

      if (ints == null) {
         return null;
      }
      final List<Integer> list = new ArrayList<Integer>(ints.length);
      for (final int anInt : ints) {
         list.add(anInt);
      }
      return list;
   }


   public static int[] copy(final int[] src) {

      final int[] dest = new int[src.length];
      System.arraycopy(src, 0, dest, 0, src.length);
      return dest;
   }


   /**
    * @since 1.6
    */
   public static byte[] copyOfRange(final byte[] original, final int from, final int to) {

      final int newLength = to - from;
      if (newLength < 0) {
         throw new IllegalArgumentException(from + " > " + to);
      }
      final byte[] arr = new byte[newLength];
      final int ceil = original.length - from;
      final int len = ceil < newLength ? ceil : newLength;
      System.arraycopy(original, from, arr, 0, len);
      return arr;
   }
}
