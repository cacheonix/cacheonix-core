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
package org.cacheonix.locks;

import junit.framework.TestCase;

/**
 * A tester for {@link DeadlockException}.
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public final class DeadlockExceptionTest extends TestCase {


   private static final String MESSAGE = "Test message";


   public void testDefaultConstructor() {

      assertNotNull(new DeadlockException().toString());
   }


   public void testConstructor() {

      final DeadlockException deadlockException = new DeadlockException(MESSAGE);
      assertNotNull(deadlockException.toString());
      assertTrue(deadlockException.getMessage().contains(MESSAGE));
   }
}
