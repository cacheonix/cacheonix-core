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
package org.cacheonix.cache;

import junit.framework.TestCase;
import org.cacheonix.Version;

/**
 * Tests CacheException
 *
 * @noinspection ClassWithoutLogger, ThrowableInstanceNeverThrown
 */
public final class CacheExceptionTest extends TestCase {

   private static final String TEST_MESSAGE = "test message";

   private static final Throwable cause = new Throwable();


   /**
    * Tests constructor.
    */
   public void testCreate1() {

      final CacheException exception = new CacheException(TEST_MESSAGE);
      assertTrue(exception.getMessage().contains(TEST_MESSAGE));
      assertTrue(exception.getMessage().startsWith(Version.getVersion().fullProductVersion(true)));
   }


   /**
    * Tests constructor.
    */
   public void testCreate2() {

      final CacheException exception = new CacheException(TEST_MESSAGE, cause);
      assertTrue(exception.getMessage().contains(TEST_MESSAGE));
      assertTrue(exception.getMessage().startsWith(Version.getVersion().fullProductVersion(true)));
      assertEquals(cause, exception.getCause());
   }


   /**
    * Tests constructor3
    */
   public void testCreate3() {

      final CacheException exception = new CacheException(cause);
      assertTrue(exception.getMessage().startsWith(Version.getVersion().fullProductVersion(true)));
      assertEquals(cause, exception.getCause());
   }
}
