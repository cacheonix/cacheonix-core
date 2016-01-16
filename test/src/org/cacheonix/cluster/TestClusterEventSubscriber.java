package org.cacheonix.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.cacheonix.CacheonixExceptionTest;
import org.cacheonix.impl.util.logging.Logger;

final class TestClusterEventSubscriber implements ClusterEventSubscriber {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheonixExceptionTest.class); // NOPMD


   private final CountDownLatch clusterEventSubscriptionStartedCalledLatch = new CountDownLatch(1);

   private final CountDownLatch clusterEventSubscriptionEndedCalledLatch = new CountDownLatch(1);

   private final CountDownLatch notifyClusterMemberJoinedCalledLatch = new CountDownLatch(1);

   private final CountDownLatch notifyClusterMemberLeftCalledLatch = new CountDownLatch(1);

   private final AtomicInteger eventCounter = new AtomicInteger(0);

   private ClusterEventSubscriptionStartedEvent clusterEventSubscriptionStartedEvent = null;

   private final List<ClusterState> clusterStates = new ArrayList<ClusterState>(2);

   private final CountDownLatch notifyClusterMemberStateChangedCalledLatch = new CountDownLatch(2);


   public ClusterEventSubscriptionStartedEvent getClusterEventSubscriptionStartedEvent() {

      return clusterEventSubscriptionStartedEvent;
   }


   public void notifyClusterEventSubscriptionStarted(final ClusterEventSubscriptionStartedEvent event) {

      eventCounter.getAndIncrement();
      clusterEventSubscriptionStartedCalledLatch.countDown();
      clusterEventSubscriptionStartedEvent = event;
   }


   public void notifyClusterMemberJoined(final ClusterMemberJoinedEvent event) {

      eventCounter.getAndIncrement();
      notifyClusterMemberJoinedCalledLatch.countDown();
   }


   public void notifyClusterMemberLeft(final ClusterMemberLeftEvent event) {

      eventCounter.getAndIncrement();
      notifyClusterMemberLeftCalledLatch.countDown();
   }


   /**
    * Notifies this subscriber that the cluster state has changed.
    *
    * @param clusterStateChangedEvent the event that Cacheonix sends to this subscriber when the cluster state changes.
    */
   public void notifyClusterStateChanged(final ClusterStateChangedEvent clusterStateChangedEvent) {

      clusterStates.add(clusterStateChangedEvent.getNewClusterState());
      notifyClusterMemberStateChangedCalledLatch.countDown();
   }


   public void notifyClusterEventSubscriptionEnded(final ClusterEventSubscriptionEndedEvent event) {

      eventCounter.getAndIncrement();
      clusterEventSubscriptionEndedCalledLatch.countDown();
   }


   int waitForNotifyClusterEventSubscriptionStarted() throws InterruptedException, TimeoutException {

      if (clusterEventSubscriptionStartedCalledLatch.await(30, TimeUnit.SECONDS)) {

         return eventCounter.get();
      }
      throw new TimeoutException("Wait for the event timed out");
   }


   int waitForNotifyClusterEventSubscriptionEnded() throws InterruptedException, TimeoutException {

      if (clusterEventSubscriptionEndedCalledLatch.await(30, TimeUnit.SECONDS)) {

         return eventCounter.get();
      }

      throw new TimeoutException("Wait for the event timed out");
   }


   int waitForNotifyClusterMemberJoined() throws InterruptedException, TimeoutException {

      if (notifyClusterMemberJoinedCalledLatch.await(30, TimeUnit.SECONDS)) {

         return eventCounter.get();
      }

      throw new TimeoutException("Wait for the event timed out");
   }


   int waitForNotifyClusterMemberLeft() throws InterruptedException, TimeoutException {

      if (notifyClusterMemberLeftCalledLatch.await(30, TimeUnit.SECONDS)) {

         return eventCounter.get();
      }

      throw new TimeoutException("Wait for the event timed out");
   }


   List<ClusterState> waitForNotifyClusterStateChanged() throws InterruptedException, TimeoutException {

      if (notifyClusterMemberStateChangedCalledLatch.await(30, TimeUnit.SECONDS)) {

         return clusterStates;
      }

      throw new TimeoutException("Wait for the event timed out");
   }
}
