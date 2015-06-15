package org.cacheonix.impl.cluster.node;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.cluster.Cluster;
import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.cluster.ClusterEventSubscriptionEndedEvent;
import org.cacheonix.cluster.ClusterEventSubscriptionStartedEvent;
import org.cacheonix.cluster.ClusterMemberJoinedEvent;
import org.cacheonix.cluster.ClusterMemberLeftEvent;
import org.cacheonix.cluster.ClusterState;
import org.cacheonix.cluster.ClusterStateChangedEvent;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tests a bug report in CACHEONIX-445 - "Single node cluster takes a lot of time to start"
 */
public final class DistrbutedCacheonixBug415Test extends CacheonixTestCase {

   @SuppressWarnings("UnusedDeclaration")
   private static final Logger LOG = Logger.getLogger(DistrbutedCacheonixBug415Test.class); // NOPMD


   public void testClusterStartupTime() throws IOException, InterruptedException {

      // Prepare latch to release when the cluster becomes operational.
      final CountDownLatch operationalClusterReceivedLatch = new CountDownLatch(1);

      // Get cluster
      final Cacheonix cacheonix = Cacheonix.getInstance(TestUtils.getTestFile("cacheonix-config-CACHEONIX-445.xml"));
      final Cluster cluster = cacheonix.getCluster();

      // Subscribe to events
      cluster.addClusterEventSubscriber(new ClusterEventSubscriber() {


         /**
          * {@inheritDoc}
          *
          * This implementation releases the operationalClusterReceivedLatch when the cluster becomes operational.
          */
         public void notifyClusterStateChanged(final ClusterStateChangedEvent event) {

            if (event.getNewClusterState().equals(ClusterState.OPERATIONAL)) {

               // Release the latch
               operationalClusterReceivedLatch.countDown();
            }
         }


         public void notifyClusterEventSubscriptionStarted(final ClusterEventSubscriptionStartedEvent event) {
            // Do nothing
         }


         public void notifyClusterMemberJoined(final ClusterMemberJoinedEvent event) {
            // Do nothing
         }


         public void notifyClusterMemberLeft(final ClusterMemberLeftEvent event) {
            // Do nothing
         }


         public void notifyClusterEventSubscriptionEnded(final ClusterEventSubscriptionEndedEvent event) {
            // Do nothing
         }
      });

      // Wait the the notification for 2 seconds (double of 1 second default for homeAloneTimeout
      final boolean clusterBecameOperational = operationalClusterReceivedLatch.await(2, TimeUnit.SECONDS);

      // Assert took expect time
      assertTrue(clusterBecameOperational);
   }
}
