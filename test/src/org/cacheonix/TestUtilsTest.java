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
package org.cacheonix;

import java.io.IOException;

import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * TestUtilsTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Jun 23, 2008 9:45:42 PM
 */
public final class TestUtilsTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TestUtilsTest.class); // NOPMD


   public void testGetInetAddress() {

      assertNotNull(TestUtils.getInetAddress(TestConstants.MULTICAST_ADDRESS));
   }


   public void testGetTempFile() throws IOException {

      assertNotNull(TestUtils.getTempFile(TestUtilsTest.class.getName()));
   }
}
