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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.logging.Logger;

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
 * @noinspection UnnecessaryLocalVariable, UnsecureRandomNumberGeneration, OverlyComplexAnonymousInnerClass,
 * OverlyComplexAnonymousInnerClass, OverlyComplexAnonymousInnerClass, ImplicitNumericConversion
 */
public final class BucketOwnershipAssignmentWithReplicasTest extends TestCase {

   /**
    * @noinspection UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketOwnershipAssignmentWithReplicasTest.class); // NOPMD

   private static final String CACHE_NAME = "test.cache.name";

   private static final int BUCKET_COUNT = 2051;

   private static final byte REPLICA_COUNT = 2;

   private static final int BUCKET_OWNER_COUNT = 3;

   private final BucketOwnershipAssignment bucketOwnershipAssignment = new BucketOwnershipAssignment(CACHE_NAME, BUCKET_COUNT, REPLICA_COUNT);

   private final TestBucketEventListener eventListener = new TestBucketEventListener();


   public void testAddFirstOwners() {

      for (byte ownerIndex = 0; ownerIndex < REPLICA_COUNT; ownerIndex++) {

         addBucketOwnerAndRepartition(TestUtils.createTestAddress(ownerIndex));
         executePendingCompletionCommands();

         for (int bucketNumber = 0; bucketNumber < BUCKET_COUNT; bucketNumber++) {
            assertNotNull(bucketOwnershipAssignment.getBucketOwnerAddress((byte) 0, bucketNumber));
         }
         for (byte storageNumber = (byte) (ownerIndex + 1); storageNumber <= REPLICA_COUNT; storageNumber++) {
            for (int bucketNumber = 0; bucketNumber < BUCKET_COUNT; bucketNumber++) {
               assertNull(bucketOwnershipAssignment.getBucketOwnerAddress(storageNumber, bucketNumber));
            }
         }
      }
   }


   public void testAddDoesNotSetUnreliableReplicas() {

      for (int ownerIndex = 0; ownerIndex < REPLICA_COUNT; ownerIndex++) {
         addBucketOwnerAndRepartition(TestUtils.createTestAddress(ownerIndex));
         executePendingCompletionCommands();
         assertBucketsAreSafe();
      }
   }


   /**
    * Tests that RBOAT does not produce replica restore commands involving duplicate primary owner.
    */
   public void testAddDoesNotGenerateDuplicatePrimaryOwner() {

      // Add 1 primary and two replicas
      for (int storageNumber = 0; storageNumber <= 2; storageNumber++) {
         addBucketOwnerAndRepartition(TestUtils.createTestAddress(storageNumber));
      }

      final Collection<BeginBucketTransferCommand> commands = eventListener.getBeginRestoreReplicaCommands();
      final Map<PrimaryBucketOwner, BeginBucketTransferCommand> alreadyRegisteredPrimaryOwners = new HashMap<PrimaryBucketOwner, BeginBucketTransferCommand>(commands.size());
      for (final BeginBucketTransferCommand command : commands) {
         final ClusterNodeAddress owner = command.getCurrentOwner();
         final Collection<Integer> bucketNumbers = command.getBucketNumbers();
         for (final Integer bucketNumber : bucketNumbers) {
            final PrimaryBucketOwner primaryBucketOwner = new PrimaryBucketOwner(owner, bucketNumber);
            final Object alreadyRegistered = alreadyRegisteredPrimaryOwners.put(primaryBucketOwner, command);
            if (alreadyRegistered != null) {
               fail("Duplicate primary owner for the bucket transfer: " + alreadyRegistered + ", new: " + command);
            }
         }
      }
   }


   public void testGetBucketCount() {

      addBucketOwners(BUCKET_OWNER_COUNT);
      assertEquals(BUCKET_COUNT, bucketOwnershipAssignment.getBucketCount());
   }


   public void testGetReplicaCount() {

      addBucketOwners(BUCKET_OWNER_COUNT);
      assertEquals(REPLICA_COUNT, bucketOwnershipAssignment.getReplicaCount());
   }


   public void testAddSecondBucketOwner() {

      addBucketOwners(1);

      assertEquals(0, eventListener.getBeginTransferCommands().size());
      for (int i = 0; i <= REPLICA_COUNT; i++) {
         final int maxOwned = bucketOwnershipAssignment.calculateMaxOwnedBucketCount(0);
         assertEquals("All buckets should be assigned to one owner: " + maxOwned, BUCKET_COUNT, maxOwned);
      }

      // Prepare
      final int bucketOwnershipSizeBefore = bucketOwnershipAssignment.getBucketOwnerCount();

      // Add one more owner
      addBucketOwnerAndRepartition(TestUtils.createTestAddress(1));

      // Assert events fired
      assertEquals(bucketOwnershipSizeBefore + 1, bucketOwnershipAssignment.getBucketOwnerCount());
      assertEquals(1, eventListener.getBeginTransferCommands().size());
      assertEquals(1025, eventListener.getBeginTransferCommandsBucketCount(0));

      // Assert rebalanced
      executePendingCompletionCommands();
      assertEquals(0, eventListener.getFinishTransferCommands().size());
      assertEquals(0, eventListener.getBeginTransferCommands().size());
      assertEquals(1026, bucketOwnershipAssignment.calculateMaxOwnedBucketCount((byte) 0));
      assertBucketsAreSafe();
   }


   public void testAddBucketOwner() {

      addBucketOwners(BUCKET_OWNER_COUNT);

      assertEquals(0, eventListener.getBeginTransferCommands().size());
      final int maxOwned = bucketOwnershipAssignment.calculateMaxOwnedBucketCount((byte) 0);
      assertTrue("Bucket ownership should be split about equally: " + maxOwned, maxOwned <= BUCKET_COUNT / BUCKET_OWNER_COUNT + 2);

      // Prepare
      final int bucketOwnershipSizeBefore = bucketOwnershipAssignment.getBucketOwnerCount();

      // Add one more owner
      final ClusterNodeAddress newAddress = TestUtils.createTestAddress(BUCKET_OWNER_COUNT);
      addBucketOwnerAndRepartition(newAddress);

      // Assert events fired
      assertEquals(bucketOwnershipSizeBefore + 1, bucketOwnershipAssignment.getBucketOwnerCount());
      assertEquals("Contains both primary and replicas", 9, eventListener.getBeginTransferCommands().size());
      assertEquals(506, eventListener.getBeginTransferCommandsBucketCount(0));
      assertEquals(506, eventListener.getBeginTransferCommandsBucketCount(1));

      // Assert rebalanced
      executePendingCompletionCommands();
      assertEquals(0, eventListener.getFinishTransferCommands().size());
      assertEquals(0, eventListener.getBeginTransferCommands().size());
      assertEquals(515, bucketOwnershipAssignment.calculateMaxOwnedBucketCount((byte) 0));
      assertEquals(506, bucketOwnershipAssignment.getOwnedBuckets(0, newAddress).size());

      assertEquals(506, bucketOwnershipAssignment.getOwnedBuckets(1, newAddress).size());

      assertEquals(349, bucketOwnershipAssignment.getOwnedBuckets(2, newAddress).size());
   }


   public void testRemoveBucketOwner() {

      // Init bucket ownership
      addBucketOwners(BUCKET_OWNER_COUNT);

      // Prepare
      final int bucketOwnershipSizeBefore = bucketOwnershipAssignment.getBucketOwnerCount();

      // Remove owner
      final ClusterNodeAddress addr = TestUtils.createTestAddress(BUCKET_OWNER_COUNT / 2);
      bucketOwnershipAssignment.removeBucketOwners(CollectionUtils.createList(addr));

      // Assert
      assertEquals(bucketOwnershipSizeBefore - 1, bucketOwnershipAssignment.getBucketOwnerCount());
      assertEquals(0, eventListener.getBeginTransferCommands().size());
      assertEquals(0, eventListener.getBeginTransferCommandsBucketCount(0));
      assertEquals(2, eventListener.getRestoreBucketCommands().size());
      assertEquals(0, eventListener.getBeginRestoreReplicaCommands().size());

      final int bucketCount = bucketOwnershipAssignment.getBucketCount();
      for (int n = 0; n < bucketCount; n++) {
         assertNotNull("Found hole in bucket assignment at: " + n, bucketOwnershipAssignment.getPrimaryOwnerAddress(n));
      }
      assertBucketsAreSafe();
   }


   public void testRemoveBucketOwnerInitiatesReplicaRestore() {

      // Init bucket ownership to just enough to hold replicas
      final int bucketOwnerCount = REPLICA_COUNT + 2;
      addBucketOwners(bucketOwnerCount);

      // Prepare
      final int bucketOwnershipSizeBefore = bucketOwnershipAssignment.getBucketOwnerCount();

      // Remove owner
      final ClusterNodeAddress addr = TestUtils.createTestAddress(bucketOwnerCount / 2);
      bucketOwnershipAssignment.removeBucketOwners(CollectionUtils.createList(addr));

      // Assert
      assertEquals(bucketOwnershipSizeBefore - 1, bucketOwnershipAssignment.getBucketOwnerCount());
      assertEquals(11, eventListener.getBeginRestoreReplicaCommands().size());
      assertEquals(1384, eventListener.getBeginRestoreReplicaCommandsBucketCount());
      assertEquals(4, eventListener.getRestoreBucketCommands().size());
      assertEquals(515, eventListener.getRestoreBucketCommandsBucketCount());
      assertEquals(2, eventListener.getBeginTransferCommands().size());
      assertEquals(162, eventListener.getBeginTransferCommandsBucketCount(0));

      final int bucketCount = bucketOwnershipAssignment.getBucketCount();
      for (int n = 0; n < bucketCount; n++) {
         assertNotNull("Found hole in bucket assignment at: " + n, bucketOwnershipAssignment.getPrimaryOwnerAddress(n));
      }
      assertBucketsAreSafe();
   }


   public void testRemoveProducesCancelTransferCommands() {

      // Init bucket ownership
      addBucketOwners(BUCKET_OWNER_COUNT);

      // Add one more owner
      final ClusterNodeAddress addr = TestUtils.createTestAddress(BUCKET_OWNER_COUNT);
      addBucketOwnerAndRepartition(addr);

      bucketOwnershipAssignment.removeBucketOwners(CollectionUtils.createList(addr));

      // Assert
      assertEquals(1361, eventListener.getCancelTransferCommandsBucketCount());
   }


   public void testProcessRejectAnnouncement() {

      // Init bucket ownership
      addBucketOwners(BUCKET_OWNER_COUNT);

      // Add one more newOwner
      final ClusterNodeAddress addr = TestUtils.createTestAddress(BUCKET_OWNER_COUNT);
      addBucketOwnerAndRepartition(addr);

      // Get transfer command to cancel
      final BeginBucketTransferCommand beginTransferCommand = eventListener.getBeginTransferCommands().get(0);
      final Set<Integer> transfers = new HashSet<Integer>(beginTransferCommand.getBucketNumbers());

      final ClusterNodeAddress currentOwner = beginTransferCommand.getCurrentOwner();
      final ClusterNodeAddress newOwner = beginTransferCommand.getNewOwner();
      final byte sourceStorageNumber = beginTransferCommand.getSourceStorageNumber();
      final byte destinationStorageNumber = beginTransferCommand.getDestinationStorageNumber();
      final Collection<Integer> bucketNumbers = beginTransferCommand.getBucketNumbers();
      bucketOwnershipAssignment.rejectBucketTransfer(sourceStorageNumber, destinationStorageNumber, currentOwner, newOwner, bucketNumbers);

      // Assert
      assertEquals(1, eventListener.getCancelTransferCommands().size());
      assertEquals(transfers, new HashSet<Integer>(eventListener.getCancelTransferCommands().get(0).getBucketNumbers()));
      assertBucketsAreSafe();
   }


   public void testFinishTransferCommands() {

      // Init bucket ownership
      addBucketOwners(BUCKET_OWNER_COUNT);

      // Produce transfer command
      final ClusterNodeAddress addr = TestUtils.createTestAddress(BUCKET_OWNER_COUNT);
      addBucketOwnerAndRepartition(addr);

      // Complete transfer
      completeTransfer(eventListener.getBeginTransferCommands().get(0));

      // Assert
      assertEquals(170, eventListener.getFinishTransferCommands().size());
   }


   public void testSerializeDeserialize() throws IOException {

      addBucketOwners(BUCKET_OWNER_COUNT);
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = ser.serialize(bucketOwnershipAssignment);
      assertEquals(bucketOwnershipAssignment, ser.deserialize(bytes));
   }


   @SuppressWarnings("InstanceMethodNamingConvention")
   public void testRepartitioningOnAllEmptyOwnersProducesBalancedDistribution() {

      for (byte ownerIndex = 0; ownerIndex < BUCKET_OWNER_COUNT; ownerIndex++) {
         bucketOwnershipAssignment.addBucketOwner(TestUtils.createTestAddress(ownerIndex));
      }

      bucketOwnershipAssignment.repartition();
      executePendingCompletionCommands();
   }


   public void testLeavingNodeGetsRidOfBucketsCompletely() {

      // Init bucket ownership
      addBucketOwners(BUCKET_OWNER_COUNT);

      // Remove address
      final ClusterNodeAddress leavingAddress = TestUtils.createTestAddress(BUCKET_OWNER_COUNT / 2);
      bucketOwnershipAssignment.markBucketOwnerLeaving(leavingAddress);

      // Repartition. This may produce incomplete surrender of buckets owned by leaving nodes.
      bucketOwnershipAssignment.repartition();
      assertEquals(2051, this.eventListener.getOrphanBucketCommands().size());

      executePendingCompletionCommands();

      // Assert that all are gone
      assertFalse(bucketOwnershipAssignment.hasBucketResponsibilities(leavingAddress));
   }


   private void addBucketOwners(final int bucketOwnerCount) {

      for (int i = 0; i < bucketOwnerCount; i++) {
         addBucketOwnerAndRepartition(TestUtils.createTestAddress(i));
         executePendingCompletionCommands();
      }

      assertNoPendingCommands();
   }


   private void addBucketOwnerAndRepartition(final ClusterNodeAddress addr) {

      bucketOwnershipAssignment.addBucketOwner(addr);
      bucketOwnershipAssignment.repartition();
   }


   private void executePendingCompletionCommands() {

      while (!eventListener.getBeginTransferCommands().isEmpty() || !eventListener.getBeginRestoreReplicaCommands().isEmpty()) {

         final LinkedList<BeginBucketTransferCommand> transferCommands = eventListener.getBeginTransferCommands();
         while (!transferCommands.isEmpty()) {

            completeTransfer(transferCommands.removeFirst());
         }
         eventListener.getFinishTransferCommands().clear();

         while (!eventListener.getBeginRestoreReplicaCommands().isEmpty()) {

            completeReplicaRestore(eventListener.getBeginRestoreReplicaCommands().removeFirst());
         }
         eventListener.getFinishReplicaRestoreCommands().clear();

         //
         eventListener.getOrphanBucketCommands().clear();
         eventListener.getAssignBucketCommands().clear();
      }
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
    * Simulates receiver announcing that a transfer is complete.
    *
    * @param command the command.
    */
   private void completeTransfer(final BeginBucketTransferCommand command) {

      final ClusterNodeAddress currentOwner = command.getCurrentOwner();
      final ClusterNodeAddress newOwner = command.getNewOwner();
      final byte sourceStorageNumber = command.getSourceStorageNumber();
      final byte destinationStorageNumber = command.getDestinationStorageNumber();
      final Collection<Integer> bucketNumbers = command.getBucketNumbers();
      for (final Integer bucketNumber : bucketNumbers) {
         bucketOwnershipAssignment.finishBucketTransfer(sourceStorageNumber, destinationStorageNumber, currentOwner, newOwner, Collections.singletonList(bucketNumber));
         assertBucketIsSafe(bucketNumber);
      }
   }


   /**
    * Simulates receiver announcing that a transfer is complete.
    *
    * @param command the command.
    */
   private void completeReplicaRestore(final BeginBucketTransferCommand command) {

      final Collection<Integer> bucketNumbers = command.getBucketNumbers();
      for (final Integer bucketNumber : bucketNumbers) {
         final ClusterNodeAddress primaryOwner = command.getCurrentOwner();
         final ClusterNodeAddress replicaOwner = command.getNewOwner();
         final byte replicaStorageNumber = command.getDestinationStorageNumber();
         Assert.assertTrue(!primaryOwner.equals(replicaOwner), "Primary cannot be the same as replica");
         bucketOwnershipAssignment.finishBucketTransfer((byte) 0, replicaStorageNumber, primaryOwner, replicaOwner, Collections.singletonList(bucketNumber));
         assertBucketIsSafe(bucketNumber);
      }
   }


   private void assertBucketsAreSafe() {

      for (int bucketNumber = 0; bucketNumber < BUCKET_COUNT; bucketNumber++) {
         assertBucketIsSafe(bucketNumber);
      }
   }


   /**
    * @param bucketNumber the bucket number to assert for safety.
    * @noinspection NumericCastThatLosesPrecision
    */
   private void assertBucketIsSafe(final int bucketNumber) {

      for (byte storageNumber1 = 0; storageNumber1 <= REPLICA_COUNT; storageNumber1++) {
         final ClusterNodeAddress owner1 = bucketOwnershipAssignment.getBucketOwnerAddress(storageNumber1, bucketNumber);
         for (byte storageNumber2 = (byte) (storageNumber1 + 1); storageNumber2 <= REPLICA_COUNT; storageNumber2++) {
            final ClusterNodeAddress owner2 = bucketOwnershipAssignment.getBucketOwnerAddress(storageNumber2, bucketNumber);
            if (owner1 == null || owner2 == null) {
               continue;
            }
            assertTrue("Owners should not be equal" + ", bucket=" + bucketNumber + ", owner1=" + owner1.getTcpPort()
                    + '@' + storageNumber1 + ", owner2=" + owner2.getTcpPort() + '@' + storageNumber2,
                    !owner1.equals(owner2));
         }
      }
   }


   /**
    * @noinspection ProhibitedExceptionDeclared
    */
   protected void setUp() throws Exception {

      super.setUp();
      final BucketEventListenerList listenerList = new BucketEventListenerList();
      listenerList.add(eventListener);
      bucketOwnershipAssignment.attachListeners(listenerList);
   }


   public String toString() {

      return "BucketOwnershipAssignmentWithReplicasTest{" +
              "bucketOwnershipAssigment=" + bucketOwnershipAssignment +
              ", eventListener=" + eventListener +
              "} " + super.toString();
   }
}