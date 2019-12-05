package org.cacheonix;

import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterProcessorState;
import org.cacheonix.impl.net.cluster.ClusterView;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestTestCase extends TestCase {

   protected  ArgumentCaptor<Response> messageArgumentCaptor = ArgumentCaptor.forClass(Response.class);

   protected ClusterProcessorState clusterProcessorState = mock(ClusterProcessorState.class);

   protected ClusterProcessor clusterProcessor = mock(ClusterProcessor.class);

   protected ExecutorService userEventExecutor = mock(ExecutorService.class);

   protected ClusterView clusterView = mock(ClusterView.class);


   public void testName() {

   }


   public void setUp() throws Exception {

      super.setUp();


      when(clusterProcessor.getProcessorState()).thenReturn(clusterProcessorState);
      when(clusterProcessorState.getClusterView()).thenReturn(clusterView);
      when(clusterProcessorState.getUserEventExecutor()).thenReturn(userEventExecutor);
      when(clusterView.getClusterUUID()).thenReturn(UUID.randomUUID());
   }


   public void tearDown() throws Exception {

      messageArgumentCaptor = null;
      clusterProcessorState = null;
      clusterProcessor = null;
      userEventExecutor = null;
      clusterView = null;

      super.tearDown();
   }
}
