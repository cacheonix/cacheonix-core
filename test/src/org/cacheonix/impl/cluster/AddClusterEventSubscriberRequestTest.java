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
package org.cacheonix.impl.cluster;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterProcessorState;
import org.cacheonix.impl.net.cluster.ClusterView;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableFactory;
import org.mockito.ArgumentCaptor;

import static org.cacheonix.impl.net.ClusterNodeAddress.createAddress;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_BLOCKED;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_CLEANUP;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_NORMAL;
import static org.cacheonix.impl.net.cluster.ClusterProcessorState.STATE_RECOVERY;
import static org.cacheonix.impl.net.processor.Response.RESULT_SUCCESS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tester for {@link AddClusterEventSubscriberRequest}.
 */
@SuppressWarnings("Duplicates")
public final class AddClusterEventSubscriberRequestTest extends TestCase {


   private final ArgumentCaptor<Response> messageArgumentCaptor = ArgumentCaptor.forClass(Response.class);

   private final ClusterProcessorState clusterProcessorState = mock(ClusterProcessorState.class);

   private final ClusterEventSubscriber subscriber = mock(ClusterEventSubscriber.class);

   private final ClusterProcessor clusterProcessor = mock(ClusterProcessor.class);

   private final ClusterNodeAddress address = createAddress("127.0.0.1", 777);

   private AddClusterEventSubscriberRequest request;


   public AddClusterEventSubscriberRequestTest() throws IOException {

   }


   public void testProcessNormal() throws InterruptedException {

      // Use STATE_NORMAL
      when(clusterProcessorState.getState()).thenReturn(STATE_NORMAL);

      // Execute
      request.execute();

      // Verify
      verify(clusterProcessorState).getClusterView();
      verify(clusterProcessorState).addUserClusterEventSubscriber(subscriber);
      verify(clusterProcessor).post(messageArgumentCaptor.capture());
      assertEquals(RESULT_SUCCESS, messageArgumentCaptor.getValue().getResultCode());
   }


   public void testProcessBlocked() throws InterruptedException {

      // Use STATE_BLOCKED
      when(clusterProcessorState.getState()).thenReturn(STATE_BLOCKED);

      // Execute
      request.execute();

      // Verify
      verify(clusterProcessorState).getLastOperationalClusterView();
      verify(clusterProcessorState).addUserClusterEventSubscriber(subscriber);
      verify(clusterProcessor).post(messageArgumentCaptor.capture());
      assertEquals(RESULT_SUCCESS, messageArgumentCaptor.getValue().getResultCode());
   }


   public void testProcessRecovery() throws InterruptedException {

      // Use STATE_RECOVERY
      when(clusterProcessorState.getState()).thenReturn(STATE_RECOVERY);

      // Execute
      request.execute();

      // Verify
      verify(clusterProcessorState).getLastOperationalClusterView();
      verify(clusterProcessorState).addUserClusterEventSubscriber(subscriber);
      verify(clusterProcessor).post(messageArgumentCaptor.capture());
      assertEquals(RESULT_SUCCESS, messageArgumentCaptor.getValue().getResultCode());
   }


   public void testProcessCleanup() {

      // Use STATE_CLEANUP
      when(clusterProcessorState.getState()).thenReturn(STATE_CLEANUP);

      // Execute
      request.processCleanup();

      // Verify
      verify(clusterProcessorState).getLastOperationalClusterView();
      verify(clusterProcessorState).addUserClusterEventSubscriber(subscriber);
      verify(clusterProcessor).post(messageArgumentCaptor.capture());
      assertEquals(RESULT_SUCCESS, messageArgumentCaptor.getValue().getResultCode());

   }


   public void testWriteReadWire() {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      try {
         ser.serialize(request);
      } catch (final IOException e) {
         assertTrue(e instanceof NotSerializableException); // NOPMD
      }
   }


   public void testWireableType() {

      assertEquals(Wireable.TYPE_ADD_USER_CLUSTER_EVENT_SUBSCRIBER, request.getWireableType());
   }


   public void testCreateWireable() {

      final WireableFactory wireableFactory = WireableFactory.getInstance();
      final Wireable wireable = wireableFactory.createWireable(Wireable.TYPE_ADD_USER_CLUSTER_EVENT_SUBSCRIBER);
      assertTrue(wireable instanceof AddClusterEventSubscriberRequest);
   }


   public void testToString() {

      assertNotNull(request.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      // Given
      final ExecutorService userEventExecutor = mock(ExecutorService.class);
      final ClusterView clusterView = mock(ClusterView.class);

      when(clusterProcessor.getProcessorState()).thenReturn(clusterProcessorState);
      when(clusterProcessorState.getClusterView()).thenReturn(clusterView);
      when(clusterProcessorState.getUserEventExecutor()).thenReturn(userEventExecutor);
      when(clusterView.getClusterUUID()).thenReturn(UUID.randomUUID());

      request = new AddClusterEventSubscriberRequest(subscriber);
      request.setProcessor(clusterProcessor);
      request.setSender(address);
   }


   public void tearDown() throws Exception {

      request = null;
      super.tearDown();
   }
}
