package org.cacheonix.impl.cluster.node.state;

import java.util.Collection;
import java.util.Map;

import org.cacheonix.impl.RuntimeIOException;
import org.cacheonix.impl.cache.distributed.partitioned.BucketEventListener;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.cluster.node.state.group.GroupEventSubscriber;
import org.cacheonix.impl.cluster.node.state.group.GroupEventSubscriberList;
import org.cacheonix.impl.cluster.node.state.group.GroupKey;
import org.cacheonix.impl.lock.LockRegistry;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * Replicated state of a cluster.
 */
public interface ReplicatedState extends Wireable {

   /**
    * Resets this replicated state with the new one while preserving local subscribers to group events.
    *
    * @param newState state to reset. It is a hand-off object so it is OK to use references.
    */
   void reset(ReplicatedState newState);

   /**
    * {@inheritDoc}
    */
   ReplicatedState copy() throws RuntimeIOException;

   /**
    * Returns a collection of groups managed by the replicated state.
    *
    * @return a collection of groups managed by the replicated state.
    */
   Map<GroupKey, Group> getGroupMap();

   /**
    * Registers a group to be managed by this replicated state.
    *
    */
   void registerGroup(int groupType, String groupName, Group cacheGroup);

   /**
    * Re-attached subscribers to the events happening to this group.
    */
   void reattachSubscribers(Group cacheGroup);

   /**
    * Returns a group identifies by this type and name.
    *
    * @param type group type.
    *
    * @param name group name.
    *
    * @return the found group or null if not found.
    */
   Group getGroup(int type, String name);

   /**
    * Returns an initialized group event subscriber list.
    *
    * @param groupType a group type.
    * @return the  initialized group event subscriber list.
    */
   GroupEventSubscriberList getGroupEventSubscriberList(int groupType);

   /**
    * Notifies subscribers that a set of nodes left the cluster.
    *
    * @param nodesLeft the collection of nodes leaving the cluster.
    */
   void notifyClusterNodesLeft(Collection<ClusterNodeAddress> nodesLeft);

   /**
    * Adds a subscriber to group membership events.
    *
    * @param groupType  group type. Group type is used as a key to the map that holds a list of subscribers for a
    *                   particular type.
    * @param subscriber subscriber to add.
    */
   void addGroupEventSubscriber(int groupType, GroupEventSubscriber subscriber);

   void addBucketEventListener(int groupType, BucketEventListener listener);

   /**
    * Returns a list of groups managed by this replicated state.
    *
    * @return a list of groups managed by this replicated state.
    */
   Collection<Group> getGroups();

   /**
    * Returns a lock registry managed by this replicated state.
    *
    * @return a lock registry managed by this replicated state.
    */
   LockRegistry getLockRegistry();
}
