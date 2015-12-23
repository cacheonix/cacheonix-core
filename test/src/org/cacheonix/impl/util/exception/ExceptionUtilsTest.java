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
package org.cacheonix.impl.util.exception;

import java.io.IOException;

import junit.framework.TestCase;

import static org.cacheonix.impl.util.exception.ExceptionUtils.createIOException;
import static org.cacheonix.impl.util.exception.ExceptionUtils.createIllegalArgumentException;
import static org.cacheonix.impl.util.exception.ExceptionUtils.createIllegalStateException;
import static org.cacheonix.impl.util.exception.ExceptionUtils.enhanceExceptionWithAddress;
import static org.cacheonix.impl.util.exception.ExceptionUtils.toStackTrace;

/**
 * Tests ExceptionUtils
 */
@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "ThrowableInstanceNeverThrown"})
public final class ExceptionUtilsTest extends TestCase {

   private static final String EXCEPTION_MESSAGE = "test_message";

   private static final String ADDITIONAL_MESSAGE = "additional_message";

   private static final Throwable EXCEPTION = new Throwable(EXCEPTION_MESSAGE);


   public void testMakeIllegalStateException() {

      final IllegalStateException actual = createIllegalStateException(EXCEPTION);
      assertEquals(EXCEPTION, actual.getCause());
      assertTrue(actual.toString().contains(EXCEPTION_MESSAGE));
   }


   public void testCreateIOException() {

      final IOException actual = createIOException(EXCEPTION);
      assertEquals(EXCEPTION, actual.getCause());
      assertTrue(actual.toString().contains(EXCEPTION_MESSAGE));
   }


   public void testCreateIllegalArgumentException() {

      final IllegalArgumentException actual1 = createIllegalArgumentException(EXCEPTION);
      assertEquals(EXCEPTION, actual1.getCause());
      assertTrue(actual1.toString().contains(EXCEPTION_MESSAGE));

      final IllegalArgumentException actual2 = createIllegalArgumentException(ADDITIONAL_MESSAGE,
              EXCEPTION);
      assertEquals(EXCEPTION, actual2.getCause());
      assertTrue(actual2.toString().contains(ADDITIONAL_MESSAGE));
   }


   public void testEnhanceExceptionWithAddressCanHandleNullChannel() {

      final IOException ioException = new IOException();
      assertEquals(ioException, enhanceExceptionWithAddress(null, ioException));
   }

   public void testToStackTrace() {

      assertTrue(toStackTrace(EXCEPTION).startsWith("java.lang.Throwable: test_message\n" +
              "\tat org.cacheonix.impl.util.exception.ExceptionUtilsTest.<clinit>(ExceptionUtilsTest.java:36)\n"));
   }
}
