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
package org.cacheonix.impl.cluster;

import java.util.concurrent.Executor;

import org.cacheonix.cluster.ClusterConfiguration;
import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.cluster.ClusterEventSubscriptionEndedEvent;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterProcessorState;
import org.cacheonix.impl.net.cluster.ClusterView;
import org.cacheonix.impl.net.cluster.LocalClusterRequest;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * A request to add a cluster event subscriber.
 */
public final class RemoveClusterEventSubscriberRequest extends LocalClusterRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private ClusterEventSubscriber clusterEventSubscriber;


   /**
    * Required by Wireable.
    */
   @SuppressWarnings("UnusedDeclaration")
   public RemoveClusterEventSubscriberRequest() {

   }


   /**
    * Creates RemoveClusterEventSubscriberRequest.
    *
    * @param clusterEventSubscriber the local subscriber.
    */
   public RemoveClusterEventSubscriberRequest(final ClusterEventSubscriber clusterEventSubscriber) {

      super(Wireable.TYPE_REMOVE_USER_CLUSTER_EVENT_SUBSCRIBER);

      this.clusterEventSubscriber = clusterEventSubscriber;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation removes the cluster event subscriber and notifies the subscriber that the subscription ended.
    */
   protected void processNormal() {

      // Use current cluster view
      removeSubscriber(getClusterProcessor().getProcessorState().getClusterView());
   }


   /**
    * Processes this message while it is at the cluster service that is in a Blocked state.
    */
   protected void processBlocked() {

      // Use last operational cluster view
      removeSubscriber(getClusterProcessor().getProcessorState().getLastOperationalClusterView());
   }


   /**
    * Processes this message while it is at the cluster service that is in a Recovery state.
    */
   protected void processRecovery() {

      // Use last operational cluster view
      removeSubscriber(getClusterProcessor().getProcessorState().getLastOperationalClusterView());
   }


   /**
    * Processes this message while it is at the cluster service that is in a Cleanup state.
    */
   protected void processCleanup() {

      // Use last operational cluster view
      removeSubscriber(getClusterProcessor().getProcessorState().getLastOperationalClusterView());
   }


   /**
    * Removes the subscriber using a given cluster view (current or last operational) depending on the state.
    *
    * @param clusterView a cluster view (current or last operational) depending on the state.
    */
   private void removeSubscriber(final ClusterView clusterView) {

      // Register the subscriber
      final ClusterProcessor clusterProcessor = getClusterProcessor();
      final ClusterProcessorState processorState = clusterProcessor.getProcessorState();
      processorState.removeUserClusterEventSubscriber(clusterEventSubscriber);

      // Create configuration
      final String clusterName = processorState.getClusterName();
      final int state = processorState.getState();
      final ClusterConfiguration clusterConfiguration = ClusterEventUtil.getUserClusterConfiguration(clusterName, state, clusterView);

      // Create event
      final ClusterEventSubscriptionEndedEvent subscriptionEndedEvent = new ClusterEventSubscriptionEndedEventImpl(clusterConfiguration);

      // Notify the subscriber that the subscription started
      final Executor executor = processorState.getUserEventExecutor();
      executor.execute(new Runnable() {

         public void run() {

            // Notify
            clusterEventSubscriber.notifyClusterEventSubscriptionEnded(subscriptionEndedEvent);
         }
      });

      clusterProcessor.post(createResponse(Response.RESULT_SUCCESS));
   }


   public String toString() {

      return "RemoveClusterEventSubscriberRequest{" +
              "clusterEventSubscriber=" + clusterEventSubscriber +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new RemoveClusterEventSubscriberRequest();
      }
   }
}
