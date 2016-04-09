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
package org.cacheonix.impl.cache.storage.disk;

import junit.framework.TestCase;

@SuppressWarnings("ThrowableInstanceNeverThrown")
public final class StorageExceptionTest extends TestCase {

   private static final String TEST_MESSAGE = "test StorageException message";

   private static final Throwable cause = new Throwable();


   /**
    * Tests constructor1
    */
   public void testCreate1() {

      assertTrue(new StorageException(TEST_MESSAGE).getMessage().contains(TEST_MESSAGE));
   }


   /**
    * Tests constructor2
    */
   public void testCreate2() {

      final StorageException storageException = new StorageException(TEST_MESSAGE, cause);
      assertTrue(storageException.getMessage().contains(TEST_MESSAGE));
      assertEquals(cause, storageException.getCause());
   }


   /**
    * Tests constructor3
    */
   public void testCreate3() {

      final StorageException storageException = new StorageException(cause);
      assertEquals(cause, storageException.getCause());
   }
}
