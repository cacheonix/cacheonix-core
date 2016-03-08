package org.cacheonix.impl.net.cluster;

import java.util.List;
import java.util.Set;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * Marker list.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface ClusterView extends Wireable {

   void setOwner(ClusterNodeAddress owner);

   boolean isRepresentative();

   ClusterNodeAddress getNextElement();

   int getSize();

   boolean remove(ClusterNodeAddress clusterNodeAddress);

   void insert(ClusterNodeAddress predecessor, final ClusterNodeAddress address);

   /**
    * @return representative.
    */
   ClusterNodeAddress getRepresentative();

   /**
    * @param elementAfter existing element after that to return a element
    * @throws IllegalStateException if the element is not in the list
    */
   ClusterNodeAddress getNextElement(ClusterNodeAddress elementAfter)
           throws IllegalStateException;

   /**
    * Returns <code>true</code> if this marker list has a majority over te other list that we have common a ancestor
    * with. In other words, we and the another list a parts of some previous list.
    *
    * @param previousView
    * @return <code>true</code> if this marker list has a majority over other list.
    */
   boolean hasMajorityOver(ClusterView previousView);

   /**
    * Returns a copy of the process list.
    *
    * @return a copy of the process list.
    */
   List<ClusterNodeAddress> getClusterNodeList();

   /**
    * {@inheritDoc}
    */
   ClusterView copy();

   /**
    * Returns this cluster's unique ID.
    *
    * @return this cluster's unique ID.
    */
   UUID getClusterUUID();

   /**
    * Returns <code>true</code> if the cluster view contains active node.
    *
    * @param address ClusterNodeAddress to check.
    * @return <code>true</code> if the cluster view contains active node.
    */
   boolean contains(ClusterNodeAddress address);

   /**
    * Returns <code>true</code> if the cluster view contains active node.
    *
    * @param address ClusterNodeAddress to check.
    * @return <code>true</code> if the cluster view contains active node.
    */
   boolean contains(ReceiverAddress address);

   /**
    * Calculates a collection of members that have left as compared to the previous view.
    *
    * @param previousClusterView previous cluster view
    * @return a collection of members that have left as compared to this previous view.
    */
   Set<ClusterNodeAddress> calculateNodesLeft(ClusterView previousClusterView);

   /**
    * Calculates a collection of members that have joined as compared to the previous view.
    *
    * @param previousClusterView previous cluster view
    * @return a collection of members that have joined as compared to this previous view.
    */
   Set<ClusterNodeAddress> calculateNodesJoined(ClusterView previousClusterView);

   ClusterNodeAddress getNextElement(ReceiverAddress elementAfter);

   ClusterNodeAddress greatestMember();
}
