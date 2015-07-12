package org.cacheonix.impl.net.processor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;
import java.util.Timer;

import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.net.ClusterNodeAddress;

/**
 * A {@link Message} processor.
 */
public interface RequestProcessor extends Processor {

   /**
    * Returns address of this cluster node.
    *
    * @return address of this cluster node.
    */
   ClusterNodeAddress getAddress();

   /**
    * The router that is responsible for placing a message being sent to an input queue of a proper local processor or
    * to a processor that serves sending messages out.
    *
    * @return the router that is responsible for placing a message being sent to an input queue of a proper local
    * processor or to a processor that serves sending messages out.
    */
   Router getRouter();

   /**
    * Returns an unmodifiable local IP address set.
    *
    * @return an unmodifiable local IP address set.
    */
   Set<InetAddress> getLocalInetAddresses();

   WaiterList getWaiterList();

   void processMessage(Message message) throws InterruptedException, IOException;

   /**
    * Puts request into the execution queue and waits for completion. Clients use this method to execute requests.
    *
    * @param message the message to wait the result for.
    * @return result.
    * @throws RetryException if destination requires to retry.
    */
   <T> T execute(Message message) throws RetryException;

   /**
    * Puts the message into the input queue for future execution by this processor. This method exits immediately
    * without waiting.
    *
    * @param message message to post.
    */
   void post(Message message);

   /**
    * Puts the messages into the input queue for future execution by this processor. This method exits immediately
    * without waiting.
    *
    * @param messages message to post.
    */
   void post(Collection<? extends Message> messages);

   /**
    * Handles messages coming from the local client threads by placing them to the right local processor.
    * <p/>
    * <code>dispatch()</code> creates and returns a response waiter if message is a request and the local client thread
    * wants to wait for the response. In this <code>dispatch()</code> is different from <code>enqueue()</code> which
    * handles request arriving from the network.
    *
    * @param message the message to dispatch
    * @return a ResponseWaiter that a client thread should use if it wants to wait for a result. See {@link
    * #execute(Message)} for details.
    */
   ResponseWaiter route(Message message);

   /**
    * Processes notification about a cluster node leaving the cluster by notifying the waiter list.
    *
    * @param nodeLeftAddress an address of a node.
    */
   void notifyNodeLeft(ClusterNodeAddress nodeLeftAddress);

   /**
    * Processes notifications about cluster nods leaving the cluster by notifying the waiter list.
    *
    * @param addresses a collection of addresses.
    */
   void notifyNodesLeft(Collection<ClusterNodeAddress> addresses);

   Timer getTimer();

   /**
    * Returns the cluster-wide clock.
    *
    * @return the cluster-wide clock.
    */
   Clock getClock();
}
