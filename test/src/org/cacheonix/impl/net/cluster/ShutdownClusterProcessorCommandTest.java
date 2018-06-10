package org.cacheonix.impl.net.cluster;

import java.util.Collection;

import junit.framework.TestCase;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.mockito.Matchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * A tester for ShutdownClusterProcessorCommand.
 */
public final class ShutdownClusterProcessorCommandTest extends TestCase {

   public void testExecute() {

      // Prepare
      final ClusterProcessor processor = mock(ClusterProcessor.class);
      final ShutdownClusterProcessorCommand command = new ShutdownClusterProcessorCommand(processor);

      // Call method under test
      command.execute();

      // Verify
      verify(processor).notifyNodesLeft(Matchers.<Collection<ClusterNodeAddress>>any());
      verify(processor).forceShutdown(null);
   }
}