package org.cacheonix.impl.net.cluster;

import java.util.Collection;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Message;

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
interface MulticastMessageListenerList {

   /**
    * @param listener a lister to add to the internal registry of listeners so that it can be notified later about
    *                 received multicast messages.
    */
   void add(MulticastMessageListener listener);

   /**
    * Notifies subscribers that an mcast message was received.
    *
    * @param message to notify about.
    */
   void notify(Message message);

   /**
    * Notifies subscribers that a collection of nodes joined the cluster
    *
    * @param nodes a collection of nodes
    */
   void notifyNodesJoined(Collection<ClusterNodeAddress> nodes);

   /**
    * Notifies subscribers that a collection of nodes left the cluster
    *
    * @param nodes a collection of nodes
    */
   void notifyNodesLeft(Collection<ClusterNodeAddress> nodes);

   /**
    * Notifies subscribers that this node entered a blocked state.
    *
    * @see MulticastMessageListener#notifyClusterNodeBlocked()
    * @see #notifyNodeUnblocked()
    */
   void notifyNodeBlocked();

   /**
    * Notifies subscribers that this node entered exited a blocked state.
    *
    * @see MulticastMessageListener#notifyClusterNodeUnblocked() ()
    * @see #notifyNodeBlocked()
    */
   void notifyNodeUnblocked();

   void notifyReset();
}
