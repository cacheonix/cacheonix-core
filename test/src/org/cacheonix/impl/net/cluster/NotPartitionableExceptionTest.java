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
package org.cacheonix.impl.net.cluster;

import java.io.NotSerializableException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;

/**
 * Tester for NotPartitionableException.
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public final class NotPartitionableExceptionTest extends CacheonixTestCase {

   private NotPartitionableException exception;

   private NotSerializableException cause;


   public void testDefaultConstructor() {

      assertNotNull(new NotPartitionableException().toString());
   }


   public void testConstructor() {

      assertNotNull(exception.toString());
      assertEquals(cause, exception.getCause());
   }


   public void setUp() throws Exception {

      super.setUp();
      cause = new NotSerializableException();
      exception = new NotPartitionableException(new JoinRequest(TestUtils.createTestAddress(1)), cause);
   }
}
