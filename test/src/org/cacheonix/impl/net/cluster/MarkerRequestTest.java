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

import junit.framework.TestCase;
import org.mockito.InOrder;

import static org.cacheonix.TestUtils.createTestAddress;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A tester for {@link MarkerRequest}.
 */
public class MarkerRequestTest extends TestCase {

   /**
    * Tests {@link MarkerRequest#execute()}.
    */
   public void testExecute() throws InterruptedException {


      //
      // Prepare
      //

      // Mock ClusterProcessorState
      final ClusterProcessorState clusterProcessorState = mock(ClusterProcessorState.class);

      // Mock ClusterProcessor
      final ClusterProcessor clusterProcessor = mock(ClusterProcessor.class);
      when(clusterProcessor.getProcessorState()).thenReturn(clusterProcessorState);

      // Mock MarkerRequest with real methods
      final MarkerRequest markerRequest = mock(MarkerRequest.class, CALLS_REAL_METHODS);
      markerRequest.setSender(createTestAddress());
      markerRequest.setProcessor(clusterProcessor);

      //
      // Execute the method under test
      //
      markerRequest.execute();

      //
      // Verify
      //
      final InOrder inOrder = inOrder(clusterProcessor);
      inOrder.verify(clusterProcessor).cancelMarkerTimeout();
      inOrder.verify(clusterProcessor).resetMarkerTimeout();
   }
}