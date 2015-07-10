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
package org.cacheonix.exceptions;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * RuntimeIOException Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/03/2008</pre>
 */
public final class RuntimeIOExceptionTest extends TestCase {

   private RuntimeIOException exception;

   private static final String TEST_MESSAGE = "Test message";


   public void testToString() {

      assertNotNull(exception.toString());
   }


   public void testCreate() {

      assertTrue(exception.toString().contains(TEST_MESSAGE));
   }


   protected void setUp() throws Exception {

      super.setUp();
      final IOException ioException = new IOException(TEST_MESSAGE);
      exception = new RuntimeIOException(ioException);
   }


   public String toString() {

      return "RuntimeIOExceptionTest{" +
              "exception=" + exception +
              "} " + super.toString();
   }
}
