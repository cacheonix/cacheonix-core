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
package org.cacheonix.exceptions;

import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * CacheonixException Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>09/08/2008</pre>
 */
public final class CacheonixExceptionTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheonixExceptionTest.class); // NOPMD


   private CacheonixException exception = null;

   private Exception cause;


   public void testToString() {

      assertNotNull(exception.toString());
   }


   public void testGetCause() {

      assertEquals(cause, exception.getCause());
   }


   protected void setUp() throws Exception {

      super.setUp();
      cause = new Exception();
      exception = new CacheonixException(cause);
   }


   public String toString() {

      return "CacheonixExceptionTest{" +
              "runtimeCacheException=" + exception +
              ", cause=" + cause +
              '}';
   }
}
