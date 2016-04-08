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
package org.cacheonix.impl.net.tcp;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Accumulates and serves ByteBuffer chunks.
 */
final class ChunkedBuffer {

   private final LinkedList<ByteBuffer> chunks = new LinkedList<ByteBuffer>(); // NOPMD

   private int available = 0;


   public void addChunk(final ByteBuffer chunk) {

      chunks.add(chunk);
      available += chunk.remaining();
   }


   public int available() {

      return available;
   }


   public byte get() {

      ByteBuffer buffer = chunks.getFirst();
      while (!buffer.hasRemaining()) {
         chunks.removeFirst();
         buffer = chunks.getFirst();
      }

      if (!buffer.hasRemaining()) {
         throw new IllegalStateException("Buffer exhausted");
      }

      // Decrement availability
      available--;

      // Return byte
      return buffer.get();
   }


   public int getInt() {

      final int b1 = get() & 0xff;
      final int b2 = get() & 0xff;
      final int b3 = get() & 0xff;
      final int b4 = get() & 0xff;
      return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
   }


   /**
    * Clears the buffer.
    */
   public void clear() {

      chunks.clear();
      available = 0;
   }


   public String toString() {

      return "ChunkedBuffer{" +
              "chunks=" + chunks +
              ", available=" + available +
              '}';
   }
}
