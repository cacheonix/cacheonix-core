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
package org.cacheonix.impl.cache.distributed.partitioned.subscriber;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A reliable multicast message to register a modification subscriber in the replicated state.
 * <p/>
 * This message supports a sending a response to a sender. A success response is sent by the CacheProcessor owning the
 * bucket after performing the subscription. A retry response is sent by the request itself when it is at the subscriber
 * node if the replicated state indicates that the bucket is busy (reconfiguring) or if it is not assigned.
 * <p/>
 * The response is sent only once. This is guaranteed by a) There is only one cache processor for the bucket b) There is
 * always a copy of the replicated state at the subscriber node. c) The "node left" event that initiates retry count is
 * sent only once.
 * <p/>
 * After finishing, this announcement sends a response to the originating <code>AddEntryModifiedSubscriberRequest</code>
 * using {@link Waiter#parentRequest} reference.
 */
@SuppressWarnings("RedundantIfStatement")
public final class AddEntryModifiedSubscriptionAnnouncement extends KeySetAnnouncement {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AddEntryModifiedSubscriptionAnnouncement.class); // NOPMD

   /**
    * The subscription information.
    */
   private EntryModifiedSubscription subscription = null;


   /**
    * Required by wireable.
    */
   public AddEntryModifiedSubscriptionAnnouncement() {

   }


   /**
    * Creates <code>AddEntryModifiedSubscriptionAnnouncement</code>.
    *
    * @param cacheName a cache name.
    */
   public AddEntryModifiedSubscriptionAnnouncement(final String cacheName) {

      super(TYPE_REGISTER_SUBSCRIPTION_ANNOUNCEMENT, cacheName);
   }


   /**
    * Sets subscription information.
    *
    * @param subscription the subscription information to set.
    */
   public void setSubscription(final EntryModifiedSubscription subscription) {

      this.subscription = subscription;
   }


   EntryModifiedSubscription getSubscription() {

      return subscription;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation modifies the replicated state and posts a message to the local cache processor to facilitate
    * re-sending rejected buckets. This method also posts a message from an expected bucket owner to facilitate
    * terminating waiting.
    * <p/>
    * The waiting terminates when:
    * <p/>
    * a) The replicated state is successfuly updated, and
    * <p/>
    * b) The expected owner responded or died.
    */
   protected void processKeys(final Integer storageNumber, final IntHashSet rejectedBuckets,
                              final IntObjectHashMap<HashSet<Binary>> keysToProcess) {

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) {
         LOG.debug("Processing keysToProcess.size(): " + keysToProcess.size()); // NOPMD
      }

      final ClusterProcessor processor = getClusterProcessor();

      final ReplicatedState replicatedState = processor.getProcessorState().getReplicatedState();
      final Group group = replicatedState.getGroup(Group.GROUP_TYPE_CACHE, getCacheName());

      // Post a response from a local node containing processed buckets if this is a node that
      // sent the buckets. We do this to send a single processed response. The logic is that
      // this is the sender who is interested in response, so this is the only one that should
      // get it. The additional benefit is that the response is going to go through the local
      // processing instead of going through the network.
      if (processor.getAddress().equals(getSender())) {

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) {
            LOG.debug("Responding from local node with processed buckets, size: " + keysToProcess.size()); // NOPMD
         }

         final AggregatingAnnouncementResponse response = (AggregatingAnnouncementResponse) createResponse(Response.RESULT_SUCCESS);
         response.setProcessedBuckets(keysToProcess.keys());
         processor.post(response);
      }

      // Modify replicated state. Group will notify the subscriber to configuration
      // EntryEventSubscriptionConfigurationSubscriber that in turn will post a registration
      // message to the local cache processor(s).
      group.addEntryEventSubscription(keysToProcess, subscription);

      // Post response  if this node is the owner of the key. This may never happen
      // if the owner dies before the announcement is delivered. The case of the node
      // dying is handled in the code responsible for leave.
      if (processor.getAddress().equals(getBucketOwnerAddress())) {

         //noinspection ControlFlowStatementWithoutBraces
         if (LOG.isDebugEnabled()) LOG.debug("Responding from bucket owner node"); // NOPMD

         processor.post(createResponse(Response.RESULT_SUCCESS));
      }
   }


   protected KeySetAnnouncement createAnnouncement() {

      final AddEntryModifiedSubscriptionAnnouncement announcement = new AddEntryModifiedSubscriptionAnnouncement(getCacheName());
      announcement.subscription = subscription;
      return announcement;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation returns an Object array containing a single value <code>null</code>.
    */
   protected Object[] createResultAccumulator() {

      return new Object[1];
   }


   protected void aggregate(final Object[] resultAccumulator, final AggregatingAnnouncementResponse response) {
      // There is nothing to aggregate
   }


   /**
    * {@inheritDoc}
    */
   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      subscription = new EntryModifiedSubscription();
      subscription.readWire(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      subscription.writeWire(out);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final AddEntryModifiedSubscriptionAnnouncement that = (AddEntryModifiedSubscriptionAnnouncement) o;

      if (subscription != null ? !subscription.equals(that.subscription) : that.subscription != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (subscription != null ? subscription.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "AddEntryModifiedSubscriptionAnnouncement{" +
              "subscription=" + subscription +
              "} " + super.toString();
   }


   /**
    * Waits for a response from the owner of the keys
    */
   static class Waiter extends KeySetAnnouncement.Waiter {


      /**
       * An originator request that should be responded to after this announcement finishes.
       */
      private transient Request parentRequest;


      /**
       * Creates waiter.
       *
       * @param request request UUID
       */
      Waiter(final Request request) {

         super(request);
      }


      /**
       * Sets an originator request that should be responded to after this announcement finishes.
       *
       * @param parentRequest the originator request that should be responded to after this announcement finishes.
       */
      public void setParentRequest(final Request parentRequest) {

         this.parentRequest = parentRequest;
      }


      protected synchronized void notifyFinished() {

         final AggregatingAnnouncement request = (AggregatingAnnouncement) getRequest();

         // Proceed as usual
         super.notifyFinished();


         // Post-process by responding to the parent request with success

         if (request.isRootRequest()) {

            final Response responseToOriginator = parentRequest.createResponse(Response.RESULT_SUCCESS);
            request.getProcessor().post(responseToOriginator);
         }
      }
   }

   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new AddEntryModifiedSubscriptionAnnouncement();
      }
   }
}
