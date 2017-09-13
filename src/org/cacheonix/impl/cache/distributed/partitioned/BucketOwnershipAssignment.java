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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.IntegerUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.IntObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * CacheGroup
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection RedundantIfStatement, NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode,
 * StandardVariableNames, ImplicitNumericConversion, UnnecessaryUnboxing
 */
public final class BucketOwnershipAssignment implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketOwnershipAssignment.class); // NOPMD

   /**
    * A map of bucket numbers to owner addresses.
    */
   private AtomicReferenceArray<ClusterNodeAddress>[] bucketAssignments = null;

   /**
    * A map of bucket owner addresses to bucket owners. Bucket owners carry information about what buckets they own and
    * what buckets are being transferred in or out.
    * <p/>
    * NOTE: simeshev@cacheonix.org - 2010-02-10 - It it *critical* for consistent bucket ownership assignment that the
    * TreeMap (using natural sort order of keys) is used. HashMap does not work order of traversal depends on order of
    * insertion, and it is not always guaranteed that the nodes are inserted in the same order. See bug CACHEONIX-168
    * for more information.
    *
    * @see BucketOwner
    * @see BucketTransfer
    */
   private TreeMap<ClusterNodeAddress, BucketOwner>[] bucketOwners = null; // NOPMD

   /**
    * Cache name.
    */
   private String cacheName = null;

   /**
    * @noinspection CanBeFinal
    */
   private transient BucketEventListenerList listeners = new BucketEventListenerList();


   /**
    * Constructor.
    *
    * @param cacheName    a cache name.
    * @param bucketCount  number of buckets.
    * @param replicaCount number of replicas.
    */
   public BucketOwnershipAssignment(final String cacheName, final int bucketCount, final int replicaCount) {

      this.cacheName = cacheName;
      this.bucketAssignments = createBucketAssignments(replicaCount, bucketCount);
      this.bucketOwners = createBucketOwners(replicaCount);
   }


   /**
    * Default constructor required by <code>Externalizable</code>.
    */
   public BucketOwnershipAssignment() {

   }


   /**
    * Adds a bucket owner. Adding a bucket owner causes repartitioning.
    *
    * @param addr bucket owner address to add.
    */
   @SuppressWarnings("unchecked")
   public void addBucketOwner(final ClusterNodeAddress addr) {

      final byte replicaCount = getReplicaCount();
      for (byte storageNumber = 0; storageNumber <= replicaCount; storageNumber++) {
         final BucketOwner newOwner = new BucketOwner(getReplicaCount(), addr);
         final BucketOwner previousOwner = bucketOwners[storageNumber].put(addr, newOwner);
         Assert.assertNull(previousOwner, "Previous owner should be null: {0}", previousOwner);
      }

      // Repartition
      repartition();
   }


   public void removeBucketOwners(final Collection<ClusterNodeAddress> removedAddresses) {

      if (removedAddresses.isEmpty()) {

         return;
      }

      for (final ClusterNodeAddress removedAddr : removedAddresses) {

         // Process primary storage
         final byte primaryStorageNumber = (byte) 0;
         final BucketOwner removedPrimaryOwner = bucketOwners[primaryStorageNumber].remove(removedAddr);
         cancelInboundTransfers(primaryStorageNumber, removedAddr, removedPrimaryOwner);
         cancelOutboundTransfers(primaryStorageNumber, removedAddr, removedPrimaryOwner);
         cancelOutboundReplicas(removedAddr, removedPrimaryOwner);
         orphanOwnedBuckets(primaryStorageNumber, removedPrimaryOwner);

         // Process replicas
         final byte replicaCount = getReplicaCount();
         for (byte replicaStorageNumber = 1; replicaStorageNumber <= replicaCount; replicaStorageNumber++) {

            final BucketOwner removedReplicaOwner = bucketOwners[replicaStorageNumber].remove(removedAddr);
            cancelInboundTransfers(replicaStorageNumber, removedAddr, removedReplicaOwner);
            cancelInboundReplicas(replicaStorageNumber, removedAddr, removedReplicaOwner);
            cancelOutboundTransfers(replicaStorageNumber, removedAddr, removedReplicaOwner);
            orphanOwnedBuckets(replicaStorageNumber, removedReplicaOwner);
         }
      }

      // Repartition
      repartition();
   }


   /**
    * Returns number of buckets.
    *
    * @return number of buckets.
    */
   public int getBucketCount() {

      return bucketAssignments[0].length();
   }


   /**
    * Returns an owner of primary bucket or null if primary bucket owner is not set.
    *
    * @param bucketNumber bucket number
    * @return an owner of primary bucket.
    */
   ClusterNodeAddress getPrimaryOwnerAddress(final int bucketNumber) {

      return getBucketOwnerAddress((byte) 0, bucketNumber);
   }


   /**
    * Returns an owner of a bucket or null if a bucket owner is not set.
    *
    * @param storageNumber storage number
    * @param bucketNumber  bucket number
    * @return an owner of a bucket.
    */
   public ClusterNodeAddress getBucketOwnerAddress(final int storageNumber, final int bucketNumber) {

      return bucketAssignments[0].length() == 0 ? null : bucketAssignments[storageNumber].get(bucketNumber);
   }


   /**
    * Marks bucket owner leaving.
    *
    * @param leavingAddress the address of the leaving bucket owner.
    */
   public void markBucketOwnerLeaving(final ClusterNodeAddress leavingAddress) {

      // Mark owners in all storages as leaving
      for (final TreeMap<ClusterNodeAddress, BucketOwner> map : bucketOwners) {

         map.get(leavingAddress).markLeaving();
      }
   }


   public boolean hasBucketResponsibilities(final ClusterNodeAddress leavingAddress) {

      // Iterate storages
      for (final TreeMap<ClusterNodeAddress, BucketOwner> map : bucketOwners) {

         final BucketOwner bucketOwner = map.get(leavingAddress);
         if (bucketOwner.hasBucketResponsibilities()) {

            return true;
         }
      }

      return false;
   }


   /**
    * Returns <code>true</code> if all bucket owners are leaving. Otherwise returns <code>false</code>.
    *
    * @return <code>true</code> if all bucket owners are leaving. Otherwise returns <code>false</code>.
    */
   public boolean isAllBucketOwnersLeaving() {

      for (final Entry<ClusterNodeAddress, BucketOwner> entry : bucketOwners[0].entrySet()) {
         if (!entry.getValue().isLeaving()) {
            return false;
         }
      }
      return true;
   }


   /**
    * Returns a copy of a list of buckets owned by a given address.
    *
    * @param storageNumber a storage number.
    * @param ownerAddress  an owner address.
    * @return the copy of a list of buckets owned by a given address.
    */
   int getOwnedBucketCount(final int storageNumber, final ClusterNodeAddress ownerAddress) {

      final BucketOwner bucketOwner = bucketOwners[storageNumber].get(ownerAddress);
      final Collection<Integer> ownedBuckets = bucketOwner.getOwnedBuckets();
      return ownedBuckets.size();
   }


   /**
    * Returns number of replicas.
    *
    * @return number of replicas.
    */
   public byte getReplicaCount() {
      //noinspection NumericCastThatLosesPrecision
      return (byte) (bucketAssignments.length - 1);
   }


   /**
    * Returns number of owners.
    *
    * @return number of owners.
    */
   public int getBucketOwnerCount() {

      return bucketOwners[0].size();
   }


   @SuppressWarnings("unchecked")
   public List<ClusterNodeAddress> getPartitionContributorsAddresses() {

      final Map<ClusterNodeAddress, BucketOwner> primaryBucketOwners = bucketOwners[0];
      final List<ClusterNodeAddress> result = new ArrayList<ClusterNodeAddress>(primaryBucketOwners.size());
      final Set<Entry<ClusterNodeAddress, BucketOwner>> entries = primaryBucketOwners.entrySet();
      for (final Entry<ClusterNodeAddress, BucketOwner> entry : entries) {
         result.add(entry.getKey());
      }
      return result;
   }


   /**
    * Calculates the maximum number of buckets owned by an owner in a given storage.
    *
    * @param storageNumber storage number.
    * @return maximum number of buckets owned by an owner in a given storage.
    */
   int calculateMaxOwnedBucketCount(final int storageNumber) {

      int result = 0;
      for (final Entry<ClusterNodeAddress, BucketOwner> entry : bucketOwners[storageNumber].entrySet()) {
         final BucketOwner bucketOwner = entry.getValue();
         result = Math.max(result, bucketOwner.ownedBucketCount());
      }
      return result;
   }


   public void attachListeners(final BucketEventListenerList bucketEventListenerList) {

      this.listeners = bucketEventListenerList;
   }


   /**
    * Processes a replicated bucket transfer completed announcement by finishing bucket transfer.
    *
    * @param sourceStorageNumber      the source storage number.
    * @param destinationStorageNumber the destination storage number.
    * @param previousOwnerAddress     a previous owner address.
    * @param newOwnerAddress          a new owner address.
    * @param bucketNumbers            a list of bucket numbers
    */
   public void finishBucketTransfer(final byte sourceStorageNumber, final byte destinationStorageNumber,
                                    final ClusterNodeAddress previousOwnerAddress,
                                    final ClusterNodeAddress newOwnerAddress, final List<Integer> bucketNumbers) {

      //noinspection ControlFlowStatementWithoutBraces
//      if (LOG.isDebugEnabled())
//         LOG.debug("Finishing bucket transfer: sourceStorage: " + sourceStorageNumber + ", destinationStorage: " + destinationStorageNumber + ", previousOwner: " + previousOwnerAddress + ", newOwner: " + newOwnerAddress + ", buckets: " + bucketNumbers); // NOPMD

      if (sourceStorageNumber == destinationStorageNumber) {

         finishStorageBucketTransfer(sourceStorageNumber, destinationStorageNumber, previousOwnerAddress, newOwnerAddress, bucketNumbers);
      } else if (sourceStorageNumber == 0 && destinationStorageNumber > 0) {

         finishReplicaRestoreTransfer(destinationStorageNumber, previousOwnerAddress, newOwnerAddress, bucketNumbers);
      } else {

         throw new IllegalStateException("Illegal combination of sourceStorage (" + sourceStorageNumber + ") and destinationStorage (" + destinationStorageNumber + ')');
      }
   }


   private void finishStorageBucketTransfer(final byte sourceStorageNumber, final byte destinationStorageNumber,
                                            final ClusterNodeAddress previousOwnerAddress,
                                            final ClusterNodeAddress newOwnerAddress,
                                            final List<Integer> bucketNumbers) {

      final Map<ClusterNodeAddress, BucketOwner> bucketOwnership = bucketOwners[sourceStorageNumber];

      final BucketOwner previousOwner = bucketOwnership.get(previousOwnerAddress);
      if (previousOwner == null) {

         throw new IllegalStateException("Previous owner is null, sourceStorageNumber: " + sourceStorageNumber
                 + ", previousOwnerAddress:" + previousOwnerAddress + ", newOwnerAddress:" + newOwnerAddress
                 + ", bucketNumbers.size:" + bucketNumbers.size());
      }

      final BucketOwner newOwner = bucketOwnership.get(newOwnerAddress);
      if (newOwner == null) {

         throw new IllegalStateException("New owner is null, sourceStorageNumber: " + sourceStorageNumber
                 + ", previousOwnerAddress:" + previousOwnerAddress + ", newOwnerAddress:" + newOwnerAddress
                 + ", bucketNumbers.size:" + bucketNumbers.size());
      }

      // Update bucket assignment
      for (final Integer bucketNumber : bucketNumbers) {

         // Update bucket assignments
         bucketAssignments[sourceStorageNumber].set(bucketNumber, newOwnerAddress);

         // Complete transfers in progress
         previousOwner.completeOutboundTransfer(bucketNumber);
         newOwner.completeInboundTransfer(bucketNumber);
      }

      // Notify listeners
      final FinishBucketTransferCommand command = new FinishBucketTransferCommand(cacheName, sourceStorageNumber, destinationStorageNumber, previousOwnerAddress, newOwnerAddress);
      command.addBucketNumbers(bucketNumbers);
      listeners.execute(command);

      // New owner got buckets and it is leaving, so it should start getting rid of them
      if (newOwner.isLeaving() || !isRepartitionInProgress(sourceStorageNumber)) {

         repartition();
      }
   }


   /**
    * Adjusts replicated structures and calls <code>repartition</code> if any of the owners is leaving.
    *
    * @param replicaStorageNumber a storage number.
    * @param primaryOwnerAddress  a primary owner address.
    * @param replicaOwnerAddress  a replica owner address.
    * @param bucketNumbers        a list of bucket numbers.
    */
   private void finishReplicaRestoreTransfer(final byte replicaStorageNumber,
                                             final ClusterNodeAddress primaryOwnerAddress,
                                             final ClusterNodeAddress replicaOwnerAddress,
                                             final List<Integer> bucketNumbers) {

      final AtomicReferenceArray<ClusterNodeAddress> bucketAssignment = bucketAssignments[replicaStorageNumber];

      final BucketOwner primaryOwner = bucketOwners[0].get(primaryOwnerAddress);
      if (primaryOwner == null) {

         throw new IllegalStateException("Primary owner is null, storageNumber: " + replicaStorageNumber
                 + ", primaryOwnerAddress:" + primaryOwnerAddress + ", replicaOwnerAddress:" + replicaOwnerAddress
                 + ", bucketNumbers.size:" + bucketNumbers.size());
      }

      final BucketOwner replicaOwner = bucketOwners[replicaStorageNumber].get(replicaOwnerAddress);
      if (replicaOwner == null) {

         throw new IllegalStateException("Replica owner is null, storageNumber: " + replicaStorageNumber
                 + ", primaryOwnerAddress:" + primaryOwnerAddress + ", replicaOwnerAddress:" + replicaOwnerAddress
                 + ", bucketNumbers.size:" + bucketNumbers.size());
      }

      for (final Integer bucketNumber : bucketNumbers) {

         // Update bucket assignments
         bucketAssignment.set(bucketNumber, replicaOwnerAddress);

         // Remove from replica restore in progress
         primaryOwner.completeOutboundReplicaRestore(replicaStorageNumber, bucketNumber);
         replicaOwner.completeInboundReplicaRestore(bucketNumber);
      }

      // Notify listeners
      final FinishBucketTransferCommand command = new FinishBucketTransferCommand(cacheName, (byte) 0,
              replicaStorageNumber, primaryOwnerAddress, replicaOwnerAddress);
      command.addBucketNumbers(bucketNumbers);

      listeners.execute(command);

      // New replica owner got buckets and it is leaving, so it should start getting rid of them.
      // Or, primary owner is leaving and now doesn't have a responsibility for the buckets.
      if (primaryOwner.isLeaving() || replicaOwner.isLeaving() || !isRepartitionInProgress(replicaStorageNumber)) {

         repartition();
      }
   }


   /**
    * Returns <code>true</code> if there is a repartition in progress. Otherwise returns <code>false</code>.
    *
    * @param storageNumber the storage number to check.
    * @return <code>true</code> if there is a repartition in progress. Otherwise returns <code>false</code>.
    */
   private boolean isRepartitionInProgress(final byte storageNumber) {

      final TreeMap<ClusterNodeAddress, BucketOwner> storageBucketOwners = bucketOwners[storageNumber];
      for (final BucketOwner owner : storageBucketOwners.values()) {

         if (owner.hasInboundBuckets() || owner.hasInboundReplicas() || owner.hasOutboundBuckets() || owner.hasOutboundReplicas()) {

            return true;
         }
      }

      return false;
   }


   /**
    * Processes a replicated bucket transfer rejected announcement by reverting a bucket transfer.
    *
    * @param sourceStorageNumber      a source storage number.
    * @param destinationStorageNumber a destination storage number.
    * @param previousOwnerAddress     a previous owner.
    * @param newOwnerAddress          a new owner.
    * @param bucketNumbers            a list of bucket numbers.
    */
   public void rejectBucketTransfer(final byte sourceStorageNumber, final byte destinationStorageNumber,
                                    final ClusterNodeAddress previousOwnerAddress,
                                    final ClusterNodeAddress newOwnerAddress, final Collection<Integer> bucketNumbers) {


      Assert.assertTrue(sourceStorageNumber == destinationStorageNumber || sourceStorageNumber == 0 && destinationStorageNumber > 0, "Transfer should be either in-storage or replica restore, sourceStorageNumber: {0}, destinationStorageNumber: {1}", sourceStorageNumber, destinationStorageNumber);

      final Map<ClusterNodeAddress, BucketOwner> bucketOwnership = bucketOwners[sourceStorageNumber];
      final BucketOwner currentOwner = bucketOwnership.get(previousOwnerAddress);
      if (currentOwner == null) {
         // Transfer has been canceled by removal of the previous owner
         return;
      }

      final BucketOwner newOwner = bucketOwnership.get(newOwnerAddress);
      if (newOwner == null) {
         // Transfer has been canceled by removal of the previous owner
         return;
      }

      if (sourceStorageNumber == destinationStorageNumber) {

         for (final Integer bucketNumber : bucketNumbers) {

            // Cancel ordinary transfer
            currentOwner.cancelOutboundTransfer(bucketNumber);

//            if (LOG.isDebugEnabled())
//               LOG.debug("Canceling inbound transfer, storageNumber: " + sourceStorageNumber + ", bucketNumber: " + bucketNumber + ", newOwner: " + newOwner); // NOPMD
            newOwner.cancelInboundTransfer(bucketNumber);
         }
      } else if (sourceStorageNumber == 0 && destinationStorageNumber > 0) {

         // Cancel replica restore
         for (final Integer bucketNumber : bucketNumbers) {

            currentOwner.cancelOutboundReplica(destinationStorageNumber, bucketNumber);
            newOwner.cancelInboundReplica(bucketNumber);
         }
      }


      // Notify listeners -
      final CancelBucketTransferCommand command = new CancelBucketTransferCommand(cacheName, sourceStorageNumber,
              destinationStorageNumber, previousOwnerAddress, newOwnerAddress);
      command.addBucketNumbers(bucketNumbers);
      listeners.execute(command);

      // Repartition
      repartition();
   }


   /**
    * Repartitions bucket ownership assignment and notifies asynchronous listeners to execute appropriate commands.
    */
   public void repartition() {

      // Check if any owners left
      if (bucketOwners[0].isEmpty()) {
         return;
      }

      final byte replicaCount = getReplicaCount();
      final int bucketCount = getBucketCount();
      final int fairBucketsPerNode = calculateFairBucketsPerNode();


      for (byte storageNumber = 0; storageNumber <= replicaCount; storageNumber++) {

//         final AtomicReferenceArray<ClusterNodeAddress> bucketAssignment = bucketAssignments[storageNumber];

         // --------------------------------------------------------------------------
         //
         // Transfer buckets belonging to the leaving owners ignoring bucket load of
         // new owners. It is done before rebalancing because the leaving nodes are in
         // danger of hard shutdown and consequent loss of data.
         //
         // --------------------------------------------------------------------------

         final Map<ClusterNodeAddress, BucketOwner> storageOwnerMap = bucketOwners[storageNumber];
         for (final Entry<ClusterNodeAddress, BucketOwner> entry : storageOwnerMap.entrySet()) {

            final BucketOwner owner = entry.getValue();
            if (!owner.isLeaving()) {

               continue;
            }

            transferLeaving(storageNumber, owner);
         }


         // --------------------------------------------------------------------------
         //
         // Collect orphans
         //
         // --------------------------------------------------------------------------

         final List<Integer> orphans = new LinkedList<Integer>();
         final AtomicReferenceArray<ClusterNodeAddress> bucketAssignment = bucketAssignments[storageNumber];
         for (int bucketNumber = 0; bucketNumber < bucketCount; bucketNumber++) {

            if (bucketAssignment.get(bucketNumber) == null) {

               orphans.add(IntegerUtils.valueOf(bucketNumber));
            }
         }

         // --------------------------------------------------------------------------
         //
         // Restore primary orphans from local replicas
         //
         // --------------------------------------------------------------------------

         if (storageNumber == 0 && replicaCount > 0 && !orphans.isEmpty()) {

            RestoreBucketCommand command = null;
            for (byte fromStorageNumber = 1; fromStorageNumber <= replicaCount; fromStorageNumber++) {

               // Find first non-null replica
               for (final Iterator<Integer> iterator = orphans.iterator(); iterator.hasNext(); ) {
                  final Integer orphanedBucketNumber = iterator.next();

                  // Pick replica
                  final ClusterNodeAddress replicaAddress = bucketAssignments[fromStorageNumber].get(orphanedBucketNumber);
                  if (replicaAddress == null) {

                     continue;
                  }

                  // When restoring a primary bucket from a local replica, the restore cannot be requested, if
                  // the replica bucket is being transferred as a result of repartitioning. It does not make
                  // sense to send a command to restore because the replica bucket may not be there by the
                  // time the command to restore the primary owner is received. The replica bucket is being
                  // transferred if it is in its owner's outboundTransfers registry. The BOAT should try to
                  // find another replica or leave the primary bucket orphaned. Next repartitioning cycle will
                  // try to adopt it again.
                  final BucketOwner replicaOwner = bucketOwners[fromStorageNumber].get(replicaAddress);
                  if (replicaOwner.isTransferringBucket(orphanedBucketNumber)) {

                     continue;
                  }

                  // Doesn't make sense to restore from this replica
                  // owner because it is leaving cache group.
                  if (replicaOwner.isLeaving()) {

                     continue;
                  }

                  // Assign bucket
                  bucketAssignments[0].set(orphanedBucketNumber, replicaAddress);
                  final BucketOwner primaryOwner = bucketOwners[0].get(replicaAddress);
                  primaryOwner.addOwnedBucketNumber(orphanedBucketNumber);

                  // Remove from replica ownership
                  bucketAssignments[fromStorageNumber].set(orphanedBucketNumber, null);
                  final boolean bucketExisted = replicaOwner.getOwnedBuckets().remove(orphanedBucketNumber);
                  Assert.assertTrue(bucketExisted, "Bucket {0} should have been owned by replica owner {1}", orphanedBucketNumber, replicaOwner);

                  // Add to command
                  if (command == null) {

                     command = new RestoreBucketCommand(cacheName, fromStorageNumber, replicaAddress);
                  } else {

                     if (command.getFromStorageNumber() != fromStorageNumber || !command.getAddress().equals(replicaAddress)) {

                        listeners.execute(command);
                        command = new RestoreBucketCommand(cacheName, fromStorageNumber, replicaAddress);
                     }
                  }

                  command.addBucketNumber(orphanedBucketNumber);

                  // Done
                  iterator.remove();
               }
            }

            if (command != null) {
               listeners.execute(command);
            }
         }

         // --------------------------------------------------------------------------
         //
         // Collect overloadedMap and underloadedMap; transfer leaving.
         //
         // --------------------------------------------------------------------------
         final HashMap<ClusterNodeAddress, BucketOwner> underloadedOwners = new HashMap<ClusterNodeAddress, BucketOwner>(1);
         final HashMap<ClusterNodeAddress, BucketOwner> overloadedOwners = new HashMap<ClusterNodeAddress, BucketOwner>(1);
         for (final Entry<ClusterNodeAddress, BucketOwner> entry : storageOwnerMap.entrySet()) {

            final ClusterNodeAddress ownerAddress = entry.getKey();
            final BucketOwner owner = entry.getValue();
            if (owner.isLeaving()) {

               continue;
            }

            final int loadCount = owner.load();
            if (loadCount > fairBucketsPerNode) {

               overloadedOwners.put(ownerAddress, owner);
            } else if (loadCount < fairBucketsPerNode) {

               underloadedOwners.put(ownerAddress, owner);
            }
         }


         // --------------------------------------------------------------------------
         //
         // Process orphaned buckets
         //
         // --------------------------------------------------------------------------

         if (!orphans.isEmpty()) {

            if (storageNumber == 0) {

               // If there are orphans, and this is a primary storage, this means that primary orphans could
               // not be restored from replicas by the code above. This means bucket loss. We just assign
               // orphaned buckets using a load balancing algorithm.
               assignPrimaryOrphans(storageNumber, fairBucketsPerNode, storageOwnerMap, underloadedOwners, orphans);
            } else {

               restoreReplicaOrphans(storageNumber, fairBucketsPerNode, storageOwnerMap, underloadedOwners, orphans);
            }
         }

         // --------------------------------------------------------------------------
         //
         // Rebalance
         //
         // --------------------------------------------------------------------------

         if (replicaCount == 0) {

            rebalanceStorageSimple(storageNumber, fairBucketsPerNode, underloadedOwners, overloadedOwners);
         } else {

            rebalanceStorageWithReplicas(storageNumber, fairBucketsPerNode, underloadedOwners, overloadedOwners);
         }
      }
   }


   /**
    * Restores replica orphanedReplicaBuckets in a configuration with replicas.
    *
    * @param replicaStorageNumber     the replica storage number
    * @param fairBucketsPerNode       fair buckets per node
    * @param storageOwnerMap          the replica's storage's owners.
    * @param underloadedReplicaOwners a list of underloaded owners in this storage number
    * @param orphanedReplicaBuckets   a list of orphaned replicas
    */
   private void restoreReplicaOrphans(final byte replicaStorageNumber, final int fairBucketsPerNode,
                                      final Map<ClusterNodeAddress, BucketOwner> storageOwnerMap,
                                      final HashMap<ClusterNodeAddress, BucketOwner> underloadedReplicaOwners, // NOPMD
                                      final List<Integer> orphanedReplicaBuckets) {

      Assert.assertTrue(replicaStorageNumber > 0, "Storage number should be greater then zero: {0}", replicaStorageNumber);

      // Prepare batch command
      BeginBucketTransferCommand command = null;

      // First, use underloaded to adopt orphanedReplicaBuckets
      for (final Iterator<Integer> orphanIter = orphanedReplicaBuckets.iterator(); orphanIter.hasNext(); ) {
         final Integer orphanedReplicaBucketNumber = orphanIter.next();

         // Get primary owner
         final ClusterNodeAddress primaryAddr = getPrimaryOwnerAddress(orphanedReplicaBucketNumber);
         if (primaryAddr == null) {
            orphanIter.remove();
            continue;
         }


         // Don't begin if primary owner is leaving. The leaving primary owner owner will only transfer
         // the bucket to the new owner and that new owner will later take care of restoring a replica.
         final BucketOwner primaryOwner = bucketOwners[0].get(primaryAddr);
         if (primaryOwner.isLeaving()) {

            // Leave the orphaned replica for now. Another round of repartitioning should pick it up.
            orphanIter.remove();
            continue;
         }

         // Do not begin if the primary bucket is being transferred as a result of repartitioning. It
         // does not make sense to send a command to begin restoring the replica because the primary bucket
         // will not be there by the time the command to restore the replica is processed. The primary bucket
         // is being transferred if it is in its owner's outboundTransfers registry.
         if (primaryOwner.isTransferringBucket(orphanedReplicaBucketNumber)) {

            // Leave the orphaned replica for now. Another round of repartitioning should pick it up.
            orphanIter.remove();
            continue;
         }

         // Don't begin it the primary owner is already handling replica restore for any replica
         // storage (including this one). Primary can handle only one restore at a time.
         if (primaryOwner.isRestoringReplicas(orphanedReplicaBucketNumber)) {
            orphanIter.remove();
            continue;
         }

         // Find safe owner
         BucketOwner safeReplicaOwner = findSafeOwner(orphanedReplicaBucketNumber, underloadedReplicaOwners);
         if (safeReplicaOwner == null) {

            // Cannot find underloaded owner, try all
            safeReplicaOwner = findSafeOwner(orphanedReplicaBucketNumber, storageOwnerMap);
            if (safeReplicaOwner == null) {

               // This bucket number cannot be safely transferred to ANY of the nodes
               continue;
            }
         } else {

            // Adjust underloaded list
            if (safeReplicaOwner.underload(fairBucketsPerNode) == 0) {
               underloadedReplicaOwners.remove(safeReplicaOwner.getAddress());
            }
         }


         // Request listeners to begin restore
         command = trackOrBeginTransfer(command, (byte) 0, replicaStorageNumber, orphanedReplicaBucketNumber, primaryOwner, safeReplicaOwner);

         // Clear handled bucket
         orphanIter.remove();
      }

      if (command != null) {
         listeners.execute(command);
      }
   }


   /**
    * Assigns orphans for cases when a primary orphan cannot be restored from replica or if there are no replicas
    * configured. First, it uses underloaded owners. If there are orphaned buckets left after that, it evenly
    * distributes orphans across bucket owners.
    *
    * @param storageNumber      a storage number.
    * @param fairBucketsPerNode fair buckets per node
    * @param bucketOwnership    bucket ownership
    * @param underloadedMap     a list of underloaded owners
    * @param orphans            a list of orphans
    */
   private void assignPrimaryOrphans(final byte storageNumber, final int fairBucketsPerNode,
                                     final Map<ClusterNodeAddress, BucketOwner> bucketOwnership,
                                     final HashMap<ClusterNodeAddress, BucketOwner> underloadedMap, // NOPMD
                                     final List<Integer> orphans) {

      // Create iterator of orphans to consume
      final Iterator<Integer> orphansIter = orphans.iterator();

      // First, use underloaded to adopt orphans
      for (final Iterator<Entry<ClusterNodeAddress, BucketOwner>> underloadedIter = underloadedMap.entrySet().iterator(); underloadedIter.hasNext() && orphansIter.hasNext(); ) {

         final BucketOwner underloadedOwner = underloadedIter.next().getValue();
         while (orphansIter.hasNext() && underloadedOwner.underload(fairBucketsPerNode) > 0) {

            final Integer orphanedBucketNumber = orphansIter.next();
            assignOrphanToOwner(storageNumber, orphanedBucketNumber, underloadedOwner);
         }

         // Remove from underloaded if filled
         if (underloadedOwner.underload(fairBucketsPerNode) == 0) {

            underloadedIter.remove();
         }
      }

      // Second, use all owners to adopt left orphans
      Iterator<Entry<ClusterNodeAddress, BucketOwner>> ownershipIter = bucketOwnership.entrySet().iterator();
      while (orphansIter.hasNext()) {

         final BucketOwner owner = ownershipIter.next().getValue();
         final Integer orphanedBucketNumber = orphansIter.next();
         assignOrphanToOwner(storageNumber, orphanedBucketNumber, owner);

         // Reset destination iterator if needed
         if (!ownershipIter.hasNext()) {

            ownershipIter = bucketOwnership.entrySet().iterator();
         }
      }
   }


   /**
    * Rebalances bucket ownership assignment in a configuration with replicas.
    *
    * @param storageNumber      storage number
    * @param fairBucketsPerNode fair buckets per node
    * @param underloadedMap     underloaded map
    * @param overloadedMap      overloaded map
    */
   private void rebalanceStorageWithReplicas(final byte storageNumber, final int fairBucketsPerNode,
                                             final HashMap<ClusterNodeAddress, BucketOwner> underloadedMap, // NOPMD
                                             final Map<ClusterNodeAddress, BucketOwner> overloadedMap) {

      BeginBucketTransferCommand command = null;

      for (final Iterator<Entry<ClusterNodeAddress, BucketOwner>> overloadedIter = overloadedMap.entrySet().iterator(); overloadedIter.hasNext() && !underloadedMap.isEmpty(); ) {

         final BucketOwner overloadedOwner = overloadedIter.next().getValue();

         for (final Iterator<Integer> bucketIter = overloadedOwner.getOwnedBuckets().iterator(); bucketIter.hasNext() && overloadedOwner.overload(fairBucketsPerNode) > 0 && !underloadedMap.isEmpty(); ) {

            final int bucketNumber = bucketIter.next();

            // A transfer for a bucket in the primary storage cannot begin if the primary storage is
            // restoring a replica (such a bucket is registered in outboundReplicas). This means
            // that while restore is in progress, the bucket is going to be locked for an update.
            // That's why it does not make sense to send a command to begin transfer. It will be
            // most likely rejected.
            final BucketOwner primaryOwner = getPrimaryOwner(bucketNumber);
            if (primaryOwner == null || storageNumber == 0 && primaryOwner.isRestoringReplicas(bucketNumber)) {
               continue;
            }

            // Find a safe owner in underloaded
            final BucketOwner underloadedOwner = findSafeOwner(bucketNumber, underloadedMap);
            if (underloadedOwner == null) {
               continue; // This bucket number cannot be safely transferred
            }

            // Transfer
            bucketIter.remove();

            // Begin bucket transfer
            command = trackOrBeginTransfer(command, storageNumber, storageNumber, bucketNumber, overloadedOwner, underloadedOwner);

            // Adjust underloaded list
            if (underloadedOwner.underload(fairBucketsPerNode) == 0) {
               underloadedMap.remove(underloadedOwner.getAddress());
            }
         }
      }

      if (command != null) {
         listeners.execute(command);
      }
   }


   private BeginBucketTransferCommand trackOrBeginTransfer(final BeginBucketTransferCommand aCommand,
                                                           final byte sourceStorageNumber,
                                                           final byte destinationStorageNumber, final int bucketNumber,
                                                           final BucketOwner transferFrom,
                                                           final BucketOwner transferTo) {

      BeginBucketTransferCommand command = aCommand;

      // Register transfer
      final ClusterNodeAddress transferToAddress = transferTo.getAddress();
      final ClusterNodeAddress transferFromAddress = transferFrom.getAddress();

      if (sourceStorageNumber == destinationStorageNumber) {

         transferFrom.getOutboundBuckets().put(bucketNumber, new BucketTransfer(sourceStorageNumber, transferToAddress));
         transferTo.registerInboundTransfer(bucketNumber, new BucketTransfer(sourceStorageNumber, transferFromAddress));
      } else if (sourceStorageNumber == 0 && destinationStorageNumber > 0) {

         transferFrom.registerOutboundReplicaRestore(destinationStorageNumber, bucketNumber, new BucketTransfer(destinationStorageNumber, transferToAddress));
         transferTo.getInboundReplicas().put(bucketNumber, new BucketTransfer((byte) 0, transferFromAddress));
      }

      // Request bucket owner to begin transfer
      if (command == null) {

         command = new BeginBucketTransferCommand(cacheName, sourceStorageNumber, destinationStorageNumber, transferFromAddress, transferToAddress);
      } else {

         if (!command.getCurrentOwner().equals(transferFromAddress) || !command.getNewOwner().equals(transferToAddress)
                 || command.getSourceStorageNumber() != sourceStorageNumber || command.getDestinationStorageNumber() != destinationStorageNumber) {

            listeners.execute(command);
            command = new BeginBucketTransferCommand(cacheName, sourceStorageNumber, destinationStorageNumber, transferFromAddress, transferToAddress);
         }
      }
      command.addBucketNumber(IntegerUtils.valueOf(bucketNumber));
      return command;
   }


   /**
    * Finds a safe owner for a bucket among prospective owners. A safe owner is an owner that is not already in one of
    * the storages, primary or replica.
    *
    * @param bucketNumber      bucket number
    * @param prospectiveOwners prospective owners
    * @return a safe owner for a bucket or null if there is no a safe owner
    */
   private BucketOwner findSafeOwner(final int bucketNumber,
                                     final Map<ClusterNodeAddress, BucketOwner> prospectiveOwners) {

      BucketOwner result = null;

      final byte replicaCount = getReplicaCount();
      for (final Entry<ClusterNodeAddress, BucketOwner> clusterNodeAddressBucketOwnerEntry : prospectiveOwners.entrySet()) {

         // The idea is to try to find if the prospective owner is registered as current or future owner of
         // the bucket.

         final BucketOwner prospectiveOwner = clusterNodeAddressBucketOwnerEntry.getValue();
         if (prospectiveOwner.isLeaving()) {

            continue;
         }

         boolean safe = true;
         for (byte storageNumber = 0; storageNumber <= replicaCount; storageNumber++) {

            // Get address of the owner of this bucket in the storage number being analyzed
            final ClusterNodeAddress address = bucketAssignments[storageNumber].get(bucketNumber);
            if (address == null) {

               // In this storage bucket is orphaned
               if (storageNumber > 0) {

                  // Check if this replica is being restored on the prospective address
                  final BucketOwner primaryOwner = getPrimaryOwner(bucketNumber);
                  if (primaryOwner != null) {

                     // NOPMD
                     final BucketTransfer outboundReplicaTransfer = primaryOwner.getOrCreateOutboundReplicas(storageNumber).get(bucketNumber);
                     if (outboundReplicaTransfer != null) {

                        if (outboundReplicaTransfer.getOwner().equals(prospectiveOwner.getAddress())) {

                           safe = false;
                           break;
                        }
                     }
                  }
               }
            }

            // Didn't find inbound replica that restores a bucket in the analyzed storage
            if (address == null) {
               continue;
            }

            // Even though the bucket might be being transferred to another owner, it is still at the current
            // location
            if (address.equals(prospectiveOwner.getAddress())) {

               safe = false;
               break;
            }

            // Get target address

            // Check if prospective owner's address is a receiver of the bucket transfer in progress
            final BucketOwner currentOwner = bucketOwners[storageNumber].get(address);
            Assert.assertNotNull(currentOwner, "Owner of bucket number {0} with address {1} in storage number {2} should not be null", bucketNumber, address, storageNumber);
            final BucketTransfer outboundTransfer = currentOwner.getOutboundTransfer(bucketNumber);
            if (outboundTransfer != null) {

               if (outboundTransfer.getOwner().equals(prospectiveOwner.getAddress())) {

                  safe = false;
                  break;
               }
            }
         }

         if (safe) {

            // Remember safe owner with lesser load

            if (result == null) {

               result = prospectiveOwner;
            } else {

               // Compare load
               if (result.load() > prospectiveOwner.load()) {

                  result = prospectiveOwner;
               }
            }
         }
      }

      return result;
   }


   /**
    * Returns primary bucket owner for the given bucket number.
    *
    * @param bucketNumber bucket number for that to return the primary bucket owner
    * @return the primary bucket owner for the given bucket number or null if the owner has not been assigned yet.
    */
   private BucketOwner getPrimaryOwner(final int bucketNumber) {

      return getBucketOwner(0, bucketNumber);
   }


   public BucketOwner getBucketOwner(final int storage, final int bucketNumber) {

      final ClusterNodeAddress ownerAddress = bucketAssignments[storage].get(bucketNumber);
      if (ownerAddress == null) {

         return null;
      }

      return bucketOwners[storage].get(ownerAddress);
   }


   /**
    * Requests bucket transfer for buckets belonging to the leaving owners ignoring bucket load of new owners. It is
    * done before main rebalancing because the leaving nodes are in danger of hard shutdown and consequent loss of
    * data.
    * <p/>
    * This method does not touch other bucket obligations such as inbound buckets and replicas and outbound replicas.
    *
    * @param storageNumber a storage number.
    * @param bucketOwner   the bucket owner that is leaving.
    */
   private void transferLeaving(final byte storageNumber, final BucketOwner bucketOwner) {

      if (!bucketOwner.isLeaving() || bucketOwner.getOwnedBuckets().isEmpty()) {

         return;
      }

      final Map<ClusterNodeAddress, BucketOwner> storageOwnerMap = bucketOwners[storageNumber];

      BeginBucketTransferCommand command = null;

      // Try to find a safe owner for leaving owner's buckets
      for (final Iterator<Integer> bucketIter = bucketOwner.getOwnedBuckets().iterator(); bucketIter.hasNext(); ) {

         // Get bucket number
         final Integer bucketNumber = bucketIter.next();


         // Check if this bucket movable or orphanable
         if (storageNumber == 0 && bucketOwner.isRestoringReplicas(bucketNumber)) {

            // Cannot move or orphan
            continue;
         }

         // Try to find a safe owner
         final BucketOwner safeOwner = findSafeOwner(bucketNumber, storageOwnerMap);
         if (safeOwner == null) {

            // Orphan the bucket that cannot be transferred safely
            bucketAssignments[storageNumber].set(bucketNumber, null);

            // Command owner to drop the bucket.
            listeners.execute(new OrphanBucketCommand(cacheName, storageNumber, bucketNumber, bucketOwner.getAddress()));
         } else {

            // Safe owner found,  begin transfer
            command = trackOrBeginTransfer(command, storageNumber, storageNumber, bucketNumber, bucketOwner, safeOwner);
         }

         bucketIter.remove();
      }

      // Finish incomplete command
      if (command != null) {

         listeners.execute(command);
      }
   }


   /**
    * Rebalances bucket ownership assignment in the configuration with no replicas.
    *
    * @param storageNumber      storage number.
    * @param fairBucketsPerNode fair buckets per node
    * @param underloadedMap     underloaded map
    * @param overloadedMap      overloaded map
    */
   private void rebalanceStorageSimple(final byte storageNumber, final int fairBucketsPerNode,
                                       final HashMap<ClusterNodeAddress, BucketOwner> underloadedMap, // NOPMD
                                       final HashMap<ClusterNodeAddress, BucketOwner> overloadedMap) { // NOPMD


      if (underloadedMap.isEmpty() || overloadedMap.isEmpty()) {

         return;
      }

      BeginBucketTransferCommand command = null;
      final Iterator<BucketOwner> overloadedIter = overloadedMap.values().iterator();
      final Iterator<BucketOwner> underloadedIterator = underloadedMap.values().iterator();
      BucketOwner underloadedOwner = underloadedIterator.next();

      while (underloadedOwner != null && overloadedIter.hasNext()) {

         // Pick up overloadedMap node
         final BucketOwner overloadedOwner = overloadedIter.next();

         // While number of buckets to move is greater then zero *and* there are
         // buckets in the underloadedMap node, move bucket to an underloadedMap node.
         while (overloadedOwner.overload(fairBucketsPerNode) > 0) {

            // Transfer
            final Integer bucketNumber = overloadedOwner.getOwnedBuckets().removeFirst();

            // Track or begin bucket transfer
            command = trackOrBeginTransfer(command, storageNumber, storageNumber, bucketNumber, overloadedOwner, underloadedOwner);

            if (overloadedOwner.overload(fairBucketsPerNode) > 0 && underloadedOwner.underload(fairBucketsPerNode) <= 0) {
               if (underloadedIterator.hasNext()) {

                  underloadedOwner = underloadedIterator.next();
               } else {

                  underloadedOwner = null;
                  break;
               }
            }
         }
      }

      if (command != null) {
         listeners.execute(command);
      }
   }


   /**
    * Adopts an orphan.
    *
    * @param storageNumber a storage number.
    * @param bucketNumber  a bucket number.
    * @param owner         owner who adopts an orphan.
    */
   private void assignOrphanToOwner(final byte storageNumber, final Integer bucketNumber, final BucketOwner owner) {

      // Modify assignment
      bucketAssignments[storageNumber].set(bucketNumber, owner.getAddress());
      owner.addOwnedBucketNumber(bucketNumber);

      // Request new owner to create a bucket
      listeners.execute(new AssignBucketCommand(cacheName, storageNumber, bucketNumber, owner.getAddress()));
   }


   /**
    * Calculates a fair number of buckets per node.
    *
    * @return a fair number of buckets per node.
    */
   private int calculateFairBucketsPerNode() {

      // Calculate non-leaving bucket count
      int nonLeavingBucketOwnerCount = 0;
      for (final Entry<ClusterNodeAddress, BucketOwner> entry : bucketOwners[0].entrySet()) {
         nonLeavingBucketOwnerCount += entry.getValue().isLeaving() ? 0 : 1;
      }


      // Calculate fair number
      final int bucketCount = getBucketCount();
      if (nonLeavingBucketOwnerCount == 0) {

         return bucketCount;
      } else {

         return bucketCount / nonLeavingBucketOwnerCount + bucketCount % nonLeavingBucketOwnerCount;
      }
   }


   private void orphanOwnedBuckets(final byte storageNumber, final BucketOwner owner) {

      final AtomicReferenceArray<ClusterNodeAddress> bucketAssignment = bucketAssignments[storageNumber];
      final List<Integer> ownedBuckets = owner.getOwnedBuckets();
      for (final Integer ownedBucket : ownedBuckets) {
         bucketAssignment.set(ownedBucket, null);
      }
   }


   private void cancelOutboundReplicas(final ClusterNodeAddress removedAddr, final BucketOwner removedOwner) {

      final CancelBucketTransferCommand[] command = new CancelBucketTransferCommand[1];
      for (byte replicaStorageNumber = 1; replicaStorageNumber <= getReplicaCount(); replicaStorageNumber++) {

         // Assign to local so that we can use it in the inner forEach class below
         final byte localReplicaStorageNumber = replicaStorageNumber;

         // Check if there is work to do
         final IntObjectHashMap<BucketTransfer> outboundReplicaMap = removedOwner.getOrCreateOutboundReplicas(replicaStorageNumber);
         if (outboundReplicaMap.isEmpty()) {
            continue;
         }

         // Iterate
         outboundReplicaMap.forEachEntry(new IntObjectProcedure<BucketTransfer>() {

            public boolean execute(final int bucketNumber, final BucketTransfer transfer) {

               Assert.assertTrue(transfer.getStorageNumber() != 0, "Storage number for replica should not be 0", transfer.getStorageNumber());
               final ClusterNodeAddress replicaOwnerAddress = transfer.getOwner();
               final BucketOwner replicaOwner = bucketOwners[transfer.getStorageNumber()].get(replicaOwnerAddress);
               replicaOwner.cancelInboundReplica(bucketNumber);
               if (command[0] == null) {

                  // First command
                  command[0] = new CancelBucketTransferCommand(cacheName, (byte) 0, localReplicaStorageNumber, removedAddr, replicaOwnerAddress);
               } else {

                  if (command[0].getDestinationStorageNumber() != localReplicaStorageNumber
                          || !command[0].getPreviousOwner().equals(removedAddr)
                          || !command[0].getNewOwner().equals(replicaOwnerAddress)) {

                     // Post previous commands and change to a new one because one of the parameters has changed
                     listeners.execute(command[0]);
                     command[0] = new CancelBucketTransferCommand(cacheName, (byte) 0, localReplicaStorageNumber, removedAddr, replicaOwnerAddress);
                  }
               }
               command[0].addBucketNumber(bucketNumber);
               return true;
            }
         });
      }

      if (command[0] != null) {
         listeners.execute(command[0]);
      }
   }


   /**
    * Cancels all outbound transfers associated with the given bucket owner.
    * <p/>
    * This method is used by {@link #removeBucketOwners(Collection)} to return buckets to their owners when a bucket
    * owner that transfers buckets out leaves the group.
    *
    * @param storageNumber storage number
    * @param ownerAddress  bucket owner address
    * @param bucketOwner   bucket owner
    */
   private void cancelOutboundTransfers(final byte storageNumber,
                                        final ClusterNodeAddress ownerAddress,
                                        final BucketOwner bucketOwner) {

      final CancelBucketTransferCommand[] command = new CancelBucketTransferCommand[1];
      final Map<ClusterNodeAddress, BucketOwner> bucketOwnership = bucketOwners[storageNumber];
      final IntObjectHashMap<BucketTransfer> outboundBuckets = bucketOwner.getOutboundBuckets();
      outboundBuckets.forEachEntry(new IntObjectProcedure<BucketTransfer>() {

         public boolean execute(final int bucketNumber, final BucketTransfer transfer) {

            // Cancel receiver's inbound bucket
            final ClusterNodeAddress receiverAddress = transfer.getOwner();
            final BucketOwner receiver = bucketOwnership.get(receiverAddress);

            //noinspection ControlFlowStatementWithoutBraces
//            if (LOG.isDebugEnabled()) {
//               LOG.debug("Canceling inbound transfer, owner: '" + bucketOwner.getAddress() + "' storageNumber: " + storageNumber + ", bucketNumber: " + bucketNumber + ", receiver: " + receiver); // NOPMD
//            }
            receiver.cancelInboundTransfer(bucketNumber);

            // Put back to owned buckets
            bucketOwner.addOwnedBucketNumber(IntegerUtils.valueOf(bucketNumber));

            // Notify
            if (command[0] == null) {

               command[0] = new CancelBucketTransferCommand(cacheName, storageNumber, storageNumber, ownerAddress, receiverAddress);
            } else {

               if (!command[0].getNewOwner().equals(receiverAddress)) {

                  listeners.execute(command[0]);
                  command[0] = new CancelBucketTransferCommand(cacheName, storageNumber, storageNumber, ownerAddress, receiverAddress);
               }
            }
            command[0].addBucketNumber(bucketNumber);

            // Continue
            return true;
         }
      });

      outboundBuckets.clear();

      if (command[0] != null) {

         listeners.execute(command[0]);
      }
   }


   private void cancelInboundReplicas(final byte storageNumber, final ClusterNodeAddress receiverAddr,
                                      final BucketOwner receiver) {

      Assert.assertTrue(storageNumber != 0, "Inbound replicas make sense only for replica owner: {0}", storageNumber);

      // NOTE: simeshev@cacheonix.org - 2010-02-26 - declared it
      final CancelBucketTransferCommand[] command = new CancelBucketTransferCommand[1];
      final IntObjectHashMap<BucketTransfer> inboundReplicas = receiver.getInboundReplicas();
      inboundReplicas.forEachEntry(new IntObjectProcedure<BucketTransfer>() {

         public boolean execute(final int bucketNumber, final BucketTransfer transfer) {

            Assert.assertTrue(transfer.getStorageNumber() == 0, "Primary owner storage number should be 0");
            final ClusterNodeAddress primaryOwnerAddress = transfer.getOwner();

            // Note the that we get a primary owner from the first
            final BucketOwner primaryOwner = bucketOwners[0].get(primaryOwnerAddress);
            primaryOwner.cancelOutboundReplica(storageNumber, bucketNumber);
            if (command[0] == null) {

               // First command
               command[0] = new CancelBucketTransferCommand(cacheName, (byte) 0, storageNumber, primaryOwnerAddress, receiverAddr);
            } else {

               if (command[0].getDestinationStorageNumber() != storageNumber
                       || !command[0].getPreviousOwner().equals(primaryOwnerAddress)
                       || !command[0].getNewOwner().equals(receiverAddr)) {

                  // Post previous and change current command because one of the parameters has changed
                  listeners.execute(command[0]);
                  command[0] = new CancelBucketTransferCommand(cacheName, (byte) 0, storageNumber, primaryOwnerAddress, receiverAddr);
               }
            }

            command[0].addBucketNumber(bucketNumber);
            return true;
         }
      });

      inboundReplicas.clear();
      if (command[0] != null) {

         listeners.execute(command[0]);
      }
   }


   /**
    * Cancels all inbound transfers associated with the given bucket owner.
    * <p/>
    * This method is used by {@link #removeBucketOwners(Collection)} to return buckets to their owners when a bucket
    * owner that receives buckets leaves the group.
    *
    * @param storageNumber      storage number
    * @param removedAddress     bucket owner address
    * @param removedBucketOwner bucket owner
    */
   private void cancelInboundTransfers(final byte storageNumber,
                                       final ClusterNodeAddress removedAddress,
                                       final BucketOwner removedBucketOwner) {

      final Map<ClusterNodeAddress, BucketOwner> bucketOwnership = bucketOwners[storageNumber];
      final CancelBucketTransferCommand[] command = new CancelBucketTransferCommand[1];
      final IntObjectHashMap<BucketTransfer> inboundBuckets = removedBucketOwner.getInboundBuckets();
      inboundBuckets.forEachEntry(new IntObjectProcedure<BucketTransfer>() {

         public boolean execute(final int bucketNumber, final BucketTransfer inboundTransfer) {

            final ClusterNodeAddress previousOwnerAddress = inboundTransfer.getOwner();
            final BucketOwner previousOwner = bucketOwnership.get(previousOwnerAddress);
            previousOwner.cancelOutboundTransfer(bucketNumber);
            if (command[0] == null) {

               // First command
               command[0] = new CancelBucketTransferCommand(cacheName, storageNumber, storageNumber, previousOwnerAddress, removedAddress);
            } else {

               if (!command[0].getNewOwner().equals(removedAddress)) {

                  // Post previous and change current command because one of the parameters has changed
                  listeners.execute(command[0]);
                  command[0] = new CancelBucketTransferCommand(cacheName, storageNumber, storageNumber, previousOwnerAddress, removedAddress);
               }
            }

            command[0].addBucketNumber(bucketNumber);
            return true;
         }
      });

      inboundBuckets.clear();

      if (command[0] != null) {

         listeners.execute(command[0]);
      }
   }


   /**
    * Creates bucket assignment.
    *
    * @param replicaCount number of replicas.
    * @param bucketCount  bucket count.
    * @return new bucket assignment.
    */
   private static AtomicReferenceArray<ClusterNodeAddress>[] createBucketAssignments(final int replicaCount,
                                                                                     final int bucketCount) {

      final AtomicReferenceArray[] atomicReferenceArrays = new AtomicReferenceArray[replicaCount + 1];
      for (int i = 0; i < atomicReferenceArrays.length; i++) {

         atomicReferenceArrays[i] = new AtomicReferenceArray<ClusterNodeAddress>(bucketCount);

      }
      //noinspection unchecked
      return atomicReferenceArrays;
   }


   /**
    * Creates bucket ownership.
    *
    * @param replicaCount number of replicas.
    * @return new bucket ownership
    */
   private static TreeMap<ClusterNodeAddress, BucketOwner>[] createBucketOwners(final int replicaCount) { // NOPMD

      @SuppressWarnings("unchecked")
      final TreeMap<ClusterNodeAddress, BucketOwner>[] maps = new TreeMap[replicaCount + 1];
      for (byte i = 0; i <= replicaCount; i++) {

         // NOTE: simeshev@cacheonix.org - 2010-02-10 - It it *critical* for consistent bucket ownership
         // assignment that the TreeMap (using natural sort order of keys) is used. HashMap does not work
         // order of traversal depends on order of insertion, and it is not always guaranteed that the
         // nodes are inserted in the same order. See bug CACHEONIX-168 for more information.
         maps[i] = new TreeMap<ClusterNodeAddress, BucketOwner>();
      }
      return maps;
   }


   public void readWire(final DataInputStream in) throws IOException {

      cacheName = SerializerUtils.readString(in);

      final byte replicaCount = in.readByte();
      final int bucketCount = in.readInt();

      bucketAssignments = createBucketAssignments(replicaCount, bucketCount);
      bucketOwners = createBucketOwners(replicaCount);

      for (byte i = 0; i <= replicaCount; i++) {
         for (int j = 0; j < bucketCount; j++) {
            bucketAssignments[i].set(j, SerializerUtils.readAddress(in));
         }
         final int size = in.readInt();
         final TreeMap<ClusterNodeAddress, BucketOwner> result = new TreeMap<ClusterNodeAddress, BucketOwner>();
         for (int i1 = 0; i1 < size; i1++) {
            final ClusterNodeAddress ownerAddress = SerializerUtils.readAddress(in);
            final BucketOwner bucketOwner = new BucketOwner();
            bucketOwner.readWire(in);
            result.put(ownerAddress, bucketOwner);
         }
         bucketOwners[i] = result;
      }
   }


   public int getWireableType() {

      return TYPE_BUCKET_OWNERSHIP_ASSIGNMENT;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      SerializerUtils.writeString(cacheName, out);

      final byte replicaCount = getReplicaCount();
      out.writeByte(replicaCount);

      final int bucketCount = getBucketCount();
      out.writeInt(bucketCount);

      for (byte i = 0; i <= replicaCount; i++) {
         for (int j = 0; j < bucketCount; j++) {
            SerializerUtils.writeAddress(bucketAssignments[i].get(j), out);
         }
         out.writeInt(bucketOwners[i].size());
         for (final Entry<ClusterNodeAddress, BucketOwner> entry : bucketOwners[i].entrySet()) {
            SerializerUtils.writeAddress(entry.getKey(), out);
            final BucketOwner bucketOwner = entry.getValue();
            bucketOwner.writeWire(out);
         }
      }
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final BucketOwnershipAssignment that = (BucketOwnershipAssignment) obj;

      if (!Arrays.equals(bucketOwners, that.bucketOwners)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return bucketOwners != null ? Arrays.hashCode(bucketOwners) : 0;
   }


   public String toString() {

      return "BucketOwnershipAssignment{" +
              "bucketAssignments=" + (bucketAssignments == null ? "null" : Integer.toString(bucketAssignments.length)) +
              ", bucketOwners=" + (bucketOwners == null ? "null" : Integer.toString(bucketOwners.length)) +
              ", listeners=" + listeners.size() +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new BucketOwnershipAssignment();
      }
   }
}
