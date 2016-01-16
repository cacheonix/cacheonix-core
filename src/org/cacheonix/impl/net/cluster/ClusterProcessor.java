/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
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

import java.io.IOException;
import java.util.Queue;

import org.cacheonix.CacheonixException;
import org.cacheonix.ShutdownMode;
import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessor;
import org.cacheonix.impl.net.multicast.server.MulticastServerListener;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.RequestProcessor;

/**
 * A processor responsible for executing reliable multicast messages.
 */
public interface ClusterProcessor extends RequestProcessor, MulticastServerListener {

   /**
    * Announces this cluster.
    */
   void announceCluster(boolean operational) throws IOException;

   void sendMulticastFrame(Frame frame) throws IOException;

   Queue<Frame> getReceivedFrames();

   /**
    * Subscribes a listener for multicast messages. There can be only one listener.
    * <p/>
    * The listener receives a message once it is received.
    * <p/>
    * REVIEWME: simeshev@cacheonix.org - 2008-03-20 - consider moving to a constructor.
    *
    * @param listener to addGroupMembershipSubscriber.
    */
   void subscribeMulticastMessageListener(MulticastMessageListener listener);

   void shutdown(ShutdownMode shutdownMode);

   /**
    * Shuts down cluster service without waiting. This method is called by a {@link ShutdownClusterProcessorCommand}
    * which is a last command the processor is going to execute.
    *
    * @param shutdownCause the exception that caused the shutdown.
    * @see MulticastMarker#forward()
    * @see BlockedMarker#forward()
    * @see ShutdownClusterProcessorCommand#execute()
    */
   void forceShutdown(CacheonixException shutdownCause);

   /**
    * Returns a queue that receives message parts and assembles whole messages. Once a whole message is assembled, the
    * queue pushes it out by calling a listener.
    *
    * @return a queue that receives message parts and assembles whole messages.
    */
   MessageAssembler getMessageAssembler();

   /**
    * Resets context by cleaning send and receive queue and resetting all counters.
    */
   void reset();

   boolean isShuttingDown();

   /**
    * Stops waiting for a marker.
    */
   void cancelMarkerTimeout();

   /**
    * Starts waiting for a marker.
    */
   void resetMarkerTimeout();

   /**
    * Delivers assembled requests accumulated in the request assembler.
    */
   void deliverAssembledMulticastMessages() throws IOException;

   /**
    * Notify messages waiting for the delivery notification
    *
    * @param frameNumbersAllDeliveredUpTo the frame number it was delivered up to.
    */
   void notifyDeliveredToAll(long frameNumbersAllDeliveredUpTo);

   // REVIEWME: 2015-10-16 - simeshev@cacheonix.org - Consider why MulticastMessageListenerList is accessed
   // outside of the ClusterProcessor. Can the use be incapsulated inside the ClusterProcessor?
   MulticastMessageListenerList getMulticastMessageListeners();

   void registerCacheProcessor(CacheProcessor cacheProcessor);

   /**
    * Removes a cache processor from the registry of cache processors.
    *
    * @param cacheName name of the cache processor.
    * @return the removed cache processor or null if there is no a cache processor with the given name.
    */
   CacheProcessor unregisterCacheProcessor(String cacheName);

   ClusterProcessorState getProcessorState();

   /**
    * Shuts down cluster service without waiting.
    *
    * @see MulticastMarker#forward()
    * @see BlockedMarker#forward()
    * @see ShutdownClusterProcessorCommand#execute()
    */
   void beginForcedShutdown();
}
