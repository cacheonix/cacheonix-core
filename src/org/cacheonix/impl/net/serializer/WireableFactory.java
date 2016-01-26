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
package org.cacheonix.impl.net.serializer;

import org.cacheonix.impl.cache.distributed.partitioned.AddEntryModifiedSubscriberRequest;
import org.cacheonix.impl.cache.distributed.partitioned.AddEntryModifiedSubscriptionAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.AddRemoteEntryModifiedSubscriberMessage;
import org.cacheonix.impl.cache.distributed.partitioned.AggregatingAnnouncementResponse;
import org.cacheonix.impl.cache.distributed.partitioned.AggregatingResponse;
import org.cacheonix.impl.cache.distributed.partitioned.AtomicRemoveRequest;
import org.cacheonix.impl.cache.distributed.partitioned.AtomicReplaceRequest;
import org.cacheonix.impl.cache.distributed.partitioned.BeginBucketTransferMessage;
import org.cacheonix.impl.cache.distributed.partitioned.Bucket;
import org.cacheonix.impl.cache.distributed.partitioned.BucketOwner;
import org.cacheonix.impl.cache.distributed.partitioned.BucketOwnershipAssignment;
import org.cacheonix.impl.cache.distributed.partitioned.BucketTransfer;
import org.cacheonix.impl.cache.distributed.partitioned.BucketTransferCompletedAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.BucketTransferRejectedAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.CacheNodeJoinedMessage;
import org.cacheonix.impl.cache.distributed.partitioned.CacheNodeLeftMessage;
import org.cacheonix.impl.cache.distributed.partitioned.CacheResponse;
import org.cacheonix.impl.cache.distributed.partitioned.CacheableEntry;
import org.cacheonix.impl.cache.distributed.partitioned.CacheableValue;
import org.cacheonix.impl.cache.distributed.partitioned.CancelBucketTransferMessage;
import org.cacheonix.impl.cache.distributed.partitioned.ClearFrontCacheBucketAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.ClearFrontCacheBucketMessage;
import org.cacheonix.impl.cache.distributed.partitioned.ClearRequest;
import org.cacheonix.impl.cache.distributed.partitioned.ContainsKeyRequest;
import org.cacheonix.impl.cache.distributed.partitioned.ContainsValueRequest;
import org.cacheonix.impl.cache.distributed.partitioned.EntryModifiedNotificationMessage;
import org.cacheonix.impl.cache.distributed.partitioned.EntryModifiedSubscription;
import org.cacheonix.impl.cache.distributed.partitioned.ExecuteAllRequest;
import org.cacheonix.impl.cache.distributed.partitioned.ExecuteRequest;
import org.cacheonix.impl.cache.distributed.partitioned.FinishBucketTransferMessage;
import org.cacheonix.impl.cache.distributed.partitioned.GetAllRequest;
import org.cacheonix.impl.cache.distributed.partitioned.GetEntrySetRequest;
import org.cacheonix.impl.cache.distributed.partitioned.GetKeyOwnerRequest;
import org.cacheonix.impl.cache.distributed.partitioned.GetKeyOwnersRequest;
import org.cacheonix.impl.cache.distributed.partitioned.GetKeySetRequest;
import org.cacheonix.impl.cache.distributed.partitioned.GetMaxSizeRequest;
import org.cacheonix.impl.cache.distributed.partitioned.GetRequest;
import org.cacheonix.impl.cache.distributed.partitioned.GetStatisticsRequest;
import org.cacheonix.impl.cache.distributed.partitioned.LeaveCacheGroupAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.OrphanBucketMessage;
import org.cacheonix.impl.cache.distributed.partitioned.PutAllRequest;
import org.cacheonix.impl.cache.distributed.partitioned.PutRequest;
import org.cacheonix.impl.cache.distributed.partitioned.RemoveAllRequest;
import org.cacheonix.impl.cache.distributed.partitioned.RemoveEntryModifiedSubscriberRequest;
import org.cacheonix.impl.cache.distributed.partitioned.RemoveEntryModifiedSubscriptionAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.RemoveRequest;
import org.cacheonix.impl.cache.distributed.partitioned.RepartitionAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.ReplaceIfMappedRequest;
import org.cacheonix.impl.cache.distributed.partitioned.RestoreBucketMessage;
import org.cacheonix.impl.cache.distributed.partitioned.RetainAllRequest;
import org.cacheonix.impl.cache.distributed.partitioned.ShutdownCacheProcessorMessage;
import org.cacheonix.impl.cache.distributed.partitioned.SizeRequest;
import org.cacheonix.impl.cache.distributed.partitioned.TransferBucketRequest;
import org.cacheonix.impl.cache.distributed.partitioned.TransferBucketResult;
import org.cacheonix.impl.cache.distributed.partitioned.UpdateKeyRequest;
import org.cacheonix.impl.cache.distributed.partitioned.ValuesRequest;
import org.cacheonix.impl.cache.item.CompressedBinary;
import org.cacheonix.impl.cache.item.PassByCopyBinary;
import org.cacheonix.impl.cache.item.PassByReferenceBinary;
import org.cacheonix.impl.cache.store.BinaryStore;
import org.cacheonix.impl.cache.store.BinaryStoreElement;
import org.cacheonix.impl.cache.store.CacheStatisticsImpl;
import org.cacheonix.impl.cache.subscriber.BinaryEntryModifiedEvent;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.cluster.AddClusterEventSubscriberRequest;
import org.cacheonix.impl.cluster.RemoveClusterEventSubscriberRequest;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.cluster.node.state.group.GroupMember;
import org.cacheonix.impl.cluster.node.state.group.JoinGroupMessage;
import org.cacheonix.impl.lock.AcquireLockRequest;
import org.cacheonix.impl.lock.EntryCountRequest;
import org.cacheonix.impl.lock.LockOwner;
import org.cacheonix.impl.lock.LockQueue;
import org.cacheonix.impl.lock.LockQueueKey;
import org.cacheonix.impl.lock.LockRegistry;
import org.cacheonix.impl.lock.ReleaseLockRequest;
import org.cacheonix.impl.lock.WaitForLockExpiredAnnouncement;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.BlockedMarker;
import org.cacheonix.impl.net.cluster.CleanupMarker;
import org.cacheonix.impl.net.cluster.ClusterAnnouncement;
import org.cacheonix.impl.net.cluster.ClusterNodeJoinedAnnouncement;
import org.cacheonix.impl.net.cluster.ClusterNodeLeftAnnouncement;
import org.cacheonix.impl.net.cluster.ClusterResponse;
import org.cacheonix.impl.net.cluster.ClusterViewImpl;
import org.cacheonix.impl.net.cluster.GetClusterViewSizeRequest;
import org.cacheonix.impl.net.cluster.JoinRequest;
import org.cacheonix.impl.net.cluster.JoiningNode;
import org.cacheonix.impl.net.cluster.MarkerListRequest;
import org.cacheonix.impl.net.cluster.MarkerTimeoutMessage;
import org.cacheonix.impl.net.cluster.MulticastFrameMessage;
import org.cacheonix.impl.net.cluster.MulticastMarker;
import org.cacheonix.impl.net.cluster.RecoveryMarker;
import org.cacheonix.impl.net.cluster.TestMessage;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.hashcode.MD5HashCodeCalculator;
import org.cacheonix.impl.util.logging.Logger;

/**
 * WireableFactory
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 1, 2010 9:29:20 PM
 */
public final class WireableFactory {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(WireableFactory.class); // NOPMD

   private static final WireableFactory INSTANCE = new WireableFactory();

   private final IntObjectHashMap<WireableBuilder> builders = new IntObjectHashMap<WireableBuilder>(256);


   private WireableFactory() {

      addMaker(Wireable.TYPE_PASS_BY_REFERENCE_BINARY, PassByReferenceBinary.BUILDER);
      addMaker(Wireable.TYPE_PASS_BY_COPY_BINARY, PassByCopyBinary.BUILDER);
      addMaker(Wireable.TYPE_COMPRESSED_BINARY, CompressedBinary.BUILDER);
      addMaker(Wireable.TYPE_BUCKET_OWNER, BucketOwner.BUILDER);
      addMaker(Wireable.TYPE_REPLICATED_STATE, ReplicatedState.BUILDER);
      addMaker(Wireable.TYPE_BUCKET, Bucket.BUILDER);
      addMaker(Wireable.TYPE_GROUP_MEMBER, GroupMember.BUILDER);
      addMaker(Wireable.TYPE_BUCKET_OWNERSHIP_ASSIGNMENT, BucketOwnershipAssignment.BUILDER);
      addMaker(Wireable.TYPE_MD5_HASH_CODE_CALC, MD5HashCodeCalculator.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_VIEW, ClusterViewImpl.BUILDER);
      addMaker(Wireable.TYPE_GROUP, Group.BUILDER);
      addMaker(Wireable.TYPE_NODE_ADDRESS, ClusterNodeAddress.BUILDER);
      addMaker(Wireable.TYPE_BUCKET_TRANSFER, BucketTransfer.BUILDER);
      addMaker(Wireable.TYPE_TEST_MESSAGE, TestMessage.BUILDER);
      addMaker(Wireable.TYPE_LOCK_REGISTRY, LockRegistry.BUILDER);
      addMaker(Wireable.TYPE_LOCK_QUEUE, LockQueue.BUILDER);
      addMaker(Wireable.TYPE_LOCK_OWNER, LockOwner.BUILDER);
      addMaker(Wireable.TYPE_BINARY_STORE, BinaryStore.BUILDER);
      addMaker(Wireable.TYPE_BINARY_STORE_ELEMENT, BinaryStoreElement.BUILDER);
      addMaker(Wireable.TYPE_CACHE_STATISTICS, CacheStatisticsImpl.BUILDER);
      addMaker(Wireable.TYPE_BINARY_ENTRY_MODIFIED_EVENT, BinaryEntryModifiedEvent.BUILDER);
      addMaker(Wireable.TYPE_ENTRY_MODIFICATION_SUBSCRIPTION, EntryModifiedSubscription.BUILDER);
      addMaker(Wireable.TYPE_TIME, TimeImpl.BUILDER);
      addMaker(Wireable.TYPE_CACHEABLE_VALUE, CacheableValue.BUILDER);
      addMaker(Wireable.TYPE_CACHEABLE_ENTRY, CacheableEntry.BUILDER);
      addMaker(Wireable.TYPE_JOINING_NODE, JoiningNode.BUILDER);
      addMaker(Wireable.TYPE_RECEIVER_ADDRESS, ReceiverAddress.BUILDER);

      addMaker(Wireable.TYPE_CLUSTER_MULTICAST_MARKER, MulticastMarker.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_JOIN_REQUEST, JoinRequest.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_MARKER_LIST, MarkerListRequest.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_RECOVERY_MARKER, RecoveryMarker.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_CLEANUP_MARKER, CleanupMarker.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_BLOCKED_MARKER, BlockedMarker.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_RESPONSE, ClusterResponse.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_MARKER_TIMEOUT, MarkerTimeoutMessage.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_ANNOUNCEMENT, ClusterAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_KEY_OWNERS, GetKeyOwnersRequest.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_KEY_OWNER, GetKeyOwnerRequest.BUILDER);
      addMaker(Wireable.TYPE_CLUSTER_GET_CLUSTER_VIEW_SIZE, GetClusterViewSizeRequest.BUILDER);
      addMaker(Wireable.TYPE_MULTICAST_FRAME_MESSAGE, MulticastFrameMessage.BUILDER);
      addMaker(Wireable.TYPE_ADD_USER_CLUSTER_EVENT_SUBSCRIBER, AddClusterEventSubscriberRequest.BUILDER);
      addMaker(Wireable.TYPE_REMOVE_USER_CLUSTER_EVENT_SUBSCRIBER, RemoveClusterEventSubscriberRequest.BUILDER);

      addMaker(Wireable.TYPE_CACHE_PUT_REQUEST, PutRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_RESPONSE, CacheResponse.BUILDER);
      addMaker(Wireable.TYPE_CACHE_AGGREGATING_CACHE_RESPONSE, AggregatingResponse.AGGREGATING_RESPONSE_BUILDER);
      addMaker(Wireable.TYPE_CACHE_GET_REQUEST, GetRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_CONTAINS_REQUEST, ContainsKeyRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_REMOVE_REQUEST, RemoveRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_MEMBER_JOINED, CacheNodeJoinedMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_MEMBER_LEFT, CacheNodeLeftMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_BEGIN_BUCKET_TRANSFER_MESSAGE, BeginBucketTransferMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_TRANSFER_BUCKET_REQUEST, TransferBucketRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_FINISH_BUCKET_TRANSFER_MESSAGE, FinishBucketTransferMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_CANCEL_BUCKET_TRANSFER_MESSAGE, CancelBucketTransferMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_RESTORE_BUCKET_MESSAGE, RestoreBucketMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_CONTAINS_VALUE_REQUEST, ContainsValueRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_SIZE_REQUEST, SizeRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_CLEAR_REQUEST, ClearRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_PUT_ALL_REQUEST, PutAllRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_VALUES_REQUEST, ValuesRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_KEY_SET_REQUEST, GetKeySetRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_ENTRY_SET_REQUEST, GetEntrySetRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_EXECUTE_REQUEST, ExecuteRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_REMOVE_ALL_REQUEST, RemoveAllRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_EXECUTE_ALL_REQUEST, ExecuteAllRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_GET_ALL_REQUEST, GetAllRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_RETAIN_ALL_REQUEST, RetainAllRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_GET_MAX_SIZE_REQUEST, GetMaxSizeRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_SHUTDOWN_MESSAGE, ShutdownCacheProcessorMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_STATISTICS_REQUEST, GetStatisticsRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_ADD_ENTRY_MODIFIED_SUBSCRIBER_REQUEST, AddEntryModifiedSubscriberRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_ADD_REMOTE_SUBSCRIBER_MESSAGE, AddRemoteEntryModifiedSubscriberMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_ENTRY_MODIFIED_MESSAGE, EntryModifiedNotificationMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_REMOVE_ENTRY_MODIFIED_SUBSCRIBER_REQUEST, RemoveEntryModifiedSubscriberRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_ORPHAN_BUCKET, OrphanBucketMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_INVALIDATE_FRONT_CACHE_MESSAGE, ClearFrontCacheBucketMessage.BUILDER);
      addMaker(Wireable.TYPE_CACHE_UPDATE_KEY_REQUEST, UpdateKeyRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_ATOMIC_REMOVE_REQUEST, AtomicRemoveRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_ATOMIC_REPLACE_REQUEST, AtomicReplaceRequest.BUILDER);
      addMaker(Wireable.TYPE_CACHE_REPLACE_IF_MAPPED_REQUEST, ReplaceIfMappedRequest.BUILDER);

      addMaker(Wireable.TYPE_GROUP_JOIN_GROUP, JoinGroupMessage.BUILDER);
      addMaker(Wireable.TYPE_GROUP_LEAVE_ANNOUNCEMENT, LeaveCacheGroupAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_GROUP_BUCKET_TRANSFER_COMPLETED, BucketTransferCompletedAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_GROUP_ANNOUNCE_REPARTITIONING, RepartitionAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_GROUP_BUCKET_TRANSFER_REJECTED, BucketTransferRejectedAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_ACQUIRE_LOCK_REQUEST, AcquireLockRequest.BUILDER);
      addMaker(Wireable.TYPE_RELEASE_LOCK_REQUEST, ReleaseLockRequest.BUILDER);
      addMaker(Wireable.TYPE_WAIT_FOR_LOCK_EXPIRED_ANNOUNCEMENT, WaitForLockExpiredAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_LOCK_ENTRY_COUNT_REQUEST, EntryCountRequest.BUILDER);
      addMaker(Wireable.TYPE_LOCK_QUEUE_KEY, LockQueueKey.BUILDER);
      addMaker(Wireable.TYPE_REGISTER_SUBSCRIPTION_ANNOUNCEMENT, AddEntryModifiedSubscriptionAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_UNREGISTER_SUBSCRIPTION_ANNOUNCEMENT, RemoveEntryModifiedSubscriptionAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_AGGREGATING_ANNOUNCEMENT_RESPONSE, AggregatingAnnouncementResponse.BUILDER);
      addMaker(Wireable.TYPE_CACHE_INVALIDATE_FRONT_CACHE_ANNOUNCEMENT, ClearFrontCacheBucketAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_TRANSFER_BUCKET_RESULT, TransferBucketResult.BUILDER);


      addMaker(Wireable.TYPE_NODE_LEFT_MESSAGE, ClusterNodeLeftAnnouncement.BUILDER);
      addMaker(Wireable.TYPE_NODE_JOINED_MESSAGE, ClusterNodeJoinedAnnouncement.BUILDER);
   }


   /**
    * Adds a class maker at a unique <code>index</code>.
    *
    * @param index   a index used to look up the constructor. The index is provided by {@link
    *                Message#getWireableType()}
    * @param builder the class used to create a given object.
    * @throws IllegalStateException if a error occurs while constructor is being added.
    */
   private void addMaker(final int index, final WireableBuilder builder) {

      final WireableBuilder existingBuilder = builders.put(index, builder);
      Assert.assertNull(existingBuilder, "Previous value should be null: {0}", existingBuilder);
   }


   /**
    * Returns singleton instance.
    *
    * @return singleton instance.
    * @see #createWireable(int)
    */
   public static WireableFactory getInstance() {

      return INSTANCE;
   }


   /**
    * Returns the number of registered Wireable objects.
    *
    * @return the number of registered Wireable objects.
    */
   public int size() {

      return builders.size();
   }


   /**
    * Creates a <code>Wireable</code> associated with the given unique <code>type</code>.
    *
    * @param type unique type
    * @return the <code>Wireable</code> associated with the given unique <code>type</code>.
    * @throws IllegalStateException if error occurred while creating a <code>Wireable</code>
    * @see #getInstance()
    */
   public Wireable createWireable(final int type) {

      try {

         final WireableBuilder builder = builders.get(type);
         Assert.assertNotNull(builder, "Unregistered type: {0}", type);
         return builder.create();
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new IllegalStateException(e);
      }
   }


   @Override
   public String toString() {

      return "WireableFactory{" +
              "constructors=" + builders +
              '}';
   }
}
