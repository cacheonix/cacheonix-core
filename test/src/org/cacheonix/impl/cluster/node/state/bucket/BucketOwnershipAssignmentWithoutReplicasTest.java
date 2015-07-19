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
package org.cacheonix.impl.cluster.node.state.bucket;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * <pre>
 * Input:
 *
 * Actual ownership:
 *
 *       1. Partition owners - a set of addresses representing partitions.
 *       2. Bucket ownership assignment - tells which bucket is owned by a partition owner
 *       3. Replica ownership assignment - tells which replica is owned by a partition owner
 *
 * Ownership in progress:
 *
 *       4. Buckets being transferred from a node to another node
 * </pre>
 *
 * @noinspection UnnecessaryLocalVariable, UnsecureRandomNumberGeneration, OverlyComplexAnonymousInnerClass
 */
public final class BucketOwnershipAssignmentWithoutReplicasTest extends TestCase {

   /**
    * @noinspection UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketOwnershipAssignmentWithoutReplicasTest.class); // NOPMD

   private static final String CACHE_NAME = "test.cache.name";

   private static final int BUCKET_COUNT = 2051;

   private static final byte REPLICA_COUNT = 0;


   private final BucketOwnershipAssignment boat = new BucketOwnershipAssignment(CACHE_NAME, BUCKET_COUNT, REPLICA_COUNT);

   private final TestBucketEventListener eventListener = new TestBucketEventListener();


   public void testSetUp() {

      assertEquals(0, eventListener.getBeginTransferCommands().size());
      final int maxOwned = boat.calculateMaxOwnedBucketCount(0);
      assertEquals("Bucket ownership should be split at least in half: " + maxOwned, 0, maxOwned);
   }


   public void testAddBucketOwner() {

      //
      // Add first bucket owner
      //

      final ClusterNodeAddress address0 = TestUtils.createTestAddress(0);
      boat.addBucketOwner(address0);
      boat.repartition();

      assertNoPendingCommands();

      for (int i = 0; i < BUCKET_COUNT; i++) {
         assertEquals(address0, boat.getBucketOwnerAddress(0, i));
      }


      //
      // Add second bucket owner. This should split the RBOAT exactly in half
      //
      final ClusterNodeAddress address1 = TestUtils.createTestAddress(1);
      boat.addBucketOwner(address1);
      boat.repartition();

      // Assert - the first half should begin to move
      assertEquals(1, eventListener.getBeginTransferCommands().size());
      assertEquals((BUCKET_COUNT / 2), eventListener.getBeginTransferCommands().get(0).getBucketNumbers().size());

      // Assert there were not other commands issued.
      assertEquals(0, eventListener.getCancelTransferCommands().size());
      assertEquals(0, eventListener.getFinishReplicaRestoreCommands().size());
      assertEquals(0, eventListener.getFinishTransferCommands().size());
      assertEquals(0, eventListener.getRestoreBucketCommands().size());
      assertEquals(0, eventListener.getBeginRestoreReplicaCommands().size());

      // Now complete transfers
      completeTransfers(eventListener);

      // Assert transfer complete
      for (int i = 0; i < BUCKET_COUNT / 2; i++) {

         assertEquals(address1, boat.getBucketOwnerAddress(0, i));
      }

      for (int i = BUCKET_COUNT / 2; i < BUCKET_COUNT; i++) {

         assertEquals(address0, boat.getBucketOwnerAddress(0, i));
      }

      assertEquals(2, boat.getBucketOwnerCount());
      assertNoPendingCommands();

      //
      // Add third bucket owner. This should split the RBOAT exactly in third
      //
      final ClusterNodeAddress address2 = TestUtils.createTestAddress(2);
      boat.addBucketOwner(address2);
      boat.repartition();

      // Assert - both existing owners must begin to move
      assertEquals(2, eventListener.getBeginTransferCommands().size());
      assertEquals((BUCKET_COUNT / 2) - (BUCKET_COUNT / 3) - 1, eventListener.getBeginTransferCommands().get(0).getBucketNumbers().size());
      assertEquals((BUCKET_COUNT / 2) - (BUCKET_COUNT / 3) - 2, eventListener.getBeginTransferCommands().get(1).getBucketNumbers().size());

      // Assert there were not other commands issued
      assertEquals(0, eventListener.getCancelTransferCommands().size());
      assertEquals(0, eventListener.getFinishReplicaRestoreCommands().size());
      assertEquals(0, eventListener.getFinishTransferCommands().size());
      assertEquals(0, eventListener.getRestoreBucketCommands().size());
      assertEquals(0, eventListener.getBeginRestoreReplicaCommands().size());

      // Now complete transfers
      completeTransfers(eventListener);

      // Assert transfer complete
      assertEquals(BUCKET_COUNT / 3 + 2, boat.getOwnedBuckets(0, address0).size());
      assertEquals(BUCKET_COUNT / 3 + 2, boat.getOwnedBuckets(0, address1).size());
      assertEquals(BUCKET_COUNT / 3 - 2, boat.getOwnedBuckets(0, address2).size());
      assertEquals(BUCKET_COUNT, boat.getOwnedBuckets(0, address0).size() + boat.getOwnedBuckets(0, address1).size() + boat.getOwnedBuckets(0, address2).size());

      // Assert there were not other commands issued
      assertEquals(3, boat.getBucketOwnerCount());
      assertNoPendingCommands();
   }


   public void testAddBucketOwnerStabilizes() {

      //
      // Add first bucket owner
      //
      final ClusterNodeAddress address0 = TestUtils.createTestAddress(0);
      boat.addBucketOwner(address0);
      boat.repartition();

      //
      // Add second bucket owner but don't complete transfers
      //
      final ClusterNodeAddress address1 = TestUtils.createTestAddress(1);
      boat.addBucketOwner(address1);
      boat.repartition();

      //
      // Add third bucket owner. This should split the RBOAT exactly in third
      //
      final ClusterNodeAddress address2 = TestUtils.createTestAddress(2);
      boat.addBucketOwner(address2);
      boat.repartition();

      // Assert - both existing owners must begin to move
      assertEquals(2, eventListener.getBeginTransferCommands().size());
      assertEquals("Transfer to the second node", (BUCKET_COUNT / 2), eventListener.getBeginTransferCommands().get(0).getBucketNumbers().size());
      assertEquals("Transfer to the third node", (BUCKET_COUNT / 2) - (BUCKET_COUNT / 3) - 1, eventListener.getBeginTransferCommands().get(1).getBucketNumbers().size());

      assertEquals(0, eventListener.getBeginRestoreReplicaCommands().size());
      assertEquals(0, eventListener.getCancelTransferCommands().size());
      assertEquals(0, eventListener.getFinishReplicaRestoreCommands().size());
      assertEquals(0, eventListener.getFinishTransferCommands().size());
      assertEquals(0, eventListener.getRestoreBucketCommands().size());

      // This complete transfer will [naturally create an unbalanced ownership 
      completeTransfers(eventListener);
      assertEquals("First bucket owner has a third because it was a source all the time", BUCKET_COUNT / 3 + 2, boat.getOwnedBuckets(0, address0).size());
      assertEquals("Second bucket owner has a half because it was second", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address1).size());
      assertEquals("Third has gotten what ever left within fair distribution", (BUCKET_COUNT / 3) - 2, boat.getOwnedBuckets(0, address2).size());
      assertEquals(BUCKET_COUNT, boat.getOwnedBuckets(0, address0).size() + boat.getOwnedBuckets(0, address1).size() + boat.getOwnedBuckets(0, address2).size());
      assertEquals(3, boat.getBucketOwnerCount());
      assertNoPendingCommands();

      // Now run one more repartition to balance distribution
      boat.repartition();
      completeTransfers(eventListener);
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address0).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address1).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) - 2, boat.getOwnedBuckets(0, address2).size());
      assertEquals(BUCKET_COUNT, boat.getOwnedBuckets(0, address0).size() + boat.getOwnedBuckets(0, address1).size() + boat.getOwnedBuckets(0, address2).size());

      assertEquals(3, boat.getBucketOwnerCount());
      assertNoPendingCommands();
   }


   /**
    * Tests adding a few owners and repartitioning after that.
    */
   public void testAddBucketOwnersAsBulk() {

      //
      // Add first bucket owner
      //
      final ClusterNodeAddress address0 = TestUtils.createTestAddress(0);
      boat.addBucketOwner(address0);

      //
      // Add second bucket owner but don't complete transfers
      //
      final ClusterNodeAddress address1 = TestUtils.createTestAddress(1);
      boat.addBucketOwner(address1);

      //
      // Add third bucket owner but don't complete transfers
      //
      final ClusterNodeAddress address2 = TestUtils.createTestAddress(2);
      boat.addBucketOwner(address2);

      //
      // Repartition
      //
      boat.repartition();

      // Assert - all joined at once when there wasn't any owners
      assertNoPendingCommands();
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address0).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) - 2, boat.getOwnedBuckets(0, address1).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address2).size());
      assertEquals(BUCKET_COUNT, boat.getOwnedBuckets(0, address0).size() + boat.getOwnedBuckets(0, address1).size() + boat.getOwnedBuckets(0, address2).size());
      assertEquals(3, boat.getBucketOwnerCount());
   }


   /**
    * Tests adding one owner and a few owners and repartitioning after that.
    */
   public void testAddBucketOwnerAndThenOwnersAsBulk() {

      //
      // Add first bucket owner
      //
      final ClusterNodeAddress address0 = TestUtils.createTestAddress(0);
      boat.addBucketOwner(address0);
      boat.repartition();

      //
      // Add second bucket owner but don't complete transfers
      //
      final ClusterNodeAddress address1 = TestUtils.createTestAddress(1);
      boat.addBucketOwner(address1);

      //
      // Add third bucket owner but don't complete transfers
      //
      final ClusterNodeAddress address2 = TestUtils.createTestAddress(2);
      boat.addBucketOwner(address2);

      //
      // Repartition
      //
      boat.repartition();

      // Assert - both existing owners must begin to move
      assertEquals(2, eventListener.getBeginTransferCommands().size());
      assertEquals("Transfer to the second node", (BUCKET_COUNT / 3) + 2, eventListener.getBeginTransferCommands().get(0).getBucketNumbers().size());
      assertEquals("Transfer to the third node", (BUCKET_COUNT / 3) - 2, eventListener.getBeginTransferCommands().get(1).getBucketNumbers().size());

      assertEquals(0, eventListener.getBeginRestoreReplicaCommands().size());
      assertEquals(0, eventListener.getCancelTransferCommands().size());
      assertEquals(0, eventListener.getFinishReplicaRestoreCommands().size());
      assertEquals(0, eventListener.getFinishTransferCommands().size());
      assertEquals(0, eventListener.getRestoreBucketCommands().size());

      // This complete transfer will [naturally create an unbalanced ownership
      completeTransfers(eventListener);
      assertNoPendingCommands();

      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address0).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) - 2, boat.getOwnedBuckets(0, address1).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address2).size());
      assertEquals(BUCKET_COUNT, boat.getOwnedBuckets(0, address0).size() + boat.getOwnedBuckets(0, address1).size() + boat.getOwnedBuckets(0, address2).size());
      assertEquals(3, boat.getBucketOwnerCount());
   }


   public void testRemoveBucketOwner() {

      // Add first bucket owner
      final ClusterNodeAddress address0 = TestUtils.createTestAddress(0);
      boat.addBucketOwner(address0);
      boat.repartition();

      // Add second bucket owner but don't complete transfers
      final ClusterNodeAddress address1 = TestUtils.createTestAddress(1);
      boat.addBucketOwner(address1);

      // Add third bucket owner but don't complete transfers
      final ClusterNodeAddress address2 = TestUtils.createTestAddress(2);
      boat.addBucketOwner(address2);

      // Repartition
      boat.repartition();
      completeTransfers(eventListener);

      // Just in case, assure proper distribution
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address0).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) - 2, boat.getOwnedBuckets(0, address1).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 3) + 2, boat.getOwnedBuckets(0, address2).size());
      assertEquals(BUCKET_COUNT, boat.getOwnedBuckets(0, address0).size() + boat.getOwnedBuckets(0, address1).size() + boat.getOwnedBuckets(0, address2).size());
      assertEquals(3, boat.getBucketOwnerCount());

      // Remove
      boat.removeBucketOwners(Collections.singleton(address1));

      // Assert
      assertNoPendingCommands(); // Because there is no replica
      assertEquals(2, boat.getBucketOwnerCount());
      assertEquals(BUCKET_COUNT, boat.getOwnedBuckets(0, address0).size() + boat.getOwnedBuckets(0, address2).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 2), boat.getOwnedBuckets(0, address0).size());
      assertEquals("All should receive roughly the same number of buckets", (BUCKET_COUNT / 2) + 1, boat.getOwnedBuckets(0, address2).size());
   }


   public void testRemoveProducesCancelTransferCommands() {

      // Add first bucket owner
      final ClusterNodeAddress address0 = TestUtils.createTestAddress(0);
      boat.addBucketOwner(address0);

      // Add second bucket owner
      final ClusterNodeAddress address1 = TestUtils.createTestAddress(1);
      boat.addBucketOwner(address1);

      // Repartition
      boat.repartition();
      completeTransfers(eventListener);

      // Add third bucket owner but don't complete transfers
      final ClusterNodeAddress address2 = TestUtils.createTestAddress(2);
      boat.addBucketOwner(address2);
      boat.repartition();

      // Remove
      boat.removeBucketOwners(CollectionUtils.createList(address2));

      // Assert that the buckets that got canceled are those that got moved
      assertEquals(2, eventListener.getBeginTransferCommands().size());
      assertEquals(1, eventListener.getCancelTransferCommands().size());

      final Set<Integer> transfers = new HashSet<Integer>();
      for (final BeginBucketTransferCommand transferCommand : eventListener.getBeginTransferCommands()) {
         transfers.addAll(transferCommand.getBucketNumbers());
      }

      final Set<Integer> cancels = new HashSet<Integer>();
      for (final CancelBucketTransferCommand cancelCommand : eventListener.getCancelTransferCommands()) {
         cancels.addAll(cancelCommand.getBucketNumbers());
      }

      assertEquals(transfers, cancels);
   }


   public void testProcessRejectAnnouncement() {

      // Add first bucket owner
      final ClusterNodeAddress address0 = TestUtils.createTestAddress(0);
      boat.addBucketOwner(address0);

      // Add second bucket owner
      final ClusterNodeAddress address1 = TestUtils.createTestAddress(1);
      boat.addBucketOwner(address1);

      // Repartition
      boat.repartition();
      completeTransfers(eventListener);

      // Add third bucket owner but don't complete transfers
      final ClusterNodeAddress address2 = TestUtils.createTestAddress(2);
      boat.addBucketOwner(address2);
      boat.repartition();

      // Assert that the buckets that got canceled are those that got moved
      assertEquals(2, eventListener.getBeginTransferCommands().size());
      final Set<Integer> transfers = new HashSet<Integer>();
      final LinkedList<BeginBucketTransferCommand> beginTransferCommand = new LinkedList<BeginBucketTransferCommand>(eventListener.getBeginTransferCommands());
      for (final BeginBucketTransferCommand transferCommand : beginTransferCommand) {
         transfers.addAll(transferCommand.getBucketNumbers());
      }


      // Cancel transfers
      for (final BeginBucketTransferCommand transferCommand : beginTransferCommand) {

         final Collection<Integer> bucketNumbers = transferCommand.getBucketNumbers();
         final ClusterNodeAddress currentOwner = transferCommand.getCurrentOwner();
         final ClusterNodeAddress newOwner = transferCommand.getNewOwner();
         final byte sourceStorageNumber = transferCommand.getSourceStorageNumber();
         final byte destinationStorageNumber = transferCommand.getDestinationStorageNumber();
         boat.rejectBucketTransfer(sourceStorageNumber, destinationStorageNumber, currentOwner, newOwner, bucketNumbers);
      }


      // Assert
      assertEquals(transfers.size(), eventListener.getCancelTransferCommandsBucketCount());

      // Assert cancel commands caused by rejects contain the same buckets
      final Set<Integer> cancels = new HashSet<Integer>();
      for (final CancelBucketTransferCommand cancelCommand : eventListener.getCancelTransferCommands()) {
         cancels.addAll(cancelCommand.getBucketNumbers());
      }

      assertEquals(transfers, cancels);
   }


   public void testFinishTransferCommands() {

      // Add first bucket owner
      final ClusterNodeAddress address0 = TestUtils.createTestAddress(0);
      boat.addBucketOwner(address0);
      boat.repartition();

      // Add second bucket owner
      final ClusterNodeAddress address1 = TestUtils.createTestAddress(1);
      boat.addBucketOwner(address1);
      boat.repartition();

      // Complete transfer
      completeTransfer(eventListener.getBeginTransferCommands().get(0));

      // Assert
      assertEquals(1, eventListener.getFinishTransferCommands().size());
      assertEquals(1025, eventListener.getFinishTransferCommands().get(0).getBucketNumbers().size());
   }


   @SuppressWarnings("InstanceMethodNamingConvention")
   public void testBug166AssignmentDoesNotDependInOrderOfAddingOwners() {

      // Create assignment #1
      final BucketOwnershipAssignment assignment1 = new BucketOwnershipAssignment(CACHE_NAME, BUCKET_COUNT, REPLICA_COUNT);
      final BucketEventListenerList eventListenerList1 = new BucketEventListenerList();
      final TestBucketEventListener eventListener1 = new TestBucketEventListener();
      eventListenerList1.add(eventListener1);
      assignment1.attachListeners(eventListenerList1);

      // Init bucket ownership
      final int nodeCount = 50;
      for (int i = 0; i < nodeCount; i++) {
         assignment1.addBucketOwner(TestUtils.createTestAddress(i));
      }
      assignment1.repartition();
      completeTransfers(assignment1, eventListener1);

      // Create assignment #2 - revers order of adding nodes
      final BucketOwnershipAssignment assignment2 = new BucketOwnershipAssignment(CACHE_NAME, BUCKET_COUNT, REPLICA_COUNT);
      final BucketEventListenerList eventListenerList2 = new BucketEventListenerList();
      final TestBucketEventListener eventListener2 = new TestBucketEventListener();
      eventListenerList2.add(eventListener2);
      assignment2.attachListeners(eventListenerList2);

      // Init bucket ownership
      for (int i = nodeCount - 1; i >= 0; i--) {
         assignment2.addBucketOwner(TestUtils.createTestAddress(i));
      }
      assignment2.repartition();
      completeTransfers(assignment2, eventListener2);

      for (int i = 0; i < BUCKET_COUNT; i++) {
         assertEquals(assignment1.getBucketOwnerAddress((byte) 0, i), assignment2.getBucketOwnerAddress((byte) 0, i));
      }
   }


   private void completeTransfers(final TestBucketEventListener listener) {

      final LinkedList<BeginBucketTransferCommand> transferCommands = listener.getBeginTransferCommands();
      while (!transferCommands.isEmpty()) {
         completeTransfer(transferCommands.removeFirst());
      }
      listener.getFinishTransferCommands().clear();
   }


   private static void completeTransfers(final BucketOwnershipAssignment ownershipAssignment,
                                         final TestBucketEventListener listener) {

      final LinkedList<BeginBucketTransferCommand> transferCommands = listener.getBeginTransferCommands();
      while (!transferCommands.isEmpty()) {
         completeTransfer(ownershipAssignment, transferCommands.removeFirst());
      }
      listener.getFinishTransferCommands().clear();
   }


   private void completeTransfer(final BeginBucketTransferCommand transferCommand) {

      completeTransfer(boat, transferCommand);
   }


   private static void completeTransfer(final BucketOwnershipAssignment rboat,
                                        final BeginBucketTransferCommand transferCommand) {

      final List<Integer> bucketNumbers = transferCommand.getBucketNumbers();

      final ClusterNodeAddress currentOwner = transferCommand.getCurrentOwner();
      final ClusterNodeAddress newOwner = transferCommand.getNewOwner();
      final byte sourceStorageNumber = transferCommand.getSourceStorageNumber();
      final byte destinationStorageNumber = transferCommand.getDestinationStorageNumber();
      rboat.finishBucketTransfer(sourceStorageNumber, destinationStorageNumber, currentOwner, newOwner, bucketNumbers);
   }


   private void assertNoPendingCommands() {

      assertEquals(0, eventListener.getBeginRestoreReplicaCommands().size());
      assertEquals(0, eventListener.getBeginTransferCommands().size());
      assertEquals(0, eventListener.getCancelTransferCommands().size());
      assertEquals(0, eventListener.getFinishReplicaRestoreCommands().size());
      assertEquals(0, eventListener.getFinishTransferCommands().size());
      assertEquals(0, eventListener.getRestoreBucketCommands().size());
   }


   /**
    * @noinspection ProhibitedExceptionDeclared
    */
   protected void setUp() throws Exception {

      super.setUp();

      final BucketEventListenerList bucketEventListenerList = new BucketEventListenerList();
      bucketEventListenerList.add(eventListener);
      boat.attachListeners(bucketEventListenerList);
   }


   public String toString() {

      return "BucketOwnershipAssignmentWithoutReplicasTest{" +
              "bucketOwnershipAssignment=" + boat +
              ", eventListener=" + eventListener +
              "} " + super.toString();
   }
}