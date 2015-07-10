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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Cluster service uses <code>MulticastMessageListenerList</code> to notify a list of subscribers about multicast
 * messages and cluster events.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see ClusterProcessor#getMulticastMessageListeners()
 * @see MulticastMessageListener
 * @since Jan 20, 2009 7:08:57 PM
 */
final class MulticastMessageListenerList {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MulticastMessageListenerList.class); // NOPMD

   /**
    * A listener to call when a message arrives.
    */
   private final List<MulticastMessageListener> subscribers = new CopyOnWriteArrayList<MulticastMessageListener>();


   /**
    * @param listener a lister to add to the internal registry of listeners so that it can be notified later about
    *                 received macast messages.
    */
   public void add(final MulticastMessageListener listener) {

      subscribers.add(listener);
   }


   /**
    * Notifies subscribers that an mcast message was received.
    *
    * @param message to notify about.
    */
   public void notify(final Message message) {

      if (!subscribers.isEmpty()) {
         for (final MulticastMessageListener subscriber : subscribers) {
            subscriber.receive(message);
         }
      }
   }


   /**
    * Notifies subscribers that a collection of nodes joined the cluster
    *
    * @param nodes a collection of nodes
    */
   public void notifyNodesJoined(final Collection<ClusterNodeAddress> nodes) {

      // Return if there are no subscribers
      if (subscribers.isEmpty()) {
         return;
      }

      // Process
      for (final MulticastMessageListener subscriber : subscribers) {
         try {
            subscriber.notifyClusterNodeJoined(new ClusterNodeJoinedEventImpl(nodes));
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored to let other subscribers to proceed");
         }
      }
   }


   /**
    * Notifies subscribers that a collection of nodes left the cluster
    *
    * @param nodes a collection of nodes
    */
   public void notifyNodesLeft(final Collection<ClusterNodeAddress> nodes) {
      // Return if there are no subscribers
      if (subscribers.isEmpty()) {
         return;
      }

      // Process
      for (final MulticastMessageListener subscriber : subscribers) {
         try {
            subscriber.notifyClusterNodeLeft(new ClusterNodeLeftEventImpl(nodes));
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored to let other subscribers to proceed");
         }
      }
   }


   /**
    * Notifies subscribers that this node entered a blocked state.
    *
    * @see MulticastMessageListener#notifyClusterNodeBlocked()
    * @see #notifyNodeUnblocked()
    */
   public void notifyNodeBlocked() {
      // Return if there are no subscribers
      if (subscribers.isEmpty()) {
         return;
      }

      // Process
      for (final MulticastMessageListener subscriber : subscribers) {
         try {
            subscriber.notifyClusterNodeBlocked();
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored to let other subscribers to proceed");
         }
      }
   }


   /**
    * Notifies subscribers that this node entered exited a blocked state.
    *
    * @see MulticastMessageListener#notifyClusterNodeUnblocked() ()
    * @see #notifyNodeBlocked()
    */
   public void notifyNodeUnblocked() {
      // Return if there are no subscribers
      if (subscribers.isEmpty()) {
         return;
      }

      // Process
      for (final MulticastMessageListener subscriber : subscribers) {
         try {
            subscriber.notifyClusterNodeUnblocked();
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored to let other subscribers to proceed");
         }
      }
   }


   public void notifyReset() {
      // Return if there are no subscribers
      if (subscribers.isEmpty()) {
         return;
      }

      // Process
      for (final MulticastMessageListener subscriber : subscribers) {
         try {
            subscriber.notifyReset();
         } catch (final Exception e) {
            ExceptionUtils.ignoreException(e, "Ignored to let other subscribers to proceed");
         }
      }
   }


   public int getSubscriberCount() {

      return subscribers.size();
   }


   public String toString() {

      return "MulticastMessageListenerList{" +
              "subscriberList=" + subscribers +
              '}';
   }
}
