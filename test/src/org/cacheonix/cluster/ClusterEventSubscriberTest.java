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
package org.cacheonix.cluster;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.Cacheonix;
import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestUtils;

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

      // REVIEWME: simeshev@cacheonix.org -> This used to be 2, changed to 3 to fix the test. The idea is that it can
      // form  2 clusters, one is self and another is two-node. Need to invesigate. A more advanced test could be to
      // measure forming an operational cluster.
      assertEquals(2, clusterEventSubscriber.waitForNotifyClusterMemberJoined());
      assertEquals(ClusterState.BLOCKED, clusterEventSubscriber.getClusterEventSubscriptionStartedEvent().getClusterConfiguration().getClusterState());
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
}
