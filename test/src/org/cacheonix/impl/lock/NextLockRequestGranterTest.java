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
package org.cacheonix.impl.lock;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.cluster.ClusterProcessor;

import static org.mockito.Mockito.mock;

/**
 * A tester for {@link NextLockRequestGranter}.
 */
public final class NextLockRequestGranterTest extends CacheonixTestCase {


   private final ClusterProcessor clusterProcessor = mock(ClusterProcessor.class);

   private final LockQueue lockQueue = mock(LockQueue.class);

   /**
    * Object under test.
    */
   private final NextLockRequestGranter nextLockRequestGranter = new NextLockRequestGranter(clusterProcessor,
           lockQueue);


   /**
    * Tests {@link NextLockRequestGranter#toString()}.
    */
   public void testToString() {

      assertNotNull(nextLockRequestGranter.toString());
   }


   /**
    * Tests {@link NextLockRequestGranter#grantNextLockRequests()}.
    */
   public void testGrantNextLockRequests() {

      nextLockRequestGranter.grantNextLockRequests();
   }
}
