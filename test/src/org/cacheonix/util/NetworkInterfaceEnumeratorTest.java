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
package org.cacheonix.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;

import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

public final class NetworkInterfaceEnumeratorTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(NetworkInterfaceEnumeratorTest.class); // NOPMD

   private NetworkInterfaceEnumerator enumerator;


   public void testPrintInterfaces() throws SocketException, IllegalAccessException,
           NoSuchMethodException, InvocationTargetException {

      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = null;
      try {
         ps = new PrintStream(baos);
         enumerator.printInterfaces(ps);
      } finally {
         IOUtils.closeHard(ps);
      }
      assertTrue(baos.toString().contains(NetworkInterfaceEnumerator.NAME));
      assertTrue(baos.toString().contains(NetworkInterfaceEnumerator.DISPLAY_NAME));
   }


   protected void setUp() throws Exception {

      super.setUp();
      enumerator = new NetworkInterfaceEnumerator();
   }


   public String toString() {

      return "NetworkInterfaceEnumeratorTest{" +
              "enumerator=" + enumerator +
              "} " + super.toString();
   }
}
