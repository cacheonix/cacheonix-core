package org.cacheonix;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.ClockImpl;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterProcessorState;
import org.cacheonix.impl.net.cluster.ClusterView;
import org.cacheonix.impl.net.cluster.JoinStatus;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.util.IOUtils;
import org.mockito.ArgumentCaptor;

import static org.cacheonix.TestUtils.toInetAddresses;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestTestCase extends TestCase {

   protected static final UUID CLUSTER_UUID = UUID.randomUUID();

   protected ArgumentCaptor<Response> messageArgumentCaptor = ArgumentCaptor.forClass(Response.class);

   protected ClusterProcessorState clusterProcessorState = mock(ClusterProcessorState.class);

   protected ClusterProcessor clusterProcessor = mock(ClusterProcessor.class);

   protected ExecutorService userEventExecutor = mock(ExecutorService.class);

   protected ClusterView clusterView = mock(ClusterView.class);

   protected JoinStatus joinStatus = mock(JoinStatus.class);

   protected Clock clock = new ClockImpl(1L);

   private static final int TCP_PORT = 888;

   private static final String IP_ADDRESS_AS_STRING = "127.0.0.1";

   private static final InetAddress IP_ADDRESS = IOUtils.getInetAddress(IP_ADDRESS_AS_STRING);

   protected static final ClusterNodeAddress CLUSTER_NODE_ADDRESS = new ClusterNodeAddress(TCP_PORT, toInetAddresses(IP_ADDRESS));


   public RequestTestCase(final String name) {

      super(name);
   }


   public RequestTestCase() {

   }


   protected void setUp() throws Exception {

      super.setUp();

      when(clusterProcessor.getAddress()).thenReturn(CLUSTER_NODE_ADDRESS);
      when(clusterProcessor.getProcessorState()).thenReturn(clusterProcessorState);
      when(clusterProcessorState.getClusterView()).thenReturn(clusterView);
      when(clusterProcessorState.getUserEventExecutor()).thenReturn(userEventExecutor);
      when(clusterView.getClusterUUID()).thenReturn(CLUSTER_UUID);
   }


   protected void tearDown() throws Exception {

      messageArgumentCaptor = null;
      clusterProcessorState = null;
      userEventExecutor = null;
      clusterProcessor = null;
      clusterView = null;

      super.tearDown();
   }
}
