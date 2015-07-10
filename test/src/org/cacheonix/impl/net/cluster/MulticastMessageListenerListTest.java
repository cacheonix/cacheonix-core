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
package org.cacheonix.impl.net.cluster;

import java.util.Collections;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * MulticastMessageListenerListTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Feb 4, 2010 9:10:40 PM
 */
public final class MulticastMessageListenerListTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MulticastMessageListenerListTest.class); // NOPMD

   private MulticastMessageListenerList subscribers;

   private TestMulticastMessageListenerList subscriber;


   public void testAdd() {

      assertEquals(1, subscribers.getSubscriberCount());
   }


   public void testNotifyClusterNodeJoined() {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      subscribers.notifyNodesJoined(Collections.singletonList(clusterNodeAddress));
      assertEquals(clusterNodeAddress, subscriber.getRecevedJoinedEvent().getNodes().get(0));
   }


   public void testNotifyClusterNodeLeft() {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      subscribers.notifyNodesLeft(Collections.singletonList(clusterNodeAddress));
      assertEquals(clusterNodeAddress, subscriber.getRecevedLeftEvent().getNodes().iterator().next());
   }


   public void testNotifyClusterNodeBlocked() {

      subscribers.notifyNodeBlocked();
      assertTrue(subscriber.isNodeBlockedCalled());
   }


   public void testToString() {

      assertNotNull(subscribers.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      subscriber = new TestMulticastMessageListenerList();
      subscribers = new MulticastMessageListenerList();
      assertEquals(0, subscribers.getSubscriberCount());
      subscribers.add(subscriber);
   }


   @Override
   public String toString() {

      return "MulticastMessageListenerListTest{" +
              "subscriberList=" + subscribers +
              "} " + super.toString();
   }
}
