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
package org.cacheonix.impl.cluster.node.state.group;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.cacheonix.impl.cache.distributed.partitioned.BucketEventListenerList;
import org.cacheonix.impl.cache.distributed.partitioned.BucketOwnershipAssignment;
import org.cacheonix.impl.cache.distributed.partitioned.subscriber.EntryEventSubscriptionConfigurationSubscriber;
import org.cacheonix.impl.cache.distributed.partitioned.subscriber.EntryModifiedSubscription;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntArrayList;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.IntObjectProcedure;
import org.cacheonix.impl.util.array.ObjectObjectProcedure;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * CacheGroup
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection ParameterHidesMemberVariable, RedundantIfStatement, ParameterNameDiffersFromOverriddenParameter,
 * NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode
 * @since Jan 19, 2009 3:18:28 AM
 */
public final class Group implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Group.class); // NOPMD

   /**
    * Group type "Unknown".
    */
   public static final int GROUP_TYPE_UNKNOWN = 0;


   /**
    * Group type "Cache".
    */
   public static final int GROUP_TYPE_CACHE = 1;


   /**
    * Group name.
    */
   private String name = null;

   /**
    * Group type.
    *
    * @noinspection NumericCastThatLosesPrecision
    */
   private int groupType = GROUP_TYPE_UNKNOWN;

   /**
    * List of members with an address as a key.
    */
   private List<GroupMember> members = null;

   /**
    * Configuration version.
    */
   private long version = 0L;


   /**
    * System-wide partition size in bytes.
    */
   private long partitionSizeBytes = 0L;


   /**
    * System-wide number of replicas.
    */
   private int replicaCount = 0;

   /**
    * Flag to show if the partition has been configured.
    */
   private boolean partitionConfigured = false;


   /**
    * Replicated bucket ownership assignment.
    */
   private BucketOwnershipAssignment bucketOwnershipAssignment = null;


   /**
    * List of subscribers to group membership events.
    */
   private transient GroupEventSubscriberList groupEventSubscriberList = null;

   private transient EntryEventSubscriptionConfigurationSubscriber entryEventSubscriptionConfigurationSubscriber = null;

   /**
    * A max number of elements in memory.
    */
   private long maxElements = Integer.MAX_VALUE;

   /**
    * Key modification subscriptions. The key in the map is bucket number. The value is map of binary keys to a set of
    * subscriptions.
    */
   private final IntObjectHashMap<HashMap<Binary, HashSet<EntryModifiedSubscription>>> entryModifiedSubscriptions = new IntObjectHashMap<HashMap<Binary, HashSet<EntryModifiedSubscription>>>();


   public Group(final String name, final int groupType) {

      this.bucketOwnershipAssignment = new BucketOwnershipAssignment(name, 0, (byte) 0);
      this.members = new ArrayList<GroupMember>(11);
      this.name = name;
      this.groupType = groupType;
   }


   /**
    * @noinspection WeakerAccess
    */
   public Group() {

   }


   /**
    * Returns group name.
    *
    * @return group name.
    */
   public String getName() {

      return name;
   }


   public int getGroupType() {

      return groupType;
   }


   public GroupMember getGroupMember(final ClusterNodeAddress address) {

      for (final GroupMember groupMember : members) {
         if (groupMember.getAddress().equals(address)) {
            return groupMember;
         }
      }
      return null;
   }


   public int getWireableType() {

      return TYPE_GROUP;
   }


   /**
    * Returns a max number of elements in memory.
    *
    * @return the max number of elements in memory.
    */
   public long getMaxElements() {

      return maxElements;
   }


   /**
    * Returns replicated entry modification subscriptions. The key in the map is bucket number. The value is map of
    * binary keys to a set of subscriptions.
    *
    * @return the replicated entry modification subscriptions. The key in the map is bucket number. The value is map of
    *         binary keys to a set of subscriptions.
    */
   public IntObjectHashMap<HashMap<Binary, HashSet<EntryModifiedSubscription>>> getEntryModifiedSubscriptions() {

      return entryModifiedSubscriptions;
   }


   /**
    * Adds member to the group.
    *
    * @param newMember to add.
    */
   public final void addMember(final GroupMember newMember) {

      // Find out if this is a re-joining member
      int existingMemberIndex = 0;
      GroupMember existingMember = null;
      for (; existingMemberIndex < members.size(); existingMemberIndex++) {
         final GroupMember member = members.get(existingMemberIndex);
         if (member.getAddress().equals(newMember.getAddress())) {
            existingMember = member;
            break;
         }
      }

      // Add/reactivate member
      if (existingMember == null) {
         // Add new member
         members.add(newMember);
         newMember.setGroup(this);
         newMember.setActive(true);
         version++;
      } else {
         // Re-active re-joining member
         Assert.assertTrue(!existingMember.isActive(), "Existing member should be inactive: {0}", existingMember);
         members.set(existingMemberIndex, newMember);
         newMember.setGroup(this);
         newMember.setActive(true);
      }

      // Notify
      // REVIEWME: simeshev@cacheonix.org - 2008-01-21 -> passing the group member by reference is dangerous
      groupEventSubscriberList.notifyMemberJoined(new GroupMemberJoinedEvent(newMember));

      // Add to RBOAT
      if (newMember.isPartitionContributor()) {
         bucketOwnershipAssignment.addBucketOwner(newMember.getAddress());
      }
   }


   public void removeMembers(final Collection<ClusterNodeAddress> clusterNodesLeft) {

      final Collection<ClusterNodeAddress> partitionContributorsLeft = new LinkedList<ClusterNodeAddress>();
      for (final ClusterNodeAddress leftAddress : clusterNodesLeft) {

         // Mark member as inactive
         GroupMember foundMember = null;
         for (final GroupMember member : members) {

            foundMember = member;
            if (foundMember.getAddress().equals(leftAddress)) {

               // NOTE: simeshev@cacheonix.org - 2010-09-10 - It is possible that
               // remove for the member is called more than once.
               //
               // Example:
               //   Step 1. CacheProcessor leaves.
               //   Step 2. ClusterProcessor leaves.
               if (member.isActive()) {

                  foundMember.setActive(false);
                  foundMember.setLeaving(false);
               } else {

                  // Though is there, it has already left
                  foundMember = null;
               }
               break;
            } else {

               foundMember = null;
            }
         }

         // Is member with the given address exist?
         if (foundMember == null) {
            // No, there is no a member with such address
            continue;
         }

         // Mark inactive
         version++;


         // Notify subscribers
         // REVIEWME: simeshev@cacheonix.org - 2008-01-21 -> passing the group member by reference is dangerous
         groupEventSubscriberList.notifyMemberLeft(new GroupMemberLeftEvent(foundMember));


         // Remove entry modified subscription
         entryModifiedSubscriptions.forEachEntry(new IntObjectProcedure<HashMap<Binary, HashSet<EntryModifiedSubscription>>>() {

            public boolean execute(final int bucketNumber,
                                   final HashMap<Binary, HashSet<EntryModifiedSubscription>> keySubscriptions) { // NOPMD

               keySubscriptions.forEachEntry(new ObjectObjectProcedure<Binary, HashSet<EntryModifiedSubscription>>() {

                  public boolean execute(final Binary key,
                                         final HashSet<EntryModifiedSubscription> subscriptions) { // NOPMD

                     for (final Iterator<EntryModifiedSubscription> iterator = subscriptions.iterator(); iterator.hasNext(); ) {

                        final EntryModifiedSubscription subscription = iterator.next();
                        if (subscription.getSubscriberAddress().equals(leftAddress)) {

                           // Remove
                           iterator.remove();

                           // Notify subscriber
                           if (entryEventSubscriptionConfigurationSubscriber != null) {

                              entryEventSubscriptionConfigurationSubscriber.notifySubscriptionRemoved(key, subscription, bucketNumber);
                           }
                        }
                     }
                     return true;
                  }
               });

               return true;
            }
         });


         // Add to the partition contributors left
         if (foundMember.isPartitionContributor()) {

            partitionContributorsLeft.add(leftAddress);
         }
      }

      // Remove from bucket ownership
      bucketOwnershipAssignment.removeBucketOwners(partitionContributorsLeft);
   }


   public void addEntryEventSubscription(final IntObjectHashMap<HashSet<Binary>> keysToProcess,
                                         final EntryModifiedSubscription subscription) {
      // Iterate through keys for that a subscription being added
      keysToProcess.forEachEntry(new IntObjectProcedure<HashSet<Binary>>() {

         public boolean execute(final int bucketNumber, final HashSet<Binary> keys) { // NOPMD

            // Get a map of keys to subscribers. Use Atomic to provide 'final' to 'forEach'
            final AtomicReference<HashMap<Binary, HashSet<EntryModifiedSubscription>>> keysToSubscriptions = new AtomicReference<HashMap<Binary, HashSet<EntryModifiedSubscription>>>(entryModifiedSubscriptions.get(bucketNumber));
            if (keysToSubscriptions.get() == null) {

               keysToSubscriptions.set(new HashMap<Binary, HashSet<EntryModifiedSubscription>>(1));
               entryModifiedSubscriptions.put(bucketNumber, keysToSubscriptions.get());
            }


            // Register subscription for each key
            keys.forEach(new ObjectProcedure<Binary>() {

               public boolean execute(final Binary key) {

                  // Get s set of subscriptions for the given key
                  HashSet<EntryModifiedSubscription> keyModificationSubscriptions = keysToSubscriptions.get().get(key);
                  if (keyModificationSubscriptions == null) {

                     keyModificationSubscriptions = new HashSet<EntryModifiedSubscription>(1);
                     keysToSubscriptions.get().put(key, keyModificationSubscriptions);
                  }

                  // Register new subscription
                  if (keyModificationSubscriptions.contains(subscription)) {

                     LOG.warn("Duplicate subscription: " + subscription);
                  } else {

                     // Add to group's subscription
                     keyModificationSubscriptions.add(subscription);

                     // Notify subscriber
                     if (entryEventSubscriptionConfigurationSubscriber != null) {

                        entryEventSubscriptionConfigurationSubscriber.notifySubscriptionAdded(key, subscription, bucketNumber);
                     }
                  }

                  return true;
               }
            });


            return true;
         }
      });
   }


   /**
    * Removes a subscription from a give set of keys.
    *
    * @param keysOfInterest     a set of keys to un-subscribe the subscriber from.
    * @param subscriberIdentity the identity of the subscriber to un-subscribe.
    */
   public void removeEntryModifiedSubscription(final IntObjectHashMap<HashSet<Binary>> keysOfInterest,
                                               final int subscriberIdentity) {

      // Keys is a bucket number, value is a map of keys to subscribers

      // Iterate through keys for that a subscription being removed
      keysOfInterest.forEachEntry(new IntObjectProcedure<HashSet<Binary>>() {

         public boolean execute(final int bucketNumber, final HashSet<Binary> keys) { // NOPMD

            // Get a map of keys to subscribers. Use Atomic to provide 'final' to 'forEach'
            final AtomicReference<HashMap<Binary, HashSet<EntryModifiedSubscription>>> keysToSubscriptions = new AtomicReference<HashMap<Binary, HashSet<EntryModifiedSubscription>>>(entryModifiedSubscriptions.get(bucketNumber));
            if (keysToSubscriptions.get() == null) {

               // REVIEWME: simeshev@cacheonix.org - Should we return
               // some kind of an error if over-un-subscription occurs?

               // No subscriptions, continue
               return true;
            }


            // Unregister subscription for each key
            keys.forEach(new ObjectProcedure<Binary>() {

               public boolean execute(final Binary key) {

                  // Get a set of subscriptions for the given key
                  final HashSet<EntryModifiedSubscription> keySubscriptions = keysToSubscriptions.get().get(key);
                  if (keySubscriptions == null) {

                     // REVIEWME: simeshev@cacheonix.org - Should we return
                     // some kind of an error if over-un-subscription occurs?

                     // No subscriptions, continue
                     return true;
                  }


                  // Un-subscribe using subscriber identity
                  for (final Iterator<EntryModifiedSubscription> iterator = keySubscriptions.iterator(); iterator.hasNext(); ) {

                     final EntryModifiedSubscription subscription = iterator.next();
                     if (subscription.getSubscriberIdentity() == subscriberIdentity) {

                        // Remove from the set
                        iterator.remove();

                        // Notify subscribers
                        if (entryEventSubscriptionConfigurationSubscriber != null) {

                           entryEventSubscriptionConfigurationSubscriber.notifySubscriptionRemoved(key, subscription, bucketNumber);
                        }
                     }
                  }


                  // Remove key because subscriptions are empty
                  if (keySubscriptions.isEmpty()) {

                     keysToSubscriptions.get().remove(key);
                  }

                  return true;
               }
            });

            // Remove keys-to-subscriptions from the per-bucket mapping
            if (keysToSubscriptions.get().isEmpty()) {

               entryModifiedSubscriptions.remove(bucketNumber);
            }

            return true;
         }
      });
   }


   public void configurePartition(final int replicaCount, final long partitionSizeBytes, final long maxElements) {

      Assert.assertTrue(replicaCount >= 0, "Replica count should be a greater or equal zero integer");
      Assert.assertTrue(partitionSizeBytes > 0L, "Partition size should be a positive long");
      Assert.assertTrue(!this.partitionConfigured, "Partition should not be configured");
      Assert.assertTrue(this.replicaCount == 0, "Replica count should not be initialized");
      Assert.assertTrue(this.partitionSizeBytes == 0L, "Partition size should not be initialized");

      // Init assignment
      this.bucketOwnershipAssignment = new BucketOwnershipAssignment(name, ConfigurationConstants.BUCKET_COUNT, replicaCount);
      this.replicaCount = replicaCount;
      this.partitionSizeBytes = partitionSizeBytes;
      this.maxElements = maxElements;
      this.partitionConfigured = true;
   }


   /**
    * Return version of this group.
    *
    * @return version of this group.
    */
   public long getVersion() {

      return version;
   }


   public void reattachGroupEventSubscriberList(final GroupEventSubscriberList groupEventSubscriberList) {

      this.groupEventSubscriberList = groupEventSubscriberList;
   }


   /**
    * Sets bucket event subscriber list.
    *
    * @param bucketEventListenerList bucket event subscriber list
    */
   public void reattachBucketEventListenerList(final BucketEventListenerList bucketEventListenerList) {

      this.bucketOwnershipAssignment.attachListeners(bucketEventListenerList);
   }


   public boolean isPartitionConfigured() {

      return partitionConfigured;
   }


   /**
    * Returns maximum size of a partition in bytes for this cache group.
    * <p/>
    * Partition size can be zero if a first partition contributor has not joined yet. After a first partition
    * contributor joins the cache group, it's partition size is used to set the group's partition size that remains the
    * same for the life time of the group.
    *
    * @return maximum size of a partition in bytes for this cache group.
    */
   public long getPartitionSizeBytes() {

      return partitionSizeBytes;
   }


   /**
    * Returns replica count for this cache group. Replica count is a non-negative integer.
    *
    * @return replica count for this cache group.
    */
   public int getReplicaCount() {

      return replicaCount;
   }


   public List<ClusterNodeAddress> getPartitionContributorsAddresses() {

      return bucketOwnershipAssignment.getPartitionContributorsAddresses();
   }


   /**
    * Returns number of buckets.
    *
    * @return number of buckets.
    */
   public int getBucketCount() {

      return bucketOwnershipAssignment.getBucketCount();
   }


   public ClusterNodeAddress getBucketOwner(final int storageNumber, final int bucketNumber) {

      return bucketOwnershipAssignment.getBucketOwnerAddress(storageNumber, bucketNumber);
   }


   public IntArrayList getOwnedBuckets(final int storageNumber, final ClusterNodeAddress ownerAddress) {

      return bucketOwnershipAssignment.getOwnedBuckets(storageNumber, ownerAddress);
   }


   public Set<ClusterNodeAddress> getBucketOwnersAddresses(final int storageNumber) {

      return bucketOwnershipAssignment.getBucketOwnersAddresses(storageNumber);
   }


   public int getBucketOwnerCount() {

      return bucketOwnershipAssignment.getBucketOwnerCount();
   }


   public BucketOwnershipAssignment getBucketOwnershipAssignment() {

      return bucketOwnershipAssignment;
   }


   public void setEntryEventSubscriptionConfigurationSubscriber(
           final EntryEventSubscriptionConfigurationSubscriber subscriber) {

      entryEventSubscriptionConfigurationSubscriber = subscriber;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.writeLong(version);
      out.writeInt(groupType);
      SerializerUtils.writeString(name, out);

      out.writeInt(members.size());
      for (final GroupMember member : members) {
         member.writeWire(out);
      }
      out.writeInt(replicaCount);
      out.writeBoolean(partitionConfigured);
      out.writeLong(partitionSizeBytes);
      out.writeLong(maxElements);
      bucketOwnershipAssignment.writeWire(out);
   }


   public void readWire(final DataInputStream in) throws IOException {

      version = in.readLong();
      groupType = in.readInt();
      name = SerializerUtils.readString(in);
      final int membersSize = in.readInt();
      members = new ArrayList<GroupMember>(membersSize);
      for (int i = 0; i < membersSize; i++) {
         final GroupMember member = new GroupMember();
         member.readWire(in);
         member.setGroup(this);
         members.add(member);
      }
      replicaCount = in.readInt();
      partitionConfigured = in.readBoolean();
      partitionSizeBytes = in.readLong();
      maxElements = in.readLong();
      bucketOwnershipAssignment = new BucketOwnershipAssignment();
      bucketOwnershipAssignment.readWire(in);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof Group)) {
         return false;
      }

      final Group group = (Group) o;

      if (partitionConfigured != group.partitionConfigured) {
         return false;
      }
      if (partitionSizeBytes != group.partitionSizeBytes) {
         return false;
      }
      if (replicaCount != group.replicaCount) {
         return false;
      }
      if (groupType != group.groupType) {
         return false;
      }
      if (version != group.version) {
         return false;
      }
      if (bucketOwnershipAssignment != null ? !bucketOwnershipAssignment.equals(group.bucketOwnershipAssignment) : group.bucketOwnershipAssignment != null) {
         return false;
      }
      if (members != null ? !members.equals(group.members) : group.members != null) {
         return false;
      }
      if (name != null ? !name.equals(group.name) : group.name != null) {
         return false;
      }

      return true;
   }


   /**
    * @noinspection NumericCastThatLosesPrecision
    */
   public int hashCode() {

      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + groupType;
      result = 31 * result + (members != null ? members.hashCode() : 0);
      result = 31 * result + (int) (version ^ (version >>> 32));
      result = 31 * result + (int) (partitionSizeBytes ^ (partitionSizeBytes >>> 32));
      result = 31 * result + replicaCount;
      result = 31 * result + (partitionConfigured ? 1 : 0);
      result = 31 * result + (bucketOwnershipAssignment != null ? bucketOwnershipAssignment.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "Group{" +
              "name='" + name + '\'' +
              ", type=" + groupType +
              ", version=" + version +
              ", members=" + members +
              ", partitionSizeBytes=" + partitionSizeBytes +
              ", replicaCount=" + replicaCount +
              ", partitionConfigured=" + partitionConfigured +
              ", bucketOwnershipAssignment=" + bucketOwnershipAssignment +
              ", groupEventSubscriberList=" + groupEventSubscriberList +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new Group();
      }
   }
}
