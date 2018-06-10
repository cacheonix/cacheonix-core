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

import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.util.logging.Logger;

/**
 * MockProcessJoinedSubscriber
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since May 2, 2008 1:59:35 PM
 */
final class TestMulticastMessageListenerList implements MulticastMessageListener {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TestMulticastMessageListenerList.class); // NOPMD


   private ClusterNodeJoinedEvent recevedJoinedEvent = null;

   private ClusterNodeLeftEvent recevedLeftEvent = null;

   private Message receivedMessage = null;

   private boolean nodeBlockedCalled = false;

   private boolean nodeUnblockedCalled = false;

   private boolean resetCalled = false;


   public ClusterNodeJoinedEvent getRecevedJoinedEvent() {

      return recevedJoinedEvent;
   }


   public ClusterNodeLeftEvent getRecevedLeftEvent() {

      return recevedLeftEvent;
   }


   public Message getReceivedMessage() {

      return receivedMessage;
   }


   public void receive(final Message message) {

      this.receivedMessage = message;
   }


   public void notifyClusterNodeJoined(final ClusterNodeJoinedEvent event) {

      this.recevedJoinedEvent = event;
   }


   public void notifyClusterNodeLeft(final ClusterNodeLeftEvent event) {

      this.recevedLeftEvent = event;
   }


   public void notifyClusterNodeBlocked() {

      nodeBlockedCalled = true;
   }


   public void notifyClusterNodeUnblocked() {

      nodeUnblockedCalled = true;
   }


   public void notifyReset() {

      resetCalled = true;
   }


   public boolean isNodeBlockedCalled() {

      return nodeBlockedCalled;
   }


   public boolean isNodeUnblockedCalled() {

      return nodeUnblockedCalled;
   }


   public boolean isResetCalled() {

      return resetCalled;
   }


   public String toString() {

      return "TestMulticastMessageListenerList{" +
              "recevedJoinedEvent=" + recevedJoinedEvent +
              ", recevedLeftEvent=" + recevedLeftEvent +
              '}';
   }
}
