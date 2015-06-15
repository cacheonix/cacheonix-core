/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;
import org.cacheonix.exceptions.CacheonixExceptionTest;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A tester for the cluster event subscriber.
 */
public final class ClusterEventSubscriberTest extends CacheonixTestCase {


   private final List<Cacheonix> managers = new ArrayList<Cacheonix>(2);

   private TestClusterEventSubscriber clusterEventSubscriber;


   public void testNotifyClusterEventSubscriptionStartedCalled() throws Exception {

      assertEquals(1, clusterEventSubscriber.waitForNotifyClusterEventSubscriptionStarted());
   }


   public void testNotifyClusterMemberJoinedCalled() throws Exception {

      assertEquals(2, clusterEventSubscriber.waitForNotifyClusterMemberJoined());
      assertEquals(ClusterState.BLOCKED, clusterEventSubscriber.getClusterEventSubscriptionStartedEvent().getCurrentClusterConfiguration().getClusterState());
   }


   public void testNotifyClusterStateChangedCalled() throws Exception {

      final List<ClusterState> states = clusterEventSubscriber.waitForNotifyClusterStateChanged();
      assertEquals(2, states.size());
      assertEquals(ClusterState.RECONFIGURING, states.get(0));
      assertEquals(ClusterState.OPERATIONAL, states.get(1));
   }


   public void testNotifyClusterMemberLeftCalled() throws Exception {

      // First make sure the node joined.
      clusterEventSubscriber.waitForNotifyClusterMemberJoined();

      // Shutdown node
      managers.get(1).shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      managers.remove(1);

      // Assert left called. 4 becuase we have observed 2 join notifications.
      assertEquals(4, clusterEventSubscriber.waitForNotifyClusterMemberLeft());
   }


   public void testNotifyClusterEventSubscriptionEndedCalled() throws Exception {

      // Unsubscribe
      managers.get(0).getCluster().removeClusterEventSubscriber(clusterEventSubscriber);

      // Assert event was sent
      assertTrue(clusterEventSubscriber.waitForNotifyClusterEventSubscriptionEnded() >= 2);
   }


   public void setUp() throws Exception {

      super.setUp();

      clusterEventSubscriber = new TestClusterEventSubscriber();

      // Create a cluster member with an event subscriber
      final Cacheonix instance = Cacheonix.getInstance(TestUtils.getTestFile("cacheonix-config-cluster-member-1.xml"));
      instance.getCluster().addClusterEventSubscriber(clusterEventSubscriber);
      managers.add(instance);

      // Create second cluster member with an event subscriber
      managers.add(Cacheonix.getInstance(TestUtils.getTestFile("cacheonix-config-cluster-member-2.xml")));
   }


   public void tearDown() throws Exception {

      // Shutdown cluster members
      for (final Cacheonix manager : managers) {

         manager.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      }

      super.tearDown();
   }


   private static class TestClusterEventSubscriber implements ClusterEventSubscriber {

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
       * @param clusterStateChangedEvent the event that Cacheonix sends to this subscriber when the cluster state
       *                                 changes.
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
}
