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
package org.cacheonix.impl.cache.item;

import java.io.IOException;

import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Compressor Tester.
 *
 * @author simeshev@cacheonix.org
 * @since <pre>12/16/2008</pre>
 */
public final class CompressorTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CompressorTest.class); // NOPMD

   private static final String VALUE = "value";

   private Compressor compressor = null;


   public void testCompressUncompress() throws IOException {

      final byte[] compressedBytes = compressor.compress(VALUE.getBytes());
      final byte[] bytes = compressor.decompress(compressedBytes);
      assertEquals(VALUE, new String(bytes));
   }


   protected void setUp() throws Exception {

      super.setUp();
      compressor = Compressor.getInstance();
   }


   public String toString() {

      return "CompressorTest{" +
              "compressor=" + compressor +
              '}';
   }
}
