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

import java.util.LinkedList;

import org.cacheonix.impl.util.logging.Logger;

/**
 * TestBucketEventListener
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Dec 2, 2009 12:47:30 AM
 */
final class TestBucketEventListener implements BucketEventListener {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TestBucketEventListener.class); // NOPMD

   private final LinkedList<BeginBucketTransferCommand> beginRestoreReplicaCommands = new LinkedList<BeginBucketTransferCommand>();  // NOPMD

   private final LinkedList<BeginBucketTransferCommand> beginTransferCommands = new LinkedList<BeginBucketTransferCommand>();   // NOPMD

   private final LinkedList<CancelBucketTransferCommand> cancelReplicaRestoreCommands = new LinkedList<CancelBucketTransferCommand>();  // NOPMD

   private final LinkedList<CancelBucketTransferCommand> cancelTransferCommands = new LinkedList<CancelBucketTransferCommand>();  // NOPMD

   private final LinkedList<FinishBucketTransferCommand> finishReplicaRestoreCommands = new LinkedList<FinishBucketTransferCommand>();  // NOPMD

   private final LinkedList<FinishBucketTransferCommand> finishTransferCommands = new LinkedList<FinishBucketTransferCommand>();  // NOPMD

   private final LinkedList<RestoreBucketCommand> restoreBucketCommands = new LinkedList<RestoreBucketCommand>();  // NOPMD

   private final LinkedList<OrphanBucketCommand> orphanBucketCommands = new LinkedList<OrphanBucketCommand>();  // NOPMD

   private final LinkedList<AssignBucketCommand> assignBucketCommands = new LinkedList<AssignBucketCommand>();  // NOPMD


   public final void execute(final BeginBucketTransferCommand command) {

      if (command.getSourceStorageNumber() == 0 && command.getDestinationStorageNumber() > 0) {

         beginRestoreReplicaCommands.add(command);
      } else {

         beginTransferCommands.addLast(command);
      }
   }


   public final void execute(final FinishBucketTransferCommand command) {

      if (command.getSourceStorageNumber() == 0 && command.getDestinationStorageNumber() > 0) {

         finishReplicaRestoreCommands.addLast(command);
      } else {
         finishTransferCommands.addLast(command);
      }
   }


   public final void execute(final CancelBucketTransferCommand command) {

      if (command.getSourceStorageNumber() == 0 && command.getDestinationStorageNumber() > 0) {
         cancelReplicaRestoreCommands.addLast(command);

      } else {
         cancelTransferCommands.addLast(command);
      }
   }


   public final void execute(final RestoreBucketCommand command) {

      restoreBucketCommands.addLast(command);
   }


   public void execute(final OrphanBucketCommand command) {

      orphanBucketCommands.add(command);
   }


   public void execute(final AssignBucketCommand command) {

      assignBucketCommands.add(command);
   }


   public final LinkedList<BeginBucketTransferCommand> getBeginTransferCommands() { // NOPMD
      return beginTransferCommands;
   }


   public final int getBeginTransferCommandsBucketCount(final int storageNumber) {

      int result = 0;
      for (final BeginBucketTransferCommand beginTransferCommand : beginTransferCommands) {
         if (beginTransferCommand.getSourceStorageNumber() == storageNumber) {
            result += beginTransferCommand.getBucketNumbers().size();
         }
      }
      return result;
   }


   public final LinkedList<FinishBucketTransferCommand> getFinishTransferCommands() { // NOPMD
      return finishTransferCommands;
   }


   public final LinkedList<FinishBucketTransferCommand> getFinishReplicaRestoreCommands() { // NOPMD
      return finishReplicaRestoreCommands;
   }


   public final LinkedList<CancelBucketTransferCommand> getCancelTransferCommands() { // NOPMD
      return cancelTransferCommands;
   }


   public int getCancelTransferCommandsBucketCount() {

      int result = 0;
      for (final CancelBucketTransferCommand cancelBucketTransferCommand : cancelTransferCommands) {
         result += (cancelBucketTransferCommand).getBucketNumbers().size();
      }
      return result;
   }


   public int getBeginRestoreReplicaCommandsBucketCount() {


      int result = 0;
      for (final BeginBucketTransferCommand command : beginRestoreReplicaCommands) {
         result += (command).getBucketNumbers().size();
      }
      return result;
   }


   public int getRestoreBucketCommandsBucketCount() {

      int result = 0;
      for (final RestoreBucketCommand command : restoreBucketCommands) {
         result += (command).getBucketNumbers().size();
      }
      return result;
   }


   public LinkedList<OrphanBucketCommand> getOrphanBucketCommands() { // NOPMD

      return orphanBucketCommands;
   }


   public LinkedList<AssignBucketCommand> getAssignBucketCommands() { // NOPMD

      return assignBucketCommands;
   }


   public final LinkedList<RestoreBucketCommand> getRestoreBucketCommands() { // NOPMD
      return restoreBucketCommands;
   }


   public final LinkedList<BeginBucketTransferCommand> getBeginRestoreReplicaCommands() { // NOPMD
      return beginRestoreReplicaCommands;
   }


   public final String toString() {

      return "TestBucketEventListener{" +
              "beginRestoreReplicaCommands=" + beginRestoreReplicaCommands.size() +
              ", beginTransferCommands=" + beginTransferCommands.size() +
              ", cancelReplicaRestoreCommands=" + cancelReplicaRestoreCommands.size() +
              ", cancelTransferCommands=" + cancelTransferCommands.size() +
              ", finishReplicaRestoreCommands=" + finishReplicaRestoreCommands.size() +
              ", finishTransferCommands=" + finishTransferCommands.size() +
              ", restoreBucketCommands=" + restoreBucketCommands.size() +
              '}';
   }
}
