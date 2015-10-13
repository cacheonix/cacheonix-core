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
package org.cacheonix.impl.cluster.node.state;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cacheonix.exceptions.RuntimeClassNotFoundException;
import org.cacheonix.exceptions.RuntimeIOException;
import org.cacheonix.impl.cache.distributed.partitioned.BucketEventListener;
import org.cacheonix.impl.cache.distributed.partitioned.BucketEventListenerList;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.cluster.node.state.group.GroupEventSubscriber;
import org.cacheonix.impl.cluster.node.state.group.GroupEventSubscriberList;
import org.cacheonix.impl.cluster.node.state.group.GroupKey;
import org.cacheonix.impl.lock.LockRegistry;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A replicated destination for reliable multicast messages.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode,
 * TransientFieldInNonSerializableClass
 */
public final class ReplicatedState implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ReplicatedState.class); // NOPMD

   /**
    * A map of groups.
    */
   private Map<GroupKey, Group> groupMap = new ConcurrentHashMap<GroupKey, Group>(5);

   /**
    * A replicated state responsible for maintaining lock state information.
    */
   private LockRegistry lockRegistry = new LockRegistry();

   /**
    *
    */
   private final transient Map<Integer, GroupEventSubscriberList> groupSubscriberMap = new HashMap<Integer, GroupEventSubscriberList>(11);

   /**
    *
    */
   private final transient Map<Integer, BucketEventListenerList> bucketListenerMap = new HashMap<Integer, BucketEventListenerList>(11);


   /**
    * Resets this replicated state with the new one while preserving local subscribers to group events.
    *
    * @param newState state to reset. It is a hand-off object so it is OK to use references.
    */
   public void reset(final ReplicatedState newState) {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Resetting replicated state, new state: " + newState);
      }

      // Set group. NOTE: simeshev@cacheonix.org - 2009-08-13 - newState
      // is a hand-off object so it is OK to use references.
      this.groupMap = newState.groupMap;

      //
      for (final Group group : groupMap.values()) {

         if (group.getGroupType() == Group.GROUP_TYPE_CACHE) {

            reattachSubscribers(group);
         }
      }

      // Set lock registry
      this.lockRegistry = newState.lockRegistry;
   }


   /**
    * {@inheritDoc}
    */
   public ReplicatedState copy() throws RuntimeIOException, RuntimeClassNotFoundException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      try {
         return (ReplicatedState) ser.deserialize(ser.serialize(this));
      } catch (final IOException e) {
         throw new RuntimeIOException(e);
      }
   }


   public int getWireableType() {

      return TYPE_REPLICATED_STATE;
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      // Read group map
      final int size = in.readInt();
      groupMap = new HashMap<GroupKey, Group>(size);
      for (int i = 0; i < size; i++) {

         final Group group = new Group();
         group.readWire(in);
         registerGroup(group.getGroupType(), group.getName(), group);
      }

      //
      lockRegistry = new LockRegistry();
      lockRegistry.readWire(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      // Write group map
      final int size = groupMap.size();
      out.writeInt(size);
      for (final Map.Entry<GroupKey, Group> entry : groupMap.entrySet()) {
         entry.getValue().writeWire(out);
      }

      // Write lock registry
      lockRegistry.writeWire(out);
   }


   public void registerGroup(final int groupType, final String groupName, final Group cacheGroup) {

      groupMap.put(new GroupKey(groupType, groupName), cacheGroup);
   }


   public void reattachSubscribers(final Group cacheGroup) {

      // REVIEWME: simeshev@cacheonix.org - 2009-08-05 -> currently groupEventSubscriberList and bucketEventListenerList is shared with cache group.
      final int groupType = cacheGroup.getGroupType();
      cacheGroup.reattachGroupEventSubscriberList(getGroupEventSubscriberList(groupType));
      cacheGroup.reattachBucketEventListenerList(getBucketListenerList(groupType));
   }


   public Group getGroup(final int type, final String name) {

      final GroupKey groupKey = new GroupKey(type, name);
      return groupMap.get(groupKey);
   }


   /**
    * Returns an initialized group event subscriber list.
    *
    * @param groupType a group type.
    * @return the  initialized group event subscriber list.
    */
   public GroupEventSubscriberList getGroupEventSubscriberList(final int groupType) {

      GroupEventSubscriberList groupEventSubscriberList = groupSubscriberMap.get(Integer.valueOf(groupType));
      if (groupEventSubscriberList == null) {
         groupEventSubscriberList = new GroupEventSubscriberList();
         groupSubscriberMap.put(Integer.valueOf(groupType), groupEventSubscriberList);
      }
      return groupEventSubscriberList;
   }


   /**
    * Returns an initialized group event subscriber list.
    *
    * @param groupType a group type.
    * @return the initialized group event subscriber list.
    */
   private BucketEventListenerList getBucketListenerList(final int groupType) {

      BucketEventListenerList bucketEventListenerList = bucketListenerMap.get(Integer.valueOf(groupType));
      if (bucketEventListenerList == null) {
         bucketEventListenerList = new BucketEventListenerList();
         bucketListenerMap.put(Integer.valueOf(groupType), bucketEventListenerList);
      }
      return bucketEventListenerList;
   }


   public void notifyClusterNodesLeft(final Collection<ClusterNodeAddress> nodesLeft) {

      for (final Map.Entry<GroupKey, Group> groupKeyGroupEntry : groupMap.entrySet()) {

         groupKeyGroupEntry.getValue().removeMembers(nodesLeft);
      }
   }


   /**
    * Adds a subscriber to group membership events.
    *
    * @param groupType  group type. Group type is used as a key to the map that holds a list of subscribers for a
    *                   particular type.
    * @param subscriber subscriber to add.
    */
   public void addGroupEventSubscriber(final int groupType, final GroupEventSubscriber subscriber) {

      getGroupEventSubscriberList(groupType).add(subscriber);
   }


   public void addBucketEventListener(final int groupType, final BucketEventListener listener) {

      getBucketListenerList(groupType).add(listener);
   }


   /**
    * @noinspection RedundantIfStatement
    */
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final ReplicatedState that = (ReplicatedState) obj;

      if (groupMap != null ? !groupMap.equals(that.groupMap) : that.groupMap != null) {
         return false;
      }
      return true;
   }


   public int hashCode() {

      return groupMap != null ? groupMap.hashCode() : 0;
   }


   public Collection<Group> getGroups() {

      return groupMap.values();
   }


   public LockRegistry getLockRegistry() {

      return lockRegistry;
   }


   public String toString() {

      return "ReplicatedState{" +
              "groupMap=" + groupMap +
              ", lockRegistry=" + lockRegistry +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new ReplicatedState();
      }
   }
}
